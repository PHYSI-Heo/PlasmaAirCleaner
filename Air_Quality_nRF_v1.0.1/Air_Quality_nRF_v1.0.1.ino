
#include <bluefruit.h>
#include <Adafruit_LittleFS.h>
//#include <InternalFileSystem.h>

#define Vc 4.95
#define R0 9.13

#define VOC_HCHO_PIN    A0
#define FAN_PIN         5
#define PLASMA1_PIN     12
#define PLASMA2_PIN     13
#define R_LED_PIN       6
#define G_LED_PIN       9
#define B_LED_PIN       10
#define BTN_PIN         11


#define FILTER_BUF_SIZE     5

// BLE Service
BLEDfu  bledfu;  // OTA DFU service
BLEDis  bledis;  // device information
BLEUart bleuart; // uart over ble

// Timer
const int vocSensingInterval = 1000;
unsigned long vocSensingTime = 0;
const int plasmaBlinkInterval = 3000;
unsigned long plasmaBlinkTime = 0;

// VOC Stage
const float VOC_NORMAL_MIN_VALUE = 1.0;
const float VOC_NORMAL_MAX_VALUE = 6.0;

// VOC Value
float vocBuf[FILTER_BUF_SIZE];
int vocBufCnt = -1;
float avgVoc = 0;

// Analog noise filter - https://en.wikipedia.org/wiki/Moving_average
float EMA_Alpha = 0.6;
int EMA_S = 0;

// Power variable
const int PW_SWITCH_CNT = 10;
int btnPressureCnt = 0;
bool pwState = false;

// BLE variable
String recvBleData = "";
bool isBleConnected = false;

// RemoteControl variable
bool cleanState = false;
bool autoConState = false;


/*
    nRF Bluetooth Le
*/
void startAdv(bool enable)
{
  if (enable) {
    Bluefruit.Advertising.addFlags(BLE_GAP_ADV_FLAGS_LE_ONLY_GENERAL_DISC_MODE);
    Bluefruit.Advertising.addTxPower();
    Bluefruit.Advertising.addService(bleuart);
    Bluefruit.ScanResponse.addName();

    Bluefruit.Advertising.restartOnDisconnect(true);
    Bluefruit.Advertising.setInterval(32, 244);    // in unit of 0.625 ms
    Bluefruit.Advertising.setFastTimeout(30);      // number of seconds in fast mode
    Bluefruit.Advertising.start(0);                // 0 = Don't stop advertising after n seconds
  } else {
    Bluefruit.Advertising.stop();
  }
}

void connect_callback(uint16_t conn_handle)
{
  // Get the reference to current connection
  BLEConnection* connection = Bluefruit.Connection(conn_handle);
  char central_name[32] = { 0 };
  connection->getPeerName(central_name, sizeof(central_name));
  Serial.print("> Connected to ");
  Serial.println(central_name);
  isBleConnected = true;
}

void disconnect_callback(uint16_t conn_handle, uint8_t reason)
{
  (void) conn_handle;
  (void) reason;
  Serial.println();
  Serial.print("> Disconnected, reason = 0x"); Serial.println(reason, HEX);
  isBleConnected = false;
}

/*
    Voc Measure & Filter
*/
int analogNoiseFilter(int val) {
  EMA_S = (EMA_Alpha * val) + ((1 - EMA_Alpha) * EMA_S);
  return EMA_S;
}

//float dataFilter(int& cnt, float buf[], float val) {
//  float totalVal = val;
//  if (cnt >= FILTER_BUF_SIZE) {
//    for (int i = 1; i < FILTER_BUF_SIZE; i++) {
//      totalVal += buf[i];
//      buf[i - 1] = buf[i];
//    }
//    buf[FILTER_BUF_SIZE - 1] = val;
//  } else {
//    for (int i = 0; i < cnt; i++) {
//      totalVal += buf[i];
//    }
//    buf[cnt++] = val;
//  }
//  return totalVal / cnt;
//}

float dataFilter_Cut(int& cnt, float buf[], float val) {
  float totalVal = val;
  if (cnt == FILTER_BUF_SIZE) {
    for (int i = 0; i < FILTER_BUF_SIZE; i++) {
      totalVal += buf[i];
    }
    cnt = 0;
    return totalVal / FILTER_BUF_SIZE;
  } else if (cnt == -1) {
    buf[++cnt] = val;
    return val;
  } else {
    buf[cnt++] = val;
    return -1;
  }
}

void measureVOCSensor() {
  // http://wiki.seeedstudio.com/Grove-HCHO_Sensor/
  double Rs = (1023.0 / analogNoiseFilter(analogRead(VOC_HCHO_PIN))) - 1;
  //  double Rs = (1023.0 / analogRead(VOC_HCHO_PIN)) - 1;
  double ppm = pow(10.0, ((log10(Rs / R0) - 0.0827) / (-0.4807)));
  float avg = dataFilter_Cut(vocBufCnt, vocBuf, (float)ppm);
  avgVoc = avg < 0 ? avgVoc : avg;

  Serial.print(F("# Voc : "));
  Serial.print(Rs);
  Serial.print(F(" (Rs),\t"));
  Serial.print(ppm);
  Serial.print(F(" (ppm),\t Avg : "));
  Serial.print(avgVoc);
  Serial.println(F(" (ppm)"));

  delay(10);
}

/*
    Led Control
*/
void showLed(bool red, bool green, bool blue) {
  digitalWrite(R_LED_PIN, red);
  digitalWrite(G_LED_PIN, green);
  digitalWrite(B_LED_PIN, blue);
}

void powerEffect() {
  for (int i = 0; i < 8; i++) {
    showLed(true, false, false);
    delay(50);
    showLed(false, true, false);
    delay(50);
    showLed(false, false, true);
    delay(50);
  }
}

void showVocStage() {
  if (avgVoc < VOC_NORMAL_MIN_VALUE) {
    showLed(false, true, false);
  } else if (avgVoc > VOC_NORMAL_MAX_VALUE) {
    showLed(true, false, false);
  } else {
    showLed(true, true, false);
  }
}


/*
    H/W Power Control
*/
void powerControl() {
  if (digitalRead(BTN_PIN)) {
    Serial.print(F("> Button Click."));
    while (digitalRead(BTN_PIN)) {
      btnPressureCnt++;
      Serial.print(".");
      delay(100);
    }
    Serial.println();

    if (btnPressureCnt > PW_SWITCH_CNT) {
      pwState = !pwState;
      Serial.print(F("# Power Enable : "));
      Serial.println(pwState);
      startAdv(pwState);
      if (!pwState) {
        // off
        showLed(false, false, false);
      }else{
        powerEffect();
      }
    }
    btnPressureCnt = 0;
  }
}

/*
    Cleaner State Control
*/
void setCleaner() {
  digitalWrite(FAN_PIN, cleanState);
  digitalWrite(PLASMA1_PIN, cleanState);
  if (!cleanState) {
    // Cleaner Stop
    digitalWrite(PLASMA2_PIN, cleanState);    
  } 
  sendCleanerState();
  delay(10);
}

void remoteDataHandler() {
  while ( bleuart.available() )
  {
    recvBleData += (char) bleuart.read();
  }

  if (recvBleData != "") {
    Serial.print("> Receive Message : ");
    Serial.println(recvBleData);

    if (recvBleData.startsWith("A")) {
      autoConState = recvBleData.substring(1, recvBleData.length()).toInt();
      Serial.print(F("Auto Control State : "));
      Serial.println(autoConState);
    } else {
      cleanState = recvBleData.substring(1, recvBleData.length()).toInt();
      Serial.print(F("Cleaner State : "));
      Serial.println(cleanState);
      setCleaner();
    }
    recvBleData = "";
  }
}


void autoController() {
  //http://ojtale.egloos.com/3185297
  //https://colagom93.tistory.com/13

  if (autoConState) {
    int isCleanStart = avgVoc > VOC_NORMAL_MIN_VALUE;
    if (cleanState != isCleanStart) {
      cleanState = isCleanStart;
      Serial.print(F("(Auto) Cleaner State : "));
      Serial.println(cleanState);      
      setCleaner();
    }
  }
  delay(5);
}


void setup()
{
  Serial.begin(115200);

  // Setup Ble
  Bluefruit.autoConnLed(true);
  Bluefruit.configPrphBandwidth(BANDWIDTH_MAX);
  Bluefruit.begin();
  Bluefruit.setTxPower(4);
  Bluefruit.setName("Anicoco");
  Bluefruit.Periph.setConnectCallback(connect_callback);
  Bluefruit.Periph.setDisconnectCallback(disconnect_callback);
  bledfu.begin();
  bledis.setManufacturer("Adafruit Industries");
  bledis.setModel("Bluefruit Feather52");
  bledis.begin();
  bleuart.begin();

  // Setup Pin
  pinMode(FAN_PIN, OUTPUT);
  pinMode(PLASMA1_PIN, OUTPUT);
  pinMode(PLASMA2_PIN, OUTPUT);
  pinMode(R_LED_PIN, OUTPUT);
  pinMode(G_LED_PIN, OUTPUT);
  pinMode(B_LED_PIN, OUTPUT);
  pinMode(BTN_PIN, INPUT);
  digitalWrite(FAN_PIN, LOW);
  digitalWrite(PLASMA1_PIN, LOW);
  digitalWrite(PLASMA2_PIN, LOW);
  digitalWrite(R_LED_PIN, LOW);
  digitalWrite(G_LED_PIN, LOW);
  digitalWrite(B_LED_PIN, LOW);

  EMA_S = analogRead(VOC_HCHO_PIN);
}


void loop()
{
  powerControl();
  remoteDataHandler();

  if (pwState) {
    if (millis() - vocSensingTime >= vocSensingInterval) {
      measureVOCSensor();
      showVocStage();
      autoController();      
      sendCleanerState();
      vocSensingTime = millis();
    }

    if (cleanState && millis() - plasmaBlinkTime >= plasmaBlinkInterval) {
      digitalWrite(PLASMA2_PIN, !digitalRead(PLASMA2_PIN));
      plasmaBlinkTime = millis();
    }
  }
  delay(50);
}


void sendCleanerState() {
  if (isBleConnected) {
    String msg = String(autoConState) + String(cleanState) + String(avgVoc);
    bleuart.println(msg);
  }
}
