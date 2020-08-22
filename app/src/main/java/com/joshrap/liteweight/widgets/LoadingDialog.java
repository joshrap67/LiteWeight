package com.joshrap.liteweight.widgets;

import android.app.ProgressDialog;
import android.content.Context;

public class LoadingDialog {

    private ProgressDialog progressDialog;

    public LoadingDialog(Context context) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
    }

    public void showDialog() {
        progressDialog.show();
    }

    public void hideDialog() {
        progressDialog.hide();
    }
}
