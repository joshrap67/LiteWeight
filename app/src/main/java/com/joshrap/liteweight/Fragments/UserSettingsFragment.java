package com.joshrap.liteweight.Fragments;

import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

import com.joshrap.liteweight.*;
import com.joshrap.liteweight.Database.Entities.ExerciseEntity;
import com.joshrap.liteweight.Database.ViewModels.ExerciseViewModel;
import com.joshrap.liteweight.Database.ViewModels.WorkoutViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class UserSettingsFragment extends Fragment {
    private AlertDialog alertDialog, rootDialog;
    private ArrayAdapter exerciseAdapter;
    private SharedPreferences.Editor editor;
    private SwitchCompat filterSwitch;
    private View view, popupView;
    private ExerciseViewModel exerciseViewModel;
    private WorkoutViewModel workoutViewModel;
    private boolean filterCustom, metricUnits;
    private int customExerciseCount = 0;
    private String selectedFocus;
    private ArrayList<String> focusList = new ArrayList<>();
    private ArrayList<String> exercisesForSelectedFocus = new ArrayList<>();
    private HashMap<String, ArrayList<String>> defaultExercises = new HashMap<>();
    private HashMap<String, ArrayList<String>> customExercises = new HashMap<>();
    private HashMap<String, ExerciseEntity> exerciseNameToEntity = new HashMap<>();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((MainActivity) getActivity()).updateToolbarTitle(Variables.SETTINGS_TITLE);
        view = inflater.inflate(R.layout.fragment_user_settings, container, false);
        SwitchCompat videoSwitch = view.findViewById(R.id.video_switch);
        SwitchCompat timerSwitch = view.findViewById(R.id.timer_switch);
        exerciseViewModel = ViewModelProviders.of(getActivity()).get(ExerciseViewModel.class);
        workoutViewModel = ViewModelProviders.of(getActivity()).get(WorkoutViewModel.class);
        filterSwitch = view.findViewById(R.id.filter_switch);
        SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences(Variables.SHARED_PREF_NAME, 0);
        editor = pref.edit();
        // have the switches setup here because otherwise there's a little bit of a delay due to the async task and it looks ugly
        timerSwitch.setChecked(pref.getBoolean(Variables.TIMER_KEY, true));
        timerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean(Variables.TIMER_KEY, isChecked);
                editor.apply();
            }
        });
        videoSwitch.setChecked(pref.getBoolean(Variables.VIDEO_KEY, true));
        videoSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean(Variables.VIDEO_KEY, isChecked);
                editor.apply();
            }
        });
        Switch unitSwitch = view.findViewById(R.id.unit_switch);
        unitSwitch.setChecked(pref.getBoolean(Variables.UNIT_KEY, false));
        unitSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                metricUnits = isChecked;
                editor.putBoolean(Variables.UNIT_KEY, isChecked);
                editor.apply();
            }
        });
        GetExercisesTask task = new GetExercisesTask();
        task.execute();
        return view;
    }

    private class GetExercisesTask extends AsyncTask<Void, Void, ArrayList<ExerciseEntity>> {
        @Override
        protected void onPreExecute(){
            ((MainActivity)getActivity()).setProgressBar(false);
        }

        @Override
        protected ArrayList<ExerciseEntity> doInBackground(Void... voids) {
            // get the defaultExercises from the database
            return exerciseViewModel.getAllExercises();
        }

        @Override
        protected void onPostExecute(ArrayList<ExerciseEntity> result) {
            ((MainActivity) getActivity()).setProgressBar(false);
            if(!result.isEmpty()){
                for(ExerciseEntity entity : result){
                    String[] focuses = entity.getFocus().split(Variables.FOCUS_DELIM_DB);
                    if(!entity.isDefaultExercise()){
                        // do the count here to avoid double counting if the exercise is in more than one focus
                        customExerciseCount++;
                    }
                    for(String focus : focuses){
                        if(!focusList.contains(focus)){
                            // found a new focus, so init the hash map with it
                            focusList.add(focus);
                            defaultExercises.put(focus,new ArrayList<String>());
                            customExercises.put(focus,new ArrayList<String>());
                        }
                        if(entity.isDefaultExercise()){
                            defaultExercises.get(focus).add(entity.getExerciseName());
                        }
                        else{
                            customExercises.get(focus).add(entity.getExerciseName());
                        }
                    }
                    exerciseNameToEntity.put(entity.getExerciseName(),entity);
                }
                ((MainActivity)getActivity()).setProgressBar(false);
                initViews();
            }
        }
    }

    public void initViews(){
        /*
            Once all exercises are retrieved from the DB, init the views
         */
        filterCustom = false;
        filterSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // allow for custom exercises to be filtered
                filterCustom = isChecked;
                populateFocusListView();
            }
        });

        Button createBtn = view.findViewById(R.id.new_exercise_btn);
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!(customExerciseCount > Variables.MAX_NUMBER_OF_CUSTOM_EXERCISES)){
                    newExercisePopup();
                }
                else{
                    Toast.makeText(getContext(),"You already have the max number ("+ Variables.MAX_NUMBER_OF_CUSTOM_EXERCISES +
                                    ") of custom exercises allowed!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        Collections.sort(focusList);
        selectedFocus = focusList.get(0); // initially select first focus
        populateFocusListView();
    }

    public void populateFocusListView(){
        /*
            Populates the focus list view
         */
        final ListView listView = view.findViewById(R.id.focus_list);
        ArrayAdapter arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_activated_1, focusList);
        listView.setAdapter(arrayAdapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedFocus = listView.getItemAtPosition(position).toString();
                populateExercisesListView();
                // provide a "clicking" animation
                Animation animation1 = new AlphaAnimation(0.3f, 1.0f);
                animation1.setDuration(50);
                view.startAnimation(animation1);
            }
        });
        // programmatically select selected focus
        listView.performItemClick(listView.getAdapter().getView(focusList.indexOf(selectedFocus), null, null),
                focusList.indexOf(selectedFocus), focusList.indexOf(selectedFocus));
        listView.setSelection(focusList.indexOf(selectedFocus));
    }

    public void populateExercisesListView(){
        /*
            Populates the exercise list view based on the selected focus
         */
        final ListView listView = view.findViewById(R.id.exercise_list);
        exercisesForSelectedFocus = new ArrayList<>();
        if(defaultExercises.get(selectedFocus) == null){
            return;
        }
        if(!filterCustom){
            for(String exercise : defaultExercises.get(selectedFocus)){
                exercisesForSelectedFocus.add(exercise);
            }
        }
        for(String exercise : customExercises.get(selectedFocus)){
            exercisesForSelectedFocus.add(exercise);
        }
        Collections.sort(exercisesForSelectedFocus);
        exerciseAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, exercisesForSelectedFocus);
        listView.setAdapter(exerciseAdapter);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = listView.getItemAtPosition(position).toString();
                if(defaultExercises.get(selectedFocus).contains(name)){
                    editDefaultExercisePopup(name);
                }
                else{
                    editCustomExercisePopup(name);
                }
            }
        });
    }
    // region
    // Popup methods
    public void newExercisePopup(){
        /*
            Popup for creating a new exercise
         */
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
                String exerciseName = exerciseNameInput.getText().toString();
                String url = editURL.getText().toString();
                if(selectedFocuses.size()==0){
                    Toast.makeText(getContext(),"Select at least one focus!",Toast.LENGTH_SHORT).show();
                }
                else if(!url.isEmpty() && Validator.checkValidURL(url)!=null){
                    // allow for url to be empty since most people won't want to upload a video
                    editURL.setError(Validator.checkValidURL(editURL.getText().toString()));
                }
                else if(validateNewExerciseName(exerciseNameInput)){
                    StringBuilder sb = new StringBuilder();
                    for(int i=0;i<selectedFocuses.size();i++){
                        customExercises.get(selectedFocuses.get(i)).add(exerciseNameInput.getText().toString());
                        sb.append(selectedFocuses.get(i) + ((i == selectedFocuses.size() - 1) ? "" : ","));
                    }
                    String focusEntry=sb.toString();
                    ExerciseEntity newEntity = new ExerciseEntity(exerciseName,focusEntry,url,false,0,
                            0,0,0);
                    exerciseViewModel.insert(newEntity);
                    exerciseNameToEntity.put(exerciseName,newEntity);
                    Toast.makeText(getContext(),"Exercise successfully created!",Toast.LENGTH_SHORT).show();
                    alertDialog.dismiss();
                }
            }
        });
        TableLayout focusTable = popupView.findViewById(R.id.table_layout);
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
            focusTable.addView(row,i);
        }
        // show the popup
        alertDialog.setView(popupView);
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
    }

    public void editDefaultExercisePopup(final String name){
        /*
            Show the popup for clicking on a default exercise
         */
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        rootDialog = alertDialogBuilder.create();
        popupView = getLayoutInflater().inflate(R.layout.popup_edit_default_exercise, null);
        rootDialog.setView(popupView);
        rootDialog.setCanceledOnTouchOutside(true);
        rootDialog.show();
        TextView exerciseName = popupView.findViewById(R.id.exercise_name);
        exerciseName.setText(name);
        Button editURLBtn = popupView.findViewById(R.id.edit_url_btn);
        editURLBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editURL(name);
            }
        });
        Button editWeightBtn = popupView.findViewById(R.id.edit_weight_btn);
        editWeightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editWeight(name);
            }
        });
        Button backBtn = popupView.findViewById(R.id.back_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rootDialog.dismiss();
            }
        });
    }

    public void editCustomExercisePopup(final String name){
        /*
            Show the popup for clicking on a custom exercise
         */
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        rootDialog = alertDialogBuilder.create();
        View popupView = getLayoutInflater().inflate(R.layout.popup_edit_custom_exercise, null);
        rootDialog.setView(popupView);
        rootDialog.setCanceledOnTouchOutside(true);
        rootDialog.show();
        TextView exerciseName = popupView.findViewById(R.id.exercise_name);
        exerciseName.setText(name);
        Button editURLBtn = popupView.findViewById(R.id.edit_url_btn);
        editURLBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editURL(name);
            }
        });
        Button renameBtn = popupView.findViewById(R.id.rename_btn);
        renameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                renameExercisePopup(name);
            }
        });
        Button deleteBtn = popupView.findViewById(R.id.delete_btn);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteExercisePopup(name);
            }
        });
        Button editWeightBtn = popupView.findViewById(R.id.edit_weight_btn);
        editWeightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editWeight(name);
            }
        });
        Button backButton = popupView.findViewById(R.id.back_btn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rootDialog.dismiss();
            }
        });
    }
    public void editURL(final String name){
        /*
            Edit the URL of a specific exercise
         */
        final ExerciseEntity entity = exerciseNameToEntity.get(name);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialog = alertDialogBuilder.create();
        popupView = getLayoutInflater().inflate(R.layout.popup_edit_url, null);
        alertDialog.setView(popupView);
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
        TextView exerciseName = popupView.findViewById(R.id.exercise_name);
        exerciseName.setText(name);
        String oldUrl = entity.getUrl();
        if(oldUrl.isEmpty()){
            oldUrl = "No URL found";
        }
        TextView oldUrlTV = popupView.findViewById(R.id.old_url);
        oldUrlTV.setText(oldUrl);
        final EditText urlInput = popupView.findViewById(R.id.edit_url_txt);
        Button backButton = popupView.findViewById(R.id.back_btn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        Button doneButton = popupView.findViewById(R.id.done_btn);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String potentialURL = urlInput.getText().toString().trim();
                String errorMsg = Validator.checkValidURL(potentialURL);
                if(errorMsg == null) {
                    entity.setUrl(potentialURL);
                    exerciseViewModel.update(entity);
                    alertDialog.dismiss();
                }
                else {
                    urlInput.setError(errorMsg);
                }
            }
        });

    }

    public void editWeight(final String name){
        /*
            Used to edit either default or custom exercise current weights
         */
        final ExerciseEntity entity = exerciseNameToEntity.get(name);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialog = alertDialogBuilder.create();
        popupView = getLayoutInflater().inflate(R.layout.popup_edit_weight, null);
        alertDialog.setView(popupView);
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
        TextView exerciseName = popupView.findViewById(R.id.exercise_name);
        final EditText weightInput = popupView.findViewById(R.id.name_input);
        Button doneButton = popupView.findViewById(R.id.done_btn);
        Button backButton = popupView.findViewById(R.id.back_btn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        final Switch ignoreWeightSwitch = popupView.findViewById(R.id.ignore_weight_switch);
        ignoreWeightSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    weightInput.setVisibility(View.GONE);
                }
                else{
                    weightInput.setVisibility(View.VISIBLE);
                }
            }
        });
        exerciseName.setText(name);
        double weight;
        if(metricUnits){
            // value in DB is always in murican units
            weight = entity.getCurrentWeight() * Variables.KG;
        }
        else{
            weight = entity.getCurrentWeight();
        }
        String formattedWeight = Validator.getFormattedWeight(weight);
        if(weight >= 0){
            weightInput.setHint(formattedWeight+(metricUnits?" kg":" lb"));
        }
        else{
            ignoreWeightSwitch.setChecked(true);
            weightInput.setHint("N/A");
        }
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ignoreWeightSwitch.isChecked()){
                    // means that we're ignoring weight
                    entity.setCurrentWeight(Variables.IGNORE_WEIGHT_VALUE);
                    exerciseViewModel.update(entity);
                    alertDialog.dismiss();
                }
                else if(!weightInput.getText().toString().equals("")){
                    double aWeight = Double.parseDouble(weightInput.getText().toString());
                    if(metricUnits){
                        // convert if in metric
                        aWeight /= Variables.KG;
                    }
                    if(aWeight > entity.getMaxWeight()){
                        entity.setMaxWeight(aWeight);
                    }
                    else if(aWeight < entity.getMinWeight()){
                        entity.setMinWeight(aWeight);
                    }
                    entity.setCurrentWeight(aWeight);
                    exerciseViewModel.update(entity);
                    alertDialog.dismiss();
                }
                else{
                    Toast.makeText(getActivity(),"Enter a valid weight!",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    public void renameExercisePopup(final String name){
        /*
            Used to rename a custom exercise
         */
        final ExerciseEntity entity = exerciseNameToEntity.get(name);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialog = alertDialogBuilder.create();
        popupView = getLayoutInflater().inflate(R.layout.popup_rename_custom_exercise, null);
        alertDialog.setView(popupView);
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
        TextView exerciseName = popupView.findViewById(R.id.exercise_name);
        exerciseName.setText(name);
        final EditText nameInput = popupView.findViewById(R.id.name_input);
        // TODO put max limit here and not in XML
        Button doneBtn = popupView.findViewById(R.id.done_btn);
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateNewExerciseName(nameInput)){
                    String newName = nameInput.getText().toString().trim();
                    entity.setExerciseName(newName);
                    exerciseViewModel.update(entity);
                    workoutViewModel.updateExerciseName(name, newName); // replace all occurrences of this exercise in any workouts in DB
                    exerciseNameToEntity.remove(name);
                    exerciseNameToEntity.put(newName, entity);
                    exercisesForSelectedFocus.remove(name);
                    exercisesForSelectedFocus.add(newName);
                    Collections.sort(exercisesForSelectedFocus);
                    exerciseAdapter.notifyDataSetChanged();
                    alertDialog.dismiss();
                }
            }
        });
        Button backBtn = popupView.findViewById(R.id.back_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
    }

    public void deleteExercisePopup(final String name){
        /*
            Used to delete a custom exercise. Removes it from the DB and also from the listview in this fragment
         */
        final ExerciseEntity entity = exerciseNameToEntity.get(name);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialog = alertDialogBuilder.create();
        popupView = getLayoutInflater().inflate(R.layout.popup_delete_custom_exercise, null);
        alertDialog.setView(popupView);
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
        TextView exerciseName = popupView.findViewById(R.id.exercise_name);
        String msg = getActivity().getResources().getString(R.string.delete) + name;
        exerciseName.setText(msg);
        Button deleteConfirm = popupView.findViewById(R.id.delete_confirm);
        deleteConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exerciseNameToEntity.remove(name);
                for(String focus : customExercises.keySet()){
                    customExercises.get(focus).remove(name);
                }
                exerciseViewModel.delete(entity);
                workoutViewModel.deleteExerciseFromWorkouts(name);
                alertDialog.dismiss();
                rootDialog.dismiss();
                populateExercisesListView();
            }
        });
        Button deleteDenial = popupView.findViewById(R.id.delete_denial);
        deleteDenial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
    }

    private boolean validateNewExerciseName(TextView nameInput){
        /*
            Validates the input for a new exercise and if an error is found, an appropriate message is displayed
            on the EditText
         */
        String potentialName = nameInput.getText().toString().trim();
        if(potentialName.isEmpty()){
            nameInput.setError("Exercise must have a name!");
            return false;
        }
        // loop over default to see if this exercise already exists in some focus
        for(String focus : defaultExercises.keySet()){
            for(String exercise : defaultExercises.get(focus)){
                if(exercise.equalsIgnoreCase(potentialName)){
                    nameInput.setError("Exercise already exists!");
                    return false;
                }
            }
        }
        for(String focus : customExercises.keySet()){
            for(String exercise : customExercises.get(focus)){
                if(exercise.equalsIgnoreCase(potentialName)){
                    nameInput.setError("Exercise already exists!");
                    return false;
                }
            }
        }
        return true;
    }
    //endregion
}
