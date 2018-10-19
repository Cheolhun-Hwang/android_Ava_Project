package com.hch.hooney.avaappproject;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.hch.hooney.avaappproject.Application.AvaApp;
import com.hch.hooney.avaappproject.SupportTool.AvaCode;

public class LoadingActivity extends AppCompatActivity {

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        startLoading();
    }
    private void startLoading() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                AvaApp.AvaCode = AvaApp.getAvaDeviceCode(LoadingActivity.this);
                AvaApp.AvaUserCode = AvaApp.getAvaUserCode(LoadingActivity.this);
                AvaApp.AvaUserNickName = AvaApp.getAvaRecentUserNickName(LoadingActivity.this);
                AvaApp.isUsingTTS = AvaApp.isAvaUsingTTS(LoadingActivity.this);
                AvaApp.getAvaRecentLocation(LoadingActivity.this);
                AvaApp.getUserColorSet(LoadingActivity.this);

                startActivity(new Intent(getApplicationContext(), PermissionActivity.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
        }, 3000);
    }
}
