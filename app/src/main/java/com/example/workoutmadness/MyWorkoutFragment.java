package com.example.workoutmadness;

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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;

public class MyWorkoutFragment extends Fragment {
    private View view;
    private ArrayList<String> workouts = new ArrayList<>();
    private TextView selectedWorkoutTV, statisticsTV;
    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;
    private static final String WORKOUT_EXT = ".txt", STATISTICS_EXT = ".stat";
    private String WORKOUT_DIRECTORY, CURRENT_WORKOUT_LOG, STATISTICS_DIRECTORY, SPLIT_DELIM="\\*", selectedWorkout;
    private Button resetStatisticsBtn, editBtn, deleteBtn;
    private int WORKOUT_NAME_INDEX=0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_my_workouts,container,false);
        ((MainActivity)getActivity()).updateToolbarTitle("My Workouts");
        WORKOUT_DIRECTORY = ((MainActivity) getActivity()).getWorkoutDirectoryName();
        CURRENT_WORKOUT_LOG = ((MainActivity) getActivity()).getWorkoutLogName();
        listView= view.findViewById(R.id.workout_list);
        selectedWorkoutTV =view.findViewById(R.id.selected_workout_text_view);
        statisticsTV=view.findViewById(R.id.stat_text_view);
        deleteBtn=view.findViewById(R.id.delete_button);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptDelete();
            }
        });
        if(updateCurrentWorkout()){
            updateWorkouts();
            populateListView();
            updateStatistics();
        }
        return view;
    }

    public void updateStatistics(){
        // dummy stuff for now
        String msg = "Times Completed: 420\n" +
                "Average Percentage of Exercises Completed: 69%\n" +
                "Most Frequent Exercise: Yeeting\n" +
                "Least Frequent Exercise: Dabbing";
        statisticsTV.setText(msg);
    }

    public boolean updateCurrentWorkout(){
        BufferedReader reader = null;
        try{
            File fhandle = new File(getContext().getExternalFilesDir(WORKOUT_DIRECTORY), CURRENT_WORKOUT_LOG);
            if(fhandle.length()==0){
                // somehow is empty, so return for error
                return false;
            }
            FileReader fileR= new FileReader(fhandle);
            reader = new BufferedReader(fileR);
            String line = reader.readLine().split(SPLIT_DELIM)[WORKOUT_NAME_INDEX];
            selectedWorkout=line.substring(0,line.lastIndexOf("."));
            reader.close();
            selectedWorkoutTV.setText(selectedWorkout);
            return true;
        }
        catch (Exception e){
            Log.d("ERROR","Error when trying to read current workout file!\n"+e);
            return false;
        }
    }

    public void updateCurrentWorkoutLog(String workoutName){
        // structure of the log is that the first line is the current workout
        BufferedReader reader = null;
        BufferedWriter writer = null;
        String _data = null;
        File fhandleOld = new File(getContext().getExternalFilesDir(WORKOUT_DIRECTORY), CURRENT_WORKOUT_LOG);
        File fhandleNew = new File(getContext().getExternalFilesDir(WORKOUT_DIRECTORY), "temp");
        try{
            // progress through the file until the correct spot is found
            writer = new BufferedWriter(new FileWriter(fhandleNew,true));
            FileReader fileR= new FileReader(fhandleOld);
            reader = new BufferedReader(fileR);
            String line;
            while((line=reader.readLine())!=null){
                String fileName = line.split(SPLIT_DELIM)[WORKOUT_NAME_INDEX];
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

    public void removeWorkoutFromLog(String workoutName){
        BufferedReader reader = null;
        BufferedWriter writer = null;
        File fhandleOld = new File(getContext().getExternalFilesDir(WORKOUT_DIRECTORY), CURRENT_WORKOUT_LOG);
        File fhandleNew = new File(getContext().getExternalFilesDir(WORKOUT_DIRECTORY), "temp");
        try{
            // progress through the file until the correct spot is found
            writer = new BufferedWriter(new FileWriter(fhandleNew,true));
            FileReader fileR= new FileReader(fhandleOld);
            reader = new BufferedReader(fileR);
            String line;
            while((line=reader.readLine())!=null){
                String fileName = line.split(SPLIT_DELIM)[WORKOUT_NAME_INDEX];
                String name = fileName.substring(0,fileName.lastIndexOf("."));
                if(!(name.equalsIgnoreCase(workoutName))){
                    // when name is found skip over it
                    writer.write(line+"\n");
                    break;
                }
            }
            reader.close();
            writer.close();
            fhandleOld.delete();
            fhandleNew.renameTo(fhandleOld);
        }
        catch (Exception e){
            Log.d("ERROR","Error when trying to delete workout from current workout log!\n"+e);
        }
    }

    public void updateWorkouts(){
        File directoryHandle = getActivity().getExternalFilesDir(WORKOUT_DIRECTORY);
        File[] contents = directoryHandle.listFiles();
        for(File file : contents){
            String workout = file.getName();
            if (workout.indexOf(".") > 0) {
                int extIndex = workout.lastIndexOf(".");
                String ext = workout.substring(extIndex);
                if(!ext.equalsIgnoreCase(".log")){
                    workout = workout.substring(0, extIndex);
                    if(!(workout.equalsIgnoreCase(selectedWorkout))){
                        workouts.add(workout);
                    }
                }
            }
        }
        Collections.sort(workouts);
    }

    public void populateListView(){
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
    }

    public void updateListView(){
        if(updateCurrentWorkout()){
            workouts.clear();
            arrayAdapter.notifyDataSetChanged();
            updateWorkouts();
            populateListView();
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
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(deleteFile(selectedWorkout+WORKOUT_EXT)){
                    workouts.remove(selectedWorkout);
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
        String directoryName=null;
        String ext = fileName.substring(fileName.lastIndexOf("."));
        switch (ext){
            case WORKOUT_EXT:
                directoryName=WORKOUT_DIRECTORY;
            case STATISTICS_EXT:
                break;
        }
        File file = new File(directoryName,fileName);
        try{
            return file.delete();
        }
        catch (Exception e){
            Log.d("ERROR","Error when trying to delete "+fileName+"\n"+e);
            return false;
        }
    }
}
