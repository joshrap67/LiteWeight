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
            if (isTaskRoot()) {
                /*
                    This might not be the most elegant solution, but this will always be true when opening
                    the app from a notification.
                    Means that the notification has likely been clicked when app is destroyed since
                    there should always be one activity running otherwise in the app lifecycle.
                 */
                launchSplashActivity(getBundleForAction(action));
            } else {
                broadcastToWorkoutActivity(getBundleForAction(action), action);
            }
        } else {
            // this shouldn't ever be reached if i do my job properly. But putting it here as a fail safe
            launchSplashActivity(getBundleForAction("f"));
        }
        finish();
    }

    private Bundle getBundleForAction(String action) {
        // todo add data to this method signature
        Bundle retVal = new Bundle();
        if (action.equals(Variables.INTENT_FRIEND_REQUEST_CLICK)) {
            retVal.putString(Variables.INTENT_FRIEND_REQUEST_DATA, "fuck");
        }

        return retVal;
    }

    private void launchSplashActivity(Bundle bundle) {
        // todo need to coordinate passing the data correctly
        Intent intent = new Intent(this, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void broadcastToWorkoutActivity(Bundle bundle, String action) {
        Intent intent = new Intent(this, WorkoutActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtras(bundle);
        intent.setAction(action);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.sendBroadcast(intent);
    }
}
