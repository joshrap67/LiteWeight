package com.joshrap.liteweight.widgets;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.joshrap.liteweight.imports.Globals;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.services.StopwatchService;

import java.util.Locale;

public class Stopwatch {
    private Button startStopwatch, stopStopwatch, resetStopwatch, showTimerButton;
    private boolean stopwatchRunning, showTimer;
    private final long timeUnit = 1000;
    private long startTimeAbsolute, initialTimeOnClock, displayTime; // in SI units of milliseconds
    private TextView stopwatchDisplay;
    private ConstraintLayout timerContainer, stopwatchContainer;
    private Activity activity;
    private SharedPreferences pref;
    private final Handler stopwatchHandler = new Handler();
    private final Runnable stopwatch = new Runnable() {
        @Override
        public void run() {
            long elapsedTime = System.currentTimeMillis() - startTimeAbsolute;
            displayTime = initialTimeOnClock + elapsedTime;
            if (displayTime > Variables.MAX_STOPWATCH_TIME) {
                stopwatchRunning = false;
                initialTimeOnClock = 0;
                displayTime = 0;
                stopwatchFinishedVisibility();
                resetStopwatch();
                stopwatchHandler.removeCallbacks(stopwatch);
            } else {
                updateStopwatchDisplay(displayTime);
                stopwatchHandler.postDelayed(stopwatch, 500);
            }
        }
    };

    public Stopwatch(Activity _activity) {
        activity = _activity;
        stopwatchRunning = false;
        pref = activity.getApplicationContext().getSharedPreferences(Variables.SHARED_PREF_SETTINGS, 0);
        initialTimeOnClock = 0; // assume at initialization the stopwatch isn't running
        displayTime = 0;
    }

    public void initStopwatchUI(View stopwatchView, Activity _activity, boolean timerVisible) {
        // this is called whenever the current workout fragment loads. It sets up the UI for the stopwatch to update
        activity = _activity;
        startStopwatch = stopwatchView.findViewById(R.id.start_stopwatch);
        stopStopwatch = stopwatchView.findViewById(R.id.stop_stopwatch);
        resetStopwatch = stopwatchView.findViewById(R.id.reset_stopwatch);
        showTimerButton = stopwatchView.findViewById(R.id.show_timer);
        stopwatchDisplay = stopwatchView.findViewById(R.id.stopwatch);
        timerContainer = stopwatchView.findViewById(R.id.timer_container);
        stopwatchContainer = stopwatchView.findViewById(R.id.stopwatch_container);
        showTimer = timerVisible;

        cancelService();
        initStopwatch();
    }

    private void initStopwatch() {
        if (stopwatchRunning) {
            stopwatchRunningVisibility();
        } else {
            stopwatchFinishedVisibility();
        }
        startStopwatch.setOnClickListener(v -> {
            stopwatchRunningVisibility();
            startStopwatch();
        });
        stopStopwatch.setOnClickListener(v -> {
            stopwatchFinishedVisibility();
            stopStopwatch();
        });
        resetStopwatch.setOnClickListener(v -> resetStopwatch());
        showTimerButton.setOnClickListener(v -> showTimer());
        updateStopwatchDisplay(displayTime);
    }

    private void startStopwatch() {
        if (!stopwatchRunning) {
            startTimeAbsolute = System.currentTimeMillis();
            stopwatchHandler.post(stopwatch);
            stopwatchRunning = true;
        }
    }

    public void stopStopwatch() {
        if (stopwatchRunning) {
            long elapsedTime = System.currentTimeMillis() - startTimeAbsolute;
            initialTimeOnClock += elapsedTime;
            startTimeAbsolute = System.currentTimeMillis();
            stopwatchHandler.removeCallbacks(stopwatch);
            stopwatchRunning = false;
        }
    }

    private void resetStopwatch() {
        initialTimeOnClock = 0;
        startTimeAbsolute = System.currentTimeMillis();
        if (stopwatchRunning) {
            stopwatchRunning = false;
            stopwatchHandler.removeCallbacks(stopwatch);
            startStopwatch();
        } else {
            // just a static reset of the textview since the stopwatch isn't actually running
            displayTime = 0;
            updateStopwatchDisplay(0);
        }
    }

    private void updateStopwatchDisplay(long elapsedTime) {
        int minutes = (int) (elapsedTime / (60 * timeUnit));
        int seconds = (int) (elapsedTime / timeUnit) % 60;
        String timeFormatted = String.format(Locale.getDefault(),
                "%02d:%02d", minutes, seconds);
        stopwatchDisplay.setText(timeFormatted);
    }

    private void stopwatchFinishedVisibility() {
        stopStopwatch.setVisibility(View.GONE);
        startStopwatch.setVisibility(View.VISIBLE);
        showTimerButton.setVisibility((showTimer) ? View.VISIBLE : View.GONE);
    }

    private void stopwatchRunningVisibility() {
        stopStopwatch.setVisibility(View.VISIBLE);
        startStopwatch.setVisibility(View.GONE);
        showTimerButton.setVisibility(View.GONE);
    }

    private void showTimer() {
        /*
            User has indicated that they want the timer, so hide the stopwatch and show timer
         */
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(Variables.LAST_CLOCK_MODE, Variables.TIMER);
        editor.apply();
        timerContainer.setVisibility(View.VISIBLE);
        stopwatchContainer.setVisibility(View.GONE);
    }

    public void startService() {
        /*
            Stopwatch is going out of the visible screen, so start a service to maintain its progress.
         */
        Intent serviceIntent = new Intent(activity, StopwatchService.class);
        serviceIntent.putExtra(Variables.INTENT_TIMER_ABSOLUTE_START_TIME, startTimeAbsolute);
        serviceIntent.putExtra(Variables.INTENT_TIMER_TIME_ON_CLOCK, initialTimeOnClock);
        activity.startService(serviceIntent);
        Globals.stopwatchServiceRunning = true;
    }

    public void cancelService() {
        activity.stopService(new Intent(activity, StopwatchService.class));
        Globals.stopwatchServiceRunning = false;
        // get rid of any notifications that are still showing now that the stopwatch is on the screen
        NotificationManager notificationManager = (NotificationManager) activity.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(StopwatchService.stopwatchRunningId);
    }

    public boolean isStopwatchRunning() {
        return stopwatchRunning;
    }
}
