package com.hch.hooney.avaappproject;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.hch.hooney.avaappproject.Alert.AvaJustAlert;
import com.hch.hooney.avaappproject.Application.AvaApp;
import com.hch.hooney.avaappproject.SupportTool.AvaCode;
import com.hch.hooney.avaappproject.SupportTool.AvaDateTime;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class NickNameActivity extends AppCompatActivity {
    private final String TAG = NickNameActivity.class.getSimpleName();

    private boolean init_process;

    private ProgressBar progressBar;
    private Button confirmBTN;
    private EditText nickNameEdit;
    private LinearLayout nickNameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nick_name);

        init();
    }

    private void init(){
        //App Firebase init
        AvaApp.initFDatabase();

        init_process = getIntent().getBooleanExtra("init_process", false);

        progressBar = (ProgressBar) findViewById(R.id.nick_name_progressbar);
        confirmBTN = (Button)findViewById(R.id.nick_name_confirm_btn);
        nickNameEdit = (EditText) findViewById(R.id.nick_name_edittext);
        nickNameLayout = (LinearLayout) findViewById(R.id.nick_name_layout);

        setEvent();
    }

    private void setEvent(){
        confirmBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(nickNameEdit.getText().length() < 1){
                    callAvaJustAlert("최소 1자 이상 작성해주세요.");
                }else{
                    callCertificationMethod();
                }
            }
        });
    }

    private void callCertificationMethod(){
        nickNameLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        if(init_process){
            callSaveAllAuth();
        }else{
            callJustNickName();
        }
    }

    private void callSaveAllAuth(){
        final String nowDate = AvaApp.getFinishAuthDate(NickNameActivity.this);
        final String userCode = AvaCode.createCode();

        AvaApp.fDatabase.getReference()
                .child("Certification")
                .child("Device")
                .child(AvaApp.AvaCode)
                .child("Users")
                .child(nowDate)
                .setValue(userCode)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        HashMap<String, String> userInfo = new HashMap<>();
                        userInfo.put("Date", nowDate);
                        userInfo.put("Device", AvaApp.AvaCode);
                        userInfo.put("NickName", nickNameEdit.getText().toString());

                        Log.d(TAG, "User Info : " + userInfo.toString());
                        AvaApp.fDatabase.getReference()
                                .child("Certification")
                                .child("User")
                                .child(userCode)
                                .setValue(userInfo)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        AvaApp.saveFinishNickName(NickNameActivity.this, true);
                                        AvaApp.saveAvaRecentUserNickName(NickNameActivity.this, nickNameEdit.getText().toString());
                                        AvaApp.AvaUserNickName = AvaApp.getAvaRecentUserNickName(NickNameActivity.this);
                                        AvaApp.saveAvaUserCode(NickNameActivity.this, userCode);
                                        AvaApp.AvaUserCode = AvaApp.getAvaUserCode(NickNameActivity.this);
                                        if(init_process){
                                            intentWifi();
                                        }else{
                                            intentMain();
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        nickNameLayout.setVisibility(View.VISIBLE);
                                        progressBar.setVisibility(View.GONE);
                                        callAvaJustAlert("Nickname 설정에 실패하였습니다.[2]");
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        nickNameLayout.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        callAvaJustAlert("Nickname 설정에 실패하였습니다.[1]");
                    }
                });
    }

    private void callJustNickName(){
        AvaApp.fDatabase.getReference()
                .child("User")
                .child(AvaApp.AvaCode)
                .child("NickName")
                .setValue(nickNameEdit.getText()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if(init_process){
                            intentWifi();
                        }else{
                            intentMain();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        nickNameLayout.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        callAvaJustAlert("Nickname 설정에 실패하였습니다.[3]");
                    }
                });
    }

    private void callAvaJustAlert(String msg){
        AvaJustAlert alert = new AvaJustAlert(NickNameActivity.this);
        alert.setTitle("Ava 사용자 설정");
        alert.setMessage(msg);
        alert.setPositiveButton("확인");
        alert.show();
    }

    private void intentWifi(){
        Intent intent = new Intent(getApplicationContext(), WifiActivity.class);
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

}
