package com.joshrap.liteweight.widgets;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;

import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.services.TimerService;

import java.util.Locale;

public class Timer {
    private Button startTimer, stopTimer, resetTimer, showStopwatchButton;
    private boolean timerRunning, showStopwatch;
    private static final long timeUnit = 1000; // in SI units of milliseconds
    private long startTimeAbsolute, initialTimeOnClock, timerDuration, displayTime; // in SI units of milliseconds
    private TextView timerDisplay, setTimeTV;
    private RelativeLayout timerContainer, stopwatchContainer;
    private Activity activity;
    private final SharedPreferences preferences;
    private AlertDialog alertDialog;
    private SharedPreferences.Editor editor;
    private final Handler timerHandler = new Handler();
    private final Runnable timer = new Runnable() {
        @Override
        public void run() {
            long elapsedTime = System.currentTimeMillis() - startTimeAbsolute;
            displayTime = initialTimeOnClock - elapsedTime;
            if (displayTime <= 0) {
                timerRunning = false;
                initialTimeOnClock = timerDuration;
                displayTime = timerDuration;
                timerFinishedVisibility();
                resetTimer();
                timerHandler.removeCallbacks(timer);
            } else {
                updateTimerDisplay(displayTime);
                timerHandler.postDelayed(timer, 500);
            }
        }
    };

    public Timer(Activity _activity, SharedPreferences sharedPreferences) {
        activity = _activity;
        timerRunning = false;
        preferences = sharedPreferences;
        timerDuration = preferences.getLong(Variables.TIMER_DURATION, Variables.DEFAULT_TIMER_VALUE);
        initialTimeOnClock = timerDuration; // assume at initialization the timer isn't running
        displayTime = initialTimeOnClock;
    }

    /**
     * Initializes the UI of the timer. Called when the current workout fragment loads.
     *
     * @param timerView        view with all the timer components.
     * @param _activity        activity that the fragment belongs to
     * @param stopwatchVisible whether the button to change to the stopwatch should be visible or not
     */
    public void initTimerUI(View timerView, Activity _activity, boolean stopwatchVisible) {
        activity = _activity;
        startTimer = timerView.findViewById(R.id.start_timer);
        stopTimer = timerView.findViewById(R.id.stop_timer);
        resetTimer = timerView.findViewById(R.id.reset_timer);
        showStopwatchButton = timerView.findViewById(R.id.show_stopwatch);
        timerDisplay = timerView.findViewById(R.id.timer);
        timerContainer = timerView.findViewById(R.id.timer_container);
        setTimeTV = timerView.findViewById(R.id.set_time_tv);
        stopwatchContainer = timerView.findViewById(R.id.stopwatch_container);
        showStopwatch = stopwatchVisible;
        cancelService();
        initTimer();
    }

    /**
     * Initializes the buttons of the timer.
     */
    private void initTimer() {
        if (timerRunning) {
            timerRunningVisibility();
        } else {
            timerFinishedVisibility();
        }
        startTimer.setOnClickListener(v -> {
            timerRunningVisibility();
            startTimer();
        });

        stopTimer.setOnClickListener(v -> {
            timerFinishedVisibility();
            stopTimer();
        });
        resetTimer.setOnClickListener(v -> resetTimer());
        showStopwatchButton.setOnClickListener(v -> showStopwatch());

        // allow the timer/text view to be clickable in order for the user to input the time they want
        setTimeTV.setOnClickListener(v -> {
            if (!timerRunning) {
                showSetTimerDurationPopup();
            }
        });
        timerDisplay.setOnClickListener(v -> {
            if (!timerRunning) {
                showSetTimerDurationPopup();
            }
        });
        updateTimerDisplay(displayTime);
    }

    /**
     * Allows the user to choose the time they wish the timer to have.
     */
    private void showSetTimerDurationPopup() {
        View popupView = activity.getLayoutInflater().inflate(R.layout.popup_pick_time, null);
        final NumberPicker minutePicker = popupView.findViewById(R.id.minutes_picker);
        minutePicker.setMaxValue(59);
        minutePicker.setMinValue(0);
        minutePicker.setValue((int) (timerDuration / (60 * timeUnit)));
        final NumberPicker secondPicker = popupView.findViewById(R.id.seconds_picker);
        secondPicker.setMaxValue(59);
        secondPicker.setMinValue(0);
        secondPicker.setValue((int) (timerDuration / timeUnit) % 60);

        // now that the widgets are all initialized from the view, create the dialog and insert the view into it
        alertDialog = new AlertDialog.Builder(activity, R.style.AlertDialogTheme)
                .setView(popupView)
                .setTitle("Set Timer Duration")
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create();
        alertDialog.setOnShowListener(dialogInterface -> {
            Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                long minutes = minutePicker.getValue() * (60 * timeUnit);
                long seconds = secondPicker.getValue() * timeUnit;
                int totalTime = (int) (minutes + seconds);
                if (totalTime > 0) {
                    timerDuration = minutes + seconds;
                    resetTimer();
                    editor = preferences.edit();
                    editor.putLong(Variables.TIMER_DURATION, timerDuration);
                    editor.apply();
                    alertDialog.dismiss();
                } else {
                    Toast.makeText(activity, "Invalid time", Toast.LENGTH_SHORT).show();
                }

            });
        });
        alertDialog.show();
    }

    /**
     * Hides the pick timer dialog if it is open. Needed to be closed when navigating from clicking a notification.
     */
    public void hideDialog() {
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }

    private void startTimer() {
        if (!timerRunning) {
            if (initialTimeOnClock == timerDuration) {
                // want the timer to start counting from timer duration, not duration - 1
                startTimeAbsolute = System.currentTimeMillis() + timeUnit - 1; // -1 so when resetting it doesn't momentarily start above actual time

            } else {
                startTimeAbsolute = System.currentTimeMillis();
            }
            timerHandler.post(timer);
            timerRunning = true;
        }
    }

    public void stopTimer() {
        if (timerRunning) {
            long elapsedTime = System.currentTimeMillis() - startTimeAbsolute;
            initialTimeOnClock -= elapsedTime;
            startTimeAbsolute = System.currentTimeMillis();
            timerHandler.removeCallbacks(timer);
            timerRunning = false;
        }
    }

    private void resetTimer() {
        initialTimeOnClock = timerDuration;
        if (timerRunning) {
            timerRunning = false;
            timerHandler.removeCallbacks(timer);
            startTimer();
        } else {
            // just a static reset of the textview since the timer isn't actually running
            displayTime = initialTimeOnClock;
            updateTimerDisplay(initialTimeOnClock);
        }
    }

    private void updateTimerDisplay(long elapsedTime) {
        int minutes = (int) (elapsedTime / (60 * timeUnit));
        int seconds = (int) (elapsedTime / timeUnit) % 60;
        String timeRemaining = String.format(Locale.getDefault(),
                "%02d:%02d", minutes, seconds);
        timerDisplay.setText(timeRemaining);
    }

    private void timerFinishedVisibility() {
        setTimeTV.setVisibility(View.VISIBLE);
        stopTimer.setVisibility(View.GONE);
        startTimer.setVisibility(View.VISIBLE);
        showStopwatchButton.setVisibility((showStopwatch) ? View.VISIBLE : View.GONE);
    }

    private void timerRunningVisibility() {
        setTimeTV.setVisibility(View.INVISIBLE);
        stopTimer.setVisibility(View.VISIBLE);
        startTimer.setVisibility(View.GONE);
        showStopwatchButton.setVisibility(View.GONE);
    }

    /**
     * User has indicated that they want the stopwatch, so hide the timer and show stopwatch
     */
    private void showStopwatch() {
        editor = preferences.edit();
        editor.putString(Variables.LAST_CLOCK_MODE, Variables.STOPWATCH);
        editor.apply();
        timerContainer.setVisibility(View.GONE);
        stopwatchContainer.setVisibility(View.VISIBLE);
    }

    /**
     * Timer is going out of the visible screen, so start a service to maintain its progress.
     */
    public void startService() {
        Intent serviceIntent = new Intent(activity, TimerService.class);
        serviceIntent.putExtra(Variables.INTENT_TIMER_ABSOLUTE_START_TIME, startTimeAbsolute);
        serviceIntent.putExtra(Variables.INTENT_TIMER_TIME_ON_CLOCK, initialTimeOnClock);
        activity.startService(serviceIntent);
    }

    public void cancelService() {
        activity.stopService(new Intent(activity, TimerService.class));

        // get rid of any notifications that are still showing now that the timer is on the screen
        NotificationManager notificationManager = (NotificationManager) activity.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(TimerService.timerRunningId);
        notificationManager.cancel(TimerService.timerFinishedId);
    }

    public boolean isTimerRunning() {
        return timerRunning;
    }
}
