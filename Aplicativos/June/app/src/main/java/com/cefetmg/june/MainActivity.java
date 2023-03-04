package com.cefetmg.june;



import static android.os.VibrationEffect.EFFECT_HEAVY_CLICK;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.os.Vibrator;


import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleMtuChangedCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleScanAndConnectCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.BleScanRuleConfig;

import android.location.LocationManager;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity {

    Button left1, right1, left2, right2, left3, right3, mainButton, changeMode;
    TextView text1, text2, text3;

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch editSwitch;

    int mode = 0; //0 = default 1 = legacy 2 = superConnection

    TextView consoleLog;
    BleDevice ESP32;

    boolean shouldWrite = false;
    boolean canPost = true;
    boolean serviceEnder = false;
    boolean wasFirstConnected = false;

    int timesConnected = 0;


    String uuid_service = "4fafc201-1fb5-459e-8fcc-c5c9c331914b";
    String uuid_characteristic_notify = "beb5483e-36e1-4688-b7f5-ea07361b26a8";

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Vibrator vibrater = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        setContentView(R.layout.activity_main4);

        define_variables();
        BleManager.getInstance().init(getApplication());

        adder(right1, left1, text1, "ms", vibrater);
        adder(right2, left2, text2, "s", vibrater);
        adder(right3, left3, text3, "s", vibrater);

        remover(left1, text1, "ms", vibrater);
        remover(left2, text2, "s", vibrater);
        remover(left3, text3, "s", vibrater);


        editSwitch.setOnCheckedChangeListener(
                (compoundButton, b) -> {

                    shouldWrite = true;
                    vibrater.vibrate(EFFECT_HEAVY_CLICK);
                    setDefaultValue(text1, "3ms");
                    setDefaultValue(text2, "5s");
                    setDefaultValue(text3, "10s");

                    if(b){

                        if(text1.getText().equals("3ms")) {
                            left1.setVisibility(View.INVISIBLE);
                        }else{
                            left1.setVisibility(View.VISIBLE);
                        }

                        if(text2.getText().equals("2s")){
                            left2.setVisibility(View.INVISIBLE);
                        }else{
                            left2.setVisibility(View.VISIBLE);
                        }

                        if(text3.getText().equals("2s")){
                            left3.setVisibility(View.INVISIBLE);
                        }else{
                            left3.setVisibility(View.VISIBLE);
                        }

                        right1.setVisibility(View.VISIBLE);
                        right2.setVisibility(View.VISIBLE);
                        right3.setVisibility(View.VISIBLE);


                    }else{
                        left1.setVisibility(View.INVISIBLE);
                        left2.setVisibility(View.INVISIBLE);
                        left3.setVisibility(View.INVISIBLE);
                        right1.setVisibility(View.INVISIBLE);
                        right2.setVisibility(View.INVISIBLE);
                        right3.setVisibility(View.INVISIBLE);

                    }



                }


        );


        changeMode.setOnClickListener(
                v->{
                    switch(changeMode.getText().toString()){
                        case "mode:\nmain":
                            changeMode.setText("mode:\nlegacy");
                            mode = 1;

                            break;
                        case "mode:\nlegacy":
                            changeMode.setText("mode:\ns.Con");
                            mode = 2;


                            break;
                        case "mode:\ns.Con":
                            changeMode.setText("mode:\nmain");
                            mode = 0;

                            break;
                    }
                }
        );

        String[] names = {"AD8232"};

        mainButton.setOnClickListener(
                v-> {

                    //Case not Legacy (new Superboard)
                    switch (mode) {
                        case 0:
                            BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder().setDeviceName(true, names).setScanTimeOut(4000).build();
                            BleManager.getInstance().initScanRule(scanRuleConfig);

                            switch (mainButton.getText().toString()) {
                                case "Connect":
                                    boolean locationEnabled = true;
                                    LocationManager myLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                                        locationEnabled = myLocationManager.isLocationEnabled();
                                    } else {
                                        consoleLog.setText(R.string.GPS);
                                    }

                                    if (locationEnabled) {


                                        serviceEnder = false;

                                        BleManager.getInstance().enableBluetooth();

                                        setDefaultValue(text1, "3ms");
                                        setDefaultValue(text2, "5s");
                                        setDefaultValue(text3, "10s");

                                        mainButton.setText(R.string.searching);

                                        Handler handler = new Handler();
                                        handler.postDelayed(this::firstConnection, 1000);


                                    } else {
                                        consoleLog.setText(R.string.GPS_off);
                                    }

                                    break;

                                case "Disconnect":
                                    serviceEnder = true;
                                    consoleLog.setText(R.string.disconnection);


                                    break;
                            }

                            break;


                        case 1:

                            BleScanRuleConfig scanRuleConfig1 = new BleScanRuleConfig.Builder().setDeviceName(true, names).setScanTimeOut(4000).build();
                            BleManager.getInstance().initScanRule(scanRuleConfig1);

                            switch (mainButton.getText().toString()) {
                                case "Connect":
                                    legacyConnection();

                                    break;


                                case "Disconnect":
                                    BleManager.getInstance().disconnect(ESP32);

                                    break;


                            }
                            break;


                        case 2:







                            switch (mainButton.getText().toString()) {
                                case "Connect":
                                    boolean locationEnabled = true;
                                    LocationManager myLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                                        locationEnabled = myLocationManager.isLocationEnabled();
                                    } else {
                                        consoleLog.setText(R.string.GPS);
                                    }

                                    if (locationEnabled) {


                                        serviceEnder = false;

                                        BleManager.getInstance().enableBluetooth();

                                        setDefaultValue(text1, "3ms");
                                        setDefaultValue(text2, "5s");
                                        setDefaultValue(text3, "10s");


                                        String[] parts = text3.getText().toString().split("s");
                                        BleScanRuleConfig scanRuleConfig2 = new BleScanRuleConfig.Builder().setDeviceName(true, names).setScanTimeOut(Integer.parseInt(parts[0])*1000).build();
                                        BleManager.getInstance().initScanRule(scanRuleConfig2);
                                        superConnection(Integer.parseInt(parts[0])*1000);


                                    } else {
                                        consoleLog.setText(R.string.GPS_off);
                                    }

                                    break;

                                case "Disconnect":
                                    serviceEnder = true;
                                    consoleLog.setText(R.string.disconnection);


                                    break;
                            }


                            break;


                    }
                } );







    }

    boolean finisher = false;

    @SuppressLint("SetTextI18n")
    private void sendPost(ArrayList<Integer> vetor) {

        consoleLog.setText("Sending " + vetor.size()+" readings");

        canPost = false;
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

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://ic-iot.herokuapp.com/api/vetores/publicar/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        JsonPlaceHolderApi jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);
        Post postTest = new Post(enviardeVerdade);
        Call<Post> call = jsonPlaceHolderApi.createPost(postTest);
        call.enqueue(new Callback<Post>() {
            @Override
            public void onResponse(@NonNull Call<Post> call, @NonNull Response<Post> response) {

                if(!finisher){
                    sendPost(vetor);
                }
                canPost = true;
                consoleLog.setText("Done");

            }

            @Override
            public void onFailure(@NonNull Call<Post> call, @NonNull Throwable t) {

            }
        });


    }

    @SuppressLint("ResourceAsColor")
    void define_variables() {

        mainButton = findViewById(R.id.principalButton);

        text1 = findViewById(R.id.number1);
        text2 = findViewById(R.id.number2);
        text3 = findViewById(R.id.number3);

        left1 = findViewById(R.id.left1);
        left2 = findViewById(R.id.left2);
        left3 = findViewById(R.id.left3);

        right1 = findViewById(R.id.right1);
        right2 = findViewById(R.id.right2);
        right3 = findViewById(R.id.right3);

        editSwitch = findViewById(R.id.switch1);
        changeMode = findViewById(R.id.button);


        consoleLog = findViewById(R.id.textView2);

        getWindow().setNavigationBarColor(R.color.pink);

    }


    String hex_to_String(byte[] data) {
        byte[] byteFinal = {data[0], data[1], 0x0000, 0x0000};
        int x = java.nio.ByteBuffer.wrap(byteFinal).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
        return String.format(Locale.getDefault(), "%d", x);
    }

    byte[] toByteArray(int value) {
        return new byte[] {
                (byte)(value >> 24),
                (byte)(value >> 16),
                (byte)(value >> 8),
                (byte)value };
    }

    Integer hex_to_Integer(byte[] data) {
        byte[] byteFinal = {data[0], data[1], 0x0000, 0x0000};
        return java.nio.ByteBuffer.wrap(byteFinal).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();

    }

    byte[] String_to_hex(String toConvert){
        Charset charset = StandardCharsets.US_ASCII;
        return toConvert.getBytes(charset);

    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    void adder(Button b, Button opposite, TextView t, String measure, Vibrator vibrater) {
        b.setOnClickListener(
                v -> {
                        String[] parts = t.getText().toString().split(measure);
                        String resposta = Integer.parseInt(parts[0]) + 1 + measure;

                    if(resposta.equals("4ms")){
                        opposite.setVisibility(VISIBLE);
                    }
                    if(resposta.equals("3s")){
                        opposite.setVisibility(VISIBLE);
                    }

                        t.setText(resposta);
                        vibrater.vibrate(EFFECT_HEAVY_CLICK);


                }
        );


    }

    void remover(Button b, TextView t, String measure, Vibrator vibrater) {
        b.setOnClickListener(
                v -> {

                        String[] parts = t.getText().toString().split(measure);
                        String resposta = String.valueOf(Integer.parseInt(parts[0]) - 1) + measure;
                        if(resposta.equals("3ms")){
                            b.setVisibility(INVISIBLE);
                        }
                        if(resposta.equals("2s")){
                            b.setVisibility(INVISIBLE);
                        }
                        t.setText(resposta);
                        vibrater.vibrate(EFFECT_HEAVY_CLICK);

                }
        );


    }

    void setDefaultValue(TextView t, String defaultValue){
        if(t.getText().equals("")){
            t.setText(defaultValue);
        }
    }

    String getTextViewText(TextView t, String measure){
        String[] parts = t.getText().toString().split(measure);
        return String.valueOf(Integer.parseInt(parts[0]));

    }

    void writeTextViewValues(BleDevice bleDevice){
        if(shouldWrite){



            String aEnviar = getTextViewText(text1, "ms") + "-"+ getTextViewText(text2, "s") + "-"+ getTextViewText(text3, "s") ;


            //To characteristic1 - ESP32Delay 3ms
            BleManager.getInstance().write(bleDevice, "4fafc201-1fb5-459e-8fcc-c5c9c331914b",
                    "beb5483e-36e1-4688-b7f5-ea07361b26a8", String_to_hex(aEnviar), new BleWriteCallback() {
                        @Override
                        public void onWriteSuccess(int current, int total, byte[] justWrite) {

                        }

                        @Override
                        public void onWriteFailure(BleException exception) {

                        }
                    }
            );






        }else{
            String aEnviar = "1";


            //To characteristic1 - ESP32Delay 3ms
            BleManager.getInstance().write(bleDevice, "4fafc201-1fb5-459e-8fcc-c5c9c331914b",
                    "beb5483e-36e1-4688-b7f5-ea07361b26a8", String_to_hex(aEnviar), new BleWriteCallback() {
                        @Override
                        public void onWriteSuccess(int current, int total, byte[] justWrite) {

                        }

                        @Override
                        public void onWriteFailure(BleException exception) {

                        }
                    }
            );
        }


    }

    void setAllVisible(){
        text1.setVisibility(VISIBLE);
        text2.setVisibility(VISIBLE);
        text3.setVisibility(VISIBLE);
    }

    void setAllInvisible(){
        text1.setVisibility(INVISIBLE);
        text2.setVisibility(INVISIBLE);
        text3.setVisibility(INVISIBLE);
    }

    void firstConnection(){

        wasFirstConnected = true;
        BleManager.getInstance().scanAndConnect(new BleScanAndConnectCallback() {
            @Override
            public void onScanFinished(BleDevice scanResult) {
                if(scanResult == null){

                    consoleLog.setText(R.string.not_found);
                    wasFirstConnected = false;
                    Handler handler = new Handler();
                    handler.postDelayed(() -> {

                        consoleLog.setText("");
                        mainButton.setText(R.string.connect);
                    }, 3000);

                }
            }

            @Override
            public void onStartConnect() {
                mainButton.setText(R.string.connecting);


            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                mainButton.setText(R.string.connection_failed);
                Handler handler = new Handler();
                handler.postDelayed(() -> {

                    consoleLog.setText("");
                    mainButton.setText(R.string.connect);
                }, 3000);

                wasFirstConnected = false;

            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {

                writeTextViewValues(bleDevice);
                mainButton.setText(R.string.disconnect);
                ESP32 = bleDevice;

            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
                String[] parts = text3.getText().toString().split("s");
                superConnection(Integer.parseInt(parts[0])*1000); //repetir o código após ser desconectado.

            }

            @Override
            public void onScanStarted(boolean success) {
                mainButton.setText(R.string.searching);
            }

            @Override
            public void onScanning(BleDevice bleDevice) {
                mainButton.setText(R.string.searching);

            }
        });

    }

    void superConnection(int delayBLEOFF){

        //Objetivo: Conectar na ESP32 via BLE, ser desconectado, esperar o delayBLEOFF, ser desconectado, repetir.
        Handler loopMethod = new Handler();
        Runnable runnableCode = () -> {

            boolean justThisOnce = wasFirstConnected;
            //Como o app todo roda em simultâneo, quando chega na parte do "connected", wasfirstconnected já é true
            //Sendo assim faz-se necessário copiar o valor de wasFirstConnected para a memória para enviar para a função
            //de conexão


            timesConnected++;
            consoleLog.setText("Collection "+ timesConnected);
            ArrayList<Integer> ESP32_read_values = new ArrayList<>();
            BleManager.getInstance().scanAndConnect(new BleScanAndConnectCallback() {
                @Override
                public void onScanFinished(BleDevice scanResult) {
                    if(scanResult == null){

                        consoleLog.setText(R.string.not_found);
                        String[] parts = text2.getText().toString().split("s");
                        int aEsperar = Integer.parseInt(parts[0]) *1000 - 4000;
                        Handler handler = new Handler();
                        handler.postDelayed(() -> {
                            superConnection(delayBLEOFF);


                        }, aEsperar);

                    }
                }

                @Override
                public void onStartConnect() {
                }

                @Override
                public void onConnectFail(BleDevice bleDevice, BleException exception) {

                    String[] parts = text2.getText().toString().split("s");
                    int aEsperar = Integer.parseInt(parts[0]) *1000;
                    Handler handler = new Handler();
                    handler.postDelayed(() -> {

                        superConnection(delayBLEOFF);
                    }, aEsperar);

                }
boolean canKill;

                @Override
                public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {

                    if(!justThisOnce){
                        mainButton.setText("Disconnect");
                    }


                    if(serviceEnder){
                        BleManager.getInstance().disconnect(bleDevice);
                        canKill=true;
                        return;

                    }
                    serviceEnder = false;

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
                                        consoleLog.setText(hex_to_String(transferByte));
                                        ESP32_read_values.add(hex_to_Integer(transferByte));

                                        i +=2;
                                        leituraAnterior = hex_to_Integer(transferByte);
                                    }


                                }
                            });
                }

                @Override
                public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
                    if(canKill){
                        mainButton.setText(R.string.connect);
                        consoleLog.setText("");
                        wasFirstConnected = false;
                        BleManager.getInstance().disableBluetooth();
                        return;
                    }


                    BleManager.getInstance().stopNotify(ESP32, uuid_service,uuid_characteristic_notify);
                    sendPost(ESP32_read_values);
                    superConnection(delayBLEOFF); //repetir o código após ser desconectado.
                }

                @Override
                public void onScanStarted(boolean success) {

                }

                @Override
                public void onScanning(BleDevice bleDevice) {


                }
            });


            if(!wasFirstConnected){
                String[] names = {"AD8232"};
                BleScanRuleConfig scanRuleConfig2 = new BleScanRuleConfig.Builder().setDeviceName(true, names).setScanTimeOut(4000).build();
                BleManager.getInstance().initScanRule(scanRuleConfig2);
                wasFirstConnected = true;
            }
        };



        if(!wasFirstConnected){
            loopMethod.postDelayed(runnableCode, 0);
        }else{
            loopMethod.postDelayed(runnableCode, delayBLEOFF);
        }




    }

    void legacyConnection(){



            ArrayList<Integer> ESP32_read_values = new ArrayList<>();
            BleManager.getInstance().scanAndConnect(new BleScanAndConnectCallback() {
                @Override
                public void onScanFinished(BleDevice scanResult) {
                    if(scanResult == null){

                        consoleLog.setText(R.string.not_found);
                        Handler handler = new Handler();
                        handler.postDelayed(() -> {

                            consoleLog.setText("");
                            mainButton.setText(R.string.connect);
                        }, 3000);

                    }
                }

                @Override
                public void onStartConnect() {
                    mainButton.setText(R.string.connecting);
                }

                @Override
                public void onConnectFail(BleDevice bleDevice, BleException exception) {
                    mainButton.setText(R.string.connection_failed);
                    Handler handler = new Handler();
                    handler.postDelayed(() -> {

                        consoleLog.setText("");
                        mainButton.setText(R.string.connect);
                    }, 3000);

                }

                @Override
                public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                    mainButton.setText(R.string.disconnect);
                    ESP32 = bleDevice;

                    BleManager.getInstance().notify(
                            bleDevice,
                            uuid_service,
                            uuid_characteristic_notify,
                            new BleNotifyCallback() {
                                @Override
                                public void onNotifySuccess() {
                                    consoleLog.setText("Notify Success");
                                }
                                @Override
                                public void onNotifyFailure(BleException exception) {
                                    consoleLog.setText("Notify Failure");
                                }
                                @Override
                                public void onCharacteristicChanged(byte[] data) {


                                    byte[] transferByte = {data[0], data[1], 0x0000, 0x0000};
                                    int x = java.nio.ByteBuffer.wrap(transferByte).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
                                        consoleLog.setText(Integer.toString(x));
                                        ESP32_read_values.add(x);

                                }
                            });








                }

                @Override
                public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
                    BleManager.getInstance().stopNotify(ESP32, uuid_service,uuid_characteristic_notify);
                    mainButton.setText(R.string.connect);
                    sendPost(ESP32_read_values);
                }

                @Override
                public void onScanStarted(boolean success) {
                    mainButton.setText(R.string.searching);
                }

                @Override
                public void onScanning(BleDevice bleDevice) {
                    mainButton.setText(R.string.searching);

                }
            });


    }

}
