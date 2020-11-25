package com.joshrap.liteweight.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.joshrap.liteweight.helpers.JsonParser;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.models.Tokens;
import com.joshrap.liteweight.models.UserWithWorkout;
import com.joshrap.liteweight.network.repos.UserRepository;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

public class SplashActivity extends AppCompatActivity {

    private String notificationData;
    private String notificationAction;
    @Inject
    public Tokens tokens;
    @Inject
    UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.getInjector(this).inject(this);
        if (getIntent().getExtras() != null && getIntent().getAction() != null) {
            notificationAction = getIntent().getAction();
            notificationData = getIntent().getExtras().getString(Variables.INTENT_NOTIFICATION_DATA);
        }
        if (tokens.getRefreshToken() == null || tokens.getIdToken() == null) {
            // no tokens exist, so user is not logged in
            launchSignInActivity();
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
                    try {
                        launchWorkoutActivity(resultStatus.getData());
                    } catch (JsonProcessingException e) {
                        launchSignInActivity();
                    }
                } else {
                    launchSignInActivity();
                }
            });
        });
    }

    private void launchSignInActivity() {
        Intent intent = new Intent(this, SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void launchWorkoutActivity(UserWithWorkout userWithWorkout) throws JsonProcessingException {
        Intent intent = new Intent(this, WorkoutActivity.class);
        if (notificationData != null && notificationAction != null) {
            intent.setAction(notificationAction);
            intent.putExtra(Variables.INTENT_NOTIFICATION_DATA, notificationData);
        }
        if (userWithWorkout != null) {
            intent.putExtra(Variables.USER_WITH_WORKOUT_DATA, JsonParser.serializeMap(userWithWorkout.asMap()));
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {
            // really should never happen, so just launch sign in activity.
            launchSignInActivity();
        }

    }

}


