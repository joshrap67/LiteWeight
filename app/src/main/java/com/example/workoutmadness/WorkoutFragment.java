package com.example.workoutmadness;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class WorkoutFragment extends Fragment {
    private View view;
    private TextView dayTV;
    TableLayout table;
    private Button forwardButton, backButton;
    private int currentDayNum=1;
    private String currentDay, SPLIT_DELIM ="\\*", END_DAY_DELIM="END DAY", END_CYCLE_DELIM="END", START_CYCLE_DELIM="START",
            DAY_DELIM="TIME", WORKOUT_FILE="Workout.txt", CURRENT_WORKOUT = "currentWorkout.txt";
    private boolean modified=false, lastDay=false, firstDay=false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_workout,container,false);
        forwardButton = view.findViewById(R.id.forwardButton);
        backButton = view.findViewById(R.id.previousDayButton);
        table = (TableLayout) view.findViewById(R.id.main_table);
        getCurrentDayNumber();
        populateWorkouts();
        return view;
    }

    public void populateWorkouts(){
        /*
        Populates workouts based on the currently selected workout.
         */
        // get the workout name and update the toolbar with the name

        String workoutName = "Josh's Workout";
        ((MainActivity)getActivity()).updateToolbarTitle(workoutName);
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new InputStreamReader(getContext().getAssets().open(WORKOUT_FILE)));
            String line;
            while(true){
                // TODO make this a for loop
                line=reader.readLine();
                String day = findDay(line); // the day
                if(day!=null){
                    dayTV = view.findViewById(R.id.dayTextView);
                    dayTV.setText(day);
                    break;
                }
            }
            // set up the forward and back buttons
            if(firstDay){
                backButton.setVisibility(View.INVISIBLE);
            }
            else{
                backButton.setVisibility(View.VISIBLE);
                backButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        currentDayNum--;
                        lastDay=false;
                        table.removeAllViews();
                        populateWorkouts();
                    }
                });
            }
            if(lastDay){
                // TODO reset cycle
                forwardButton.setText("RESET");
                forwardButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
            }
            else{
                forwardButton.setText("Next");
                forwardButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        currentDayNum++;
                        firstDay=false;
                        table.removeAllViews();
                        populateWorkouts();
                    }
                });
            }
            // Now, loop through and populate the scroll view with all the exercises in this day
            int count =0;
            while(!(line=reader.readLine()).equalsIgnoreCase(END_DAY_DELIM)){
                final String[] strings = line.split(SPLIT_DELIM);
                TableRow row = new TableRow(getActivity());
                TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
                row.setLayoutParams(lp);

                CheckBox exercise = new CheckBox(getActivity());
                exercise.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        modified=true;
                    }
                });
                exercise.setText(strings[0]);
                row.addView(exercise);
                if(strings.length==3){
                    // means that the workout has already been done, so make sure to check the checkbox
                    exercise.setChecked(true);
                }

                Button videoButton = new Button(getActivity());
                videoButton.setText("Video");
                videoButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(strings.length>=2){
                            String URL = strings[1];
                            // TODO actually launch youtube
                            Toast.makeText(getActivity(), URL, Toast.LENGTH_LONG).show();
                        }
                        else{
                            Toast.makeText(getActivity(), "No video found", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                row.addView(videoButton);
                table.addView(row,count);
                count++;
            }
            reader.close();
        }
        catch (Exception e){
            Log.d("ERROR","Error when trying to read workout file!");
        }
    }

    public String findDay(String _data){
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

    public void getCurrentDayNumber(){
        /*
            This method ensures that when the app is closed and re-opened, it will pick up where the user
            last left off. It looks into the currentDay text file and simply returns the number corresponding to what day the user is on.
         */
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new InputStreamReader(getContext().getAssets().open(CURRENT_WORKOUT)));
            int dayNumber = Integer.parseInt(reader.readLine().split(SPLIT_DELIM)[1]);
            reader.close();
            currentDayNum = dayNumber;
        }
        catch (Exception e){

            Log.d("ERROR","Error when trying to read day file!");
            currentDayNum=-1;
        }

    }
}
