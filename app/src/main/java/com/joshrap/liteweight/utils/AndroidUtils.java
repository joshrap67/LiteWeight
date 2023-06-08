package com.joshrap.liteweight.utils;

import static com.joshrap.liteweight.utils.ValidatorUtils.passwordNotMatchingMsg;

import androidx.appcompat.app.AlertDialog;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;
import com.google.common.base.Strings;

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

    public static void setPasswordRequirementsWatcher(EditText input1, TextInputLayout layout1, EditText input2, TextInputLayout layout2) {
        TextWatcher watcher1 = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password1 = input1.getText().toString().trim();
                String password2 = input2.getText().toString().trim();
                String errorMessage = ValidatorUtils.validNewPassword(input1.getText().toString().trim());
                layout1.setError(errorMessage);

                if (passwordsDoNotMatch(password1, password2) && Strings.isNullOrEmpty(errorMessage)) {
                    layout1.setError(passwordNotMatchingMsg);
                } else if (passwordsDoNotMatch(password1, password2)) {
                    layout1.setError(errorMessage + passwordNotMatchingMsg);
                } else {
                    layout1.setError(errorMessage);
                    layout1.setErrorEnabled(errorMessage != null);
                }

                if (password2.isEmpty()) return;

                String confirmPasswordErrorMessage = ValidatorUtils.validNewPassword(password2);
                if (passwordsDoNotMatch(password1, password2) && confirmPasswordErrorMessage == null) {
                    layout2.setError(passwordNotMatchingMsg);
                } else if (passwordsDoNotMatch(password1, password2)) {
                    layout2.setError(confirmPasswordErrorMessage + passwordNotMatchingMsg);
                } else {
                    layout2.setError(confirmPasswordErrorMessage);
                    layout2.setErrorEnabled(confirmPasswordErrorMessage != null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
        input1.addTextChangedListener(watcher1);

        TextWatcher watcher2 = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password1 = input1.getText().toString().trim();
                String password2 = input2.getText().toString().trim();
                String errorMessage = ValidatorUtils.validNewPassword(input2.getText().toString().trim());
                layout2.setError(errorMessage);

                if (passwordsDoNotMatch(password1, password2) && Strings.isNullOrEmpty(errorMessage)) {
                    layout2.setError(passwordNotMatchingMsg);
                } else if (passwordsDoNotMatch(password1, password2)) {
                    layout2.setError(errorMessage + passwordNotMatchingMsg);
                } else {
                    layout2.setError(errorMessage);
                    layout2.setErrorEnabled(errorMessage != null);
                }

                if (password1.isEmpty()) return;

                String confirmPasswordErrorMessage = ValidatorUtils.validNewPassword(password1);
                if (passwordsDoNotMatch(password1, password2) && confirmPasswordErrorMessage == null) {
                    layout1.setError(passwordNotMatchingMsg);
                } else if (passwordsDoNotMatch(password1, password2)) {
                    layout1.setError(confirmPasswordErrorMessage + passwordNotMatchingMsg);
                } else {
                    layout1.setError(confirmPasswordErrorMessage);
                    layout1.setErrorEnabled(confirmPasswordErrorMessage != null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
        input2.addTextChangedListener(watcher2);
    }

    private static boolean passwordsDoNotMatch(String password1, String password2) {
        if (password1.isEmpty() || password2.isEmpty()) {
            return false;
        } else {
            return !password1.equals(password2);
        }
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

    public static void showErrorDialog(String msg, Context context) {
        if (context == null) {
            return;
        }
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle("Error")
                .setMessage(msg)
                .setPositiveButton("Ok", null)
                .create();
        alertDialog.show();
    }
}
