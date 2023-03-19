package com.joshrap.liteweight.widgets;

import android.os.Handler;

import androidx.lifecycle.MutableLiveData;

public class Timer {

    public final MutableLiveData<Boolean> timerRunning;
    public static final long timeUnit = 1000; // SI units of milliseconds
    public long startTimeAbsolute, timerDuration; // in SI units of milliseconds
    public long initialTimeRemaining; // in SI units of milliseconds. utilized when pausing the timer to keep the latest time on the clock
    public final MutableLiveData<Long> timeRemaining; // in SI units of milliseconds

    private final Handler timerHandler = new Handler();
    private final Runnable timer = new Runnable() {
        @Override
        public void run() {
            long elapsedTimeAbsolute = System.currentTimeMillis() - startTimeAbsolute;
            timeRemaining.setValue(initialTimeRemaining - elapsedTimeAbsolute + timeUnit - 1); // don't want the timer to start counting down from duration-1 but rather duration
            if (timeRemaining.getValue() <= timeUnit - 1) {
                cancelRunnable();
                resetTimer();
            } else {
                timerHandler.postDelayed(timer, 500);
            }
        }
    };

    public Timer(long _timerDuration) {
        timerRunning = new MutableLiveData<>(false);
        timerDuration = _timerDuration;
        initialTimeRemaining = timerDuration; // assume at initialization the timer isn't running
        timeRemaining = new MutableLiveData<>(initialTimeRemaining);
    }

    public void startTimer() {
        if (!isTimerRunning()) {
            startTimeAbsolute = System.currentTimeMillis();
            timerHandler.post(timer);
            timerRunning.setValue(true);
        }
    }

    public void stopTimer() {
        if (isTimerRunning()) {
            long elapsedTime = System.currentTimeMillis() - startTimeAbsolute;
            initialTimeRemaining -= elapsedTime;
            cancelRunnable();
        }
    }

    public void resetTimer() {
        initialTimeRemaining = timerDuration;
        if (isTimerRunning()) {
            cancelRunnable();
            startTimer();
        } else {
            timeRemaining.setValue(initialTimeRemaining);
        }
    }

    private void cancelRunnable() {
        timerRunning.setValue(false);
        timerHandler.removeCallbacks(timer);
    }

    public boolean isTimerRunning() {
        return timerRunning.getValue();
    }
}
