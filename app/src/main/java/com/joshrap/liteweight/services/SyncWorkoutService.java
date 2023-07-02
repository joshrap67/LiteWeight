package com.joshrap.liteweight.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.managers.WorkoutManager;
import com.joshrap.liteweight.models.workout.Workout;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

public class SyncWorkoutService extends Service {
    @Inject
    WorkoutManager workoutManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Injector.getInjector(getBaseContext()).inject(this);
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        String workoutJson = intent.getStringExtra(Variables.INTENT_WORKOUT);
        int currentWeek = intent.getIntExtra(Variables.INTENT_CURRENT_WEEK, 0);
        int currentDay = intent.getIntExtra(Variables.INTENT_CURRENT_DAY, 0);
        Workout workout = null;
        try {
            workout = new Workout(new ObjectMapper().readValue(workoutJson, Workout.class));
        } catch (JsonProcessingException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        Executor executor = Executors.newSingleThreadExecutor();
        Workout finalWorkout = workout;
        executor.execute(() -> {
            if (finalWorkout != null) {
                workoutManager.updateWorkoutProgress(currentWeek, currentDay, finalWorkout);
                Handler handler = new Handler(getMainLooper());
                handler.post(this::stopSelf);
            } else {
                stopSelf();
            }
        });
        return START_STICKY;
    }
}
