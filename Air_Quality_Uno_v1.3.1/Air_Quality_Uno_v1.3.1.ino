#include <SoftwareSerial.h>
#include <U8glib.h>
#include <Adafruit_NeoPixel.h>
#ifdef __AVR__
#include <avr/power.h> // Required for 16 MHz Adafruit Trinket
#endif
#include <Wire.h>

#define VOC_HCHO_PIN        A0
#define DUST_PMS7003_RX     10
#define DUST_PMS7003_TX     11
#define BTN_PIN             7
#define BTN_LED_PIN         8
#define LED_PIN             9
#define FAN_PIN             6
#define FAN_PW_PIN          5
#define BUZZER_PIN          2
#define PLASMA1_PIN         3
#define PLASMA2_PIN         4
#define OLED_ADDR           0x3C

#define LED_PIXELs_SIZE   24
#define Vc 4.95
#define R0 25.92

#define GOOD_VOC_PPM      2
#define NORMAL_VOC_PPM    6
#define BAD_VOC_PPM       10

#define GOOD_DUST_PPM     15
#define NORMAL_DUST_PPM   35
#define BAD_DUST_PPM      75

#define CLEAN_MODE_AUTO     0
#define CLEAN_MODE_LOW      1
#define CLEAN_MODE_NORMAL   2
#define CLEAN_MODE_HIGH     3
#define CLEAN_MODE_FULL     4

#define FAN_SPEED_ZERO    0
#define FAN_SPEED_SLOW    1
#define FAN_SPEED_NORMAL  2
#define FAN_SPEED_FAST    3
#define FAN_SPEED_FULL    4

#define BUZZER_POWER_ON     0
#define BUZZER_POWER_OFF    1
#define BUZZER_FAN_CONTROL  2

#define LED_GOOD_COLOR      12
#define LED_NORMAL_COLOR    6
#define LED_BAD_COLOR       0

SoftwareSerial dSerial(DUST_PMS7003_RX, DUST_PMS7003_TX);
//U8GLIB_SH1106_128X64 u8g(U8G_I2C_OPT_NONE);  // I2C / TWI 
U8GLIB_SH1106_128X64 u8g(U8G_I2C_OPT_DEV_0|U8G_I2C_OPT_FAST); // Dev 0, Fast I2C / TWI
//U8GLIB_SH1106_128X64 u8g(U8G_I2C_OPT_NO_ACK); // Display which does not send ACK
Adafruit_NeoPixel pixels = Adafruit_NeoPixel(LED_PIXELs_SIZE, LED_PIN, NEO_GRB + NEO_KHZ800);

const uint32_t colors[] = {
  pixels.Color(240, 0, 15), pixels.Color(248, 0, 7), pixels.Color(255, 0, 0),
  pixels.Color(240, 15, 0), pixels.Color(225, 30, 0), pixels.Color(212, 38, 0),
  pixels.Color(192, 63, 0), pixels.Color(176, 79, 0), pixels.Color(159, 96, 0),
  pixels.Color(144, 111, 0), pixels.Color(129, 126, 0), pixels.Color(113, 81, 0),
  pixels.Color(96, 159, 0), pixels.Color(80, 175, 0), pixels.Color(63, 192, 0),
  pixels.Color(48, 207, 0), pixels.Color(33, 222, 0), pixels.Color(16, 239, 0),
  pixels.Color(0, 255, 0), pixels.Color(0, 222, 33), pixels.Color(0, 192, 63),
  pixels.Color(0, 159, 96), pixels.Color(0, 126, 129), pixels.Color(0, 96, 159),
  pixels.Color(0, 63, 192), pixels.Color(0, 30, 225), pixels.Color(0, 0, 255),
  pixels.Color(33, 0, 222), pixels.Color(66, 0, 189), pixels.Color(96, 0, 159),
  pixels.Color(129, 0, 126), pixels.Color(162, 0, 93), pixels.Color(192, 0, 63),
};

const int sensingInterval = 3000;
unsigned long sensingTime = 0;
const int plasmaToggleInterval = 3000;
unsigned long plasmaToggleTime = 0;

byte dustCount = 0;
unsigned char dustData[30];
float vocValue;
int dustValue;
String vocStr = "000", dustStr = "000";

/*
   Long Click - Power(Loop Enable) On/Off, One Click - Mode Change
   1 - Auto Mode, 2 - Low Speed, 3 - Normal Speed, 4 - High Speed
*/
int btnPressureCnt = 0;
bool cleanEnable = false;
int cleanMode = 0;
int fanSpeed = 0;
int autoSpeed = 0;

int ledPos = 0;
int colorRate;

bool isDiscoveryI2C = true;


String dataLengthPad(int value) {
  String val = String(value);
  int len = val.length();
  if (len > 3)
    return "000";
  for (int i = 0; i < 3 - len; i++) {
    val = "0" + val;
  }
  return val;
}

void setup() {
  Serial.begin(9600);
  dSerial.begin(9600);
  pixels.begin();
  Wire.begin();

  pinMode(BTN_PIN, INPUT);
  pinMode(BTN_LED_PIN, OUTPUT);
  pinMode(FAN_PIN, OUTPUT);
  pinMode(FAN_PW_PIN, OUTPUT);
  pinMode(BUZZER_PIN, OUTPUT);
  pinMode(PLASMA1_PIN, OUTPUT);
  pinMode(PLASMA2_PIN, OUTPUT);

  digitalWrite(BTN_LED_PIN, LOW);
  digitalWrite(FAN_PW_PIN, LOW);
  digitalWrite(BUZZER_PIN, LOW);
  digitalWrite(PLASMA1_PIN, LOW);
  digitalWrite(PLASMA2_PIN, LOW);

  clearEffect();
  clearText();
}

void loop() {
  btnControl();

  accessI2C();

  if (cleanEnable) {
    long cMillis = millis();

    if (cMillis - sensingTime >= sensingInterval) {
      measureVOCSensor();
      measureDustSensor();
      setStatusConfig();
      if (cleanMode == CLEAN_MODE_AUTO && autoSpeed != fanSpeed) {
        setFanSpeed(autoSpeed);
      }
      showStatusText();
      sensingTime = cMillis;
    }

    if (cMillis - plasmaToggleTime >= plasmaToggleInterval) {
      digitalWrite(PLASMA2_PIN, !digitalRead(PLASMA2_PIN));
      plasmaToggleTime = cMillis;
    }
    statusEffect();
  }
  delay(75);
}



/*
   Cleaner Controller
*/
void btnControl() {
  if (digitalRead(BTN_PIN)) {
    while (digitalRead(BTN_PIN)) {
      btnPressureCnt++;
      delay(100);
    }
    if (btnPressureCnt < 10) {
      changeCleanMode();
    } else {
      swapCleanEnable();
    }
    btnPressureCnt = 0;
  }
}

void changeCleanMode() {
  if (cleanEnable) {
    cleanMode++;
    if (cleanMode > CLEAN_MODE_FULL) {
      cleanMode = CLEAN_MODE_AUTO;
    } else if (cleanMode == CLEAN_MODE_LOW) {
      setFanSpeed(FAN_SPEED_SLOW);
    } else if (cleanMode == CLEAN_MODE_NORMAL) {
      setFanSpeed(FAN_SPEED_NORMAL);
    } else if (cleanMode == CLEAN_MODE_HIGH) {
      setFanSpeed(FAN_SPEED_FAST);
    } else if (cleanMode == CLEAN_MODE_FULL) {
      setFanSpeed(FAN_SPEED_FULL);
    }
    showStatusText();
    buzzerEffect(BUZZER_FAN_CONTROL);
    clearEffect();
    delay(50);
  }
}

void swapCleanEnable() {
  cleanEnable = !cleanEnable;
  showLogoText();
  digitalWrite(BTN_LED_PIN, cleanEnable);
  buzzerEffect(cleanEnable ? BUZZER_POWER_ON : BUZZER_POWER_OFF);
  powerEffect(cleanEnable);
  digitalWrite(PLASMA1_PIN, cleanEnable);
  if (cleanEnable) {
    sensingTime = 0;
    cleanMode = CLEAN_MODE_AUTO;
    plasmaToggleTime = millis();
  } else {
    digitalWrite(PLASMA2_PIN, LOW);
    setFanSpeed(FAN_SPEED_ZERO);
    clearEffect();
    clearText();
  }
}

void setFanSpeed(int speed) {
  fanSpeed = speed;
  int pwm;
  if (fanSpeed == FAN_SPEED_ZERO) {
    pwm = 0;
  } else if (fanSpeed == FAN_SPEED_SLOW) {
    pwm = 10;
  } else if (fanSpeed == FAN_SPEED_NORMAL) {
    pwm = 100;
  } else if (fanSpeed == FAN_SPEED_FAST) {
    pwm = 140;
  } else {
    pwm = 200;
  }
  digitalWrite(FAN_PW_PIN, fanSpeed != FAN_SPEED_ZERO);
  analogWrite(FAN_PIN, pwm);
}

void setStatusConfig() {
  int speed;
  if (vocValue < GOOD_VOC_PPM && dustValue <= GOOD_DUST_PPM) {
    speed = FAN_SPEED_SLOW;
    colorRate = LED_GOOD_COLOR;
  } else if (vocValue > NORMAL_VOC_PPM || dustValue > NORMAL_DUST_PPM) {
    speed = FAN_SPEED_FAST;
    colorRate = LED_BAD_COLOR;
  } else {
    speed = FAN_SPEED_NORMAL;
    colorRate = LED_NORMAL_COLOR;
  }
  if (autoSpeed != speed) {
    autoSpeed = speed;
  }
}


/*
   Buzzer Controller
*/
void buzzerEffect(int state) {
  if (state == BUZZER_POWER_ON) {
    digitalWrite(BUZZER_PIN, HIGH);
    delay(100);
    digitalWrite(BUZZER_PIN, LOW);
    delay(100);
    digitalWrite(BUZZER_PIN, HIGH);
    delay(100);
    digitalWrite(BUZZER_PIN, LOW);
  } else if (state == BUZZER_POWER_OFF) {
    digitalWrite(BUZZER_PIN, HIGH);
    delay(600);
    digitalWrite(BUZZER_PIN, LOW);
  } else {
    digitalWrite(BUZZER_PIN, HIGH);
    delay(100);
    digitalWrite(BUZZER_PIN, LOW);
  }
}


/*
   Display OLED
*/
void accessI2C() {
  Wire.beginTransmission(OLED_ADDR);
  byte error = Wire.endTransmission();
  if (error != 0) {
    Serial.print(F("oled error : "));
    Serial.println(error);
    isDiscoveryI2C = false;
  } else if (!isDiscoveryI2C) {
    Serial.println(F("u8g begin."));
    u8g.begin();
    if (cleanEnable)
      showStatusText();
    else
      clearText();
    isDiscoveryI2C = true;
  }
}

void showStatusText() {
  u8g.firstPage();
  do {
    u8gPrepare();
    u8g.drawLine(4, 32, 124, 32);
    u8g.drawLine(36, 2, 36, 28);
    u8g.drawLine(36, 36, 36, 62);
    u8g.drawStr(107, 3, "VOC");
    u8g.drawStr(107, 38, "PM");
    u8g.drawCircle(18, 15, 12);
    u8g.drawTriangle(82, 26, 85, 32, 79, 32);

    u8g.setScale2x2();
    if (cleanMode == CLEAN_MODE_AUTO) {
      u8g.drawStr(7, 3, "A");
    } else {
      u8g.drawStr(7, 3, "M");
    }
    u8g.undoScale();
    u8g.drawCircle(18, 50, 3);
    if (fanSpeed >= FAN_SPEED_NORMAL)
      u8g.drawCircle(18, 50, 6);
    if (fanSpeed >= FAN_SPEED_FAST)
      u8g.drawCircle(18, 50, 9);
    if (fanSpeed >= FAN_SPEED_FULL)
      u8g.drawCircle(18, 50, 12);

    u8g.setFont(u8g_font_fur25n);
    u8g.drawStr(44, 28, vocStr.c_str());
    u8g.drawStr(44, 64, dustStr.c_str());
  } while (u8g.nextPage());
  delay(50);
}

void clearText() {
  u8g.firstPage();
  do {
  } while ( u8g.nextPage() );
}

void showLogoText() {
  u8g.firstPage();
  do {
    u8gPrepare();
    u8g.setScale2x2();
    u8g.drawStr(10, 12, "PURICOCO");
    u8g.undoScale();
  } while ( u8g.nextPage() );
}

void u8gPrepare(void) {
  u8g.setFont(u8g_font_6x10);
  u8g.setFontRefHeightExtendedText();
  u8g.setDefaultForegroundColor();
  u8g.setFontPosTop();
}


/*
   Sensing Data
*/
void measureVOCSensor() {
  // http://wiki.seeedstudio.com/Grove-HCHO_Sensor/
  double Rs = (1023.0 / analogRead(VOC_HCHO_PIN)) - 1;
  double ppm = pow(10.0, ((log10(Rs / R0) - 0.0827) / (-0.4807)));
  Serial.print(F("Rs : "));
  Serial.print(Rs);
  Serial.print(F("\tPPM : "));
  Serial.println(ppm);
  vocValue = (float)ppm;
  String voc = dataLengthPad(int(vocValue * 10));
  if (!voc.equals("000")) {
    vocStr = voc;
  }
}

void measureDustSensor() {
  //https://makernambo.com/79
  if (dSerial.available()) {
    for (int i = 0; i < 32; i++) {
      char chrRecv = dSerial.read();
      if (chrRecv == 0x4d) {
        dustCount = 2;
        break;
      }
    }
    if (dustCount == 2)
    {
      dustCount = 0;
      for (int i = 0; i < 30; i++) {
        dustData[i] = dSerial.read();
      }

      if ( (unsigned int) dustData[27] == 0 ) {
        int PM25  = (dustData[10] << 8) + dustData[11];
        if (PM25 > 0 && PM25 < 1000) {
          dustValue = PM25;
          Serial.print(F("Dust : "));
          Serial.println(dustValue);
          String dust = dataLengthPad(dustValue);
          if (!dust.equals("000")) {
            dustStr = dust;
          }
        }
      }
    }
  }
}


/*
   LED Effect
*/
void powerEffect(bool enable) {
  if (enable) {
    rainbowEffect(true, 20);
  }
  for (int i = 0; i < 48; i++) {
    rainbowEffect(false, 0);
    delay(50);
  }
}

void clearEffect() {
  for (int i = 0; i < LED_PIXELs_SIZE; i ++) {
    pixels.setPixelColor(i,  pixels.Color(0, 0, 0));
  }
  pixels.show();
}

void statusEffect() {
  int pos = ledPos++;
  for (int i = 0; i < LED_PIXELs_SIZE; i ++) {
    if (pos >= LED_PIXELs_SIZE)
      pos = 0;
    pixels.setPixelColor(pos++, colors[(int)(i / 4) + colorRate]);
  }
  pixels.show();

  if (ledPos >= LED_PIXELs_SIZE)
    ledPos = 0;
}

void rainbowEffect(bool isDot, int delayTime) {
  int pos = ledPos++;
  for (int i = 0; i < LED_PIXELs_SIZE; i ++) {
    if (pos >= LED_PIXELs_SIZE)
      pos = 0;
    int colorIndex = i < 9 ? i * 2 : i + 9;
    pixels.setPixelColor(pos++, colors[colorIndex]);
    if (isDot) {
      pixels.show();
      delay(delayTime);
    }
  }
  if (!isDot)
    pixels.show();
  if (ledPos >= LED_PIXELs_SIZE)
    ledPos = 0;
}
