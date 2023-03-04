#include "FS.h"
#include "SD.h"
#include "SPI.h"
#include <NimBLEDevice.h>

#include <WiFi.h>
#include <BluetoothSerial.h>
#include "driver/adc.h"
#include <esp_bt.h>

#include <vector>
#include <algorithm>


//Definições para o usuário

const int delay_BLE_ligado = 5000;     //Alterar a velocidade em que o BLE permanece ligado
const int delay_BLE_desligado = 100*1000; //Alterar a velocidade em que o BLE permanece desligado
int delay_geracao_Dados = 10;     //Alterar a velocidade com que os dados são gerados pela ESP

const int clockSlower = 10; //10 20 40 80 160 240
const int clockTurbo = 240; //80 160 240

bool compressedSensing = false;
int compressedRatio; //Varia de 0 a 20. Escolhido dessa forma pois permite porcentagens de 5 em 5. (5%, 10%, etc) 
int probabilidadedeSerGerado; //= 20-compressedRatio

//Variáveis Globais

NimBLEServer* pServer = NULL;
NimBLECharacteristic* pCharacteristic = NULL;

boolean Connected = false;
boolean canStart = false;
boolean firstTime = true;

boolean wasConnected = true;

#define SERVICE_UUID        "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"

//Funções de MicroSD Principais



//Funções do BLE

class MyServerCallbacks: public NimBLEServerCallbacks {
    void onConnect(NimBLEServer* pServer) {
     NimBLEDevice::stopAdvertising();
     Connected = true;
     wasConnected = true;
     
     };

    void onDisconnect(NimBLEServer* pServer, NimBLEConnInfo& connInfo, int reason) {
      NimBLEDevice::startAdvertising();
      firstTime = true;
      Connected = false;
};
};


class MyServerCallbacksFirst: public NimBLEServerCallbacks{
      void onConnect(NimBLEServer* pServer) {
        NimBLEDevice::stopAdvertising();
   delay(1000);
     Connected = true;
     wasConnected = true;
     };

    void onDisconnect(NimBLEServer* pServer) {

      Connected = false;
    
    if(!canStart){
      NimBLEDevice::startAdvertising();
    }

};
};

class MyCallbacks: public NimBLECharacteristicCallbacks {
    void onWrite(NimBLECharacteristic *pCharacteristic) {
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
    delay_geracao_Dados =  valoresColetadosdaSequencia[0];
  
  if(valoresColetadosdaSequencia[1] == 0){
    
    compressedSensing = false;
  }else{
    compressedSensing = true;
    compressedRatio = valoresColetadosdaSequencia[2];
    probabilidadedeSerGerado = 20 - compressedRatio;
  }

     canStart = true;
    }
};

void createFirstNimBLEDevice(){
  // Create the BLE Device
  NimBLEDevice::init("AD8232");
  // Create the BLE Server
  pServer = NimBLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacksFirst());

  // Create the BLE Service
  NimBLEService *pService = pServer->createService(SERVICE_UUID);

  // Create a BLE Characteristic
  pCharacteristic = pService->createCharacteristic(
                      CHARACTERISTIC_UUID,
                      NIMBLE_PROPERTY::WRITE 
                    );                  
  pCharacteristic->setCallbacks(new MyCallbacks());
  // Start the service
  pService->start();
    NimBLEAdvertising* pAdvertising = NimBLEDevice::getAdvertising();
    pAdvertising->addServiceUUID(pService->getUUID());
    pAdvertising->setScanResponse(true);
    pAdvertising->start();

    
  
}

void createNimBLEDevice(){
  
    setCpuFrequencyMhz(clockTurbo);
  // Create the BLE Device
  NimBLEDevice::init("AD8232");
  NimBLEDevice::setMTU(512);

  // Create the BLE Server
  pServer = NimBLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());

  // Create the BLE Service
  NimBLEService *pService = pServer->createService(SERVICE_UUID);

  // Create a BLE Characteristic
  pCharacteristic = pService->createCharacteristic(CHARACTERISTIC_UUID, NIMBLE_PROPERTY::NOTIFY);                    
                                    
  

  // Start the service
  pService->start();
  
    BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(false);
  /** Note, this could be left out as that is the default value */
  pAdvertising->setMinPreferred(0x0);  // set value to 0x00 to not advertise this parameter
  
  NimBLEDevice::startAdvertising();

}

void destroyNimBLEDevice(){
  
  NimBLEDevice::deinit(true);
  if(wasConnected){
    wasConnected = false;
  }else{
    ESP.restart();
  }
  
    setCpuFrequencyMhz(clockSlower); 
  
}

//Funções para Avaliar Existência de Arquivo + Encontrar último arquivo para se criar (removido)



std::vector<int> leiturasESP32; //Vetor global onde é armazenado os dados da ESP.
//em gerarDados dados são colocados nesse vetor.
//em gravarSD esse vetor é clonado e resetado, e a sua cópia é armazenada nos arquivos upload.

//looperI //<- Dado coletado da ESP32.
int looperI = 0;
void gerarDados(void * parameters){ //Task1

TickType_t momentoDoInicio;
  momentoDoInicio = xTaskGetTickCount();
  
  for( ;; ){
  int ans;
    if(digitalRead(40)==1||digitalRead(41)==1){
      ans = 0;
      
      
    }else{
      
      ans = analogRead(15);
    }
    
    
    
leiturasESP32.push_back(ans);

vTaskDelayUntil(&momentoDoInicio,pdMS_TO_TICKS(delay_geracao_Dados));
  }
}
  

void gerarDadoscomCS(void* parameters){

  TickType_t momentoDoInicio;
  momentoDoInicio = xTaskGetTickCount();
  
for(; ;){
    

    if(rand()%20 < probabilidadedeSerGerado){
    
    int ans;
    if(digitalRead(40)==1||digitalRead(41)==1){
      ans = 0;
      
      
    }else{
      
      ans = analogRead(15);
    }
    
    
    
leiturasESP32.push_back(ans);
vTaskDelayUntil(&momentoDoInicio,pdMS_TO_TICKS(delay_geracao_Dados));
    }else{
vTaskDelayUntil(&momentoDoInicio,pdMS_TO_TICKS(delay_geracao_Dados));
  }

  



}


}  
//Em gravarSD, dados são armazenados sempre em pacotes de 600 ints.
//Esse valor ser definido sempre em 600 ajuda na função de envio.

void setup(){

  setCpuFrequencyMhz(clockTurbo);
  WiFi.setSleep(true);
  analogReadResolution(10);
  
  pinMode(41, INPUT);
  pinMode(40, INPUT);
  
createFirstNimBLEDevice();

while(!canStart){
  delay(1000);
  } //loopar notConnected
  
destroyNimBLEDevice();

wasConnected = true;

if(compressedSensing){

xTaskCreate(gerarDadoscomCS, "Task1", 4000, NULL, 1, NULL);  

}else{
 xTaskCreate(gerarDados, "Task 1", 4000, NULL, 1, NULL); 
}


}

bool canConnect = false;

void loop(){

if(!canConnect){
    destroyNimBLEDevice(); //destroy BLE Device
    delay(delay_BLE_desligado);
    createNimBLEDevice(); //Create BLE Device
  canConnect = true;
  
  
}
if(canConnect){
  int i = 0;
  
  while(i<delay_BLE_ligado){
  
if(Connected){
  if(firstTime){
      delay(2000);
      firstTime = false;
    
  }

while(leiturasESP32.size() >= 100){ //Enquanto ainda estiverem arquivos para serem enviados

int arr[100];
std::copy(leiturasESP32.begin(), leiturasESP32.begin() + 100, arr);
leiturasESP32.erase(leiturasESP32.begin(), leiturasESP32.begin() + 100);

  if(!Connected){
    ESP.restart();
  }
uint8_t arrayBytes[200];
int i = 0;
int j = 0;
while(i<200){


arrayBytes[i] = (uint8_t)(arr[j] & 0x00FF);
i++;
arrayBytes[i] = (uint8_t)((arr[j] & 0xFF00) >> 8);
i++;
j++;
}

      pCharacteristic->setValue(arrayBytes, 200);
      pCharacteristic->notify();
    delay(10);
}
firstTime = true;
Connected = false;
canConnect = false;
  break;

} 

delay(1);
i++;

}


if(canConnect){
      ESP.restart();
}


}
  
}