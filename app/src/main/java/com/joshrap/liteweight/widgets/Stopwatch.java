package com.joshrap.liteweight.widgets;

import android.os.Handler;

import androidx.lifecycle.MutableLiveData;

import com.joshrap.liteweight.imports.Variables;

public class Stopwatch {
    private boolean stopwatchRunning;
    public static final long timeUnit = 1000; // in SI units of milliseconds
    public long startTimeAbsolute, initialTimeOnClock; // in SI units of milliseconds
    public MutableLiveData<Long> displayTime; // in SI units of milliseconds

    private final Handler stopwatchHandler = new Handler();
    private final Runnable stopwatch = new Runnable() {
        @Override
        public void run() {
            long elapsedTime = System.currentTimeMillis() - startTimeAbsolute;
            displayTime.setValue(initialTimeOnClock + elapsedTime);
            if (displayTime.getValue() > Variables.MAX_STOPWATCH_TIME) {
                stopwatchRunning = false;
                displayTime.setValue(0L);
                resetStopwatch();
                stopwatchHandler.removeCallbacks(stopwatch);
            } else {
                stopwatchHandler.postDelayed(stopwatch, 500);
            }
        }
    };

    public Stopwatch() {
        stopwatchRunning = false;
        initialTimeOnClock = 0; // assume at initialization the stopwatch isn't running
        displayTime = new MutableLiveData<>();
        displayTime.setValue(0L);
    }

    public void startStopwatch() {
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

    public void resetStopwatch() {
        initialTimeOnClock = 0;
        startTimeAbsolute = System.currentTimeMillis();
        if (stopwatchRunning) {
            stopwatchRunning = false;
            stopwatchHandler.removeCallbacks(stopwatch);
            startStopwatch();
        } else {
            displayTime.setValue(0L);
        }
    }

    public boolean isStopwatchRunning() {
        return stopwatchRunning;
    }
}
