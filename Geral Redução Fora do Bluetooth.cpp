#include "FS.h"
#include "SD.h"
#include "SPI.h"
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

#include <WiFi.h>
#include <BluetoothSerial.h>
#include "driver/adc.h"
#include <esp_bt.h>

#include <vector>


//Definições para o usuário

int delay_BLE_ligado = 5000;     //Alterar a velocidade em que o BLE permanece ligado
int delay_BLE_desligado = 10000; //Alterar a velocidade em que o BLE permanece desligado
int delay_geracao_Dados = 3;     //Alterar a velocidade com que os dados são gerados pela ESP

const int clockSlower = 40; //10 20 40 80 160 240
const int clockTurbo = 240; //80 160 240

//Variáveis Globais

uint16_t deviceID;

BLEServer* pServer = NULL;
BLECharacteristic* pCharacteristic = NULL;

boolean Connected = false;
boolean canStart = false;

#define SERVICE_UUID        "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"


int arquivoEnviar = 0; //int que sinaliza arquivos criados pela ESP
int arquivoEnviado = 0; //int que sinaliza arquivos enviados pela ESP
//naturalmente, os que faltam enviar é a diferença dos dois.

std::string ultimoDiretorio; //String global que contém o nome da pasta (leituraN).
//Em SETUP, a ESP32 lê o cartão SD e identifica qual a última pasta do tipo "leitura" existente, e então adiciona um número na frente.

//Funções de MicroSD Principais


void deleteFile(const char * path){
  SD.remove(path);
}

void formatSD(){

  File root = SD.open("/");
    File file = root.openNextFile();
  while(file){
  deleteFile(file.path());
    file = root.openNextFile();
  }
}


std::string readFile(fs::FS &fs, const char * path){ //Função que lê arquivo
  std::string respostaLeitura = "";

  File file = fs.open(path);
  if(!file){
    return "";
  }

  while(file.available()){
    respostaLeitura += (char)file.read();
 
  }

  
  file.close();
  return respostaLeitura;
}

void writeFile(fs::FS &fs, const char * path, const char * message){ //Função que cria arquivo

  File file = fs.open(path, FILE_WRITE);
  if(!file){
    return;
  }
  if(file.print(message)){
  } else {
  }
  file.close();
}

//Funções do BLE

class MyServerCallbacks: public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) {
      deviceID = pServer -> getConnId();
      delay(1000);
     
     Connected = true;
     
     }

    void onDisconnect(BLEServer* pServer) {
      
      Connected = false;
      BLEDevice::startAdvertising();
}
};


class MyServerCallbacksFirst: public BLEServerCallbacks{
      void onConnect(BLEServer* pServer) {
      
	 delay(1000);
     Connected = true;
     
     }

    void onDisconnect(BLEServer* pServer) {
      
      Connected = false;
      BLEDevice::startAdvertising();

}
};

class MyCallbacks: public BLECharacteristicCallbacks {
    void onWrite(BLECharacteristic *pCharacteristic) {
      std::string value = pCharacteristic->getValue();
if(value.length()<2){
  canStart = true;
  return;
}

  std::string coletadodaCaracteristica = pCharacteristic->getValue(); //braço para ler o valor da característica enviado pelo app

   int valoresColetadosdaSequencia[3];
   int h = 0;
      std::string w = "";
    for (auto x : coletadodaCaracteristica)
    {
        if (x == '-')
        {
            valoresColetadosdaSequencia[h] = atoi(w.c_str());
            w = "";
            h++;
        }
        else {
            w = w + x;
        }
    }
    valoresColetadosdaSequencia[h] = atoi(w.c_str());

    delay_BLE_ligado = valoresColetadosdaSequencia[1]*1000;
    delay_BLE_desligado = valoresColetadosdaSequencia[2]*1000;
    delay_geracao_Dados =  valoresColetadosdaSequencia[0];

     canStart = true;
    }
};

void createFirstBLEDevice(){
  // Create the BLE Device
  BLEDevice::init("AD8232-BLE-SENSOR");
  // Create the BLE Server
  pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacksFirst());

  // Create the BLE Service
  BLEService *pService = pServer->createService(SERVICE_UUID);

  // Create a BLE Characteristic
  pCharacteristic = pService->createCharacteristic(
                      CHARACTERISTIC_UUID,
                      BLECharacteristic::PROPERTY_WRITE 
                    );                  
  pCharacteristic->setCallbacks(new MyCallbacks());
  // Start the service
  pService->start();

  // Start advertising
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(false);
  pAdvertising->setMinPreferred(0x0);  // set value to 0x00 to not advertise this parameter
  BLEDevice::startAdvertising();
  
}

void createBLEDevice(){
	
	setCpuFrequencyMhz(clockTurbo);
  // Create the BLE Device
  BLEDevice::startAdvertising();
}

void destroyBLEDevice(){
  
  BLEDevice::stopAdvertising();
  setCpuFrequencyMhz(clockSlower); 
}

//Funções para Avaliar Existência de Arquivo + Encontrar último arquivo para se criar (removido)



std::vector<int> leiturasESP32; //Vetor global onde é armazenado os dados da ESP.
//em gerarDados dados são colocados nesse vetor.
//em gravarSD esse vetor é clonado e resetado, e a sua cópia é armazenada nos arquivos upload.

//looperI //<- Dado coletado da ESP32.

void gerarDados(void * parameters){ //Task1
  for( ;; ){
int looperI;
if(digitalRead(40) == 1 || digitalRead(41) == 1){
  looperI = 0;
}else{
  looperI = analogRead(15);
}
leiturasESP32.push_back(looperI);
vTaskDelay(delay_geracao_Dados /portTICK_PERIOD_MS);
  }
}

void gravarSD(void * parameters){ //Task2
for( ;; ){
  
if(leiturasESP32.size() == 600){
  std::vector<int> copiaRAM = leiturasESP32;
  leiturasESP32.clear();
  std::string textoFinal = "";
int k = 0;
  for (auto it = copiaRAM.begin(); k<600; k++){

  textoFinal += std::to_string(*it) + ",";
  copiaRAM.erase(copiaRAM.begin());
  
}

  std::string nomedoArquivo = ultimoDiretorio + "/upload" + std::to_string(arquivoEnviar) + ".txt";

  writeFile(SD, nomedoArquivo.c_str(), textoFinal.c_str());

  arquivoEnviar++;
}

  vTaskDelay(1 /portTICK_PERIOD_MS);
  }
  
}



void gerenciamentoBLE(void * parameters){ //Task3
for( ; ;){
  if(!Connected){
    destroyBLEDevice(); //destroy BLE Device
    delay(delay_BLE_desligado);
    createBLEDevice(); //Create BLE Device
    
    vTaskDelay(delay_BLE_ligado /portTICK_PERIOD_MS);
  }else{

    vTaskDelay(5 /portTICK_PERIOD_MS); //Esperar até ele se desconectar.
  }
  }
  }
  
//Em gravarSD, dados são armazenados sempre em pacotes de 600 ints.
//Esse valor ser definido sempre em 600 ajuda na função de envio.

void enviarArquivosviaBLE(){
  std::string aLer = ultimoDiretorio + "/upload" + std::to_string(arquivoEnviado) + ".txt";
std::string stringHolder = readFile(SD, aLer.c_str());
deleteFile(aLer.c_str());

String w = "";
uint8_t arrayBytes[6][200];
int i = 0;
int j = 0;

    for (auto x : stringHolder)
    {
        if (x == ',')
        {

           arrayBytes[j][i] = (uint8_t)(atoi(w.c_str()) & 0x00FF);
           i++;
           arrayBytes[j][i] = (uint8_t)((atoi(w.c_str()) & 0xFF00) >> 8);
           i++;
            
          if(i == 200){
            i = 0;
            j++;
          }
            w = "";
            
        }
        else {
            w = w + x;
        }
    }
    i = 0;
    j = 0;

    while(j<6){
      pCharacteristic->setValue(arrayBytes[j], 200);
      pCharacteristic->notify();
      j++;
      delay(3);
    }
}

void setup(){


  setCpuFrequencyMhz(clockTurbo);
  WiFi.setSleep(true);
  analogReadResolution(10);
  
  pinMode(41, INPUT);
  pinMode(40, INPUT);
  while(!SD.begin(5)){ //Esperar para o cartão estar funcionando apropriadamente.

    delay(100);
  }
createFirstBLEDevice();

while(!canStart){
  delay(1000);
  } //loopar notConnected
  
formatSD();
ultimoDiretorio = "/"; //Escrever o nome do último diretório nesta string.
  
  BLEDevice::deinit();


  // Create the BLE Device
  BLEDevice::init("AD8232-BLE-SENSOR");
  BLEDevice::setMTU(512);

  // Create the BLE Server
  pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());

  // Create the BLE Service
  BLEService *pService = pServer->createService(SERVICE_UUID);

  // Create a BLE Characteristic
  pCharacteristic = pService->createCharacteristic(CHARACTERISTIC_UUID, BLECharacteristic::PROPERTY_NOTIFY);                    
                                    
  // Create a BLE Descriptor
  pCharacteristic->addDescriptor(new BLE2902());

  // Start the service
  pService->start();

  // Start advertising
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(false);
  pAdvertising->setMinPreferred(0x0);  // set value to 0x00 to not advertise this parameter


xTaskCreate(gerarDados, "Task 1", 2000, NULL, 1, NULL); //Postar TASK 1 - GERAR DADOS

xTaskCreate(gravarSD, "Task 2", 5000, NULL, 1, NULL);  //Postar TASK 2 - GRAVAR CARTÃO SD

xTaskCreate(gerenciamentoBLE, "Task 3", 4000, NULL, 1, NULL);  //Postar TASK 3 - LIGAR E DESLIGAR BLE

}

void loop(){


if(Connected){

while(arquivoEnviar - arquivoEnviado > 0){ //Enquanto ainda estiverem arquivos para serem enviados

  if(!Connected){
    ESP.restart();
  }
  enviarArquivosviaBLE();
  arquivoEnviado++;
}

pServer -> disconnect(deviceID);
} 
  
}