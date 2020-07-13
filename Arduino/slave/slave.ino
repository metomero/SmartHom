#include <SPI.h>
#include <nRF24L01.h>
#include <RF24.h>

#define CE_PIN   9
#define CSN_PIN 10

#define RED 3
#define GREEN 5
#define BLUE 6

#define RELAY 7
#define LED 2
#define LED2 4
#define YLED 8
#define REFRESH_TIME 20

#define LED_ST 2000
#define LED_UNST 2000

void rgbSet(int idx);
void calculateSteps();

//int RGB_PINS[] = {A4, A5, A3};
int rgb_map[][3] = {{255, 0 ,0},{255, 255, 0},{0, 255, 0},{0, 255, 255},{0, 0, 255},{255, 0, 255}};
double RGB[] = {255, 0, 0};
int STEP_IDX = 0;
double* steps;
int step_count;
int total_step;
bool isInc = false;
int rgb_idx = -1;
int stepidx2 = 0;

int sleep_step = 31;
int sleep_count;

const byte masterAddr[5] = {'R','x','A','A','A'};;
const byte slaveAddr[] = "00001";   //HERE IS SLAVE DOG
uint8_t address[] = { 0xCC,0xCE,0xCC,0xCE,0xCC };
uint8_t addresses[][6] = {"1Node","2Node"};

char my_ans[2] = {'O', 'K'};
char m_order[9];
bool isNew = true;

int MODE = 0; // 0-> LAMP  1->RGBLED
bool mcflag = false;


RF24 radio(CE_PIN, CSN_PIN);

void setup(){
  //Serial.begin(9600);
  //SPI.begin();
  pinMode(LED, OUTPUT);  
  pinMode(LED2, OUTPUT);  
  pinMode(YLED, OUTPUT); 
  pinMode(RELAY, OUTPUT);
  pinMode(RED, OUTPUT);
  pinMode(GREEN, OUTPUT);
  pinMode(BLUE, OUTPUT);

  pinMode(RED, OUTPUT);
  pinMode(GREEN, OUTPUT);
  pinMode(BLUE, OUTPUT);

  analogWrite(RED, 255);
  analogWrite(GREEN, 255);
  analogWrite(BLUE, 255);
  
  radio.begin();
  radio.setChannel(90);
  radio.setDataRate(RF24_250KBPS);
  radio.setPALevel(RF24_PA_MAX);
  //radio.enableAckPayload();
  radio.openReadingPipe(0, addresses[0]);
  radio.startListening();
  //radio.writeAckPayload(1, &my_ans, sizeof(my_ans));

  calculateSteps();
}

void loop(){
  
  if((STEP_IDX % 5 == 0) && radio.available()){
    
      radio.read(&m_order, sizeof(m_order));
      //radio.writeAckPayload(1, &my_ans, sizeof(my_ans));
      digitalWrite(YLED, HIGH);
    
      if(m_order[4] == 'L'){
        MODE = 0;
        digitalWrite(LED, HIGH);
        mcflag = true;
        delay(90);
      }
      if(m_order[4] == 'R'){
        MODE = 1;
        digitalWrite(LED, HIGH);
        mcflag = true;
        delay(90);
      }

      int i;
      for(i = 0; i < 9; i = i + 1)
        m_order[i] = ' ';
  }
  digitalWrite(LED, LOW);
  digitalWrite(LED2, LOW);
  digitalWrite(YLED, LOW);


  switch(MODE){
    case 0:{//LAMP MODE
      if(mcflag == true){
        
  analogWrite(RED, 255);
  analogWrite(GREEN, 255);
  analogWrite(BLUE, 255);
        
          RGB[0] = 255;
          RGB[1] = 0;
          RGB[2] = 0;
          
          mcflag = false;
      }

        digitalWrite(RELAY, HIGH);
        delay(300);
      break;}
    case 1:{ // RGB MODE
      mcflag = false;

      digitalWrite(RELAY, LOW);
      //delay(300);

      if(STEP_IDX % step_count == 0 && sleep_step <= sleep_count){
            sleep_step++;
            //printf("sleep now -> %d\n", sleep_step);

        }
        else{
            rgbSet(STEP_IDX);
            
           analogWrite(RED, (int)RGB[0]);
           analogWrite(GREEN, (int)RGB[1]);
           analogWrite(BLUE, (int)RGB[2]);

           //delay(5);
           //Serial.print((int)RGB[0]);
           //Serial.print(" - ");
           //Serial.print((int)RGB[1]);
           //Serial.print(" - ");
           //Serial.print((int)RGB[2]);
           //Serial.println();
           
              
            STEP_IDX ++;
            if(STEP_IDX >= total_step)
              STEP_IDX = 0;
              
            sleep_step = 0;
            //printf("%d - %d - %d\n", RGB[0], RGB[1], RGB[2]);
        }

      break;}
  }

  delay(REFRESH_TIME);

}

void rgbSet(int idx){
    int cur_idx= idx % step_count;

    if(cur_idx == 0) {
        isInc = !isInc;
        rgb_idx++;
        if(rgb_idx >= 6)
            rgb_idx = 0;
    }

    if(isInc == true)
        RGB[(rgb_idx/2 + 1)%3] += steps[stepidx2++];
    else{
        RGB[(rgb_idx/2)%3] -= steps[stepidx2++];
    }

    if(stepidx2 >= step_count)
        stepidx2 = 0;
}

void calculateSteps(){
    int step_size = (255 / (LED_UNST / 100) + 1);
    step_count = (LED_UNST / REFRESH_TIME);
    steps = (double*)malloc(step_count * sizeof(double));

    int i;
    int rem = 255;
    double step_val = (double)255 / step_count;
    for(i = 0; i < step_count; i = i + 1)
        steps[i] = step_val;

    //steps[i] = 255 - ((step_val) * (step_count - 1));
    total_step = 6 * step_count;

    sleep_count = LED_ST / REFRESH_TIME;
}
