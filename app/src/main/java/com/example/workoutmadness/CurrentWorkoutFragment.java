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

public class CurrentWorkoutFragment extends Fragment {
    private View view;
    private TextView dayTV;
    private TableLayout table;
    private Button forwardButton, backButton, startTimer, stopTimer, resetTimer, hideTimer;
    private int currentDayNum, arrayListIndex;
    private String currentLine, SPLIT_DELIM ="\\*", END_DAY_DELIM="END DAY", END_CYCLE_DELIM="END", START_CYCLE_DELIM="START",
            DAY_DELIM="TIME", WORKOUT_FILE= "Josh's Workout.txt", EXERCISE_DONE="DONE",
            CURRENT_WORKOUT_LOG, DIRECTORY_NAME;
    private boolean modified=false, lastDay=false, firstDay=false, exerciseModified=false, timerRunning=false;
    private ArrayList<String> exercises;
    private Chronometer timer;
    private long lastTime;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_workout,container,false);
        forwardButton = view.findViewById(R.id.forwardButton);
        backButton = view.findViewById(R.id.previousDayButton);
        startTimer=view.findViewById(R.id.start_timer);
        stopTimer=view.findViewById(R.id.stop_timer);
        resetTimer=view.findViewById(R.id.reset_timer);
        hideTimer=view.findViewById(R.id.hide_timer);
        table = view.findViewById(R.id.main_table);
        timer = view.findViewById(R.id.timer);

        CURRENT_WORKOUT_LOG = ((MainActivity)getActivity()).getWorkoutLogName();
        DIRECTORY_NAME=((MainActivity)getActivity()).getDirectoryName();
        exercises = new ArrayList<String>();

        getCurrentWorkout();
        getCurrentDayNumber();
        if(currentDayNum!=-1){
            // get the workout name and update the toolbar with the name
            String[] workoutFile = WORKOUT_FILE.split(".txt");
            String workoutName = workoutFile[0];
            ((MainActivity)getActivity()).updateToolbarTitle(workoutName);
            initTimer();

            populateWorkouts();
        }
        else{
            //TODO add error screen
        }
        return view;
    }

    public void populateWorkouts(){
        /*
        Populates exercises based on the currently selected workout.
         */
        exerciseModified=false;
        exercises.clear();
        arrayListIndex=0;
        BufferedReader reader = null;
        try{
            // progress through the file until the correct spot is found
            File fhandle = new File(getContext().getExternalFilesDir(DIRECTORY_NAME), WORKOUT_FILE);
            FileReader fileR= new FileReader(fhandle);
            reader = new BufferedReader(fileR);
            String line;
            while((line=reader.readLine())!=null){
                String day = findDay(line); // possible day, if it is null then it is not at the current day specified in file
                if(day!=null){
                    dayTV = view.findViewById(R.id.dayTextView);
                    dayTV.setText(day);
                    break;
                }
            }
            setupButtons();
            // Now, loop through and populate the scroll view with all the exercises in this day
            int count =0;
            while(!(currentLine=reader.readLine()).equalsIgnoreCase(END_DAY_DELIM)){
                exercises.add(currentLine);
                final String[] strings = currentLine.split(SPLIT_DELIM);
                TableRow row = new TableRow(getActivity());
                TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
                row.setLayoutParams(lp);

                final CheckBox exercise = new CheckBox(getActivity());
                if(strings.length==3){
                    // means that the workout has already been done, so make sure to check the checkbox
                    exercise.setChecked(true);
                }
                exercise.setOnClickListener(new View.OnClickListener() {
                    int index = arrayListIndex;
                    String line = currentLine;
                    boolean checked = exercise.isChecked();

                    @Override
                    public void onClick(View v) {
                        updateExercise(index, line, checked);
                        modified=true;
                        exerciseModified=true;
                    }
                });
                exercise.setText(strings[0]);
                row.addView(exercise);

                Button videoButton = new Button(getActivity());
                videoButton.setText("Video");
                videoButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(strings.length>=2){
                            String URL = strings[1];
                            if(!URL.equalsIgnoreCase("none")){
                                // found on SO
                                Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL));
                                Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL));
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

                    }
                });
                row.addView(videoButton);
                table.addView(row,count);
                arrayListIndex++;
                count++;
            }
            reader.close();
        }
        catch (Exception e){
            Log.d("ERROR","Error when trying to read workout file!"+e);
        }
    }

    private void setupButtons(){
        // setup back button
        if(firstDay){
            backButton.setVisibility(View.INVISIBLE);
        }
        else{
            backButton.setVisibility(View.VISIBLE);
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(exerciseModified){
                        // if any exercise status was altered, write to file before switching to previous day
                        recordToWorkoutFile();
                    }
                    currentDayNum--;
                    modified=true; // modified since changed day
                    table.removeAllViews();
                    lastDay=false;
                    populateWorkouts();
                }
            });
        }
        // set up the forward button, make it so user can always reset if holding down button
        if(lastDay){
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
                    if(exerciseModified){
                        // if any exercise was checked off as completed, write to file before switching to next day
                        recordToWorkoutFile();
                    }
                    currentDayNum++;
                    modified=true;
                    table.removeAllViews();
                    firstDay=false;
                    populateWorkouts();
                }
            });
            forwardButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    resetPopup();
                    return true;
                }
            });
        }
    }

    private String findDay(String _data){
        /*
            This method is used to parse a line of the workout text file. It splits the line on the given
            delimiter and then returns that title of the day.
         */
        if(_data==null){
            return null;
        }
        String[] strings = _data.split(SPLIT_DELIM);
        String delim = strings[0];
        if(delim.equalsIgnoreCase(DAY_DELIM)){
            // found a line that represents a day, see if it's the day we are indeed looking for by using the day number
            if(Integer.parseInt(strings[2])==currentDayNum){
                // indeed the correct day number, now check if it is a last or first day before returning
                if(strings.length==4){
                    if(strings[3].equalsIgnoreCase(START_CYCLE_DELIM)){
                        firstDay = true;
                    }
                    else if(strings[3].equalsIgnoreCase(END_CYCLE_DELIM)){
                        lastDay = true;
                    }
                }
                // return the title of said day, not the day number
                return strings[1];
            }
        }
        // no day was found, return null to signal error
        return null;
    }

    public boolean isModified(){
        /*
            Is used to check if the user has made any at all changes to their workout. If so, appropriate
            action (namely altering the text file) must be taken.
         */
        return modified;
    }

    private void getCurrentWorkout(){
        /*
            This method ensures that when the app is closed and re-opened, it will pick up where the user
            last left off. It looks into the currentWorkout log file and simply returns the workout file
            corresponding to what workout the user currently has selected.
         */
        BufferedReader reader = null;
        try{
            File fhandle = new File(getContext().getExternalFilesDir(DIRECTORY_NAME), CURRENT_WORKOUT_LOG);
            FileReader fileR= new FileReader(fhandle);
            reader = new BufferedReader(fileR);
            WORKOUT_FILE = reader.readLine().split(SPLIT_DELIM)[0];
            reader.close();
        }
        catch (Exception e){

            Log.d("ERROR","Error when trying to read current workout file!"+e);
            currentDayNum=-1;
        }

    }

    private void updateExercise(int index, String line, boolean checked){
        String[] strings = line.split(SPLIT_DELIM);
        String updatedExercise;
        if(strings.length>=2){
            String exercise = strings[0];
            String video = strings[1];
            if(checked){
                updatedExercise = exercise+"*"+video+"*";
            }
            else{
                updatedExercise = exercise+"*"+video+"*"+EXERCISE_DONE;
            }
        }
        else{
            String exercise = strings[0];
            if(checked){
                updatedExercise = exercise+"*";
            }
            else{
                updatedExercise = exercise+"*"+EXERCISE_DONE;
            }
        }
        exercises.set(index, updatedExercise);
    }

    private void getCurrentDayNumber(){
        /*
            This method ensures that when the app is closed and re-opened, it will pick up where the user
            last left off. It looks into the currentDay text file and simply returns the number corresponding to what day the user is on.
         */
        BufferedReader reader = null;
        try{
            File fhandle = new File(getContext().getExternalFilesDir(DIRECTORY_NAME), CURRENT_WORKOUT_LOG);
            FileReader fileR= new FileReader(fhandle);
            reader = new BufferedReader(fileR);
            currentDayNum = Integer.parseInt(reader.readLine().split(SPLIT_DELIM)[1]);
            Log.d("Number", currentDayNum+"");
            reader.close();
        }
        catch (Exception e){

            Log.d("ERROR","Error when trying to read current workout log!"+e);
            currentDayNum=-1;
        }

    }

    public void recordToCurrentWorkoutLog(){
        /*
            Is called whenever the user either switches to another fragment, or exits the application. Saves the state of the
            current workout.
         */
        String _data = WORKOUT_FILE+"*"+currentDayNum;
        try{
            Log.d("recording", "Recording to log file..."+ _data);
            File fhandle = new File(getContext().getExternalFilesDir(DIRECTORY_NAME), CURRENT_WORKOUT_LOG);
            BufferedWriter writer = new BufferedWriter(new FileWriter(fhandle,false));
            writer.write(_data);
            writer.close();
        }
        catch (Exception e){
            Log.d("ERROR","Error when trying to write to current workout log!"+e);
        }
    }

    public void recordToWorkoutFile(){
        /*
            Updates the workout file to include the changes that were made by the user. This
            is called whenever the user clicks to go to another day or exits out of the fragment.
         */
        BufferedReader reader = null;
        BufferedWriter writer = null;
        File fhandleOld = new File(getContext().getExternalFilesDir(DIRECTORY_NAME), WORKOUT_FILE);
        File fhandleNew = new File(getContext().getExternalFilesDir(DIRECTORY_NAME), "temp");
        try{
            // progress through the file until the correct spot is found
            writer = new BufferedWriter(new FileWriter(fhandleNew,true));
            FileReader fileR= new FileReader(fhandleOld);
            reader = new BufferedReader(fileR);
            String line;
            while((line=reader.readLine())!=null){
                writer.write(line+"\n");
                String day = findDay(line); // possible day, if it is null then it is not at the current day specified in file
                if(day!=null){
                    break;
                }
            }
            int count = 0;
            while(!(line=reader.readLine()).equalsIgnoreCase(END_DAY_DELIM)){
                writer.write(exercises.get(count)+"\n");
                count++;
            }
            writer.write(END_DAY_DELIM+"\n");
            while((line=reader.readLine())!=null){
                writer.write(line+"\n");
            }
            reader.close();
            writer.close();
            fhandleOld.delete();
            fhandleNew.renameTo(fhandleOld);
            exercises.clear();
        }
        catch (Exception e){
            Log.d("ERROR","Error when trying to update workout file!"+e);
        }
    }

    public void resetWorkout(){
        BufferedReader reader = null;
        BufferedWriter writer = null;
        File fhandleOld = new File(getContext().getExternalFilesDir(DIRECTORY_NAME), WORKOUT_FILE);
        File fhandleNew = new File(getContext().getExternalFilesDir(DIRECTORY_NAME), "temp");
        try{
            // progress through the file until the correct spot is found
            writer = new BufferedWriter(new FileWriter(fhandleNew,true));
            FileReader fileR= new FileReader(fhandleOld);
            reader = new BufferedReader(fileR);
            String line;
            while((line=reader.readLine())!=null){
                String[] strings = line.split(SPLIT_DELIM);
                if(!strings[0].equalsIgnoreCase(END_DAY_DELIM)&&!strings[0].equalsIgnoreCase(START_CYCLE_DELIM)&&
                        !strings[0].equalsIgnoreCase(END_DAY_DELIM)&&!strings[0].equalsIgnoreCase(DAY_DELIM)){
                    String upatedExercise = strings[0]+"*"+strings[1]+"\n";
                    writer.write(upatedExercise);
                }
                else{
                    writer.write(line+"\n");
                }
            }
            reader.close();
            writer.close();
            fhandleOld.delete();
            fhandleNew.renameTo(fhandleOld);

            exercises.clear();
            currentDayNum=1;
            lastDay=false;
            firstDay=true;
            modified=true;
        }
        catch (Exception e){
            Log.d("ERROR","Error when trying to reset workout file!"+e);
        }
    }

    public void resetPopup(){
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
                populateWorkouts();
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

    }
    public void startTimer(){
        if(!timerRunning){
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
}

