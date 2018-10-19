package com.hch.hooney.avaappproject;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
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
import com.hch.hooney.avaappproject.ListPack.MovieChart.MovieChartAdapter;
import com.hch.hooney.avaappproject.ListPack.MovieChart.MovieChartDAO;
import com.hch.hooney.avaappproject.NetTools.Get;
import com.hch.hooney.avaappproject.SupportTool.AvaCode;
import com.hch.hooney.avaappproject.SupportTool.AvaDateTime;
import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.transform.ScaleTransformer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MovieChartActivity extends AppCompatActivity {
    private final String TAG = MovieChartActivity.class.getSimpleName();

    private ImageButton backBTN;
    private Button CGVBTN;
    private DiscreteScrollView discreteScrollView;
    private ProgressBar progressBar;
    private TextView noDataNotify;

    private ArrayList<MovieChartDAO> NaverMovieList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_chart);

        init();
        loadList();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void init(){
        if(AvaApp.fDatabase == null){
            AvaApp.initFDatabase();
        }
        progressBar = (ProgressBar) findViewById(R.id.movie_chart_progress);
        backBTN = (ImageButton) findViewById(R.id.movie_chart_back);
        CGVBTN = (Button) findViewById(R.id.movie_chart_cgv);
        noDataNotify = (TextView) findViewById(R.id.movie_chart_noData);
        discreteScrollView = (DiscreteScrollView) findViewById(R.id.movie_chart_movie_list);
        discreteScrollView.setItemTransformer(new ScaleTransformer.Builder().setMinScale(0.9f).build());
        discreteScrollView.setSlideOnFling(true); //false : moving card one by one...;

        setEvent();
    }

    private void setEvent(){
        backBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAddEvent();
            }
        });

        CGVBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(MovieChartActivity.this);
                alert.setTitle("Ava Movie");
                alert.setMessage("더 알찬 정보를 위해 CGV 로 이동합니다 !!\n(단, 일부 영화는 상영하지 않을 수도 있습니다.)");
                alert.setPositiveButton("이동", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(getApplicationContext(), AvaWebViewActivity.class);
                        intent.putExtra("url", "http://m.cgv.co.kr/WebAPP/MovieV4/movieList.aspx?mtype=now&iPage=1");
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

        NaverMovieList = new ArrayList<>();

        AvaApp.fDatabase.getReference()
                .child("Movie")
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
                            MovieChartDAO movie = new MovieChartDAO();
                            movie.setName(item.get("title").toString());
                            movie.setRank( index+"" );
                            movie.setImageURL(item.get("title_picture").toString());
                            movie.setTicketSales(item.get("ticket_sales").toString());
                            movie.setShowDate(item.get("playdate").toString());

                            NaverMovieList.add(movie);
                        }
                    }

                }
                if(NaverMovieList.size() < 1){
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
        discreteScrollView.setAdapter(new MovieChartAdapter(NaverMovieList, MovieChartActivity.this));
        completeUI();
    }
}
