#define Ultra_Trig A0  //Ultrasonic sensor
#define Ultra_Echo A1

#define LS_R A2  //line sensor right
#define LS_MR A3
#define LS_ML A4
#define LS_L A5  //line sensor left

#define TM_L 2  //tachometer left
#define TM_R 3  //tachometer left

#define servo_M 5 //servo motor

#define EN_Left 6 //Left motor
#define Left1 7
#define Left2 8

#define Right2 9  //Right motor
#define Right1 10
#define EN_Right 11

#include <math.h>

const String U = "MF";  //move forward
const String D = "MB";  //move backward
const String R = "MR";  //move right
const String L = "ML";  //move left
const String O = "OE";  //move auto and evade object
const String EMO = "EMO"; //emergency stop
const String LI = "LINE"; //line tracking
const String M20 = "M20";   //move 20 steps
const String M10 = "M10";   //move 10 steps
const String M05 = "M05";   //move 5 steps
const String N20 = "N20";   //move -20 steps
const String N10 = "N10";   //move -10 steps
const String N05 = "N05";   //move -5 steps
const String ZI = "ZI";   //move zigzag
const String DM = "DM";   //measure distance
const String REC = "REC";   //run by record direction
const String MN = "MN";  //manual control

int distance;

const float rr = 25/45.0;   //for angle calculation
const float rl = 25/45.0;

volatile int cl = 0;   //check wheel step
volatile int cr = 0;

#include <Servo.h>

Servo motor;
byte pos;
const byte angle_R = 25;
const byte angle_L = 175;
const byte angle_C = 100;
const byte MaxAutoSpeed = 255;
byte LT_Speed = 175;

float DisOpposAngle(int a, int b, byte angle);
int cm_to_steps(int cm);
void Move_rt();
void Move_lt();
void Move_fw();
void Move_bw();
void Stop();
void Move_right(int slot_count, byte motor_speed);
void Move_left(int slot_count, byte motor_speed);
void Move_backward(int slot_count, byte motor_speed);
void Move_forward(int slot_count, byte motor_speed);
int trigUltraSonicSensor();
void R_count();
void L_count();
void printDistance();

void setup()
{
  Serial.begin(115200);
  pinMode(Ultra_Trig, OUTPUT);
  pinMode(Ultra_Echo, INPUT);
  pinMode(LS_R, INPUT);
  pinMode(LS_MR, INPUT);
  pinMode(LS_ML, INPUT);
  pinMode(LS_L, INPUT);
  attachInterrupt(digitalPinToInterrupt(TM_L), L_count, RISING);
  attachInterrupt(digitalPinToInterrupt(TM_R), R_count, RISING);
  motor.attach(servo_M);
  pinMode(EN_Left, OUTPUT);
  pinMode(Left1, OUTPUT);
  pinMode(Left2, OUTPUT);
  pinMode(EN_Right, OUTPUT);
  pinMode(Right1, OUTPUT);
  pinMode(Right2, OUTPUT);
  pinMode(LED_BUILTIN, OUTPUT);
  pos = motor.read();
  digitalWrite(LED_BUILTIN, LOW);
  Serial.setTimeout(1);
}

void loop()
{
  int d[11], LE[2], A;
  byte i, r, c, j, go, angle, check_L, check_R;
  String control;
  if(Serial.available() > 0)
  {
    control = Serial.readString();
  }
  if(control == U)
  {
    while(pos < angle_C)
    {
      pos++;
      motor.write(pos);
      delay(10);
    }
    while(pos > angle_C)
    {
      pos--;
      motor.write(pos);
      delay(10);
    }
    distance = trigUltraSonicSensor();
    if(distance > 10)
    {
      Move_forward(cm_to_steps(distance), 255);
    }
    else
    {
      Stop();
    }
    Serial.print("Ready");
  }
  else if(control == M20)
  {
    Move_forward(20, 255);
    Serial.print("Ready");
  }
  else if(control == M10)
  {
    Move_forward(10, 255);
    Serial.print("Ready");
  }
  else if(control == M05)
  {
    Move_forward(5, 255);
    Serial.print("Ready");
  }
  else if(control == D)
  {
    Move_backward(40, 255);
    Serial.print("Ready");
  }
  else if(control==L)
  {
    while(pos < angle_L)
    {
      pos++;
      motor.write(pos);
      delay(10);
    }
    while(pos > angle_L)
    {
      pos--;
      motor.write(pos);
      delay(10);
    }
    distance = trigUltraSonicSensor();
    if(distance > 10)
    {
      r = 15*rl;
      Move_left(r, 255);
    }
    else
    {
      Stop();
    }
    Serial.print("Ready");
  }
  else if(control == R)
  {
    while(pos < angle_R)
    {
      pos++;
      motor.write(pos);
      delay(10);
    }
    while(pos > angle_R)
    {
      pos--;
      motor.write(pos);
      delay(10);
    }
    distance = trigUltraSonicSensor();
    if(distance > 10)
    {
      r = 15*rr;
      Move_right(r, 255);
    }
    else
    {
      Stop();
    }
    Serial.print("Ready");
  }
  else if(control == O)     //obstacle avoidance
  {
    go = 0;
    do
    {
      distance = 0;
      c = 0;
      pos = angle_R;
      motor.write(pos);
      delay(280);
      for(i=0;i<11;i++)
      {
        angle = angle_R + 15*i;
        motor.write(angle);
        delay(30);
        d[i] = trigUltraSonicSensor();
        if(d[i] < 20)
        {
          c++;
        }
        if(d[i] > distance)
        {
          distance = d[i];
          j = i;
        }
      }
      angle = angle_R + 15*j;
      pos = angle_C;
      motor.write(pos);
      delay(200);
      if((c > 6) && ((d[8]+d[9]+d[10]) < 60))
      {
        go = 0;
        r = 120*rr;
        Move_backward(10, MaxAutoSpeed);
        delay(200);
        Move_right(r, MaxAutoSpeed);
        delay(200);
        distance = trigUltraSonicSensor();
        Move_forward(cm_to_steps(distance), MaxAutoSpeed);
      }
      else if((c > 6) && ((d[0]+d[1]+d[2]) < 60))
      {
        go = 0;
        r = 120*rl;
        Move_backward(10, MaxAutoSpeed);
        delay(200);
        Move_left(r, MaxAutoSpeed);
        delay(200);
        distance = trigUltraSonicSensor();
        Move_forward(cm_to_steps(distance), MaxAutoSpeed);
      }
      else if((c > 6) && ((d[4]+d[5]+d[6]) < 60))
      {
        go = 0;
        r = 100*rr;
        Move_backward(10, MaxAutoSpeed);
        delay(200);
        Move_right(r, MaxAutoSpeed);
        delay(200);
        distance = trigUltraSonicSensor();
        Move_forward(cm_to_steps(distance), MaxAutoSpeed);
      }
      else if((c > 4) && (d[0] < 20) && (d[2] < 20))
      {
        go = 0;
        r = 50*rl;
        Move_left(r, MaxAutoSpeed);
        delay(200);
        distance = trigUltraSonicSensor();
        Move_forward(cm_to_steps(distance), MaxAutoSpeed);
      }
      else if((c > 4) && (d[8] < 20) && (d[10] < 20))
      {
        go = 0;
        r = 50*rr;
        Move_right(r, MaxAutoSpeed);
        delay(200);
        distance = trigUltraSonicSensor();
        Move_forward(cm_to_steps(distance), MaxAutoSpeed);
      }
      else if((c > 4) && (d[1] < 20) &&(d[3] < 20))
      {
        go = 0;
        r = 80*rl;
        Move_left(r, MaxAutoSpeed);
        delay(200);
        distance = trigUltraSonicSensor();
        Move_forward(cm_to_steps(distance), MaxAutoSpeed);
      }
      else if((c > 4) && (d[7] < 20) && (d[9] < 20))
      {
        go = 0;
        r = 80*rr;
        Move_right(r, MaxAutoSpeed);
        delay(200);
        distance = trigUltraSonicSensor();
        Move_forward(cm_to_steps(distance), MaxAutoSpeed);
      }
      else if((c > 4) && (d[4] < 20) && (d[6] < 20))
      {
        go = 0;
        r = 110*rr;
        Move_right(r, MaxAutoSpeed);
        delay(200);
        distance = trigUltraSonicSensor();
        Move_forward(cm_to_steps(distance), MaxAutoSpeed);
      }
      else if((c > 2) && ((d[0]+d[1]) < 40))
      {
        go = 0;
        r = 30*rl;
        Move_left(r, MaxAutoSpeed);
        delay(200);
        distance = trigUltraSonicSensor();
        Move_forward(cm_to_steps(distance), MaxAutoSpeed);
      }
      else if((c > 2) && ((d[9]+d[10]) < 40))
      {
        go = 0;
        r = 30*rr;
        Move_right(r, MaxAutoSpeed);
        delay(200);
        distance = trigUltraSonicSensor();
        Move_forward(cm_to_steps(distance), MaxAutoSpeed);
      }
      else if((c > 2) && ((d[2]+d[3]) < 40))
      {
        go = 0;
        r = 60*rl;
        Move_left(r, MaxAutoSpeed);
        delay(200);
        distance = trigUltraSonicSensor();
        Move_forward(cm_to_steps(distance), MaxAutoSpeed);
      }
      else if((c > 2) && ((d[7]+d[8]) < 40))
      {
        go = 0;
        r = 60*rr;
        Move_right(r, MaxAutoSpeed);
        delay(200);
        distance = trigUltraSonicSensor();
        Move_forward(cm_to_steps(distance), MaxAutoSpeed);
      }
      else if((c > 2) && ((d[5]+d[6]) < 40))
      {
        go = 0;
        r = 90*rr;
        Move_right(r, MaxAutoSpeed);
        delay(200);
        distance = trigUltraSonicSensor();
        Move_forward(cm_to_steps(distance), MaxAutoSpeed);
      }
      else if((c > 2) && ((d[5]+d[4]) < 40))
      {
        go = 0;
        r = 90*rl;
        Move_left(r, MaxAutoSpeed);
        delay(200);
        distance = trigUltraSonicSensor();
        Move_forward(cm_to_steps(distance), MaxAutoSpeed);
      }
      else if((c > 0) && (d[10] < 20))
      {
        go = 0;
        r = 20*rr;
        Move_right(r, MaxAutoSpeed);
        delay(200);
        distance = trigUltraSonicSensor();
        Move_forward(cm_to_steps(distance), MaxAutoSpeed);
      }
      else if((c > 0) && (d[0] < 20))
      {
        go = 0;
        r = 20*rl;
        Move_left(r, MaxAutoSpeed);
        delay(200);
        distance = trigUltraSonicSensor();
        Move_forward(cm_to_steps(distance), MaxAutoSpeed);
      }
      else if((c > 0) && (d[1] < 20))
      {
        go = 0;
        r = 40*rl;
        Move_left(r, MaxAutoSpeed);
        delay(200);
        distance = trigUltraSonicSensor();
        Move_forward(cm_to_steps(distance), MaxAutoSpeed);
      }
      else if((c > 0) && (d[9] < 20))
      {
        go = 0;
        r = 40*rr;
        Move_right(r, MaxAutoSpeed);
        delay(200);
        distance = trigUltraSonicSensor();
        Move_forward(cm_to_steps(distance), MaxAutoSpeed);
      }
      else if((c > 0) && (d[8] < 20))
      {
        go = 0;
        r = 60*rr;
        Move_right(r, MaxAutoSpeed);
        delay(200);
        distance = trigUltraSonicSensor();
        Move_forward(cm_to_steps(distance), MaxAutoSpeed);
      }
      else if((c > 0) && (d[2] < 20))
      {
        go = 0;
        r = 60*rl;
        Move_left(r, MaxAutoSpeed);
        delay(200);
        distance = trigUltraSonicSensor();
        Move_forward(cm_to_steps(distance), MaxAutoSpeed);
      }
      else if((c > 0) && (d[7] < 20))
      {
        go = 0;
        r = 80*rr;
        Move_right(r, MaxAutoSpeed);
        delay(200);
        distance = trigUltraSonicSensor();
        Move_forward(cm_to_steps(distance), MaxAutoSpeed);
      }
      else if((c > 0) && (d[3] < 20))
      {
        go = 0;
        r = 80*rl;
        Move_left(r, MaxAutoSpeed);
        delay(200);
        distance = trigUltraSonicSensor();
        Move_forward(cm_to_steps(distance), MaxAutoSpeed);
      }
      else if((c > 0) && (d[6] < 20))
      {
        go = 0;
        r = 100*rr;
        Move_right(r, MaxAutoSpeed);
        delay(200);
        distance = trigUltraSonicSensor();
        Move_forward(cm_to_steps(distance), MaxAutoSpeed);
      }
      else if((c > 0) && (d[4] < 20))
      {
        go = 0;
        r = 100*rl;
        Move_left(r, MaxAutoSpeed);
        delay(200);
        distance = trigUltraSonicSensor();
        Move_forward(cm_to_steps(distance), MaxAutoSpeed);
      }
      else if((c > 0) && (d[5] < 20))
      {
        go = 0;
        r = 120*rr;
        Move_right(r, MaxAutoSpeed);
        delay(200);
        distance = trigUltraSonicSensor();
        Move_forward(cm_to_steps(distance), MaxAutoSpeed);
      }
      else
      {
        go++;
        if(go > 7)
        {
          go = 0;
          r = 180*rr;
          Move_backward(10, MaxAutoSpeed);
          delay(200);
          Move_right(r, MaxAutoSpeed);
          delay(200);
          distance = trigUltraSonicSensor();
          Move_forward(cm_to_steps(distance), MaxAutoSpeed);
        }
        else
        {
          if(angle < angle_C)
          {
            r = (angle_C - angle)*rr;
            Move_right(r, MaxAutoSpeed);
          }
          else if(angle > angle_C)
          {
            r = (angle - angle_C)*rl;
            Move_left(r, MaxAutoSpeed);
          }
          delay(200);
          distance = trigUltraSonicSensor();
          Move_forward(cm_to_steps(distance), MaxAutoSpeed);
        }
      }
      if(Serial.available() > 0)
      {
        control = Serial.readString();
      }
    }while(control != EMO);
    Serial.print("Ready");
  }
  else if(control == LI)      //line tracking
  {
    LT_Speed = 175;
    int check_bw = 0;
    do
    {
      int leftSensor = digitalRead(LS_L);
      int rightSensor = digitalRead(LS_R);
      int middleLeftSensor = digitalRead(LS_ML);
      int middleRightSensor = digitalRead(LS_MR);
      if(middleLeftSensor && middleRightSensor)
      {
        Move_fw();
        check_bw=0;
        LT_Speed++;
        if(LT_Speed > 250) {
          LT_Speed = 250;
        }
      }
      else if(leftSensor)
      {
        Move_lt();
        check_bw = 0;
      }
      else if(rightSensor)
      {
        Move_rt();
        check_bw = 0;
      }
      else if(middleLeftSensor)
      {
        Move_fw();
        check_bw = 0;
      }
      else if(middleRightSensor)
      {
        Move_fw();
        check_bw = 0;
      }
      else
      {
        Move_bw();
        check_bw++;
        LT_Speed--;
        if(LT_Speed < 150) {
          LT_Speed = 150;
        }
      }
      if(Serial.available() > 0)
      {
        control = Serial.readString();
      }
      if(check_bw > 300) {
        digitalWrite(Left1, LOW);
        digitalWrite(Left2, LOW);
        digitalWrite(Right1, LOW);
        digitalWrite(Right2, LOW);
      }
      delay(1);
    }while(control != EMO);
    Stop();
    Serial.print("Ready");
  }
  else if(control == REC)      //use record direction to run
  {
    LT_Speed = 175;
    pos = angle_C;
    motor.write(pos);
    delay(200);
    Serial.print("Ready");
    delay(100);
    do
    {
      if(control == "fw")
      {
        Move_fw();
        delay(10);
        Serial.print("Ready");
        control = REC;
      }
      else if(control == "bw")
      {
        Move_bw();
        delay(10);
        Serial.print("Ready");
        control = REC;
      }
      else if(control == "lt")
      {
        Move_lt();
        delay(10);
        Serial.print("Ready");
        control = REC;
      }
      else if(control == "rt")
      {
        Move_rt();
        delay(10);
        Serial.print("Ready");
        control = REC;
      }
      else if(control == "00")
      {
        digitalWrite(Left1, LOW);
        digitalWrite(Left2, LOW);
        digitalWrite(Right1, LOW);
        digitalWrite(Right2, LOW);
        Serial.print("Ready");
        control = REC;
      }
      if(Serial.available() > 0)
      {
        control = Serial.readString();
      }
    }while(control != EMO);
    Stop();
    Serial.print("Ready");
  }
  else if(control == ZI)      //zigzag movement
  {
    check_L = 0;
    check_R = 0;
    do
    {
      distance = 999;
      c = 0;
      pos = angle_R;
      motor.write(pos);
      delay(280);
      for(i=0;i<11;i++)
      {
        angle = angle_R + 15*i;
        motor.write(angle);
        delay(30);
        d[i] = trigUltraSonicSensor();
        if(d[i] < distance)
        {
          distance = d[i];
          j = i; 
        }
        if(d[i] < 20)
        {
          c++;
        }
      }
      angle = angle_R + 15*j;
      pos = angle_C;
      motor.write(pos);
      delay(200);
      if((j > 0) && (j < 10) && (c > 1))
      {
        LE[0] = DisOpposAngle(d[j-1],d[j],15);
        LE[1] = DisOpposAngle(d[j],d[j+1],15);
        if(LE[0] < LE[1])
        {
          A = (180*acos((d[j-1]*d[j-1]-d[j]*d[j]-LE[0]*LE[0])/(2*d[j]*LE[0])))/PI;
          if(angle_C > angle)
          {
            A = 180-A+(angle_C-angle);  //for move right
          }
          else if(angle_C < angle)
          {
            A = 180-A-(angle-angle_C);  //for move right
          }
        }
        else
        {
          A = (180*acos((d[j+1]*d[j+1]-d[j]*d[j]-LE[1]*LE[1])/(2*d[j]*LE[1])))/PI;
          if(angle_C > angle)
          {
            A = A+(angle_C-angle);  //for move right
          }
          else if(angle_C < angle)
          {
            A = A-(angle-angle_C);  //for move right
          }
        }
        if(j == 5)
        {
          if(check_R < 3)
          {
            check_R++;
            r = 90*rr;
            Move_right(r, MaxAutoSpeed);
            delay(200);
          }
          else
          {
            if(check_L < 2)
            {
              check_L++;
              r = 90*rl;
              Move_left(r, MaxAutoSpeed);
              delay(200);
            }
            else
            {
              check_L = 0;
              check_R = 1;
            }
          }
        }
        else
        {
          if(check_R < 3)
          {
            check_R++;
            r = A*rr;
            Move_right(r, MaxAutoSpeed);
            delay(200);
          }
          else
          {
            if(check_L < 2)
            {
              check_L++;
              r = 90*rl;
              Move_left(r, MaxAutoSpeed);
              delay(200);
            }
            else
            {
              check_L = 0;
              check_R = 1;
            }
          }
        }
      }
      else if(j == 0 && c > 1)
      {
        if(check_R < 3)
        {
          r = 205*rl;
          Move_left(r, MaxAutoSpeed);
          delay(200);
        }
        else
        {
          if(check_L < 2)
          {
            check_L++;
            r = 90*rl;
            Move_left(r, MaxAutoSpeed);
            delay(200);
          }
          else
          {
            check_L = 0;
            check_R = 1;
          }
        }
      }
      else if(j == 10 && c > 1)
      {
        if(check_R < 3)
        {
          r = 25*rr;
          Move_right(r, MaxAutoSpeed);
          delay(200);
        }
        else
        {
          if(check_L < 2)
          {
            check_L++;
            r = 90*rl;
            Move_left(r, MaxAutoSpeed);
            delay(200);
          }
          else
          {
            check_L = 0;
            check_R = 1;
          }
        }
      }
      else
      {
        if(check_R == 2)
        {
          check_R++;
          r = 90*rr;
          Move_forward(cm_to_steps(40), MaxAutoSpeed);
          delay(200);
          Move_right(r, MaxAutoSpeed);
          delay(200);
        }
        else if(check_L == 1)
        {
          check_L++;
          r = 90*rl;
          Move_forward(cm_to_steps(40), MaxAutoSpeed);
          delay(200);
          Move_left(r, MaxAutoSpeed);
          delay(200);
        }
        else
        {
          distance = trigUltraSonicSensor();
          Move_forward(cm_to_steps(distance), MaxAutoSpeed);
        }
      }
      if(Serial.available() > 0)
      {
        control = Serial.readString();
      }
    }while(control != EMO);
    Serial.print("Ready");
  }
  else if(control == DM)      //measure distance
  {
    byte action = 0;
    pos = angle_C;
    motor.write(pos);
    delay(200);
    printDistance();
    do
    {
      if(control == M05)
      {
        Move_forward(5, 255);
        delay(200);
        action++;
      }
      else if(control == M10)
      {
        Move_forward(10, 255);
        delay(200);
        action++;
      }
      else if(control == M20)
      {
        Move_forward(20, 255);
        delay(200);
        action++;
      }
      else if(control == N05)
      {
        Move_backward(5, 255);
        delay(200);
        action++;
      }
      else if(control == N10)
      {
        Move_backward(10, 255);
        delay(200);
        action++;
      }
      else if(control == N20)
      {
        Move_backward(20, 255);
        delay(200);
        action++;
      }
      else if(control == L)
      {
        r = 15*rl;
        Move_left(r, 255);
        delay(200);
        action++;
      }
      else if(control == R)
      {
        r = 15*rr;
        Move_right(r, 255);
        delay(200);
        action++;
      }
      else
      {
        if(action > 0)
        {
          printDistance();
          action = 0;
        }
      }
      if(Serial.available() > 0)
      {
        control = Serial.readString();
      }
      else
      {
        control = "00";
      }
    }while(control != EMO);
  }
  else if(control == MN)      //manual movement
  {
    LT_Speed = 175;
    bool up_speed = false;
    pos = angle_C;
    motor.write(pos);
    delay(200);
    Serial.print("Ready");
    delay(100);
    do
    {
      if(control == "fw")
      {
        Move_fw();
        delay(10);
        if(!up_speed) {
          up_speed = true;
          delay(100);
          Serial.print("Ready");
          delay(100);
        }
        LT_Speed++;
        if(LT_Speed > 254) {
          LT_Speed = 254;
        }
      }
      else if(control == "bw")
      {
        Move_bw();
        delay(10);
        if(!up_speed) {
          up_speed = true;
          delay(100);
          Serial.print("Ready");
          delay(100);
        }
        LT_Speed++;
        if(LT_Speed > 254) {
          LT_Speed = 254;
        }
      }
      else if(control == "lt")
      {
        Move_lt();
        delay(10);
        if(!up_speed) {
          up_speed = true;
          delay(100);
          Serial.print("Ready");
          delay(100);
        }
        LT_Speed++;
        if(LT_Speed > 254) {
          LT_Speed = 254;
        }
      }
      else if(control == "rt")
      {
        Move_rt();
        delay(10);
        if(!up_speed) {
          up_speed = true;
          delay(100);
          Serial.print("Ready");
          delay(100);
        }
        LT_Speed++;
        if(LT_Speed > 254) {
          LT_Speed = 254;
        }
      }
      else if(control == "00")
      {
        digitalWrite(Left1, LOW);
        digitalWrite(Left2, LOW);
        digitalWrite(Right1, LOW);
        digitalWrite(Right2, LOW);
        LT_Speed = 175;
        up_speed = false;
        Serial.print("Ready");
        control = MN;
      }
      if(Serial.available() > 0)
      {
        control = Serial.readString();
      }
    }while(control != EMO);
    Stop();
    Serial.print("Ready");
  }
}

void printDistance()
{
  distance = trigUltraSonicSensor();
  Serial.print("D");
  if(distance > 99)
  {
    Serial.print(distance);
  }
  else if(distance > 9)
  {
    Serial.print("0");
    Serial.print(distance);
  }
  else
  {
    Serial.print("00");
    Serial.print(distance);
  }
  Serial.print(" cm");
}

void L_count()
{
  cl++;
}

void R_count()
{
  cr++;
}

int trigUltraSonicSensor()
{
  int duration, cm;
  digitalWrite(Ultra_Trig, LOW);
  delayMicroseconds(2);
  digitalWrite(Ultra_Trig, HIGH);
  delayMicroseconds(10);
  digitalWrite(Ultra_Trig, LOW);
  duration = pulseIn(Ultra_Echo, HIGH);
  delay(15);
  cm = duration /29 /2;
  return cm;
}
// The speed of sound is 340 m/s or 29 microseconds per centimeter.
// The ping travels out and back, so to find the distance of the
// object we take half of the distance travelled.

void Move_forward(int slot_count, byte motor_speed)
{
  cl=0;
  cr=0;
  digitalWrite(Left1, HIGH);
  digitalWrite(Left2, LOW);
  digitalWrite(Right1, HIGH);
  digitalWrite(Right2, LOW);
  while((slot_count > cl) && (slot_count > cr))
  {
    analogWrite(EN_Left, motor_speed);
    analogWrite(EN_Right, motor_speed);
  }
  Stop();
  cl=0;
  cr=0;
}

void Move_backward(int slot_count, byte motor_speed)
{
  cl=0;
  cr=0;
  digitalWrite(Left1, LOW);
  digitalWrite(Left2, HIGH);
  digitalWrite(Right1, LOW);
  digitalWrite(Right2, HIGH);
  while((slot_count > cl) && (slot_count > cr))
  {
    analogWrite(EN_Left, motor_speed);
    analogWrite(EN_Right, motor_speed);
  }
  Stop();
  cl=0;
  cr=0;
}

void Move_left(int slot_count, byte motor_speed)
{
  cr=0;
  digitalWrite(Left1, LOW);
  digitalWrite(Left2, HIGH);
  digitalWrite(Right1, HIGH);
  digitalWrite(Right2, LOW);
  while(slot_count > cr)
  {
    analogWrite(EN_Left, motor_speed);
    analogWrite(EN_Right, motor_speed);
  }
  Stop();
  cr=0;
}

void Move_right(int slot_count, byte motor_speed)
{
  cl=0;
  digitalWrite(Left1, HIGH);
  digitalWrite(Left2, LOW);
  digitalWrite(Right1, LOW);
  digitalWrite(Right2, HIGH);
  while(slot_count > cl)
  {
    analogWrite(EN_Left, motor_speed);
    analogWrite(EN_Right, motor_speed);
  }
  Stop();
  cl=0;
}

void Stop()
{
  analogWrite(EN_Right, 0);
  analogWrite(EN_Left, 0);
}

void Move_fw()
{
  analogWrite(EN_Left, LT_Speed);
  analogWrite(EN_Right, LT_Speed);
  digitalWrite(Left1, HIGH);
  digitalWrite(Left2, LOW);
  digitalWrite(Right1, HIGH);
  digitalWrite(Right2, LOW);
}

void Move_bw()
{
  analogWrite(EN_Left, LT_Speed);
  analogWrite(EN_Right, LT_Speed);
  digitalWrite(Left1, LOW);
  digitalWrite(Left2, HIGH);
  digitalWrite(Right1, LOW);
  digitalWrite(Right2, HIGH);
}

void Move_lt()
{
  analogWrite(EN_Left, LT_Speed);
  analogWrite(EN_Right, LT_Speed);
  digitalWrite(Left1, LOW);
  digitalWrite(Left2, HIGH);
  digitalWrite(Right1, HIGH);
  digitalWrite(Right2, LOW);
}

void Move_rt()
{
  analogWrite(EN_Left, LT_Speed);
  analogWrite(EN_Right, LT_Speed);
  digitalWrite(Left1, HIGH);
  digitalWrite(Left2, LOW);
  digitalWrite(Right1, LOW);
  digitalWrite(Right2, HIGH);
}

int cm_to_steps(int cm)
{
  float a = 87/20540.0;   //for distance calculation
  float b = 43367/20540.0;
  float c = -406/13.0;
  float step_count = a*cm*cm + b*cm + c;
  int Steps = step_count;
  return Steps;
}

float DisOpposAngle(int a, int b, int angle)
{
  float c = (a*a)+(b*b)-(2*a*b*cos(angle*PI/180));
  return sqrt(c);
}
//find distance opposite side of angle
