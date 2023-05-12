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
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        Injector.getInjector(this).inject(this);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            launchSignInActivity();
        } else if (currentUser.isEmailVerified()) {
            launchMainActivity();
        } else {
            launchUnverifiedActivity();
        }
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


