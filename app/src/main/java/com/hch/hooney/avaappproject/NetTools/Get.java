package com.hch.hooney.avaappproject.NetTools;

import android.util.Log;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Get {
    private final String TAG = "GET";
    private String URL;
    private OkHttpClient client;

    public Get(String url){
        this.URL = url;
        client = new OkHttpClient();
    }

    public String send() throws IOException{
        Request request = new Request.Builder()
                .url(this.URL)
                .build();
        Response response = client.newCall(request).execute();
        Log.d(TAG, "GET Callback Code : " + response.isSuccessful());
        Log.d(TAG, "RES : " + response);
        return response.body().string();
    }

    public String sendToWeather() throws IOException{
        Request request = new Request.Builder()
                .url(this.URL)
                .header("Accept", "application/json")
                .addHeader("appKey", "708800a4-d67e-4d2d-86b8-e1b1cdfddfea")
                .addHeader("Content-Type", "application/json; charset=UTF-8")
                .build();
        Response response = client.newCall(request).execute();
        Log.d(TAG, "GET Callback Code : " + response.isSuccessful());
        Log.d(TAG, "RES : " + response);
        return response.body().string();
    }

}
