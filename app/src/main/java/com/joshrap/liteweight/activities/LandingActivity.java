package com.joshrap.liteweight.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.injection.Injector;

public class LandingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        Injector.getInjector(this).inject(this);

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            launchSignInActivity();
            return;
        }

        if (currentUser.isEmailVerified()) {
            launchMainActivity();
            return;
        }

        currentUser.reload().addOnCompleteListener(task -> {
            // reload only happens once every hour or so. if the user is not verified, manually reload in case they clicked to verify their email
            if (task.isSuccessful()) {
                if (currentUser.isEmailVerified()) {
                    launchMainActivity();
                } else {
                    launchUnverifiedActivity();
                }
            } else {
                // todo error message?
                launchSignInActivity();
            }
        });
    }

    private void launchSignInActivity() {
        Intent intent = new Intent(this, SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void launchUnverifiedActivity() {
        Intent intent = new Intent(this, UnverifiedActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void launchMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}


