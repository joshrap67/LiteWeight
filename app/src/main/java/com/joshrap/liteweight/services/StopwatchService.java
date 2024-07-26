package com.joshrap.liteweight.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.joshrap.liteweight.activities.MainActivity;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.utils.TimeUtils;

import java.util.Timer;
import java.util.TimerTask;

public class StopwatchService extends Service {

    public static final int stopwatchRunningId = 3;

    private long startTimeAbsolute, initialElapsedTime; // in SI units of milliseconds
    private Timer stopwatch;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        startForeground(stopwatchRunningId, stopwatchRunningNotification("Stopwatch starting..."));
        startTimeAbsolute = intent.getLongExtra(Variables.INTENT_ABSOLUTE_START_TIME, 0);
        initialElapsedTime = intent.getLongExtra(Variables.INTENT_STOPWATCH_INITIAL_ELAPSED_TIME, 0);

        stopwatch = new Timer();
        stopwatch.schedule(new TimerTask() {
            @Override
            public void run() {
                long elapsedTimeAbsolute = System.currentTimeMillis() - startTimeAbsolute;
                long elapsedTime = initialElapsedTime + elapsedTimeAbsolute;
                if (elapsedTime > Variables.MAX_STOPWATCH_TIME) {
                    stopwatch.cancel();
                    stopSelf();
                    showStopwatchFinishedNotification();
                } else {
                    // stopwatch still has time left to go
                    updateStopwatchRunningNotificationMessage(elapsedTime);
                }
            }
        }, 0, 500);
        return START_REDELIVER_INTENT;
    }

    /**
     * Get rid of the stopwatch running notification whenever the service is killed
     */
    @Override
    public void onDestroy() {
        // Get rid of the stopwatch running notification whenever the service is killed
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(stopwatchRunningId);
        stopwatch.cancel();
        super.onDestroy();
    }

    private void updateStopwatchRunningNotificationMessage(long time) {
        String timeRemaining = TimeUtils.getClockDisplay(time);
        Notification notification = stopwatchRunningNotification(timeRemaining);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(stopwatchRunningId, notification);
    }

    private Notification stopwatchRunningNotification(String content) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.putExtra(Variables.NOTIFICATION_ACTION, Variables.INTENT_STOPWATCH_NOTIFICATION_CLICK);

        PendingIntent contentIntent = PendingIntent.getActivity(this,
                Variables.STOPWATCH_RUNNING_REQUEST_CODE, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // weird bug on android 12 if this isn't used sometimes the notification is delayed
            return new Notification.Builder(this, Variables.STOPWATCH_RUNNING_CHANNEL)
                    .setContentTitle("Stopwatch")
                    .setContentText(content)
                    .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
                    .setSmallIcon(R.drawable.notification_icon)
                    .setContentIntent(contentIntent)
                    .setOnlyAlertOnce(true) // only the first notification sent has a sound
                    .build();
        } else {
            return new NotificationCompat.Builder(this, Variables.STOPWATCH_RUNNING_CHANNEL)
                    .setContentTitle("Stopwatch")
                    .setContentText(content)
                    .setSmallIcon(R.drawable.notification_icon)
                    .setSound(null)
                    .setContentIntent(contentIntent)
                    .setOnlyAlertOnce(true) // only the first notification sent has a sound
                    .build();
        }
    }

    /**
     * This shouldn't ever really happen, but if the stopwatch max limit is reached then show a notification
     */
    private void showStopwatchFinishedNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.putExtra(Variables.NOTIFICATION_ACTION, Variables.INTENT_STOPWATCH_NOTIFICATION_CLICK);

        PendingIntent contentIntent = PendingIntent.getActivity(this,
                Variables.STOPWATCH_FINISHED_REQUEST_CODE, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new NotificationCompat.Builder(this, Variables.STOPWATCH_RUNNING_CHANNEL)
                .setContentTitle("Stopwatch")
                .setContentText("Stopwatch limit reached.")
                .setSmallIcon(R.drawable.notification_icon)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setOngoing(false)
                .setOnlyAlertOnce(true) // only the first notification sent has a sound
                .build();
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(stopwatchRunningId, notification);
    }
}
