package com.joshrap.liteweight.activities;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.utils.AndroidUtils;
import com.joshrap.liteweight.utils.ValidatorUtils;

public class ResetPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.getInjector(this).inject(this);
        FirebaseAuth auth = FirebaseAuth.getInstance();

        setContentView(R.layout.activity_reset_password_layout);

        EditText emailInput = findViewById(R.id.email_input);
        TextInputLayout emailInputLayout = findViewById(R.id.email_input_layout);
        Button sendResetPasswordEmailButton = findViewById(R.id.send_reset_email_btn);
        sendResetPasswordEmailButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            if (email.isEmpty()) {
                return;
            }

            auth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Email sent successfully", Toast.LENGTH_LONG).show();
                }
            });
        });
        Button backToSignInButton = findViewById(R.id.back_to_sign_in_btn);
        backToSignInButton.setOnClickListener(v -> finish());

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
        if (getIntent().getExtras() != null) {
            String errorMessage = getIntent().getExtras().getString(Variables.INTENT_ERROR_MESSAGE);
            if (errorMessage != null) {
                AndroidUtils.showErrorDialog(errorMessage, this);
            }
        }
    }
}
