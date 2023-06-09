package com.joshrap.liteweight.activities;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.utils.AndroidUtils;
import com.joshrap.liteweight.utils.ValidatorUtils;

import javax.inject.Inject;

public class SignInWithEmailActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private TextInputLayout emailInputLayout, passwordInputLayout;
    private FirebaseAuth auth;
    private boolean shouldFinish;

    @Inject
    AlertDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Injector.getInjector(this).inject(this);
        auth = FirebaseAuth.getInstance();

        setContentView(R.layout.activity_sign_in_with_email_layout);

        emailInput = findViewById(R.id.email_input);
        emailInputLayout = findViewById(R.id.email_input_layout);
        passwordInput = findViewById(R.id.password_input);
        passwordInputLayout = findViewById(R.id.password_input_layout);

        Button signInButton = findViewById(R.id.sign_in_btn);
        Button backButton = findViewById(R.id.back_to_sign_in_btn);
        backButton.setOnClickListener(view -> onBackPressed());
        signInButton.setOnClickListener(view -> {
            if (validSignInInput()) {
                attemptSignIn(emailInput.getText().toString().trim(), passwordInput.getText().toString().trim());
            }
        });

        TextView resetPasswordTV = findViewById(R.id.forgot_password_tv);
        resetPasswordTV.setOnClickListener(view -> launchResetPassword());

        initEditTexts();
        if (getIntent().getExtras() != null) {
            String errorMessage = getIntent().getExtras().getString(Variables.INTENT_ERROR_MESSAGE);
            if (errorMessage != null) {
                AndroidUtils.showErrorDialog(errorMessage, this);
            }
        }
    }

    private void initEditTexts() {
        emailInput.setOnKeyListener((View view, int keyCode, KeyEvent keyevent) -> {
            if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                String errorMsg = ValidatorUtils.validEmail(emailInput.getText().toString().trim());
                if (errorMsg == null) {
                    emailInputLayout.setError(null);
                    return true;
                } else {
                    emailInputLayout.setError(errorMsg);
                }
            }
            return false;
        });
        emailInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(emailInputLayout));

        passwordInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(passwordInputLayout));
        passwordInput.setOnKeyListener((View view, int keyCode, KeyEvent keyevent) -> {
            if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                // on enter try to sign in if input is valid
                String errorMsg = ValidatorUtils.validPassword(passwordInput.getText().toString().trim());
                if (errorMsg == null) {
                    passwordInputLayout.setError(null);
                    if (validSignInInput()) {
                        attemptSignIn(emailInput.getText().toString().trim(), passwordInput.getText().toString().trim());
                    }
                    return true;
                } else {
                    passwordInputLayout.setError(errorMsg);
                }
            }
            return false;
        });
    }

    private boolean validSignInInput() {
        String emailErrorMsg = ValidatorUtils.validEmail(emailInput.getText().toString().trim());
        if (emailErrorMsg != null) {
            emailInputLayout.setError(emailErrorMsg);
            emailInputLayout.startAnimation(AndroidUtils.shakeError(2));
        }
        String passwordErrorMsg = ValidatorUtils.validPassword(passwordInput.getText().toString().trim());
        if (passwordErrorMsg != null) {
            passwordInputLayout.setError(passwordErrorMsg);
            passwordInputLayout.startAnimation(AndroidUtils.shakeError(2));
        }

        return (emailErrorMsg == null) && (passwordErrorMsg == null);
    }

    private void attemptSignIn(String email, String password) {
        hideKeyboard(getCurrentFocus());
        AndroidUtils.showLoadingDialog(loadingDialog, "Signing in...");
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            loadingDialog.dismiss();
            if (task.isSuccessful()) {
                FirebaseUser user = auth.getCurrentUser();
                if (user != null && user.isEmailVerified()) {
                    launchMainActivity();
                } else if (user != null && !user.isEmailVerified()) {
                    launchUnverifiedActivity();
                }
            } else {
                AndroidUtils.showErrorDialog("Authentication failed", this);
            }
        });
    }

    private void launchMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
        shouldFinish = true;
    }

    private void launchUnverifiedActivity() {
        Intent intent = new Intent(this, UnverifiedActivity.class);
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
        shouldFinish = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        // prevents flash of activity being finished when transition animations are used
        if (shouldFinish) {
            finish();
        }
    }

    private void launchResetPassword() {
        Intent intent = new Intent(this, ResetPasswordActivity.class);
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
    }

    private void hideKeyboard(View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
