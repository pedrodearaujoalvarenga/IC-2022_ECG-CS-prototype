package com.cefetmg.september;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleMtuChangedCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleScanAndConnectCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.BleScanRuleConfig;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class bluetoothService extends Service {

    BleDevice ESP32;
    int timesConnected = 0;
    int delayBLEOFF = 38000; //in September, delayBLEOFF is a constant.
    int statusConnection = 1;
    boolean shouldSpin = false;
    boolean killYourself = false;
    boolean novaLeitura = true;

    String consoleServiceBus = "";

    int placetoSend;

    //0: Stopped
    //1: Started Connection (FirstConnect)
    //2: Superconnection (waiting)
    //3: Superconnection (Connected)
    //4: Sending to server
    //5: Disconnected

    String uuid_service = "4fafc201-1fb5-459e-8fcc-c5c9c331914b";
    String uuid_characteristic_notify = "beb5483e-36e1-4688-b7f5-ea07361b26a8";

    public bluetoothService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        EventBus.getDefault().register(this);

        BleManager.getInstance().init(getApplication());

        String[] names = {"AD8232"};
        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder().setDeviceName(true, names).setScanTimeOut(4000).build();
        BleManager.getInstance().initScanRule(scanRuleConfig);

        placetoSend = intent.getIntExtra("target", 0);
        consoleServiceBus = consoleServiceBus + "Started connection.\n";
        MessageEvent beggining = new MessageEvent();
        beggining.setConsole(consoleServiceBus);
        EventBus.getDefault().post(beggining);

        superConnection();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            final String mypoggers = "ESP32";
            NotificationChannel notificationChannel = new NotificationChannel(mypoggers, mypoggers,
                    NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(notificationChannel);
            Notification.Builder notification = new Notification.Builder(this, mypoggers)
                    .setContentText("Conexão em progresso")
                    .setContentTitle("AD8232-ESP32")
                    .setSmallIcon(R.drawable.notification);

            startForeground(1001, notification.build());

        } else {

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText("Status atual: Conectado")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);

            Notification notification = builder.build();

            startForeground(1001, notification);
        }

        return START_NOT_STICKY;
    }


    void superConnection(){

        MessageEvent event = new MessageEvent();
        statusConnection = 2;
        event.setStatus(statusConnection);
        event.setShouldSpin(false);
        EventBus.getDefault().post(event);


        //Objetivo: Conectar na ESP32 via BLE, ser desconectado, esperar o delayBLEOFF, ser desconectado, repetir.

        new CountDownTimer(delayBLEOFF, 50) {

            public void onTick(long millisUntilFinished) {
                if(killYourself){



                    cancel();
                    stopSelf();

                    MessageEvent confirmationStopped = new MessageEvent();
                    confirmationStopped.setStatus(404);
                    consoleServiceBus = consoleServiceBus + "Stopping connection. \nReason: User pressed stop.\n";
                    confirmationStopped.setConsole(consoleServiceBus);
                    EventBus.getDefault().post(confirmationStopped);
                    return;

                }
                long milisRunned = delayBLEOFF - millisUntilFinished;
                MessageEvent loadingBar = new MessageEvent();
                loadingBar.setWaitingTime((int) milisRunned);
                loadingBar.setStatus(2);

                EventBus.getDefault().post(loadingBar);



            }

            public void onFinish() {

                timesConnected++;
                consoleServiceBus = consoleServiceBus + "Connection times: " + timesConnected + ".\n";
                while(consoleServiceBus.length()>8000){
                    consoleServiceBus = consoleServiceBus.substring(100);

                }

                if(!BleManager.getInstance().isBlueEnable()){
                    stopSelf();

                    MessageEvent confirmationStopped = new MessageEvent();
                    confirmationStopped.setStatus(404);
                    consoleServiceBus = consoleServiceBus + "Stopping connection.\n";
                    consoleServiceBus = consoleServiceBus + "Reason: Bluetooth is turned off.\n";
                    confirmationStopped.setConsole(consoleServiceBus);
                    EventBus.getDefault().post(confirmationStopped);
                    return;
                }


                MessageEvent event = new MessageEvent();
                event.setConsole(consoleServiceBus);
                EventBus.getDefault().post(event);

                ArrayList<Integer> ESP32_read_values = new ArrayList<>();
                BleManager.getInstance().scanAndConnect(new BleScanAndConnectCallback() {
                    @Override
                    public void onScanFinished(BleDevice scanResult) {
                        if(scanResult == null){
                            stopSelf();

                            MessageEvent confirmationStopped = new MessageEvent();
                            confirmationStopped.setStatus(404);
                            consoleServiceBus = consoleServiceBus + "Stopping connection.\n";
                            consoleServiceBus = consoleServiceBus + "Reason: Did not find ESP32.\n";
                            confirmationStopped.setConsole(consoleServiceBus);
                            EventBus.getDefault().post(confirmationStopped);
                            return;

                        }
                    }

                    @Override
                    public void onStartConnect() {
                    }

                    @Override
                    public void onConnectFail(BleDevice bleDevice, BleException exception) {
                        stopSelf();

                        MessageEvent confirmationStopped = new MessageEvent();
                        confirmationStopped.setStatus(404);
                        consoleServiceBus = consoleServiceBus + "Stopping connection.\n";
                        consoleServiceBus = consoleServiceBus + "Reason: Connection failed.\n";
                        confirmationStopped.setConsole(consoleServiceBus);
                        EventBus.getDefault().post(confirmationStopped);
                        return;
                    }

                    int numberofReadingsCollected = 0;

                    @Override
                    public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {

                        statusConnection = 3;
                        MessageEvent event = new MessageEvent();
                        shouldSpin = true;
                        event.setShouldSpin(shouldSpin);
                        event.setStatus(statusConnection);

                        EventBus.getDefault().post(event);

                        ESP32 = bleDevice;

                        BleManager.getInstance().notify(
                                bleDevice,
                                uuid_service,
                                uuid_characteristic_notify,
                                new BleNotifyCallback() {
                                    @Override
                                    public void onNotifySuccess() {
                                        BleManager.getInstance().setMtu(bleDevice, 512, new BleMtuChangedCallback() {
                                                    @Override
                                                    public void onSetMTUFailure(BleException exception) {}
                                                    @Override
                                                    public void onMtuChanged(int mtu) {}
                                                }
                                        );


                                    }
                                    @Override
                                    public void onNotifyFailure(BleException exception) {

                                    }
                                    int leituraAnterior = 0;

                                    @Override
                                    public void onCharacteristicChanged(byte[] data) {



                                        int numberBytes = data.length;
                                        int i = 0;
                                        while(i<numberBytes){
                                            byte[] transferByte = {data[i], data[i+1]};
                                            ESP32_read_values.add(hex_to_Integer(transferByte));
                                            i +=2;
                                            numberofReadingsCollected++;
                                            leituraAnterior = hex_to_Integer(transferByte);
                                        }
                                    }
                                });
                    }

                    @Override
                    public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {

                        consoleServiceBus = consoleServiceBus + "Received a total of " + numberofReadingsCollected + " readings.\n";
                        consoleServiceBus = consoleServiceBus + "Disconnected and waiting for another " + delayBLEOFF/1000 + " seconds.\n";


                        while(consoleServiceBus.length()>8000){
                            consoleServiceBus = consoleServiceBus.substring(100);

                        }
                        MessageEvent event = new MessageEvent();
                        shouldSpin = false;
                        event.setShouldSpin(shouldSpin);
                        event.setConsole(consoleServiceBus);
                        EventBus.getDefault().post(event);

                        BleManager.getInstance().stopNotify(ESP32, uuid_service,uuid_characteristic_notify);
                        sendPost(ESP32_read_values);
                        superConnection(); //repetir o código após ser desconectado.

                    }

                    @Override
                    public void onScanStarted(boolean success) {

                    }

                    @Override
                    public void onScanning(BleDevice bleDevice) {


                    }
                });



            }
        }.start();




    }

    Integer hex_to_Integer(byte[] data) {
        byte[] byteFinal = {data[0], data[1], 0x0000, 0x0000};
        return java.nio.ByteBuffer.wrap(byteFinal).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();

    }

    boolean finisher = false;

    @SuppressLint("SetTextI18n")
    private void sendPost(ArrayList<Integer> vetor) { //sendPost is ready to send any size of array.

        MessageEvent event = new MessageEvent();
        event.setStatus(4);
        EventBus.getDefault().post(event);

        ArrayList<Integer> enviardeVerdade = new ArrayList<>();
        if(vetor.size()>15000) {
            for (int loopador = 0; loopador < 15000; loopador++) {
                enviardeVerdade.add(vetor.get(0));
                vetor.remove(0);

            }
            finisher = false;
        }else{
            enviardeVerdade = vetor;

            finisher = true;

        }

        consoleServiceBus = consoleServiceBus +"Sending " + enviardeVerdade.size() + " readings to the server.\n";
        MessageEvent myevent = new MessageEvent();

        while(consoleServiceBus.length()>8000){
            consoleServiceBus = consoleServiceBus.substring(100);

        }
        event.setConsole(consoleServiceBus);
        EventBus.getDefault().post(myevent);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://ic-iot.herokuapp.com/api/vetores/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        JsonPlaceHolderApi jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);

        Call<Post> call;

        if(placetoSend == 0){
            AutoPost postAuto = new AutoPost(enviardeVerdade, novaLeitura);
            call = jsonPlaceHolderApi.createPostLatest(postAuto);
            novaLeitura = false;

        }else{
            Post postTest = new Post(enviardeVerdade);
            call = jsonPlaceHolderApi.createPostspecific(postTest, placetoSend);
        }




        call.enqueue(new Callback<Post>() {
            @Override
            public void onResponse(@NonNull Call<Post> call, @NonNull Response<Post> response) {

                consoleServiceBus = consoleServiceBus + "Server confirmed receiving readings.\n";
                MessageEvent event = new MessageEvent();

                while(consoleServiceBus.length()>8000){
                    consoleServiceBus = consoleServiceBus.substring(100);

                }
                event.setConsole(consoleServiceBus);
                EventBus.getDefault().post(event);

                if(!finisher){
                    sendPost(vetor);
                }

            }

            @Override
            public void onFailure(@NonNull Call<Post> call, @NonNull Throwable t) {

            }
        });


    }

    @Subscribe
    public void onEvent(MessageEvent event){
        if(event.getStatus() == 6){
            killYourself = true;

        }

    }



}