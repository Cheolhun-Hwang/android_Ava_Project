package com.hch.hooney.avaappproject;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
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
import com.hch.hooney.avaappproject.ListPack.RemoteDevice.RemoteDeviceAdapter;
import com.hch.hooney.avaappproject.ListPack.RemoteDevice.RemoteDeviceDAO;

import java.util.ArrayList;
import java.util.HashMap;

public class RemoteActivity extends AppCompatActivity {
    private final String TAG = RemoteActivity.class.getSimpleName();

    private ArrayList<RemoteDeviceDAO> deviceList;

    private ProgressBar progressBar;
    private TextView notifyText;
    private ImageButton addDeviceBTN, backBTN;
    private RecyclerView deviceListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote);

        init();
    }

    @Override
    protected void onStart() {
        super.onStart();

        getRemoteDeviceList();
    }

    @Override
    public void onBackPressed() {
        finishAddEvent();
    }

    private void init(){
        if(AvaApp.fDatabase == null){
            AvaApp.initFDatabase();
        }

        progressBar = (ProgressBar) findViewById(R.id.remote_progress);
        backBTN = (ImageButton) findViewById(R.id.remote_back_btn);
        addDeviceBTN = (ImageButton) findViewById(R.id.remote_add_btn);
        notifyText = (TextView) findViewById(R.id.remote_notify_text_view);

        deviceListView = (RecyclerView) findViewById(R.id.remote_device_list_view);
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

        addDeviceBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(RemoteActivity.this);
                alert.setTitle("Ava 원격제어");
                alert.setMessage("원격 제어가 가능한 장치를 등록하기 위해 AvA 가 블루투스를 통해 주변을 검색합니다.");
                alert.setPositiveButton("검색", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(getApplicationContext(), AddRemoteDeviceActivity.class));
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
                    }
                }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alert.show();
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
                .child("AddDevice")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        deviceList.clear();
                        for(DataSnapshot item : dataSnapshot.getChildren()){
                            HashMap<String, Object> map = (HashMap<String, Object>) item.getValue();
                            RemoteDeviceDAO dao = new RemoteDeviceDAO();
                            dao.setDeviceMacAddress(item.getKey());
                            dao.setDeviceName(map.get("localName").toString());
                            dao.setDeviceUseFlag(true); //must connect
                            dao.setDeviceServiceUUID(map.get("serviceUUID").toString());

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

        deviceListView.setAdapter(new RemoteDeviceAdapter(deviceList, RemoteActivity.this));
        if(deviceListView.getVisibility() == View.GONE){
            deviceListView.setVisibility(View.VISIBLE);
        }
    }
}
