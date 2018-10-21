package com.hch.hooney.avaappproject;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hch.hooney.avaappproject.Application.AvaApp;
import com.hch.hooney.avaappproject.NetTools.Post;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class MicActivity extends AppCompatActivity implements RecognitionListener, TextToSpeech.OnInitListener {
    private final String TAG = MicActivity.class.getSimpleName();
    private final String DIALOGFLOW_URL = "https://api.dialogflow.com/v1/query?v=20150910";

    private TextView notifyText;
    private ProgressBar progressBar;

    private String RecordResult;

    private SpeechRecognizer mRecognizer;
    private Intent recordingIntent;
    private Thread orderThread;
    private Handler handler;

    private String ttsTarget;
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mic);

        init();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mRecognizer.startListening(recordingIntent);
    }

    @Override
    protected void onDestroy() {

        if(orderThread!=null){
            if(orderThread.isAlive()){
                orderThread.interrupt();
                orderThread = null;
            }
        }
        if(mRecognizer !=null) {
            mRecognizer.stopListening();
            mRecognizer.cancel();
            mRecognizer.destroy();
        }

        super.onDestroy();
    }

    private void init(){
        progressBar = (ProgressBar) findViewById(R.id.mic_notify_progress);
        notifyText = (TextView) findViewById(R.id.mic_notify_question_text);

        handler = initHandler();
        ttsTarget = "";
        recordingIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recordingIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        recordingIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");

        mRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
        mRecognizer.setRecognitionListener(this);

        tts = new TextToSpeech(MicActivity.this, MicActivity.this);
    }

    @Override
    public void onBackPressed() {
        finishAddEvent();
    }



    private Thread initOrderThread(){
        return new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = handler.obtainMessage();
                try {
                    JSONObject body = new JSONObject();
                    body.put("lang", "ko");
                    body.put("query", RecordResult);
                    body.put("sessionId", AvaApp.AvaUserCode);
                    body.put("timezone", "Asia/Seoul");

                    String res = new Post(DIALOGFLOW_URL).sendToDialog(body.toString());
                    if(res != null){
                        Log.d(TAG, res);
                        JSONObject resJson = new JSONObject(res);
                        JSONObject result = resJson.getJSONObject("result");
                        JSONObject status = resJson.getJSONObject("status");
                        if(res != null && status.getString("code").equals("200")){
                            JSONObject fulfillment = result.getJSONObject("fulfillment");
                            if(fulfillment!= null){
                                String speach = fulfillment.getString("speech");
                                if(speach!= null){
                                    msg.what = 107;
                                    msg.obj = speach;
                                }else{
                                    msg.what = 106;
                                    msg.obj = "AvA 명령이 전달되지 못했습니다. [6]";
                                }
                            }else{
                                msg.what = 105;
                                msg.obj = "AvA 명령이 전달되지 못했습니다. [5]";
                            }
                        }else{
                            msg.what = 104;
                            msg.obj = "AvA 명령이 전달되지 못했습니다. [4]";
                        }
                    }else{
                        msg.what = 103;
                        msg.obj = "AvA 명령이 전달되지 못했습니다. [3]";
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    msg.what = 101;
                    msg.obj = "AvA 명령이 전달되지 못했습니다. [1]";
                } catch (IOException e) {
                    e.printStackTrace();
                    msg.what = 102;
                    msg.obj = "AvA 명령이 전달되지 못했습니다. [2]";
                }finally {
                    handler.sendMessage(msg);
                }
            }
        });
    }

    private Handler initHandler(){
        return new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what){
                    case 101:
                    case 102:
                    case 103:
                    case 104:
                    case 105:
                    case 106:
                        listeningUI();
                        Log.d(TAG, "Case : " + msg.what);
                        callJustNotify("Ava 음성 명령", msg.obj.toString());
                        break;
                    case 107:
                        completeUI();
                        Log.d(TAG, "TTS : " +AvaApp.isUsingTTS);

                        if(msg.obj.toString().contains("tmin") || msg.obj.toString().contains("tmax")){
                            String[] split = msg.obj.toString()
                                    .replaceAll(" ", "")
                                    .split(",");
                            String[] name = split[0].split(":");
                            String[] tc = split[1].split(":");
                            ttsTarget = "기온은 "+tc[1]+"℃ 이며, "+ name[1] + " 입니다.";
                        }else{
                            ttsTarget = msg.obj.toString();
                        }

                        if(AvaApp.isUsingTTS){
                            saidTTS();
                            callJustNotify("Ava 의 답변", ttsTarget);
                        }else{
                            callJustNotify("Ava 의 답변", ttsTarget);
                        }
                        break;
                }
                return true;
            }
        });
    }

    private void progressUI(){
        notifyText.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void listeningUI(){
        notifyText.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);

        notifyText.setText(" ? ");
        notifyText.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red_600));
    }

    private void completeUI(){
        notifyText.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);

        notifyText.setText(" ! ");
        notifyText.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.blue_800));
    }

    private void finishAddEvent(){
        finish();
        overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
    }

    private void callJustNotify(String title, String msg){
        AlertDialog.Builder alert = new AlertDialog.Builder(MicActivity.this);
        alert.setTitle(title);
        alert.setMessage(msg);
        alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finishAddEvent();
            }
        });

        alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                finishAddEvent();
            }
        });

        alert.show();
    }
    //Mic
    @Override
    public void onReadyForSpeech(Bundle params) {
        Toast.makeText(getApplicationContext(), "AvA 가 당신의 명령을 기다리고 있어요!",
                Toast.LENGTH_SHORT).show();
    }
    //Mic
    @Override
    public void onBeginningOfSpeech() {

    }
    //Mic
    @Override
    public void onRmsChanged(float rms) {

    }
    //Mic
    @Override
    public void onBufferReceived(byte[] buffer) {

    }
    //Mic
    @Override
    public void onEndOfSpeech() {
        progressUI();
    }
    //Mic
    @Override
    public void onError(int error) {
        Toast.makeText(getApplicationContext(), "AvA 가 당신의 명령을 이해하지 못했어요.",
                Toast.LENGTH_SHORT).show();

        finishAddEvent();
    }
    //Mic
    @Override
    public void onResults(Bundle results) {
        String key = "";
        key = SpeechRecognizer.RESULTS_RECOGNITION;
        ArrayList<String> mResult = results.getStringArrayList(key);

        String[] rs = new String[mResult.size()];
        mResult.toArray(rs);
        RecordResult = rs[0];

        Log.d(TAG, "Result Record : "+RecordResult);
        Toast.makeText(getApplicationContext(),
                "AvA 가 당신의 명령을 이해했어요!\n입력명령 : "+RecordResult,
                Toast.LENGTH_SHORT).show();

        progressUI();

        orderThread = initOrderThread();
        orderThread.start();
    }
    //Mic
    @Override
    public void onPartialResults(Bundle partialResults) {

    }
    //Mic
    @Override
    public void onEvent(int eventType, Bundle params) {

    }

    private void saidTTS(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(ttsTarget,TextToSpeech.QUEUE_FLUSH,null,null);
        } else {
            tts.speak(ttsTarget, TextToSpeech.QUEUE_FLUSH, null);
        }
    }
    //TTS
    @Override
    public void onInit(int status) {
        if(status == TextToSpeech.SUCCESS){
            int korea = tts.setLanguage(Locale.KOREA);
            if(korea == TextToSpeech.LANG_MISSING_DATA || korea == TextToSpeech.LANG_NOT_SUPPORTED){
                Toast.makeText(getApplicationContext(), "TTS 기능을 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
                AvaApp.saveAvaUsingTTS(MicActivity.this, false);
                AvaApp.isUsingTTS = AvaApp.isAvaUsingTTS(MicActivity.this);
            }else{
                tts.setPitch(1.0f);
                tts.setSpeechRate(0.9f);
            }
        }
    }

}
