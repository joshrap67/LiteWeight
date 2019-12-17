package com.joshrap.liteweight.Fragments;

import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.joshrap.liteweight.*;
import com.joshrap.liteweight.Database.Entities.*;
import com.joshrap.liteweight.Database.ViewModels.*;
import com.joshrap.liteweight.Globals.Variables;
import com.joshrap.liteweight.Helpers.ExerciseHelper;
import com.joshrap.liteweight.Helpers.InputHelper;
import com.joshrap.liteweight.Helpers.WorkoutHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class NewWorkoutFragment extends Fragment {
    private EditText workoutNameInput, numWeeksInput, numDaysInput;
    private Button previousDayButton, nextDayButton;
    private View view;
    private ViewGroup fragmentContainer;
    private AlertDialog alertDialog;
    private TableLayout pickExerciseTable, displayedExercisesTable;
    private TextView dayTitle;
    private boolean modified = false, firstDay, lastDay, firstWorkout = false;
    private int finalDayNum, finalWeekNum, currentDayIndex, maxDayIndex;
    private String finalName;
    private WorkoutViewModel workoutModel;
    private MetaViewModel metaModel;
    private ExerciseViewModel exerciseModel;
    private ArrayList<String> checkedExercises = new ArrayList<>();
    private ArrayList<String> focusList = new ArrayList<>();
    private ArrayList<String> workoutNames = new ArrayList<>();
    private HashMap<Integer, ArrayList<String>> pendingWorkout = new HashMap<>();
    private HashMap<String, ArrayList<String>> allExercises = new HashMap<>();
    private HashMap<String, ExerciseEntity> exerciseNameToEntity = new HashMap<>();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentContainer = container;
        view = inflater.inflate(R.layout.fragment_new_workout, container, false);
        ((MainActivity) getActivity()).updateToolbarTitle(Variables.NEW_WORKOUT_TITLE);
        currentDayIndex = 0;
        /*
            Setup view models
         */
        workoutModel = ViewModelProviders.of(getActivity()).get(WorkoutViewModel.class);
        metaModel = ViewModelProviders.of(getActivity()).get(MetaViewModel.class);
        exerciseModel = ViewModelProviders.of(getActivity()).get(ExerciseViewModel.class);
        GetAllWorkoutsTask task = new GetAllWorkoutsTask();
        task.execute();
        return view;
    }

    private class GetAllWorkoutsTask extends AsyncTask<Void, Void, List<MetaEntity>> {

        @Override
        protected List<MetaEntity> doInBackground(Void... voids) {
            // get the current workout from the database
            return metaModel.getAllMetadata();
        }

        @Override
        protected void onPostExecute(List<MetaEntity> result) {
            ((MainActivity) getActivity()).setProgressBar(false);
            if (!result.isEmpty()) {
                for (MetaEntity entity : result) {
                    workoutNames.add(entity.getWorkoutName());
                }
            } else {
                // no workouts found
                firstWorkout = true;
            }
            GetAllExercisesTask task = new GetAllExercisesTask();
            task.execute();
        }
    }

    private class GetAllExercisesTask extends AsyncTask<Void, Void, ArrayList<ExerciseEntity>> {

        @Override
        protected ArrayList<ExerciseEntity> doInBackground(Void... voids) {
            // get the current workout from the database
            return exerciseModel.getAllExercises();
        }

        @Override
        protected void onPostExecute(ArrayList<ExerciseEntity> result) {
            if (!result.isEmpty()) {
                for (ExerciseEntity entity : result) {
                    String[] focuses = entity.getFocus().split(Variables.FOCUS_DELIM_DB);
                    for (String focus : focuses) {
                        // get all focuses from the DB and put it in a list
                        if (!focusList.contains(focus)) {
                            focusList.add(focus);
                            allExercises.put(focus, new ArrayList<String>()); // init new focus index with list
                        }
                        allExercises.get(focus).add(entity.getExerciseName());
                    }
                    exerciseNameToEntity.put(entity.getExerciseName(), entity);
                }
            }
            ((MainActivity) getActivity()).setProgressBar(false);
            initViews();
        }
    }

    public void initViews() {
        /*
            Initialize the edit texts and ensure that each validates the input correctly.
         */
        workoutNameInput = view.findViewById(R.id.workout_name_input);
        numWeeksInput = view.findViewById(R.id.week_input);
        numDaysInput = view.findViewById(R.id.day_input);
        Button nextButton = view.findViewById(R.id.next_button);
        workoutNameInput.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent keyevent) {
                if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    String errorMsg = InputHelper.checkValidName(workoutNameInput.getText().toString().trim(), workoutNames);
                    if (errorMsg == null) {
                        modified = true;
                        return true;
                    } else {
                        displayErrorMessage("Name", errorMsg);
                    }
                }
                return false;
            }
        });

        numWeeksInput.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent keyevent) {
                if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    String errorMsg = InputHelper.checkValidWeek(numWeeksInput.getText().toString());
                    if (errorMsg == null) {
                        modified = true;
                        return true;
                    } else {
                        displayErrorMessage("Weeks", errorMsg);
                    }
                }
                return false;
            }
        });
        numDaysInput.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent keyevent) {
                if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    String errorMsg = InputHelper.checkValidDay(numDaysInput.getText().toString());
                    if (errorMsg == null) {
                        modified = true;
                        return true;
                    } else {
                        displayErrorMessage("Days", errorMsg);
                    }
                }
                return false;
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentName = workoutNameInput.getText().toString().trim();
                String currentWeeks = numWeeksInput.getText().toString();
                String currentDays = numDaysInput.getText().toString();
                String nameError = InputHelper.checkValidName(currentName, workoutNames);
                String weekError = InputHelper.checkValidWeek(currentWeeks);
                String dayError = InputHelper.checkValidDay(currentDays);
                if (workoutNames.size() > Variables.MAX_NUMBER_OF_WORKOUTS) {
                    Toast.makeText(getContext(), "Whoa there! You have too many workouts! Go delete some.", Toast.LENGTH_SHORT).show();
                } else if (nameError != null) {
                    displayErrorMessage("Name", nameError);
                } else if (weekError != null) {
                    displayErrorMessage("Weeks", weekError);
                } else if (dayError != null) {
                    displayErrorMessage("Days", dayError);
                } else {
                    finalName = currentName.trim();
                    finalWeekNum = Integer.parseInt(currentWeeks);
                    finalDayNum = Integer.parseInt(currentDays);
                    maxDayIndex = (finalWeekNum * finalDayNum) - 1;
                    createWorkout();
                }
            }
        });
    }

    public void displayErrorMessage(String editText, String msg) {
        /*
            Displays an error message to the appropriate EditText
         */
        switch (editText) {
            case "Name":
                workoutNameInput.setError(msg);
                workoutNameInput.setText("");
                break;
            case "Weeks":
                numWeeksInput.setError(msg);
                numWeeksInput.setText("");
                break;
            case "Days":
                numDaysInput.setError(msg);
                numDaysInput.setText("");
        }
    }

    public void createWorkout() {
        /*
            After parameters are validated, inflate the view that allows the user to start picking specific exercises for this
            new workout.
         */
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View createWorkoutView = inflater.inflate(R.layout.create_workout, fragmentContainer, false);
        ViewGroup rootView = (ViewGroup) getView();
        rootView.removeAllViews();
        rootView.addView(createWorkoutView);
        displayedExercisesTable = createWorkoutView.findViewById(R.id.main_table);
        dayTitle = createWorkoutView.findViewById(R.id.day_text_view);
        dayTitle.setText(WorkoutHelper.generateDayTitle(currentDayIndex, finalDayNum));
        for (int i = 0; i <= maxDayIndex; i++) {
            // create the hash map that maps day numbers to lists of exercises
            pendingWorkout.put(i, new ArrayList<String>());
        }
        setButtons(createWorkoutView);
    }

    public void setButtons(View createWorkoutView) {
        /*
            Setup buttons to allow for cycling through all the days of the workout. Logic is included to ensure that the user
            does not go beyond the bounds of the days specified by their input. Also ensure that each day has at
            least one exercise before allowing output to the DB.
         */
        final Button addExercises = createWorkoutView.findViewById(R.id.add_exercises);
        addExercises.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupAddExercises();
            }
        });
        firstDay = true;
        previousDayButton = createWorkoutView.findViewById(R.id.previous_day_button);
        nextDayButton = createWorkoutView.findViewById(R.id.next_day_button);
        previousDayButton.setVisibility(View.INVISIBLE);
        if (maxDayIndex == 0) {
            // in case some jabroni only wants to workout one day total
            nextDayButton.setText(getActivity().getResources().getString(R.string.finish_button));
        }
        previousDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentDayIndex > 0) {
                    displayedExercisesTable.removeAllViews();
                    currentDayIndex--;
                }
                if (lastDay) {
                    lastDay = false;
                    nextDayButton.setText(getActivity().getResources().getString(R.string.forward_button));
                }

                if (currentDayIndex == 0) {
                    previousDayButton.setVisibility(View.INVISIBLE);
                    firstDay = true;
                }
                addExercisesToMainTable();
                dayTitle.setText(WorkoutHelper.generateDayTitle(currentDayIndex, finalDayNum));
            }
        });
        nextDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentDayIndex < maxDayIndex) {
                    displayedExercisesTable.removeAllViews();
                    currentDayIndex++;
                    if (firstDay) {
                        firstDay = false;
                        previousDayButton.setVisibility(View.VISIBLE);
                    }
                    if (currentDayIndex == maxDayIndex) {
                        lastDay = true;
                        nextDayButton.setText(getActivity().getResources().getString(R.string.finish_button));
                    }
                    addExercisesToMainTable();
                    dayTitle.setText(WorkoutHelper.generateDayTitle(currentDayIndex, finalDayNum));
                } else {
                    // on the last day so check if every day has at least one exercise in it before writing to file
                    boolean ready = true;
                    for (int i = 0; i < pendingWorkout.size(); i++) {
                        if (pendingWorkout.get(i) == null) {
                            ready = false;
                        } else if (pendingWorkout.get(i).isEmpty()) {
                            ready = false;
                        }
                    }
                    if (ready) {
                        writeToDatabase();
                        modified = false;
                        Toast.makeText(getContext(), "Workout successfully created!", Toast.LENGTH_SHORT).show();
                        // restart this fragment
                        resetFragment();
                    } else {
                        Toast.makeText(getContext(), "Ensure each day has at least one exercise!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    public void writeToDatabase() {
        /*
            Writes the new workout to both the meta table and workout table
         */
        // write the metadata to the meta table
        SimpleDateFormat formatter = new SimpleDateFormat(Variables.DATE_PATTERN);
        Date date = new Date();
        String mostFrequentFocus = ExerciseHelper.mostFrequentFocus(pendingWorkout, exerciseNameToEntity, focusList);
        MetaEntity log = new MetaEntity(finalName, 0, maxDayIndex, finalDayNum, formatter.format(date), formatter.format(date),
                0, 0, firstWorkout, mostFrequentFocus, 0, 0);
        metaModel.insert(log);
        // write to the workout table
        for (int i = 0; i <= maxDayIndex; i++) {
            // loop through all the days of the workouts
            for (String exercise : pendingWorkout.get(i)) {
                // loop through selectedExercises of a specific day
                WorkoutEntity workoutEntity = new WorkoutEntity(exercise, finalName, i, false);
                workoutModel.insert(workoutEntity);
            }
        }
    }

    public void addExercisesToMainTable() {
        /*
            After user has selected exercises from the popup, add them to the table view and allow for them to be deleted.
         */
        Collections.sort(pendingWorkout.get(currentDayIndex));
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int count = 0;
        for (final String exercise : pendingWorkout.get(currentDayIndex)) {
            final View row = inflater.inflate(R.layout.row_list_view_element, null);
            TextView exerciseName = row.findViewById(R.id.exercise_name);
            exerciseName.setText(exercise);
            ImageButton deleteIcon = row.findViewById(R.id.delete_exercise);
            deleteIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    displayedExercisesTable.removeView(row);
                    pendingWorkout.get(currentDayIndex).remove(exercise);
                }
            });
            displayedExercisesTable.addView(row, count);
            count++;
        }
    }

    public void popupAddExercises() {
        /*
            User has indicated they wish to add exercises to this specific day. Show a popup that provides a spinner
            that is programmed to list all exercises for a given exercise focus.
         */
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialog = alertDialogBuilder.create();
        View popupView = getLayoutInflater().inflate(R.layout.popup_add_exercise, null);
        pickExerciseTable = popupView.findViewById(R.id.main_table);
        Spinner focusSpinner = popupView.findViewById(R.id.focus_spinner);
        Collections.sort(focusList);
        ArrayAdapter<String> focusAdapter = new ArrayAdapter<String>(
                getContext(), android.R.layout.simple_spinner_dropdown_item, focusList);
        focusSpinner.setAdapter(focusAdapter);
        focusSpinner.setOnItemSelectedListener(new SpinnerListener());
        alertDialog.setView(popupView);
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
        Button doneBtn = popupView.findViewById(R.id.done_btn);
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // done adding
                for (String exercise : checkedExercises) {
                    pendingWorkout.get(currentDayIndex).add(exercise);
                }
                checkedExercises.clear();
                displayedExercisesTable.removeAllViews();
                addExercisesToMainTable();
                alertDialog.dismiss();
            }
        });
    }

    public void updateExerciseChoices(String exerciseFocus) {
        /*
            Given a value from the exercise focus spinner, list all the selectedExercises associate with it.
         */
        ArrayList<String> sortedExercises = new ArrayList<>();
        for (String exercise : allExercises.get(exerciseFocus)) {
            sortedExercises.add(exercise);
        }
        Collections.sort(sortedExercises);
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (int i = 0; i < sortedExercises.size(); i++) {
            final View row = inflater.inflate(R.layout.row_add_exercise, null);
            final CheckBox exercise = row.findViewById(R.id.exercise_checkbox);
            String exerciseName = sortedExercises.get(i);
            exercise.setText(exerciseName);
            if (checkedExercises.contains(exerciseName) || pendingWorkout.get(currentDayIndex).contains(exerciseName)) {
                exercise.setChecked(true);
            }
            exercise.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (exercise.isChecked()) {
                        checkedExercises.add(exercise.getText().toString());
                    } else {
                        pendingWorkout.get(currentDayIndex).remove(exercise.getText().toString());
                        checkedExercises.remove(exercise.getText().toString());
                    }

                }
            });
            pickExerciseTable.addView(row, i);
        }
    }

    public boolean isModified() {
        /*
            Is used to check if the user has made any at all changes to their workout. If so, appropriate
            action (namely altering the text file) must be taken.
         */
        return modified;
    }

    public void resetFragment() {
        /*
            Resets the current fragment

         */
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new NewWorkoutFragment(), Variables.NEW_WORKOUT_TITLE).commit();
    }

    public void setModified(boolean status) {
        modified = status;
    }

    private class SpinnerListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            String selectedExerciseFocus = parent.getItemAtPosition(pos).toString();
            pickExerciseTable.removeAllViews();
            updateExerciseChoices(selectedExerciseFocus);
        }

        public void onNothingSelected(AdapterView parent) {
        }
    }

}
