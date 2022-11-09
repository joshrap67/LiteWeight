package com.joshrap.liteweight.activities;

import android.app.Activity;

import androidx.appcompat.app.AlertDialog;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.joshrap.liteweight.R;
import com.joshrap.liteweight.imports.BackendConfig;
import com.joshrap.liteweight.imports.Globals;
import com.joshrap.liteweight.utils.AndroidUtils;
import com.joshrap.liteweight.utils.ValidatorUtils;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.models.CognitoResponse;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.models.Tokens;
import com.joshrap.liteweight.models.UserWithWorkout;
import com.joshrap.liteweight.network.repos.CognitoRepository;
import com.joshrap.liteweight.network.repos.UserRepository;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

public class SignInActivity extends AppCompatActivity {
    private static final int SIGN_IN_VIEW = 0;
    private static final int SIGN_UP_VIEW = 1;
    private static final int CONFIRM_EMAIL_VIEW = 2;
    private static final int RESET_PASSWORD_VIEW = 3;
    private static final String passwordNotMatchingMsg = "Passwords do not match.";
    private static final int RC_SIGN_IN = 69;

    private EditText usernameInputSignIn, passwordInputSignIn, emailInputSignUp, passwordConfirmInputSignUp,
            usernameInputSignUp, passwordInputSignUp;
    private TextInputLayout emailLayoutSignUp, passwordConfirmLayoutSignUp, usernameLayoutSignUp, passwordLayoutSignUp,
            usernameLayoutSignIn, passwordLayoutSignIn;
    private ViewFlipper viewFlipper;
    private TextView passwordAttributesTV;
    private GoogleSignInClient googleSignInClient;
    private AlertDialog alertDialog;

    @Inject
    AlertDialog loadingDialog;
    @Inject
    Tokens tokens;
    @Inject
    UserRepository userRepository;
    @Inject
    SharedPreferences sharedPreferences;
    @Inject
    CognitoRepository cognitoRepository;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.getInjector(this).inject(this);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(BackendConfig.googleSignInClientId)
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        setContentView(R.layout.activity_login_page);
        viewFlipper = findViewById(R.id.view_flipper);
        viewFlipper.setMeasureAllChildren(false);

        // get all the views for the sign in layout
        usernameInputSignIn = findViewById(R.id.sign_in_username_input);
        usernameLayoutSignIn = findViewById(R.id.sign_in_username_input_layout);
        passwordInputSignIn = findViewById(R.id.sign_in_password_input);
        passwordLayoutSignIn = findViewById(R.id.sign_in_password_input_layout);
        Button signInButton = findViewById(R.id.sign_in_primary_btn);
        // get all the views for the sign up layout
        emailInputSignUp = findViewById(R.id.sign_up_email_input);
        emailLayoutSignUp = findViewById(R.id.sign_up_email_input_layout);
        usernameInputSignUp = findViewById(R.id.sign_up_username_input);
        usernameLayoutSignUp = findViewById(R.id.sign_up_username_input_layout);
        passwordInputSignUp = findViewById(R.id.sign_up_password_input);
        passwordLayoutSignUp = findViewById(R.id.sign_up_password_input_layout);
        passwordConfirmInputSignUp = findViewById(R.id.sign_up_password_confirm_input);
        passwordConfirmLayoutSignUp = findViewById(R.id.sign_up_password_confirm_input_layout);
        passwordAttributesTV = findViewById(R.id.sign_up_password_attributes_tv);
        Button signUpButton = findViewById(R.id.sign_up_primary_btn);

        signInButton.setOnClickListener(view -> {
            if (validSignInInput()) {
                attemptSignIn(usernameInputSignIn.getText().toString().trim(), passwordInputSignIn.getText().toString().trim());
            }
        });

        signUpButton.setOnClickListener(view -> {
            passwordAttributesTV.setVisibility(View.GONE); // wish I could do this after pswd loses focus, but can't get it to work

            if (validSignUpInput()) {
                if (emailInputSignUp.getText().toString().trim().contains("@gmail.com")) {
                    showGmailDetectedPopup();
                } else {
                    attemptSignUp(usernameInputSignUp.getText().toString().trim(),
                            passwordInputSignUp.getText().toString().trim(), emailInputSignUp.getText().toString().trim(), null);
                }
            }
        });

        Button switchToSignUp = findViewById(R.id.sign_in_change_mode_btn);
        Button switchToSignIn = findViewById(R.id.sign_up_change_mode_btn);
        switchToSignUp.setOnClickListener(view -> {
            // clear text before switching to the sign up page
            usernameInputSignIn.setText(null);
            passwordInputSignIn.setText(null);
            // erase any errors before switching to the sign up page
            usernameLayoutSignIn.setErrorEnabled(false);
            usernameLayoutSignIn.setError(null);
            passwordLayoutSignIn.setErrorEnabled(false);
            passwordLayoutSignIn.setError(null);

            viewFlipper.setInAnimation(this, R.anim.slide_in_right);
            viewFlipper.setOutAnimation(this, R.anim.slide_out_left);
            viewFlipper.setDisplayedChild(SIGN_UP_VIEW);
        });
        switchToSignIn.setOnClickListener(view -> {
            resetViewsToSignInFromSignUp();
            viewFlipper.setInAnimation(this, android.R.anim.slide_in_left);
            viewFlipper.setOutAnimation(this, android.R.anim.slide_out_right);
            viewFlipper.setDisplayedChild(SIGN_IN_VIEW);
        });

        TextView resetPasswordTV = findViewById(R.id.sign_in_forgot_password);
        resetPasswordTV.setOnClickListener(view -> {
            usernameInputSignIn.setText(null);
            passwordInputSignIn.setText(null);
            // erase any errors before switching to the sign up page
            usernameLayoutSignIn.setErrorEnabled(false);
            usernameLayoutSignIn.setError(null);
            passwordLayoutSignIn.setErrorEnabled(false);
            passwordLayoutSignIn.setError(null);
            resetPassword();
        });
        initEditTexts();
        if (getIntent().getExtras() != null) {
            String errorMessage = getIntent().getExtras().getString(Variables.ERROR_MESSAGE);
            if (errorMessage != null) {
                AndroidUtils.showErrorDialog("Error", errorMessage, this);
            }
        }
    }

    private void showGmailDetectedPopup() {
        View popupView = getLayoutInflater().inflate(R.layout.popup_gmail_detected, null);
        SignInButton signInButton = popupView.findViewById(R.id.google_sign_in_btn);
        signInButton.setOnClickListener(view -> {
            alertDialog.dismiss();
            googleSignIn();
        });
        alertDialog = new AlertDialog.Builder(this)
                .setTitle("Gmail Detected")
                .setView(popupView)
                .setPositiveButton("Send Code", null)
                .create();
        alertDialog.setOnShowListener(dialogInterface -> {
            Button sendCodeButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            sendCodeButton.setOnClickListener(view -> {
                alertDialog.dismiss();
                attemptSignUp(usernameInputSignUp.getText().toString().trim(),
                        passwordInputSignUp.getText().toString().trim(), emailInputSignUp.getText().toString().trim(), null);
            });
        });
        alertDialog.show();
    }

    private void googleSignIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN && resultCode == Activity.RESULT_OK) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGoogleSignInResult(task);
        }
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            if (account.getEmail().toLowerCase().equals(emailInputSignUp.getText().toString().toLowerCase().trim())) {
                attemptSignUp(usernameInputSignUp.getText().toString().trim(),
                        passwordInputSignUp.getText().toString().trim(), emailInputSignUp.getText().toString().trim(), account.getIdToken());
            } else {
                Toast.makeText(this, "The email you signed in with did not match the email you put for your new LiteWeight account.", Toast.LENGTH_LONG).show();
            }
            googleSignOut();
        } catch (ApiException e) {
            AndroidUtils.showErrorDialog("Error", "There was an error verifying your email.", this);
        }
    }

    private void googleSignOut() {
        // only using google sign in for sending data to cognito, so sign out after we get the user's id token
        googleSignInClient.revokeAccess()
                .addOnCompleteListener(this, task -> googleSignInClient.signOut());
    }

    @Override
    public void onBackPressed() {
        if (viewFlipper.getDisplayedChild() == SIGN_UP_VIEW) {
            resetViewsToSignInFromSignUp();
            viewFlipper.setInAnimation(this, android.R.anim.slide_in_left);
            viewFlipper.setOutAnimation(this, android.R.anim.slide_out_right);
            viewFlipper.setDisplayedChild(SIGN_IN_VIEW);
        } else {
            super.onBackPressed();
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
        // since user can sign in with username or email, can't restrict length by only username
        usernameInputSignIn.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_URL_LENGTH)});
        usernameInputSignIn.setOnKeyListener((View view, int keyCode, KeyEvent keyevent) -> {
            if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                String errorMsg = ValidatorUtils.validUsername(usernameInputSignIn.getText().toString().trim());
                if (errorMsg == null) {
                    usernameLayoutSignIn.setError(null);
                    return true;
                } else {
                    usernameLayoutSignIn.setError(errorMsg);
                }
            }
            return false;
        });
        usernameInputSignIn.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(usernameLayoutSignIn));

        passwordInputSignIn.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(passwordLayoutSignIn));
        passwordInputSignIn.setOnKeyListener((View view, int keyCode, KeyEvent keyevent) -> {
            if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                // on enter try to sign in if input is valid
                String errorMsg = ValidatorUtils.validPassword(passwordInputSignIn.getText().toString().trim());
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
                String errorMsg = ValidatorUtils.validNewEmail(emailInputSignUp.getText().toString().trim());
                if (errorMsg == null) {
                    emailLayoutSignUp.setError(null);
                    return true;
                } else {
                    emailLayoutSignUp.setError(errorMsg);
                }
            }
            return false;

        });
        emailInputSignUp.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(emailLayoutSignUp));

        usernameInputSignUp.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_USERNAME_LENGTH)});
        usernameInputSignUp.setOnKeyListener((View view, int keyCode, KeyEvent keyevent) -> {
            if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                // only show errors immediately when signing up
                String errorMsg = ValidatorUtils.validNewUsername(usernameInputSignUp.getText().toString().trim());
                if (errorMsg == null) {
                    usernameLayoutSignUp.setError(null);
                    return true;
                } else {
                    usernameLayoutSignUp.setError(errorMsg);
                }
            }
            return false;
        });
        usernameInputSignUp.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(usernameLayoutSignUp));

        passwordInputSignUp.setOnFocusChangeListener((View v, boolean hasFocus) -> {
            if (v.hasFocus()) {
                String errorMessage = ValidatorUtils.validNewPassword(passwordInputSignUp.getText().toString().trim());
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
                String errorMessage = ValidatorUtils.validNewPassword(s.toString().trim());
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
                // if all valid input, try to sign up after user hits enter button
                if (validSignUpInput()) {
                    hideKeyboard(getCurrentFocus());
                    if (emailInputSignUp.getText().toString().trim().contains("@gmail.com")) {
                        showGmailDetectedPopup();
                    } else {
                        attemptSignUp(usernameInputSignUp.getText().toString().trim(),
                                passwordInputSignUp.getText().toString().trim(), emailInputSignUp.getText().toString().trim(), null);
                    }
                }
                return true;
            }
            return false;
        });
    }

    private void resetViewsToSignInFromSignUp() {
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

    private boolean validSignInInput() {
        String usernameErrorMsg = ValidatorUtils.validUsername(usernameInputSignIn.getText().toString().trim());
        if (usernameErrorMsg != null) {
            usernameLayoutSignIn.setError(usernameErrorMsg);
            usernameLayoutSignIn.startAnimation(AndroidUtils.shakeError(2));
        }
        String passwordErrorMsg = ValidatorUtils.validPassword(passwordInputSignIn.getText().toString().trim());
        if (passwordErrorMsg != null) {
            passwordLayoutSignIn.setError(passwordErrorMsg);
            passwordLayoutSignIn.startAnimation(AndroidUtils.shakeError(2));
        }

        return (usernameErrorMsg == null) && (passwordErrorMsg == null);
    }

    private void attemptSignIn(String username, String password) {
        AndroidUtils.showLoadingDialog(loadingDialog, "Signing in...");
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ResultStatus<CognitoResponse> resultStatus = this.cognitoRepository.initiateAuth(username, password);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                if (resultStatus.isSuccess()) {
                    signInSuccess(resultStatus);
                } else {
                    loadingDialog.dismiss();
                    AndroidUtils.showErrorDialog("Error", resultStatus.getErrorMessage(), this);
                }
            });
        });
    }

    private void signInSuccess(ResultStatus<CognitoResponse> resultStatus) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Variables.REFRESH_TOKEN_KEY, resultStatus.getData().getRefreshToken());
        editor.putString(Variables.ID_TOKEN_KEY, resultStatus.getData().getIdToken());
        // update tokens singleton that the repositories will be using to connect to api gateway
        tokens.setIdToken(resultStatus.getData().getIdToken());
        tokens.setRefreshToken(resultStatus.getData().getRefreshToken());
        editor.apply();
        getUserWithWorkout();
    }

    private void getUserWithWorkout() {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ResultStatus<UserWithWorkout> resultStatus = this.userRepository.getUserAndCurrentWorkout();
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                loadingDialog.dismiss();
                if (resultStatus.isSuccess()) {
                    Globals.userWithWorkout = resultStatus.getData(); // turns out if you send a big object in an intent, it causes performance problems so instead get this fun hack :(
                    launchWorkoutActivity(resultStatus.getData());
                } else {
                    AndroidUtils.showErrorDialog("Error", resultStatus.getErrorMessage(), this);
                }
            });
        });
    }

    private void launchWorkoutActivity(UserWithWorkout userWithWorkout) {
        Intent intent = new Intent(SignInActivity.this, WorkoutActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        if (userWithWorkout != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }

    private void attemptSignUp(String username, String password, String email, String optionalIdToken) {
        AndroidUtils.showLoadingDialog(loadingDialog, "Signing up...");
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ResultStatus<Boolean> resultStatus = this.cognitoRepository.signUp(username, password, email, optionalIdToken);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                loadingDialog.dismiss();
                if (resultStatus.isSuccess() && !resultStatus.getData()) {
                    // user is not confirmed, so take them to the confirm email stage
                    promptConfirmEmailAddress();
                } else if (resultStatus.isSuccess() && resultStatus.getData()) {
                    // user was confirmed, so no need to ask for a confirmation code
                    attemptSignIn(usernameInputSignUp.getText().toString().trim(),
                            passwordInputSignUp.getText().toString().trim());
                } else {
                    AndroidUtils.showErrorDialog("Error", resultStatus.getErrorMessage(), this);
                }
            });
        });
    }

    private boolean validSignUpInput() {
        boolean validInput = true;
        String usernameErrorMsg = ValidatorUtils.validNewUsername(usernameInputSignUp.getText().toString().trim());
        if (usernameErrorMsg != null) {
            usernameLayoutSignUp.setError(usernameErrorMsg);
            usernameLayoutSignUp.startAnimation(AndroidUtils.shakeError(2));
            validInput = false;
        }
        String emailErrorMsg = ValidatorUtils.validNewEmail(emailInputSignUp.getText().toString().trim());
        if (emailErrorMsg != null) {
            emailLayoutSignUp.setError(emailErrorMsg);
            emailLayoutSignUp.startAnimation(AndroidUtils.shakeError(2));
            validInput = false;
        }
        String passwordErrorMsg = ValidatorUtils.validNewPassword(passwordInputSignUp.getText().toString().trim());
        if (passwordErrorMsg != null) {
            passwordLayoutSignUp.setError(passwordErrorMsg);
            passwordLayoutSignUp.startAnimation(AndroidUtils.shakeError(2));
            validInput = false;
        }
        String passwordConfirmErrorMsg = ValidatorUtils.validNewPassword(passwordInputSignUp.getText().toString().trim());
        if (passwordErrorMsg != null) {
            passwordConfirmLayoutSignUp.setError(passwordConfirmErrorMsg);
            passwordConfirmLayoutSignUp.startAnimation(AndroidUtils.shakeError(2));
            validInput = false;
        }
        // make sure that the passwords match assuming they are actually valid
        if (passwordErrorMsg == null && passwordConfirmErrorMsg == null &&
                !passwordInputSignUp.getText().toString().trim().equals(passwordConfirmInputSignUp.getText().toString().trim())) {
            passwordLayoutSignUp.setError(passwordNotMatchingMsg);
            passwordLayoutSignUp.startAnimation(AndroidUtils.shakeError(2));
            passwordConfirmLayoutSignUp.setError(passwordNotMatchingMsg);
            passwordConfirmLayoutSignUp.startAnimation(AndroidUtils.shakeError(2));
            validInput = false;
        }

        return validInput;
    }

    private void promptConfirmEmailAddress() {
        viewFlipper.setInAnimation(this, R.anim.slide_in_right);
        viewFlipper.setOutAnimation(this, R.anim.slide_out_left);
        viewFlipper.setDisplayedChild(CONFIRM_EMAIL_VIEW);
        EditText codeInput = findViewById(R.id.code_input);
        TextInputLayout codeLayout = findViewById(R.id.code_input_layout);
        viewFlipper.getOutAnimation().setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // request the keyboard to the code input after the animation is finished
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(codeInput, InputMethodManager.SHOW_IMPLICIT);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        codeInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(codeLayout));
        codeInput.setOnKeyListener((View view, int keyCode, KeyEvent keyevent) -> {
            if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                // if all valid input, try to confirm after user hits enter button
                if (codeInput.getText().toString().length() == Variables.EMAIL_CODE_LENGTH) {
                    confirmEmailAddress(codeInput.getText().toString().trim(), codeInput);
                } else {
                    codeLayout.setError("Please enter valid code.");
                    codeLayout.startAnimation(AndroidUtils.shakeError(2));
                }
                return true;
            }
            return false;
        });
        codeInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.EMAIL_CODE_LENGTH)});
        codeInput.requestFocus();

        Button confirmButton = findViewById(R.id.confirm_email_btn);
        confirmButton.setOnClickListener(view -> {
            if (codeInput.getText().toString().length() == Variables.EMAIL_CODE_LENGTH) {
                confirmEmailAddress(codeInput.getText().toString().trim(), codeInput);
            } else {
                codeLayout.setError("Please enter valid code.");
                codeLayout.startAnimation(AndroidUtils.shakeError(2));
            }
        });
        Button resendCodeButton = findViewById(R.id.resend_code_btn);
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
                        AndroidUtils.showErrorDialog("Error", resultStatus.getErrorMessage(), this);
                    }
                });
            });
        });
    }

    private void confirmEmailAddress(String code, EditText codeInput) {
        AndroidUtils.showLoadingDialog(loadingDialog, "Confirming...");
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ResultStatus<CognitoResponse> resultStatus = this.cognitoRepository.confirmSignUp(usernameInputSignUp.getText().toString(), code);
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
                        AndroidUtils.showErrorDialog("Error", resultStatus.getErrorMessage(), this);
                    } else {
                        AndroidUtils.showErrorDialog("Error", resultStatus.getErrorMessage(), this);
                        codeInput.setText(null);
                        viewFlipper.showPrevious();
                    }
                }
            });
        });
    }

    private void resetPassword() {
        viewFlipper.setInAnimation(this, R.anim.slide_in_right);
        viewFlipper.setOutAnimation(this, R.anim.slide_out_left);
        viewFlipper.setDisplayedChild(RESET_PASSWORD_VIEW);

        final boolean[] codeSent = {false};
        RelativeLayout usernameContainer = findViewById(R.id.reset_password_username_container);
        RelativeLayout passwordContainer = findViewById(R.id.reset_password_container);
        TextInputLayout usernameInputLayout = findViewById(R.id.reset_password_username_input_layout);
        EditText usernameInput = findViewById(R.id.reset_password_username_input);
        usernameInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(usernameInputLayout));
        Button primaryButton = findViewById(R.id.reset_password_primary_btn);
        Button backButton = findViewById(R.id.reset_password_back_btn);

        TextView resetPasswordAttributesTV = findViewById(R.id.reset_password_password_attributes_tv);
        TextInputLayout resetCodeLayout = findViewById(R.id.reset_password_code_input_layout);
        EditText resetCode = findViewById(R.id.reset_password_code_input);
        resetCode.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(resetCodeLayout));
        resetCode.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.EMAIL_CODE_LENGTH)});

        TextInputLayout newPasswordLayout = findViewById(R.id.reset_password_password_input_layout);
        EditText newPasswordInput = findViewById(R.id.reset_password_password_input);
        TextInputLayout confirmNewPasswordLayout = findViewById(R.id.reset_password_confirm_password_input_layout);
        EditText confirmNewPasswordInput = findViewById(R.id.reset_password_confirm_password_input);
        newPasswordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String errorMessage = ValidatorUtils.validNewPassword(s.toString().trim());
                if (errorMessage == null) {
                    // no error so hide attributes
                    resetPasswordAttributesTV.setVisibility(View.GONE);
                } else {
                    resetPasswordAttributesTV.setText(errorMessage);
                    resetPasswordAttributesTV.setVisibility(View.VISIBLE);
                }

                if (newPasswordLayout.isErrorEnabled()) {
                    // if an error is present, stop showing the error message once the user types (acknowledged it)
                    newPasswordLayout.setErrorEnabled(false);
                    newPasswordLayout.setError(null);
                }
                if (confirmNewPasswordLayout.isErrorEnabled() && confirmNewPasswordLayout.getError().equals(passwordNotMatchingMsg)) {
                    // if the passwords weren't matching, hide the error on confirmPassword edittext since user just acknowledged the error
                    confirmNewPasswordLayout.setErrorEnabled(false);
                    confirmNewPasswordLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        confirmNewPasswordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (confirmNewPasswordLayout.isErrorEnabled()) {
                    // if an error is present, stop showing the error message once the user types (acknowledged it)
                    confirmNewPasswordLayout.setErrorEnabled(false);
                    confirmNewPasswordLayout.setError(null);
                }
                if (newPasswordLayout.isErrorEnabled() && newPasswordLayout.getError().equals(passwordNotMatchingMsg)) {
                    // if the passwords weren't matching, hide the error on confirmPassword edittext since user just acknowledged the error
                    newPasswordLayout.setErrorEnabled(false);
                    newPasswordLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        primaryButton.setOnClickListener(view -> {
            if (codeSent[0]) {
                // code has already been sent, so this button should send the new password to cognito
                resetPasswordAttributesTV.setText(null);
                resetPasswordAttributesTV.setVisibility(View.GONE);
                String confirmationCode = resetCode.getText().toString().trim();
                String newPassword = newPasswordInput.getText().toString().trim();
                String newPasswordConfirm = confirmNewPasswordInput.getText().toString().trim();

                String passwordError = ValidatorUtils.validNewPassword(newPassword);
                String passwordConfirmError = ValidatorUtils.validNewPassword(newPasswordConfirm);
                String confirmError = (confirmationCode.length() == Variables.EMAIL_CODE_LENGTH) ? null : "Enter a valid code.";
                if (passwordError == null && confirmError == null &&
                        passwordConfirmError == null && newPassword.equals(newPasswordConfirm)) {
                    // all input is valid, so reset the password then sign user in
                    AndroidUtils.showLoadingDialog(loadingDialog, "Resetting password...");
                    Executor executor = Executors.newSingleThreadExecutor();
                    executor.execute(() -> {
                        ResultStatus<Boolean> resultStatus = this.cognitoRepository.confirmForgotPassword(usernameInput.getText().toString().trim(), newPassword, confirmationCode);
                        Handler handler = new Handler(getMainLooper());
                        handler.post(() -> {
                            loadingDialog.dismiss();
                            if (resultStatus.isSuccess()) {
                                attemptSignIn(usernameInput.getText().toString().trim(), newPassword);
                            } else {
                                AndroidUtils.showErrorDialog("Error", resultStatus.getErrorMessage(), this);
                            }
                        });
                    });
                } else {
                    // at least on input error exists
                    if (passwordError != null) {
                        newPasswordLayout.setError(passwordError);
                        newPasswordLayout.startAnimation(AndroidUtils.shakeError(2));
                    }
                    if (passwordConfirmError != null) {
                        confirmNewPasswordLayout.setError(passwordConfirmError);
                        confirmNewPasswordLayout.startAnimation(AndroidUtils.shakeError(2));
                    }
                    if (confirmError != null) {
                        resetCodeLayout.setError(confirmError);
                        resetCodeLayout.startAnimation(AndroidUtils.shakeError(2));
                    }

                    if (passwordError == null && passwordConfirmError == null &&
                            !newPassword.equals(newPasswordConfirm)) {
                        newPasswordLayout.setError(passwordNotMatchingMsg);
                        newPasswordLayout.startAnimation(AndroidUtils.shakeError(2));
                        confirmNewPasswordLayout.setError(passwordNotMatchingMsg);
                        confirmNewPasswordLayout.startAnimation(AndroidUtils.shakeError(2));
                    }
                }

            } else {
                // code hasn't been sent, so this button should send a code to the right email when clicked
                if (!usernameInput.getText().toString().trim().isEmpty()) {
                    AndroidUtils.showLoadingDialog(loadingDialog, "Sending code...");
                    Executor executor = Executors.newSingleThreadExecutor();
                    executor.execute(() -> {
                        ResultStatus<Boolean> resultStatus = this.cognitoRepository.forgotPassword(usernameInput.getText().toString().trim());
                        Handler handler = new Handler(getMainLooper());
                        handler.post(() -> {
                            loadingDialog.dismiss();
                            if (resultStatus.isSuccess()) {
                                codeSent[0] = true;
                                primaryButton.setText(R.string.reset_password_btn_msg);
                                passwordContainer.setVisibility(View.VISIBLE);
                                usernameContainer.setVisibility(View.GONE);
                            } else {
                                AndroidUtils.showErrorDialog("Error", resultStatus.getErrorMessage(), this);
                            }
                        });
                    });
                } else {
                    usernameInputLayout.setError("Cannot be empty");
                    usernameInputLayout.startAnimation(AndroidUtils.shakeError(2));
                }
            }
        });
        backButton.setOnClickListener(view -> {
            viewFlipper.setInAnimation(this, android.R.anim.slide_in_left);
            viewFlipper.setOutAnimation(this, android.R.anim.slide_out_right);
            // reset any text and errors that might be there
            resetCodeLayout.setError(null);
            resetCodeLayout.setErrorEnabled(false);
            newPasswordLayout.setError(null);
            newPasswordLayout.setErrorEnabled(false);
            confirmNewPasswordLayout.setError(null);
            confirmNewPasswordLayout.setErrorEnabled(false);
            usernameInputLayout.setError(null);
            usernameInputLayout.setErrorEnabled(false);

            resetCode.setText(null);
            newPasswordInput.setText(null);
            confirmNewPasswordInput.setText(null);
            usernameInput.setText(null);
            resetPasswordAttributesTV.setText(null);
            passwordAttributesTV.setVisibility(View.GONE);

            passwordContainer.setVisibility(View.GONE);
            usernameContainer.setVisibility(View.VISIBLE);
            viewFlipper.setDisplayedChild(SIGN_IN_VIEW);
        });
    }

    private void hideKeyboard(View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
