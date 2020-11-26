package com.joshrap.liteweight.helpers;

import android.app.ProgressDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;

import com.google.android.material.textfield.TextInputLayout;

public class AndroidHelper {

    /**
     * Returns a TextWatcher that detects when error is present and hides it once user starts typing.
     *
     * @param layout layout that contains a given EditText
     * @return TextWatcher that does the detection.
     */
    public static TextWatcher hideErrorTextWatcher(TextInputLayout layout) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (layout.isErrorEnabled()) {
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

    public static TranslateAnimation shakeError() {
        TranslateAnimation shake = new TranslateAnimation(0, 10, 0, 0);
        shake.setDuration(350);
        shake.setInterpolator(new CycleInterpolator(2));
        return shake;
    }
}
