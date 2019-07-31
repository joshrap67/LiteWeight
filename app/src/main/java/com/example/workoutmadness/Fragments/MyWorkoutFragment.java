package com.example.workoutmadness.Fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.workoutmadness.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class MyWorkoutFragment extends Fragment {
    private View view;
    private ArrayList<String> workouts = new ArrayList<>();
    private TextView selectedWorkoutTV, statisticsTV;
    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;
    private String selectedWorkout;
    private Button resetStatisticsBtn, editBtn, deleteBtn;
    private HashMap<Integer, ArrayList<String>> exercises = new HashMap<>();
    private HashMap<Integer, String> totalDayTitles = new HashMap<>();
    private int maxDayIndex;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_my_workouts,container,false);
        ((MainActivity)getActivity()).updateToolbarTitle("My Workouts");
        listView= view.findViewById(R.id.workout_list);
        selectedWorkoutTV =view.findViewById(R.id.selected_workout_text_view);
        statisticsTV=view.findViewById(R.id.stat_text_view);
        deleteBtn=view.findViewById(R.id.delete_button);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File fhandle = new File(getContext().getExternalFilesDir(Variables.WORKOUT_DIRECTORY), Variables.CURRENT_WORKOUT_LOG);
                if(!(fhandle.length()==0)){
                    promptDelete();
                }
            }
        });
        arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, workouts);
        listView.setAdapter(arrayAdapter);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                updateCurrentWorkoutLog(workouts.get(position));
                updateListView();
            }
        });
        updateListView();
        // TODO add sorting for listview?
        return view;
    }

    public void updateStatistics(){
        // dummy stuff for now
        String msg = "Times Completed: 420\n" +
                "Average Percentage of Exercises Completed: 69%\n" +
                "Most Frequent Exercise: Dabbing\n" +
                "Least Frequent Exercise: Yeeting";
        statisticsTV.setText(msg);
    }

    public boolean selectWorkout(){
        /*
            Updates the current workout variable and TextView by grabbing the name from the first line of the workout log.
            Then, it grabs the rest of the workouts in the log and adds them to the list in order of most recently accessed.
         */
        BufferedReader reader = null;
        try{
            File fhandle = new File(getContext().getExternalFilesDir(Variables.WORKOUT_DIRECTORY), Variables.CURRENT_WORKOUT_LOG);
            if(fhandle.length()==0){
                // somehow is empty, so return for error
                return false;
            }
            workouts.clear(); // clear any previous workouts when re doing this.
            FileReader fileR= new FileReader(fhandle);
            reader = new BufferedReader(fileR);
            String line = reader.readLine().split(Variables.SPLIT_DELIM)[Variables.WORKOUT_NAME_INDEX];
            selectedWorkout=line.substring(0,line.lastIndexOf("."));
            selectedWorkoutTV.setText(selectedWorkout);
            // now grab the rest of the workouts to put into the list
            while(!((line=reader.readLine())==null)){
                String workout = line.split(Variables.SPLIT_DELIM)[Variables.WORKOUT_NAME_INDEX];
                int extIndex = workout.lastIndexOf(".");
                workout = line.substring(0, extIndex);
                workouts.add(workout);
            }
            arrayAdapter.notifyDataSetChanged();
            reader.close();
            return true;
        }
        catch (Exception e){
            Log.d("ERROR","Error when trying to read current workout file!\n"+e);
            return false;
        }
    }

    public void updateCurrentWorkoutLog(String workoutName){
        /*
            Called when user selects a workout from the list. The log must be changed so this workout is at the top of the file
            while all of the other workouts are placed below it.
            workoutName has no file extension, it is the name of the workout
         */
        BufferedReader reader = null;
        BufferedWriter writer = null;
        String _data = null;
        File fhandleOld = new File(getContext().getExternalFilesDir(Variables.WORKOUT_DIRECTORY), Variables.CURRENT_WORKOUT_LOG);
        File fhandleNew = new File(getContext().getExternalFilesDir(Variables.WORKOUT_DIRECTORY), "temp");
        try{
            // progress through the file until the correct spot is found
            writer = new BufferedWriter(new FileWriter(fhandleNew,true));
            FileReader fileR= new FileReader(fhandleOld);
            reader = new BufferedReader(fileR);
            String line;
            while((line=reader.readLine())!=null){
                String fileName = line.split(Variables.SPLIT_DELIM)[Variables.WORKOUT_NAME_INDEX];
                String name = fileName.substring(0,fileName.lastIndexOf("."));
                if(name.equalsIgnoreCase(workoutName)){
                    // found the workout data so preserve it
                    _data = line;
                    break;
                }
            }
            // reset the reader to the top of the file
            reader.close();
            fileR= new FileReader(fhandleOld);
            reader = new BufferedReader(fileR);

            writer.write(_data+"\n"); // put the new selected workout at the top of the file
            while(((line=reader.readLine())!=null)){
                if(!line.equalsIgnoreCase(_data)){
                    // the workout name is already at the top of the file, don't write it twice
                    writer.write(line+"\n");
                }
            }
            reader.close();
            writer.close();
            fhandleOld.delete();
            fhandleNew.renameTo(fhandleOld);
        }
        catch (Exception e){
            Log.d("ERROR","Error when trying to update current workout log!\n"+e);
        }
    }

    public void updateListView(){
        if(selectWorkout()){
            updateStatistics();
        }
        else{
            // display error message
        }

    }

    public void promptDelete(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        final AlertDialog alertDialog = alertDialogBuilder.create();
        final View popupView = getLayoutInflater().inflate(R.layout.delete_popup, null);
        Button confirmButton = popupView.findViewById(R.id.popupYes);
        TextView workoutName = popupView.findViewById(R.id.workout_name);
        workoutName.setText(selectedWorkout);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(deleteFile(selectedWorkout+Variables.WORKOUT_EXT)){
                    removeWorkoutFromLog(selectedWorkout);
                    updateListView();
                }
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

    public boolean deleteFile(String fileName){
        // TODO need to handle when no files left
        String directoryName=null;
        String ext = fileName.substring(fileName.lastIndexOf("."));
        switch (ext){
            case Variables.WORKOUT_EXT:
                directoryName=Variables.WORKOUT_DIRECTORY;
            case Variables.STATISTICS_EXT:
                break;
        }

        File file = new File(getContext().getExternalFilesDir(directoryName), fileName);
        try{
            return file.delete();
        }
        catch (Exception e){
            Log.d("ERROR","Error when trying to delete "+fileName+"\n"+e);
            return false;
        }
    }

    public void removeWorkoutFromLog(String workoutName){
        /*
            workoutName has no extension
         */
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
            while((line=reader.readLine())!=null){
                String fileName = line.split(Variables.SPLIT_DELIM)[Variables.WORKOUT_NAME_INDEX];
                String name = fileName.substring(0,fileName.lastIndexOf("."));
                if(!(name.equalsIgnoreCase(workoutName))){
                    // when name is found skip over it
                    writer.write(line+"\n");
                }
            }
            reader.close();
            writer.close();
            fhandleOld.delete();
            fhandleNew.renameTo(fhandleOld);
            if(fhandleNew.length()==0){
                // all the workouts have now been deleted, so update text view to alert user
                // TODO put some type of popup to tell them to go create a workout?
                selectedWorkoutTV.setText("No Workouts Found!");
            }
        }
        catch (Exception e){
            Log.d("ERROR","Error when trying to delete workout from current workout log!\n"+e);
        }
    }
    /*
        **************************
        Edit Workout Methods
        **************************
     */
    public void editWorkout(){
        // TODO change layout
        // TODO init the buttons
        // TODO update list view
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
            File fhandle = new File(getContext().getExternalFilesDir(Variables.WORKOUT_DIRECTORY), selectedWorkout+Variables.WORKOUT_EXT);
            FileReader fileR= new FileReader(fhandle);
            reader = new BufferedReader(fileR);
            String line;
            while((line=reader.readLine())!=null){
                boolean day = isDay(line); // possible day, if it is null then it is not at the current day specified in file
                if(day){
                    hashIndex++;
                    exercises.put(hashIndex, new ArrayList<String>());
                    totalDayTitles.put(hashIndex, line.split(Variables.SPLIT_DELIM)[Variables.TIME_TITLE_INDEX]); // add day
                }
                else{
                    exercises.get(hashIndex).add(line.split(Variables.SPLIT_DELIM)[Variables.WORKOUT_NAME_INDEX]);
                }
            }
        }
        catch (Exception e){
            Log.d("ERROR","Error when trying to read "+selectedWorkout+" file!\n"+e);
        }
        maxDayIndex = hashIndex;
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
        return delim.equalsIgnoreCase(Variables.DAY_DELIM); // return true if this indeed is a day
    }
}
