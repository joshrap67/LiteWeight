package com.joshrap.liteweight.activities;


import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.adapters.RoutineAdapter;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.models.CognitoResponse;
import com.joshrap.liteweight.models.ExerciseRoutine;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.network.CognitoGateway;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO do the check here for tokens and what not?
        // TODO new setting: "Auto Track Exercise Updates" -> whether changes in currentworkout auto change for exercise defaults
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Variables.SHARED_PREF_SETTINGS, 0);
        String refreshToken = pref.getString(Variables.REFRESH_TOKEN_KEY, null);
        String idToken = pref.getString(Variables.ID_TOKEN_KEY, null);
        refreshTokens(refreshToken);
        if (refreshToken == null) {
            launchSignInActivity();
        } else {
            refreshTokens(refreshToken);
        }
        // TODO logged in key?
        // TODO try and get the user object if tokens exist. If idToken fails then try to initiate auth again using refresh to get new one
    }

    private void refreshTokens(String refreshToken) {
        if (refreshToken == null) {
            return;
        }
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            System.out.println("refreshing...");
            ResultStatus<CognitoResponse> resultStatus = CognitoGateway.refreshTokens(refreshToken);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {

                if (resultStatus.isSuccess()) {
                    System.out.println("****************REFRESH SUCCEEDED *****************");
                    System.out.println(resultStatus.getData());
                    SharedPreferences pref = getApplicationContext().getSharedPreferences(Variables.SHARED_PREF_SETTINGS, 0);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString(Variables.ID_TOKEN_KEY, resultStatus.getData().getIdToken());
                    editor.apply();
                } else {
                    System.out.println("****************REFRESH FAILED *****************");

                }
                launchSignInActivity();
            });
        });
    }

    private void launchSignInActivity() {
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent, options.toBundle());
        finish();
    }

}


