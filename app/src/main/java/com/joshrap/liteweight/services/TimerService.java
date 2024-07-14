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
import com.joshrap.liteweight.messages.activitymessages.TimerRestartMessage;
import com.joshrap.liteweight.utils.TimeUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.Timer;
import java.util.TimerTask;

public class TimerService extends Service {

    public static final int timerRunningId = 1;
    public static final int timerFinishedId = 2;
    public static final String resetExtra = "reset";

    private long startTimeAbsolute, initialTimeRemaining, timerDuration; // in SI units of milliseconds
    private Timer timer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        startForeground(timerRunningId, timerRunningNotification("Timer starting..."));
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(timerFinishedId);

        initialTimeRemaining = intent.getLongExtra(Variables.INTENT_TIMER_INITIAL_TIME_REMAINING, 0);
        timerDuration = intent.getLongExtra(Variables.INTENT_TIMER_DURATION, 0);
        if (intent.hasExtra(resetExtra)) {
            // if restarting from notification we always start the timer from the beginning
            startTimeAbsolute = System.currentTimeMillis();
            initialTimeRemaining = timerDuration;

            // broadcast to workout activity so that it knows it needs to start its timer again
            EventBus.getDefault().post(new TimerRestartMessage(startTimeAbsolute, initialTimeRemaining));
        } else {
            startTimeAbsolute = intent.getLongExtra(Variables.INTENT_ABSOLUTE_START_TIME, 0);
        }

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                long elapsedTime = System.currentTimeMillis() - startTimeAbsolute;
                long timeRemaining = initialTimeRemaining - elapsedTime;
                if (timeRemaining <= 0) {
                    timer.cancel();
                    stopSelf();
                    showTimerFinishedNotification(timerDuration); // if we kill the initial service, we have to persist the timer duration for a potential restart
                } else {
                    // timer still has time left to go
                    updateTimerRunningNotificationMessage(timeRemaining + 999); // don't want the timer to start counting down from duration-1 but rather duration
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
        if (timer != null) {
            timer.cancel();
        }

        super.onDestroy();
    }

    private void updateTimerRunningNotificationMessage(long time) {
        String timeRemaining = TimeUtils.getClockDisplay(time);
        Notification notification = timerRunningNotification(timeRemaining);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(timerRunningId, notification);
    }

    private Notification timerRunningNotification(String content) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        notificationIntent.putExtra(Variables.NOTIFICATION_ACTION, Variables.INTENT_TIMER_NOTIFICATION_CLICK);
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                Variables.TIMER_RUNNING_REQUEST_CODE, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // weird bug on android 12 if this isn't used sometimes the notification is delayed
            return new Notification.Builder(this, Variables.TIMER_RUNNING_CHANNEL)
                    .setContentTitle("Timer")
                    .setContentText(content)
                    .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
                    .setSmallIcon(R.drawable.notification_icon)
                    .setContentIntent(contentIntent)
                    .setOngoing(true)
                    .setOnlyAlertOnce(true) // only the first notification sent has a sound
                    .build();
        } else {
            return new NotificationCompat.Builder(this, Variables.TIMER_RUNNING_CHANNEL)
                    .setContentTitle("Timer")
                    .setContentText(content)
                    .setSmallIcon(R.drawable.notification_icon)
                    .setSound(null)
                    .setContentIntent(contentIntent)
                    .setOnlyAlertOnce(true) // only the first notification sent has a sound
                    .build();
        }
    }

    /**
     * Once the timer limit has been reached, show a one time notification (is no longer a foreground service at this point)
     */
    private void showTimerFinishedNotification(long timerDuration) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.putExtra(Variables.NOTIFICATION_ACTION, Variables.INTENT_TIMER_NOTIFICATION_CLICK);
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                Variables.TIMER_FINISHED_REQUEST_CODE, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent restartIntent = new Intent(this, TimerService.class);
        restartIntent.putExtra(resetExtra, true);
        restartIntent.putExtra(Variables.INTENT_TIMER_DURATION, timerDuration);
        PendingIntent pendingRestartIntent = PendingIntent.getService(this, 0, restartIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, Variables.TIMER_FINISHED_CHANNEL)
                .setContentTitle("Timer")
                .setContentText("Timer finished!")
                .setSmallIcon(R.drawable.notification_icon)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setOngoing(false)
                .addAction(R.drawable.restart_icon, "Restart Timer", pendingRestartIntent)
                .setOnlyAlertOnce(true) // only the first notification sent has a sound
                .build();
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(timerFinishedId, notification);
    }
}
