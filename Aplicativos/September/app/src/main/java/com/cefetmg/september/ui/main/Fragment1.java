package com.cefetmg.september.ui.main;

import static android.os.VibrationEffect.EFFECT_HEAVY_CLICK;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cefetmg.september.JsonPlaceHolderApi;
import com.cefetmg.september.MessageEvent;
import com.cefetmg.september.R;
import com.cefetmg.september.MainActivity;
import com.cefetmg.september.bluetoothService;
import com.cefetmg.september.testService;
import com.clj.fastble.BleManager;
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


public class Fragment1 extends Fragment {



    ProgressBar progressBar;
    Button mainButton;
    Button compressedSensing;
    Button compressedRatio;
    Button dataFrequency;

    Button target;
    Button delete;
    Button setPost;
    boolean hasCompressedSensing = true;
    int dataFrequencymilis = 3;
    int compressedRatioNumber = 10; //ESP32 recebe números de 1 a 20. Default: 50% -> 10

    int leituratoSend = 0;

    TextView status;


    boolean canConnect = true; //this defines if the code can

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        EventBus.getDefault().register(this);
        return inflater.inflate(R.layout.fragment_1, container, false);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.progressBar);
        mainButton = view.findViewById(R.id.mainButton);
        compressedSensing = view.findViewById(R.id.compressedSensing);
        compressedRatio = view.findViewById(R.id.compressedRatio);
        dataFrequency = view.findViewById(R.id.dataFrequency);

        target = view.findViewById(R.id.target);
        delete = view.findViewById(R.id.delete);
        setPost = view.findViewById(R.id.setPost);
        status = view.findViewById(R.id.status);

        Vibrator vibrater = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);



        mainButton.setOnClickListener(
                v->{
                    switch(mainButton.getText().toString()){

                        case("Start"):
                            vibrater.vibrate(EFFECT_HEAVY_CLICK);
                            if(canConnect) {

                                boolean locationEnabled = true;
                                LocationManager myLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                                    locationEnabled = myLocationManager.isLocationEnabled();
                                } else {
                                    status.setText("Status: \nCheck your GPS");
                                }

                                if (locationEnabled) {


                                    BleManager.getInstance().enableBluetooth();

                                    Handler handler = new Handler();
                                    canConnect = false;
                                    mainButton.setText("Wait");
                                    handler.postDelayed(this::firstConnection, 1000);


                                } else {
                                    status.setText("Status: \nGPS OFF");
                                    Handler myHandler = new Handler();
                                    myHandler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            status.setText("Status: \nDisconnected");
                                        }
                                    }, 1500);
                                }
                            }


                            break;

                        case("Stop"):
                            vibrater.vibrate(EFFECT_HEAVY_CLICK);
                            MessageEvent event = new MessageEvent();
                            event.setStatus(6);
                            EventBus.getDefault().post(event);

                            break;
                    }
                }
        );

        compressedRatio.setOnClickListener(
                v->{
                    vibrater.vibrate(EFFECT_HEAVY_CLICK);
                    String texto = compressedRatio.getText().toString();
                    String conteudo[] = texto.split("%");
                    int compressedRatioAtual = Integer.parseInt(conteudo[0]);

                    if(compressedRatioAtual == 100){
                        compressedRatioAtual = 0;
                    }else{
                        compressedRatioAtual+=5;
                    }
                    compressedRatioNumber = compressedRatioAtual/5; //ESP32 recebe de 1 a 20
                    String respostaFinal = compressedRatioAtual + "%";

                    compressedRatio.setText(respostaFinal);



                }
        );

        compressedSensing.setOnClickListener(
                v->{
                    vibrater.vibrate(EFFECT_HEAVY_CLICK);
                    if(compressedSensing.getText().toString().equals("on")){
                        compressedSensing.setText("off");
                        hasCompressedSensing = false;

                    }else{
                        compressedSensing.setText("on");
                        hasCompressedSensing = true;
                    }



                }
        );

        dataFrequency.setOnClickListener(
                v->{
                    vibrater.vibrate(EFFECT_HEAVY_CLICK);
                    switch(dataFrequency.getText().toString()){
                        case "333hz":
                            dataFrequency.setText("250hz");
                            dataFrequencymilis = 4;
                            break;

                        case "250hz":
                            dataFrequency.setText("200hz");
                            dataFrequencymilis = 5;
                            break;

                        case "200hz":
                            dataFrequency.setText("100hz");
                            dataFrequencymilis = 10;
                            break;

                        case "100hz":
                            dataFrequency.setText("333hz");
                            dataFrequencymilis = 3;
                            break;


                    }
                }
        );

        target.setOnClickListener(
                v->{
                    vibrater.vibrate(EFFECT_HEAVY_CLICK);
                    if(target.getText().toString().equals("default")){
                        target.setText("1");
                        leituratoSend = 1;
                    }else{
                        target.setText(Integer.toString(Integer.parseInt(target.getText().toString()) + 1));
                        leituratoSend++;
                    }

                }
        );

        target.setOnLongClickListener(
                v->{
                    vibrater.vibrate(EFFECT_HEAVY_CLICK);
                    target.setText("default");
                    leituratoSend = 0;
                    return true;
                }
        );
        delete.setOnClickListener(
                v->{

                    vibrater.vibrate(EFFECT_HEAVY_CLICK);
                    if(target.getText().toString().equals("default")){
                        //Delete ALL readings
                        Retrofit retrofit = new Retrofit.Builder()
                                .baseUrl("https://ic-iot.herokuapp.com/api/vetores/")
                                .addConverterFactory(GsonConverterFactory.create())
                                .build();
                        JsonPlaceHolderApi jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);
                        Call<Void> call = jsonPlaceHolderApi.deleteAllPosts();
                        call.enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {

                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {

                            }
                        });

                    }else{

                        //Delete reading shown in "TARGET"
                        Retrofit retrofit = new Retrofit.Builder()
                                .baseUrl("https://ic-iot.herokuapp.com/api/vetores/")
                                .addConverterFactory(GsonConverterFactory.create())
                                .build();
                        JsonPlaceHolderApi jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);
                        Call<Void> call = jsonPlaceHolderApi.deleteSpecific(Integer.parseInt(target.getText().toString()));
                        call.enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {

                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {

                            }
                        });
                    }



                }
        );

        setPost.setOnClickListener(
                v->{

                    vibrater.vibrate(EFFECT_HEAVY_CLICK);
                    if(target.getText().toString().equals("default")){
                        leituratoSend = 0;
                    }else{
                        leituratoSend = Integer.parseInt(target.getText().toString());
                    }
                }
        );






    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onEvent(MessageEvent event){

        mainButton.setText("Stop");
        switch(event.getStatus()){

            case 1:
                break;
            case 2:
                status.setText("Status: \nWaiting");
                break;

            case 3:
                status.setText("Status: \nConnected");
                progressBar.setProgress(0);
                progressBar.setIndeterminate(true);
                break;

            case 404:
                mainButton.setText("Start");
                status.setText("Status: Disconnected");
                progressBar.setProgress(0);
                break;




        }

        if(event.getWaitingTime() != 0){
            progressBar.setProgress(event.getWaitingTime());
        }

        if(event.isShouldSpin()){
            progressBar.setProgress(0);
            progressBar.setIndeterminate(true);
        }else{
            progressBar.setIndeterminate(false);
        }


    }
    void firstConnection(){

        BleManager.getInstance().init(getActivity().getApplication());

        String[] names = {"AD8232"};
        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder().setDeviceName(true, names).setScanTimeOut(4000).build();
        BleManager.getInstance().initScanRule(scanRuleConfig);

        BleManager.getInstance().scanAndConnect(new BleScanAndConnectCallback() {
            @Override
            public void onScanFinished(BleDevice scanResult) {
                canConnect = true;
                if(scanResult == null){
                    status.setText("Status: \nNot found");
                    Handler myHandler = new Handler();
                    myHandler.postDelayed(() -> {
                        mainButton.setText("Start");
                        status.setText("Status: \nDisconnected");
                    }, 1500);

                }
            }

            @Override
            public void onStartConnect() {
                status.setText("Status: \nConnecting");

            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                status.setText("Status: \nFailed connection");
                Handler myHandler = new Handler();
                myHandler.postDelayed(() -> {
                    mainButton.setText("Start");
                    status.setText("Status: \nDisconnected");
                }, 1500);

            }


            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                writeTextViewValues(bleDevice);
            }


            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
                mainButton.setText("Stop");
                Intent serviceIntent = new Intent(getActivity(), bluetoothService.class);
                serviceIntent.putExtra("target", leituratoSend);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    getActivity().startForegroundService(serviceIntent);
                }else{
                    getActivity().startService(serviceIntent);
                }

            }

            @Override
            public void onScanStarted(boolean success) {
                status.setText("Status: Searching");
            }

            @Override
            public void onScanning(BleDevice bleDevice) {
            }
        });

    }
    void writeTextViewValues(BleDevice bleDevice){


        int toSend = hasCompressedSensing ? 1 : 0;
        String aEnviar = dataFrequencymilis + "-" + toSend + "-" + compressedRatioNumber + "-";

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
    byte[] String_to_hex(String toConvert){
        Charset charset = StandardCharsets.US_ASCII;
        return toConvert.getBytes(charset);

    }
}