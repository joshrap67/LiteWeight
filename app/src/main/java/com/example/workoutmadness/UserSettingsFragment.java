package com.example.workoutmadness;

import android.app.AlertDialog;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
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
import java.util.Collections;
import java.util.HashMap;

public class UserSettingsFragment extends Fragment {
    private View view;
    private boolean modifed, filterCustom;
    private Switch videoSwitch, timerSwitch, filterSwitch;
    private Button importBtn, exportBtn;
    private ViewGroup fragmentContainer;
    private AlertDialog alertDialog;
    private ArrayList<String> focuses;
    private HashMap<String, ArrayList<String>> defaultExerciseVideos = new HashMap<>();
    private HashMap<String, ArrayList<String>> customExerciseVideos = new HashMap<>();
    private HashMap<String,ArrayList<String>> defaultExercises = new HashMap<>();
    private HashMap<String,ArrayList<String>> customExercises = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((MainActivity)getActivity()).updateToolbarTitle("Settings");
        view = inflater.inflate(R.layout.fragment_user_settings,container,false);
        fragmentContainer = container;
        videoSwitch = view.findViewById(R.id.video_switch);
        filterSwitch = view.findViewById(R.id.filter_switch);
        filterCustom=false;
        filterSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterCustom=isChecked;
                populateFocusList();
            }
        });
        Button createBtn = view.findViewById(R.id.new_exercise_btn);
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newExercisePopup();
            }
        });
        populateDefaultExercises();
        populateFocusList();
        // todo make rows "raised" so they are clearly clickable?
        return view;
    }

    public void populateDefaultExercises(){
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new InputStreamReader(getActivity().getAssets().open(Variables.DEFAULT_EXERCISES_FILE)));
            String line;
            String focus=null;
            while((line=reader.readLine())!=null){
                if(line.split(Variables.SPLIT_DELIM)[Variables.FOCUS_INDEX].equals(Variables.FOCUS_DELIM)){
                    focus = line.split(Variables.SPLIT_DELIM)[Variables.FOCUS_NAME_INDEX];
                    defaultExercises.put(focus,new ArrayList<String>());
                    customExercises.put(focus,new ArrayList<String>());
                }
                else{
                    defaultExercises.get(focus).add(line);
                }
            }
            reader.close();
        }
        catch (Exception e){
            Log.d("ERROR","Error when trying to populate from default exercises file\n"+e);
        }
    }

    public void populateExercises(){
        BufferedReader reader;
        try{
            //TODO need to ensure this file isn't null
            File fhandle = new File(getContext().getExternalFilesDir(Variables.USER_SETTINGS_DIRECTORY_NAME), Variables.CUSTOM_EXERCISES);
            FileReader fileR= new FileReader(fhandle);
            reader = new BufferedReader(fileR);
            String line;
            String focus=null;
            while((line=reader.readLine())!=null){
                if(line.split(Variables.SPLIT_DELIM)[Variables.FOCUS_INDEX].equals(Variables.FOCUS_DELIM)){
                    focus = line.split(Variables.SPLIT_DELIM)[Variables.FOCUS_NAME_INDEX];
                }
                else{
                    customExercises.get(focus).add(line);
                }
            }
            reader.close();
        }
        catch (Exception e){
            Log.d("ERROR","Error when trying to populate from custom exercises file\n"+e);
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

    public void populateFocusList(){
        final ListView listView = view.findViewById(R.id.focus_list);
        focuses = new ArrayList<>();
        for(String key : defaultExercises.keySet()){
            focuses.add(key);
        }
        Collections.sort(focuses);
        ArrayAdapter arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_activated_1, focuses);
        listView.setAdapter(arrayAdapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                populateExercises(listView.getItemAtPosition(position).toString());
            }
        });
        // programmatically select first item
        listView.performItemClick(listView.getAdapter().getView(0, null, null), 0, 0);
        listView.setSelection(0);
    }

    public void populateExercises(String focus){
        final ListView listView = view.findViewById(R.id.exercise_list);
        ArrayList<String> exercises = new ArrayList<>();
        if(defaultExercises.get(focus)==null){
            return;
        }
        if(!filterCustom){
            for(String exercise : defaultExercises.get(focus)){
                exercises.add(exercise);
            }
        }
        for(String exercise : customExercises.get(focus)){
            exercises.add(exercise);
        }
        Collections.sort(exercises);
        ArrayAdapter arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, exercises);
        listView.setAdapter(arrayAdapter);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                editExercisePopup(listView.getItemAtPosition(position).toString());
            }
        });
    }
    /*
        -----------------
        Popups
        -----------------
     */
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
        final ArrayList<String> selectedFocuses = new ArrayList<>();
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialog = alertDialogBuilder.create();
        View popupView = getLayoutInflater().inflate(R.layout.popup_new_exercise, null);
        Button doneBtn = popupView.findViewById(R.id.done_btn);
        final EditText exerciseNameInput = popupView.findViewById(R.id.edit_name_txt);
        final EditText editURL = popupView.findViewById(R.id.edit_url_txt);
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateNewExercise(exerciseNameInput, editURL)){
                    // TODO add the exercise to file
                    for(String focus : selectedFocuses){
                        customExercises.get(focus).add(exerciseNameInput.getText().toString());
                        populateFocusList();
                    }
                    // TODO add the exercise to file
                    Toast.makeText(getContext(),"Exercise successfully created!",Toast.LENGTH_SHORT).show();
                    alertDialog.dismiss();
                }
                else{
                    Toast.makeText(getContext(),"Exercise already exists",Toast.LENGTH_SHORT).show();
                }
            }
        });
        TableLayout table = popupView.findViewById(R.id.table_layout);
        for(int i=0;i<focuses.size();i++){
            // add a checkbox for each focus that is available
            TableRow row = new TableRow(getActivity());
            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
            row.setLayoutParams(lp);
            final CheckBox focus = new CheckBox(getContext());
            focus.setText(focuses.get(i));
            focus.setOnClickListener(new View.OnClickListener() {
                boolean checked = focus.isChecked();
                @Override
                public void onClick(View v) {
                    if(checked){
                        selectedFocuses.remove(focus.getText().toString());
                    }
                    else{
                        selectedFocuses.add(focus.getText().toString());
                    }
                }
            });
            row.addView(focus);
            table.addView(row,i);
        }
        // show the popup
        alertDialog.setView(popupView);
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
    }

    public boolean validateNewExercise(TextView nameInput, TextView urlInput){
        // TODO do any validation on number of total exercises here? Absolute worst case scenario stuff but still
        String potentialName = nameInput.getText().toString();
        if(potentialName.isEmpty()){
            return false;
        }
        String potentialURL = nameInput.getText().toString();
        // loop over default to see if this exercise already exists in some focus
        for(String focus : defaultExercises.keySet()){
            for(String exercise : defaultExercises.get(focus)){
                if(exercise.equalsIgnoreCase(potentialName)){
                    return false;
                }
            }
        }
        for(String focus : customExercises.keySet()){
            for(String exercise : customExercises.get(focus)){
                if(exercise.equalsIgnoreCase(potentialName)){
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isModified(){
        return modifed;
    }
}
