package com.hch.hooney.avaappproject;

import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.hch.hooney.avaappproject.Application.AvaApp;
import com.hch.hooney.avaappproject.SupportTool.AvaDateTime;

import java.util.ArrayList;
import java.util.HashMap;

public class RemoteDetailActivity extends AppCompatActivity {
    private final String TAG = RemoteDetailActivity.class.getSimpleName();

    private String deviceName;
    private String macAddress;
    private static class deviceMethods{
        public static String variableName;
        private String methodKey;
        private String methodDetail;

        public deviceMethods(){
            methodDetail = null;
            methodKey = null;
        }

        public String getMethodKey() {
            return methodKey;
        }

        public void setMethodKey(String methodKey) {
            this.methodKey = methodKey;
        }

        public String getMethodDetail() {
            return methodDetail;
        }

        public void setMethodDetail(String methodDetail) {
            this.methodDetail = methodDetail;
        }
    }
    private ArrayList<deviceMethods> remoteDetailList;
    private String nowState;

    private ImageButton backBTN;
    private TextView deviceNameView, deviceStateView;
    private ProgressBar progressBar;
    private LinearLayout detailLayout, buttonsLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_detail);

        macAddress = getIntent().getStringExtra("macAddress");
        if(macAddress == null){
            Toast.makeText(getApplicationContext(), "잘못된 정보입니다. 잠시 후 다시 시도해주세요.",
                    Toast.LENGTH_SHORT).show();
            finishAddEvent();
        }

        init();
        getList();
        setEvent();
    }

    @Override
    public void onBackPressed() {
        finishAddEvent();
    }

    private void init(){
        if(AvaApp.fDatabase == null){
            AvaApp.initFDatabase();
        }

        remoteDetailList = new ArrayList<>();

        backBTN = (ImageButton) findViewById(R.id.remote_detail_back);
        deviceNameView = (TextView) findViewById(R.id.remote_detail_device_name);
        deviceStateView = (TextView) findViewById(R.id.remote_detail_device_state);
        progressBar = (ProgressBar) findViewById(R.id.remote_detail_progress);
        detailLayout = (LinearLayout) findViewById(R.id.remote_detail_layout);
        buttonsLayout = (LinearLayout) findViewById(R.id.remote_detail_ctrl_btn_layout);

    }

    private void setEvent(){

        backBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAddEvent();
            }
        });

        AvaApp.fDatabase.getReference()
                .child("Command")
                .child(AvaApp.AvaCode)
                .child("AddDevice")
                .child(macAddress)
                .child("State")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.child("method").getValue() == null){
                            nowState = remoteDetailList.get(0).getMethodKey();
                        }else{
                            nowState = dataSnapshot.child("method").getValue().toString();
                        }
                        deviceStateView.setText("[ 현재상태 : " + nowState + " ]");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void finishAddEvent(){
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    private void getList(){
        AvaApp.fDatabase.getReference()
                .child("Command")
                .child(AvaApp.AvaCode)
                .child("AddDevice")
                .child(macAddress)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DataSnapshot LocalName = dataSnapshot.child("localName");
                deviceName = LocalName.getValue().toString();

                DataSnapshot Methods = dataSnapshot.child("methods");
                for(DataSnapshot item : Methods.getChildren()){
                    if(item.getKey().toLowerCase().equals("variable")){
                        deviceMethods.variableName = item.getValue().toString();
                    }else{
                        deviceMethods dm = new deviceMethods();
                        dm.setMethodKey(item.getKey());
                        dm.setMethodDetail(item.getValue().toString());
                        remoteDetailList.add(dm);
                    }
                }

                DataSnapshot StateSnap = dataSnapshot.child("State");
                if(StateSnap.child("method").getValue() == null){
                    //first Time
                    nowState = remoteDetailList.get(0).getMethodKey();
                }else{
                    nowState = StateSnap.child("method").getValue().toString();
                }

                progressBar.setVisibility(View.GONE);
                detailLayout.setVisibility(View.VISIBLE);


                setUi();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setUi(){
        deviceNameView.setText("# "+deviceName);
        deviceStateView.setText("[ 현재상태 : " + nowState + " ]");


        addButtons();
    }

    private void addButtons(){
        for(final deviceMethods item : remoteDetailList){
            Button button = new Button(getApplicationContext());
            button.setText(item.getMethodDetail());
            button.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.main_green));
            button.setTextSize(24f);
            button.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final HashMap<String, Object> map = new HashMap<>();
                    map.put("method", item.getMethodKey());
                    map.put("variable", deviceMethods.variableName);
                    AvaApp.fDatabase.getReference()
                            .child("Command")
                            .child(AvaApp.AvaCode)
                            .child("AddDevice")
                            .child(macAddress)
                            .child("Log")
                            .child(AvaDateTime.getNowDateTime())
                            .setValue(map)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            AvaApp.fDatabase.getReference()
                                    .child("Command")
                                    .child(AvaApp.AvaCode)
                                    .child("AddDevice")
                                    .child(macAddress)
                                    .child("State")
                                    .setValue(map);
                        }
                    });
                }
            });
            buttonsLayout.addView(button);
        }
    }
}
