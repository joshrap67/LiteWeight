package com.joshrap.liteweight.fragments;

import android.app.AlertDialog;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.joshrap.liteweight.activities.WorkoutActivity;
import com.joshrap.liteweight.adapters.RoutineAdapter;
import com.joshrap.liteweight.imports.Globals;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.interfaces.FragmentWithDialog;
import com.joshrap.liteweight.models.ExerciseRoutine;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.models.Routine;
import com.joshrap.liteweight.models.User;
import com.joshrap.liteweight.models.UserWithWorkout;
import com.joshrap.liteweight.models.Workout;
import com.joshrap.liteweight.helpers.WorkoutHelper;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.network.repos.WorkoutRepository;
import com.joshrap.liteweight.widgets.Stopwatch;
import com.joshrap.liteweight.widgets.Timer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import static android.os.Looper.getMainLooper;

public class ActiveWorkoutFragment extends Fragment implements FragmentWithDialog {
    private TextView dayTV;
    private ImageButton forwardButton, backButton;
    private int currentDayIndex;
    private int currentWeekIndex;
    private Timer timer;
    private Stopwatch stopwatch;
    private Workout currentWorkout;
    private User user;
    private Routine routine;
    private AlertDialog alertDialog;
    private RecyclerView recyclerView;
    private ProgressDialog loadingDialog;
    @Inject
    SharedPreferences sharedPreferences;
    @Inject
    WorkoutRepository workoutRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Injector.getInjector(getContext()).inject(this);
        // TODO injection or view model for these two???
        currentWorkout = Globals.activeWorkout;
        user = Globals.user;
        loadingDialog = new ProgressDialog(getContext());
        loadingDialog.setCancelable(false);
        ((WorkoutActivity) getActivity()).toggleBackButton(false);

        View view;
        if (currentWorkout == null) {
            ((WorkoutActivity) getActivity()).updateToolbarTitle("LiteWeight");
            view = inflater.inflate(R.layout.default_layout, container, false);

        } else {
            routine = currentWorkout.getRoutine();
            view = inflater.inflate(R.layout.fragment_current_workout, container, false);
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (currentWorkout == null) {
            FloatingActionButton createWorkoutBtn = view.findViewById(R.id.create_workout_btn);
            createWorkoutBtn.setOnClickListener(v -> ((WorkoutActivity) getActivity()).goToNewWorkout());
            return;
        }

        currentWeekIndex = Globals.activeWorkout.getCurrentWeek();
        currentDayIndex = Globals.activeWorkout.getCurrentDay();

        ((WorkoutActivity) getActivity()).updateToolbarTitle(currentWorkout.getWorkoutName());
        recyclerView = view.findViewById(R.id.recycler_view);
        timer = ((WorkoutActivity) getActivity()).getTimer();
        stopwatch = ((WorkoutActivity) getActivity()).getStopwatch();
        forwardButton = view.findViewById(R.id.next_day_button);
        backButton = view.findViewById(R.id.previous_day_button);
        dayTV = view.findViewById(R.id.day_text_view);

        setupClock(view);
        setupButtons();
        updateRoutineListUI();
    }

    @Override
    public void onResume() {
        // when this fragment is visible again, the clock service is no longer needed so cancel it
        if (Globals.timerServiceRunning) {
            timer.cancelService();
        }

        if (Globals.stopwatchServiceRunning) {
            stopwatch.cancelService();
        }
        super.onResume();
    }

    @Override
    public void onStop() {
        // as soon as this fragment isn't visible, start any running clock as a service
        if (timer != null && timer.isTimerRunning()) {
            timer.startService();
        }

        if (stopwatch != null && stopwatch.isStopwatchRunning()) {
            stopwatch.startService();
        }
        super.onStop();
    }

    private void setupClock(View view) {
        // todo do this differently (using a broadcast?)

        ConstraintLayout stopwatchContainer = view.findViewById(R.id.stopwatch_container);
        ConstraintLayout timerContainer = view.findViewById(R.id.timer_container);
        /*
            Get shared preferences data for user settings
         */

        boolean timerEnabled = sharedPreferences.getBoolean(Variables.TIMER_ENABLED, true);
        boolean stopwatchEnabled = sharedPreferences.getBoolean(Variables.STOPWATCH_ENABLED, true);

        if (timerEnabled && stopwatchEnabled) {
            // both are enabled, so use whatever was last used
            timer.initTimerUI(view, getActivity(), getContext(), true);
            stopwatch.initStopwatchUI(view, getActivity(), true);
            String lastMode = sharedPreferences.getString(Variables.LAST_CLOCK_MODE, Variables.TIMER);
            switch (lastMode) {
                case Variables.TIMER:
                    stopwatchContainer.setVisibility(View.GONE);
                    break;
                case Variables.STOPWATCH:
                    timerContainer.setVisibility(View.GONE);
                    break;
            }
        } else if (timerEnabled) {
            // only the timer is enabled, hide the stopwatch
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(Variables.LAST_CLOCK_MODE, Variables.TIMER);
            editor.apply();

            timerContainer.setVisibility(View.VISIBLE);
            stopwatchContainer.setVisibility(View.GONE);
            timer.initTimerUI(view, getActivity(), getContext(), false);
        } else if (stopwatchEnabled) {
            // only the stopwatch is enabled, hide the timer
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(Variables.LAST_CLOCK_MODE, Variables.STOPWATCH);
            editor.apply();

            timerContainer.setVisibility(View.GONE);
            stopwatchContainer.setVisibility(View.VISIBLE);
            stopwatch.initStopwatchUI(view, getActivity(), false);
        } else {
            // none are enabled so don't show any
            stopwatchContainer.setVisibility(View.GONE);
            timerContainer.setVisibility(View.GONE);
        }
    }

    private void setupButtons() {
        /*
            Setup button listeners.
         */
        dayTV.setOnClickListener(v -> jumpDaysPopup());
        backButton.setOnClickListener(v -> {
            if (currentDayIndex > 0) {
                // if on this week there are more days, just decrease the current day index
                currentDayIndex--;
                updateRoutineListUI();
            } else if (currentWeekIndex > 0) {
                // there are more previous weeks
                currentWeekIndex--;
                currentDayIndex = routine.getWeek(currentWeekIndex).size() - 1;
                updateRoutineListUI();
            }
            currentWorkout.setCurrentDay(currentDayIndex);
            currentWorkout.setCurrentWeek(currentWeekIndex);
        });
        forwardButton.setOnClickListener(v -> {
            if (currentDayIndex + 1 < routine.getWeek(currentWeekIndex).size()) {
                // if can progress further in this week, do so
                currentDayIndex++;
                updateRoutineListUI();
            } else if (currentWeekIndex + 1 < routine.size()) {
                // there are more weeks
                currentDayIndex = 0;
                currentWeekIndex++;
                updateRoutineListUI();
            } else {
                // on last week
                restartPopup();
            }
            currentWorkout.setCurrentDay(currentDayIndex);
            currentWorkout.setCurrentWeek(currentWeekIndex);
        });
    }

    private void updateButtonViews() {
        /*
            Updates the visibility and icon of the navigation buttons depending on the current day.
         */
        if (currentDayIndex == 0 && currentWeekIndex == 0) {
            // means it's the first day in routine, so hide the back button
            backButton.setVisibility(View.INVISIBLE);
            forwardButton.setVisibility(View.VISIBLE);
            forwardButton.setImageResource(R.drawable.next_icon);
            if (currentWeekIndex + 1 == routine.size() && routine.getWeek(currentWeekIndex).size() == 1) {
                // a one day workout
                forwardButton.setImageResource(R.drawable.restart_icon);
            }
        } else if (currentWeekIndex + 1 == routine.size()
                && currentDayIndex + 1 == routine.getWeek(currentWeekIndex).size()) {
            // last day, so show reset icon
            backButton.setVisibility(View.VISIBLE);
            // lil hacky, but don't want the ripple showing when the icons switch
            forwardButton.setVisibility(View.INVISIBLE);
            forwardButton.setVisibility(View.VISIBLE);
            // last day so set the restart icon instead of next icon
            forwardButton.setImageResource(R.drawable.restart_icon);
        } else if (currentWeekIndex < routine.size()) {
            // not first day, not last. So show back and forward button
            backButton.setVisibility(View.VISIBLE);
            forwardButton.setVisibility(View.VISIBLE);
            forwardButton.setImageResource(R.drawable.next_icon);
        }
    }

    private void updateRoutineListUI() {
        /*
            Updates the list of displayed exercises in the workout depending on the current day.
         */
        boolean videosEnabled = sharedPreferences.getBoolean(Variables.VIDEO_KEY, true);
        boolean metricUnits = user.getUserPreferences().isMetricUnits();

        RoutineAdapter routineAdapter = new RoutineAdapter(routine.getExerciseListForDay(currentWeekIndex, currentDayIndex),
                user.getUserExercises(), getContext(), metricUnits, videosEnabled);
        recyclerView.setAdapter(routineAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        dayTV.setText(WorkoutHelper.generateDayTitleNew(currentWeekIndex, currentDayIndex));
        updateButtonViews();
    }

    private void restartWorkout() {
        /*
            Reset all of the exercises to being incomplete and then write to the database with these changes.
         */
        showLoadingDialog();
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ResultStatus<UserWithWorkout> resultStatus = this.workoutRepository.restartWorkout(currentWorkout);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                loadingDialog.dismiss();
                if (resultStatus.isSuccess()) {
                    Globals.activeWorkout = resultStatus.getData().getWorkout();
                    user = resultStatus.getData().getUser();
                    currentWorkout = new Workout(resultStatus.getData().getWorkout());
                    Globals.user = user;

                    routine = currentWorkout.getRoutine();
                    currentDayIndex = 0;
                    currentWeekIndex = 0;
                    updateRoutineListUI();
                } else {
                    showErrorMessage(resultStatus.getErrorMessage());
                }
            });
        });
    }

    private void showLoadingDialog() {
        loadingDialog.setMessage("Saving...");
        loadingDialog.show();
    }

    private void showErrorMessage(String message) {
        alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                .setTitle("Save workout error")
                .setMessage(message)
                .setPositiveButton("Ok", null)
                .create();
        alertDialog.show();
        // make the message font a little bigger than the default one provided by the alertdialog
        TextView messageTV = alertDialog.getWindow().findViewById(android.R.id.message);
        messageTV.setTextSize(18);
    }

    private void jumpDaysPopup() {
        /*
            Allow the user to scroll through the list of days to quickly jump around in workout
         */
        int totalDays = 0;
        int selectedVal = 0;
        List<String> days = new ArrayList<>();
        for (int week = 0; week < routine.size(); week++) {
            for (int day = 0; day < routine.getWeek(week).size(); day++) {
                if (week == currentWeekIndex && day == currentDayIndex) {
                    selectedVal = totalDays;
                }
                String dayTitle = WorkoutHelper.generateDayTitleNew(week, day);
                days.add(dayTitle);
                totalDays++;
            }
        }
        String[] daysAsArray = new String[totalDays];
        for (int i = 0; i < totalDays; i++) {
            daysAsArray[i] = days.get(i);
        }
        View popupView = getLayoutInflater().inflate(R.layout.popup_jump_days, null);
        final NumberPicker dayPicker = popupView.findViewById(R.id.day_picker);
        dayPicker.setMinValue(0);
        dayPicker.setMaxValue(totalDays - 1);
        dayPicker.setValue(selectedVal);
        dayPicker.setWrapSelectorWheel(false);
        dayPicker.setDisplayedValues(daysAsArray);

        alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                .setTitle("Jump to Day")
                .setView(popupView)
                .setPositiveButton("Go", (dialog, which) -> {
                    int count = 0;
                    for (int week = 0; week < routine.size(); week++) {
                        for (int day = 0; day < routine.getWeek(week).size(); day++) {
                            if (count == dayPicker.getValue()) {
                                currentWeekIndex = week;
                                currentDayIndex = day;
                            }
                            count++;
                        }
                    }
                    currentWorkout.setCurrentDay(currentDayIndex);
                    currentWorkout.setCurrentWeek(currentWeekIndex);
                    updateRoutineListUI();
                })
                .create();
        alertDialog.show();
    }

    private void restartPopup() {
        /*
            Prompt the user if they wish to restart the current workout.
         */
        int exercisesCompleted = 0;
        int totalExercises = 0;
        for (int week = 0; week < routine.size(); week++) {
            for (int day = 0; day < routine.getWeek(week).size(); day++) {
                for (ExerciseRoutine exerciseRoutine : routine.getExerciseListForDay(week, day)) {
                    totalExercises++;
                    if (exerciseRoutine.isCompleted()) {
                        exercisesCompleted++;
                    }
                }
            }
        }
        int percentage = (int) (((double) exercisesCompleted / (double) totalExercises) * 100);

        View popupView = getLayoutInflater().inflate(R.layout.popup_restart_workout, null);
        ProgressBar progressBar = popupView.findViewById(R.id.progress_bar);
        progressBar.setProgress(percentage);
        TextView progressTV = popupView.findViewById(R.id.progress_percentage_TV);
        progressTV.setText(String.format("%d %%", percentage));
        // color the percentage/percentage bar based on how much has been done
        if (percentage <= 20) {
            progressBar.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.workout_very_low_percentage)));
            progressTV.setTextColor(getResources().getColor(R.color.workout_very_low_percentage));
        } else if (percentage <= 40) {
            progressBar.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.workout_low_percentage)));
            progressTV.setTextColor(getResources().getColor(R.color.workout_low_percentage));
        } else if (percentage <= 60) {
            progressBar.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.workout_medium_percentage)));
            progressTV.setTextColor(getResources().getColor(R.color.workout_medium_percentage));
        } else if (percentage <= 80) {
            progressBar.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.workout_high_percentage)));
            progressTV.setTextColor(getResources().getColor(R.color.workout_high_percentage));
        } else {
            progressBar.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.workout_very_high_percentage)));
            progressTV.setTextColor(getResources().getColor(R.color.workout_very_high_percentage));
        }

        final AlertDialog alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                .setTitle("Restart Workout")
                .setView(popupView)
                .setPositiveButton("Yes", (dialog, which) -> {
                    restartWorkout();
                    updateRoutineListUI();
                })
                .setNegativeButton("No", null)
                .create();
        alertDialog.show();
    }

    @Override
    public void hideAllDialogs() {
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }
}
