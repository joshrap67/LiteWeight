package com.joshrap.liteweight.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.joshrap.liteweight.imports.Variables;

public class NotificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent notificationIntent = getIntent();
        String action = notificationIntent.getAction();
        if (notificationIntent.getExtras() != null && action != null) {
            String jsonData = notificationIntent.getExtras().getString(Variables.INTENT_NOTIFICATION_DATA);
            if (isTaskRoot()) {
                /*
                    This might not be the most elegant solution, but this will always be true when opening
                    the app from a notification.
                    Means that the notification has likely been clicked when app is destroyed since
                    there should always be one activity running otherwise in the app lifecycle.
                 */
                launchSplashActivity(jsonData, action);
            } else {
                // workout activity is already running. Send data to it instead of making new activity
                broadcastToWorkoutActivity(jsonData, action);
            }
        } else {
            // this shouldn't ever be reached if i do my job properly. But putting it here as a fail safe
            launchSplashActivity("", "");
        }
        finish();
    }

    private void launchSplashActivity(String jsonData, String action) {
        Intent intent = new Intent(this, SplashActivity.class);
        intent.putExtra(Variables.INTENT_NOTIFICATION_DATA, jsonData);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setAction(action);
        startActivity(intent);
    }

    private void broadcastToWorkoutActivity(String jsonData, String action) {
        Intent intent = new Intent(this, WorkoutActivity.class);
        intent.putExtra(Variables.INTENT_NOTIFICATION_DATA, jsonData);
        intent.setAction(action);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.sendBroadcast(intent);
    }
}
