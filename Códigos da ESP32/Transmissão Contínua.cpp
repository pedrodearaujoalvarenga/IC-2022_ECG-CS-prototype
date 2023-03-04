#include <NimBLEDevice.h>

const int clockSpeed = 80;
const int compressRatio = 0;

NimBLEServer* pServer = NULL;
NimBLECharacteristic* pCharacteristic = NULL;

#define SERVICE_UUID        "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"

bool deviceConnected = false;

class MyServerCallbacks: public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) {
      NimBLEDevice::stopAdvertising();
      deviceConnected = true;
    };

    void onDisconnect(BLEServer* pServer) {
      deviceConnected = false;
      NimBLEDevice::startAdvertising();
      
    }
};


void setup() {

  setCpuFrequencyMhz(clockSpeed);
  analogReadResolution(10);
  NimBLEDevice::init("AD8232-BLE-SENSOR");
  pServer = NimBLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());
  NimBLEService *pService = pServer->createService(SERVICE_UUID);
  pCharacteristic = pService->createCharacteristic(
                      CHARACTERISTIC_UUID,
                      NIMBLE_PROPERTY::WRITE  |
                      NIMBLE_PROPERTY::NOTIFY
                    );
  pService->start();
  NimBLEAdvertising *pAdvertising = NimBLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(false);
  pAdvertising->setMinPreferred(0x0);
  NimBLEDevice::startAdvertising();
}

bool clockSpeed1 = false;
int valueCollector;
int nextCollector;
int value = 0;
void loop() {
  
    if (deviceConnected) {
int calculatedTime = micros();
    if(!clockSpeed1){
      valueCollector = millis();
      clockSpeed1 = true;
    }
    if(rand()%100>=compressRatio){
     uint16_t AD8232_reading;
      if((digitalRead(40) == 1)||(digitalRead(41)==1)){
        AD8232_reading = 0;
      }else{
        AD8232_reading = analogRead(15);
      }
        pCharacteristic->setValue((uint8_t*)&AD8232_reading, 2);
        pCharacteristic->notify();
     }
int nowTime = micros();
nowTime = nowTime - calculatedTime;
delayMicroseconds(10000-nowTime);
     
     }else{
      delay(1000);
     }
     
 }