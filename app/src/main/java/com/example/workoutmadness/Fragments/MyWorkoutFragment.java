package com.example.workoutmadness.Fragments;

import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.AsyncTask;
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
import com.example.workoutmadness.Database.Entities.MetaEntity;
import com.example.workoutmadness.Database.ViewModels.MetaViewModel;
import com.example.workoutmadness.Database.ViewModels.WorkoutViewModel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Stack;

public class MyWorkoutFragment extends Fragment {
    private View view;
    private TextView selectedWorkoutTV, statisticsTV, defaultTV;
    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;
    private MetaEntity selectedWorkout;
    private Button resetStatisticsBtn, editBtn, deleteBtn;
    private HashMap<Integer, ArrayList<String>> exercises = new HashMap<>();
    private HashMap<Integer, String> totalDayTitles = new HashMap<>();
    private HashMap<String, MetaEntity> workoutNameToEntity = new HashMap<>();
    private int maxDayIndex;
    private WorkoutViewModel workoutModel;
    private MetaViewModel metaModel;
    private ArrayList<MetaEntity> metaEntities = new ArrayList<>();
    private ArrayList<String> workoutNames = new ArrayList<>();
    private ViewGroup fragmentContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.default_layout,container,false);
        fragmentContainer = container;
        defaultTV = view.findViewById(R.id.default_tv);
        defaultTV.setVisibility(View.GONE);
        ((MainActivity)getActivity()).updateToolbarTitle("My Workouts");
        metaModel = ViewModelProviders.of(getActivity()).get(MetaViewModel.class);
        workoutModel = ViewModelProviders.of(getActivity()).get(WorkoutViewModel.class);
        GetAllWorkoutsTask task = new GetAllWorkoutsTask();
        task.execute();
        // TODO add sorting for listview?
        return view;
    }

    private class GetAllWorkoutsTask extends AsyncTask<Void, Void, ArrayList<MetaEntity>> {

        @Override
        protected ArrayList<MetaEntity> doInBackground(Void... voids) {
            // get the current workout from the database
            return metaModel.getAllMetadata();
        }

        @Override
        protected void onPostExecute(ArrayList<MetaEntity> result) {
            if(!result.isEmpty()) {
                Log.d("TAG","Result size: "+result.size());
                metaEntities = result;
                initViews();
            }
            else{
                defaultTV.setVisibility(View.VISIBLE);
                Log.d("TAG","No workouts found!");
            }
        }
    }

    public void initViews(){
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.fragment_my_workouts, fragmentContainer,false);
        ViewGroup rootView = (ViewGroup) getView();
        rootView.removeAllViews();
        rootView.addView(view);
        listView = view.findViewById(R.id.workout_list);
        selectedWorkoutTV = view.findViewById(R.id.selected_workout_text_view);
        statisticsTV = view.findViewById(R.id.stat_text_view);
        deleteBtn = view.findViewById(R.id.delete_button);

        for(MetaEntity entity : metaEntities){
            Log.d("TAG","Meta entity: "+entity.toString());
            if(entity.getCurrentWorkout()){
                selectedWorkout = entity;
                selectedWorkoutTV.setText(selectedWorkout.getWorkoutName());
                updateStatistics();
            }
            else{
                workoutNames.add(entity.getWorkoutName());
            }
            workoutNameToEntity.put(entity.getWorkoutName(),entity);
        }
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!metaEntities.isEmpty()){
                    promptDelete();
                }
            }
        });
        // TODO sort by date
        sortWorkouts();
        arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, workoutNames);
        listView.setAdapter(arrayAdapter);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectWorkout(listView.getItemAtPosition(position).toString());
            }
        });
    }

    public void sortWorkouts(){
        Collections.sort(workoutNames);
//        Collections.addAll(sortedWorkoutNames,workoutNames)
//        sortedWorkoutNames.push(selectedWorkout.getWorkoutName());
        // TODO sort by date and possible give other options?
    }

    public void selectWorkout(String workoutName){
        // TODO update the entity with new date
        workoutNames.remove(workoutName);
        // TODO would do this part differently if different sorting method
        workoutNames.add(0,selectedWorkout.getWorkoutName());
        selectedWorkout.setCurrentWorkout(false);
        metaModel.update(selectedWorkout);
        selectedWorkout = workoutNameToEntity.get(workoutName);
        selectedWorkout.setCurrentWorkout(true);
        metaModel.update(selectedWorkout);
        if(selectedWorkout==null){
            // uh oh
            Log.d("TAG","Selected workout was somehow null");
        }
        selectedWorkoutTV.setText(workoutName);
        arrayAdapter.notifyDataSetChanged();
        updateStatistics();
    }
    public void updateStatistics(){
        // dummy stuff for now
        String msg = "Times Completed: 420\n" +
                "Average Percentage of Exercises Completed: 69%\n" +
                "Most Frequent Exercise: Dabbing\n" +
                "Least Frequent Exercise: Yeeting";
        statisticsTV.setText(msg);
    }

    public void promptDelete(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        final AlertDialog alertDialog = alertDialogBuilder.create();
        final View popupView = getLayoutInflater().inflate(R.layout.delete_popup, null);
        Button confirmButton = popupView.findViewById(R.id.popupYes);
        TextView workoutName = popupView.findViewById(R.id.workout_name);
        workoutName.setText(selectedWorkout.getWorkoutName());
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteWorkout();
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
    public void deleteWorkout(){
        metaEntities.remove(selectedWorkout);
        DeleteWorkoutAsync task = new DeleteWorkoutAsync();
        task.execute(selectedWorkout);

    }

    private class DeleteWorkoutAsync extends AsyncTask<MetaEntity, Void, Void> {

        @Override
        protected Void doInBackground(MetaEntity... param) {
            // TODO put a lock here that prevents user from leaving until it's done
            metaModel.delete(param[0]);
            workoutModel.deleteEntireWorkout(param[0].getWorkoutName());
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if(!workoutNames.isEmpty()){
                selectedWorkout = workoutNameToEntity.get(workoutNames.get(0)); // get the top of the list
                workoutNames.remove(selectedWorkout.getWorkoutName());
                selectedWorkout.setCurrentWorkout(true);
                // TODO update the date last of this workout
                metaModel.update(selectedWorkout);

                selectedWorkoutTV.setText(selectedWorkout.getWorkoutName());
                arrayAdapter.notifyDataSetChanged();
            }
            else{
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.default_layout, fragmentContainer,false);
                ViewGroup rootView = (ViewGroup) getView();
                rootView.removeAllViews();
                rootView.addView(view);
                // signal to go make a new workout
            }
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
        // TODO updateWorkoutEntity list view
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
