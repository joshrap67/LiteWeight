package com.joshrap.liteweight.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.ClearCredentialStateRequest;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.imports.BackendConfig;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.utils.AndroidUtils;

import java.util.concurrent.Executors;

import javax.inject.Inject;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;

public class SignInActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private boolean shouldFinish;
    private CredentialManager credentialManager;

    @Inject
    AlertDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.getInjector(this).inject(this);
        credentialManager = CredentialManager.create(this);
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

    private void googleSignIn() {
        GetSignInWithGoogleOption googleIdOption = new GetSignInWithGoogleOption(BackendConfig.googleSignInClientId, null, null);

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();
        credentialManager.getCredentialAsync(this,
                request,
                new CancellationSignal(),
                Executors.newSingleThreadExecutor(),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.getCredential().getData());
                        runOnUiThread(() -> handleGoogleSignInResult(googleIdTokenCredential));
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException exception) {
                        runOnUiThread(() -> AndroidUtils.showErrorDialog("There was an error signing in with Google.", getBaseContext()));
                    }
                }
        );
    }

    private void handleGoogleSignInResult(GoogleIdTokenCredential googleIdTokenCredential) {
        try {
            String idToken = googleIdTokenCredential.getIdToken();
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
        } catch (Exception e) {
            AndroidUtils.showErrorDialog("There was an error signing in with Google.", getApplicationContext());
        }
    }

    private void googleSignOut() {
        // only using google sign in for getting id token to link to firebase. Can immediately log out once getting that token
        Continuation<Unit> continuation = new Continuation<Unit>() {
            @NonNull
            @Override
            public CoroutineContext getContext() {
                return EmptyCoroutineContext.INSTANCE;
            }

            @Override
            public void resumeWith(@NonNull Object o) {

            }
        };
        credentialManager.clearCredentialState(new ClearCredentialStateRequest(), continuation);
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
