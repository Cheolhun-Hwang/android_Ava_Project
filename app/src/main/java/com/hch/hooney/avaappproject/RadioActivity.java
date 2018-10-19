package com.hch.hooney.avaappproject;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.hch.hooney.avaappproject.Application.AvaApp;
import com.hch.hooney.avaappproject.ListPack.RadioChannel.RadioChannelAdapter;
import com.hch.hooney.avaappproject.ListPack.RadioChannel.RadioChannelDAO;
import com.hch.hooney.avaappproject.SupportTool.AvaDateTime;
import com.squareup.picasso.Picasso;
import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.transform.ScaleTransformer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RadioActivity extends AppCompatActivity {
    private final String TAG = RadioActivity.class.getSimpleName();

    private TextView nowRadioTitle;
    private ImageButton nowStartAndStop, backBTN;
    private ImageView nowRadioImage;
    private DiscreteScrollView channelListView;
    private ProgressBar radioProgress;

    private ArrayList<RadioChannelDAO> radioChannelList;

    private RadioChannelDAO nowStateRadioChannel;
    private boolean isNowOn;
    private int nowStateRadioIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radio);

        init();
    }

    @Override
    protected void onStart() {
        super.onStart();

        progressUI();
        getRadioList();
    }

    private void init(){
        if(AvaApp.fDatabase == null){
            AvaApp.initFDatabase();
        }

        nowStateRadioChannel = null;
        isNowOn = false;
        nowStartAndStop = (ImageButton) findViewById(R.id.radio_channel_stop_and_start);
        radioProgress = (ProgressBar) findViewById(R.id.radio_channel_progress);
        nowRadioImage = (ImageView) findViewById(R.id.radio_channel_main_image);
        nowRadioTitle = (TextView) findViewById(R.id.radio_channel_main_title);
        channelListView = (DiscreteScrollView) findViewById(R.id.radio_channel_list_view);
        channelListView.setItemTransformer(new ScaleTransformer.Builder().setMinScale(0.9f).build());
        channelListView.setSlideOnFling(true);   //false : moving card one by one..

        radioChannelList = new ArrayList<>();

        setEvent();
    }

    private void setEvent(){
        findViewById(R.id.radio_back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAddEvent();
            }
        });

        nowStartAndStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isNowOn){
                    askStopToChannel();
                }else{
                    if(nowStateRadioChannel == null){
                        callJustAlert("선택된 라디오 채널이 없습니다.");
                    }else{
                        askToPlayChannel(nowStateRadioChannel);
                    }
                }
            }
        });
    }

    private void finishAddEvent(){
        finish();
        overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_top);
    }

    private void getRadioList(){
        AvaApp.fDatabase.getReference().child("Radio").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot item : dataSnapshot.getChildren()){
                    HashMap<String, Object> map = (HashMap<String, Object>) item.getValue();
                    RadioChannelDAO dao = new RadioChannelDAO();
                    dao.setRadioImage(map.get("image").toString());
                    dao.setRadioTitle(map.get("title").toString());
                    dao.setRadioURL(map.get("address").toString());
                    radioChannelList.add(dao);
                }

                if(radioChannelList.size() > 0){
                    getRadioState();
                }else{
                    doneUI(false);      //fail to load.
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getRadioState(){
        AvaApp.fDatabase.getReference()
                .child("Command")
                .child(AvaApp.AvaCode)
                .child("Radio")
                .child("State")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot == null){
                            //first Time
                            nowStateRadioIndex = -1;

                            settingUI();
                        }else{
                            HashMap<String, Object> map = (HashMap<String, Object>) dataSnapshot.getValue();

                            String nowState = map.get("now").toString();
                            Log.d(TAG, "Radio State : " + nowState);
                            if(nowState.equals("on")){
                                //now is play!!;
                                isNowOn = true;
                            }else{
                                //now is stop.
                                isNowOn = false;
                            }

                            String ch_key = map.get("ch_key").toString();
                            Log.d(TAG, "Channel URL : " + ch_key);

                            for(int index = 0; index < radioChannelList.size();index++){
                                RadioChannelDAO dao = radioChannelList.get(index);
                                if(dao.getRadioURL().equals(ch_key)){
                                    nowStateRadioChannel = dao;
                                    nowStateRadioIndex = index;
                                    break;
                                }
                            }

                            if(isNowOn && nowStateRadioChannel == null){
                                //matching url failed....
                                doneUI(false);
                            }else if( (isNowOn && nowStateRadioChannel != null) ||
                                    (!isNowOn && nowState != null )){
                                //Success
                                settingUI();
                            }else if(!isNowOn && nowStateRadioChannel == null ){
                                //never happen but..
                                nowStateRadioIndex = -1;
                                settingUI();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void progressUI(){
        radioProgress.setVisibility(View.VISIBLE);
        channelListView.setVisibility(View.GONE);
        nowRadioTitle.setVisibility(View.GONE);
        nowRadioImage.setVisibility(View.GONE);
    }

    private void playBtnUI(){
        if(isNowOn){
            nowStartAndStop.setImageDrawable(getDrawable(R.drawable.ic_pause_circle_outline_black));
        }else{
            nowStartAndStop.setImageDrawable(getDrawable(R.drawable.ic_play_circle_outline_black));
        }
    }

    private void doneUI(boolean type){
        if(!type){
            //fail
            radioProgress.setVisibility(View.GONE);
            nowRadioTitle.setVisibility(View.VISIBLE);
            nowRadioTitle.setText("라디오 채널 정보를 가져오지 못했습니다.\n잠시 후 다시 시도해주세요.");
        }else{
            //success
            radioProgress.setVisibility(View.GONE);
            channelListView.setVisibility(View.VISIBLE);
            nowRadioTitle.setVisibility(View.VISIBLE);
            nowRadioImage.setVisibility(View.VISIBLE);
        }
    }

    private void settingUI(){
        radioProgress.setVisibility(View.GONE);
        playBtnUI();
        if(nowStateRadioChannel == null){
            //now State off.
            nowRadioTitle.setText("# 기존 청취 기록이 없습니다.");
            nowRadioTitle.setVisibility(View.VISIBLE);
            //대체할 이미지 사진이 필요할때 여기 넣기
            //Picasso.get().load( 대체 사진 ).into(nowRadioImage);
            //nowRadioImage.setVisibility(View.VISIBLE);
        }else{
            //now State on.
            nowRadioTitle.setText("# "+nowStateRadioChannel.getRadioTitle());
            nowRadioTitle.setVisibility(View.VISIBLE);
            Picasso.get().load(nowStateRadioChannel.getRadioImage()).into(nowRadioImage);
            nowRadioImage.setVisibility(View.VISIBLE);
        }

        callListViewSetting();
    }

    private void callListViewSetting(){
        channelListView.setAdapter(new RadioChannelAdapter(radioChannelList,
                RadioActivity.this, nowStateRadioIndex));
        channelListView.scrollToPosition(nowStateRadioIndex);

        if(channelListView.getVisibility() == View.GONE){
            channelListView.setVisibility(View.VISIBLE);
        }
    }

    public void askToPlayChannel(final RadioChannelDAO item){
        AlertDialog.Builder alert = new AlertDialog.Builder(RadioActivity.this);
        alert.setTitle("Ava 라디오");
        alert.setMessage(item.getRadioTitle()+" 를 청취합니다.");
        alert.setPositiveButton("청취", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callToChangeRadioChannel(item);
            }
        }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.show();
    }

    private void callToChangeRadioChannel(final RadioChannelDAO item){
        Log.d(TAG, "change Channel Name : " + item.getRadioTitle());

        HashMap<String, Object> map = new HashMap<>();
        map.put("ch_key", item.getRadioURL());
        map.put("method", "on");

        AvaApp.fDatabase.getReference().child("Command")
                .child(AvaApp.AvaCode).child("Radio").child("Log").child(AvaDateTime.getNowDateTime())
                .setValue(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                //현재는 테스트 개발자 로컬 서버
                HashMap<String, Object> tmap = new HashMap<>();
                tmap.put("ch_key", item.getRadioURL());
                tmap.put("now", "on");
                AvaApp.fDatabase.getReference().child("Command")
                        .child(AvaApp.AvaCode).child("Radio").child("State").setValue(tmap);

                //여기서부터 글로벌 코드
                callJustAlert("정상적으로 실행되었습니다.");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callJustAlert("실행에 실패하였습니다.\n잠시 후 다시 시도해주세요.");
            }
        });
    }

    private void callStopRadio(){
        HashMap<String, Object> map = new HashMap<>();
        map.put("ch_key", nowStateRadioChannel.getRadioURL());
        map.put("method", "off");

        AvaApp.fDatabase.getReference().child("Command")
                .child(AvaApp.AvaCode).child("Radio").child("Log").child(AvaDateTime.getNowDateTime())
                .setValue(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                //현재는 테스트 개발자 로컬 서버
                HashMap<String, Object> tmap = new HashMap<>();
                tmap.put("ch_key", nowStateRadioChannel.getRadioURL());
                tmap.put("now", "off");
                AvaApp.fDatabase.getReference().child("Command")
                        .child(AvaApp.AvaCode).child("Radio").child("State").setValue(tmap);

                //여기서부터 글로벌 코드
                callJustAlert("정상적으로 실행되었습니다.");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callJustAlert("실행에 실패하였습니다.\n잠시 후 다시 시도해주세요.");
            }
        });
    }

    private void callJustAlert(String msg){
        AlertDialog.Builder alert = new AlertDialog.Builder(RadioActivity.this);
        alert.setTitle("Ava 라디오");
        alert.setMessage(msg);
        alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.show();
    }

    private void askStopToChannel(){
        AlertDialog.Builder alert = new AlertDialog.Builder(RadioActivity.this);
        alert.setTitle("Ava 라디오");
        alert.setMessage("라디오를 정지합니다.");
        alert.setPositiveButton("정지", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callStopRadio();
            }
        }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.show();
    }

}
