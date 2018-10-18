package com.hch.hooney.avaappproject;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.hch.hooney.avaappproject.Alert.AvaJustAlert;
import com.hch.hooney.avaappproject.Application.AvaApp;

public class WifiActivity extends AppCompatActivity {
    private String TAG = WifiActivity.class.getSimpleName();

    private ProgressBar progressBar;
    private LinearLayout directWifiSelect, wifiSkipLayout;

    private EditText wifiDirectSSID, wifiDirectPSK;
    private Button wifiDirectConnectBTN, wifiSkipBTN;
    private CheckBox wifiDirectCheckable;
    private TextView wifiSkipNotifyText;

    private Thread connectionThread, wifiConnectThread;
    private Handler handler;
    private String s_req, nowAvaIP, nowAvaSSID;
    private boolean connectType, init_process;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);

        init();
    }

    @Override
    protected void onStart() {
        super.onStart();

        callFirebaseBLEOn();
    }

    @Override
    protected void onStop() {
        if(connectionThread != null){
            if(connectionThread.isAlive()){
                connectionThread.interrupt();
            }
            connectionThread = null;
        }
        if(wifiConnectThread != null){
            if(wifiConnectThread.isAlive()){
                wifiConnectThread.interrupt();
            }
            wifiConnectThread = null;
        }
        super.onStop();
    }

    private void init(){
        //Ble 초기 설정 확인
        AvaApp.initMethod(WifiActivity.this);

        if(AvaApp.fDatabase == null){
            AvaApp.initFDatabase();
        }

        connectType = false;
        init_process = getIntent().getBooleanExtra("init_process", false);

        progressBar = (ProgressBar) findViewById(R.id.wifi_progressbar);
        directWifiSelect = (LinearLayout) findViewById(R.id.wifi_direct_connection_layout);
        wifiSkipLayout = (LinearLayout) findViewById(R.id.wifi_skip_layout);
        //Direct
        wifiDirectSSID = (EditText) findViewById(R.id.wifi_direct_ssid_edittext);
        wifiDirectPSK = (EditText) findViewById(R.id.wifi_direct_psk_edittext);
        wifiDirectConnectBTN = (Button) findViewById(R.id.wifi_direct_connection_btn);
        wifiDirectCheckable = (CheckBox) findViewById(R.id.wifi_direct_password_checkable);
        wifiSkipBTN = (Button) findViewById(R.id.wifi_skip_btn);
        wifiSkipNotifyText = (TextView) findViewById(R.id.wifi_skip_notify_text);

        handler = initHandler();

        setEvent();
    }

    private void setEvent(){
        wifiDirectConnectBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s_ssid = wifiDirectSSID.getText().toString();
                String s_psk = wifiDirectPSK.getText().toString();

                if(s_ssid.equals("")){
                    Log.d(TAG, "s_ssid : empty");
                    Toast.makeText(getApplicationContext(),
                            "와이파이 이름을 작성해주세요.", Toast.LENGTH_SHORT).show();
                    wifiDirectPSK.setFocusable(true);
                }else{
                    Log.d(TAG, "s_ssid : " + s_ssid);

                    if(s_psk.equals("")){
                        Log.d(TAG, "s_psk : empty");
                        s_req = s_ssid;
                    }else{
                        Log.d(TAG, "s_psk : "+s_psk);
                        s_req = s_ssid+"&"+s_psk;
                    }

                    if(AvaApp.AvaBle.nowService == null){
                        connectionThread = initConnectionThread();
                        connectionThread.start();
                    }else{
                        startWriteWifi();
                    }

                }
            }
        });

        wifiDirectCheckable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    wifiDirectPSK.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }else{
                    wifiDirectPSK.setInputType(InputType.TYPE_CLASS_TEXT|
                            InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
            }
        });

        wifiSkipBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AvaApp.saveFinishWifi(WifiActivity.this, true, nowAvaSSID);
                intentMain();
            }
        });
    }

    private void callFirebaseBLEOn(){
        AvaApp.fDatabase.getReference().child("Command").child(AvaApp.AvaCode)
                .child("Auth").setValue(true)
        .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                callGetWifiState();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                AlertDialog.Builder alert = new AlertDialog.Builder(WifiActivity.this);
                alert.setTitle("Ava Wifi 연결");
                alert.setMessage("Ava 블루투스 연결에 실패하였습니다.\n잠시 후 다시 시도해주세요.");
                alert.setNegativeButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });
                alert.show();
            }
        });
    }

    private void callFirebaseBLEOff(){
        AvaApp.fDatabase.getReference().child("Command").child(AvaApp.AvaCode)
                .child("Auth").setValue(false);
    }

    private Handler initHandler(){
        return new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what){
                    case 101:
                        //이미 wifi 연결됨.
                        if(!connectType){
                            //실행시
                            if(init_process){
                                wifiSkipLayout.setVisibility(View.VISIBLE);
                            }
                            directWifiSelect.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            wifiSkipNotifyText.setText(
                                    "AvA 는 현재 " + nowAvaSSID + " 와이파이와 연결되어 있습니다.\n"+
                            "- ip : "+nowAvaIP + " -");
                        }else{
                            //중도 연결 끊김
                            startWriteWifi();
                        }
                        connectionThread.interrupt();
                        connectionThread = null;
                        break;
                    case 102 :
                    case 103 :
                        //wifi 연결되지 않음.
                        directWifiSelect.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        connectionThread.interrupt();
                        connectionThread = null;
                        saidFail(msg.obj.toString(), false);
                        break;
                    case 201:
                        //연결 완료
                        if(init_process){
                            AvaApp.saveFinishWifi(WifiActivity.this, true, nowAvaSSID);
                            intentMain();
                        }else{
                            AvaApp.saveFinishWifi(WifiActivity.this, true, nowAvaSSID);
                            intentSettings();
                        }
                        break;
                    case 202:
                    case 203:
                    case 204:
                    case 205:
                        //연결 실패
                        saidFail(msg.obj.toString(), true);
                        AvaApp.AvaBle.scanLeDevice(false);
                        break;
                }
                return true;
            }
        });
    }

    private Thread initConnectionThread(){
        return new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = handler.obtainMessage();
                AvaApp.AvaBle.setRes(null);
                if(AvaApp.AvaBle.callBleSearch()){
                    AvaApp.AvaBle.getWifiState();
                    int count = 0;
                    while (true){
                        try {
                            Thread.sleep(3000);
                            count++;
                            Log.d(TAG, "Progress : " + count+" sec...");
                        } catch (InterruptedException e) {
                            Log.e(TAG, "Read Auth Character UUID Error...");
                            e.printStackTrace();
                        }
                        if(AvaApp.AvaBle.getRes() != null){
                            String[] sp_res = AvaApp.AvaBle.getRes().split("&");
                            nowAvaIP=sp_res[1];
                            nowAvaSSID=sp_res[0];
                            msg.what = 101;
                            break;
                        }

                        if(count > 60){
                            msg.what = 103;
                            msg.obj = "[인증실패]\n검색 시간이 초과되었습니다.\n일부 스마트폰의 경우 위치 기능이 필요합니다.";
                            break;
                        }
                    }
                }else{
                    msg.what = 102;
                    msg.obj = "[인증실패]\nAvA 장치를 찾을 수 없습니다.\n일부 스마트폰의 경우 위치 기능이 필요합니다.";
                }
                handler.sendMessage(msg);
            }
        });
    }

    private Thread initWifiConnectThread(){
        return new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = handler.obtainMessage();
                AvaApp.AvaBle.setRes(null);
                if(AvaApp.AvaBle.nowService == null){
                    if(!AvaApp.AvaBle.callBleSearch()) {
                        msg.what = 205;
                        msg.obj = "AvA 장치를 찾을 수 없습니다.";
                    }
                }

                if(AvaApp.AvaBle.writeWifi(s_req)){
                    while (true){
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if(AvaApp.AvaBle.getRes() != null){
                            String[] res = AvaApp.AvaBle.getRes().split("&");
                            if(res[1].equals("undefined") || res[0].equals("undefined")){
                                msg.what = 202;
                                msg.obj = "wifi 연결 정보를 받지 못했습니다 [2]";
                            }else{
                                nowAvaIP = res[1];
                                nowAvaSSID = res[0];
                                msg.what = 201;
                            }
                            break;
                        }else{
                            msg.what = 203;
                            msg.obj = "wifi 연결 정보를 받지 못했습니다. [1]";
                        }
                    }
                }else{
                    msg.what = 204;
                    msg.obj = "wifi 연결에 실패하였습니다. [1]";
                }

                handler.sendMessage(msg);
            }
        });
    }

    private void startWriteWifi(){
        if(wifiSkipLayout.getVisibility() == View.VISIBLE){
            wifiSkipLayout.setVisibility(View.GONE);
        }
        directWifiSelect.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        wifiConnectThread = initWifiConnectThread();
        wifiConnectThread.start();
    }

    private void callGetWifiState(){
        directWifiSelect.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        connectionThread = initConnectionThread();
        connectionThread.start();
    }

    private void saidFail(String msg, boolean type){
        callAvaJustAlert(msg, type);
        directWifiSelect.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    private void callAvaJustAlert(String msg, boolean type){
        AvaJustAlert alert = new AvaJustAlert(WifiActivity.this);
        alert.setTitle("Ava 와이파이 연결");
        alert.setMessage(msg);
        if(type){
            alert.setPositiveButton("확인");
        }else{
            alert.setPositiveButton("위치 기능 켜기", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            }).setNegativeButton("닫기", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }

        alert.show();
    }

    private void intentMain(){
        callFirebaseBLEOff();
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    private void intentSettings(){
        callFirebaseBLEOff();
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
