#include <Servo.h>

#define EN_Left 2 //Left motor
#define Left1 3
#define Left2 4
#define Right2 5  //Right motor
#define Right1 6
#define EN_Right 7
#define servo_R 9 //right servo motor
#define servo_L 10 //left servo motor

Servo motor_R,motor_L;

const String U = "MF";  //move 45 cw slow
const String D = "MB";  //move 135 ccw slow
const String R = "MR";  //move 170 cw fast
const String L = "ML";  //move 10 ccw fast
const String EMO = "EMO"; //emergency stop
const String REC = "REC";   //run by record direction
const String MN = "MN";  //manual control

const byte MaxSpeedL = 170;
const byte MaxSpeedR = 171;
const int MaxCW = 1000;
const int MaxCCW = 2100;
const int StopServo = 1500;

void Move_rt();
void Move_lt();
void Move_fw();
void Move_bw();
void Servo_rt();
void Servo_lt();
void Servo_fw();
void Servo_bw();
void Servo_stop();
void Stop();

void setup()
{
  Serial.begin(115200);
  pinMode(EN_Left, OUTPUT);
  pinMode(Left1, OUTPUT);
  pinMode(Left2, OUTPUT);
  pinMode(Right1, OUTPUT);
  pinMode(Right2, OUTPUT);
  pinMode(EN_Right, OUTPUT);
  pinMode(LED_BUILTIN, OUTPUT);
  digitalWrite(LED_BUILTIN, LOW);
  motor_R.attach(servo_R);
  motor_L.attach(servo_L);
  Serial.setTimeout(50);
}

void loop()
{
  String control;
  if(Serial.available() > 0)
  {
    control = Serial.readString();
  }
  if(control == U)
  {
    Servo_fw();
    delay(5000);
    Servo_stop();
    delay(10);
    Serial.print("Ready");
  }
  else if(control == D)
  {
    Servo_bw();
    delay(5000);
    Servo_stop();
    delay(10);
    Serial.print("Ready");
  }
  else if(control == R)
  {
    Servo_rt();
    delay(5000);
    Servo_stop();
    delay(10);
    Serial.print("Ready");
  }
  else if(control == L)
  {
    Servo_lt();
    delay(5000);
    Servo_stop();
    delay(10);
    Serial.print("Ready");
  }
  else if(control == REC)      //use record direction to run
  {
    delay(10);
    Serial.print("Ready");
    do
    {
      if(control == "fw")
      {
        digitalWrite(LED_BUILTIN, HIGH);
        Move_fw();
        delay(1);
        Serial.print("Ready");
        control = REC;
      }
      else if(control == "bw")
      {
        digitalWrite(LED_BUILTIN, HIGH);
        Move_bw();
        delay(1);
        Serial.print("Ready");
        control = REC;
      }
      else if(control == "lt")
      {
        digitalWrite(LED_BUILTIN, HIGH);
        Move_lt();
        delay(1);
        Serial.print("Ready");
        control = REC;
      }
      else if(control == "rt")
      {
        digitalWrite(LED_BUILTIN, HIGH);
        Move_rt();
        delay(1);
        Serial.print("Ready");
        control = REC;
      }
      else if(control == "00")
      {
        digitalWrite(LED_BUILTIN, LOW);
        digitalWrite(Left1, LOW);
        digitalWrite(Left2, LOW);
        digitalWrite(Right1, LOW);
        digitalWrite(Right2, LOW);
        delay(1);
        Serial.print("Ready");
        control = REC;
      }
      if(Serial.available() > 0)
      {
        control = Serial.readString();
      }
    }while(control != EMO);
    digitalWrite(LED_BUILTIN, LOW);
    Stop();
    Serial.print("Ready");
  }
  else if(control == MN)      //manual movement
  {
    delay(10);
    Serial.print("Ready");
    do
    {
      if(control == "fw")
      {
        digitalWrite(LED_BUILTIN, HIGH);
        Servo_fw();
        Move_fw();
        delay(1);
        Serial.print("Ready");
        control = MN;
      }
      else if(control == "bw")
      {
        digitalWrite(LED_BUILTIN, HIGH);
        Servo_bw();
        Move_bw();
        delay(1);
        Serial.print("Ready");
        control = MN;
      }
      else if(control == "lt")
      {
        digitalWrite(LED_BUILTIN, HIGH);
        Servo_lt();
        Move_lt();
        delay(1);
        Serial.print("Ready");
        control = MN;
      }
      else if(control == "rt")
      {
        digitalWrite(LED_BUILTIN, HIGH);
        Servo_rt();
        Move_rt();
        delay(1);
        Serial.print("Ready");
        control = MN;
      }
      else if(control == "00")
      {
        digitalWrite(LED_BUILTIN, LOW);
        Servo_stop();
        digitalWrite(Left1, LOW);
        digitalWrite(Left2, LOW);
        digitalWrite(Right1, LOW);
        digitalWrite(Right2, LOW);
        delay(1);
        Serial.print("Ready");
        control = MN;
      }
      if(Serial.available() > 0)
      {
        control = Serial.readString();
      }
    }while(control != EMO);
    digitalWrite(LED_BUILTIN, LOW);
    Servo_stop();
    Stop();
    Serial.print("Ready");
  }
}

void Move_fw()
{
  analogWrite(EN_Left, MaxSpeedL);
  analogWrite(EN_Right, MaxSpeedR);
  digitalWrite(Left1, HIGH);
  digitalWrite(Left2, LOW);
  digitalWrite(Right1, HIGH);
  digitalWrite(Right2, LOW);
}

void Move_bw()
{
  analogWrite(EN_Left, MaxSpeedL);
  analogWrite(EN_Right, MaxSpeedR);
  digitalWrite(Left1, LOW);
  digitalWrite(Left2, HIGH);
  digitalWrite(Right1, LOW);
  digitalWrite(Right2, HIGH);
}

void Move_lt()
{
  analogWrite(EN_Left, MaxSpeedL);
  analogWrite(EN_Right, MaxSpeedR);
  digitalWrite(Left1, LOW);
  digitalWrite(Left2, HIGH);
  digitalWrite(Right1, HIGH);
  digitalWrite(Right2, LOW);
}

void Move_rt()
{
  analogWrite(EN_Left, MaxSpeedL);
  analogWrite(EN_Right, MaxSpeedR);
  digitalWrite(Left1, HIGH);
  digitalWrite(Left2, LOW);
  digitalWrite(Right1, LOW);
  digitalWrite(Right2, HIGH);
}

void Stop()
{
  analogWrite(EN_Left, 0);
  analogWrite(EN_Right, 0);
}

void Servo_fw() {
  motor_R.writeMicroseconds(MaxCW);
  motor_L.writeMicroseconds(MaxCCW);
}

void Servo_bw() {
  motor_R.writeMicroseconds(MaxCCW);
  motor_L.writeMicroseconds(MaxCW);
}

void Servo_rt() {
  motor_R.writeMicroseconds(MaxCW);
  motor_L.writeMicroseconds(MaxCW);
}

void Servo_lt() {
  motor_R.writeMicroseconds(MaxCCW);
  motor_L.writeMicroseconds(MaxCCW);
}

void Servo_stop() {
  motor_R.writeMicroseconds(StopServo);
  motor_L.writeMicroseconds(StopServo);
}
