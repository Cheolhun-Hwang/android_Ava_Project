package com.hch.hooney.avaappproject;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.hch.hooney.avaappproject.Application.AvaApp;
import com.hch.hooney.avaappproject.ListPack.RemoteDevice.AddRemoteDeviceAdapter;
import com.hch.hooney.avaappproject.ListPack.RemoteDevice.RemoteDeviceAdapter;
import com.hch.hooney.avaappproject.ListPack.RemoteDevice.RemoteDeviceDAO;

import java.util.ArrayList;
import java.util.HashMap;

public class AddRemoteDeviceActivity extends AppCompatActivity {
    private final String TAG = AddRemoteDeviceActivity.class.getSimpleName();

    private ArrayList<RemoteDeviceDAO> deviceList;

    private ProgressBar progressBar;
    private TextView notifyText;
    private ImageButton backBTN;
    private RecyclerView deviceListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_remote_device);

        init();
    }

    @Override
    public void onBackPressed() {
        finishAddEvent();
    }

    @Override
    protected void onStart() {
        super.onStart();

        getRemoteDeviceList();
    }

    private void init(){
        if(AvaApp.fDatabase == null){
            AvaApp.initFDatabase();
        }

        progressBar = (ProgressBar) findViewById(R.id.add_remote_progress);
        backBTN = (ImageButton) findViewById(R.id.add_remote_back_btn);
        notifyText = (TextView) findViewById(R.id.add_remote_notify_text_view);

        deviceListView = (RecyclerView) findViewById(R.id.add_remote_device_list_view);
        deviceListView.setLayoutManager(new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.VERTICAL, false));
        deviceListView.setHasFixedSize(true);

        setEvent();
    }

    private void setEvent(){
        backBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAddEvent();
            }
        });
    }

    private void finishAddEvent(){
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    private void getRemoteDeviceList(){
        deviceList = new ArrayList<>();
        AvaApp.fDatabase.getReference()
                .child("Command")
                .child(AvaApp.AvaCode)
                .child("BleDevice")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        deviceList.clear();
                        for(DataSnapshot item : dataSnapshot.getChildren()){
                            HashMap<String, Object> map = (HashMap<String, Object>) item.getValue();
                            RemoteDeviceDAO dao = new RemoteDeviceDAO();
                            dao.setDeviceMacAddress(item.getKey());
                            dao.setDeviceName(map.get("localName").toString());
                            dao.setDeviceServiceUUID(map.get("serviceUUID").toString());

                            if(map.get("connectable").toString().equals("true")){
                                dao.setDeviceUseFlag(true);
                            }else{
                                dao.setDeviceUseFlag(false);
                            }

                            deviceList.add(dao);
                        }

                        if(deviceList.size() > 0){
                            //연결할 장치가 존재, 기존 등록됨.
                            showDeviceList();
                        }else{
                            //등록된 장치가 없음, 등록이 필요함.
                            showNotifyAdd();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void showNotifyAdd(){
        if(progressBar.getVisibility() == View.VISIBLE){
            progressBar.setVisibility(View.GONE);
        }
        notifyText.setText("# 등록된 장치가 없습니다.\n등록 버튼을 통해 장치를 추가해주세요.");
        notifyText.setVisibility(View.VISIBLE);
    }

    private void showDeviceList(){
        if(progressBar.getVisibility() == View.VISIBLE){
            progressBar.setVisibility(View.GONE);
        }

        deviceListView.setAdapter(new AddRemoteDeviceAdapter(deviceList, AddRemoteDeviceActivity.this));
        if(deviceListView.getVisibility() == View.GONE){
            deviceListView.setVisibility(View.VISIBLE);
        }
    }


}
