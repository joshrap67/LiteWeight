package com.joshrap.liteweight.fragments;

import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputFilter;
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
import com.joshrap.liteweight.activities.MainActivity;
import com.joshrap.liteweight.adapters.ExerciseAdapter;
import com.joshrap.liteweight.database.entities.ExerciseEntity;
import com.joshrap.liteweight.database.viewModels.ExerciseViewModel;
import com.joshrap.liteweight.database.viewModels.WorkoutViewModel;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.helpers.ExerciseHelper;
import com.joshrap.liteweight.helpers.InputHelper;
import com.joshrap.liteweight.helpers.WeightHelper;
import com.joshrap.liteweight.interfaces.FragmentWithDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.Future;

import static android.content.Context.CLIPBOARD_SERVICE;

public class MyExercisesFragment extends Fragment implements FragmentWithDialog {
    private ExerciseAdapter exerciseAdapter;
    private View view;
    private ExerciseViewModel exerciseViewModel;
    private WorkoutViewModel workoutViewModel;
    private boolean filterDefault, metricUnits;
    private int customExerciseCount = 0;
    private String selectedFocus;
    private ArrayList<String> focusList = new ArrayList<>();
    private ArrayList<ExerciseEntity> exercisesForSelectedFocus = new ArrayList<>();
    private HashMap<String, ArrayList<ExerciseEntity>> totalExercises = new HashMap<>();
    private ClipboardManager clipboard;
    private AlertDialog alertDialog, deleteDialog;
    private GetExercisesTask getExercisesTask;
    private NewExerciseTask newExerciseTask;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((MainActivity) getActivity()).updateToolbarTitle(Variables.MY_EXERCISES_TITLE);
        clipboard = (ClipboardManager) getActivity().getSystemService(CLIPBOARD_SERVICE);
        SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences(Variables.SHARED_PREF_SETTINGS, 0);
        metricUnits = pref.getBoolean(Variables.UNIT_KEY, false);
        if (view != null) {
            // if the fragment has already been created, use the old view. Preserves previous user action like filtering default exercises
            return view;
        }
        view = inflater.inflate(R.layout.fragment_my_exercises, container, false);
        exerciseViewModel = ViewModelProviders.of(getActivity()).get(ExerciseViewModel.class);
        workoutViewModel = ViewModelProviders.of(getActivity()).get(WorkoutViewModel.class);
        // determine if metric units are enabled or not
        getExercisesTask = new GetExercisesTask();
        getExercisesTask.execute();
        return view;
    }

    @Override
    public void onDestroy() {
        if (getExercisesTask != null) {
            getExercisesTask.cancel(true);
        }
        if (newExerciseTask != null) {
            newExerciseTask.cancel(true);
        }
        super.onDestroy();
    }

    @Override
    public void hideAllDialogs() {
        /*
            Close any dialogs that might be showing. This is essential when clicking a notification that takes
            the user to a new page.
         */
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        if (deleteDialog != null && deleteDialog.isShowing()) {
            deleteDialog.dismiss();
        }
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

    private void initViews() {
        /*
            Once all exercises are retrieved from the DB, init the views
         */
        filterDefault = false;
        SwitchCompat filterSwitch = view.findViewById(R.id.filter_switch);
        filterSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // allow for custom exercises to be filtered
                filterDefault = isChecked;
                populateFocusListView();
            }
        });

        FloatingActionButton createBtn = view.findViewById(R.id.new_exercise_btn);
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

    private void populateFocusListView() {
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

    private void populateExercisesListView() {
        /*
            Populates the exercise list view based on the selected focus
         */
        final ListView listView = view.findViewById(R.id.exercise_list);
        exercisesForSelectedFocus = new ArrayList<>();
        if (totalExercises.get(selectedFocus) == null) {
            return;
        }
        if (!filterDefault) {
            exercisesForSelectedFocus.addAll(totalExercises.get(selectedFocus));
        } else {
            for (ExerciseEntity entity : totalExercises.get(selectedFocus)) {
                if (!entity.isDefaultExercise()) {
                    exercisesForSelectedFocus.add(entity);
                }
            }
        }
        Collections.sort(exercisesForSelectedFocus);
        exerciseAdapter = new ExerciseAdapter(getContext(), exercisesForSelectedFocus);
        listView.setAdapter(exerciseAdapter);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ExerciseEntity exercise = (ExerciseEntity) listView.getItemAtPosition(position);
                editExercise(exercise);
            }
        });
    }

    // region Popup methods
    private void newExercisePopup() {
        /*
            Popup for creating a new exercise
         */
        final ArrayList<String> selectedFocuses = new ArrayList<>();
        View popupView = getLayoutInflater().inflate(R.layout.popup_new_exercise, null);
        final EditText exerciseNameInput = popupView.findViewById(R.id.edit_name_txt);
        exerciseNameInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_EXERCISE_NAME)});
        final EditText editURL = popupView.findViewById(R.id.edit_url_txt);
        editURL.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_URL_LENGTH)});

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
        // view is all set up, so now create the dialog with it
        alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                .setTitle("Create Exercise")
                .setView(popupView)
                .setNegativeButton("Back", null)
                .setPositiveButton("Save", null)
                .create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String exerciseName = exerciseNameInput.getText().toString().trim();
                        String url = editURL.getText().toString().trim();
                        if (selectedFocuses.isEmpty()) {
                            Toast.makeText(getContext(), "Select at least one focus!", Toast.LENGTH_SHORT).show();
                        } else if (!url.isEmpty() && InputHelper.validUrl(url) != null) {
                            // allow for url to be empty since most people won't want to upload a video
                            editURL.setError(InputHelper.validUrl(editURL.getText().toString()));
                        } else if (InputHelper.validNewExerciseName(exerciseNameInput.getText().toString(), totalExercises) == null) {
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < selectedFocuses.size(); i++) {
                                sb.append(selectedFocuses.get(i) + ((i == selectedFocuses.size() - 1) ? "" : ","));
                            }
                            String focusEntry = sb.toString().trim();
                            // TODO put this in an async and get the ID
                            ExerciseEntity newEntity = new ExerciseEntity(exerciseName, focusEntry, url, false, 0,
                                    0, 0, 0);
                            for (int i = 0; i < selectedFocuses.size(); i++) {
                                totalExercises.get(selectedFocuses.get(i)).add(newEntity);
                            }
                            newExerciseTask = new NewExerciseTask();
                            newExerciseTask.execute(newEntity);
                            populateExercisesListView();
                            Toast.makeText(getContext(), "Exercise successfully created!", Toast.LENGTH_SHORT).show();
                            alertDialog.dismiss();
                        } else {
                            // there was an error with the name
                            exerciseNameInput.setError(InputHelper.validNewExerciseName(exerciseNameInput.getText().toString(), totalExercises));
                        }
                    }
                });
            }
        });
        alertDialog.show();
    }

    private void editExercise(final ExerciseEntity exercise) {
        /*
            Popup for editing both default and custom exercises. Changes are only attempted to save if user hits save button.
         */
        View popupView = getLayoutInflater().inflate(R.layout.popup_edit_exercise, null);

        final EditText renameInput = popupView.findViewById(R.id.rename_input);
        renameInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_EXERCISE_NAME)});
        if (exercise.isDefaultExercise()) {
            popupView.findViewById(R.id.rename_layout).setVisibility(View.GONE);
        }
        //region Edit weight widgets
        final EditText weightInput = popupView.findViewById(R.id.weight_input);
        weightInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_WEIGHT_DIGITS)});

        final double weight = WeightHelper.getConvertedWeight(metricUnits, exercise);
        String formattedWeight = WeightHelper.getFormattedWeight(WeightHelper.getConvertedWeight(metricUnits, exercise));
        weightInput.setHint((exercise.getCurrentWeight() != Variables.IGNORE_WEIGHT_VALUE)
                ? formattedWeight + (metricUnits ? " kg" : " lb")
                : "N/A");
        weightInput.setEnabled((exercise.getCurrentWeight() != Variables.IGNORE_WEIGHT_VALUE));

        final Switch ignoreWeightSwitch = popupView.findViewById(R.id.ignore_weight_switch);
        ignoreWeightSwitch.setChecked((weight == Variables.IGNORE_WEIGHT_VALUE));
        ignoreWeightSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String formattedWeight = WeightHelper.getFormattedWeight(WeightHelper.getConvertedWeight(metricUnits, exercise));
                if (isChecked) {
                    // ignoring weight, so update the hint of the input
                    weightInput.setEnabled(false);
                    weightInput.setError(null); // hide any error that might have been showing
                    weightInput.setHint("N/A");
                } else {
                    /*
                        If in the DB there is a weight that's valid for the exercise, show it once the user clicks 'show weight'.
                        Otherwise prompt user to enter weight (would happen if in the DB its saved as ignore).
                     */
                    weightInput.setEnabled(true);
                    if (exercise.getCurrentWeight() == Variables.IGNORE_WEIGHT_VALUE) {
                        weightInput.setHint(String.format("Enter weight (%s)", (metricUnits ? " kg" : " lb")));
                    } else {
                        weightInput.setHint(formattedWeight + (metricUnits ? " kg" : " lb"));
                    }
                }
            }
        });
        //endregion
        //region Edit Url widgets
        final ImageButton clipboardBtn = popupView.findViewById(R.id.clipboard_btn);
        final ImageButton previewBtn = popupView.findViewById(R.id.preview_btn);
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
        urlInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_URL_LENGTH)});
        urlInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty()) {
                    if (!exercise.getUrl().isEmpty()) {
                        clipboardBtn.setVisibility(View.VISIBLE);
                        previewBtn.setVisibility(View.VISIBLE);
                    }
                } else {
                    clipboardBtn.setVisibility(View.GONE);
                    previewBtn.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        //endregion

        // all the views are set up, so show the alertdialog
        if (exercise.isDefaultExercise()) {
            alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                    .setTitle(exercise.getExerciseName())
                    .setView(popupView)
                    .setNegativeButton("Back", null)
                    .setPositiveButton("Save", null)
                    .create();
        } else {
            alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                    .setTitle(exercise.getExerciseName())
                    .setView(popupView)
                    .setNegativeButton("Back", null)
                    .setPositiveButton("Save", null)
                    .setNeutralButton("Delete Exercise", null)
                    .create();
        }
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button saveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                saveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String urlError = null;
                        String weightError = null;
                        String renameError = null;

                        if (!urlInput.getText().toString().isEmpty() && !urlInput.toString().trim().equals(exercise.getUrl())) {
                            // try to save the url if user has inputted something and the url is different than one in DB
                            urlError = saveUrl(exercise, urlInput);
                            urlInput.setError(urlError);
                        }

                        if (ignoreWeightSwitch.isChecked()) {
                            /*
                                Must do this because when the switch is checked the user might not have inputted anything.
                                So the below else elif would always fail because the input is empty.
                             */
                            weightError = saveWeight(exercise, weightInput, ignoreWeightSwitch.isChecked());
                            weightInput.setError(weightError);
                        } else if (WeightHelper.validWeight(weightInput.getText().toString()) == null
                                && Double.parseDouble(weightInput.getText().toString()) != exercise.getCurrentWeight()) {
                            // try to save the weight if user has inputted something and the weight is different than one in DB
                            weightError = saveWeight(exercise, weightInput, ignoreWeightSwitch.isChecked());
                            weightInput.setError(weightError);
                        }

                        if (!exercise.isDefaultExercise()
                                && !renameInput.getText().toString().isEmpty()
                                && !exercise.getExerciseName().equals(renameInput.getText().toString())) {
                            // try to save the new name if user has inputted something and the name is different than one in DB
                            renameError = renameExercise(exercise, renameInput);
                            renameInput.setError(renameError);
                        }

                        if (renameError == null && weightError == null && urlError == null) {
                            // only dismiss the dialogs if there were no input errors
                            alertDialog.dismiss();
                        }
                    }
                });
                Button deleteButton = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                if (deleteButton != null) {
                    deleteButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            deleteExercisePopup(exercise);
                        }
                    });
                }
            }
        });
        alertDialog.show();
    }

    private String renameExercise(ExerciseEntity exercise, EditText nameInput) {
        /*
            Rename a given exercise and update the entity in the DB. Return null if no problems
         */
        String retVal = InputHelper.validNewExerciseName(nameInput.getText().toString(), totalExercises);
        if (retVal == null) {
            // no error in input, so go ahead and save
            String newName = nameInput.getText().toString().trim();
            workoutViewModel.updateExerciseName(exercise.getExerciseName(), newName); // replace all occurrences of this exercise in any workouts in DB
            exercise.setExerciseName(newName);
            exerciseViewModel.update(exercise);

            Collections.sort(exercisesForSelectedFocus);
            exerciseAdapter.notifyDataSetChanged();
        }
        return retVal;
    }

    private String saveWeight(ExerciseEntity exercise, final EditText weightInput, boolean ignoreWeight) {
        /*
            Updates the weight of a given exercise and updates the entity in the DB. Return null if no problems.
         */
        String retVal = null;
        if (ignoreWeight) {
            exercise.setCurrentWeight(Variables.IGNORE_WEIGHT_VALUE);
            exerciseViewModel.update(exercise);
        } else if (WeightHelper.validWeight(weightInput.getText().toString()) == null) {
            // no input error, so go ahead and save weight
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
        } else {
            retVal = WeightHelper.validWeight(weightInput.getText().toString());
        }
        return retVal;
    }

    private String saveUrl(ExerciseEntity exercise, EditText urlInput) {
        /*
            Updates the url of a given exercise and updates the entity in the DB. Return null if no problems
         */
        String potentialURL = urlInput.getText().toString().trim();
        String errorMsg = InputHelper.validUrl(potentialURL);
        if (errorMsg == null) {
            exercise.setUrl(potentialURL.trim());
            exerciseViewModel.update(exercise);
        }
        return errorMsg;
    }

    private void deleteExercisePopup(final ExerciseEntity exercise) {
        /*
            Used to delete a custom exercise. Removes it from the DB and also from the listview in this fragment.
         */
        deleteDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                .setTitle("Delete Exercise")
                .setMessage(R.string.delete_custom_exercise_msg)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (String focus : totalExercises.keySet()) {
                            totalExercises.get(focus).remove(exercise);
                        }
                        workoutViewModel.deleteExerciseFromWorkouts(exercise.getExerciseName());
                        exerciseViewModel.delete(exercise);
                        alertDialog.dismiss();
                        populateExercisesListView();
                    }
                })
                .setNegativeButton("No", null)
                .create();
        deleteDialog.show();
        // make the message font a little bigger than the default one provided by the alertdialog
        TextView messageTV = alertDialog.getWindow().findViewById(android.R.id.message);
        messageTV.setTextSize(18);
    }

    private class NewExerciseTask extends AsyncTask<ExerciseEntity, Void, Long> {
        private ExerciseEntity entity;

        @Override
        protected Long doInBackground(ExerciseEntity... exerciseEntities) {
            // return this so we can save the ID correctly
            entity = exerciseEntities[0];
            return exerciseViewModel.insert(exerciseEntities[0]);
        }

        @Override
        protected void onPostExecute(Long id) {
            // have to set the id to the one in the DB otherwise subsequent inserts and deletes won't work
            entity.setId(id.intValue());
        }
    }
    //endregion
}
