package com.joshrap.liteweight.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.network.repos.UserRepository;

public class SyncRoutineService extends Service {
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
        UserRepository.getUser("testing");
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        // get rid of the stopwatch running notification whenever the service is killed

        super.onDestroy();
    }
}
