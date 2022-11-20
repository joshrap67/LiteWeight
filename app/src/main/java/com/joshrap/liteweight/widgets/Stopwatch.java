package com.joshrap.liteweight.widgets;

import android.os.Handler;

import androidx.lifecycle.MutableLiveData;

import com.joshrap.liteweight.imports.Variables;

public class Stopwatch {
    public final MutableLiveData<Boolean> stopwatchRunning;
    public static final long timeUnit = 1000; // in SI units of milliseconds
    public long startTimeAbsolute, initialTimeOnClock; // in SI units of milliseconds
    public final MutableLiveData<Long> displayTime; // in SI units of milliseconds

    private final Handler stopwatchHandler = new Handler();
    private final Runnable stopwatch = new Runnable() {
        @Override
        public void run() {
            long elapsedTime = System.currentTimeMillis() - startTimeAbsolute;
            displayTime.setValue(initialTimeOnClock + elapsedTime);
            if (displayTime.getValue() > Variables.MAX_STOPWATCH_TIME) {
                stopwatchRunning.setValue(false);
                displayTime.setValue(0L);
                resetStopwatch();
                stopwatchHandler.removeCallbacks(stopwatch);
            } else {
                stopwatchHandler.postDelayed(stopwatch, 500);
            }
        }
    };

    public Stopwatch() {
        stopwatchRunning = new MutableLiveData<>(false);
        initialTimeOnClock = 0; // assume at initialization the stopwatch isn't running
        displayTime = new MutableLiveData<>(0L);
    }

    public void startStopwatch() {
        if (!isStopwatchRunning()) {
            startTimeAbsolute = System.currentTimeMillis();
            stopwatchHandler.post(stopwatch);
            stopwatchRunning.setValue(true);
        }
    }

    public void stopStopwatch() {
        if (isStopwatchRunning()) {
            long elapsedTime = System.currentTimeMillis() - startTimeAbsolute;
            initialTimeOnClock += elapsedTime;
            startTimeAbsolute = System.currentTimeMillis();
            stopwatchHandler.removeCallbacks(stopwatch);
            stopwatchRunning.setValue(false);
        }
    }

    public void resetStopwatch() {
        initialTimeOnClock = 0;
        startTimeAbsolute = System.currentTimeMillis();
        if (isStopwatchRunning()) {
            stopwatchRunning.setValue(false);
            stopwatchHandler.removeCallbacks(stopwatch);
            startStopwatch();
        } else {
            displayTime.setValue(0L);
        }
    }

    public boolean isStopwatchRunning() {
        return stopwatchRunning.getValue();
    }
}
