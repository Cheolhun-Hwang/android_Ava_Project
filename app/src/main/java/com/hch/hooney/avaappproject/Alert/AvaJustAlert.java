package com.hch.hooney.avaappproject.Alert;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class AvaJustAlert extends AlertDialog.Builder {
    public AvaJustAlert(Context context) {
        super(context);
        super.setCancelable(false);
    }

    @Override
    public AlertDialog.Builder setTitle(CharSequence title) {
        return super.setTitle(title);
    }

    @Override
    public AlertDialog.Builder setMessage(CharSequence message) {
        return super.setMessage(message);
    }

    public AlertDialog.Builder setPositiveButton(CharSequence text) {
        return super.setPositiveButton(text, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }
}
