package com.joshrap.liteweight.activities;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

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

public class SplashActivity extends AppCompatActivity {

    /*
        If a token key value pair exists in shared prefs
            If valid then immediately go to WorkoutActivity
        Else prompt for sign in again
     */

    private String notificationData;
    private String notificationAction;
    @Inject
    public Tokens tokens;
    @Inject
    UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getExtras() != null && getIntent().getAction() != null) {
            notificationAction = getIntent().getAction();
            notificationData = getIntent().getExtras().getString(Variables.INTENT_NOTIFICATION_DATA);
        }
        Injector.getInjector(this).inject(this);
        if (tokens.getRefreshToken() == null || tokens.getIdToken() == null) {
            launchSignInActivity();
        } else {
            getUser();
        }
    }

    private void getUser() {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ResultStatus<UserWithWorkout> resultStatus = this.userRepository.getUserAndCurrentWorkout();
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                if (resultStatus.isSuccess()) {
                    Globals.user = resultStatus.getData().getUser();
                    Globals.activeWorkout = resultStatus.getData().getWorkout();
                    launchWorkoutActivity();
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

    private void launchWorkoutActivity() {
        Intent intent = new Intent(this, WorkoutActivity.class);
        if (notificationData != null && notificationAction != null) {
            intent.setAction(notificationAction);
            intent.putExtra(Variables.INTENT_NOTIFICATION_DATA, notificationData);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

}


