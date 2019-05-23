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
    private String currentDay, SPLIT_DELIM ="\\*", END_DAY_DELIM="END DAY", WORKOUT_FILE="workout.txt";
    private boolean modified=false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_workout,container,false);
        getCurrentDay();
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
        //first find the right spot in the file
        currentDay = getCurrentDay();
        BufferedReader reader = null;
        TableLayout ll = (TableLayout) view.findViewById(R.id.main_table);
        try{
            reader = new BufferedReader(new InputStreamReader(getContext().getAssets().open("Workout.txt")));
            while(true){
                // TODO make this a for loop!
                String line=reader.readLine();
                String day = findDay(line);
                Log.d("ERROR",line);
                if(day!=null&&day.equalsIgnoreCase(currentDay)){
//                    currentDay=line.split(SPLIT_DELIM)[1];
                    dayTV = view.findViewById(R.id.dayTextView);
                    dayTV.setText(currentDay);
                    break;
                }
            }

            // Now, loop through and populate the scroll view with all the exercises in this day
            String line;
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
                ll.addView(row,count);
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
            delimiter and then returns that value of the day.
         */
        if(_data==null){
            return null;
        }
        String[] strings = _data.split(SPLIT_DELIM);
        if(strings[0].equalsIgnoreCase("time")){
            return strings[1];
        }
        return null;
    }

    public boolean isModified(){
        /*
            Is used to check if the user has made any at all changes to their workout. If so, appropriate
            action (namely altering the text file) must be taken.
         */
        return modified;
    }

    public String getCurrentDay(){
        /*
            This method ensures that when the app is closed and re-opened, it will pick up where the user
            last left off. It looks into the currentDay text file and simply returns the day.
         */
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new InputStreamReader(getContext().getAssets().open("currentDay.txt")));
            String day = reader.readLine();
            reader.close();
            return day;
        }
        catch (Exception e){

            Log.d("ERROR","Error when trying to read day file!");
            return null;
        }

    }
}
