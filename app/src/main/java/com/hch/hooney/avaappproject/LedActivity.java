package com.hch.hooney.avaappproject;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hch.hooney.avaappproject.Alert.AvaJustAlert;
import com.hch.hooney.avaappproject.Application.AvaApp;
import com.hch.hooney.avaappproject.SupportTool.AvaDateTime;
import com.jaredrummler.android.colorpicker.ColorPickerView;

import java.util.HashMap;
import java.util.Iterator;

public class LedActivity extends AppCompatActivity {
    private final String TAG = "LedActivity";
    private final int REQUEST_ENABLE = 203;

    private Thread getStateBLE, changeColorBLE;
    private Handler handler;

    private EditText getTintColor;
    private Button complete;
    private ImageView ledLight;
    private String nowColor, ledNowState, themeName;
    private LinearLayout color_set_linear;
    private TextView ledState;
    private ColorPickerView colorPickerView;
    private ImageButton backBTN, saveThemeBTN, turnOffBTN;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_led);

        try {
            init();
            setEvent();

            checkUseMethod();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            setUI();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void finishAddEvent(){
        finish();
        overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_top);
    }

    private void init() throws Exception {
        //Ble 초기 설정 확인
        AvaApp.initMethod(LedActivity.this);

        if(AvaApp.fDatabase == null){
            AvaApp.initFDatabase();
        }

        ledNowState = "off";

        handler = initHandler();

        getTintColor = (EditText) findViewById(R.id.led_color_text);
        complete = (Button) findViewById(R.id.led_complete);
        ledLight = (ImageView) findViewById(R.id.led_image);
        colorPickerView = (ColorPickerView) findViewById(R.id.led_color_picker_view);
        color_set_linear = (LinearLayout) findViewById(R.id.led_linear_horizontal_colors);
        ledState = (TextView) findViewById(R.id.led_now_state);
        backBTN = (ImageButton) findViewById(R.id.led_back_btn);
        saveThemeBTN = (ImageButton) findViewById(R.id.led_color_save);
        turnOffBTN = (ImageButton) findViewById(R.id.led_color_off);
        progressBar = (ProgressBar) findViewById(R.id.led_progress);

    }

    private Handler initHandler(){
        return new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what){
                    case 8001:
                        //연결!
                        if(progressBar.getVisibility() == View.VISIBLE){
                            progressBar.setVisibility(View.GONE);
                            ledLight.setVisibility(View.VISIBLE);
                        }

                        if(getStateBLE != null){
                            String res = (String) msg.obj;
                            String[] sp_res = res.split("&");
                            setfilter(sp_res[1], false);
                            releaseStateBLEThread();
                        }else if(changeColorBLE != null){
                            String res = (String) msg.obj;
                            String[] sp_res = res.split("&");
                            setfilter(sp_res[1], true);
                            releaseChangeColorBLEThread();
                            callAvaJustAlert("성공적으로 변경하였습니다.");
                        }else{
                            Log.e(TAG, "Led Thread Call back Error.");
                            callAvaLEDStateAlert("무드등 기능에 문제를 발견하였습니다.\n잠시 후 다시 시도해주십시오.");
                        }
                        break;
                    case 8002:
                    case 8003:
                        callAvaLEDStateAlert(msg.obj.toString());
                        break;
                    case 8004:
                        Toast.makeText(getApplicationContext(),
                                "무드등 상태를 읽을 수 없습니다.[3]",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
                return true;
            }
        });
    }

    private void setEvent()throws Exception{
        backBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAddEvent();
            }
        });

        saveThemeBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveColorOnAPP();
            }
        });
        turnOffBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PowerOff();
            }
        });
        complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(progressBar.getVisibility() == View.GONE){
                    progressBar.setVisibility(View.VISIBLE);
                    ledLight.setVisibility(View.GONE);
                }

                if(AvaApp.AvaBLEOrder){
                    //bluth method
                    changeColorBLE = initChangeColorBLEThread();
                    changeColorBLE.start();
                }else{
                    //Firebase
                    changeFirebaseColor(nowColor);
                }
            }
        });

        getTintColor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() == 6) {
                    try{
                        nowColor = "#"+s.toString();
                        ledLight.setColorFilter(Color.parseColor(nowColor));
                    }catch (Exception e){
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(),
                                "16진수 색상값을 확인해주세요.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        colorPickerView.setOnColorChangedListener(new ColorPickerView.OnColorChangedListener() {
            @Override
            public void onColorChanged(int newColor) {
                //int to Hax to String;
                String hex = String.format("%06X", (0xFFFFFF & colorPickerView.getColor()));
                getTintColor.setText(hex);
                nowColor = "#"+hex;

                ledLight.setColorFilter(colorPickerView.getColor());
            }
        });
    }

    private void checkUseMethod() {
        if (AvaApp.AvaBLEOrder) {
            //BLE 우선 통신
            Log.d(TAG, "LED BLE State");
            getStateBLE = initStateBLEThread();
            getStateBLE.start();
        } else {
            //Net 차선 통신
            Log.d(TAG, "LED Firebase State");
            AvaApp.fDatabase.getReference().child("Command")
                    .child(AvaApp.AvaCode).child("LED").child("State").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    HashMap<String, Object> map = (HashMap<String, Object>) dataSnapshot.getValue();

                    nowColor = (String) map.get("color");
                    ledNowState = (String) map.get("now");

                    if(nowColor != null || ledNowState != null){
                        progressBar.setVisibility(View.GONE);
                        ledLight.setVisibility(View.VISIBLE);

                        setfilter(nowColor, true);
                    }else{
                        progressBar.setVisibility(View.GONE);
                        ledLight.setVisibility(View.VISIBLE);

                        callAvaJustAlert("LED 정보를 받지 못했습니다. [5]");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void setUI() throws Exception{
        setColorSet();

        ledState.setText("현재 상태 : "+ledNowState );
    }

    private void setColorSet() throws Exception{
        Iterator now = AvaApp.UserColorSet.keys();
        while (now.hasNext()){
            String key = now.next().toString();
            ColorSetListLinear item = new ColorSetListLinear(getApplicationContext(),
                    AvaApp.UserColorSet.getString(key), key);
            color_set_linear.addView(item);
        }
    }

    private void changeFirebaseColor(String color){
        final HashMap<String, Object> map = new HashMap<>();
        map.put("color", color);
        if(color.equals("#000000")){
            map.put("now", "off");
        }else{
            map.put("now", "on");
        }

        AvaApp.fDatabase.getReference().child("Command")
                .child(AvaApp.AvaCode).child("LED").child("Log").child(AvaDateTime.getNowDateTime())
                .setValue(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
//                AvaApp.fDatabase.getReference().child("Command")
//                        .child(AvaApp.AvaCode).child("LED").child("State").setValue(map)
//                        .addOnSuccessListener(new OnSuccessListener<Void>() {
//                            @Override
//                            public void onSuccess(Void aVoid) {
//                                callAvaJustAlert("성공적으로 변경되었습니다.");
//                                setfilter(nowColor, true);
//                            }
//                        }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        callAvaJustAlert("Ava Led 정보를 받지 못했습니다.[2]");
//                    }
//                });
                callAvaJustAlert("성공적으로 변경되었습니다.");
                setfilter(nowColor, true);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callAvaJustAlert("Ava Led 정보를 받지 못했습니다.[1]");
            }
        });
    }

    private void callAvaJustAlert(String msg){
        AvaJustAlert alert = new AvaJustAlert(LedActivity.this);
        alert.setTitle("Ava LED");
        alert.setMessage(msg);
        alert.setPositiveButton("확인");
        alert.show();
    }

    private void callAvaLEDStateAlert(String msg){
        AvaJustAlert alert = new AvaJustAlert(LedActivity.this);
        alert.setTitle("Ava LED");
        alert.setMessage(msg);
        alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finishAddEvent();
            }
        });
        alert.show();
    }

    private void saveColorOnAPP(){
        final AlertDialog.Builder alert = new AlertDialog.Builder(LedActivity.this);
        alert.setTitle("현재 색상을 저장합니다.");
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_only_edittext, null);
        final EditText getClassName = (EditText) view.findViewById(R.id.dialog_only_edittext);
        getClassName.setHint("# 저장할 색상의 이름을 작성해주세요.");
        alert.setView(view).setPositiveButton("추가", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try{
                    themeName = getClassName.getText().toString();

                    if(themeName.length() > 0){
                        Log.d(TAG, "Name : " + themeName+" / Color : " + nowColor);

                        try{
                            AvaApp.UserColorSet.put(themeName, nowColor);
                            setfilter(nowColor, true);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        AvaApp.setUserColorSet(LedActivity.this);
                        Toast.makeText(getApplicationContext(),
                                "색상이 저장되었습니다.",
                                Toast.LENGTH_SHORT).show();
                        ColorSetListLinear item = new ColorSetListLinear(getApplicationContext(),
                                nowColor, themeName);
                        color_set_linear.addView(item);
                        themeName = "";
                    }else{
                        callAvaJustAlert("최소 1자 이상 작성하셔야 합니다.");
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

    private void PowerOff(){
        nowColor = "#000000";
        if(AvaApp.AvaBLEOrder){
            changeColorBLE = initChangeColorBLEThread();
            changeColorBLE.start();
        }else{
            changeFirebaseColor(nowColor);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_ENABLE){
            if(resultCode == RESULT_OK){
                releaseStateBLEThread();
                checkUseMethod();
            }
        }
    }

    private Thread initStateBLEThread(){
        return new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = handler.obtainMessage();
                int count = 0;
                if(AvaApp.AvaBle.callBleSearch()) {
                    AvaApp.AvaBle.getLedState();

                    while (true) {
                        try {
                            Thread.sleep(300);
                            count++;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            msg.what = 8003;
                            msg.obj = "무드등 상태를 읽을 수 없습니다.[2]";
                        }
                        String res = AvaApp.AvaBle.getRes();
                        if (res != null) {
                            msg.what = 8001;
                            msg.obj = res;
                            Log.d(TAG, "LED : " + msg.obj.toString());
                            break;
                        }

                        if (count > 100) {
                            msg.what = 8002;
                            msg.obj = "무드등 상태를 읽을 수 없습니다.[1]";
                            break;
                        }
                    }
                }
                handler.sendMessage(msg);
            }
        });
    }

    private void releaseStateBLEThread(){
        if(getStateBLE!= null){
            if(getStateBLE.isAlive()){
                getStateBLE.interrupt();
            }
            getStateBLE = null;
        }
    }

    private Thread initChangeColorBLEThread(){
        return new Thread(new Runnable() {
            @Override
            public void run() {
                //이미 연결되었다는 전제가 되어있음.
                Message msg = handler.obtainMessage();
                AvaApp.AvaBle.setRes(null);
                Log.d(TAG, "Change BLE Thread Start...");
                Log.d(TAG, "Change Color : " + nowColor.toString());
                if(AvaApp.AvaBle.writeLed(nowColor.toString())){
                    while (true){
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if(AvaApp.AvaBle.getRes() != null){
                            msg.what = 8001;
                            msg.obj = AvaApp.AvaBle.getRes();
                            Log.d(TAG, "LED : " + msg.obj.toString());
                            break;
                        }
                    }

                }else{
                    msg.what = 8004;
                }
                handler.sendMessage(msg);
            }
        });
    }

    private void releaseChangeColorBLEThread(){
        if(changeColorBLE != null){
            if(changeColorBLE.isAlive()){
                changeColorBLE.interrupt();
            }
            changeColorBLE = null;
        }
    }

    private void setfilter(String color, boolean isNet){
        if(isNet){
            if(color.equals("#000000") ){
                ledNowState = "Off";
            }else{
                ledNowState = "On";
            }
        }

        getTintColor.setText(color.replace("#", ""));
        colorPickerView.setColor(Color.parseColor(color));
        ledLight.setColorFilter(Color.parseColor(color));
        ledState.setText("현재 상태 : " + ledNowState);
    }

    private class ColorSetListLinear extends LinearLayout{
        private View view;
        private Context mContext;
        private Button colorButton;
        private String SaveColorHax;
        private String SaveColorName;

        public ColorSetListLinear(Context context) {
            super(context);
            init(context);
        }

        public ColorSetListLinear(Context context, String hax, String name) {
            super(context);
            this.mContext = context;
            this.SaveColorHax = hax;
            this.SaveColorName = name;
            init(context);
        }

        public ColorSetListLinear(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);

            init(context);
        }

        private void init(Context context){
            LayoutInflater inflater =(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_led_save_colors,this,true);

            colorButton = (Button) view.findViewById(R.id.item_led_color);
            colorButton.setBackgroundColor(Color.parseColor(SaveColorHax));
            colorButton.setText(SaveColorName);

            colorButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    setfilter(SaveColorHax, false);
                }
            });
            colorButton.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(LedActivity.this);
                    alertDialog.setTitle("색상 삭제");
                    alertDialog.setMessage("\""+SaveColorName+"\"을(를) 삭제하시겠습니까?");

                    alertDialog.setPositiveButton("예",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    AvaApp.UserColorSet.remove(SaveColorName);
                                    AvaApp.setUserColorSet(LedActivity.this);
                                    view.setVisibility(View.GONE);

                                    Toast.makeText(getApplicationContext(),
                                            "삭제 완료되었습니다.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                    alertDialog.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    alertDialog.show();
                    return true;
                }
            });
        }
    }
}
