package com.joshrap.liteweight.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.imports.BackendConfig;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.utils.AndroidUtils;

import javax.inject.Inject;

public class SignInActivity extends AppCompatActivity {

    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth auth;
    private boolean shouldFinish;

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
        auth = FirebaseAuth.getInstance();

        setContentView(R.layout.activity_sign_in_layout);

        Button signInButton = findViewById(R.id.sign_in_with_email_btn);
        Button signUpButton = findViewById(R.id.sign_up_with_email_btn);
        Button googleSignInButton = findViewById(R.id.google_sign_in_btn);
        googleSignInButton.setOnClickListener(view -> googleSignIn());

        signUpButton.setOnClickListener(view -> launchSignUp());
        signInButton.setOnClickListener(view -> launchSignInWithEmail());

        if (getIntent().getExtras() != null) {
            String errorMessage = getIntent().getExtras().getString(Variables.INTENT_ERROR_MESSAGE);
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
            auth.signInWithCredential(firebaseCredential).addOnCompleteListener(this, task -> {
                loadingDialog.dismiss();
                googleSignOut();
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

    private void googleSignOut() {
        // only using google sign in for getting id token to link to firebase. Can immediately log out once getting that token
        googleSignInClient.signOut();
    }

    private void launchMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        shouldFinish = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (shouldFinish) {
            finish();
        }
    }

    private void launchSignUp() {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }

    private void launchSignInWithEmail() {
        Intent intent = new Intent(this, SignInWithEmailActivity.class);
        startActivity(intent);
    }
}
