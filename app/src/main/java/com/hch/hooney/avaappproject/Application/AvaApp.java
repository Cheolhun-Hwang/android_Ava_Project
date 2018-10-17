package com.hch.hooney.avaappproject.Application;

import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hch.hooney.avaappproject.BleHandler.AvaBleHandler;

import org.json.JSONException;
import org.json.JSONObject;

public class AvaApp extends Application {
    /**
     * App 공유 변수들
     */
    public static boolean AvaBLEOrder;
    public static AvaBleHandler AvaBle;
    public static FirebaseDatabase fDatabase;

    public static String AvaCode;
    public static String AvaWifiSSID;
    public static String AvaUserCode;
    public static String AvaUserNickName;
    public static float AvaLat;
    public static float AvaLon;

    public static JSONObject UserColorSet;

    /**
     * 공유 메소드들
     * @param activity
     * 실행 시 현재 활성화되고 있는 Activity 가 필요함.
     */
    public static void initMethod(Activity activity){
        if(AvaApp.AvaBle == null){
            AvaApp.AvaBle = new AvaBleHandler(
                    ((BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter(),
                    activity.getPackageManager(), getMacAddress(activity));
            AvaApp.AvaBle.setActivity(activity);
        }else{
            AvaApp.AvaBle.setActivity(activity);
        }

    }

    public static boolean isAvaBLEOrder(Activity activity){
        SharedPreferences pref = activity.getSharedPreferences("pref", MODE_PRIVATE);
        return pref.getBoolean("ava_ble_order", true);
    }

    public static void saveAvaBLEOrder(Activity activity, boolean flag){
        SharedPreferences pref = activity.getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor =  pref.edit();
        editor.putBoolean("ava_ble_order", flag);
        editor.commit();
    }

    public static boolean isFinishWifi(Activity activity){
        SharedPreferences pref = activity.getSharedPreferences("pref", MODE_PRIVATE);
        return pref.getBoolean("ava_wifi_finish", false);
    }

    public static String getWifiSSID(Activity activity){
        SharedPreferences pref = activity.getSharedPreferences("pref", MODE_PRIVATE);
        return pref.getString("ava_wifi_ssid", null);
    }

    public static void saveFinishWifi(Activity activity, boolean flag, String ssid){
        SharedPreferences pref = activity.getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor =  pref.edit();
        editor.putBoolean("ava_wifi_finish", flag);
        editor.putString("ava_wifi_ssid", ssid);
        editor.commit();
    }

    public static String getMacAddress(Activity activity){
        SharedPreferences pref = activity.getSharedPreferences("pref", MODE_PRIVATE);
        String mac = pref.getString("ava_mac_address", null);
        return mac;
    }

    public static void saveMacAddress(Activity activity, String store){
        SharedPreferences pref = activity.getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor =  pref.edit();
        editor.putString("ava_mac_address", store);
        editor.commit();
    }

    public static boolean isFinishAuth(Activity activity){
        SharedPreferences pref = activity.getSharedPreferences("pref", MODE_PRIVATE);
        return pref.getBoolean("ava_auth_finish", false);
    }

    public static String getFinishAuthDate(Activity activity){
        SharedPreferences pref = activity.getSharedPreferences("pref", MODE_PRIVATE);
        return pref.getString("ava_auth_finish_date", null);
    }

    public static void saveFinishAuth(Activity activity, boolean flag, String date){
        SharedPreferences pref = activity.getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor =  pref.edit();
        editor.putBoolean("ava_auth_finish", flag);
        editor.putString("ava_auth_finish_date", date);
        editor.commit();
    }

    public static String getAvaDeviceCode(Activity activity){
        SharedPreferences pref = activity.getSharedPreferences("pref", MODE_PRIVATE);
        return pref.getString("ava_device_code", null);
    }

    public static void saveAvaDeviceCode(Activity activity, String code){
        SharedPreferences pref = activity.getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor =  pref.edit();
        editor.putString("ava_device_code", code);
        editor.commit();
    }

    public static boolean isFinishNickName(Activity activity){
        SharedPreferences pref = activity.getSharedPreferences("pref", MODE_PRIVATE);
        return pref.getBoolean("ava_nick_name_finish", false);
    }

    public static void saveFinishNickName(Activity activity, boolean flag){
        SharedPreferences pref = activity.getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor =  pref.edit();
        editor.putBoolean("ava_nick_name_finish", flag);
        editor.commit();
    }

    public static String getAvaRecentUserNickName(Activity activity){
        SharedPreferences pref = activity.getSharedPreferences("pref", MODE_PRIVATE);
        return pref.getString("ava_recent_nick_name", null);
    }

    public static void saveAvaRecentUserNickName(Activity activity, String code){
        SharedPreferences pref = activity.getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor =  pref.edit();
        editor.putString("ava_recent_nick_name", code);
        editor.commit();
    }

    public static String getAvaUserCode(Activity activity){
        SharedPreferences pref = activity.getSharedPreferences("pref", MODE_PRIVATE);
        return pref.getString("ava_user_code", null);
    }

    public static void saveAvaUserCode(Activity activity, String code){
        SharedPreferences pref = activity.getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor =  pref.edit();
        editor.putString("ava_user_code", code);
        editor.commit();
    }

    public static void getAvaRecentLocation(Activity activity){
        SharedPreferences pref = activity.getSharedPreferences("pref", MODE_PRIVATE);
        AvaLat = pref.getFloat("ava_recent_lat", 0.0f);
        AvaLon = pref.getFloat("ava_recent_lon", 0.0f);
    }

    public static void saveAvaRecentLocation(Activity activity, float lat, float lon){
        SharedPreferences pref = activity.getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor =  pref.edit();
        editor.putFloat("ava_recent_lat", lat);
        editor.putFloat("ava_recent_lon", lon);
        editor.commit();
    }

    public static void setUserColorSet(Activity activity){
        SharedPreferences pref = activity.getSharedPreferences("pref", activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("colorset", UserColorSet.toString());
        editor.commit();
    }

    public static void setUserColorSetEmpty(Activity activity){
        SharedPreferences pref = activity.getSharedPreferences("pref", activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("colorset", null);
        editor.commit();
    }

    public static void getUserColorSet(Activity activity){
        SharedPreferences pref = activity.getSharedPreferences("pref", activity.MODE_PRIVATE);
        String temp = pref.getString("colorset", "");
        try {
            Log.d("DAO", "DAO INIT...");
            if(temp.equals("") || temp == null){
                Log.d("DAO", "DAO Null Or Empty...");
                UserColorSet = new JSONObject();
            }else{
                Log.d("DAO", "DAO Not Null...");
                UserColorSet = new JSONObject(temp);
            }
        } catch (JSONException e) {
            Log.d("DAO", "Json ERROR : UserColorSet...");
            e.printStackTrace();
        }
    }


    /**
     * Firebase Database Method
     *
     * save and event method
     */
    public static void initFDatabase(){
        fDatabase = FirebaseDatabase.getInstance();
    }

    /** onCreate()
     * Activity, Receiver, Service 생성되기 전, 어플리케이션 시작 중일때 호출.
     */
    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * onConfigurationChanged()
     * 컴포넌트가 실행되는 동안 단말의 화면이 바뀌면 시스템이 실행된다.
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
