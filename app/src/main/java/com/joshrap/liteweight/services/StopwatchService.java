package com.joshrap.liteweight.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.joshrap.liteweight.activities.WorkoutActivity;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.R;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This service once created only is there for showing the stopwatch progress. It currently does not
 * need to constantly communicate to any activities listening. In the future buttons could be provided in the
 * notification to stop/reset the stopwatch.
 */
public class StopwatchService extends Service {

    public static final int stopwatchRunningId = 3;

    private long startTimeAbsolute, initialTimeOnClock; // in SI units of milliseconds
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
        initialTimeOnClock = intent.getLongExtra(Variables.INTENT_TIMER_TIME_ON_CLOCK, 0);

        stopwatch = new Timer();
        stopwatch.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long elapsedTime = System.currentTimeMillis() - startTimeAbsolute;
                long timeRemaining = initialTimeOnClock + elapsedTime;
                if (timeRemaining > Variables.MAX_STOPWATCH_TIME) {
                    stopwatch.cancel();
                    stopSelf();
                    showStopwatchFinishedNotification();
                } else {
                    // stopwatch still has time left to go
                    updateStopwatchRunningNotificationMessage(timeRemaining);
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
        String timeRemaining = String.format(Locale.getDefault(),
                "%02d:%02d", minutes, seconds);

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
        notificationIntent.setAction(Variables.NOTIFICATION_CLICKED);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.putExtra(Variables.NOTIFICATION_ACTION, Variables.INTENT_STOPWATCH_NOTIFICATION_CLICK);
        // don't actually need to send data as of now, but putting dummy data in order to not have specific branches in notification activity
        notificationIntent.putExtra(Variables.INTENT_NOTIFICATION_DATA, "Clicky-Doo");
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                Variables.STOPWATCH_RUNNING_REQUEST_CODE, notificationIntent, 0);
        return new NotificationCompat.Builder(this, Variables.STOPWATCH_RUNNING_CHANNEL)
                .setContentTitle("Stopwatch")
                .setContentText(content)
                .setSmallIcon(R.drawable.notification_icon)
                .setSound(null)
                .setContentIntent(contentIntent)
                .setOnlyAlertOnce(true) // only the first notification sent has a sound
                .build();
    }

    /**
     * This shouldn't ever really happen, but if the stopwatch max limit is reached then show a notification
     */
    private void showStopwatchFinishedNotification() {
        Intent notificationIntent = new Intent(this, WorkoutActivity.class);
        notificationIntent.setAction(Variables.NOTIFICATION_CLICKED);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.putExtra(Variables.NOTIFICATION_ACTION, Variables.INTENT_STOPWATCH_NOTIFICATION_CLICK);
        // don't actually need to send data as of now, but putting dummy data in order to not have specific branches in notification activity
        notificationIntent.putExtra(Variables.INTENT_NOTIFICATION_DATA, "Clicky-Doo");
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                Variables.STOPWATCH_FINISHED_REQUEST_CODE, notificationIntent, 0);
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
