package com.joshrap.liteweight.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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
    private RelativeLayout stopwatchLayout, timerContainer, timerDurationContainer;
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
        stopwatchLayout = view.findViewById(R.id.stopwatch_layout);
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
                    stopwatchLayout.setVisibility(View.INVISIBLE);
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
            stopwatchLayout.setVisibility(View.INVISIBLE);
        } else if (stopwatchEnabled) {
            // only the stopwatch is enabled, hide the timer
            editor.putString(Variables.LAST_CLOCK_MODE, Variables.STOPWATCH);
            editor.apply();

            timerContainer.setVisibility(View.INVISIBLE);
            stopwatchLayout.setVisibility(View.VISIBLE);
        } else {
            // shouldn't be reached, but none are enabled so don't show anything
            stopwatchLayout.setVisibility(View.INVISIBLE);
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

        Button saveTimeDurationButton = view.findViewById(R.id.save_time_btn);
        Button timerPickerBackButton = view.findViewById(R.id.timer_picker_back_btn);
        timerPickerBackButton.setOnClickListener(v -> setTimerDurationVisibility(false));
        saveTimeDurationButton.setOnClickListener(v -> {
            // clear focus so if user inputted text it gets set to the number pickers
            minutePicker.clearFocus();
            secondPicker.clearFocus();
            // hide keyboard
            final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);

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
        startTimerButton = view.findViewById(R.id.start_timer_btn);
        stopTimerButton = view.findViewById(R.id.stop_timer_btn);
        Button resetTimerButton = view.findViewById(R.id.reset_timer_btn);
        showStopwatchButton = view.findViewById(R.id.show_stopwatch_btn);
        timerTV = view.findViewById(R.id.timer_tv);
        setTimerDurationTV = view.findViewById(R.id.set_time_tv);
        timerDurationContainer = view.findViewById(R.id.timer_picker_layout);
        timerDisplayLayout = view.findViewById(R.id.timer_display_container);
        timerButtonsLayout = view.findViewById(R.id.timer_buttons_container);

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

        timer.timeRemaining.observe(getViewLifecycleOwner(), this::updateTimerDisplays);
        timer.timerRunning.observe(getViewLifecycleOwner(), this::setTimerViewsVisibility);
        //endregion

        //region Stopwatch
        startStopwatchButton = view.findViewById(R.id.start_stopwatch_btn);
        stopStopwatchButton = view.findViewById(R.id.stop_stopwatch_btn);
        Button resetStopwatchButton = view.findViewById(R.id.reset_stopwatch_btn);
        showTimerButton = view.findViewById(R.id.show_timer_btn);
        stopwatchTV = view.findViewById(R.id.stopwatch_tv);

        startStopwatchButton.setOnClickListener(v -> startStopwatch());
        stopStopwatchButton.setOnClickListener(v -> stopStopwatch());
        resetStopwatchButton.setOnClickListener(v -> stopwatch.resetStopwatch());
        showTimerButton.setOnClickListener(v -> switchToTimer());

        stopwatch.elapsedTime.observe(getViewLifecycleOwner(), this::updateStopwatchDisplays);
        stopwatch.stopwatchRunning.observe(getViewLifecycleOwner(), this::setStopwatchViewsVisibility);

        return view;

    }

    private void startStopwatch() {
        stopwatch.startStopwatch();
        dismiss();
    }

    private void startTimer() {
        timer.startTimer();
        dismiss();
    }

    private void stopTimer() {
        timer.stopTimer();
    }

    private void stopStopwatch() {
        stopwatch.stopStopwatch();
    }

    private void setTimerViewsVisibility(boolean timerRunning) {
        setTimerDurationTV.setVisibility(timerRunning ? View.INVISIBLE : View.VISIBLE);
        stopTimerButton.setVisibility(timerRunning ? View.VISIBLE : View.GONE);
        startTimerButton.setVisibility(timerRunning ? View.GONE : View.VISIBLE);

        if (showStopwatch && !timerRunning) {
            showStopwatchButton.setVisibility(View.VISIBLE);
        } else {
            showStopwatchButton.setVisibility(View.GONE);
        }
    }

    private void setStopwatchViewsVisibility(boolean stopwatchRunning) {
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
        stopwatchLayout.setVisibility(View.VISIBLE);
    }

    private void switchToTimer() {
        editor.putString(Variables.LAST_CLOCK_MODE, Variables.TIMER);
        editor.apply();
        timerContainer.setVisibility(View.VISIBLE);
        stopwatchLayout.setVisibility(View.INVISIBLE);
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