package com.joshrap.liteweight.activities;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.joshrap.liteweight.imports.Globals;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.models.UserWithWorkout;
import com.joshrap.liteweight.network.repos.UserRepository;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

public class SplashActivity extends AppCompatActivity {

    /*
        1. If a token key value pair exists in shared prefs
            If valid then immediately go to WorkoutActivity
            else prompt for sign in again
        2. If no token then check if guest mode is active in shared prefs
            If guest mode is active then immediately go to the WorkoutActivity
        3. Else the app has never been used before
           Prompt for signin/signup
     */

    @Inject
    public SharedPreferences sharedPreferences;
    @Inject
    UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.getInjector(this).inject(this);
        String refreshToken = sharedPreferences.getString(Variables.REFRESH_TOKEN_KEY, null);
        String idToken = sharedPreferences.getString(Variables.ID_TOKEN_KEY, null);
        if (refreshToken == null || idToken == null) {
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
                    System.out.println("**************** USER GET SUCCEEDED *****************");
                    Globals.user = resultStatus.getData().getUser();
                    Globals.activeWorkout = resultStatus.getData().getWorkout();
                    launchWorkoutActivity();
                } else {
                    System.out.println("**************** USER GET FAILED *****************");
                    System.out.println(resultStatus.getErrorMessage());
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
        // todo when launching to the workout activity, check if the user had clicked on a notification
        Intent intent = new Intent(this, WorkoutActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

}


