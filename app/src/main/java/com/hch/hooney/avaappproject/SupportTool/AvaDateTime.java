package com.hch.hooney.avaappproject.SupportTool;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AvaDateTime {
    public static String getNowDateTime(){
        String currentDateandTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        Log.d("Ava Date Time", "Now : " + currentDateandTime);
        return currentDateandTime;
    }

    public static String getNowDateToString(){
        String currentDateandTime = new SimpleDateFormat("yyyyMMdd").format(new Date());
        Log.d("Ava Date", "Now : " + currentDateandTime);
        return currentDateandTime;
    }
}
