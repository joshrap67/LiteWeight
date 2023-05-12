package com.joshrap.liteweight.activities;

import android.app.Activity;
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
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
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
    private TextView passwordAttributesTV;
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
        passwordAttributesTV = findViewById(R.id.password_attributes_tv);
        Button signUpButton = findViewById(R.id.sign_up_btn);

        signUpButton.setOnClickListener(view -> {
            passwordAttributesTV.setVisibility(View.GONE); // wish I could do this after pswd loses focus, but can't get it to work

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

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        /*
            Found on SO. Hides keyboard when clicking outside editText.
            https://gist.github.com/sc0rch/7c982999e5821e6338c25390f50d2993
         */
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect viewRect = new Rect();
                v.getGlobalVisibleRect(viewRect);
                if (!viewRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    boolean touchTargetIsEditText = false;
                    //Check if another editText has been touched
                    for (View vi : v.getRootView().getTouchables()) {
                        if (vi instanceof EditText) {
                            Rect clickedViewRect = new Rect();
                            vi.getGlobalVisibleRect(clickedViewRect);
                            //Bounding box is to big, reduce it just a little bit
                            if (clickedViewRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                                touchTargetIsEditText = true;
                                break;
                            }
                        }
                    }
                    if (!touchTargetIsEditText) {
                        v.clearFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            }
        }
        return super.dispatchTouchEvent(event);
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
                if (errorMessage != null) {
                    passwordAttributesTV.setVisibility(View.VISIBLE);
                    passwordAttributesTV.setText(errorMessage);
                }
            }
        });
        passwordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String errorMessage = ValidatorUtils.validNewPassword(s.toString().trim());
                if (errorMessage == null) {
                    // no error so hide attributes
                    passwordAttributesTV.setVisibility(View.GONE);
                } else {
                    passwordAttributesTV.setText(errorMessage);
                    passwordAttributesTV.setVisibility(View.VISIBLE);
                }

                if (passwordLayout.isErrorEnabled()) {
                    // if an error is present, stop showing the error message once the user types (acknowledged it)
                    passwordLayout.setErrorEnabled(false);
                    passwordLayout.setError(null);
                }
                if (passwordConfirmLayout.isErrorEnabled() && passwordConfirmLayout.getError().equals(passwordNotMatchingMsg)) {
                    // if the passwords weren't matching, hide the error on confirmPassword edittext since user just acknowledged the error
                    passwordConfirmLayout.setErrorEnabled(false);
                    passwordConfirmLayout.setError(null);
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
                if (passwordConfirmLayout.isErrorEnabled()) {
                    // if an error is present, stop showing the error message once the user types (acknowledged it)
                    passwordConfirmLayout.setErrorEnabled(false);
                    passwordConfirmLayout.setError(null);
                }
                if (passwordLayout.isErrorEnabled() && passwordLayout.getError().equals(passwordNotMatchingMsg)) {
                    // if the passwords weren't matching, hide the error on confirmPassword edittext since user just acknowledged the error
                    passwordLayout.setErrorEnabled(false);
                    passwordLayout.setError(null);
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


    private void attemptSignUp(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                // Sign in success, update UI with the signed-in user's information
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null && user.isEmailVerified()) {
                    launchMainActivity();
                } else if (user != null && !user.isEmailVerified()) {
                    ActionCodeSettings actionCodeSettings = ActionCodeSettings.newBuilder()
                            // URL you want to redirect back to. The domain (www.example.com) for this
                            // URL must be whitelisted in the Firebase Console.
                            .setUrl("https://www.google.com") // todo a site
                            .setHandleCodeInApp(true)
                            .setAndroidPackageName(BuildConfig.APPLICATION_ID, true, "14")
                            .build();
                    user.sendEmailVerification(actionCodeSettings);
                    launchUnverifiedActivity();
                }
            } else {
                // If sign in fails, display a message to the user.
                AndroidUtils.showErrorDialog("Authentication failed", SignUpActivity.this);
            }
        });
    }

    private void launchMainActivity() {
        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void launchUnverifiedActivity() {
        Intent intent = new Intent(SignUpActivity.this, UnverifiedActivity.class);
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
