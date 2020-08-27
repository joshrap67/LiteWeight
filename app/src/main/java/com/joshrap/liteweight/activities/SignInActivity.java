package com.joshrap.liteweight.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.textfield.TextInputLayout;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.joshrap.liteweight.R;
import com.joshrap.liteweight.helpers.InputHelper;
import com.joshrap.liteweight.imports.Globals;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.models.CognitoResponse;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.models.User;
import com.joshrap.liteweight.models.UserWithWorkout;
import com.joshrap.liteweight.network.CognitoGateway;
import com.joshrap.liteweight.network.repos.UserRepository;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import javax.inject.Inject;

public class SignInActivity extends AppCompatActivity {

    private EditText usernameInput, passwordInput, emailInput, passwordConfirmInput;
    private Button primaryBtn, changeModeBtn, guestBtn;
    private TextInputLayout emailLayout, passwordConfirmLayout, usernameLayout, passwordLayout;
    private LinearLayout passwordAttributesLayout;
    private ImageView logo;
    private boolean signInMode;
    private static final String passwordNotMatchingMsg = "Passwords do not match.";
    private AlertDialog confirmEmailPopup;
    private ProgressDialog loadingDialog;
    @Inject
    UserRepository userRepository;
    @Inject
    public SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Injector.getInjector(this).inject(this);
        loadingDialog = new ProgressDialog(SignInActivity.this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        this.usernameInput = findViewById(R.id.username_input);
        this.passwordInput = findViewById(R.id.password_input);
        this.passwordConfirmInput = findViewById(R.id.password_input_confirm);
        this.emailInput = findViewById(R.id.email_input);

        this.emailLayout = findViewById(R.id.email_input_layout);
        this.passwordConfirmLayout = findViewById(R.id.password_confirm_layout);
        this.usernameLayout = findViewById(R.id.username_input_layout);
        this.passwordLayout = findViewById(R.id.password_input_layout);
        this.passwordAttributesLayout = findViewById(R.id.password_attributes_layout);

        this.logo = findViewById(R.id.app_logo);

        this.primaryBtn = findViewById(R.id.primary_btn);
        this.changeModeBtn = findViewById(R.id.change_mode_btn);
        this.guestBtn = findViewById(R.id.guest_btn);

        this.signInMode = true; // default to signing in
        updateUI();
        initInputs();

        // TODO password reset - fuck me
        primaryBtn.setOnClickListener((View v) -> {
            if (signInMode) {
                if (validSignInInput()) {
                    attemptSignIn(usernameInput.getText().toString().trim(), passwordInput.getText().toString().trim());
                }

            } else {
                passwordAttributesLayout.setVisibility(View.GONE);
                if (validSignUpInput()) {
                    attemptSignUp(usernameInput.getText().toString().trim(), passwordInput.getText().toString().trim(),
                            emailInput.getText().toString().trim());
                }
            }
        });
        changeModeBtn.setOnClickListener((View v) -> {
            signInMode = !signInMode;
            updateUI();
        });
        // TODO guest mode
    }

    private void showLoadingDialog(String message) {
        loadingDialog.setMessage(message);
        loadingDialog.show();
    }

    private boolean validSignInInput() {
        String usernameErrorMsg = InputHelper.validUsername(usernameInput.getText().toString().trim());
        if (usernameErrorMsg != null) {
            usernameLayout.setError(usernameErrorMsg);
            usernameLayout.startAnimation(shakeError());
        }
        String passwordErrorMsg = InputHelper.validPassword(passwordInput.getText().toString().trim());
        if (passwordErrorMsg != null) {
            passwordLayout.setError(passwordErrorMsg);
            passwordLayout.startAnimation(shakeError());
        }

        return (usernameErrorMsg == null) && (passwordErrorMsg == null);
    }

    private boolean validSignUpInput() {
        boolean validInput = true;
        String usernameErrorMsg = InputHelper.validNewUsername(usernameInput.getText().toString().trim());
        if (usernameErrorMsg != null) {
            usernameLayout.setError(usernameErrorMsg);
            usernameLayout.startAnimation(shakeError());
            validInput = false;
        }
        String emailErrorMsg = InputHelper.validNewEmail(emailInput.getText().toString().trim());
        if (emailErrorMsg != null) {
            emailLayout.setError(emailErrorMsg);
            emailLayout.startAnimation(shakeError());
            validInput = false;
        }
        String passwordErrorMsg = InputHelper.validNewPassword(passwordInput.getText().toString().trim());
        if (passwordErrorMsg != null) {
            passwordLayout.setError(passwordErrorMsg);
            passwordLayout.startAnimation(shakeError());
            validInput = false;
        }
        String passwordConfirmErrorMsg = InputHelper.validNewPassword(passwordInput.getText().toString().trim());
        if (passwordErrorMsg != null) {
            passwordConfirmLayout.setError(passwordConfirmErrorMsg);
            passwordConfirmLayout.startAnimation(shakeError());
            validInput = false;
        }
        // make sure that the passwords match assuming they are actually valid
        if (passwordErrorMsg == null && passwordConfirmErrorMsg == null &&
                !passwordInput.getText().toString().trim().equals(passwordConfirmInput.getText().toString().trim())) {
            passwordLayout.setError(passwordNotMatchingMsg);
            passwordLayout.startAnimation(shakeError());
            passwordConfirmLayout.setError(passwordNotMatchingMsg);
            passwordConfirmLayout.startAnimation(shakeError());
        }

        return validInput;
    }

    private void attemptSignIn(String username, String password) {
        showLoadingDialog("Signing in...");
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ResultStatus<CognitoResponse> resultStatus = CognitoGateway.initiateAuth(username, password);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                if (resultStatus.isSuccess()) {
                    signInSuccess(resultStatus);
                } else {
                    signInFailed(resultStatus);
                }
            });
        });
    }

    private void signInSuccess(ResultStatus<CognitoResponse> resultStatus) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Variables.SHARED_PREF_SETTINGS, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(Variables.REFRESH_TOKEN_KEY, resultStatus.getData().getRefreshToken());
        editor.putString(Variables.ID_TOKEN_KEY, resultStatus.getData().getIdToken());
        editor.apply();
        Globals.refreshToken = resultStatus.getData().getRefreshToken();
        Globals.idToken = resultStatus.getData().getIdToken();
        getUser();

    }

    private void signInFailed(ResultStatus<CognitoResponse> resultStatus) {
        loadingDialog.dismiss();
        AlertDialog f = new AlertDialog.Builder(SignInActivity.this, R.style.AlertDialogTheme)
                .setTitle("Failed :(")
                .setMessage(resultStatus.getErrorMessage())
                .setPositiveButton("Done", null)
                .create();
        f.show();
    }

    private void attemptSignUp(String username, String password, String email) {
        showLoadingDialog("Signing up...");
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ResultStatus<Boolean> resultStatus = CognitoGateway.signUp(username, password, email);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                loadingDialog.dismiss();
                if (resultStatus.isSuccess()) {
                    signUpSuccess(resultStatus);
                } else {
                    signUpFailed(resultStatus);
                }
            });
        });
    }

    private void signUpSuccess(ResultStatus<Boolean> resultStatus) {
        View popupView = getLayoutInflater().inflate(R.layout.popup_confirm_email, null);
        final EditText codeInput = popupView.findViewById(R.id.code_input);
        confirmEmailPopup = new AlertDialog.Builder(SignInActivity.this, R.style.AlertDialogTheme)
                .setTitle("Confirm Account")
                .setView(popupView)
                .setPositiveButton("Submit", null)
                .create();
        confirmEmailPopup.setOnShowListener((DialogInterface dialogInterface) -> {
            Button saveButton = confirmEmailPopup.getButton(AlertDialog.BUTTON_POSITIVE);
            saveButton.setOnClickListener((View view) -> {
                // TODO use api to confirm
                Executor executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    ResultStatus<Boolean> resultStatus1 = CognitoGateway.confirmSignUp(usernameInput.getText().toString(),
                            codeInput.getText().toString().trim());
                    Handler handler = new Handler(getMainLooper());
                    handler.post(() -> {
                        confirmEmailPopup.dismiss();
                        if (resultStatus1.isSuccess()) {
                            confirmSuccess(resultStatus1);
                        } else {
                            confirmFailed(resultStatus1);
                        }
                    });
                });
            });
        });
        confirmEmailPopup.show();
    }

    private void confirmSuccess(ResultStatus<Boolean> resultStatus) {
        AlertDialog succ = new AlertDialog.Builder(SignInActivity.this, R.style.AlertDialogTheme)
                .setTitle("Success")
                .setPositiveButton("Done", null)
                .create();
        succ.show();
        newUser();
    }

    private void signUpFailed(ResultStatus<Boolean> resultStatus) {
        AlertDialog f = new AlertDialog.Builder(SignInActivity.this, R.style.AlertDialogTheme)
                .setTitle("Failed :(")
                .setMessage(resultStatus.getErrorMessage())
                .setPositiveButton("Done", null)
                .create();
        f.show();
    }

    private void confirmFailed(ResultStatus<Boolean> resultStatus) {
        AlertDialog f = new AlertDialog.Builder(SignInActivity.this, R.style.AlertDialogTheme)
                .setTitle("Failed :(")
                .setMessage(resultStatus.getErrorMessage())
                .setPositiveButton("Done", null)
                .create();
        f.show();
    }

    private void updateUI() {
        this.passwordAttributesLayout.setVisibility(View.GONE);
        this.passwordConfirmLayout.setVisibility(this.signInMode ? View.GONE : View.VISIBLE);
        this.emailLayout.setVisibility(this.signInMode ? View.GONE : View.VISIBLE);

        this.logo.setVisibility(this.signInMode ? View.VISIBLE : View.GONE);
        this.guestBtn.setVisibility(this.signInMode ? View.VISIBLE : View.GONE);

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
        emailInput.setOnKeyListener((View v, int keyCode, KeyEvent keyevent) -> {
            if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                String errorMsg = InputHelper.validNewEmail(emailInput.getText().toString().trim());
                if (errorMsg == null) {
                    emailLayout.setError(null);
                    return true;
                } else {
                    emailLayout.setError(errorMsg);
                }
            }
            return false;

        });
        emailInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (emailLayout.isErrorEnabled()) {
                    // if an error is present, stop showing the error message once the user types (acknowledged it)
                    emailLayout.setErrorEnabled(false);
                    emailLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        usernameInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_USERNAME_LENGTH)});
        usernameInput.setOnKeyListener((View view, int keyCode, KeyEvent keyevent) -> {
            if (!signInMode && (keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                // only show errors immediately when signing up
                String errorMsg = InputHelper.validNewUsername(usernameInput.getText().toString().trim());
                if (errorMsg == null) {
                    usernameLayout.setError(null);
                    return true;
                } else {
                    usernameLayout.setError(errorMsg);
                }
            }
            return false;
        });

        usernameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (usernameLayout.isErrorEnabled()) {
                    // if an error is present, stop showing the error message once the user types (acknowledged it)
                    usernameLayout.setErrorEnabled(false);
                    usernameLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        passwordInput.setOnFocusChangeListener((View v, boolean hasFocus) -> {
            if (v.hasFocus()) {
                if (!signInMode) {
                    // we only want to check valid new passwords if user is signing up, not signing in
                    checkPasswordAttributes(passwordInput.getText().toString().trim());
                    passwordAttributesLayout.setVisibility(View.VISIBLE);
                }
            }
        });
        passwordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!signInMode) {
                    // we only want to check valid new passwords if user is signing up, not signing in
                    checkPasswordAttributes(s.toString().trim());
                }
                if (passwordLayout.isErrorEnabled()) {
                    // if an error is present, stop showing the error message once the user types (acknowledged it)
                    passwordLayout.setErrorEnabled(false);
                    passwordLayout.setError(null);
                }
                if (passwordConfirmLayout.isErrorEnabled() && passwordConfirmLayout.getError().equals(passwordNotMatchingMsg)) {
                    // if the passwords weren't matching, hide the error on confirm one since user just acknowledged the error
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
                    // if the passwords weren't matching, hide the error on confirm one since user just acknowledged the error
                    passwordLayout.setErrorEnabled(false);
                    passwordLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void checkPasswordAttributes(String password) {
        password = password.trim();
        TextView lengthTv = findViewById(R.id.password_length_tv);
        TextView lowercaseTV = findViewById(R.id.password_lowercase_tv);
        TextView uppercaseTV = findViewById(R.id.password_uppercase_tv);
        TextView numberTV = findViewById(R.id.password_numbers_tv);
        TextView specialCharTV = findViewById(R.id.password_special_chars_tv);

        if (password.length() < Variables.MIN_PASSWORD_LENGTH) {
            lengthTv.setTextColor(getResources().getColor(R.color.password_constraint_unsuccessful_match));
        } else {
            lengthTv.setTextColor(getResources().getColor(R.color.password_constraint_successful_match));
        }

        if (!Pattern.compile("^.*[a-z].*").matcher(password).find()) {
            lowercaseTV.setTextColor(getResources().getColor(R.color.password_constraint_unsuccessful_match));
        } else {
            lowercaseTV.setTextColor(getResources().getColor(R.color.password_constraint_successful_match));
        }

        if (!Pattern.compile("^.*[A-Z].*").matcher(password).find()) {
            uppercaseTV.setTextColor(getResources().getColor(R.color.password_constraint_unsuccessful_match));
        } else {
            uppercaseTV.setTextColor(getResources().getColor(R.color.password_constraint_successful_match));
        }

        if (!Pattern.compile("^.*\\d.*").matcher(password).find()) {
            numberTV.setTextColor(getResources().getColor(R.color.password_constraint_unsuccessful_match));
        } else {
            numberTV.setTextColor(getResources().getColor(R.color.password_constraint_successful_match));
        }
        // TODO add one for invalid chars so they know the invalid char is gone, in that case it would be a hidden textview

        // TODO fix this regex to actually include them all
        if (!Pattern.compile("^.*[!@#$%^&*()_].*").matcher(password).find()) {
            specialCharTV.setTextColor(getResources().getColor(R.color.password_constraint_unsuccessful_match));
        } else {
            specialCharTV.setTextColor(getResources().getColor(R.color.password_constraint_successful_match));
        }
    }

    private void newUser() {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            System.out.println("New user...");
            ResultStatus<User> resultStatus = this.userRepository.newUser(usernameInput.getText().toString().trim());
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                if (resultStatus.isSuccess()) {
                    System.out.println("**************** USER NEW SUCCEEDED *****************");
                    Globals.user = resultStatus.getData();
                    launchWorkoutActivity();
                } else {
                    System.out.println("**************** USER NEW FAILED *****************");
                    System.out.println(resultStatus.getErrorMessage());
                }
            });
        });
    }

    private void getUser() {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            System.out.println("Getting user...");
            ResultStatus<UserWithWorkout> resultStatus = this.userRepository.getUserAndCurrentWorkout();
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                loadingDialog.dismiss();
                if (resultStatus.isSuccess()) {
                    System.out.println("**************** USER GET SUCCEEDED *****************");
                    Globals.user = resultStatus.getData().getUser();
                    Globals.activeWorkout = resultStatus.getData().getWorkout();
                    launchWorkoutActivity();
                } else {
                    System.out.println("**************** USER GET FAILED *****************");
                    System.out.println(resultStatus.getErrorMessage());
                }
            });
        });
    }

    private void launchWorkoutActivity() {
        Intent intent = new Intent(SignInActivity.this, WorkoutActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        startActivity(intent);
        SignInActivity.this.finish();
    }

    public TranslateAnimation shakeError() {
        TranslateAnimation shake = new TranslateAnimation(0, 10, 0, 0);
        shake.setDuration(350);
        shake.setInterpolator(new CycleInterpolator(2));
        return shake;
    }
}
