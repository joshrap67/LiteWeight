package com.joshrap.liteweight.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.joshrap.liteweight.R;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.models.Tokens;

import javax.inject.Inject;

public class LandingActivity extends AppCompatActivity {

    @Inject
    public Tokens tokens;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        Injector.getInjector(this).inject(this);

        if (tokens.getRefreshToken() == null || tokens.getIdToken() == null) {
            // no tokens exist, so user is not logged in
            launchSignInActivity();
        } else {
            // user is logged in, attempt to fetch their info
            launchWorkoutActivity();
        }
    }

    private void launchSignInActivity() {
        Intent intent = new Intent(this, SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void launchWorkoutActivity() {
        Intent intent = new Intent(this, WorkoutActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}


