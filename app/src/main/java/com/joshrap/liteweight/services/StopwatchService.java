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

import com.joshrap.liteweight.activities.WorkoutActivity;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.R;

import java.util.Locale;
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
    public void onCreate() {
        startForeground(stopwatchRunningId, stopwatchRunningNotification("Stopwatch starting..."));
        super.onCreate();
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        startTimeAbsolute = intent.getLongExtra(Variables.INTENT_TIMER_ABSOLUTE_START_TIME, 0);
        initialElapsedTime = intent.getLongExtra(Variables.INTENT_STOPWATCH_INITIAL_ELAPSED_TIME, 0);

        stopwatch = new Timer();
        stopwatch.scheduleAtFixedRate(new TimerTask() {
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
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(stopwatchRunningId);
        stopwatch.cancel();
        super.onDestroy();
    }

    /**
     * Is called by the stopwatch. Formats a long to a string to then display it in a notification
     *
     * @param aTime time to be displayed on the notification
     */
    private void updateStopwatchRunningNotificationMessage(long aTime) {
        int minutes = (int) (aTime / 60000);
        int seconds = (int) (aTime / 1000) % 60;
        String timeRemaining = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

        Notification notification = stopwatchRunningNotification(timeRemaining);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(stopwatchRunningId, notification);
    }

    /**
     * As long as the stopwatch is running in the background, show a notification
     *
     * @param content formatted time to be displayed.
     * @return Notification to be displayed on the status bar.
     */
    private Notification stopwatchRunningNotification(String content) {
        Intent notificationIntent = new Intent(this, WorkoutActivity.class);
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
        Intent notificationIntent = new Intent(this, WorkoutActivity.class);
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
