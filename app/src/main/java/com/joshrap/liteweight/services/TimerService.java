package com.joshrap.liteweight.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.activities.WorkoutActivity;
import com.joshrap.liteweight.R;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class TimerService extends Service {
    /*
        This service once created only is there for showing the timer progress. It currently does not
        need to constantly communicate to any activities listening. In the future buttons could be provided in the
        notification to stop/reset the timer.
     */
    public static final int timerRunningId = 1;
    public static final int timerFinishedId = 2;

    private long startTimeAbsolute, initialTimeOnClock; // in SI units of milliseconds
    private Timer timer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        startForeground(timerRunningId, timerRunningNotification("Timer starting..."));
        super.onCreate();
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        startTimeAbsolute = intent.getLongExtra(Variables.INTENT_TIMER_ABSOLUTE_START_TIME, 0);
        initialTimeOnClock = intent.getLongExtra(Variables.INTENT_TIMER_TIME_ON_CLOCK, 0);

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long elapsedTime = System.currentTimeMillis() - startTimeAbsolute;
                long timeRemaining = initialTimeOnClock - elapsedTime;
                if (timeRemaining <= 0) {
                    timer.cancel();
                    stopSelf();
                    showTimerFinishedNotification();
                } else {
                    // timer still has time left to go
                    updateTimerRunningNotificationMessage(timeRemaining);
                }
            }
        }, 0, 500);
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        // get rid of the timer running notification whenever the service is killed
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(timerRunningId);
        timer.cancel();
        super.onDestroy();
    }

    private void updateTimerRunningNotificationMessage(long aTime) {
        /*
            Is called by the timer. Formats a long to a string and then displays it in a notification
         */
        int minutes = (int) (aTime / 60000);
        int seconds = (int) (aTime / 1000) % 60;
        String timeRemaining = String.format(Locale.getDefault(),
                "%02d:%02d", minutes, seconds);

        Notification notification = timerRunningNotification(timeRemaining);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(timerRunningId, notification);
    }

    private Notification timerRunningNotification(String content) {
        /*
            As long as the timer is running in the background, show a notification
         */
        Intent notificationIntent = new Intent(this, WorkoutActivity.class);
        notificationIntent.setAction(Variables.INTENT_TIMER_NOTIFICATION_CLICK);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        return new NotificationCompat.Builder(this, Variables.TIMER_RUNNING_CHANNEL)
                .setContentTitle("Timer")
                .setContentText(content)
                .setSmallIcon(R.drawable.notification_icon)
                .setSound(null)
                .setContentIntent(contentIntent)
                .setOnlyAlertOnce(true) // only the first notification sent has a sound
                .build();
    }

    private void showTimerFinishedNotification() {
        /*
            Once the timer limit has been reached, show a one time notification (is no longer a foreground service at this point)
         */
        Intent notificationIntent = new Intent(this, WorkoutActivity.class);
        notificationIntent.setAction(Variables.INTENT_TIMER_NOTIFICATION_CLICK);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, Variables.TIMER_FINISHED_CHANNEL)
                .setContentTitle("Timer")
                .setContentText("Timer finished!")
                .setSmallIcon(R.drawable.notification_icon)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setOngoing(false)
                .setOnlyAlertOnce(true) // only the first notification sent has a sound
                .build();
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(timerFinishedId, notification);
    }
}
