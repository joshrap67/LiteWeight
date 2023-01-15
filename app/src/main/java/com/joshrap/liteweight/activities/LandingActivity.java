package com.joshrap.liteweight.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.joshrap.liteweight.R;
import com.joshrap.liteweight.imports.Globals;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.models.Tokens;
import com.joshrap.liteweight.models.UserWithWorkout;
import com.joshrap.liteweight.network.repos.UserRepository;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

public class LandingActivity extends AppCompatActivity {

    private String notificationData;
    private String notificationAction;

    @Inject
    public Tokens tokens;
    @Inject
    UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        Injector.getInjector(this).inject(this);
        if (getIntent().getExtras() != null && getIntent().getAction() != null) {
            notificationAction = getIntent().getAction();
            notificationData = getIntent().getExtras().getString(Variables.INTENT_NOTIFICATION_DATA);
        }

        if (tokens.getRefreshToken() == null || tokens.getIdToken() == null) {
            // no tokens exist, so user is not logged in
            launchSignInActivity(null);
        } else {
            // user is logged in, attempt to fetch their info
            getUserWithWorkout();
        }
    }

    private void getUserWithWorkout() {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ResultStatus<UserWithWorkout> resultStatus = this.userRepository.getUserAndCurrentWorkout();
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                if (resultStatus.isSuccess()) {
                    Globals.userWithWorkout = resultStatus.getData(); // turns out if you send a big object in an intent, it causes performance problems so instead get this fun hack :(
                    launchWorkoutActivity(resultStatus.getData());
                } else {
                    launchSignInActivity(resultStatus.getErrorMessage());
                }
            });
        });
    }

    private void launchSignInActivity(String errorMessage) {
        Intent intent = new Intent(this, SignInActivity.class);
        if (errorMessage != null) {
            intent.putExtra(Variables.ERROR_MESSAGE, errorMessage);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void launchWorkoutActivity(UserWithWorkout userWithWorkout) {
        Intent intent = new Intent(this, WorkoutActivity.class);
        if (notificationData != null && notificationAction != null) {
            intent.setAction(notificationAction);
            intent.putExtra(Variables.INTENT_NOTIFICATION_DATA, notificationData);
        }
        if (userWithWorkout != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {
            // really should never happen, so just launch sign in activity.
            String errorMessage = "There was a problem trying to load your data.";
            Toast.makeText(this, "There was a problem loading your data.", Toast.LENGTH_SHORT).show();
            launchSignInActivity(errorMessage);
        }
    }
}


