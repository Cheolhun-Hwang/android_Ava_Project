package com.hch.hooney.avaappproject;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.hch.hooney.avaappproject.Application.AvaApp;
import com.hch.hooney.avaappproject.ListPack.MelonChart.MelonChartAdapter;
import com.hch.hooney.avaappproject.ListPack.MelonChart.MelonChartDAO;
import com.hch.hooney.avaappproject.ListPack.MovieChart.MovieChartAdapter;
import com.hch.hooney.avaappproject.ListPack.MovieChart.MovieChartDAO;
import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.transform.ScaleTransformer;

import java.util.ArrayList;
import java.util.Map;

public class MelonChartActivity extends AppCompatActivity {
    private final String TAG = MelonChartActivity.class.getSimpleName();

    private ImageButton backBTN;
    private Button MelonBTN;
    private DiscreteScrollView discreteScrollView;
    private ProgressBar progressBar;
    private TextView noDataNotify;

    private ArrayList<MelonChartDAO> MelonList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_melon_chart);

        init();
        loadList();
    }

    @Override
    public void onBackPressed() {
        finishAddEvent();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void init(){
        if(AvaApp.fDatabase == null){
            AvaApp.initFDatabase();
        }
        progressBar = (ProgressBar) findViewById(R.id.melon_chart_progress);
        backBTN = (ImageButton) findViewById(R.id.melon_chart_back);
        MelonBTN = (Button) findViewById(R.id.melon_chart_melon);
        noDataNotify = (TextView) findViewById(R.id.melon_chart_noData);
        discreteScrollView = (DiscreteScrollView) findViewById(R.id.melon_chart_movie_list);
        discreteScrollView.setItemTransformer(new ScaleTransformer.Builder().setMinScale(0.9f).build());
        discreteScrollView.setSlideOnFling(true);   //false : moving one by one card..

        setEvent();
    }

    private void setEvent(){
        backBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAddEvent();
            }
        });

        MelonBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(MelonChartActivity.this);
                alert.setTitle("Ava Melon");
                alert.setMessage("더 알찬 정보를 위해 멜론으로 이동합니다!");
                alert.setPositiveButton("이동", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(getApplicationContext(), AvaWebViewActivity.class);
                        intent.putExtra("url", "https://www.melon.com/chart/index.htm");
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
                    }
                });
                alert.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alert.show();
            }
        });
    }

    private void loadList(){
        progressUI();

        MelonList = new ArrayList<>();

        AvaApp.fDatabase.getReference()
                .child("Melon")
                .child("Data")
                .limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d(TAG, "List : " + dataSnapshot.getValue());
                        for(DataSnapshot items : dataSnapshot.getChildren()){
                            ArrayList<Map<String, String>> list = (ArrayList<Map<String, String>>) items.getValue();
                            for(int index = 0 ; index < list.size();index++){
                                Map<String, String> item = list.get(index);
                                if(item == null){
                                    Log.d(TAG, "index : "+index+" is NULL..." );
                                }else{
                                    MelonChartDAO melon = new MelonChartDAO();
                                    melon.setArtTitle(item.get("title").toString());
                                    melon.setRank( index+"" );
                                    melon.setPickture(item.get("title_src").toString());
                                    melon.setArtist(item.get("artist").toString());

                                    MelonList.add(melon);
                                }
                            }

                        }
                        if(MelonList.size() < 1){
                            noDataUI();
                        }else{
                            dataLoadComplete();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void finishAddEvent(){
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    private void progressUI(){
        discreteScrollView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void completeUI(){
        discreteScrollView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    private void noDataUI(){
        progressBar.setVisibility(View.GONE);
        noDataNotify.setVisibility(View.VISIBLE);
    }

    private void dataLoadComplete(){
        discreteScrollView.setAdapter(new MelonChartAdapter(MelonList, MelonChartActivity.this));
        completeUI();
    }
}
