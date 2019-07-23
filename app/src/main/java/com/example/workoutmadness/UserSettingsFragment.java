package com.example.workoutmadness;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class UserSettingsFragment extends Fragment {
    private View view;
    private boolean modifed;
    private Switch videoSwitch, timerSwitch;
    private Button importBtn, exportBtn;
    private ViewGroup fragmentContainer;
    private AlertDialog alertDialog;
    private ConstraintLayout constraintLayout;
    private RadioGroup radioGroup;
    private HashMap<String, ArrayList<String>> exerciseVideos = new HashMap<>();
    private HashMap<String,ArrayList<String>> defaultExercises = new HashMap<>(); // TODO put in global class?
    private HashMap<String,ArrayList<String>> customExercises = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_user_settings,container,false);
        constraintLayout = view.findViewById(R.id.constraintLayout);
        fragmentContainer = container;
        videoSwitch = view.findViewById(R.id.video_switch);
        populateDefaultExercises();
        radioGroup = (RadioGroup)view.findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.custom_exercise_radio_btn) {
                    inflateCustomLayout("exercises");
                }
                else if(checkedId == R.id.edit_video_radio_btn) {
                    inflateCustomLayout("videos");
                }
            }
        });

        ((MainActivity)getActivity()).updateToolbarTitle("Settings");
        // todo make rows "raised" so they are clearly clickable
        return view;
    }

    public void populateDefaultExercises(){
        BufferedReader reader = null;
        try{
            File fhandle = new File(getActivity().getExternalFilesDir(Variables.USER_SETTINGS_DIRECTORY_NAME), Variables.DEFAULT_EXERCISES_FILE);
            reader = new BufferedReader(new InputStreamReader(getActivity().getAssets().open(Variables.DEFAULT_EXERCISES_FILE)));
            String line;
            String cluster=null;
            while((line=reader.readLine())!=null){
                if(line.split(Variables.SPLIT_DELIM)[Variables.CLUSTER_INDEX].equals(Variables.CLUSTER_DELIM)){
                    cluster = line.split(Variables.SPLIT_DELIM)[Variables.CLUSTER_NAME_INDEX];
                    defaultExercises.put(cluster,new ArrayList<String>());
                }
                else{
                    defaultExercises.get(cluster).add(line);
                }
            }
            reader.close();
        }
        catch (Exception e){
            Log.d("ERROR","Error when trying to populate from default exercises file\n"+e);
        }
    }

    public void populateCustomExercises(){
        BufferedReader reader;
        try{
            File fhandle = new File(getContext().getExternalFilesDir(Variables.USER_SETTINGS_DIRECTORY_NAME), Variables.CUSTOM_EXERCISES);
            FileReader fileR= new FileReader(fhandle);
            reader = new BufferedReader(fileR);
            String line;
            String cluster=null;
            while((line=reader.readLine())!=null){
                if(line.split(Variables.SPLIT_DELIM)[Variables.CLUSTER_INDEX].equals(Variables.CLUSTER_DELIM)){
                    cluster = line.split(Variables.SPLIT_DELIM)[Variables.CLUSTER_NAME_INDEX];
                    defaultExercises.put(cluster,new ArrayList<String>());
                }
                else{
                    defaultExercises.get(cluster).add(line);
                }
            }
            reader.close();
        }
        catch (Exception e){
            Log.d("ERROR","Error when trying to populate from default exercises file\n"+e);
        }
    }

    public void writeToExerciseFile(){
        BufferedReader reader = null;
        BufferedWriter writer = null;
        try{
            File fhandle = new File(getActivity().getExternalFilesDir(Variables.USER_SETTINGS_DIRECTORY_NAME), Variables.DEFAULT_EXERCISES_FILE);
            writer = new BufferedWriter(new FileWriter(fhandle,false));
            reader = new BufferedReader(new InputStreamReader(getActivity().getAssets().open(Variables.DEFAULT_EXERCISES_FILE)));
            String line;
            while((line=reader.readLine())!=null){
                writer.write(line+"\n");
            }
            writer.close();
            reader.close();
        }
        catch (Exception e){
            Log.d("ERROR","Error when trying to write to exercises file\n"+e);
        }
    }

    public void inflateCustomLayout(String mode){
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        constraintLayout.removeAllViews();
        View view = null;
        switch (mode){
            case "videos":
                view = inflater.inflate(R.layout.video_list, constraintLayout,false);
                constraintLayout.addView(view);
                populateClusterList(view, "videos");
                break;
            case "exercises":
                view = inflater.inflate(R.layout.custom_exercises, constraintLayout,false);
                constraintLayout.addView(view);
                Button createBtn = view.findViewById(R.id.new_exercise_btn);
                createBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        newExercisePopup();
                    }
                });
                populateClusterList(view,"exercises");
                break;
            default:
                break;
        }

    }

    public void populateClusterList(final View view, final String mode){
        final ListView listView = view.findViewById(R.id.cluster_list);
        ArrayList<String> clusters = new ArrayList<>();
        for(String key : defaultExercises.keySet()){
            clusters.add(key);
        }
        Collections.sort(clusters);
        ArrayAdapter arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_activated_1, clusters);
        listView.setAdapter(arrayAdapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            private View parentView = view;
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                populateCustomExercises(parentView, listView.getItemAtPosition(position).toString(),mode);
            }
        });
        // programmatically select first item
        listView.performItemClick(listView.getAdapter().getView(0, null, null), 0, 0);
        listView.setSelection(0);
//        listView.setItemChecked(1, true);
    }

    public void populateCustomExercises(View view, String cluster, final String mode){
        final ListView listView = view.findViewById(R.id.exercise_list);
        ArrayList<String> exercises = new ArrayList<>();
        if(defaultExercises.get(cluster)==null){
            return;
        }
        for(String exercise : defaultExercises.get(cluster)){
            exercises.add(exercise);
        }
        Collections.sort(exercises);
        ArrayAdapter arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, exercises);
        listView.setAdapter(arrayAdapter);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO populate exercise
                switch (mode){
                    case "videos":
                        editUrlPopup(listView.getItemAtPosition(position).toString());
                        break;
                    case "exercises":
                        editExercisePopup(listView.getItemAtPosition(position).toString());
                        break;
                }
            }
        });
    }

    /*
        -----------------
        Popups
        -----------------
     */

    public void editUrlPopup(String name){
        /*
            User has indicated they wish to add exercises to this specific day. Show a popup that provides a spinner
            that is programmed to list all exercises for a given exercise cluster.
         */
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialog = alertDialogBuilder.create();
        View popupView = getLayoutInflater().inflate(R.layout.popup_edit_url, null);
        alertDialog.setView(popupView);
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
        Button doneButton = popupView.findViewById(R.id.done_btn);
        TextView exerciseName = popupView.findViewById(R.id.exercise_name);
        exerciseName.setText(name);
        TextView oldURL = popupView.findViewById(R.id.old_url);
        oldURL.setText("https://www.youtube.com/watch?v=Vyqz_-sJGFk");
        EditText userInput = popupView.findViewById(R.id.edit_url_txt);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
    }

    public void editExercisePopup(String name){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialog = alertDialogBuilder.create();
        View popupView = getLayoutInflater().inflate(R.layout.popup_edit_custom_exercise, null);
        alertDialog.setView(popupView);
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
        Button renameBtn = popupView.findViewById(R.id.rename_btn);
        Button deleteBtn = popupView.findViewById(R.id.delete_btn);
        TextView exerciseName = popupView.findViewById(R.id.exercise_name);
        exerciseName.setText(name);
        EditText userInput = popupView.findViewById(R.id.edit_name_txt);
        renameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
    }

    public void newExercisePopup(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialog = alertDialogBuilder.create();
        View popupView = getLayoutInflater().inflate(R.layout.popup_new_exercise, null);
        alertDialog.setView(popupView);
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
        Button doneBtn = popupView.findViewById(R.id.done_btn);
        TextView exerciseName = popupView.findViewById(R.id.exercise_name);
        EditText userInput = popupView.findViewById(R.id.edit_name_txt);
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
    }

    public boolean isModified(){
        return modifed;
    }
}
