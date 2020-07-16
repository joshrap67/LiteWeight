package com.joshrap.liteweight.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.joshrap.liteweight.R;
import com.joshrap.liteweight.helpers.InputHelper;
import com.joshrap.liteweight.imports.Variables;

public class MainActivity extends AppCompatActivity {
    /*
        1. If a token key value pair exists in shared prefs
            If valid then immediately go to WorkoutActivity
            else prompt for sign in again
        2. If no token then check if guest mode is active in shared prefs
            If guest mode is active then immediately go to the WorkoutActivity
        3. Else the app has never been used before
           Prompt for signin/signup
     */

    private EditText usernameInput, passwordInput, emailInput, passwordConfirmInput;
    private Button primaryBtn, changeModeBtn, guestBtn;
    private TextInputLayout emailLayout, passwordConfirmLayout, usernameLayout, passwordLayout;
    private ImageView logo;
    private boolean signInMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in);
        this.usernameInput = findViewById(R.id.username_input);
        this.passwordInput = findViewById(R.id.password_input);
        this.passwordConfirmInput = findViewById(R.id.password_input_confirm);
        this.emailInput = findViewById(R.id.email_input);

        this.emailLayout = findViewById(R.id.email_input_layout);
        this.passwordConfirmLayout = findViewById(R.id.password_confirm_layout);
        this.usernameLayout = findViewById(R.id.username_input_layout);
        this.passwordLayout = findViewById(R.id.password_input_layout);

        this.logo = findViewById(R.id.app_logo);

        this.primaryBtn = findViewById(R.id.primary_btn);
        this.changeModeBtn = findViewById(R.id.change_mode_btn);
        this.guestBtn = findViewById(R.id.guest_btn);

        this.signInMode = true; // default to signing in
        updateUI();
        initInputs();

        // TODO password reset - fuck me
        String version = "v";
        try {
            version += getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        TextView versionTV = findViewById(R.id.app_version);
        versionTV.setText(version);

        primaryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (signInMode) {
                    if (attemptSignIn("fuck", "me")) {
                        launchWorkoutActivity();
                    }
                }
            }
        });
        changeModeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInMode = !signInMode;
                updateUI();
            }
        });
    }

    private boolean attemptSignIn(String username, String password) {
        // TODO authenticate with cognito
        // TODO validate input
        return false;
    }

    private boolean attemptSignUp() {

        return false;
    }

    private void updateUI() {
        this.passwordConfirmLayout.setVisibility(this.signInMode ? View.GONE : View.VISIBLE);
        this.emailLayout.setVisibility(this.signInMode ? View.GONE : View.VISIBLE);

        this.logo.setVisibility(this.signInMode ? View.VISIBLE : View.GONE);
        this.guestBtn.setVisibility(this.signInMode ? View.VISIBLE : View.VISIBLE);

        this.primaryBtn.setText(this.signInMode ? getString(R.string.sign_in) : getString(R.string.sign_up));
        this.changeModeBtn.setText(this.signInMode ? getString(R.string.sign_up) : "BACK TO " + getString(R.string.sign_in));

        this.usernameLayout.setHint(this.signInMode ? getString(R.string.username_and_email_hint) : getString(R.string.username_hint));
        // erase any input
        this.usernameInput.setText(null);
        this.passwordInput.setText(null);
        this.emailInput.setText(null);
        this.passwordConfirmInput.setText(null);
        // erase any errors
        this.emailLayout.setErrorEnabled(false);
        this.emailLayout.setError(null);
        this.usernameLayout.setErrorEnabled(false);
        this.usernameLayout.setError(null);
        this.passwordLayout.setErrorEnabled(false);
        this.passwordLayout.setError(null);
        this.passwordConfirmLayout.setErrorEnabled(false);
        this.passwordConfirmLayout.setError(null);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // used for hiding keyboard when user clicks outside the editText. Found on Stack Overflow
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }

    private void initInputs() {
        emailInput.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent keyevent) {
                if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    String errorMsg = InputHelper.validEmail(emailInput.getText().toString().trim());
                    if (errorMsg == null) {
                        emailLayout.setError(null);
                        return true;
                    } else {
                        emailLayout.setError(errorMsg);
                    }
                }
                return false;
            }
        });

        usernameInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_USERNAME_LENGTH)});
        usernameInput.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent keyevent) {
                if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    String errorMsg = InputHelper.validUsername(usernameInput.getText().toString().trim());
                    if (errorMsg == null) {
                        usernameLayout.setError(null);
                        return true;
                    } else {
                        usernameLayout.setError(errorMsg);
                    }
                }
                return false;
            }
        });
    }

    private void launchWorkoutActivity() {
        Intent intent = new Intent(MainActivity.this, WorkoutActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        startActivity(intent);
        MainActivity.this.finish();
    }
}
