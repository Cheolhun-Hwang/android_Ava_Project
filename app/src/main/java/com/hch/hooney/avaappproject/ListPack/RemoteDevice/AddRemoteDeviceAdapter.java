package com.hch.hooney.avaappproject.ListPack.RemoteDevice;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.hch.hooney.avaappproject.AddRemoteDeviceActivity;
import com.hch.hooney.avaappproject.R;

import java.util.ArrayList;

public class AddRemoteDeviceAdapter extends RecyclerView.Adapter {
    private ArrayList<RemoteDeviceDAO> list;
    private Activity mContext;

    private int lastPosition = -1;

    public AddRemoteDeviceAdapter(ArrayList<RemoteDeviceDAO> list, Activity mContext) {
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
        final RemoteDeviceDAO item = list.get(position);
        if(item != null){
            RemoteDeviceHolder hold = (RemoteDeviceHolder)holder;
            hold.deviceName.setText(item.getDeviceName());
            if(item.isDeviceUseFlag()){
                hold.flagBluetooth.setImageDrawable(mContext.getDrawable(R.drawable.ic_bluetooth));
                hold.flagBluetooth.setColorFilter(ContextCompat.getColor(mContext, R.color.blue_700));
                hold.backgound.setBackgroundTintList(ContextCompat.getColorStateList(mContext, R.color.blue_100));
            }else{
                hold.flagBluetooth.setImageDrawable(mContext.getDrawable(R.drawable.ic_bluetooth_disabled));
                hold.flagBluetooth.setColorFilter(ContextCompat.getColor(mContext, R.color.red_700));
                hold.backgound.setBackgroundTintList(ContextCompat.getColorStateList(mContext, R.color.red_100));
            }

            hold.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
                    alert.setTitle("Ava 원격제어 등록");
                    if(item.isDeviceUseFlag()){
                        alert.setMessage(item.getDeviceName()+" 장치를 연결합니다.");
                        alert.setPositiveButton("연결", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ((AddRemoteDeviceActivity)mContext).addConnect(item.getDeviceMacAddress());
                            }
                        }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                    }else{
                        alert.setMessage("연결이 불가능한 장치입니다.");
                        alert.setPositiveButton("닫기", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                    }
                    alert.show();
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
