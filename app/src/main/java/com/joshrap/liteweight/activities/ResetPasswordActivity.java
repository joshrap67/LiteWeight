package com.joshrap.liteweight.activities;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.utils.AndroidUtils;
import com.joshrap.liteweight.utils.ValidatorUtils;


public class ResetPasswordActivity extends AppCompatActivity {

    private EditText emailInput;
    private TextInputLayout emailInputLayout;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.getInjector(this).inject(this);
        mAuth = FirebaseAuth.getInstance();

        setContentView(R.layout.activity_reset_password_layout);

        emailInput = findViewById(R.id.email_input);
        emailInputLayout = findViewById(R.id.email_input_layout);
        Button sendResetPasswordEmailButton = findViewById(R.id.send_reset_email_btn);
        Button backToSignInButton = findViewById(R.id.back_to_sign_in_btn);
        backToSignInButton.setOnClickListener(v -> finish());

        initEditTexts();
        if (getIntent().getExtras() != null) {
            String errorMessage = getIntent().getExtras().getString(Variables.ERROR_MESSAGE);
            if (errorMessage != null) {
                AndroidUtils.showErrorDialog(errorMessage, this);
            }
        }
    }

    private void initEditTexts() {
        emailInput.setOnKeyListener((View v, int keyCode, KeyEvent keyevent) -> {
            if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                String errorMsg = ValidatorUtils.validNewEmail(emailInput.getText().toString().trim());
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
    }
}
