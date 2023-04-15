package com.joshrap.liteweight.widgets;

import android.os.Handler;

import androidx.lifecycle.MutableLiveData;

import com.joshrap.liteweight.imports.Variables;

public class Stopwatch {

    public final MutableLiveData<Boolean> stopwatchRunning;
    public static final long timeUnit = 1000; // in SI units of milliseconds
    public long startTimeAbsolute; // in SI units of milliseconds (UNIX Timestamp)
    public long initialElapsedTime; // in SI units of milliseconds. utilized when pausing the stopwatch to keep the latest time on the clock
    public final MutableLiveData<Long> elapsedTime; // in SI units of milliseconds

    private final Handler stopwatchHandler = new Handler();
    private final Runnable stopwatch = new Runnable() {
        @Override
        public void run() {
            long elapsedTimeAbsolute = System.currentTimeMillis() - startTimeAbsolute;
            elapsedTime.setValue(initialElapsedTime + elapsedTimeAbsolute);
            if (elapsedTime.getValue() > Variables.MAX_STOPWATCH_TIME) {
                cancelRunnable();
                resetStopwatch();
            } else {
                stopwatchHandler.postDelayed(stopwatch, 500);
            }
        }
    };

    public Stopwatch() {
        stopwatchRunning = new MutableLiveData<>(false);
        initialElapsedTime = 0; // assume at initialization the stopwatch isn't running
        elapsedTime = new MutableLiveData<>(0L);
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
            initialElapsedTime += elapsedTime;
            cancelRunnable();
        }
    }

    public void resetStopwatch() {
        initialElapsedTime = 0;
        if (isStopwatchRunning()) {
            cancelRunnable();
            startStopwatch();
        } else {
            elapsedTime.setValue(0L);
        }
    }

    private void cancelRunnable() {
        stopwatchRunning.setValue(false);
        stopwatchHandler.removeCallbacks(stopwatch);
    }

    public boolean isStopwatchRunning() {
        return stopwatchRunning.getValue();
    }
}
