package com.hch.hooney.avaappproject.ListPack.MovieChart;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.hch.hooney.avaappproject.AvaWebViewActivity;
import com.hch.hooney.avaappproject.R;
import com.joooonho.SelectableRoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MovieChartAdapter extends RecyclerView.Adapter {
    private ArrayList<MovieChartDAO> list;
    private Activity mContext;
    private int lastPosition = -1;

    public MovieChartAdapter(ArrayList<MovieChartDAO> list, Activity mContext) {
        this.list = list;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie_chart,parent,false);
        MovieChartHolder holder = new MovieChartHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        final MovieChartDAO item = list.get(i);
        if(item != null){
            MovieChartHolder hold = (MovieChartHolder) viewHolder;
            hold.movieTitle.setText(item.getName());
            hold.showDate.setText("[ 개봉일 : " + item.getShowDate()+" ]");
            hold.ticketSales.setText("예매율 : " + item.getTicketSales());
            Picasso.get().load(item.getImageURL()).into(hold.mainImage);

            setAnimation(hold.itemView, i);
        }
    }

    private void setAnimation(View viewToAnimate, int position) {
        // 새로 보여지는 뷰라면 애니메이션을 해줍니다
        if (position > lastPosition) {
            Animation animation = AnimationUtils
                    .loadAnimation(mContext.getApplicationContext(), R.anim.fade_in);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private class MovieChartHolder extends RecyclerView.ViewHolder{
        public TextView movieTitle;
        public TextView showDate;
        public TextView ticketSales;
        public SelectableRoundedImageView mainImage;

        public MovieChartHolder(@NonNull View itemView) {
            super(itemView);

            movieTitle = (TextView) itemView.findViewById(R.id.item_movie_chart_rank_and_name);
            mainImage = (SelectableRoundedImageView) itemView.findViewById(R.id.item_movie_chart_image);
            showDate = (TextView) itemView.findViewById(R.id.item_movie_chart_show_date);
            ticketSales = (TextView) itemView.findViewById(R.id.item_movie_chart_ticket_sales);
        }
    }
}
