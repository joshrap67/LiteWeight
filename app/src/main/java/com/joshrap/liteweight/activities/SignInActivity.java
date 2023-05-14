package com.joshrap.liteweight.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.imports.BackendConfig;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.utils.AndroidUtils;
import com.joshrap.liteweight.utils.ValidatorUtils;

import javax.inject.Inject;

public class SignInActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private TextInputLayout emailInputLayout, passwordInputLayout;
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth mAuth;

    @Inject
    AlertDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.getInjector(this).inject(this);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(BackendConfig.googleSignInClientId)
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();

        setContentView(R.layout.activity_sign_in_layout);

        emailInput = findViewById(R.id.email_input);
        emailInputLayout = findViewById(R.id.email_input_layout);
        passwordInput = findViewById(R.id.password_input);
        passwordInputLayout = findViewById(R.id.password_input_layout);

        Button signInButton = findViewById(R.id.sign_in_btn);
        Button signUpButton = findViewById(R.id.sign_up_btn);
        SignInButton googleSignInButton = findViewById(R.id.google_sign_in_btn);
        googleSignInButton.setOnClickListener(view -> googleSignIn());

        signInButton.setOnClickListener(view -> {
            if (validSignInInput()) {
                attemptSignIn(emailInput.getText().toString().trim(), passwordInput.getText().toString().trim());
            }
        });

        signUpButton.setOnClickListener(view -> launchSignUp());

        TextView resetPasswordTV = findViewById(R.id.forgot_password_tv);
        resetPasswordTV.setOnClickListener(view -> launchResetPassword());

        initEditTexts();
        if (getIntent().getExtras() != null) {
            String errorMessage = getIntent().getExtras().getString(Variables.ERROR_MESSAGE);
            if (errorMessage != null) {
                AndroidUtils.showErrorDialog(errorMessage, this);
            }
        }
    }

    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result == null)
                    return;

                if (result.getResultCode() == Activity.RESULT_OK) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    handleGoogleSignInResult(task);
                }
            });

    private void googleSignIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String idToken = account.getIdToken();
            AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);

            AndroidUtils.showLoadingDialog(loadingDialog, "Signing in...");
            mAuth.signInWithCredential(firebaseCredential).addOnCompleteListener(this, task -> {
                loadingDialog.dismiss();
                if (task.isSuccessful()) {
                    launchMainActivity();
                } else {
                    AndroidUtils.showErrorDialog("There was an error signing in with Google.", SignInActivity.this);
                }
            });
        } catch (ApiException e) {
            AndroidUtils.showErrorDialog("There was an error signing in with Google.", this);
        }
    }

    private void initEditTexts() {
        // since user can sign in with username or email, can't restrict length by only username
        emailInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_URL_LENGTH)});
        emailInput.setOnKeyListener((View view, int keyCode, KeyEvent keyevent) -> {
            if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                String errorMsg = ValidatorUtils.validUsername(emailInput.getText().toString().trim());
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
                        hideKeyboard(getCurrentFocus());
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
        String usernameErrorMsg = ValidatorUtils.validUsername(emailInput.getText().toString().trim());
        if (usernameErrorMsg != null) {
            emailInputLayout.setError(usernameErrorMsg);
            emailInputLayout.startAnimation(AndroidUtils.shakeError(2));
        }
        String passwordErrorMsg = ValidatorUtils.validPassword(passwordInput.getText().toString().trim());
        if (passwordErrorMsg != null) {
            passwordInputLayout.setError(passwordErrorMsg);
            passwordInputLayout.startAnimation(AndroidUtils.shakeError(2));
        }

        return (usernameErrorMsg == null) && (passwordErrorMsg == null);
    }

    private void attemptSignIn(String email, String password) {
        AndroidUtils.showLoadingDialog(loadingDialog, "Signing in...");
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            loadingDialog.dismiss();
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null && user.isEmailVerified()) {
                    launchMainActivity();
                } else if (user != null && !user.isEmailVerified()) {
                    launchUnverifiedActivity();
                }
            } else {
                AndroidUtils.showErrorDialog("Authentication failed", getApplicationContext());
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

    private void launchSignUp() {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }

    private void launchUnverifiedActivity() {
        Intent intent = new Intent(this, UnverifiedActivity.class);
        startActivity(intent);
        finish();
    }

    private void launchResetPassword() {
        Intent intent = new Intent(this, ResetPasswordActivity.class);
        startActivity(intent);
    }

    private void hideKeyboard(View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
