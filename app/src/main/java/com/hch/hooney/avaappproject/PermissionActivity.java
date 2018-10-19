package com.hch.hooney.avaappproject;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.hch.hooney.avaappproject.Alert.myAlert;

public class PermissionActivity extends AppCompatActivity {
    private final String TAG = PermissionActivity.class.getSimpleName();
    private final int SIGNAL_PERMISSION = 1001;

    private String[] permissions = {//import android.Manifest;
            android.Manifest.permission.READ_PHONE_STATE,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);

        init();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(checkAllPermission()){
            intentAuthActivity();
        }
    }

    private void init(){
        findViewById(R.id.permission_commit_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               callPermission();
            }
        });
    }

    private void callPermission(){
        if(checkAllPermission()){
            intentAuthActivity();
        }else{
            commitPermission();
        }
    }

    private void intentAuthActivity(){
        startActivity(new Intent(getApplicationContext(), AuthCodeActivity.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    private boolean checkAllPermission(){
        boolean isAll = true;
        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        for (int i = 0; i < permissions.length; i++) {
            permissionCheck = ContextCompat.checkSelfPermission(PermissionActivity.this, permissions[i]);
            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                isAll = false;
                break;
            }
        }
        return isAll;
    }

    private void requirePermission(){
        final myAlert alert = new myAlert(this);
        alert.setTitle("권한 설정");
        alert.setMessage("AvA가 동작하기 위해서는 모든 권한이 필요합니다.");

        alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callPermission();
            }
        });

        alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(),
                        "다시 권한 설정을 눌러주세요.",
                        Toast.LENGTH_SHORT).show();
            }
        });

        alert.show();
    }

    private void commitPermission(){
        ActivityCompat.requestPermissions(PermissionActivity.this, permissions, SIGNAL_PERMISSION);
    }

    // 사용자의 권한 확인 후 사용자의 권한에 대한 응답 결과를 확인하는 콜백 메소드
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] perms, int[] grantResults) {
        if (requestCode == SIGNAL_PERMISSION) {
            boolean isAll = true;
            for(int temp : grantResults){
                if(temp == PackageManager.PERMISSION_DENIED){
                    isAll = false;
                    break;
                }
            }

            if(isAll){
                intentAuthActivity();
            }else{
                requirePermission();
            }
        }
    }//end of onRequestPermissionsResult
}
