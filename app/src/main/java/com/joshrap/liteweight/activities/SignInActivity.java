package com.joshrap.liteweight.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
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
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.joshrap.liteweight.R;
import com.joshrap.liteweight.helpers.InputHelper;
import com.joshrap.liteweight.imports.Globals;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.models.CognitoResponse;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.models.Tokens;
import com.joshrap.liteweight.models.UserWithWorkout;
import com.joshrap.liteweight.network.repos.CognitoRepository;
import com.joshrap.liteweight.network.repos.UserRepository;
import com.joshrap.liteweight.widgets.ErrorDialog;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

public class SignInActivity extends AppCompatActivity {
    private static final int SIGN_IN_VIEW = 0;
    private static final int SIGN_UP_VIEW = 1;
    private static final int CONFIRM_EMAIL_VIEW = 2;
    private static final int RESET_PASSWORD_VIEW = 3;
    private static final String passwordNotMatchingMsg = "Passwords do not match.";

    private EditText usernameInputSignIn, passwordInputSignIn, emailInputSignUp, passwordConfirmInputSignUp,
            usernameInputSignUp, passwordInputSignUp;
    private TextInputLayout emailLayoutSignUp, passwordConfirmLayoutSignUp, usernameLayoutSignUp, passwordLayoutSignUp,
            usernameLayoutSignIn, passwordLayoutSignIn;
    private ViewFlipper viewFlipper;
    private TextView passwordAttributesTV;
    private ProgressDialog loadingDialog;
    @Inject
    Tokens tokens;
    @Inject
    UserRepository userRepository;
    @Inject
    public SharedPreferences sharedPreferences;
    @Inject
    public CognitoRepository cognitoRepository;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Injector.getInjector(this).inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
        loadingDialog = new ProgressDialog(this);
        viewFlipper = findViewById(R.id.view_flipper);
        viewFlipper.setMeasureAllChildren(false);

        // get all the views for the sign in layout
        this.usernameInputSignIn = findViewById(R.id.sign_in_username_input);
        this.usernameLayoutSignIn = findViewById(R.id.sign_in_username_input_layout);
        this.passwordInputSignIn = findViewById(R.id.sign_in_password_input);
        this.passwordLayoutSignIn = findViewById(R.id.sign_in_password_input_layout);
        Button signInButton = findViewById(R.id.sign_in_primary_btn);
        // get all the views for the sign up layout
        this.emailInputSignUp = findViewById(R.id.sign_up_email_input);
        this.emailLayoutSignUp = findViewById(R.id.sign_up_email_input_layout);
        this.usernameInputSignUp = findViewById(R.id.sign_up_username_input);
        this.usernameLayoutSignUp = findViewById(R.id.sign_up_username_input_layout);
        this.passwordInputSignUp = findViewById(R.id.sign_up_password_input);
        this.passwordLayoutSignUp = findViewById(R.id.sign_up_password_input_layout);
        this.passwordConfirmInputSignUp = findViewById(R.id.sign_up_password_input_confirm);
        this.passwordConfirmLayoutSignUp = findViewById(R.id.sign_up_password_confirm_layout);
        this.passwordAttributesTV = findViewById(R.id.sign_up_password_attributes_tv);
        Button signUpButton = findViewById(R.id.sign_up_primary_btn);

        signInButton.setOnClickListener(view -> {
            if (validSignInInput()) {
                attemptSignIn(usernameInputSignIn.getText().toString().trim(), passwordInputSignIn.getText().toString().trim());
            }
        });

        signUpButton.setOnClickListener(view -> {
            passwordAttributesTV.setVisibility(View.GONE); // wish I could do this after pswd loses focus, but can't get it to work
            if (validSignUpInput()) {
                attemptSignUp(usernameInputSignUp.getText().toString().trim(), passwordInputSignUp.getText().toString().trim(),
                        emailInputSignUp.getText().toString().trim());
            }
        });

        Button switchToSignUp = findViewById(R.id.sign_in_change_mode_btn);
        Button switchToSignIn = findViewById(R.id.sign_up_change_mode_btn);
        switchToSignUp.setOnClickListener(view -> {
            // clear text before switching to the sign up page
            this.usernameInputSignIn.setText(null);
            this.passwordInputSignIn.setText(null);
            // erase any errors before switching to the sign up page
            this.usernameLayoutSignIn.setErrorEnabled(false);
            this.usernameLayoutSignIn.setError(null);
            this.passwordLayoutSignIn.setErrorEnabled(false);
            this.passwordLayoutSignIn.setError(null);
            confirmEmailAddress();
//            viewFlipper.setInAnimation(this, R.anim.slide_in_right);
//            viewFlipper.setOutAnimation(this, R.anim.slide_out_left);
//            viewFlipper.setDisplayedChild(CONFIRM_EMAIL_VIEW);
        });
        switchToSignIn.setOnClickListener(view -> {
            switchToSignInFromSignUp();
            viewFlipper.setInAnimation(this, android.R.anim.slide_in_left);
            viewFlipper.setOutAnimation(this, android.R.anim.slide_out_right);
            viewFlipper.setDisplayedChild(SIGN_IN_VIEW);
        });

        initEditTexts();
        // TODO password reset - fuck me
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

    @Override
    public void onBackPressed() {
        if (viewFlipper.getDisplayedChild() == SIGN_UP_VIEW) {
            switchToSignInFromSignUp();
            viewFlipper.setInAnimation(this, android.R.anim.slide_in_left);
            viewFlipper.setOutAnimation(this, android.R.anim.slide_out_right);
            viewFlipper.setDisplayedChild(SIGN_IN_VIEW);
        } else {
            super.onBackPressed();
        }
    }

    private void switchToSignInFromSignUp() {
        // clear text before switching to the sign up page
        usernameInputSignUp.setText(null);
        passwordInputSignUp.setText(null);
        emailInputSignUp.setText(null);
        passwordConfirmInputSignUp.setText(null);
        // erase any errors before switching to the sign up page
        emailLayoutSignUp.setErrorEnabled(false);
        emailLayoutSignUp.setError(null);
        usernameLayoutSignUp.setErrorEnabled(false);
        usernameLayoutSignUp.setError(null);
        passwordLayoutSignUp.setErrorEnabled(false);
        passwordLayoutSignUp.setError(null);
        passwordConfirmLayoutSignUp.setErrorEnabled(false);
        passwordConfirmLayoutSignUp.setError(null);
        // have to do this last otherwise onTextChanged will be called when i set text to null above
        passwordAttributesTV.setText(null);
        passwordAttributesTV.setVisibility(View.GONE);
    }

    private void showLoadingDialog(String message) {
        loadingDialog.setMessage(message);
        loadingDialog.show();
    }

    private boolean validSignInInput() {
        String usernameErrorMsg = InputHelper.validUsername(usernameInputSignIn.getText().toString().trim());
        if (usernameErrorMsg != null) {
            usernameLayoutSignIn.setError(usernameErrorMsg);
            usernameLayoutSignIn.startAnimation(shakeError());
        }
        String passwordErrorMsg = InputHelper.validPassword(passwordInputSignIn.getText().toString().trim());
        if (passwordErrorMsg != null) {
            passwordLayoutSignIn.setError(passwordErrorMsg);
            passwordLayoutSignIn.startAnimation(shakeError());
        }

        return (usernameErrorMsg == null) && (passwordErrorMsg == null);
    }

    private boolean validSignUpInput() {
        boolean validInput = true;
        String usernameErrorMsg = InputHelper.validNewUsername(usernameInputSignUp.getText().toString().trim());
        if (usernameErrorMsg != null) {
            usernameLayoutSignUp.setError(usernameErrorMsg);
            usernameLayoutSignUp.startAnimation(shakeError());
            validInput = false;
        }
        String emailErrorMsg = InputHelper.validNewEmail(emailInputSignUp.getText().toString().trim());
        if (emailErrorMsg != null) {
            emailLayoutSignUp.setError(emailErrorMsg);
            emailLayoutSignUp.startAnimation(shakeError());
            validInput = false;
        }
        String passwordErrorMsg = InputHelper.validNewPassword(passwordInputSignUp.getText().toString().trim());
        if (passwordErrorMsg != null) {
            passwordLayoutSignUp.setError(passwordErrorMsg);
            passwordLayoutSignUp.startAnimation(shakeError());
            validInput = false;
        }
        String passwordConfirmErrorMsg = InputHelper.validNewPassword(passwordInputSignUp.getText().toString().trim());
        if (passwordErrorMsg != null) {
            passwordConfirmLayoutSignUp.setError(passwordConfirmErrorMsg);
            passwordConfirmLayoutSignUp.startAnimation(shakeError());
            validInput = false;
        }
        // make sure that the passwords match assuming they are actually valid
        if (passwordErrorMsg == null && passwordConfirmErrorMsg == null &&
                !passwordInputSignUp.getText().toString().trim().equals(passwordConfirmInputSignUp.getText().toString().trim())) {
            passwordLayoutSignUp.setError(passwordNotMatchingMsg);
            passwordLayoutSignUp.startAnimation(shakeError());
            passwordConfirmLayoutSignUp.setError(passwordNotMatchingMsg);
            passwordConfirmLayoutSignUp.startAnimation(shakeError());
            validInput = false;
        }

        return validInput;
    }

    private void attemptSignIn(String username, String password) {
        showLoadingDialog("Signing in...");
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ResultStatus<CognitoResponse> resultStatus = this.cognitoRepository.initiateAuth(username, password);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                if (resultStatus.isSuccess()) {
                    signInSuccess(resultStatus);
                } else {
                    loadingDialog.dismiss();
                    ErrorDialog.showErrorDialog("Error", resultStatus.getErrorMessage(), this);
                }
            });
        });
    }

    private void signInSuccess(ResultStatus<CognitoResponse> resultStatus) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // might not be necessary to save them to shared prefs here as this should always be done in workout activity, but just to be safe
        editor.putString(Variables.REFRESH_TOKEN_KEY, resultStatus.getData().getRefreshToken());
        editor.putString(Variables.ID_TOKEN_KEY, resultStatus.getData().getIdToken());
        // update tokens singleton that the repositories will be using to connect to api gateway
        tokens.setIdToken(resultStatus.getData().getIdToken());
        tokens.setRefreshToken(resultStatus.getData().getRefreshToken());
        editor.apply();
        getUserWithWorkout();
    }

    private void attemptSignUp(String username, String password, String email) {
        showLoadingDialog("Signing up...");
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ResultStatus<Boolean> resultStatus = this.cognitoRepository.signUp(username, password, email);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                loadingDialog.dismiss();
                if (resultStatus.isSuccess()) {
                    confirmEmailAddress();
                } else {
                    ErrorDialog.showErrorDialog("Error", resultStatus.getErrorMessage(), this);
                }
            });
        });
    }

    private void confirmEmailAddress() {
        viewFlipper.setInAnimation(this, R.anim.slide_in_right);
        viewFlipper.setOutAnimation(this, R.anim.slide_out_left);
        viewFlipper.setDisplayedChild(CONFIRM_EMAIL_VIEW);
        final TextInputLayout codeLayout = findViewById(R.id.code_input_layout);
        final EditText codeInput = findViewById(R.id.code_input);
        codeInput.requestFocus(); // todo this doesn't work
        codeInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (codeLayout.isErrorEnabled()) {
                    codeLayout.setErrorEnabled(false);
                    codeLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        codeInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.EMAIL_CODE_LENGTH)});
        final Button confirmButton = findViewById(R.id.confirm_email_btn);
        confirmButton.setOnClickListener(view -> {
            if (codeInput.getText().toString().length() == Variables.EMAIL_CODE_LENGTH) {
                showLoadingDialog("Confirming...");
                Executor executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    ResultStatus<CognitoResponse> resultStatus = this.cognitoRepository.confirmSignUp(usernameInputSignUp.getText().toString(),
                            codeInput.getText().toString().trim());
                    Handler handler = new Handler(getMainLooper());
                    handler.post(() -> {
                        loadingDialog.dismiss();
                        if (resultStatus.isSuccess()) {
                            attemptSignIn(usernameInputSignUp.getText().toString().trim(),
                                    passwordInputSignUp.getText().toString().trim());
                        } else {
                            if (resultStatus.getErrorMessage().equals(CognitoResponse.expiredCodeErrorMsg) ||
                                    resultStatus.getErrorMessage().equals(CognitoResponse.incorrectCodeErrorMsg)) {
                                // don't kick user off this view if these errors occur
                                ErrorDialog.showErrorDialog("Error", resultStatus.getErrorMessage(), this);
                            } else {
                                ErrorDialog.showErrorDialog("Error", resultStatus.getErrorMessage(), this);
                                viewFlipper.showPrevious();
                            }
                        }
                    });
                });
            } else {
                codeLayout.setError("Please enter valid code.");
                codeLayout.startAnimation(shakeError());
            }
        });
        final Button resendCodeButton = findViewById(R.id.resend_code_btn);
        resendCodeButton.setOnClickListener(view -> {
            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                ResultStatus<Boolean> resultStatus = cognitoRepository.resendEmailConfirmationCode(usernameInputSignUp.getText().toString().trim());
                Handler handler = new Handler(getMainLooper());
                handler.post(() -> {
                    loadingDialog.dismiss();
                    if (resultStatus.isSuccess()) {
                        Toast.makeText(this, "Code successfully sent to your email.", Toast.LENGTH_LONG).show();
                    } else {
                        ErrorDialog.showErrorDialog("Error", resultStatus.getErrorMessage(), this);
                    }
                });
            });
        });

    }

    private void hideKeyboard(View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void initEditTexts() {
        // since user can sign in with username or email, can't restrict length by only username
        usernameInputSignIn.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_URL_LENGTH)});
        usernameInputSignIn.setOnKeyListener((View view, int keyCode, KeyEvent keyevent) -> {
            if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                // only show errors immediately when signing up
                String errorMsg = InputHelper.validNewUsername(usernameInputSignIn.getText().toString().trim());
                if (errorMsg == null) {
                    usernameLayoutSignIn.setError(null);
                    return true;
                } else {
                    usernameLayoutSignIn.setError(errorMsg);
                }
            }
            return false;
        });

        usernameInputSignIn.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (usernameLayoutSignIn.isErrorEnabled()) {
                    // if an error is present, stop showing the error message once the user types (acknowledged it)
                    usernameLayoutSignIn.setErrorEnabled(false);
                    usernameLayoutSignIn.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        passwordInputSignIn.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (passwordLayoutSignIn.isErrorEnabled()) {
                    // if an error is present, stop showing the error message once the user types (acknowledged it)
                    passwordLayoutSignIn.setErrorEnabled(false);
                    passwordLayoutSignIn.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        passwordInputSignIn.setOnKeyListener((View view, int keyCode, KeyEvent keyevent) -> {
            if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                // only show errors immediately when signing up
                String errorMsg = InputHelper.validPassword(passwordInputSignIn.getText().toString().trim());
                if (errorMsg == null) {
                    passwordLayoutSignIn.setError(null);
                    if (validSignInInput()) {
                        hideKeyboard(getCurrentFocus());
                        attemptSignIn(usernameInputSignIn.getText().toString().trim(), passwordInputSignIn.getText().toString().trim());
                    }
                    return true;
                } else {
                    passwordLayoutSignIn.setError(errorMsg);
                }
            }
            return false;
        });

        emailInputSignUp.setOnKeyListener((View v, int keyCode, KeyEvent keyevent) -> {
            if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                String errorMsg = InputHelper.validNewEmail(emailInputSignUp.getText().toString().trim());
                if (errorMsg == null) {
                    emailLayoutSignUp.setError(null);
                    return true;
                } else {
                    emailLayoutSignUp.setError(errorMsg);
                }
            }
            return false;

        });
        emailInputSignUp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (emailLayoutSignUp.isErrorEnabled()) {
                    // if an error is present, stop showing the error message once the user types (acknowledged it)
                    emailLayoutSignUp.setErrorEnabled(false);
                    emailLayoutSignUp.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        usernameInputSignUp.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_USERNAME_LENGTH)});
        usernameInputSignUp.setOnKeyListener((View view, int keyCode, KeyEvent keyevent) -> {
            if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                // only show errors immediately when signing up
                String errorMsg = InputHelper.validNewUsername(usernameInputSignUp.getText().toString().trim());
                if (errorMsg == null) {
                    usernameLayoutSignUp.setError(null);
                    return true;
                } else {
                    usernameLayoutSignUp.setError(errorMsg);
                }
            }
            return false;
        });

        usernameInputSignUp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (usernameLayoutSignUp.isErrorEnabled()) {
                    // if an error is present, stop showing the error message once the user types (acknowledged it)
                    usernameLayoutSignUp.setErrorEnabled(false);
                    usernameLayoutSignUp.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        passwordInputSignUp.setOnFocusChangeListener((View v, boolean hasFocus) -> {
            if (v.hasFocus()) {
                String errorMessage = InputHelper.validNewPassword(passwordInputSignUp.getText().toString().trim());
                if (errorMessage != null) {
                    passwordAttributesTV.setVisibility(View.VISIBLE);
                    passwordAttributesTV.setText(errorMessage);
                }
            }
        });

        passwordInputSignUp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String errorMessage = InputHelper.validNewPassword(s.toString().trim());
                if (errorMessage == null) {
                    // no error so hide attributes
                    passwordAttributesTV.setVisibility(View.GONE);
                } else {
                    passwordAttributesTV.setText(errorMessage);
                    passwordAttributesTV.setVisibility(View.VISIBLE);
                }

                if (passwordLayoutSignUp.isErrorEnabled()) {
                    // if an error is present, stop showing the error message once the user types (acknowledged it)
                    passwordLayoutSignUp.setErrorEnabled(false);
                    passwordLayoutSignUp.setError(null);
                }
                if (passwordConfirmLayoutSignUp.isErrorEnabled() && passwordConfirmLayoutSignUp.getError().equals(passwordNotMatchingMsg)) {
                    // if the passwords weren't matching, hide the error on confirmPassword edittext since user just acknowledged the error
                    passwordConfirmLayoutSignUp.setErrorEnabled(false);
                    passwordConfirmLayoutSignUp.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        passwordConfirmInputSignUp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (passwordConfirmLayoutSignUp.isErrorEnabled()) {
                    // if an error is present, stop showing the error message once the user types (acknowledged it)
                    passwordConfirmLayoutSignUp.setErrorEnabled(false);
                    passwordConfirmLayoutSignUp.setError(null);
                }
                if (passwordLayoutSignUp.isErrorEnabled() && passwordLayoutSignUp.getError().equals(passwordNotMatchingMsg)) {
                    // if the passwords weren't matching, hide the error on confirmPassword edittext since user just acknowledged the error
                    passwordLayoutSignUp.setErrorEnabled(false);
                    passwordLayoutSignUp.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        passwordConfirmInputSignUp.setOnKeyListener((View view, int keyCode, KeyEvent keyevent) -> {
            if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                if (validSignUpInput()) {
                    hideKeyboard(getCurrentFocus());
                    attemptSignUp(usernameInputSignUp.getText().toString().trim(),
                            passwordInputSignUp.getText().toString().trim(), emailInputSignUp.getText().toString().trim());
                }
                return true;
            }
            return false;
        });
    }

    private void getUserWithWorkout() {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
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
        finish();
    }

    public TranslateAnimation shakeError() {
        TranslateAnimation shake = new TranslateAnimation(0, 10, 0, 0);
        shake.setDuration(350);
        shake.setInterpolator(new CycleInterpolator(2));
        return shake;
    }
}
