package com.hch.hooney.avaappproject;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hch.hooney.avaappproject.Alert.AvaJustAlert;
import com.hch.hooney.avaappproject.Application.AvaApp;

public class SettingsActivity extends AppCompatActivity {
    private final String TAG = SettingsActivity.class.getSimpleName();
    private final int INIT_PROCESS_SIG = 711;

    private TextView versionNotify, nickNameNotify, wifiSsidNotify;
    private Button initBTN;
    private SwitchCompat orderBleSwitch;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        init();
    }

    @Override
    public void onBackPressed() {
        finishForResultAddEvent(Activity.RESULT_CANCELED);
        super.onBackPressed();
    }

    @Override
    protected void onStart() {
        super.onStart();

        setUi();
    }

    private void init(){
        if(AvaApp.fDatabase == null){
            AvaApp.initFDatabase();
        }

        versionNotify = (TextView) findViewById(R.id.setting_version);
        nickNameNotify = (TextView) findViewById(R.id.setting_nick_notify_text);
        wifiSsidNotify = (TextView) findViewById(R.id.setting_now_ava_ssid);
        orderBleSwitch = (SwitchCompat) findViewById(R.id.setting_useBlueth);
        progressBar = (ProgressBar) findViewById(R.id.setting_init_progress);

        //Buttons
        findViewById(R.id.setting_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishForResultAddEvent(Activity.RESULT_CANCELED);
            }
        });

        findViewById(R.id.setting_nick_change_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callMethodChangeNickName();
            }
        });

        findViewById(R.id.setting_wifi_connection).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WifiActivity.class);
                intent.putExtra("init_process", false);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        initBTN = (Button) findViewById(R.id.setting_init_button);
        initBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                warningInitAlert();
            }
        });

        orderBleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AvaApp.saveAvaBLEOrder(SettingsActivity.this, isChecked);
                AvaApp.AvaBLEOrder = AvaApp.isAvaBLEOrder(SettingsActivity.this);
            }
        });
    }

    private void setUi(){
        showVersion();
        showOrderBLE();
        showWifiSSID();
        showNickName();
    }

    private void showVersion(){
        try {
            versionNotify.setText("Ver. "+getPackageManager().getPackageInfo(getApplicationContext().
                    getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            versionNotify.setText("Ver. 1.0.0");
        }
    }

    private void showOrderBLE(){
        Log.d(TAG, "BLE Order: " + AvaApp.AvaBLEOrder);
        orderBleSwitch.setChecked(AvaApp.AvaBLEOrder);
    }

    private void showWifiSSID(){
        String ssid = AvaApp.getWifiSSID(SettingsActivity.this);
        wifiSsidNotify.setText( ((ssid!=null)?ssid:"연결되지 않음") );
    }

    private void showNickName(){
        nickNameNotify.setText(AvaApp.AvaUserNickName);
    }

    private void progressInitUi(){
        progressBar.setVisibility(View.VISIBLE);
        initBTN.setVisibility(View.GONE);
    }

    private void endInitUi(){
        progressBar.setVisibility(View.GONE);
        initBTN.setVisibility(View.VISIBLE);
    }

    private void warningInitAlert(){
        AlertDialog.Builder alert = new AlertDialog.Builder(SettingsActivity.this);
        alert.setTitle("Ava 초기화");
        alert.setMessage("초기화 시에 기존의 모든 데이터가 삭제됩니다. 계속 진행하시겠습니까?");
        alert.setPositiveButton("초기화", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progressInitUi();
                startInitProgress();
            }
        }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.show();
    }

    private void startInitProgress(){
        final DatabaseReference fCertification = AvaApp.fDatabase.getReference().child("Certification");
        fCertification.child("Device")
                .child(AvaApp.AvaCode)
                .child("Users")
                .child(AvaApp.getFinishAuthDate(SettingsActivity.this))
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        fCertification.child("User")
                                .child(AvaApp.AvaUserCode)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        clearSharedPre();
                                        notifyInitSuccess();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        fCertification.child("Device")
                                                .child(AvaApp.AvaCode)
                                                .child("Users")
                                                .child(AvaApp.getFinishAuthDate(SettingsActivity.this))
                                                .setValue(AvaApp.AvaUserCode);
                                        endInitUi();
                                        callAvaJustAlert("Ava 초기화","초기화에 실패하였습니다.\n잠시 후 다시 시도해주세요.");
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        endInitUi();
                        callAvaJustAlert("Ava 초기화","초기화에 실패하였습니다.\n잠시 후 다시 시도해주세요.");
                    }
                });
    }

    private void clearSharedPre(){
        AvaApp.saveAvaBLEOrder(SettingsActivity.this, true);
        AvaApp.saveAvaRecentLocation(SettingsActivity.this, 0.0f, 0.0f);
        AvaApp.saveAvaUserCode(SettingsActivity.this, null);
        AvaApp.saveAvaRecentUserNickName(SettingsActivity.this, null);
        AvaApp.saveAvaDeviceCode(SettingsActivity.this, null);
        AvaApp.saveFinishWifi(SettingsActivity.this, false, null);
        AvaApp.saveFinishNickName(SettingsActivity.this, false);
        AvaApp.saveFinishAuth(SettingsActivity.this, false, null);
        AvaApp.saveMacAddress(SettingsActivity.this, null);
        AvaApp.setUserColorSetEmpty(SettingsActivity.this);
    }

    private void callMethodChangeNickName(){
        final AlertDialog.Builder alert = new AlertDialog.Builder(SettingsActivity.this);
        alert.setTitle("Ava 사용자 닉네임 변경");
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_only_edittext, null);
        final EditText getClassName = (EditText) view.findViewById(R.id.dialog_only_edittext);
        getClassName.setHint("# 변경할 닉네임을 작성해주세요.");
        alert.setView(view).setPositiveButton("변경", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try{
                    String changeNickName = getClassName.getText().toString();
                    if(changeNickName.length() > 0){
                        Log.d(TAG, "Change Nickname : " + changeNickName);
                        changeFirebaseNickName(changeNickName);
                    }else{
                        callAvaJustAlert("Ava 사용자 닉네임 변경", "최소 1자 이상 작성하셔야 합니다.");
                        dialog.dismiss();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alert.show();
    }

    private void changeFirebaseNickName(final String nick){
        nickNameNotify.setText("[ 변경 중... ]");

        AvaApp.fDatabase.getReference()
                .child("Certification")
                .child("User")
                .child(AvaApp.AvaUserCode)
                .child("NickName")
                .setValue(nick)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        nickNameNotify.setText(nick);
                        callAvaJustAlert("Ava 사용자 닉네임 변경", "성공적으로 변경되었습니다.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callAvaJustAlert("Ava 사용자 닉네임 변경", "변경에 실패하였습니다.\n잠시 후 다시 시도해주세요.");
                    }
                });
    }

    private void notifyInitSuccess(){
        AvaJustAlert alert = new AvaJustAlert(SettingsActivity.this);
        alert.setTitle("Ava 초기화");
        alert.setMessage("초기화가 완료되었습니다.");
        alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finishForResultAddEvent(INIT_PROCESS_SIG);
            }
        });
        alert.show();
    }

    private void callAvaJustAlert(String title, String msg){
        AvaJustAlert alert = new AvaJustAlert(SettingsActivity.this);
        alert.setTitle(title);
        alert.setMessage(msg);
        alert.setPositiveButton("확인");
        alert.show();
    }

    private void finishForResultAddEvent(int sig){
        setResult(sig);
        finish();
        overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_top);
    }
}
