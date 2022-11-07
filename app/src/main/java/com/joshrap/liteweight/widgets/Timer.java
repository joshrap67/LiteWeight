package com.joshrap.liteweight.widgets;

import android.os.Handler;

import androidx.lifecycle.MutableLiveData;

public class Timer {
    public MutableLiveData<Boolean> timerRunning;
    public static final long timeUnit = 1000; // in SI units of milliseconds
    public long startTimeAbsolute, initialTimeOnClock, timerDuration; // in SI units of milliseconds
    public MutableLiveData<Long> displayTime; // in SI units of milliseconds

    private final Handler timerHandler = new Handler();
    private final Runnable timer = new Runnable() {
        @Override
        public void run() {
            long elapsedTime = System.currentTimeMillis() - startTimeAbsolute;
            displayTime.setValue(initialTimeOnClock - elapsedTime);
            if (displayTime.getValue() <= 0) {
                timerRunning.setValue(false);
                initialTimeOnClock = timerDuration;
                resetTimer();
                timerHandler.removeCallbacks(timer);
            } else {
                timerHandler.postDelayed(timer, 500);
            }
        }
    };

    public Timer(long _timerDuration) {
        timerRunning = new MutableLiveData<>(false);
        timerDuration = _timerDuration;
        initialTimeOnClock = timerDuration; // assume at initialization the timer isn't running
        displayTime = new MutableLiveData<>(initialTimeOnClock);
    }

    public void startTimer() {
        if (!isTimerRunning()) {
            if (initialTimeOnClock == timerDuration) {
                // want the timer to start counting from timer duration, not duration - 1
                startTimeAbsolute = System.currentTimeMillis() + timeUnit - 1; // -1 so when resetting it doesn't momentarily start above actual time

            } else {
                startTimeAbsolute = System.currentTimeMillis();
            }
            timerHandler.post(timer);
            timerRunning.setValue(true);
        }
    }

    public void stopTimer() {
        if (isTimerRunning()) {
            long elapsedTime = System.currentTimeMillis() - startTimeAbsolute;
            initialTimeOnClock -= elapsedTime;
            startTimeAbsolute = System.currentTimeMillis();
            timerHandler.removeCallbacks(timer);
            timerRunning.setValue(false);
        }
    }

    public void resetTimer() {
        initialTimeOnClock = timerDuration;
        if (isTimerRunning()) {
            timerRunning.setValue(false);
            timerHandler.removeCallbacks(timer);
            startTimer();
        } else {
            displayTime.setValue(initialTimeOnClock);
        }
    }

    public boolean isTimerRunning() {
        return timerRunning.getValue();
    }
}
