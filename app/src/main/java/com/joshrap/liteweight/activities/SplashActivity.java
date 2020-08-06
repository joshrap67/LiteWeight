package com.joshrap.liteweight.activities;


import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.joshrap.liteweight.imports.Globals;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.models.User;
import com.joshrap.liteweight.network.repos.UserRepository;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

public class SplashActivity extends AppCompatActivity {

    @Inject
    public SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.getInjector(this).inject(this);
        sharedPreferences = getApplicationContext().getSharedPreferences(Variables.SHARED_PREF_SETTINGS, 0);
        // TODO guest mode
        Globals.refreshToken = sharedPreferences.getString(Variables.REFRESH_TOKEN_KEY, null);
        Globals.idToken = sharedPreferences.getString(Variables.ID_TOKEN_KEY, null);

        if (Globals.refreshToken == null || Globals.idToken == null) {
            launchSignInActivity();
        } else {
            getUser();
        }
    }

    private void getUser() {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            System.out.println("Getting user...");
            ResultStatus<User> resultStatus = UserRepository.getUser(null);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                if (resultStatus.isSuccess()) {
                    System.out.println("**************** USER GET SUCCEEDED *****************");
                    Globals.user = resultStatus.getData();
                    launchWorkoutActivity();
                } else {
                    System.out.println("**************** USER GET FAILED *****************");
                    System.out.println(resultStatus.getErrorMessage());
                }
            });
        });
    }

    private void launchSignInActivity() {
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this);
        Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent, options.toBundle());
        finish();
    }

    private void launchWorkoutActivity() {
        Intent intent = new Intent(this, WorkoutActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

}


