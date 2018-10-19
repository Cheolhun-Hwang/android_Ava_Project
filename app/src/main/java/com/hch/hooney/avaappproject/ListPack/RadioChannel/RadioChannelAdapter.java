package com.hch.hooney.avaappproject.ListPack.RadioChannel;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.hch.hooney.avaappproject.R;
import com.hch.hooney.avaappproject.RadioActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class RadioChannelAdapter extends RecyclerView.Adapter {
    private ArrayList<RadioChannelDAO> list;
    private Activity mContext;
    private int nowPlayIndex;

    private int lastPosition = -1;

    public RadioChannelAdapter(ArrayList<RadioChannelDAO> list, Activity mContext, int nowPlayIndex) {
        this.list = list;
        this.mContext = mContext;
        this.nowPlayIndex = nowPlayIndex;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_radio_channel,parent,false);
        RadioChannelHolder holder = new RadioChannelHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final RadioChannelDAO item = list.get(position);
        if(item != null){
            RadioChannelHolder hold = (RadioChannelHolder) holder;

            hold.radioTitle.setText(item.getRadioTitle());
            Picasso.get().load(item.getRadioImage()).into(hold.mainImage);

            hold.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((RadioActivity) mContext).askToPlayChannel(item);
                }
            });

            setAnimation(hold.itemView, position);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private class RadioChannelHolder extends RecyclerView.ViewHolder{
        private ImageView mainImage;
        private TextView radioTitle;

        public RadioChannelHolder(View itemView) {
            super(itemView);

            mainImage = (ImageView) itemView.findViewById(R.id.item_radio_channel_image);
            radioTitle = (TextView) itemView.findViewById(R.id.item_radio_channel_title);


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
}
