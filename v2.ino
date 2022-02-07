// программа для Arduino


#include <EEPROM.h>

const int COUNT_SENSOR = 8;
const int COUNT_MEMORY_CHAR = 60;

byte mode = 0;
byte detected_char = 0;
int active_port = 0; // 1 - usb, 2 - usart

enum Sensors {
  FlexSensor1,
  FlexSensor2,
  FlexSensor3,
  FlexSensor4,
  FlexSensor5,
  AxeleratorX,
  AxeleratorY,
  AxeleratorZ,
};

struct t_char_data {
    byte sensors_value[COUNT_SENSOR];
    byte char_value;
  };
  
struct t_sensor_data {
    int CurrentADC;
    int PIN;
  };

t_sensor_data sensor_data[COUNT_SENSOR];
t_char_data char_data[COUNT_MEMORY_CHAR];

void setup() {
Serial.begin(9600); 
Serial1.begin(9600); 
pinMode(3, OUTPUT);
digitalWrite(3, HIGH);

sensor_data[FlexSensor1].PIN = A0;
sensor_data[FlexSensor2].PIN = A1;
sensor_data[FlexSensor3].PIN = A2;
sensor_data[FlexSensor4].PIN = A3;
sensor_data[FlexSensor5].PIN = A6;
sensor_data[AxeleratorX].PIN = A7;
sensor_data[AxeleratorY].PIN = A8;
sensor_data[AxeleratorZ].PIN = A9;

read_char_data();

}

void loop() {

if(Serial.available()>0){
  mode = Serial.read();
  active_port = 1;
}

if(Serial1.available()>0){
  mode = Serial1.read();
  //Serial.println(mode);   
  active_port = 2;
}

for (int i = 0; i < COUNT_SENSOR; i++) sensor_data[i].CurrentADC = analogRead(sensor_data[i].PIN)>>2;

if (mode == 0) // stop
{
  detected_char = 0;
  delay(500);
  return;
}

if ((mode == 1) || (mode == 2)) // work
{
  byte find_char = 0;
  for (int i = 0; i < COUNT_MEMORY_CHAR && char_data[i].char_value > 0; i++)
  {
    bool is_search = true;     
    for (int j = 0; j < COUNT_SENSOR; j++)
    {
      if (abs(char_data[i].sensors_value[j] - sensor_data[j].CurrentADC) > 20)
      {
        is_search = false;
        break;
      }
    }
    if (is_search)
    {
      find_char = char_data[i].char_value;
      break;
    }
  }
  if (find_char)
  {
    if ((find_char != detected_char) || (mode == 2))
    {
      print_port(find_char);
      detected_char = find_char;        
    }
  } else if (mode == 2) print_port('!');
  
  delay(500);
}
if (mode == 3) // clear
{
  for (int i = 0; i < COUNT_MEMORY_CHAR; i++)
  {
    for (int j = 0; j < COUNT_SENSOR; j++)
    {
      char_data[i].sensors_value[j] = 0;
    }
    char_data[i].char_value = 0;
  }
  save_char_data();
  mode = 0;
  delay(500);
}
if (mode == 4) // print
{
  print_data();
  read_char_data();
  print_char_data();
  mode = 0;
  delay(500);
  return;
}
if (mode >= 5) // new char
{
  int index_char = -1;
  for (int i = 0; i < COUNT_MEMORY_CHAR; i++)
  {
    if (char_data[i].char_value == mode || char_data[i].char_value == 0)
    {
      index_char = i;
      break;
    }
  }
  if (index_char > -1)
  {
    char_data[index_char].char_value = mode;
    for (int i = 0; i < COUNT_SENSOR; i++)
    {
      char_data[index_char].sensors_value[i] = sensor_data[i].CurrentADC;
    }
    save_char_data();
  }
  mode = 0;
  delay(500);
}

}

void save_char_data()
{
  for (int i = 0; i < sizeof(t_char_data)*COUNT_MEMORY_CHAR; i++)
  {
    EEPROM.update(i, *(((uint8_t *) char_data)+i));
  }
}

void read_char_data()
{
  for (int i = 0; i < sizeof(t_char_data)*COUNT_MEMORY_CHAR; i++)
  {
    *(((uint8_t *) char_data)+i) = EEPROM.read(i);
  }
}

void print_port(byte cp)
{
  if (active_port == 1) Serial.write(cp);    
  if (active_port == 2) Serial1.write(cp);
}

void print_data()
{
  if (active_port == 1) 
  {
    for (t_sensor_data data : sensor_data)
    {
      Serial.print("Sensor ");
      Serial.print(data.PIN);
      Serial.print(" value is:  ");
      Serial.println(data.CurrentADC);      
    }
  }
  if (active_port == 2) 
  {
    for (t_sensor_data data : sensor_data)
    {
      Serial1.print("Sensor ");
      Serial1.print(data.PIN);
      Serial1.print(" value is:  ");
      Serial1.println(data.CurrentADC);      
    }
  }
}

void print_char_data()
{
  if (active_port == 1) t_char_data char_data[COUNT_MEMORY_CHAR];
  {
    for (int i = 0; i < COUNT_MEMORY_CHAR; i++)
    {
      Serial.print("char_data ");
      Serial.print(char_data[i].char_value);
      for (int j = 0; j < COUNT_SENSOR; j++)
      {
        Serial.print(" Sensor ");
        Serial.print(j);
        Serial.print(" value is:  ");
        Serial.println(char_data[i].sensors_value[j]); 
      }
    }
  }
  if (active_port == 2) 
  {
    for (int i = 0; i < COUNT_MEMORY_CHAR; i++)
    {
      Serial1.print("char_data ");
      Serial1.print(char_data[i].char_value);
      for (int j = 0; j < COUNT_SENSOR; j++)
      {
        Serial1.print(" Sensor ");
        Serial1.print(j);
        Serial1.print(" value is:  ");
        Serial1.println(char_data[i].sensors_value[j]); 
      }
    }
  }
}
