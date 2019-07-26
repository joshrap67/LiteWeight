package com.example.workoutmadness;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
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
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

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
    private String WORKOUT_FILE;
    private boolean modified = false, exerciseModified = false, timerRunning = false, timerEnabled, videosEnabled;
    private Chronometer timer;
    private long lastTime;
    private ConstraintLayout timerContainer;
    private HashMap<Integer, ArrayList<Exercise>> totalExercises = new HashMap<>();
    private HashMap<Integer, String> totalDayTitles = new HashMap<>();
    private HashMap<String, String> defaultExerciseVideos = new HashMap<>();
    private HashMap<String, String> customExerciseVideos = new HashMap<>();

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

        checkUserSettings();
        boolean flag1 = updateCurrentWorkoutFile();
        boolean flag2 = updateCurrentDayNumber();
        if(flag1&&flag2){
            // get the workout name and update the toolbar with the name
            String workoutName = WORKOUT_FILE.split(Variables.WORKOUT_EXT)[Variables.WORKOUT_NAME_INDEX];
            ((MainActivity)getActivity()).updateToolbarTitle(workoutName);
            if(timerEnabled){
                initTimer();
            }
            else{
                timerContainer.setVisibility(View.GONE);
            }
            getDefaultExerciseVideos();
            getCustomExerciseVideos();
            populateExercises();
            // TODO need to put error checking here in case file gets wiped.
            populateTable();
        }
        else{
            //TODO add error screen and say to create a workout
            Log.d("ERROR","Problem with the current workout log!");
        }
        return view;
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
    public void getDefaultExerciseVideos(){
        /*
            Reads the asset folder and populates a hash table with the exercise name as the key and its corresponding URL as the value
         */
        BufferedReader reader;
        try{
            reader = new BufferedReader(new InputStreamReader(getContext().getAssets().open(Variables.DEFAULT_EXERCISE_VIDEOS)));
            String line;
            while((line=reader.readLine())!=null){
                defaultExerciseVideos.put(line.split(Variables.SPLIT_DELIM)[Variables.NAME_INDEX],
                        line.split(Variables.SPLIT_DELIM)[Variables.VIDEO_INDEX]);
            }
            reader.close();
        }
        catch (Exception e){
            Log.d("ERROR","Error when trying to read default exercise videos file!\n"+e);
        }
    }

    public void getCustomExerciseVideos(){
        /*
            Reads the custom video file and populates a hash table with the exercise name as the key and its
            corresponding URL as the value. Note that any URL for a default exercise found in this file then the default
            URL will not be used.
         */
        BufferedReader reader;
        try {
            File fhandle = new File(getContext().getExternalFilesDir(Variables.USER_SETTINGS_DIRECTORY_NAME), Variables.EXERCISE_VIDEOS);
            FileReader fileR= new FileReader(fhandle);
            reader = new BufferedReader(fileR);
            String line;
            while((line=reader.readLine())!=null){
                customExerciseVideos.put(line.split(Variables.SPLIT_DELIM)[Variables.NAME_INDEX],
                        line.split(Variables.SPLIT_DELIM)[Variables.VIDEO_INDEX]);
            }
            reader.close();
        }
        catch (Exception e){
            Log.d("ERROR","Error when trying to read default exercise videos file!\n"+e);
        }
    }

    public void populateExercises(){
        /*
            Reads the file and populates the hash map with the exercises. This allows for memory to be utilized
            instead of disk and makes the entire process of switching days a lot more elegant.
         */
        BufferedReader reader = null;
        int hashIndex = -1;
        try{
            // progress through the file until a day  is found. Once found, populate with exercises
            File fhandle = new File(getContext().getExternalFilesDir(Variables.WORKOUT_DIRECTORY), WORKOUT_FILE);
            FileReader fileR= new FileReader(fhandle);
            reader = new BufferedReader(fileR);
            String line;
            while((line=reader.readLine())!=null){
                boolean day = isDay(line); // possible day, if it is null then it is not at the current day specified in file
                if(day){
                    hashIndex++;
                    totalExercises.put(hashIndex, new ArrayList<Exercise>());
                    totalDayTitles.put(hashIndex, line.split(Variables.SPLIT_DELIM)[Variables.TIME_TITLE_INDEX]); // add day
                }
                else{
                    String name = line.split(Variables.SPLIT_DELIM)[Variables.NAME_INDEX];
                    String URL;
                    if(customExerciseVideos.get(name)!=null){
                        URL=customExerciseVideos.get(name);
                    }
                    else if(defaultExerciseVideos.get(name)!=null){
                        URL=defaultExerciseVideos.get(name);
                    }
                    else{
                        URL="NONE";
                    }
                    Exercise exercise = new Exercise(line.split(Variables.SPLIT_DELIM),getContext(),getActivity(),this, videosEnabled,URL);
                    totalExercises.get(hashIndex).add(exercise);
                }
            }
            reader.close();
        }
        catch (Exception e){
            Log.d("ERROR","Error when trying to read workout file!\n"+e);
        }
        maxDayIndex = hashIndex;
    }

    public void populateTable(){
        /*
            Populates exercises based on the current day.
         */
        table.removeAllViews();
        dayTV.setText(totalDayTitles.get(currentDayIndex));
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

    private boolean updateCurrentWorkoutFile(){
        /*
            This method ensures that when the app is closed and re-opened, it will pick up where the user
            last left off. It looks into the currentWorkout log file and updates the workout file variable to
            match the workout found in the log.
         */
        BufferedReader reader = null;
        try{
            File fhandle = new File(getContext().getExternalFilesDir(Variables.WORKOUT_DIRECTORY), Variables.CURRENT_WORKOUT_LOG);
            FileReader fileR= new FileReader(fhandle);
            reader = new BufferedReader(fileR);
            WORKOUT_FILE = reader.readLine().split(Variables.SPLIT_DELIM)[Variables.WORKOUT_NAME_INDEX];
            reader.close();
            return true;
        }
        catch (Exception e){
            Log.d("ERROR","Error when trying to read current workout file!\n"+e);
            return false;
        }
    }

    private boolean updateCurrentDayNumber(){
        /*
            This method ensures that when the app is closed and re-opened, it will pick up where the user
            last left off. It looks into the currentWorkout log file and updates the current day index to
            match the index found in the log.
         */
        BufferedReader reader = null;
        try{
            File fhandle = new File(getContext().getExternalFilesDir(Variables.WORKOUT_DIRECTORY), Variables.CURRENT_WORKOUT_LOG);
            FileReader fileR= new FileReader(fhandle);
            reader = new BufferedReader(fileR);
            currentDayIndex = Integer.parseInt(reader.readLine().split(Variables.SPLIT_DELIM)[Variables.CURRENT_DAY_INDEX]);
            reader.close();
            return true;
        }
        catch (Exception e){
            Log.d("ERROR","Error when trying to read current workout log!\n"+e);
            return false;
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
