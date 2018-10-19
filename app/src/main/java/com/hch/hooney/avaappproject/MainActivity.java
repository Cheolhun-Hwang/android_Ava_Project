package com.hch.hooney.avaappproject;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hch.hooney.avaappproject.Alert.AvaJustAlert;
import com.hch.hooney.avaappproject.Application.AvaApp;
import com.hch.hooney.avaappproject.NetTools.Get;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Headers;

public class MainActivity extends AppCompatActivity {
    private final String TAG = MainActivity.class.getSimpleName();
    private final int SETTING_CALLBACK = 701;

    //Variable;
    private Thread getWeather;
    private Handler handler;
    private AvaGetGPS avaLocation;

    //Weather;
    private ProgressBar weather_progress;
    private ImageButton weather_lcoation_set_ibtn;
    private ImageView weather_icon;
    private TextView weather_area_and_notify_btn;
    private TextView weather_simply_iconic_text_and_notify_location;
    private TextView weather_degree;

    //Tabs;
    private LinearLayout tab1, tab2, tab3, tab4, tab5, tab6;

    //Mic;
    private ImageButton mic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();


        //첫 실행시 한번만 받을거임.
        getNowWeather();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void getNowWeather(){
        //날씨 정보 확인;
        if (AvaApp.AvaLat == 0.0f || AvaApp.AvaLon == 0.0f) {
            Log.d(TAG, "Ava Location Error. 0.0f");
            askLocationSetting();
        } else {
            //날씨 정보 받기
            if(getWeather != null){
                if(getWeather.isAlive()){
                    getWeather.interrupt();
                }
                getWeather = null;
            }

            getWeather = initGetWeather();
            getWeather.start();
        }
    }

    @Override
    protected void onDestroy() {
        AvaApp.AvaBle.disConnected();
        AvaApp.AvaBle.stop();
        super.onDestroy();
    }

    private void init() {
        //Variable;
        handler = initHandler();
        avaLocation = new AvaGetGPS();

        //Weather;
        weather_progress = (ProgressBar) findViewById(R.id.main_tab_0_weather_progress);
        weather_icon = (ImageView) findViewById(R.id.main_tab_0_weather_image);
        weather_lcoation_set_ibtn = (ImageButton) findViewById(R.id.main_tab_0_weather_reload);
        weather_area_and_notify_btn = (TextView) findViewById(R.id.main_tab_0_weather_area);
        weather_simply_iconic_text_and_notify_location = (TextView) findViewById(R.id.main_tab_0_weather_tag);
        weather_degree = (TextView) findViewById(R.id.main_tab_0_weather_degree);

        //Tabs;
        tab1 = (LinearLayout) findViewById(R.id.main_tab_1);
        tab2 = (LinearLayout) findViewById(R.id.main_tab_2);
        tab3 = (LinearLayout) findViewById(R.id.main_tab_3);
        tab4 = (LinearLayout) findViewById(R.id.main_tab_4);
        tab5 = (LinearLayout) findViewById(R.id.main_tab_5);
        tab6 = (LinearLayout) findViewById(R.id.main_tab_6);

        //Mic;
        mic = (ImageButton) findViewById(R.id.main_home_mic);

        setEvent();
    }

    private Handler initHandler() {
        return new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case 101:
                        JSONObject today = null;
                        try {
                            today = new JSONObject(msg.obj.toString());
                            Log.d(TAG, "info : " + today.get("station").toString());
                            JSONObject station = (JSONObject) today.get("station");
                            Log.d(TAG, "info : " + today.get("sky").toString());
                            JSONObject sky = (JSONObject) today.get("sky");
                            Log.d(TAG, "info : " + today.get("temperature").toString());
                            JSONObject temperature = (JSONObject) today.get("temperature");
                            showWeatherUI(station.get("name").toString(),
                                    sky.get("name").toString(),
                                    sky.get("code").toString(),
                                    temperature.get("tc").toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        break;
                    case 102:
                        weather_progress.setVisibility(View.GONE);
                        weather_lcoation_set_ibtn.setVisibility(View.VISIBLE);
                        weather_simply_iconic_text_and_notify_location.setVisibility(View.VISIBLE);
                        weather_area_and_notify_btn.setVisibility(View.VISIBLE);
                        weather_degree.setVisibility(View.GONE);
                        weather_icon.setVisibility(View.INVISIBLE);

                        weather_simply_iconic_text_and_notify_location.setText(
                                getResources().getText(R.string.main_home_weather_notify_location_sensor));
                        weather_area_and_notify_btn.setText("지역 설정하기");

                        callAvaJustAlert("위치 정보를 받지 못했습니다.");
                        break;
                }
                return true;
            }
        });
    }

    private void showWeatherUI(String name, String tag,  String code, String temp){
        weather_progress.setVisibility(View.GONE);
        weather_lcoation_set_ibtn.setVisibility(View.VISIBLE);
        weather_simply_iconic_text_and_notify_location.setVisibility(View.VISIBLE);
        weather_area_and_notify_btn.setVisibility(View.VISIBLE);
        weather_degree.setVisibility(View.VISIBLE);
        weather_icon.setVisibility(View.VISIBLE);

        if(code.contains("A01")){
            weather_icon.setImageDrawable(getDrawable(R.drawable.sun));
        }else if(code.contains("A02")){
            weather_icon.setImageDrawable(getDrawable(R.drawable.sun_cloud));
        }else if(code.contains("A03") || code.contains("A07") || code.contains("A11")){
            weather_icon.setImageDrawable(getDrawable(R.drawable.cloud));
        }else if(code.contains("A04") || code.contains("A08") || code.contains("A12")){
            weather_icon.setImageDrawable(getDrawable(R.drawable.rain));
        }else if(code.contains("A05") || code.contains("A09") || code.contains("A13")){
            weather_icon.setImageDrawable(getDrawable(R.drawable.snow));
        }else if(code.contains("A06") || code.contains("A10") || code.contains("A14")){
            weather_icon.setImageDrawable(getDrawable(R.drawable.raion_snow));
        }
        weather_simply_iconic_text_and_notify_location.setText(tag);
        weather_degree.setText( ( (temp != null) ? Math.floor(Float.parseFloat(temp)):"0.0" ) +" ℃");
        weather_area_and_notify_btn.setText("현재 "+name+"의 날씨는? ");
    }

    private Thread initGetWeather(){
        return new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = handler.obtainMessage();
                String url = "https://api2.sktelecom.com/weather/current/minutely?" +
                        "lat="+AvaApp.AvaLat+"&lon="+AvaApp.AvaLon;
                try {
                    String res = new Get(url).sendToWeather();
                    Log.d(TAG, "Weather RES : "+ res);
                    JSONObject resjson = new JSONObject(res);
                    JSONObject resjson2 = resjson.getJSONObject("weather");
                    JSONArray daysArray = resjson2.getJSONArray("minutely");
                    if(daysArray == null){
                        Log.e(TAG, "Days Array NULL....");
                        msg.what = 102;
                    }else{
                        msg.obj = (JSONObject) daysArray.get(0);
                        msg.what = 101;

                        if(AvaApp.fDatabase == null){
                            AvaApp.initFDatabase();
                        }

                        AvaApp.fDatabase.getReference()
                                .child("Certification")
                                .child("Device")
                                .child(AvaApp.AvaCode)
                                .child("Location")
                                .child("Lat")
                                .setValue(AvaApp.AvaLat);
                        AvaApp.fDatabase.getReference()
                                .child("Certification")
                                .child("Device")
                                .child(AvaApp.AvaCode)
                                .child("Location")
                                .child("Lon")
                                .setValue(AvaApp.AvaLon);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    handler.sendMessage(msg);
                }
            }
        });
    }

    private void setEvent() {
        weather_lcoation_set_ibtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Weather Location reset");
                callLocationMethod();
            }
        });

        //Life 1
        tab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MovieChartActivity.class));
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
            }
        });

        //Life 2
        tab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MelonChartActivity.class));
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
            }
        });

        //Led
        tab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), LedActivity.class));
                overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
            }
        });

        //Radio
        tab4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), RadioActivity.class));
                overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
            }
        });

        //Remote
        tab5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        //Setting
        tab6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getApplicationContext(), SettingsActivity.class), SETTING_CALLBACK);
                overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
            }
        });

        mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MicActivity.class));
                overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_top);
            }
        });
    }

    private void askLocationSetting() {
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        alert.setTitle("Ava 날씨 설정");
        alert.setMessage("현재 날씨 정보를 얻기 위해 위치 설정이 필요합니다." +
                "\n설정하시겠습니까?");
        alert.setPositiveButton("설정하기", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callLocationMethod();
            }
        });

        alert.setNegativeButton("나중에", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callAvaJustAlert("위치 설정하기 버튼을 클릭하면 날씨 정보 서비스를 이용하실 수 있습니다.");
                dialog.dismiss();
            }
        });
        alert.show();
    }

    private boolean checkEnabledLocation() {
        if (avaLocation.isGPSEnabled() || avaLocation.isNetworkEnabled()) {
            return true;
        } else {
            return false;
        }
    }

    private void callLocationMethod() {
        Log.d(TAG, "check Enabled Location : " + checkEnabledLocation());
        if (checkEnabledLocation()) {
            weather_progress.setVisibility(View.VISIBLE);
            weather_lcoation_set_ibtn.setVisibility(View.INVISIBLE);
            weather_simply_iconic_text_and_notify_location.setVisibility(View.INVISIBLE);
            weather_area_and_notify_btn.setVisibility(View.INVISIBLE);
            weather_icon.setVisibility(View.INVISIBLE);
            weather_degree.setVisibility(View.GONE);

            int count = 0;
            Location location = avaLocation.getLocation();
            if (location != null) {
                AvaApp.AvaLat = (float) location.getLatitude();
                AvaApp.AvaLon = (float) location.getLongitude();

                if(AvaApp.AvaLat != 0.0f && AvaApp.AvaLon != 0.0f){
                    AvaApp.saveAvaRecentLocation(MainActivity.this, AvaApp.AvaLat, AvaApp.AvaLon);
                    if(getWeather != null){
                        if(getWeather.isAlive()){
                            getWeather.interrupt();
                        }
                        getWeather = null;
                    }

                    getWeather = initGetWeather();
                    getWeather.start();
                }else{
                    callAvaJustAlert("위치 정보를 가져오지 못했습니다.\n잠시 후 다시 시도해주세요. [2]");
                }
            } else {
                weather_progress.setVisibility(View.GONE);
                weather_lcoation_set_ibtn.setVisibility(View.VISIBLE);
                weather_simply_iconic_text_and_notify_location.setVisibility(View.VISIBLE);
                weather_area_and_notify_btn.setVisibility(View.VISIBLE);
                callAvaJustAlert("위치 정보를 가져오지 못했습니다.\n잠시 후 다시 시도해주세요. [1]");
            }

        } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
            alert.setTitle("Ava 날씨 설정");
            alert.setMessage("GPS 기능을 사용해야합니다.");
            alert.setPositiveButton("설정으로", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            });

            alert.setNegativeButton("나중에", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alert.show();
        }

        avaLocation.stopUsingGPS();
    }

    private void callAvaJustAlert(String msg) {
        AvaJustAlert alert = new AvaJustAlert(MainActivity.this);
        alert.setTitle("AvA 설정");
        alert.setMessage(msg);
        alert.setPositiveButton("확인");
        alert.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case SETTING_CALLBACK:
                if(resultCode == 711){
                    //초기화 코드
                    startActivity(new Intent(getApplicationContext(), AuthCodeActivity.class));
                    finish();
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                }
                break;
        }
    }

    private class AvaGetGPS implements LocationListener {
        private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; //10미터 당
        private static final long MIN_TIME_UPDATES = 1000 * 10 * 1; // 10 sec 마다

        private LocationManager manager;

        public AvaGetGPS() {
            manager = (LocationManager) getSystemService(LOCATION_SERVICE);

        }

        public boolean isGPSEnabled() {
            return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }

        public boolean isNetworkEnabled() {
            return manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }

        public Location getLocation() {
            Location location = null;
            if ((!isGPSEnabled()) && (!isNetworkEnabled())) {
                Log.d(TAG, "Location use disabled...");
                return null;
            } else {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Location permission Error...");
                    return null;
                }
                if (isNetworkEnabled()) {
                    manager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            this
                    );
                    if(manager != null){
                        location = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                }
            }

            if(isGPSEnabled()){
                if(location==null){
                    manager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            this
                    );
                    if(manager != null){
                        location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    }
                }
            }

            return location;
        }

        @Override
        public void onLocationChanged(Location location) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        public void stopUsingGPS(){
            if(manager!=null){
                manager.removeUpdates(AvaGetGPS.this);
            }
        }
    }

}
