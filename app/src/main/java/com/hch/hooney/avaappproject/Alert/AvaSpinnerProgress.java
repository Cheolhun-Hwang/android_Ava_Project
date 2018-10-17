package com.hch.hooney.avaappproject.Alert;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.hch.hooney.avaappproject.R;

public class AvaSpinnerProgress extends AlertDialog.Builder {
    public AvaSpinnerProgress(Context context) {
        super(context);
    }

    public void setInflater(LayoutInflater inflater, String msg){
        View dialogView = inflater.inflate(R.layout.item_progress_dialog_view, null);
        TextView msgText = (TextView) dialogView.findViewById(R.id.item_progress_dialog_text);
        msgText.setText(msg);
        super.setView(dialogView);
        super.setCancelable(false);
    }
}
