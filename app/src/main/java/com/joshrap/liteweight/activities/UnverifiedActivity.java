package com.joshrap.liteweight.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.joshrap.liteweight.BuildConfig;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.utils.AndroidUtils;

import javax.inject.Inject;

public class UnverifiedActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    @Inject
    AlertDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.getInjector(this).inject(this);

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            finish();
            return;
        }

        setContentView(R.layout.activity_unverified);

        Button logoutButton = findViewById(R.id.log_out_btn);
        Button retryVerificationButton = findViewById(R.id.retry_verification_btn);
        Button resendEmailVerificationButton = findViewById(R.id.send_verification_email_btn);
        TextView signedInAs = findViewById(R.id.signed_in_as_tv);
        signedInAs.setText(String.format("%s %s", getString(R.string.signed_in_as), user.getEmail()));

        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            launchSignInActivity();
        });

        resendEmailVerificationButton.setOnClickListener(v -> {
            ActionCodeSettings actionCodeSettings = ActionCodeSettings.newBuilder()
                    // URL you want to redirect back to. The domain (www.example.com) for this
                    // URL must be whitelisted in the Firebase Console.
                    .setUrl("https://www.google.com") // todo a site
                    .setHandleCodeInApp(true)
                    .setAndroidPackageName(BuildConfig.APPLICATION_ID, true, "14")
                    .build();
            user.sendEmailVerification().addOnCompleteListener(task -> Toast.makeText(this, "Email sent successfully", Toast.LENGTH_LONG).show());
        });

        retryVerificationButton.setOnClickListener(v -> {
            AndroidUtils.showLoadingDialog(loadingDialog, "Signing In...");
            user.reload().addOnCompleteListener(task -> {
                loadingDialog.dismiss();
                if (auth.getCurrentUser().isEmailVerified()) {
                    launchMainActivity();
                } else {
                    Toast.makeText(UnverifiedActivity.this, "Email still not verified", Toast.LENGTH_SHORT).show();
                }
            });
        });

        if (getIntent().getExtras() != null) {
            String errorMessage = getIntent().getExtras().getString(Variables.ERROR_MESSAGE);
            if (errorMessage != null) {
                AndroidUtils.showErrorDialog(errorMessage, this);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            user.reload().addOnCompleteListener(task -> {
                if (auth.getCurrentUser().isEmailVerified()) {
                    launchMainActivity();
                }
            });
        }
    }

    private void launchMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void launchSignInActivity() {
        Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent);
        finish();
    }
}
