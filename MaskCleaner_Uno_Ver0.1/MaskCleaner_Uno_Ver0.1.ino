#define BTN_PIN           6
#define LED_PW_PIN        5
#define LED_STEP1_PIN     7
#define LED_STEP2_PIN     8
#define LED_STEP3_PIN     9
#define PLASMA_PIN        3
#define FAN_PIN           4

// Loop Cycler = 3 Sec
long LoopCycler = 3 * 1000;
long loopTime;

// Countdown Control
long CLEAN_TIME_CNT1 = 20 * 60;
long CLEAN_TIME_CNT2 = 20 * 120;
long CLEAN_TIME_CNT3 = 20 * 180;

int pressureCnt = 0;
bool motionState;
int cleanStep;
int timeCnt, finishCnt;

void setup() {
  Serial.begin(9600);
  Serial.println("Start..");

  pinMode(BTN_PIN, INPUT);
  pinMode(LED_PW_PIN, OUTPUT);
  pinMode(LED_STEP1_PIN, OUTPUT);
  pinMode(LED_STEP2_PIN, OUTPUT);
  pinMode(LED_STEP3_PIN, OUTPUT);
  pinMode(FAN_PIN, OUTPUT);

  pinMode(PLASMA_PIN, OUTPUT);

  digitalWrite(LED_STEP1_PIN, LOW);
  digitalWrite(LED_STEP2_PIN, LOW);
  digitalWrite(LED_STEP3_PIN, LOW);
  digitalWrite(LED_PW_PIN, LOW);
  digitalWrite(PLASMA_PIN, LOW);
  digitalWrite(FAN_PIN, LOW);

  motionState = false;
}

void loop() {
  btnControl();

  if (motionState && millis() - loopTime > LoopCycler) {
    digitalWrite(PLASMA_PIN, !digitalRead(PLASMA_PIN));
    timeCnt++;
    Serial.print(" Time Cnt : ");
    Serial.println(timeCnt);
    if (timeCnt >= finishCnt)
      setCleanerStatus(motionState = false);    // disable

    loopTime = millis();
  }

  delay(50);
}

void btnControl() {
  if (digitalRead(BTN_PIN)) {
    while (digitalRead(BTN_PIN)) {
      pressureCnt++;
      delay(100);
    }
    if (pressureCnt < 10) {
      if (!motionState) return;
      digitalWrite(cleanStep, LOW);
      if (cleanStep == LED_STEP1_PIN) {
        cleanStep = LED_STEP2_PIN;
        finishCnt = CLEAN_TIME_CNT2;
      } else if (cleanStep == LED_STEP2_PIN) {
        cleanStep = LED_STEP3_PIN;
        finishCnt = CLEAN_TIME_CNT3;
      } else {
        cleanStep = LED_STEP1_PIN;
        finishCnt = CLEAN_TIME_CNT1;
      }
      Serial.print("Finish Cnt : ");
      Serial.println(finishCnt);
      digitalWrite(cleanStep, HIGH);
    } else {
      motionState = !digitalRead(LED_PW_PIN);
      if (motionState) {
        timeCnt = 0;
        loopTime = millis();
        cleanStep = LED_STEP1_PIN;
        finishCnt = CLEAN_TIME_CNT1;
        Serial.print("Finish Cnt : ");
        Serial.println(finishCnt);
      }
      setCleanerStatus(motionState);
    }
    pressureCnt = 0;
  }
}

void setCleanerStatus(bool enable) {
  digitalWrite(LED_PW_PIN, enable);
  digitalWrite(PLASMA_PIN, enable);
  digitalWrite(FAN_PIN, enable);
  digitalWrite(cleanStep, enable);

  Serial.print("Motion Enable : ");
  Serial.println(enable);
}
