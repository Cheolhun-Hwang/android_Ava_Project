package com.hch.hooney.avaappproject.Alert;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class myAlert extends AlertDialog.Builder {
    public myAlert(Context context) {
        super(context);
    }

    @Override
    public AlertDialog.Builder setTitle(CharSequence title) {
        return super.setTitle(title);
    }

    @Override
    public AlertDialog.Builder setMessage(CharSequence message) {
        return super.setMessage(message);
    }

    @Override
    public AlertDialog.Builder setPositiveButton(CharSequence text, DialogInterface.OnClickListener listener) {
        return super.setPositiveButton(text, listener);
    }

    @Override
    public AlertDialog.Builder setNegativeButton(CharSequence text, DialogInterface.OnClickListener listener) {
        return super.setNegativeButton(text, listener);
    }
}
