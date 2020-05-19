
#include <bluefruit.h>
#include <Adafruit_LittleFS.h>
//#include <InternalFileSystem.h>

#define Vc 4.95
#define R0 10.00    //32.00

#define FAN_PIN       5
#define VOC_HCHO_PIN  A0

// BLE Service
BLEDfu  bledfu;  // OTA DFU service
BLEDis  bledis;  // device information
BLEUart bleuart; // uart over ble
BLEBas  blebas;  // battery

String recvData = "";
byte dustCount = 0;
unsigned char dustData[30];

const int duration = 1000;
unsigned long pre_time = 0;
unsigned long cur_time = 0;

float VOC_STD_VALUE = 1.0;
float vocValue;
bool fanState = false;
bool autoState = false;
bool isConnected = false;

void setup()
{
  Serial.begin(115200);

  Bluefruit.autoConnLed(true);
  Bluefruit.configPrphBandwidth(BANDWIDTH_MAX);

  Bluefruit.begin();
  Bluefruit.setTxPower(4);
  Bluefruit.setName("Bluefruit52");
  Bluefruit.Periph.setConnectCallback(connect_callback);
  Bluefruit.Periph.setDisconnectCallback(disconnect_callback);

  bledfu.begin();
  bledis.setManufacturer("Adafruit Industries");
  bledis.setModel("Bluefruit Feather52");
  bledis.begin();

  bleuart.begin();

  blebas.begin();
  blebas.write(100);

  startAdv();

  pinMode(FAN_PIN, OUTPUT);
}

void startAdv(void)
{
  Bluefruit.Advertising.addFlags(BLE_GAP_ADV_FLAGS_LE_ONLY_GENERAL_DISC_MODE);
  Bluefruit.Advertising.addTxPower();
  Bluefruit.Advertising.addService(bleuart);
  Bluefruit.ScanResponse.addName();

  Bluefruit.Advertising.restartOnDisconnect(true);
  Bluefruit.Advertising.setInterval(32, 244);    // in unit of 0.625 ms
  Bluefruit.Advertising.setFastTimeout(30);      // number of seconds in fast mode
  Bluefruit.Advertising.start(0);                // 0 = Don't stop advertising after n seconds
}

void connect_callback(uint16_t conn_handle)
{
  // Get the reference to current connection
  BLEConnection* connection = Bluefruit.Connection(conn_handle);
  char central_name[32] = { 0 };
  connection->getPeerName(central_name, sizeof(central_name));
  Serial.print("Connected to ");
  Serial.println(central_name);
  isConnected = true;
}

void disconnect_callback(uint16_t conn_handle, uint8_t reason)
{
  (void) conn_handle;
  (void) reason;
  Serial.println();
  Serial.print("Disconnected, reason = 0x"); Serial.println(reason, HEX);
  isConnected = false;
}


void loop()
{
  while ( bleuart.available() )
  {
    recvData += (char) bleuart.read();
  }

  if (recvData != "") {
    Serial.print("> Recv Msg : ");
    Serial.println(recvData);
    receiveHandler();
    recvData = "";
  }

  if (millis() - pre_time >= duration) {
    measureVOCSensor();
    if (autoState) {
      autoControl();
    }
    if (isConnected) {
      String msg = String(autoState) + String(fanState) + String(vocValue);
      bleuart.println(msg);
    }
    pre_time = millis();
  }
}

void receiveHandler() {
  if (recvData.startsWith("A")) {
    autoState = recvData.substring(1, recvData.length()).toInt();
    Serial.print(F("# Setup Auto Mode - "));
    Serial.println(autoState);
  } else {
    fanState = recvData.substring(1, recvData.length()).toInt();
    Serial.print(F("# Setup Fan Status - "));
    Serial.println(fanState);
    digitalWrite(FAN_PIN, fanState);
  }
}

void autoControl() {
  //http://ojtale.egloos.com/3185297
  //https://colagom93.tistory.com/13

  int fanStart = vocValue > VOC_STD_VALUE;

  if (fanStart != fanState) {
    fanState = fanStart;
    Serial.print(F("# Change Fan Status - "));
    Serial.println(fanState);
    digitalWrite(FAN_PIN, fanState);
  }
}

void measureVOCSensor() {
  // http://wiki.seeedstudio.com/Grove-HCHO_Sensor/
  double Rs = (1023.0 / analogRead(VOC_HCHO_PIN)) - 1;
  double ppm = pow(10.0, ((log10(Rs / R0) - 0.0827) / (-0.4807)));

  Serial.print(F("# [VOC] HCHO > "));
  Serial.print(Rs);
  Serial.print(F(" (Rs), "));
  Serial.print(ppm);
  Serial.println(F(" (ppm)"));
  vocValue = (float)ppm;
}
