package com.example.workoutmadness.Fragments;

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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TableLayout;
import android.widget.TextView;

import com.example.workoutmadness.Database.Entities.*;
import com.example.workoutmadness.Database.ViewModels.*;
import com.example.workoutmadness.Exercise;
import com.example.workoutmadness.MainActivity;
import com.example.workoutmadness.R;
import com.example.workoutmadness.Variables;

import java.util.ArrayList;
import java.util.HashMap;

public class CurrentWorkoutFragment extends Fragment {
    private TextView dayTV, defaultTV;
    private TableLayout table;
    private Button forwardButton, backButton, startTimer, stopTimer, resetTimer, hideTimer, showTimer;
    private int currentDayIndex, maxDayIndex;
    private String currentWorkout;
    private MetaEntity currentWorkoutEntity;
    private boolean modified = false, exerciseModified = false, timerRunning = false;
    private Chronometer timer;
    private long lastTime;
    private WorkoutViewModel workoutModel;
    private MetaViewModel metaModel;
    private ExerciseViewModel exerciseModel;
    private HashMap<Integer, ArrayList<Exercise>> totalExercises = new HashMap<>();
    private HashMap<String, ExerciseEntity> exerciseToExerciseEntity = new HashMap<>();
    private View view;
    private ViewGroup fragmentContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.default_layout,container,false);
        defaultTV = view.findViewById(R.id.default_tv);
        defaultTV.setVisibility(View.INVISIBLE); // only show this default message later if no workouts are found
        fragmentContainer = container;
        ((MainActivity)getActivity()).updateToolbarTitle("");
        // Set up the view models
        metaModel = ViewModelProviders.of(getActivity()).get(MetaViewModel.class);
        workoutModel = ViewModelProviders.of(getActivity()).get(WorkoutViewModel.class);
        exerciseModel = ViewModelProviders.of(getActivity()).get(ExerciseViewModel.class);
        // attempt to fetch the current workout from database
        GetCurrentWorkoutTask task = new GetCurrentWorkoutTask();
        task.execute();
        return view;
    }

    private class GetCurrentWorkoutTask extends AsyncTask<Void, Void, MetaEntity>{
        @Override
        protected void onPreExecute(){
            ((MainActivity)getActivity()).setProgressBar(true);
        }

        @Override
        protected MetaEntity doInBackground(Void... voids) {
            // get the current workout from the database
            return metaModel.getCurrentWorkoutMeta();
        }

        @Override
        protected void onPostExecute(MetaEntity result) {
            ((MainActivity)getActivity()).setProgressBar(false);
            if(result!=null) {
                // database found a workout, so assign it then move to the next stop in the chain
                currentWorkoutEntity = result;
                currentWorkout = currentWorkoutEntity.getWorkoutName();
                currentDayIndex = currentWorkoutEntity.getCurrentDay();
                maxDayIndex = currentWorkoutEntity.getTotalDays();
                Log.d("TAG","CurrentWorkout: "+currentWorkoutEntity.toString());
                getVideos();
            }
            else{
                // no workout found,error
                defaultTV.setVisibility(View.VISIBLE);
                ((MainActivity)getActivity()).updateToolbarTitle("Current Workout");
                Log.d("TAG","Get current workout result was null!");
            }
        }
    }
    public void getVideos(){
        GetVideosTask task = new GetVideosTask();
        task.execute();
    }

    private class GetVideosTask extends AsyncTask<Void, Void, ArrayList<ExerciseEntity>>{
        @Override
        protected void onPreExecute(){
            ((MainActivity)getActivity()).setProgressBar(true);
        }

        @Override
        protected ArrayList<ExerciseEntity> doInBackground(Void... voids) {
            // get the exercises from the database
            return exerciseModel.getAllExercises();
        }

        @Override
        protected void onPostExecute(ArrayList<ExerciseEntity> result) {
            ((MainActivity)getActivity()).setProgressBar(false);
            if(!result.isEmpty()){
                for(ExerciseEntity entity : result){
                    exerciseToExerciseEntity.put(entity.getExerciseName(),entity);
                }
                getExercises();
            }
            else{
                Log.d("TAG","Get exercises result was null!");
            }
        }
    }

    public void getExercises(){
        GetExercisesTask task = new GetExercisesTask();
        task.execute();
    }

    private class GetExercisesTask extends AsyncTask<Void, Void, ArrayList<WorkoutEntity>>{
        @Override
        protected void onPreExecute(){
            ((MainActivity)getActivity()).setProgressBar(true);
        }

        @Override
        protected ArrayList<WorkoutEntity> doInBackground(Void... voids) {
            // get the exercises from the database
            return workoutModel.getExercises(currentWorkout);
        }

        @Override
        protected void onPostExecute(ArrayList<WorkoutEntity> result) {
            ((MainActivity)getActivity()).setProgressBar(false);
            if(result != null){
                // query produced a valid list, so populate it in local memory
                populateExercises(result);
            }
            else{
                Log.d("TAG","Get exercises result was null!");
            }
        }
    }

    public void populateExercises(ArrayList<WorkoutEntity> rawData){
        /*
            Database queries complete, so switch layouts and init all the widgets
         */
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.fragment_workout, fragmentContainer,false);
        ViewGroup rootView = (ViewGroup) getView();
        rootView.removeAllViews();
        rootView.addView(view);
        forwardButton = view.findViewById(R.id.forwardButton);
        backButton = view.findViewById(R.id.previousDayButton);
        startTimer = view.findViewById(R.id.start_timer);
        stopTimer = view.findViewById(R.id.stop_timer);
        resetTimer = view.findViewById(R.id.reset_timer);
        hideTimer = view.findViewById(R.id.hide_timer);
        showTimer = view.findViewById(R.id.show_timer);
        showTimer.setVisibility(View.INVISIBLE);
        table = view.findViewById(R.id.main_table);
        timer = view.findViewById(R.id.timer);
        dayTV = view.findViewById(R.id.dayTextView);
        ConstraintLayout timerContainer = view.findViewById(R.id.constraint_layout);
        /*
            Get shared preferences data
         */
        SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences(Variables.SHARED_PREF_NAME, 0);
        boolean videosEnabled = pref.getBoolean(Variables.VIDEO_KEY,true);
        boolean metricUnits = pref.getBoolean(Variables.UNIT_KEY,false);
        if(pref.getBoolean(Variables.TIMER_KEY,true)){
            initTimer();
        }
        else{
            timerContainer.setVisibility(View.GONE);
        }

        Log.d("TAG","Rawdata size: "+rawData.size());
        ((MainActivity)getActivity()).updateToolbarTitle(currentWorkout);
        // TODO handle case where custom exercise is deleted but it still is in a workout
        // TODO handle concurrency problems? since will be trying to pull video from the exercise table
        // init the hash table
        for(int i = 0;i<=maxDayIndex;i++){
            totalExercises.put(i, new ArrayList<Exercise>());
        }
        // fill the hash table with exercises
        for(WorkoutEntity entity : rawData){
            String exerciseName = entity.getExercise();
            Exercise exercise = new Exercise(entity,exerciseToExerciseEntity.get(exerciseName),getContext(),
                    getActivity(),this,videosEnabled,metricUnits,
                    exerciseToExerciseEntity.get(exerciseName).getCurrentWeight(), workoutModel,exerciseModel);
            totalExercises.get(entity.getDay()).add(exercise);
        }
        populateTable();
    }

    public void populateTable(){
        /*
            Populates exercises based on the current day.
         */
        table.removeAllViews();
        dayTV.setText(Variables.generateDayTitle(currentDayIndex, maxDayIndex));
        int count = 0;
        for(Exercise exercise : totalExercises.get(currentDayIndex)){
            View row = exercise.getDisplayedRow();
            table.addView(row,count);
            count++;
        }
        setupButtons();
    }

    private void setupButtons(){
        /*
            Setup back and forward buttons.
         */
        if(currentDayIndex==0){
            // means it's the first day, so hide the back button
            backButton.setVisibility(View.INVISIBLE);
        }
        else{
            backButton.setVisibility(View.VISIBLE);
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentDayIndex--;
                    currentWorkoutEntity.setCurrentDay(currentDayIndex);
                    metaModel.update(currentWorkoutEntity);
                    modified=true; // modified since changed day
                    populateTable();
                }
            });
        }
        // set up the forward button, make it so user can always reset if holding down button
        if(currentDayIndex==maxDayIndex){
            forwardButton.setText("Reset");
            forwardButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    resetPopup();
                }
            });

        }
        else{
            forwardButton.setText("Next");
            forwardButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentDayIndex++;
                    currentWorkoutEntity.setCurrentDay(currentDayIndex);
                    metaModel.update(currentWorkoutEntity);
                    modified=true;
                    populateTable();
                }
            });
            forwardButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(exerciseModified){
                        resetPopup();
                    }
                    return true;
                }
            });
        }
    }

    public void resetWorkout(){
        /*
            Reset all of the exercises to being incomplete and then write to the workout file with these changes.
         */
        for(int i=0;i<=maxDayIndex;i++){
            for(Exercise exercise : totalExercises.get(i)){
                exercise.setStatus(false);
                workoutModel.update(exercise.getWorkoutEntity());
                // TODO would do any statistics stuff here
            }
        }
        currentDayIndex = 0;
        currentWorkoutEntity.setCurrentDay(currentDayIndex);
        metaModel.update(currentWorkoutEntity);
        modified = true;
        exerciseModified = false;
        populateTable();
    }

    public void resetPopup(){
        /*
            Prompt the user if they wish to reset the current workout. Only can be called if the workout has been modified.
         */
        if(!exerciseModified && !modified){
            return;
        }
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        final AlertDialog alertDialog = alertDialogBuilder.create();
        final View popupView = getLayoutInflater().inflate(R.layout.reset_popup, null);
        Button confirmButton = popupView.findViewById(R.id.popupYes);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                table.removeAllViews();
                resetWorkout();
                populateTable();
                alertDialog.dismiss();
            }
        });
        Button quitButton = popupView.findViewById(R.id.popupNo);

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

    public boolean isModified(){
        /*
            TODO remove from this database version
         */
        return modified;
    }

    public void setModified(boolean status){
        modified = status;
    }

    public void setPreviouslyModified(boolean status){
        exerciseModified = status;
    }
    // region
    // Timer methods
    public void initTimer(){
        startTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTimer();
            }
        });
        stopTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTimer();
            }
        });
        resetTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTimer();
            }
        });
        hideTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideTimer();
            }
        });
        showTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimer();
            }
        });


    }
    public void startTimer(){
        if(!timerRunning){
            // TODO timer is reset when app goes into onpause
            timer.setBase(SystemClock.elapsedRealtime()-lastTime);
            timer.start();
            timerRunning=true;
        }
    }

    public void stopTimer(){
        if(timerRunning){
            timer.stop();
            lastTime=SystemClock.elapsedRealtime()-timer.getBase();
            timerRunning=false;
        }
    }

    public void resetTimer(){
        timer.setBase(SystemClock.elapsedRealtime());
        lastTime=0;
    }

    public void hideTimer(){
        startTimer.setVisibility(View.INVISIBLE);
        stopTimer.setVisibility(View.INVISIBLE);
        resetTimer.setVisibility(View.INVISIBLE);
        hideTimer.setVisibility(View.INVISIBLE);
        stopTimer();
        resetTimer();
        timer.setVisibility(View.INVISIBLE);
        showTimer.setVisibility(View.VISIBLE);
    }

    public void showTimer(){
        startTimer.setVisibility(View.VISIBLE);
        stopTimer.setVisibility(View.VISIBLE);
        resetTimer.setVisibility(View.VISIBLE);
        hideTimer.setVisibility(View.VISIBLE);
        timer.setVisibility(View.VISIBLE);
        showTimer.setVisibility(View.INVISIBLE);
    }
    //endregion
}
