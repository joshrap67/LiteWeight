package com.joshrap.liteweight.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.joshrap.liteweight.BuildConfig;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.imports.BackendConfig;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;

public class LandingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        if (BuildConfig.FLAVOR.equalsIgnoreCase("sandbox")) {
            //http://10.0.2.2:5174/ for localhost. then remember to allow plaintext in android manifest
            BackendConfig.googleSignInClientId = "929931641278-88gnp9fmll3d8h2r557shbv0cij1crk9.apps.googleusercontent.com";
            BackendConfig.profilePictureBaseUrl = "https://storage.googleapis.com/liteweight-sandbox-profile-pictures/";
            BackendConfig.baseUrl = "https://liteweightapi-929931641278.us-central1.run.app/";
        } else if (BuildConfig.FLAVOR.equalsIgnoreCase("prod")) {
            BackendConfig.googleSignInClientId = "990471046455-g5s0mqhm6sm3b66ki7fle6n2ud0msim1.apps.googleusercontent.com";
            BackendConfig.profilePictureBaseUrl = "https://storage.googleapis.com/liteweight-profile-pictures/";
            BackendConfig.baseUrl = "https://liteweightapi-bxtag6fcfa-uc.a.run.app/";
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();
        Injector.getInjector(this).inject(this);

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            launchSignInActivity(null);
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
                launchSignInActivity("There was a problem with your account. Please try and login again.");
            }
        });
    }

    private void launchSignInActivity(String errorMessage) {
        Intent intent = new Intent(this, SignInActivity.class);
        if (errorMessage != null) {
            intent.putExtra(Variables.INTENT_ERROR_MESSAGE, errorMessage);
        }
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


