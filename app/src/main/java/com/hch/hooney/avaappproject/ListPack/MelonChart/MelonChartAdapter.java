package com.hch.hooney.avaappproject.ListPack.MelonChart;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.hch.hooney.avaappproject.R;
import com.joooonho.SelectableRoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MelonChartAdapter extends RecyclerView.Adapter {
    private ArrayList<MelonChartDAO> list;
    private Activity activity;
    private int lastPosition = -1;

    public MelonChartAdapter(ArrayList<MelonChartDAO> list, Activity activity) {
        this.list = list;
        this.activity = activity;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_melon_chart,parent,false);
        MelonChartHolder holder = new MelonChartHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        MelonChartDAO item = list.get(i);
        if(item != null){
            MelonChartHolder hold = (MelonChartHolder) viewHolder;
            hold.melonTitle.setText(item.getRank()+". "+item.getArtTitle());
            hold.artist.setText("# "+ item.getArtist());
            Picasso.get().load(item.getPickture()).into(hold.mainImage);

            setAnimation(hold.itemView, i);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private void setAnimation(View viewToAnimate, int position) {
        // 새로 보여지는 뷰라면 애니메이션을 해줍니다
        if (position > lastPosition) {
            Animation animation = AnimationUtils
                    .loadAnimation(activity.getApplicationContext(), R.anim.fade_in);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    private class MelonChartHolder extends RecyclerView.ViewHolder{
        public TextView melonTitle;
        public TextView artist;
        public SelectableRoundedImageView mainImage;

        public MelonChartHolder(@NonNull View itemView) {
            super(itemView);

            melonTitle = (TextView) itemView.findViewById(R.id.item_melon_chart_rank_and_name);
            artist = (TextView) itemView.findViewById(R.id.item_melon_chart_artist);
            mainImage = (SelectableRoundedImageView) itemView.findViewById(R.id.item_melon_chart_image);
        }
    }
}
