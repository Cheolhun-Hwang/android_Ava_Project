package com.hch.hooney.avaappproject.ListPack.RemoteDevice;

import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.hch.hooney.avaappproject.R;

import java.util.ArrayList;

public class RemoteDeviceAdapter extends RecyclerView.Adapter {
    private ArrayList<RemoteDeviceDAO> list;
    private Activity mContext;

    private int lastPosition = -1;

    public RemoteDeviceAdapter(ArrayList<RemoteDeviceDAO> list, Activity mContext) {
        this.list = list;
        this.mContext = mContext;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_remote_device,parent,false);
        RemoteDeviceHolder holder = new RemoteDeviceHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        RemoteDeviceDAO item = list.get(position);
        if(item != null){
            RemoteDeviceHolder hold = (RemoteDeviceHolder)holder;
            hold.deviceName.setText(item.getDeviceName());
            if(item.isDeviceUseFlag()){
                hold.flagBluetooth.setImageDrawable(mContext.getDrawable(R.drawable.ic_bluetooth));
                hold.flagBluetooth.setColorFilter(ContextCompat.getColor(mContext, R.color.blue_700));
            }else{
                hold.flagBluetooth.setImageDrawable(mContext.getDrawable(R.drawable.ic_bluetooth_disabled));
                hold.flagBluetooth.setColorFilter(ContextCompat.getColor(mContext, R.color.red_700));
            }

            hold.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            setAnimation(hold.itemView, position);
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
}
