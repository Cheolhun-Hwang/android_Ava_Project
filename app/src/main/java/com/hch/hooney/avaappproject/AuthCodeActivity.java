package com.hch.hooney.avaappproject;

import android.app.Activity;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.hch.hooney.avaappproject.Alert.AvaJustAlert;
import com.hch.hooney.avaappproject.Alert.myAlert;
import com.hch.hooney.avaappproject.Application.AvaApp;
import com.hch.hooney.avaappproject.BleHandler.AvaBleHandler;
import com.hch.hooney.avaappproject.SupportTool.AvaDateTime;

public class AuthCodeActivity extends AppCompatActivity {
    private final String TAG = AuthCodeActivity.class.getSimpleName();

    private EditText typingText;
    private Button confirmBTN;
    private ProgressBar progressBar;
    private Thread startSearchThread;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_code);

        init();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(AvaApp.isFinishAuth(AuthCodeActivity.this)){
            AvaApp.AvaCode = AvaApp.getAvaDeviceCode(AuthCodeActivity.this);

            if(AvaApp.isFinishNickName(AuthCodeActivity.this)){
                if(AvaApp.isFinishWifi(AuthCodeActivity.this)){
                    intentMain();
                }else{
                    intentWifi();
                }
            }else{
                intentNickName();
            }
        }else{
            if(!AvaApp.AvaBle.checkFeatureBLE()){
                showNoBleFeature();
            }
        }
    }

    @Override
    protected void onStop() {
        if(startSearchThread != null){
            if(startSearchThread.isAlive()){
                startSearchThread.interrupt();
            }
            startSearchThread = null;
        }
        super.onStop();
    }

    private void init(){
        //Ble 초기 설정 확인
        AvaApp.initMethod(AuthCodeActivity.this);

        typingText = (EditText) findViewById(R.id.init_bluth_edit);
        confirmBTN = (Button) findViewById(R.id.init_connect_button);
        confirmBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(AvaApp.AvaBle.nowService == null){
                    confirmBTN.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    typingText.setFocusable(false);
                    if(!checkAvaAuthKey()){
                        confirmBTN.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        typingText.setFocusableInTouchMode(true);
                    }
                }else{
                    if(equalKeys()){
                        intentWifi();
                    }else{
                        saidFail("인증키가 다릅니다.");
                    }
                }
            }
        });
        progressBar = (ProgressBar) findViewById(R.id.init_progress_bar);

        handler = initHandler();
    }

    private Handler initHandler(){
        return new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what){
                    case 101:
                        //인증 성공.
                        AvaApp.saveFinishAuth(AuthCodeActivity.this, true, AvaDateTime.getNowDateTime());
                        AvaApp.saveAvaDeviceCode(AuthCodeActivity.this, AvaApp.AvaCode);
                        intentNickName();
                        break;
                    case 102 :
                    case 103 :
                    case 104 :
                        //검색 실패.
                        saidFail(msg.obj.toString());
                        break;
                }
                return true;
            }
        });
    }

    private void saidFail(String msg){
        callAvaJustAlert(msg);
        confirmBTN.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        typingText.setFocusableInTouchMode(true);
    }

    private void intentWifi(){
        Intent intent = new Intent(getApplicationContext(), WifiActivity.class);
        intent.putExtra("init_process", true);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    private void intentNickName(){
        Intent intent = new Intent(getApplicationContext(), NickNameActivity.class);
        intent.putExtra("init_process", true);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    private void intentMain(){
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    private Thread initStartSearchThread(){
        return new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = handler.obtainMessage();
                if(AvaApp.AvaBle.callBleSearch()){
                   AvaApp.AvaBle.getAuthKey();
                    int count = 0;
                    while (true){
                        try {
                            Thread.sleep(1000);
                            count++;
                            Log.d(TAG, "Progress : " + count+" sec...");
                        } catch (InterruptedException e) {
                            Log.e(TAG, "Read Auth Character UUID Error...");
                            e.printStackTrace();
                        }

                        if(AvaApp.AvaBle.getRes() != null){
                            Log.d(TAG, "get Res : " + AvaApp.AvaBle.getRes().toLowerCase());
                            Log.d(TAG, "typing : " + typingText.getText().toString().toLowerCase());
                            if(equalKeys()){
                                msg.what = 101;
                            }else{
                                Log.d(TAG, "RES : " + AvaApp.AvaBle.getRes());
                                msg.obj = "인증키가 다릅니다.";
                                msg.what = 104;
                            }
                            break;
                        }

                        if(count > 30){
                            msg.what = 103;
                            msg.obj = "검색 시간이 초과되었습니다.";
                            break;
                        }
                    }
                }else{
                    msg.what = 102;
                    msg.obj = "AvA 장치를 찾을 수 없습니다.";
                }

                handler.sendMessage(msg);
            }
        });
    }

    private boolean equalKeys(){
        if(AvaApp.AvaBle.getRes().toLowerCase().equals(
                typingText.getText().toString().toLowerCase())) {
            AvaApp.AvaCode = AvaApp.AvaBle.getRes();
            return true;
        }else{
            return false;
        }
    }
    private boolean checkAvaAuthKey(){
        if(AvaApp.AvaBle.isOnBle()){
            String typing = typingText.getText().toString();
            if(typing.length() > 0){
                if(typing.toLowerCase().contains("ava-") && typing.length()>9){
                    startSearch();
                }else{
                    callAvaJustAlert("잘못된 인증키 입니다.");
                    return false;
                }
            }else{
                callAvaJustAlert("AvA 장치의 인증키를 입력해주세요.");
                return false;
            }
            return true;
        }else{
            AvaApp.AvaBle.onBluetooth(AuthCodeActivity.this);
            return false;
        }
    }

    private void startSearch(){
        Log.d(TAG, "BLE Search Start...");
        if(startSearchThread != null){
            if(startSearchThread.isAlive()){
                startSearchThread.interrupt();
            }
            startSearchThread = null;
        }

        startSearchThread = initStartSearchThread();
        startSearchThread.start();
    }

    private void callAvaJustAlert(String msg){
        AvaJustAlert alert = new AvaJustAlert(AuthCodeActivity.this);
        alert.setTitle("AvA 인증");
        alert.setMessage(msg);
        alert.setPositiveButton("확인");
        alert.show();
    }

    private void showNoBleFeature(){
        myAlert alert = new myAlert(AuthCodeActivity.this);
        alert.setTitle("AvA 인증");
        alert.setMessage("스마트 블루투스 기능을 제공하지 않습니다.");
        alert.setPositiveButton("종료", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), "인증을 위해 블루투스 기능이 필요합니다.",
                        Toast.LENGTH_SHORT).show();
            }
        });
        alert.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == AvaApp.AvaBle.REQUEST_ENABLE){
            if(resultCode == Activity.RESULT_OK){
                Toast.makeText(getApplicationContext(), "블루투스 기능이 실행되었습니다.",
                        Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getApplicationContext(), "인증을 위해 블루투스 기능이 필요합니다.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
