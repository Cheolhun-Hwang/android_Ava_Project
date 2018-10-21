package com.hch.hooney.avaappproject.ListPack.RemoteDevice;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hch.hooney.avaappproject.R;

public class RemoteDeviceHolder extends RecyclerView.ViewHolder {
    public ImageView flagBluetooth;
    public TextView deviceName;
    public LinearLayout backgound;

    public RemoteDeviceHolder(View itemView) {
        super(itemView);

        flagBluetooth = (ImageView) itemView.findViewById(R.id.item_remote_flag);
        deviceName = (TextView) itemView.findViewById(R.id.item_remote_device_name);
        backgound = (LinearLayout) itemView.findViewById(R.id.item_remote_background);
    }
}
