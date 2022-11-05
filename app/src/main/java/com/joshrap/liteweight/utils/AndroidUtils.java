package com.joshrap.liteweight.utils;

import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;

import com.google.android.material.textfield.TextInputLayout;

public class AndroidUtils {

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

    public static void showLoadingDialog(AlertDialog loadingDialog, String message) {
        loadingDialog.setMessage(message);
        loadingDialog.show();
    }

    public static TranslateAnimation shakeError(int shakeCycles) {
        TranslateAnimation shake = new TranslateAnimation(0, 10, 0, 0);
        shake.setDuration(350);
        shake.setInterpolator(new CycleInterpolator(shakeCycles));
        return shake;
    }

    // used to shake a view to grab attention of user, not to signal error
    public static TranslateAnimation wiggleFromLeft(int shakeCycles) {
        TranslateAnimation shake = new TranslateAnimation(-10, 0, 0, 0);
        shake.setDuration(400);
        shake.setInterpolator(new CycleInterpolator(shakeCycles));
        return shake;
    }

    public static TranslateAnimation wiggleFromRight(int shakeCycles) {
        TranslateAnimation shake = new TranslateAnimation(0, 10, 0, 0);
        shake.setDuration(400);
        shake.setInterpolator(new CycleInterpolator(shakeCycles));
        return shake;
    }

    public static void showErrorDialog(String title, String msg, Context context) {
        if (context == null) {
            return;
        }
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("Ok", null)
                .create();
        alertDialog.show();
    }
}
