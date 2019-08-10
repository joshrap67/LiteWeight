package com.example.workoutmadness.Fragments;

import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.support.v7.widget.SwitchCompat;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.workoutmadness.*;
import com.example.workoutmadness.Database.Entities.ExerciseEntity;
import com.example.workoutmadness.Database.ViewModels.ExerciseViewModel;

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
    private SwitchCompat videoSwitch, timerSwitch, filterSwitch;
    private Button importBtn, exportBtn;
    private ViewGroup fragmentContainer;
    private AlertDialog alertDialog;
    private ArrayList<String> focusList = new ArrayList<>();
    private HashMap<String,ArrayList<String>> exercises = new HashMap<>();
    private HashMap<String,ArrayList<String>> customExercises = new HashMap<>();
    private ArrayList<ExerciseEntity> exerciseEntities = new ArrayList<>();
    private SharedPreferences.Editor editor;
    private ExerciseViewModel exerciseViewModel;
    private SharedPreferences pref;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((MainActivity)getActivity()).updateToolbarTitle("Settings");
        view = inflater.inflate(R.layout.fragment_user_settings,container,false);
        fragmentContainer = container;
        videoSwitch = view.findViewById(R.id.video_switch);
        exerciseViewModel = ViewModelProviders.of(getActivity()).get(ExerciseViewModel.class);
        filterSwitch = view.findViewById(R.id.filter_switch);
        timerSwitch = view.findViewById(R.id.timer_switch);
        pref = getActivity().getApplicationContext().getSharedPreferences(Variables.SHARED_PREF_NAME, 0);
        editor = pref.edit();
        // have the switches setup here because otherwise there's a little bit of a delay due to the async task and it looks ugly
        timerSwitch.setChecked(pref.getBoolean(Variables.TIMER_KEY,true));
        timerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean(Variables.TIMER_KEY,isChecked);
                editor.apply();
            }
        });
        videoSwitch.setChecked(pref.getBoolean(Variables.VIDEO_KEY,true));
        videoSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean(Variables.VIDEO_KEY,isChecked);
                editor.apply();
            }
        });
        Switch unitSwitch = view.findViewById(R.id.unit_switch);
        unitSwitch.setChecked(pref.getBoolean(Variables.UNIT_KEY,false));
        unitSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean(Variables.UNIT_KEY,isChecked);
                editor.apply();
            }
        });
        GetExercisesTask task = new GetExercisesTask();
        task.execute();

        // todo make rows "raised" so they are clearly clickable?
        return view;
    }

    private class GetExercisesTask extends AsyncTask<Void, Void, ArrayList<ExerciseEntity>> {
        @Override
        protected void onPreExecute(){
            ((MainActivity)getActivity()).setProgressBar(false);
        }

        @Override
        protected ArrayList<ExerciseEntity> doInBackground(Void... voids) {
            // get the exercises from the database
            return exerciseViewModel.getAllExercises();
        }

        @Override
        protected void onPostExecute(ArrayList<ExerciseEntity> result) {
            ((MainActivity)getActivity()).setProgressBar(false);
            if(!result.isEmpty()){
                for(ExerciseEntity entity : result){
                    String[] focuses = entity.getFocus().split(Variables.FOCUS_DELIM_DB);
                    for(String focus : focuses){
                        if(!focusList.contains(focus)){
                            focusList.add(focus);
                            exercises.put(focus,new ArrayList<String>());
                            customExercises.put(focus,new ArrayList<String>());
                        }
                        exercises.get(focus).add(entity.getExerciseName());
                    }
                    exerciseEntities.add(entity);
                }
                initViews();
            }
            else{
                // uh oh
                Log.d("ERROR","Exercise table has is somehow empty!");
            }
        }
    }

    public void initViews(){
        filterCustom = false;
        filterSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterCustom = isChecked;
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
        populateFocusList();
    }

    public void populateFocusList(){
        final ListView listView = view.findViewById(R.id.focus_list);
        Collections.sort(focusList);
        ArrayAdapter arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_activated_1, focusList);
        listView.setAdapter(arrayAdapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                populateExercises(listView.getItemAtPosition(position).toString());
                Animation animation1 = new AlphaAnimation(0.3f, 1.0f);
                animation1.setDuration(50);
                view.startAnimation(animation1);
            }
        });
        // programmatically select first item
        listView.performItemClick(listView.getAdapter().getView(0, null, null), 0, 0);
        listView.setSelection(0);
    }

    public void populateExercises(String focus){
        final ListView listView = view.findViewById(R.id.exercise_list);
        ArrayList<String> exercisesTotal = new ArrayList<>();
        if(exercises.get(focus)==null){
            return;
        }
        if(!filterCustom){
            for(String exercise : exercises.get(focus)){
                exercisesTotal.add(exercise);
            }
        }
        for(String exercise : customExercises.get(focus)){
            exercisesTotal.add(exercise);
        }
        Collections.sort(exercisesTotal);
        ArrayAdapter arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, exercisesTotal);
        listView.setAdapter(arrayAdapter);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                editExercisePopup(listView.getItemAtPosition(position).toString());
            }
        });
    }
    // region
    // Popup methods
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
        for(int i=0;i<focusList.size();i++){
            // add a checkbox for each focus that is available
            TableRow row = new TableRow(getActivity());
            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
            row.setLayoutParams(lp);
            final CheckBox focus = new CheckBox(getContext());
            focus.setText(focusList.get(i));
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
        for(String focus : exercises.keySet()){
            for(String exercise : exercises.get(focus)){
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
    //endregion
    public boolean isModified(){
        return modifed;
    }
}
