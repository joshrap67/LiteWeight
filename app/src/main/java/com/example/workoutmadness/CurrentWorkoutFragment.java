package com.example.workoutmadness;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import java.util.ArrayList;
import java.util.HashMap;

public class CurrentWorkoutFragment extends Fragment {
    private View view;
    private TextView dayTV;
    private TableLayout table;
    private Button forwardButton, backButton, startTimer, stopTimer, resetTimer, hideTimer, showTimer;
    private int currentDayIndex, maxDayIndex;
    private static final int TIME_INDEX = 0, TIME_TITLE_INDEX = 1, NAME_INDEX = 0, STATUS_INDEX = 1, VIDEO_INDEX = 2,
            WORKOUT_NAME_INDEX = 0, CURRENT_DAY_INDEX = 1;
    private String SPLIT_DELIM ="\\*", DAY_DELIM="TIME", EXERCISE_COMPLETE ="COMPLETE", EXERCISE_INCOMPLETE = "INCOMPLETE",
            WORKOUT_FILE= "Josh's Workout.txt",
            CURRENT_WORKOUT_LOG, WORKOUT_DIRECTORY_NAME;
    private boolean modified = false, exerciseModified = false, timerRunning = false;
    private Chronometer timer;
    private long lastTime;
    private HashMap<Integer, ArrayList<Exercise>> totalExercises = new HashMap<>();
    private HashMap<Integer, String> totalDayTitles = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_workout,container,false);
        // init all the views
        forwardButton = view.findViewById(R.id.forwardButton);
        backButton = view.findViewById(R.id.previousDayButton);
        startTimer=view.findViewById(R.id.start_timer);
        stopTimer=view.findViewById(R.id.stop_timer);
        resetTimer=view.findViewById(R.id.reset_timer);
        hideTimer=view.findViewById(R.id.hide_timer);
        showTimer=view.findViewById(R.id.show_timer);
        showTimer.setVisibility(View.INVISIBLE);
        table = view.findViewById(R.id.main_table);
        timer = view.findViewById(R.id.timer);
        dayTV = view.findViewById(R.id.dayTextView);
        // get workout file location information
        CURRENT_WORKOUT_LOG = ((MainActivity)getActivity()).getWorkoutLogName();
        WORKOUT_DIRECTORY_NAME =((MainActivity)getActivity()).getWorkoutDirectoryName();

        boolean flag1 = updateCurrentWorkoutFile();
        boolean flag2 = updateCurrentDayNumber();
        // TODO change format of workout log. Have each line correspond to the different workouts in the directory
        if(flag1&&flag2){
            // get the workout name and update the toolbar with the name
            String[] workoutFile = WORKOUT_FILE.split(".txt");
            String workoutName = workoutFile[0];
            ((MainActivity)getActivity()).updateToolbarTitle(workoutName);
            initTimer();
            populateExercises();
            populateTable();
        }
        else{
            //TODO add error screen and say to create a workout
            Log.d("ERROR","Problem with the current workout log!");
        }
        return view;
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
            File fhandle = new File(getContext().getExternalFilesDir(WORKOUT_DIRECTORY_NAME), WORKOUT_FILE);
            FileReader fileR= new FileReader(fhandle);
            reader = new BufferedReader(fileR);
            String line;
            while((line=reader.readLine())!=null){
                boolean day = isDay(line); // possible day, if it is null then it is not at the current day specified in file
                if(day){
                    hashIndex++;
                    totalExercises.put(hashIndex, new ArrayList<Exercise>());
                    totalDayTitles.put(hashIndex, line.split(SPLIT_DELIM)[TIME_TITLE_INDEX]); // add day
                }
                else{
                    totalExercises.get(hashIndex).add(new Exercise(line.split(SPLIT_DELIM)));
                }
            }
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
            TableRow row = exercise.getDisplayedRow();
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
            forwardButton.setText("RESET");
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
            File fhandle = new File(getContext().getExternalFilesDir(WORKOUT_DIRECTORY_NAME), CURRENT_WORKOUT_LOG);
            FileReader fileR= new FileReader(fhandle);
            reader = new BufferedReader(fileR);
            WORKOUT_FILE = reader.readLine().split(SPLIT_DELIM)[WORKOUT_NAME_INDEX];
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
            File fhandle = new File(getContext().getExternalFilesDir(WORKOUT_DIRECTORY_NAME), CURRENT_WORKOUT_LOG);
            FileReader fileR= new FileReader(fhandle);
            reader = new BufferedReader(fileR);
            currentDayIndex = Integer.parseInt(reader.readLine().split(SPLIT_DELIM)[CURRENT_DAY_INDEX]);
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
        String[] strings = data.split(SPLIT_DELIM);
        String delim = strings[TIME_INDEX];
        if(delim.equalsIgnoreCase(DAY_DELIM)){
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
        try{
            File fhandle = new File(getContext().getExternalFilesDir(WORKOUT_DIRECTORY_NAME), CURRENT_WORKOUT_LOG);
            BufferedWriter writer = new BufferedWriter(new FileWriter(fhandle,false));
            writer.write(_data);
            writer.close();
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
        File fhandle = new File(getContext().getExternalFilesDir(WORKOUT_DIRECTORY_NAME), WORKOUT_FILE);
        try{
            writer = new BufferedWriter(new FileWriter(fhandle,false));
            for(int i=0;i<=maxDayIndex;i++){
                String dayData = DAY_DELIM+"*"+totalDayTitles.get(i);
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

    private class Exercise{
        private String name;
        private String videoURL;
        private boolean status;
        private TableRow displayedRow;

        private Exercise(final String[] rawText){
            if(rawText[STATUS_INDEX].equals(EXERCISE_COMPLETE)){
                // means that the exercise has already been done, so make sure to set status as so
                exerciseModified=true;
                status=true;
            }
            else{
                status=false;
            }
            name=rawText[NAME_INDEX];
            videoURL=rawText[VIDEO_INDEX];
        }
        private void setStatus(boolean aStatus){
            /*
                Sets the status of the exercise as either being complete or incomplete.
             */
            status=aStatus;
        }

        private TableRow getDisplayedRow(){
            /*
                Takes all of the information from the instance variables of this exercise and puts it into a row to be displayed
                by the main table.
             */
            displayedRow = new TableRow(getActivity());
            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
            displayedRow.setLayoutParams(lp);

            final CheckBox exercise = new CheckBox(getActivity());
            if(status){
                exercise.setChecked(true);
            }
            exercise.setOnClickListener(new View.OnClickListener() {
                boolean checked = exercise.isChecked();

                @Override
                public void onClick(View v) {
                    if(checked){
                        status=false;
                    }
                    else{
                        status=true;
                    }
                    modified=true;
                    exerciseModified=true;
                }
            });
            exercise.setText(name);
            displayedRow.addView(exercise);

            Button videoButton = new Button(getActivity());
            videoButton.setText("Video");
            videoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!videoURL.equalsIgnoreCase("none")){
                        // found on SO
                        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoURL));
                        Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoURL));
                        try{
                            getContext().startActivity(appIntent);
                        }
                        catch(ActivityNotFoundException ex) {
                            getContext().startActivity(webIntent);
                        }
                    }
                    else{
                        Toast.makeText(getActivity(), "No video found", Toast.LENGTH_LONG).show();
                    }
                }
            });
            displayedRow.addView(videoButton);
            return displayedRow;
        }

        private String getFormattedLine(){
            /*
                Utilized whenever writing to a file. This method formats the information of the exercise
                instance into the proper format specified in this project.
             */
            String retVal;
            if(status){
                retVal = name+"*"+EXERCISE_COMPLETE+"*"+videoURL;
            }
            else{
                retVal = name+"*"+EXERCISE_INCOMPLETE+"*"+videoURL;
            }
            return retVal;
        }
    }
}
