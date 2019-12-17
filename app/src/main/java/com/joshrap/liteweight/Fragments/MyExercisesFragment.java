package com.joshrap.liteweight.Fragments;

import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
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
import android.widget.ImageButton;
import android.widget.ListView;
import android.support.v7.widget.SwitchCompat;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.joshrap.liteweight.*;
import com.joshrap.liteweight.Adapters.ExerciseAdapter;
import com.joshrap.liteweight.Database.Entities.ExerciseEntity;
import com.joshrap.liteweight.Database.ViewModels.ExerciseViewModel;
import com.joshrap.liteweight.Database.ViewModels.WorkoutViewModel;
import com.joshrap.liteweight.Globals.Variables;
import com.joshrap.liteweight.Helpers.ExerciseHelper;
import com.joshrap.liteweight.Helpers.InputHelper;
import com.joshrap.liteweight.Helpers.WeightHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import static android.content.Context.CLIPBOARD_SERVICE;

public class MyExercisesFragment extends Fragment {
    private AlertDialog alertDialog, rootDialog;
    private ArrayAdapter exerciseAdapter;
    private View view;
    private ExerciseViewModel exerciseViewModel;
    private WorkoutViewModel workoutViewModel;
    private boolean filterCustom, metricUnits;
    private int customExerciseCount = 0;
    private String selectedFocus;
    private ArrayList<String> focusList = new ArrayList<>();
    private ArrayList<ExerciseEntity> exercisesForSelectedFocus = new ArrayList<>();
    private HashMap<String, ArrayList<ExerciseEntity>> totalExercises = new HashMap<>();
    private ClipboardManager clipboard;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((MainActivity) getActivity()).updateToolbarTitle(Variables.MY_EXERCISES_TITLE);
        clipboard = (ClipboardManager) getActivity().getSystemService(CLIPBOARD_SERVICE);
        view = inflater.inflate(R.layout.fragment_my_exercises, container, false);
        exerciseViewModel = ViewModelProviders.of(getActivity()).get(ExerciseViewModel.class);
        workoutViewModel = ViewModelProviders.of(getActivity()).get(WorkoutViewModel.class);
        SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences(Variables.SHARED_PREF_NAME, 0);
        metricUnits = pref.getBoolean(Variables.UNIT_KEY, false);
        // have the switches setup here because otherwise there's a little bit of a delay due to the async task and it looks ugly
        GetExercisesTask task = new GetExercisesTask();
        task.execute();
        return view;
    }

    private class GetExercisesTask extends AsyncTask<Void, Void, ArrayList<ExerciseEntity>> {
        @Override
        protected void onPreExecute() {
            ((MainActivity) getActivity()).setProgressBar(false);
        }

        @Override
        protected ArrayList<ExerciseEntity> doInBackground(Void... voids) {
            // get the defaultExercises from the database
            return exerciseViewModel.getAllExercises();
        }

        @Override
        protected void onPostExecute(ArrayList<ExerciseEntity> result) {
            ((MainActivity) getActivity()).setProgressBar(false);
            if (!result.isEmpty()) {
                for (ExerciseEntity entity : result) {
                    String[] focuses = entity.getFocus().split(Variables.FOCUS_DELIM_DB);
                    if (!entity.isDefaultExercise()) {
                        // do the count here to avoid double counting if the exercise is in more than one focus
                        customExerciseCount++;
                    }
                    for (String focus : focuses) {
                        if (!focusList.contains(focus)) {
                            // found a new focus, so init the hash map with it
                            focusList.add(focus);
                            totalExercises.put(focus, new ArrayList<ExerciseEntity>());
                        }
                        totalExercises.get(focus).add(entity);
                    }
                }
                ((MainActivity) getActivity()).setProgressBar(false);
                initViews();
            }
        }
    }

    public void initViews() {
        /*
            Once all exercises are retrieved from the DB, init the views
         */
        filterCustom = false;
        SwitchCompat filterSwitch = view.findViewById(R.id.filter_switch);
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
                if (!(customExerciseCount > Variables.MAX_NUMBER_OF_CUSTOM_EXERCISES)) {
                    newExercisePopup();
                } else {
                    Toast.makeText(getContext(), "You already have the max number (" + Variables.MAX_NUMBER_OF_CUSTOM_EXERCISES +
                            ") of custom exercises allowed!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        Collections.sort(focusList);
        selectedFocus = focusList.get(0); // initially select first focus
        populateFocusListView();
    }

    public void populateFocusListView() {
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

    public void populateExercisesListView() {
        /*
            Populates the exercise list view based on the selected focus
         */
        final ListView listView = view.findViewById(R.id.exercise_list);
        exercisesForSelectedFocus = new ArrayList<>();
        if (totalExercises.get(selectedFocus) == null) {
            return;
        }
        if (!filterCustom) {
            exercisesForSelectedFocus.addAll(totalExercises.get(selectedFocus));
        } else {
            for (ExerciseEntity entity : totalExercises.get(selectedFocus)) {
                if (!entity.isDefaultExercise()) {
                    exercisesForSelectedFocus.add(entity);
                }
            }
        }
        Collections.sort(exercisesForSelectedFocus);
        exerciseAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, exercisesForSelectedFocus);
        ExerciseAdapter adapter = new ExerciseAdapter(getContext(), exercisesForSelectedFocus);
        listView.setAdapter(adapter);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ExerciseEntity exercise = (ExerciseEntity) listView.getItemAtPosition(position);
                editExercise(exercise);
            }
        });
    }

    // region
    // Popup methods
    public void newExercisePopup() {
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
                String exerciseName = exerciseNameInput.getText().toString().trim();
                String url = editURL.getText().toString();
                if (selectedFocuses.size() == 0) {
                    Toast.makeText(getContext(), "Select at least one focus!", Toast.LENGTH_SHORT).show();
                } else if (!url.isEmpty() && InputHelper.checkValidURL(url) != null) {
                    // allow for url to be empty since most people won't want to upload a video
                    editURL.setError(InputHelper.checkValidURL(editURL.getText().toString()));
                } else if (validateNewExerciseName(exerciseNameInput)) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < selectedFocuses.size(); i++) {
                        sb.append(selectedFocuses.get(i) + ((i == selectedFocuses.size() - 1) ? "" : ","));
                    }
                    String focusEntry = sb.toString();
                    ExerciseEntity newEntity = new ExerciseEntity(exerciseName, focusEntry, url, false, 0,
                            0, 0, 0);
                    for (int i = 0; i < selectedFocuses.size(); i++) {
                        totalExercises.get(selectedFocuses.get(i)).add(newEntity);
                    }
                    exerciseViewModel.insert(newEntity);
                    populateExercisesListView();
                    Toast.makeText(getContext(), "Exercise successfully created!", Toast.LENGTH_SHORT).show();
                    alertDialog.dismiss();
                }
            }
        });
        TableLayout focusTable = popupView.findViewById(R.id.table_layout);
        for (int i = 0; i < focusList.size(); i++) {
            // add a checkbox for each focus that is available
            TableRow row = new TableRow(getActivity());
            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
            row.setLayoutParams(lp);
            final CheckBox focus = new CheckBox(getContext());
            focus.setText(focusList.get(i));
            focus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!focus.isChecked()) {
                        selectedFocuses.remove(focus.getText().toString());
                    } else {
                        selectedFocuses.add(focus.getText().toString());
                    }
                }
            });
            row.addView(focus);
            focusTable.addView(row, i);
        }
        // show the popup
        alertDialog.setView(popupView);
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
    }

    public void editExercise(final ExerciseEntity exercise) {
        /*
            Show the popup for editing an exercise. If it is a default one, hide the ability to rename/delete
         */
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        rootDialog = alertDialogBuilder.create();
        View popupView = getLayoutInflater().inflate(R.layout.popup_edit_exercise, null);
        rootDialog.setView(popupView);
        rootDialog.setCanceledOnTouchOutside(true);
        rootDialog.show();
        if (exercise.isDefaultExercise()) {
            popupView.findViewById(R.id.rename_layout).setVisibility(View.GONE);
            popupView.findViewById(R.id.delete_btn).setVisibility(View.GONE);
        }
        ImageButton backButton = popupView.findViewById(R.id.back_btn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rootDialog.dismiss();
            }
        });

        final TextView exerciseNameTV = popupView.findViewById(R.id.exercise_name);
        exerciseNameTV.setText(exercise.getExerciseName());

        final Button renameBtn = popupView.findViewById(R.id.rename_btn);
        final EditText renameInput = popupView.findViewById(R.id.rename_input);
        renameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals(exercise.getExerciseName()) || s.toString().isEmpty()) {
                    renameBtn.setVisibility(View.GONE);
                } else {
                    renameBtn.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        renameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                renameExercise(exercise, renameInput, exerciseNameTV, renameBtn);
            }
        });
        renameBtn.setVisibility(View.GONE);

        final Button weightBtn = popupView.findViewById(R.id.save_weight_btn);
        final EditText weightInput = popupView.findViewById(R.id.weight_input);
        double weight = WeightHelper.convertWeight(metricUnits, exercise);
        setWeightHint(weightInput, weight);

        weightInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty()) {
                    weightBtn.setVisibility(View.GONE);
                } else if (Double.parseDouble(s.toString()) == exercise.getCurrentWeight()) {
                    weightBtn.setVisibility(View.GONE);
                } else {
                    weightBtn.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        final Switch ignoreWeightSwitch = popupView.findViewById(R.id.ignore_weight_switch);
        if (weight == Variables.IGNORE_WEIGHT_VALUE) {
            weightInput.setVisibility(View.GONE);
            ignoreWeightSwitch.setChecked(true);
        }
        ignoreWeightSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    weightInput.setVisibility(View.GONE);
                    if (!(exercise.getCurrentWeight() == Variables.IGNORE_WEIGHT_VALUE)) {
                        weightBtn.setVisibility(View.VISIBLE);
                    }
                } else {
                    weightInput.setVisibility(View.VISIBLE);
                    if (!(exercise.getCurrentWeight() == Variables.IGNORE_WEIGHT_VALUE)) {
                        weightBtn.setVisibility(View.GONE);
                    }
                }
            }
        });
        // this button will only appear when the weight is different than the one in the DB
        weightBtn.setVisibility(View.GONE);
        weightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveWeight(exercise, weightInput, ignoreWeightSwitch.isChecked(), weightBtn);
            }
        });
        // set up url widgets

        final Button clipboardBtn = popupView.findViewById(R.id.clipboard_btn);
        final Button saveUrlBtn = popupView.findViewById(R.id.save_url_btn);
        saveUrlBtn.setVisibility(View.GONE); // initially hide it because no input is given

        final Button previewBtn = popupView.findViewById(R.id.preview_btn);
        previewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExerciseHelper.launchVideo(exercise, getContext(), getActivity());
            }
        });
        if (exercise.getUrl().isEmpty()) {
            clipboardBtn.setVisibility(View.GONE);
            previewBtn.setVisibility(View.GONE);
        }
        clipboardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clipboard.setPrimaryClip(new ClipData(ClipData.newPlainText("url", exercise.getUrl())));
                Toast.makeText(getContext(), "Link copied to clipboard!", Toast.LENGTH_SHORT).show();
            }
        });
        final EditText urlInput = popupView.findViewById(R.id.url_input);
        urlInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty()) {
                    saveUrlBtn.setVisibility(View.GONE);
                    clipboardBtn.setVisibility(View.VISIBLE);
                    previewBtn.setVisibility(View.VISIBLE);
                } else if (s.toString().equals(exercise.getUrl())) {
                    clipboardBtn.setVisibility(View.GONE);
                    previewBtn.setVisibility(View.GONE);
                    saveUrlBtn.setVisibility(View.GONE);
                } else {
                    clipboardBtn.setVisibility(View.GONE);
                    previewBtn.setVisibility(View.GONE);
                    saveUrlBtn.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        saveUrlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUrl(exercise, urlInput);
            }
        });
        Button deleteBtn = popupView.findViewById(R.id.delete_btn);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteExercisePopup(exercise);
            }
        });

    }

    public void renameExercise(ExerciseEntity exercise, EditText nameInput, TextView exerciseNameTV, Button renameBtn) {
        if (validateNewExerciseName(nameInput)) {
            String newName = nameInput.getText().toString().trim();
            workoutViewModel.updateExerciseName(exercise.getExerciseName(), newName); // replace all occurrences of this exercise in any workouts in DB
            exercise.setExerciseName(newName);
            exerciseViewModel.update(exercise);
            Collections.sort(exercisesForSelectedFocus);
            exerciseAdapter.notifyDataSetChanged();
            exerciseNameTV.setText(newName);
            nameInput.setText("");
            renameBtn.setVisibility(View.GONE);
        }
    }

    public void saveWeight(ExerciseEntity exercise, final EditText weightInput, boolean ignoreWeight, final Button weightBtn) {
        if (ignoreWeight) {
            exercise.setCurrentWeight(Variables.IGNORE_WEIGHT_VALUE);
            exerciseViewModel.update(exercise);
            weightBtn.setVisibility(View.GONE);
            weightInput.setHint("Enter Weight");
        } else if (!weightInput.getText().toString().isEmpty()) {
            double aWeight = Double.parseDouble(weightInput.getText().toString());
            if (metricUnits) {
                // convert if in metric since values in DB are always murican units
                aWeight /= Variables.KG;
            }
            if (aWeight > exercise.getMaxWeight()) {
                exercise.setMaxWeight(aWeight);
            } else if (aWeight < exercise.getMinWeight() || exercise.getMinWeight() == 0) {
                exercise.setMinWeight(aWeight);
            }
            exercise.setCurrentWeight(aWeight);
            exerciseViewModel.update(exercise);
            // update the text field to show the new weight
            weightInput.setText("");
            aWeight = WeightHelper.convertWeight(metricUnits, exercise);
            setWeightHint(weightInput, aWeight);
        } else {
            Toast.makeText(getActivity(), "Enter a valid weight!", Toast.LENGTH_SHORT).show();
        }
    }

    public void setWeightHint(EditText weightInput, double weight) {
        String formattedWeight = WeightHelper.getFormattedWeight(weight);
        if (weight >= 0) {
            weightInput.setHint(formattedWeight + (metricUnits ? " kg" : " lb"));
        } else {
            weightInput.setHint("Enter Weight");
        }
    }

    public void saveUrl(ExerciseEntity exercise, EditText urlInput) {
        String potentialURL = urlInput.getText().toString().trim();
        String errorMsg = InputHelper.checkValidURL(potentialURL);
        if (errorMsg == null) {
            exercise.setUrl(potentialURL);
            exerciseViewModel.update(exercise);
        } else {
            urlInput.setError(errorMsg);
        }
    }

    public void deleteExercisePopup(final ExerciseEntity exercise) {
        /*
            Used to delete a custom exercise. Removes it from the DB and also from the listview in this fragment
         */
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialog = alertDialogBuilder.create();
        View popupView = getLayoutInflater().inflate(R.layout.popup_delete_custom_exercise, null);
        alertDialog.setView(popupView);
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
        TextView exerciseName = popupView.findViewById(R.id.exercise_name);
        String msg = getActivity().getResources().getString(R.string.delete) + " " + exercise.getExerciseName();
        exerciseName.setText(msg);
        Button deleteConfirm = popupView.findViewById(R.id.delete_confirm);
        deleteConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (String focus : totalExercises.keySet()) {
                    totalExercises.get(focus).remove(exercise);
                }
                workoutViewModel.deleteExerciseFromWorkouts(exercise.getExerciseName());
                exerciseViewModel.delete(exercise);
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

    private boolean validateNewExerciseName(TextView nameInput) {
        /*
            Validates the input for a new exercise and if an error is found, an appropriate message is displayed
            on the EditText
         */
        String potentialName = nameInput.getText().toString().trim();
        if (potentialName.isEmpty()) {
            nameInput.setError("Exercise must have a name!");
            return false;
        }
        // loop over default to see if this exercise already exists in some focus
        for (String focus : totalExercises.keySet()) {
            for (ExerciseEntity exercise : totalExercises.get(focus)) {
                if (exercise.getExerciseName().equalsIgnoreCase(potentialName)) {
                    nameInput.setError("Exercise already exists!");
                    return false;
                }
            }
        }
        return true;
    }
    //endregion
}
