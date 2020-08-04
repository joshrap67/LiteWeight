package com.joshrap.liteweight.activities;


import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.joshrap.liteweight.imports.Globals;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.models.CognitoResponse;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.models.User;
import com.joshrap.liteweight.network.CognitoGateway;
import com.joshrap.liteweight.network.repos.UserRepository;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO new setting: "Auto Track Exercise Updates" -> whether changes in currentworkout auto change for exercise defaults
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Variables.SHARED_PREF_SETTINGS, 0);
        Globals.refreshToken = pref.getString(Variables.REFRESH_TOKEN_KEY, null);
        Globals.idToken = pref.getString(Variables.ID_TOKEN_KEY, null);

        if(Globals.refreshToken == null || Globals.idToken ==null){
            launchSignInActivity();
        } else{
            getUser();
        }
    }

    private void getUser(){
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            System.out.println("Getting user...");
            ResultStatus<User> resultStatus = UserRepository.getUser(null);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                if (resultStatus.isSuccess()) {
                    System.out.println("**************** USER GET SUCCEEDED *****************");
                    System.out.println(resultStatus.getData());
                    Globals.user = resultStatus.getData();
                    launchWorkoutActivity();
                } else {
                    System.out.println("**************** USER GET FAILED *****************");
                    System.out.println(resultStatus.getErrorMessage());
                }
            });
        });
    }

    private void refreshIdToken(String refreshToken) {
        if (refreshToken == null) {
            return;
        }
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            System.out.println("refreshing...");
            ResultStatus<CognitoResponse> resultStatus = CognitoGateway.refreshIdToken(refreshToken);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                if (resultStatus.isSuccess()) {
                    System.out.println("**************** REFRESH SUCCEEDED *****************");
                    System.out.println(resultStatus.getData());
                    SharedPreferences pref = getApplicationContext().getSharedPreferences(Variables.SHARED_PREF_SETTINGS, 0);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString(Variables.ID_TOKEN_KEY, resultStatus.getData().getIdToken());
                    editor.apply();
                } else {
                    System.out.println("**************** REFRESH FAILED *****************");
                }
                launchSignInActivity();
            });
        });
    }

    private void launchSignInActivity() {
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this);
        Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent, options.toBundle());
        finish();
    }

    private void launchWorkoutActivity(){
//        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this);
        Intent intent = new Intent(this, WorkoutActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        startActivity(intent, options.toBundle());
        startActivity(intent);

        finish();
    }

}


