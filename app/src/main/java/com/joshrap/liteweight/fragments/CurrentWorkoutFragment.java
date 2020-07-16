package com.joshrap.liteweight.fragments;

import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;

import com.joshrap.liteweight.database.entities.*;
import com.joshrap.liteweight.database.viewModels.*;
import com.joshrap.liteweight.helpers.StatisticsHelper;
import com.joshrap.liteweight.imports.Globals;
import com.joshrap.liteweight.widgets.ExerciseRow;
import com.joshrap.liteweight.helpers.WorkoutHelper;
import com.joshrap.liteweight.activities.WorkoutActivity;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.widgets.Stopwatch;
import com.joshrap.liteweight.widgets.Timer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class CurrentWorkoutFragment extends Fragment {
    private TextView dayTV, defaultTV;
    private TableLayout mainExercisesTable;
    private ImageButton forwardButton, backButton;
    private FloatingActionButton createWorkoutBtn;
    private View view;
    private ViewGroup fragmentContainer;
    private int currentDayIndex, maxDayIndex, daysPerWeek;
    private String currentWorkout;
    private MetaEntity currentWorkoutEntity;
    private WorkoutViewModel workoutModel;
    private MetaViewModel metaModel;
    private ExerciseViewModel exerciseModel;
    private HashMap<Integer, ArrayList<ExerciseRow>> workout = new HashMap<>();
    private HashMap<String, ExerciseEntity> exerciseToExerciseEntity = new HashMap<>();
    private Timer timer;
    private Stopwatch stopwatch;
    private GetExercisesTask getExercisesTask;
    private GetCurrentWorkoutMetaTask getCurrentWorkoutTask;
    private GetWorkoutTask getWorkoutTask;
    private Handler loadingHandler;
    private final Runnable showLoadingIconRunnable = new Runnable() {
        @Override
        public void run() {
            if (getActivity() != null) {
                ((WorkoutActivity) getActivity()).setProgressBar(true);
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.default_layout, container, false);
        createWorkoutBtn = view.findViewById(R.id.create_workout_btn);
        createWorkoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((WorkoutActivity) getActivity()).createWorkout();
            }
        });
        timer = ((WorkoutActivity) getActivity()).getTimer();
        stopwatch = ((WorkoutActivity) getActivity()).getStopwatch();
        defaultTV = view.findViewById(R.id.default_text_view);
        createWorkoutBtn.hide();
        defaultTV.setVisibility(View.INVISIBLE); // only show this default message later if no workouts are found
        fragmentContainer = container;
        ((WorkoutActivity) getActivity()).updateToolbarTitle(""); // empty so workout name doesn't flash once loaded
        // Set up the view models
        metaModel = ViewModelProviders.of(getActivity()).get(MetaViewModel.class);
        workoutModel = ViewModelProviders.of(getActivity()).get(WorkoutViewModel.class);
        exerciseModel = ViewModelProviders.of(getActivity()).get(ExerciseViewModel.class);

        // show loading dialog only if workout hasn't loaded in certain amount of time
        loadingHandler = new Handler();
        loadingHandler.postDelayed(showLoadingIconRunnable, 2000);
        if (Globals.currentWorkout == null) {
            // attempt to fetch the current workout from database
            getCurrentWorkoutTask = new GetCurrentWorkoutMetaTask();
            getCurrentWorkoutTask.execute();
        } else {
            // user has already loaded the current workout so skip that DB call
            currentWorkoutEntity = Globals.currentWorkout;
            currentWorkout = currentWorkoutEntity.getWorkoutName();
            currentDayIndex = currentWorkoutEntity.getCurrentDay();
            maxDayIndex = currentWorkoutEntity.getMaxDayIndex();
            daysPerWeek = currentWorkoutEntity.getNumDays();
            getExercisesTask = new GetExercisesTask();
            getExercisesTask.execute();
        }
        return view;
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
        if (timer.isTimerRunning()) {
            timer.startService();
        }

        if (stopwatch.isStopwatchRunning()) {
            stopwatch.startService();
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getCurrentWorkoutTask != null) {
            getCurrentWorkoutTask.cancel(true);
        }
        if (getExercisesTask != null) {
            getExercisesTask.cancel(true);
        }
        if (getWorkoutTask != null) {
            getWorkoutTask.cancel(true);
        }
        if (loadingHandler != null) {
            loadingHandler.removeCallbacks(showLoadingIconRunnable);
        }
    }

    private class GetCurrentWorkoutMetaTask extends AsyncTask<Void, Void, MetaEntity> {
        @Override
        protected MetaEntity doInBackground(Void... voids) {
            // get the current workout meta from the database
            return metaModel.getCurrentWorkoutMeta();
        }

        @Override
        protected void onPostExecute(MetaEntity result) {
            if (result != null) {
                // database found a workout, so assign it then move to the next stop in the chain
                Globals.currentWorkout = result;
                currentWorkoutEntity = result;
                currentWorkout = currentWorkoutEntity.getWorkoutName();
                currentDayIndex = currentWorkoutEntity.getCurrentDay();
                maxDayIndex = currentWorkoutEntity.getMaxDayIndex();
                daysPerWeek = currentWorkoutEntity.getNumDays();
                getExercisesTask = new GetExercisesTask();
                getExercisesTask.execute();
            } else {
                // no workout found, error
                if (getActivity() != null) {
                    ((WorkoutActivity) getActivity()).setProgressBar(false);
                }
                loadingHandler.removeCallbacks(showLoadingIconRunnable);
                createWorkoutBtn.show();
                defaultTV.setVisibility(View.VISIBLE);
                ((WorkoutActivity) getActivity()).updateToolbarTitle(Variables.CURRENT_WORKOUT_TITLE);
            }
        }
    }

    private class GetExercisesTask extends AsyncTask<Void, Void, ArrayList<ExerciseEntity>> {
        @Override
        protected ArrayList<ExerciseEntity> doInBackground(Void... voids) {
            // get the exercises from the database
            return exerciseModel.getAllExercises();
        }

        @Override
        protected void onPostExecute(ArrayList<ExerciseEntity> result) {
            if (!result.isEmpty()) {
                for (ExerciseEntity entity : result) {
                    exerciseToExerciseEntity.put(entity.getExerciseName(), entity);
                }
                getWorkoutTask = new GetWorkoutTask();
                getWorkoutTask.execute();
            } else {
                if (getActivity() != null) {
                    ((WorkoutActivity) getActivity()).setProgressBar(false);
                }
                loadingHandler.removeCallbacks(showLoadingIconRunnable);
                createWorkoutBtn.hide();
                defaultTV.setVisibility(View.VISIBLE);
                ((WorkoutActivity) getActivity()).updateToolbarTitle(Variables.CURRENT_WORKOUT_TITLE);
            }
        }
    }

    private class GetWorkoutTask extends AsyncTask<Void, Void, ArrayList<WorkoutEntity>> {
        @Override
        protected ArrayList<WorkoutEntity> doInBackground(Void... voids) {
            // get the exercises from the database
            return workoutModel.getExercises(currentWorkout);
        }

        @Override
        protected void onPostExecute(ArrayList<WorkoutEntity> result) {
            if (getActivity() != null) {
                ((WorkoutActivity) getActivity()).setProgressBar(false);
            }
            loadingHandler.removeCallbacks(showLoadingIconRunnable);
            if (result != null) {
                // query produced a valid list, so populate it in local memory
                initWorkout(result);
            }
        }
    }

    private void initWorkout(ArrayList<WorkoutEntity> rawData) {
        /*
            Database queries complete, so switch layouts and init all the widgets
         */
        if (getActivity() == null) {
            // to avoid possible crashes? idk why this error keeps happening sometimes
            return;
        }
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.fragment_current_workout, fragmentContainer, false);
        ViewGroup rootView = (ViewGroup) getView();
        rootView.removeAllViews();
        rootView.addView(view);
        forwardButton = view.findViewById(R.id.next_day_button);
        backButton = view.findViewById(R.id.previous_day_button);
        mainExercisesTable = view.findViewById(R.id.main_table);
        dayTV = view.findViewById(R.id.day_text_view);
        ConstraintLayout stopwatchContainer = view.findViewById(R.id.stopwatch_container);
        ConstraintLayout timerContainer = view.findViewById(R.id.timer_container);
        /*
            Get shared preferences data for user settings
         */
        SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences(Variables.SHARED_PREF_SETTINGS, 0);
        boolean videosEnabled = pref.getBoolean(Variables.VIDEO_KEY, true);
        boolean metricUnits = pref.getBoolean(Variables.UNIT_KEY, false);

        boolean timerEnabled = pref.getBoolean(Variables.TIMER_ENABLED, true);
        boolean stopwatchEnabled = pref.getBoolean(Variables.STOPWATCH_ENABLED, true);

        if (timerEnabled && stopwatchEnabled) {
            // both are enabled, so use whatever was last used
            timer.initTimerUI(view, getActivity(), getContext(), true);
            stopwatch.initStopwatchUI(view, getActivity(), true);
            String lastMode = pref.getString(Variables.LAST_CLOCK_MODE, Variables.TIMER);
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
            SharedPreferences.Editor editor = pref.edit();
            editor.putString(Variables.LAST_CLOCK_MODE, Variables.TIMER);
            editor.apply();

            timerContainer.setVisibility(View.VISIBLE);
            stopwatchContainer.setVisibility(View.GONE);
            timer.initTimerUI(view, getActivity(), getContext(), false);
        } else if (stopwatchEnabled) {
            // only the stopwatch is enabled, hide the timer
            SharedPreferences.Editor editor = pref.edit();
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

        ((WorkoutActivity) getActivity()).updateToolbarTitle(currentWorkout);
        // init the hash table that the entire workout will be in
        for (int i = 0; i <= maxDayIndex; i++) {
            workout.put(i, new ArrayList<ExerciseRow>());
        }
        // fill the hash table with exercises
        for (WorkoutEntity entity : rawData) {
            String exerciseName = entity.getExercise();
            ExerciseRow exercise = new ExerciseRow(entity, exerciseToExerciseEntity.get(exerciseName), getContext(),
                    getActivity(), videosEnabled, metricUnits, workoutModel, exerciseModel);
            workout.get(entity.getDay()).add(exercise);
        }
        // sort all the workouts in each day alphabetically
        for (int i = 0; i <= maxDayIndex; i++) {
            Collections.sort(workout.get(i));
        }
        setupButtons();
        updateWorkoutUI();
    }

    private void setupButtons() {
        /*
            Setup button listeners.
         */
        dayTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jumpDaysPopup();
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentDayIndex > 0) {
                    currentDayIndex--;
                    currentWorkoutEntity.setCurrentDay(currentDayIndex);
                    metaModel.update(currentWorkoutEntity);
                    updateWorkoutUI();
                }
            }
        });
        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentDayIndex < maxDayIndex) {
                    currentDayIndex++;
                    currentWorkoutEntity.setCurrentDay(currentDayIndex);
                    metaModel.update(currentWorkoutEntity);
                    updateWorkoutUI();
                } else if (currentDayIndex == maxDayIndex) {
                    restartPopup();
                }
            }
        });
        forwardButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                restartPopup();
                return true;
            }
        });
    }

    private void updateWorkoutUI() {
        /*
            Populates the list of exercise rows based on the current day in the workout.
         */
        mainExercisesTable.removeAllViews();
        dayTV.setText(WorkoutHelper.generateDayTitle(currentDayIndex, daysPerWeek, Globals.currentWorkout.getWorkoutType()));
        int count = 0;
        for (ExerciseRow exercise : workout.get(currentDayIndex)) {
            View row = exercise.getDisplayedRow();
            mainExercisesTable.addView(row, count);
            count++;
        }
        updateButtons();
    }

    private void updateButtons() {
        /*
            Updates the visibility and icon of the navigation buttons depending on the current day.
         */
        if (currentDayIndex == 0) {
            // means it's the first day, so hide the back button
            backButton.setVisibility(View.INVISIBLE);
            forwardButton.setVisibility(View.VISIBLE);
            forwardButton.setImageResource(R.drawable.next_icon);
            if (currentDayIndex == maxDayIndex) {
                // a one day workout
                forwardButton.setImageResource(R.drawable.restart_icon);
            }
        } else if (currentDayIndex != maxDayIndex) {
            // day in the middle, so just show the normal back and forward buttons
            backButton.setVisibility(View.VISIBLE);
            forwardButton.setVisibility(View.VISIBLE);
            forwardButton.setImageResource(R.drawable.next_icon);
        } else {
            // lil hacky, but don't want the ripple showing when the icons switch
            forwardButton.setVisibility(View.INVISIBLE);
            forwardButton.setVisibility(View.VISIBLE);
            backButton.setVisibility(View.VISIBLE);
            // last day so set the restart icon instead of next icon
            forwardButton.setImageResource(R.drawable.restart_icon);
        }
    }

    private void restartWorkout() {
        /*
            Reset all of the exercises to being incomplete and then write to the database with these changes.
         */
        int exercisesCompleted = 0;
        int totalExercises = 0;
        for (int i = 0; i <= maxDayIndex; i++) {
            for (ExerciseRow exercise : workout.get(i)) {
                ExerciseEntity exerciseEntity = exerciseToExerciseEntity.get(exercise.getName());
                totalExercises++;
                if (exercise.getStatus()) {
                    exerciseEntity.setTimesCompleted(exerciseEntity.getTimesCompleted() + 1);
                    exercisesCompleted++;
                }
                exercise.setStatus(false);
                exerciseModel.update(exerciseEntity);
                workoutModel.update(exercise.getWorkoutEntity());
            }
        }
        StatisticsHelper.workoutResetStatistics(currentWorkoutEntity, metaModel, exercisesCompleted, totalExercises);

        currentDayIndex = 0;
        updateWorkoutUI();
    }

    private void jumpDaysPopup() {
        /*
            Allow the user to scroll through the list of days to quickly jump around in workout
         */
        String[] days = new String[maxDayIndex + 1];
        for (int i = 0; i <= maxDayIndex; i++) {
            days[i] = WorkoutHelper.generateDayTitle(i, daysPerWeek, Globals.currentWorkout.getWorkoutType());
        }
        View popupView = getLayoutInflater().inflate(R.layout.popup_jump_days, null);
        final NumberPicker dayPicker = popupView.findViewById(R.id.day_picker);
        dayPicker.setMinValue(0);
        dayPicker.setMaxValue(maxDayIndex);
        dayPicker.setValue(currentDayIndex);
        dayPicker.setWrapSelectorWheel(false);
        dayPicker.setDisplayedValues(days);

        final AlertDialog alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                .setTitle("Jump to Day")
                .setView(popupView)
                .setPositiveButton("Go", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        currentDayIndex = dayPicker.getValue();
                        currentWorkoutEntity.setCurrentDay(currentDayIndex);
                        metaModel.update(currentWorkoutEntity);
                        updateWorkoutUI();
                    }
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
        for (int i = 0; i <= maxDayIndex; i++) {
            for (ExerciseRow exercise : workout.get(i)) {
                totalExercises++;
                if (exercise.getStatus()) {
                    exercisesCompleted++;
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
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mainExercisesTable.removeAllViews();
                        restartWorkout();
                        updateWorkoutUI();
                    }
                })
                .setNegativeButton("No", null)
                .create();
        alertDialog.show();
    }
}
