package com.joshrap.liteweight.helpers;

import android.app.ProgressDialog;
import android.text.Editable;
import android.text.TextWatcher;

import com.google.android.material.textfield.TextInputLayout;

public class AndroidHelper {

    public static TextWatcher hideErrorTextWatcher(TextInputLayout layout) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (layout.isErrorEnabled()) {
                    // if an error is present, stop showing the error message once the user types (acknowledged it)
                    layout.setErrorEnabled(false);
                    layout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
    }

    public static void showLoadingDialog(ProgressDialog loadingDialog, String message) {
        loadingDialog.setMessage(message);
        loadingDialog.show();
    }
}
