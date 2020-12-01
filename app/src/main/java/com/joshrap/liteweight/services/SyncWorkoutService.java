package com.joshrap.liteweight.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.models.Tokens;
import com.joshrap.liteweight.models.Workout;
import com.joshrap.liteweight.network.ApiGateway;
import com.joshrap.liteweight.network.CognitoGateway;
import com.joshrap.liteweight.network.RequestFields;
import com.joshrap.liteweight.network.repos.CognitoRepository;
import com.joshrap.liteweight.network.repos.WorkoutRepository;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SyncWorkoutService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        String refreshToken = intent.getStringExtra(Variables.INTENT_REFRESH_TOKEN);
        String idToken = intent.getStringExtra(Variables.INTENT_ID_TOKEN);
        String workoutJson = intent.getStringExtra(RequestFields.WORKOUT);
        Workout workout = null;
        try {
            workout = new Workout(new ObjectMapper().readValue(workoutJson, Map.class));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        ApiGateway apiGateway = new ApiGateway(new Tokens(refreshToken, idToken), new CognitoRepository(new CognitoGateway()));
        WorkoutRepository repository = new WorkoutRepository(apiGateway);
        Executor executor = Executors.newSingleThreadExecutor();
        Workout finalWorkout = workout;
        executor.execute(() -> {
            if (finalWorkout != null) {
                ResultStatus<String> resultStatus = repository.syncWorkout(finalWorkout);
                Handler handler = new Handler(getMainLooper());
                handler.post(() -> {
                    if (resultStatus.isSuccess()) {
                        System.out.println("**************** SYNC SUCCEEDED *****************");
                    } else {
                        System.out.println("**************** SYNC FAILED *****************");
                    }
                    stopSelf();
                });
            } else {
                stopSelf();
            }
        });
        return START_STICKY;
    }
}
