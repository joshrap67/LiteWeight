package com.joshrap.liteweight.widgets;

import android.app.AlertDialog;
import android.content.Context;

import com.joshrap.liteweight.R;

public class ErrorDialog {
    public static void showErrorDialog(String title, String msg, Context context) {
        if (context == null) {
            return;
        }
        AlertDialog alertDialog = new AlertDialog.Builder(context, R.style.AlertDialogTheme)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("Ok", null)
                .create();
        alertDialog.show();
    }
}
