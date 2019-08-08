package com.example.workoutmadness.Fragments;

import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class CurrentWorkoutFragment extends Fragment {
    private TextView dayTV;
    private TableLayout table;
    private Button forwardButton, backButton, startTimer, stopTimer, resetTimer, hideTimer, showTimer;
    private int currentDayIndex, maxDayIndex;
    private String WORKOUT_FILE, currentWorkout;
    private MetaEntity currentWorkoutEntity;
    private boolean modified = false, exerciseModified = false, timerRunning = false, timerEnabled, videosEnabled, firstTime = true;
    private Chronometer timer;
    private long lastTime;
    private ConstraintLayout timerContainer;
    private WorkoutViewModel workoutModel;
    private MetaViewModel metaModel;
    private HashMap<Integer, ArrayList<Exercise>> totalExercises = new HashMap<>();
    private HashMap<Integer, String> totalDayTitles = new HashMap<>();
    private HashMap<String, String> defaultExerciseVideos = new HashMap<>();
    private HashMap<String, String> customExerciseVideos = new HashMap<>();
    private ArrayList<WorkoutEntity> entities;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workout,container,false);
        // init all the views
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
        timerContainer = view.findViewById(R.id.constraint_layout);
        entities = new ArrayList<>();

        /*
            Get shared preferences data
         */
        SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences(Variables.SHARED_PREF_NAME, 0); // 0 - for private mode
        videosEnabled = pref.getBoolean(Variables.VIDEO_KEY,true);
        /*
            Set up the view models
         */
        metaModel = ViewModelProviders.of(getActivity()).get(MetaViewModel.class);
        workoutModel = ViewModelProviders.of(getActivity()).get(WorkoutViewModel.class);

        GetCurrentWorkoutTask task = new GetCurrentWorkoutTask();
        task.execute();
        return view;
    }

    private class GetCurrentWorkoutTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            // get the current workout from the database
            metaModel.getCurrentWorkoutMeta();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if(metaModel.getCurrentWorkoutMetaResult()!=null) {
                // database found a workout, so assign it then move to the next stop in the chain
                currentWorkoutEntity = metaModel.getCurrentWorkoutMetaResult();
                currentWorkout = currentWorkoutEntity.getWorkoutName();
                currentDayIndex = currentWorkoutEntity.getCurrentDay();
                maxDayIndex = currentWorkoutEntity.getTotalDays();
                Log.d("TAG","CurrentWorkout: "+currentWorkoutEntity.toString());
                getExercises();
            }
            else{
                // no workout found,error
                Log.d("TAG","Get current workout result was null!");
            }
        }
    }

    public void getExercises(){
        GetExercisesTask task = new GetExercisesTask();
        task.execute();
    }

    private class GetExercisesTask extends AsyncTask<Void, Void, ArrayList<WorkoutEntity>>{

        @Override
        protected ArrayList<WorkoutEntity> doInBackground(Void... voids) {
            // get the exercises from the database
            return workoutModel.getExercises(currentWorkout);
        }

        @Override
        protected void onPostExecute(ArrayList<WorkoutEntity> result) {
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
        Log.d("TAG","Rawdata size: "+rawData.size());
        ((MainActivity)getActivity()).updateToolbarTitle(currentWorkout);
        // TODO handle case where custom exercise is deleted but it still is in a workout (
        //  since will be trying to pull video from the exercise table
        // init the hash table
        for(int i = 0;i<=maxDayIndex;i++){
            totalExercises.put(i, new ArrayList<Exercise>());
        }
//        if(customExerciseVideos.get(name)!=null){
//            URL=customExerciseVideos.get(name);
//        }
//        else if(defaultExerciseVideos.get(name)!=null){
//            URL=defaultExerciseVideos.get(name);
//        }
//        else{
//            URL="NONE";
//        }
        for(WorkoutEntity entity : rawData){
            Exercise exercise = new Exercise(entity,getContext(),getActivity(),this,videosEnabled,"hey",workoutModel);
            totalExercises.get(entity.getDay()).add(exercise);
        }
        populateTable();
    }

    public String generateDayTitle(int num){
        int weekNum = (num / (maxDayIndex+1))+1;
        int dayNum = (num % (maxDayIndex+1))+1;
        return "W"+weekNum+":D"+dayNum;
    }

    public void checkUserSettings(){
        BufferedReader reader;
        try{
            // check if videos and timer are enabled from user settings
            File fhandle = new File(getContext().getExternalFilesDir(Variables.USER_SETTINGS_DIRECTORY_NAME), Variables.USER_SETTINGS_FILE);
            if(fhandle.length()==0){
                // settings fragment has somehow never been touched, so just show them the damn videos
                timerEnabled = videosEnabled = true;
            }
            FileReader fileR= new FileReader(fhandle);
            reader = new BufferedReader(fileR);
            String line;
            while((line=reader.readLine())!=null){
                String val = line.split(Variables.SPLIT_DELIM)[Variables.SETTINGS_INDEX];
                if(val.equals(Variables.TIMER_DELIM)){
                    timerEnabled = Boolean.parseBoolean(line.split(Variables.SPLIT_DELIM)[Variables.SETTINGS_VALUE_INDEX]);
                }
                else if(val.equals(Variables.VIDEO_DELIM)){
                    videosEnabled = Boolean.parseBoolean(line.split(Variables.SPLIT_DELIM)[Variables.SETTINGS_VALUE_INDEX]);
                }
            }
            reader.close();
        }
        catch (Exception e){
            Log.d("ERROR","Error when trying to read user settings file!\n"+e);
        }
    }

    public void populateTable(){
        /*
            Populates exercises based on the current day.
         */
        table.removeAllViews();
        dayTV.setText(generateDayTitle(currentDayIndex));
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

    public boolean isDay(String data){
        /*
            Checks if passed in string from file denotes a day or an exercise
         */
        if(data==null){
            return false;
        }
        String[] strings = data.split(Variables.SPLIT_DELIM);
        String delim = strings[Variables.TIME_INDEX];
        if(delim.equalsIgnoreCase(Variables.DAY_DELIM)){
            // found a line that represents a day
            return true;
        }
        // not a day but an exercise, so return false;
        return false;
    }

    public void recordToCurrentWorkoutLog(){
        /*
            Is called whenever the user either switches to another fragment, or exits the application. Saves the state of the
            current workout.
         */
        String _data = WORKOUT_FILE+"*"+currentDayIndex;
        BufferedReader reader = null;
        BufferedWriter writer = null;
        File fhandleOld = new File(getContext().getExternalFilesDir(Variables.WORKOUT_DIRECTORY), Variables.CURRENT_WORKOUT_LOG);
        File fhandleNew = new File(getContext().getExternalFilesDir(Variables.WORKOUT_DIRECTORY), "temp");
        try{
            // progress through the file until the correct spot is found
            writer = new BufferedWriter(new FileWriter(fhandleNew,true));
            FileReader fileR= new FileReader(fhandleOld);
            reader = new BufferedReader(fileR);
            String line;
            writer.write(_data+"\n"); // put the current workout at top of file
            reader.readLine(); // skip over first line when copying over from previous log since we just updated in the line above
            while(((line=reader.readLine())!=null)){
                writer.write(line+"\n");
            }
            reader.close();
            writer.close();
            fhandleOld.delete();
            fhandleNew.renameTo(fhandleOld);
        }
        catch (Exception e){
            Log.d("ERROR","Error when trying to write to current workout log!\n"+e);
        }

    }

    public void recordToWorkoutFile(){
        /*
            Updates the workout file to include the changes that were made by the user. This
            is called whenever the user clicks to go to another day or exits out of the fragment.
         */
        BufferedWriter writer = null;
        File fhandle = new File(getContext().getExternalFilesDir(Variables.WORKOUT_DIRECTORY), WORKOUT_FILE);
        try{
            writer = new BufferedWriter(new FileWriter(fhandle,false));
            for(int i=0;i<=maxDayIndex;i++){
                String dayData = Variables.DAY_DELIM+"*"+totalDayTitles.get(i);
                writer.write(dayData+"\n");
                for(Exercise exercise : totalExercises.get(i)){
                    String data = exercise.getFormattedLine();
                    writer.write(data+"\n");
                }
            }
            writer.close();
        }
        catch (Exception e){
            Log.d("ERROR","Error when trying to record to workout file!\n"+e);
        }
    }

    public void resetWorkout(){
        /*
            Reset all of the exercises to being incomplete and then write to the workout file with these changes.
         */
        for(int i=0;i<=maxDayIndex;i++){
            for(Exercise exercise : totalExercises.get(i)){
                exercise.setStatus(false);
            }
        }
        recordToWorkoutFile();
        currentDayIndex=0;
        currentWorkoutEntity.setCurrentDay(currentDayIndex);
        metaModel.update(currentWorkoutEntity);
        modified=true;
        exerciseModified=false;
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
            Is used to check if the user has made any at all changes to their workout. If so, appropriate
            action (namely altering the text file) must be taken.
         */
        return modified;
    }

    public void setModified(boolean status){
        modified=status;
    }

    public void setPreviouslyModified(boolean status){
        exerciseModified=status;
    }

    /*
        ***********************************
        Setting up all of the timer methods
        ***********************************
     */
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
}
