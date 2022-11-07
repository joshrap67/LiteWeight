package com.joshrap.liteweight.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.activities.WorkoutActivity;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.widgets.Stopwatch;
import com.joshrap.liteweight.widgets.Timer;

import java.util.Locale;

import javax.inject.Inject;

public class ClockBottomFragment extends BottomSheetDialogFragment {

    // timer views
    private Button startTimerButton, stopTimerButton, showStopwatchButton;
    private boolean showStopwatch;
    private TextView timerTV, setTimerDurationTV;
    private RelativeLayout stopwatchContainer, timerContainer, timerDurationContainer;
    private LinearLayout timerDisplayLayout, timerButtonsLayout;

    // stopwatch views
    private Button startStopwatchButton, stopStopwatchButton, showTimerButton;
    private boolean showTimer;
    private TextView stopwatchTV;

    private Timer timer;
    private Stopwatch stopwatch;
    private SharedPreferences.Editor editor;

    @Inject
    SharedPreferences sharedPreferences;

    public static ClockBottomFragment newInstance() {
        return new ClockBottomFragment();
    }

    public static final String TAG = "ClockBottomFragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Injector.getInjector(getContext()).inject(this);

        timer = ((WorkoutActivity) getActivity()).getTimer();
        stopwatch = ((WorkoutActivity) getActivity()).getStopwatch();
        editor = sharedPreferences.edit();

        View view = inflater.inflate(R.layout.bottom_sheet_clock, container, false);
        // get the views and attach the listener
        stopwatchContainer = view.findViewById(R.id.stopwatch_container);
        timerContainer = view.findViewById(R.id.timer_container);

        boolean timerEnabled = sharedPreferences.getBoolean(Variables.TIMER_ENABLED, true);
        boolean stopwatchEnabled = sharedPreferences.getBoolean(Variables.STOPWATCH_ENABLED, true);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        if (timerEnabled && stopwatchEnabled) {
            // both are enabled, so use whatever was last used
            String lastMode = sharedPreferences.getString(Variables.LAST_CLOCK_MODE, Variables.TIMER);
            showStopwatch = true;
            showTimer = true;
            switch (lastMode) {
                case Variables.TIMER:
                    stopwatchContainer.setVisibility(View.INVISIBLE);
                    break;
                case Variables.STOPWATCH:
                    timerContainer.setVisibility(View.INVISIBLE);
                    break;
            }
        } else if (timerEnabled) {
            // only the timer is enabled, hide the stopwatch
            editor.putString(Variables.LAST_CLOCK_MODE, Variables.TIMER);
            editor.apply();

            timerContainer.setVisibility(View.VISIBLE);
            stopwatchContainer.setVisibility(View.INVISIBLE);
        } else if (stopwatchEnabled) {
            // only the stopwatch is enabled, hide the timer
            editor.putString(Variables.LAST_CLOCK_MODE, Variables.STOPWATCH);
            editor.apply();

            timerContainer.setVisibility(View.INVISIBLE);
            stopwatchContainer.setVisibility(View.VISIBLE);
        } else {
            // shouldn't be reached, but none are enabled so don't show anything
            stopwatchContainer.setVisibility(View.INVISIBLE);
            timerContainer.setVisibility(View.INVISIBLE);
        }
        // setup logic for buttons/display


        //region Time Duration Picker
        final NumberPicker minutePicker = view.findViewById(R.id.minutes_picker);
        minutePicker.setMaxValue(59);
        minutePicker.setMinValue(0);
        minutePicker.setValue((int) (timer.timerDuration / (60 * Timer.timeUnit)));
        minutePicker.setFormatter(i -> String.format("%02d", i));
        final NumberPicker secondPicker = view.findViewById(R.id.seconds_picker);
        secondPicker.setMaxValue(59);
        secondPicker.setMinValue(0);
        secondPicker.setValue((int) (timer.timerDuration / Timer.timeUnit) % 60);
        secondPicker.setFormatter(i -> String.format("%02d", i));

        Button saveTimeDurationButton = view.findViewById(R.id.timer_picker_save_time_btn);
        Button timerPickerBackButton = view.findViewById(R.id.timer_picker_back_btn);
        timerPickerBackButton.setOnClickListener(v -> setTimerDurationVisibility(false));
        saveTimeDurationButton.setOnClickListener(v -> {
            long minutes = minutePicker.getValue() * (60 * Timer.timeUnit);
            long seconds = secondPicker.getValue() * Timer.timeUnit;
            int totalTime = (int) (minutes + seconds);
            if (totalTime > 0) {
                timer.timerDuration = minutes + seconds;
                timer.resetTimer();
                editor.putLong(Variables.TIMER_DURATION, timer.timerDuration);
                editor.apply();
                setTimerDurationVisibility(false);
            } else {
                Toast.makeText(getActivity(), "Invalid time", Toast.LENGTH_SHORT).show();
            }
        });
        //endregion

        //region Timer
        startTimerButton = view.findViewById(R.id.start_timer);
        stopTimerButton = view.findViewById(R.id.stop_timer);
        Button resetTimerButton = view.findViewById(R.id.reset_timer);
        showStopwatchButton = view.findViewById(R.id.show_stopwatch);
        timerTV = view.findViewById(R.id.timer);
        setTimerDurationTV = view.findViewById(R.id.set_time_tv);
        timerDurationContainer = view.findViewById(R.id.timer_picker_layout);
        timerDisplayLayout = view.findViewById(R.id.timer_display_layout);
        timerButtonsLayout = view.findViewById(R.id.timer_buttons_layout);

        setTimerViewsVisibility();
        startTimerButton.setOnClickListener(v -> startTimer());
        stopTimerButton.setOnClickListener(v -> stopTimer());
        resetTimerButton.setOnClickListener(v -> timer.resetTimer());
        showStopwatchButton.setOnClickListener(v -> switchToStopwatch());

        setTimerDurationTV.setOnClickListener(v -> {
            if (!timer.isTimerRunning()) {
                setTimerDurationVisibility(true);
            }
        });
        timerTV.setOnClickListener(v -> {
            if (!timer.isTimerRunning()) {
                setTimerDurationVisibility(true);
            }
        });

        timer.displayTime.observe(getViewLifecycleOwner(), elapsedTime -> {
            if (elapsedTime <= 0) {
                // timer is done
                updateTimerDisplays(elapsedTime);
                setTimerViewsVisibility();
            } else {
                updateTimerDisplays(elapsedTime);
            }
        });
        //endregion

        //region Stopwatch
        startStopwatchButton = view.findViewById(R.id.start_stopwatch);
        stopStopwatchButton = view.findViewById(R.id.stop_stopwatch);
        Button resetStopwatchButton = view.findViewById(R.id.reset_stopwatch);
        showTimerButton = view.findViewById(R.id.show_timer);
        stopwatchTV = view.findViewById(R.id.stopwatch);

        setStopwatchViewsVisibility();
        startStopwatchButton.setOnClickListener(v -> startStopwatch());
        stopStopwatchButton.setOnClickListener(v -> stopStopwatch());
        resetStopwatchButton.setOnClickListener(v -> stopwatch.resetStopwatch());
        showTimerButton.setOnClickListener(v -> switchToTimer());

        stopwatch.displayTime.observe(getViewLifecycleOwner(), elapsedTime -> {
            if (elapsedTime >= Variables.MAX_STOPWATCH_TIME) {
                updateStopwatchDisplays(elapsedTime);
                setStopwatchViewsVisibility();
            } else {
                updateStopwatchDisplays(elapsedTime);
            }
        });

        return view;

    }

    private void startStopwatch() {
        stopwatch.startStopwatch();
        dismiss();
        setStopwatchViewsVisibility();
    }

    private void startTimer() {
        timer.startTimer();
        dismiss();
        setTimerViewsVisibility();
    }

    private void stopTimer() {
        timer.stopTimer();
        setTimerViewsVisibility();
    }

    private void stopStopwatch() {
        stopwatch.stopStopwatch();
        setStopwatchViewsVisibility();
    }

    private void setTimerViewsVisibility() {
        boolean timerRunning = timer.isTimerRunning();
        setTimerDurationTV.setVisibility(timerRunning ? View.INVISIBLE : View.VISIBLE);
        stopTimerButton.setVisibility(timerRunning ? View.VISIBLE : View.GONE);
        startTimerButton.setVisibility(timerRunning ? View.GONE : View.VISIBLE);

        if (showStopwatch && !timerRunning) {
            showStopwatchButton.setVisibility(View.VISIBLE);
        } else {
            showStopwatchButton.setVisibility(View.GONE);
        }
    }

    private void setStopwatchViewsVisibility() {
        boolean stopwatchRunning = stopwatch.isStopwatchRunning();
        stopStopwatchButton.setVisibility(stopwatchRunning ? View.VISIBLE : View.GONE);
        startStopwatchButton.setVisibility(stopwatchRunning ? View.GONE : View.VISIBLE);

        if (showTimer && !stopwatchRunning) {
            showTimerButton.setVisibility(View.VISIBLE);
        } else {
            showTimerButton.setVisibility(View.GONE);
        }
    }

    private void setTimerDurationVisibility(boolean showTimerDuration) {
        timerDurationContainer.setVisibility(showTimerDuration ? View.VISIBLE : View.INVISIBLE);
        timerButtonsLayout.setVisibility(showTimerDuration ? View.INVISIBLE : View.VISIBLE);
        timerDisplayLayout.setVisibility(showTimerDuration ? View.INVISIBLE : View.VISIBLE);
    }


    public void switchToStopwatch() {
        editor.putString(Variables.LAST_CLOCK_MODE, Variables.STOPWATCH);
        editor.apply();
        timerContainer.setVisibility(View.INVISIBLE);
        stopwatchContainer.setVisibility(View.VISIBLE);
    }

    private void switchToTimer() {
        editor.putString(Variables.LAST_CLOCK_MODE, Variables.TIMER);
        editor.apply();
        timerContainer.setVisibility(View.VISIBLE);
        stopwatchContainer.setVisibility(View.INVISIBLE);
    }

    private void updateTimerDisplays(long elapsedTime) {
        int minutes = (int) (elapsedTime / (60 * Timer.timeUnit));
        int seconds = (int) (elapsedTime / Timer.timeUnit) % 60;
        String timeRemaining = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

        if (timerTV != null) {
            timerTV.setText(timeRemaining);
        }
    }

    private void updateStopwatchDisplays(long elapsedTime) {
        int minutes = (int) (elapsedTime / (60 * Stopwatch.timeUnit));
        int seconds = (int) (elapsedTime / Stopwatch.timeUnit) % 60;
        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

        if (stopwatchTV != null) {
            stopwatchTV.setText(timeFormatted);
        }
    }
}