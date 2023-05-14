package com.joshrap.liteweight.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.common.base.Strings;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.joshrap.liteweight.BuildConfig;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.utils.AndroidUtils;
import com.joshrap.liteweight.utils.ValidatorUtils;

public class SignUpActivity extends AppCompatActivity {
    private static final String passwordNotMatchingMsg = "Passwords do not match.";

    private EditText emailInput, passwordInput, passwordConfirmInput;
    private TextInputLayout emailLayout, passwordConfirmLayout, passwordLayout;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        setContentView(R.layout.activity_sign_up_layout);

        emailInput = findViewById(R.id.email_input);
        emailLayout = findViewById(R.id.email_input_layout);
        passwordInput = findViewById(R.id.password_input);
        passwordLayout = findViewById(R.id.password_input_layout);
        passwordConfirmInput = findViewById(R.id.password_confirm_input);
        passwordConfirmLayout = findViewById(R.id.password_confirm_input_layout);
        Button signUpButton = findViewById(R.id.sign_up_btn);

        signUpButton.setOnClickListener(view -> {
            if (validSignUpInput()) {
                attemptSignUp(emailInput.getText().toString().trim(), passwordInput.getText().toString().trim());
            }
        });

        Button switchToSignIn = findViewById(R.id.back_to_sign_in_btn);
        switchToSignIn.setOnClickListener(v -> finish());

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
                    emailLayout.setError(null);
                    return true;
                } else {
                    emailLayout.setError(errorMsg);
                }
            }
            return false;

        });
        emailInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(emailLayout));

        passwordInput.setOnFocusChangeListener((View v, boolean hasFocus) -> {
            if (v.hasFocus()) {
                String errorMessage = ValidatorUtils.validNewPassword(passwordInput.getText().toString().trim());
                passwordLayout.setError(errorMessage);
            }
        });
        passwordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String errorMessage = ValidatorUtils.validNewPassword(passwordInput.getText().toString().trim());
                passwordLayout.setError(errorMessage);

                if (passwordsDoNotMatch() && Strings.isNullOrEmpty(errorMessage)) {
                    passwordLayout.setError(passwordNotMatchingMsg);
                } else if (passwordsDoNotMatch()) {
                    passwordLayout.setError(errorMessage + passwordNotMatchingMsg);
                } else {
                    passwordLayout.setError(errorMessage);
                    passwordLayout.setErrorEnabled(errorMessage != null);
                }

                String confirmPassword = passwordConfirmInput.getText().toString().trim();
                if (confirmPassword.isEmpty()) return;

                String confirmPasswordErrorMessage = ValidatorUtils.validNewPassword(confirmPassword);
                if (passwordsDoNotMatch() && confirmPasswordErrorMessage == null) {
                    passwordConfirmLayout.setError(passwordNotMatchingMsg);
                } else if (passwordsDoNotMatch()) {
                    passwordConfirmLayout.setError(confirmPasswordErrorMessage + passwordNotMatchingMsg);
                } else {
                    passwordConfirmLayout.setError(confirmPasswordErrorMessage);
                    passwordConfirmLayout.setErrorEnabled(confirmPasswordErrorMessage != null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        passwordConfirmInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String errorMessage = ValidatorUtils.validNewPassword(passwordConfirmInput.getText().toString().trim());

                if (passwordsDoNotMatch() && Strings.isNullOrEmpty(errorMessage)) {
                    passwordConfirmLayout.setError(passwordNotMatchingMsg);
                } else if (passwordsDoNotMatch()) {
                    passwordConfirmLayout.setError(errorMessage + passwordNotMatchingMsg);
                } else {
                    passwordConfirmLayout.setError(errorMessage);
                    passwordConfirmLayout.setErrorEnabled(errorMessage != null);
                }

                String password = passwordInput.getText().toString().trim();
                if (password.isEmpty()) return;

                String passwordErrorMessage = ValidatorUtils.validNewPassword(password);
                if (passwordsDoNotMatch() && Strings.isNullOrEmpty(passwordErrorMessage)) {
                    passwordLayout.setError(passwordNotMatchingMsg);
                } else if (passwordsDoNotMatch()) {
                    passwordLayout.setError(passwordErrorMessage + passwordNotMatchingMsg);
                } else {
                    passwordLayout.setError(passwordErrorMessage);
                    passwordLayout.setErrorEnabled(passwordErrorMessage != null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        passwordConfirmInput.setOnKeyListener((View view, int keyCode, KeyEvent keyevent) -> {
            if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                // if all valid input, try to sign up after user hits enter button
                if (validSignUpInput()) {
                    hideKeyboard(getCurrentFocus());
                    attemptSignUp(emailInput.getText().toString().trim(), passwordInput.getText().toString().trim());
                }
                return true;
            }
            return false;
        });
    }

    private boolean passwordsDoNotMatch() {
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = passwordConfirmInput.getText().toString().trim();
        if (password.isEmpty() || confirmPassword.isEmpty()) {
            return false;
        } else {
            return !password.equals(confirmPassword);
        }
    }

    private void attemptSignUp(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                // Sign in success, update UI with the signed-in user's information
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null && user.isEmailVerified()) {
                    launchMainActivity();
                } else if (user != null && !user.isEmailVerified()) {
//                    ActionCodeSettings actionCodeSettings = ActionCodeSettings.newBuilder()
//                            // URL you want to redirect back to. The domain (www.example.com) for this
//                            // URL must be whitelisted in the Firebase Console.
//                            .setUrl("https://www.google.com") // todo a site
//                            .setHandleCodeInApp(true)
//                            .setAndroidPackageName(BuildConfig.APPLICATION_ID, true, "14")
//                            .build();
                    user.sendEmailVerification();
                    launchUnverifiedActivity();
                }
            } else {
                // If sign in fails, display a message to the user.
                AndroidUtils.showErrorDialog("Authentication failed", SignUpActivity.this);
            }
        });
    }

    private void launchMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void launchUnverifiedActivity() {
        Intent intent = new Intent(this, UnverifiedActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean validSignUpInput() {
        boolean validInput = true;
        String emailErrorMsg = ValidatorUtils.validNewEmail(emailInput.getText().toString().trim());
        if (emailErrorMsg != null) {
            emailLayout.setError(emailErrorMsg);
            emailLayout.startAnimation(AndroidUtils.shakeError(2));
            validInput = false;
        }
        String passwordErrorMsg = ValidatorUtils.validNewPassword(passwordInput.getText().toString().trim());
        if (passwordErrorMsg != null) {
            passwordLayout.setError(passwordErrorMsg);
            passwordLayout.startAnimation(AndroidUtils.shakeError(2));
            validInput = false;
        }
        String passwordConfirmErrorMsg = ValidatorUtils.validNewPassword(passwordInput.getText().toString().trim());
        if (passwordConfirmErrorMsg != null) {
            passwordConfirmLayout.setError(passwordConfirmErrorMsg);
            passwordConfirmLayout.startAnimation(AndroidUtils.shakeError(2));
            validInput = false;
        }
        // make sure that the passwords match assuming they are actually valid
        if (passwordErrorMsg == null && passwordConfirmErrorMsg == null &&
                !passwordInput.getText().toString().trim().equals(passwordConfirmInput.getText().toString().trim())) {
            passwordLayout.setError(passwordNotMatchingMsg);
            passwordLayout.startAnimation(AndroidUtils.shakeError(2));
            passwordConfirmLayout.setError(passwordNotMatchingMsg);
            passwordConfirmLayout.startAnimation(AndroidUtils.shakeError(2));
            validInput = false;
        }

        return validInput;
    }

    private void hideKeyboard(View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
