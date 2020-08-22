package com.joshrap.liteweight.widgets;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.TextView;

import com.joshrap.liteweight.R;

public class ErrorDialog {
    public static void showErrorDialog(String title, String msg, Context context){
        AlertDialog alertDialog = new AlertDialog.Builder(context, R.style.AlertDialogTheme)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("Ok", null)
                .create();
        alertDialog.show();
        // make the message font a little bigger than the default one provided by the alertdialog
        TextView messageTV = alertDialog.getWindow().findViewById(android.R.id.message);
        messageTV.setTextSize(18);
    }
}
