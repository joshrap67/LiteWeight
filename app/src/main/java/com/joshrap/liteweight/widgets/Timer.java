package com.joshrap.liteweight.widgets;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.joshrap.liteweight.imports.Globals;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.services.TimerService;

import java.util.Locale;

public class Timer {
    private Button startTimer, stopTimer, resetTimer, showStopwatchButton;
    private boolean timerRunning, showStopwatch;
    private final long timeUnit = 1000;
    private long startTimeAbsolute, initialTimeOnClock, timerDuration, displayTime; // in SI units of milliseconds
    private TextView timerDisplay;
    private ConstraintLayout timerContainer, stopwatchContainer;
    private Activity activity;
    private Context context;
    private SharedPreferences pref;
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

    public Timer(Activity _activity) {
        activity = _activity;
        timerRunning = false;
        pref = activity.getApplicationContext().getSharedPreferences(Variables.SHARED_PREF_SETTINGS, 0);
        timerDuration = pref.getLong(Variables.TIMER_DURATION, Variables.DEFAULT_TIMER_VALUE);
        initialTimeOnClock = timerDuration; // assume at initialization the timer isn't running
        displayTime = initialTimeOnClock;
    }

    public void initTimerUI(View timerView, Activity _activity, Context _context, boolean stopwatchVisible) {
        // this is called whenever the current workout fragment loads. It sets up the UI for the timer to update
        activity = _activity;
        startTimer = timerView.findViewById(R.id.start_timer);
        stopTimer = timerView.findViewById(R.id.stop_timer);
        resetTimer = timerView.findViewById(R.id.reset_timer);
        showStopwatchButton = timerView.findViewById(R.id.show_stopwatch);
        timerDisplay = timerView.findViewById(R.id.timer);
        timerContainer = timerView.findViewById(R.id.timer_container);
        stopwatchContainer = timerView.findViewById(R.id.stopwatch_container);
        showStopwatch = stopwatchVisible;
        context = _context;
        cancelService();
        initTimer();
    }

    private void initTimer() {
        if (timerRunning) {
            timerRunningVisibility();
        } else {
            timerFinishedVisibility();
        }
        startTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timerRunningVisibility();
                startTimer();
            }
        });
        stopTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timerFinishedVisibility();
                stopTimer();
            }
        });
        resetTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTimer();
            }
        });
        showStopwatchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStopwatch();
            }
        });
        timerDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // allow the timer to be clickable in order for the user to input the time they want
                if (!timerRunning) {
                    showTimerPopup();
                }
            }
        });
        updateTimerDisplay(displayTime);
    }

    private void showTimerPopup() {
        /*
            Allows the user to choose the time they wish the timer to have.
         */
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
        final AlertDialog alertDialog = new AlertDialog.Builder(context, R.style.AlertDialogTheme)
                .setView(popupView)
                .setTitle("Pick Time")
                .setPositiveButton("Save Time", null)
                .setNegativeButton("Cancel", null)
                .create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        long minutes = minutePicker.getValue() * (60 * timeUnit);
                        long seconds = secondPicker.getValue() * timeUnit;
                        timerDuration = minutes + seconds;
                        if (timerDuration > 0) {
                            resetTimer();
                            editor = pref.edit();
                            editor.putLong(Variables.TIMER_DURATION, timerDuration);
                            editor.apply();
                            alertDialog.dismiss();
                        } else {
                            Toast.makeText(context, "Invalid time", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        });
        alertDialog.show();
    }

    private void startTimer() {
        if (!timerRunning) {
            timerDisplay.setBackgroundResource(0);
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
            timerDisplay.setBackgroundResource(R.drawable.timer_background);
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
        timerDisplay.setBackgroundResource(R.drawable.timer_background);
        stopTimer.setVisibility(View.GONE);
        startTimer.setVisibility(View.VISIBLE);
        showStopwatchButton.setVisibility((showStopwatch) ? View.VISIBLE : View.GONE);
    }

    private void timerRunningVisibility() {
        timerDisplay.setBackgroundResource(0);
        stopTimer.setVisibility(View.VISIBLE);
        startTimer.setVisibility(View.GONE);
        showStopwatchButton.setVisibility(View.GONE);
    }

    private void showStopwatch() {
        /*
            User has indicated that they want the stopwatch, so hide the timer and show stopwatch
         */
        editor = pref.edit();
        editor.putString(Variables.LAST_CLOCK_MODE, Variables.STOPWATCH);
        editor.apply();
        timerContainer.setVisibility(View.GONE);
        stopwatchContainer.setVisibility(View.VISIBLE);
    }

    public void startService() {
        /*
            Timer is going out of the visible screen, so start a service to maintain its progress.
         */
        Intent serviceIntent = new Intent(activity, TimerService.class);
        serviceIntent.putExtra(Variables.INTENT_TIMER_ABSOLUTE_START_TIME, startTimeAbsolute);
        serviceIntent.putExtra(Variables.INTENT_TIMER_TIME_ON_CLOCK, initialTimeOnClock);
        activity.startService(serviceIntent);
        Globals.timerServiceRunning = true;
    }

    public void cancelService() {
        activity.stopService(new Intent(activity, TimerService.class));
        Globals.timerServiceRunning = false;

        // get rid of any notifications that are still showing now that the timer is on the screen
        NotificationManager notificationManager = (NotificationManager) activity.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(TimerService.timerRunningId);
        notificationManager.cancel(TimerService.timerFinishedId);
    }

    public boolean isTimerRunning() {
        return timerRunning;
    }
}
