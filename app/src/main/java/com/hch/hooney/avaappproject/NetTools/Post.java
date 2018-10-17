package com.hch.hooney.avaappproject.NetTools;

import android.util.Log;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Post {
    private final String TAG = "Post";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private String URL;
    private OkHttpClient client;

    public Post(String url){
        this.URL = url;
        client = new OkHttpClient();
    }

    public String send(String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(this.URL)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        Log.d(TAG, "Send Ok...");
        return response.body().string();
    }

    public String sendToDialog(String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(this.URL)
                .header("Authorization", "Bearer 345a55cae0ee4acc8fbb13857234d449")
                .addHeader("Content-Type", "application/json; charset=UTF-8")
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        Log.d(TAG, "Send Ok...");
        return response.body().string();
    }

}
