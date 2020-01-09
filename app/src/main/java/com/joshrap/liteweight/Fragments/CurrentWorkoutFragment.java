package com.joshrap.liteweight.Fragments;

import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TableLayout;
import android.widget.TextView;

import com.joshrap.liteweight.Database.Entities.*;
import com.joshrap.liteweight.Database.ViewModels.*;
import com.joshrap.liteweight.Classes.Exercise;
import com.joshrap.liteweight.Helpers.WorkoutHelper;
import com.joshrap.liteweight.MainActivity;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.Globals.Variables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class CurrentWorkoutFragment extends Fragment {
    private TextView dayTV, defaultTV;
    private TableLayout table;
    private Button forwardButton, backButton, startStopwatch, stopStopwatch,
            resetStopwatch, hideStopwatch, showStopwatch, createWorkoutBtn;
    private View view;
    private ViewGroup fragmentContainer;
    private Chronometer stopwatch;
    private int currentDayIndex, maxDayIndex, numDays;
    private String currentWorkout;
    private boolean workoutModified = false, stopwatchRunning = false;
    private long lastTime;
    private MetaEntity currentWorkoutEntity;
    private WorkoutViewModel workoutModel;
    private MetaViewModel metaModel;
    private ExerciseViewModel exerciseModel;
    private HashMap<Integer, ArrayList<Exercise>> workout = new HashMap<>();
    private HashMap<String, ExerciseEntity> exerciseToExerciseEntity = new HashMap<>();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.default_layout, container, false);
        createWorkoutBtn = view.findViewById(R.id.create_workout_btn);
        createWorkoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).createWorkout();
            }
        });
        defaultTV = view.findViewById(R.id.default_text_view);
        createWorkoutBtn.setVisibility(View.INVISIBLE); // only show this button later if no workouts are found
        defaultTV.setVisibility(View.INVISIBLE); // only show this default message later if no workouts are found
        fragmentContainer = container;
        ((MainActivity) getActivity()).updateToolbarTitle(""); // empty so workout name doesn't flash once loaded
        // Set up the view models
        metaModel = ViewModelProviders.of(getActivity()).get(MetaViewModel.class);
        workoutModel = ViewModelProviders.of(getActivity()).get(WorkoutViewModel.class);
        exerciseModel = ViewModelProviders.of(getActivity()).get(ExerciseViewModel.class);
        // attempt to fetch the current workout from database
        GetCurrentWorkoutTask task = new GetCurrentWorkoutTask();
        task.execute();
        return view;
    }

    private class GetCurrentWorkoutTask extends AsyncTask<Void, Void, MetaEntity> {
        @Override
        protected void onPreExecute() {
            ((MainActivity) getActivity()).setProgressBar(true);
        }

        @Override
        protected MetaEntity doInBackground(Void... voids) {
            // get the current workout from the database
            return metaModel.getCurrentWorkoutMeta();
        }

        @Override
        protected void onPostExecute(MetaEntity result) {
            ((MainActivity) getActivity()).setProgressBar(false);
            if (result != null) {
                // database found a workout, so assign it then move to the next stop in the chain
                currentWorkoutEntity = result;
                currentWorkout = currentWorkoutEntity.getWorkoutName();
                currentDayIndex = currentWorkoutEntity.getCurrentDay();
                maxDayIndex = currentWorkoutEntity.getMaxDayIndex();
                numDays = currentWorkoutEntity.getNumDays();
                GetExercisesTask task = new GetExercisesTask();
                task.execute();
            } else {
                // no workout found, error
                createWorkoutBtn.setVisibility(View.VISIBLE);
                defaultTV.setVisibility(View.VISIBLE);
                ((MainActivity) getActivity()).updateToolbarTitle(Variables.CURRENT_WORKOUT_TITLE);
            }
        }
    }

    private class GetExercisesTask extends AsyncTask<Void, Void, ArrayList<ExerciseEntity>> {
        @Override
        protected void onPreExecute() {
            ((MainActivity) getActivity()).setProgressBar(true);
        }

        @Override
        protected ArrayList<ExerciseEntity> doInBackground(Void... voids) {
            // get the exercises from the database
            return exerciseModel.getAllExercises();
        }

        @Override
        protected void onPostExecute(ArrayList<ExerciseEntity> result) {
            ((MainActivity) getActivity()).setProgressBar(false);
            if (!result.isEmpty()) {
                for (ExerciseEntity entity : result) {
                    exerciseToExerciseEntity.put(entity.getExerciseName(), entity);
                }
                GetWorkoutTask task = new GetWorkoutTask();
                task.execute();
            } else {
                createWorkoutBtn.setVisibility(View.VISIBLE);
                defaultTV.setVisibility(View.VISIBLE);
                ((MainActivity) getActivity()).updateToolbarTitle(Variables.CURRENT_WORKOUT_TITLE);
            }
        }
    }

    private class GetWorkoutTask extends AsyncTask<Void, Void, ArrayList<WorkoutEntity>> {
        @Override
        protected void onPreExecute() {
            ((MainActivity) getActivity()).setProgressBar(true);
        }

        @Override
        protected ArrayList<WorkoutEntity> doInBackground(Void... voids) {
            // get the exercises from the database
            return workoutModel.getExercises(currentWorkout);
        }

        @Override
        protected void onPostExecute(ArrayList<WorkoutEntity> result) {
            ((MainActivity) getActivity()).setProgressBar(false);
            if (result != null) {
                // query produced a valid list, so populate it in local memory
                populateExercises(result);
            }
        }
    }

    public void populateExercises(ArrayList<WorkoutEntity> rawData) {
        /*
            Database queries complete, so switch layouts and init all the widgets
         */
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.fragment_current_workout, fragmentContainer, false);
        ViewGroup rootView = (ViewGroup) getView();
        rootView.removeAllViews();
        rootView.addView(view);
        forwardButton = view.findViewById(R.id.next_day_button);
        backButton = view.findViewById(R.id.previous_day_button);
        startStopwatch = view.findViewById(R.id.start_stopwatch);
        stopStopwatch = view.findViewById(R.id.stop_stopwatch);
        resetStopwatch = view.findViewById(R.id.reset_stopwatch);
        hideStopwatch = view.findViewById(R.id.hide_stopwatch);
        showStopwatch = view.findViewById(R.id.show_stopwatch);
        showStopwatch.setVisibility(View.INVISIBLE);
        table = view.findViewById(R.id.main_table);
        stopwatch = view.findViewById(R.id.stopwatch);
        dayTV = view.findViewById(R.id.day_text_view);
        ConstraintLayout stopwatchContainer = view.findViewById(R.id.constraint_layout);
        /*
            Get shared preferences data
         */
        SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences(Variables.SHARED_PREF_NAME, 0);
        boolean videosEnabled = pref.getBoolean(Variables.VIDEO_KEY, true);
        boolean metricUnits = pref.getBoolean(Variables.UNIT_KEY, false);
        if (pref.getBoolean(Variables.STOPWATCH_KEY, true)) {
            initStopwatch();
        } else {
            stopwatchContainer.setVisibility(View.GONE);
        }
        ((MainActivity) getActivity()).updateToolbarTitle(currentWorkout);
        // init the hash table that the entire workout will be in
        for (int i = 0; i <= maxDayIndex; i++) {
            workout.put(i, new ArrayList<Exercise>());
        }
        // fill the hash table with exercises
        for (WorkoutEntity entity : rawData) {
            String exerciseName = entity.getExercise();
            Exercise exercise = new Exercise(entity, exerciseToExerciseEntity.get(exerciseName), getContext(),
                    getActivity(), this, videosEnabled, metricUnits, workoutModel, exerciseModel);
            workout.get(entity.getDay()).add(exercise);
        }
        // sort all the days in the workout alphabetically
        for (int i = 0; i <= maxDayIndex; i++) {
            Collections.sort(workout.get(i));
        }
        populateTable();
    }

    public void populateTable() {
        /*
            Populates exercises based on the current day.
         */
        table.removeAllViews();
        dayTV.setText(WorkoutHelper.generateDayTitle(currentDayIndex, numDays));
        int count = 0;
        for (Exercise exercise : workout.get(currentDayIndex)) {
            View row = exercise.getDisplayedRow();
            table.addView(row, count);
            count++;
        }
        setupButtons();
    }

    private void setupButtons() {
        /*
            Setup back and forward buttons.
         */
        if (currentDayIndex == 0) {
            // means it's the first day, so hide the back button
            backButton.setVisibility(View.INVISIBLE);
        } else {
            backButton.setVisibility(View.VISIBLE);
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentDayIndex--;
                    currentWorkoutEntity.setCurrentDay(currentDayIndex);
                    metaModel.update(currentWorkoutEntity);
                    populateTable();
                }
            });
        }
        // set up the forward button, make it so user can always reset if holding down button
        if (currentDayIndex == maxDayIndex) {
            forwardButton.setText(getActivity().getResources().getString(R.string.reset));
            forwardButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    resetPopup();
                }
            });
        } else {
            forwardButton.setText(getActivity().getResources().getString(R.string.forward_button));
            forwardButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentDayIndex++;
                    currentWorkoutEntity.setCurrentDay(currentDayIndex);
                    metaModel.update(currentWorkoutEntity);
                    populateTable();
                }
            });
            forwardButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (workoutModified) {
                        resetPopup();
                    }
                    return true;
                }
            });
        }
    }

    public void resetWorkout() {
        /*
            Reset all of the exercises to being incomplete and then write to the database with these changes.
         */
        int exercisesCompleted = 0;
        int totalExercises = 0;
        for (int i = 0; i <= maxDayIndex; i++) {
            for (Exercise exercise : workout.get(i)) {
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
        // calculate average percentage of exercises completed over all times the workout has been completed
        int totalCompleted = currentWorkoutEntity.getCompletedSum() + exercisesCompleted;
        int totalSum = currentWorkoutEntity.getTotalSum() + totalExercises;
        double percentage = ((double) totalCompleted / (double) totalSum) * 100;

        currentDayIndex = 0;
        currentWorkoutEntity.setCurrentDay(currentDayIndex);
        currentWorkoutEntity.setTimesCompleted(currentWorkoutEntity.getTimesCompleted() + 1);
        currentWorkoutEntity.setTotalSum(totalSum);
        currentWorkoutEntity.setCompletedSum(totalCompleted);
        currentWorkoutEntity.setPercentageExercisesCompleted(percentage);
        metaModel.update(currentWorkoutEntity);
        workoutModified = false;
        populateTable();
    }

    public void resetPopup() {
        /*
            Prompt the user if they wish to reset the current workout. Only can be called if the workout has been modified.
         */
        if (!workoutModified) {
            return;
        }
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        final AlertDialog alertDialog = alertDialogBuilder.create();
        final View popupView = getLayoutInflater().inflate(R.layout.popup_reset_workout, null);
        Button confirmButton = popupView.findViewById(R.id.popup_yes);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                table.removeAllViews();
                resetWorkout();
                populateTable();
                alertDialog.dismiss();
            }
        });
        Button quitButton = popupView.findViewById(R.id.popup_no);

        quitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        alertDialog.setView(popupView);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    public void setPreviouslyModified(boolean status) {
        workoutModified = status;
    }

    // region Stopwatch methods
    public void initStopwatch() {
        startStopwatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startStopwatch();
            }
        });
        stopStopwatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopStopwatch();
            }
        });
        resetStopwatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetStopwatch();
            }
        });
        hideStopwatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideStopwatch();
            }
        });
        showStopwatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStopwatch();
            }
        });
    }

    public void startStopwatch() {
        if (!stopwatchRunning) {
            stopwatch.setBase(SystemClock.elapsedRealtime() - lastTime);
            stopwatch.start();
            stopwatchRunning = true;
        }
    }

    public void stopStopwatch() {
        if (stopwatchRunning) {
            stopwatch.stop();
            lastTime = SystemClock.elapsedRealtime() - stopwatch.getBase();
            stopwatchRunning = false;
        }
    }

    public void resetStopwatch() {
        stopwatch.setBase(SystemClock.elapsedRealtime());
        lastTime = 0;
    }

    public void hideStopwatch() {
        startStopwatch.setVisibility(View.INVISIBLE);
        stopStopwatch.setVisibility(View.INVISIBLE);
        resetStopwatch.setVisibility(View.INVISIBLE);
        hideStopwatch.setVisibility(View.INVISIBLE);
        stopStopwatch();
        resetStopwatch();
        stopwatch.setVisibility(View.INVISIBLE);
        showStopwatch.setVisibility(View.VISIBLE);
    }

    public void showStopwatch() {
        startStopwatch.setVisibility(View.VISIBLE);
        stopStopwatch.setVisibility(View.VISIBLE);
        resetStopwatch.setVisibility(View.VISIBLE);
        hideStopwatch.setVisibility(View.VISIBLE);
        stopwatch.setVisibility(View.VISIBLE);
        showStopwatch.setVisibility(View.INVISIBLE);
    }
    //endregion
}
