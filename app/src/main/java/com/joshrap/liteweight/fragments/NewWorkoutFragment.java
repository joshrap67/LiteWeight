package com.joshrap.liteweight.fragments;

import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.joshrap.liteweight.*;
import com.joshrap.liteweight.activities.MainActivity;
import com.joshrap.liteweight.database.entities.*;
import com.joshrap.liteweight.database.viewModels.*;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.helpers.ExerciseHelper;
import com.joshrap.liteweight.helpers.InputHelper;
import com.joshrap.liteweight.helpers.WorkoutHelper;
import com.joshrap.liteweight.interfaces.FragmentWithDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class NewWorkoutFragment extends Fragment implements FragmentWithDialog {
    private EditText workoutNameInput, numWeeksInput, numDaysInput;
    private ImageButton previousDayButton, nextDayButton;
    private View view;
    private ScrollView addExercisesScrollView;
    private ListView exerciseListView;
    private PendingExerciseAdapter exerciseAdapter;
    private ViewGroup fragmentContainer;
    private AlertDialog alertDialog;
    private TableLayout pickExerciseTable;
    private TextView dayTitle;
    private boolean modified = false, firstWorkout = false;
    private int daysPerWeek, finalWeekNum, currentDayIndex, maxDayIndex;
    private String finalName, spinnerFocus, workoutType;
    private WorkoutViewModel workoutModel;
    private MetaViewModel metaModel;
    private ExerciseViewModel exerciseModel;
    private ArrayList<String> focusList = new ArrayList<>();
    private ArrayList<String> workoutNames = new ArrayList<>();
    private HashMap<Integer, ArrayList<String>> pendingWorkout = new HashMap<>();
    private HashMap<String, ArrayList<String>> allExercises = new HashMap<>();
    private HashMap<String, ExerciseEntity> exerciseNameToEntity = new HashMap<>();
    private GetAllWorkoutsTask getAllWorkoutsTask;
    private GetAllExercisesTask getAllExercisesTask;
    private Switch workoutTypeSwitch;
    private SharedPreferences pref;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentContainer = container;
        view = inflater.inflate(R.layout.fragment_new_workout, container, false);
        ((MainActivity) getActivity()).enableBackButton(true);
        ((MainActivity) getActivity()).updateToolbarTitle(Variables.NEW_WORKOUT_TITLE);
        currentDayIndex = 0;

        pref = getActivity().getApplicationContext().getSharedPreferences(Variables.SHARED_PREF_SETTINGS, 0);
        workoutType = pref.getString(Variables.WORKOUT_TYPE_PREF_KEY, Variables.WORKOUT_FLEXIBLE);
        // Doing this here to avoid flashing of views after going invisible
        workoutTypeSwitch = view.findViewById(R.id.workout_type_switch);
        numDaysInput = view.findViewById(R.id.day_input);
        numWeeksInput = view.findViewById(R.id.week_input);
        final TextView weekTv = view.findViewById(R.id.week_TV);
        if (workoutType.equals(Variables.WORKOUT_FLEXIBLE)) {
            // in a flexible workout, there are no "weeks"
            numWeeksInput.setVisibility(View.GONE);
            weekTv.setVisibility(View.GONE);
            numDaysInput.setHint(R.string.flexible_workout_day_input_hint);
            workoutTypeSwitch.setChecked(false);
        } else if (workoutType.equals(Variables.WORKOUT_FIXED)) {
            numWeeksInput.setVisibility(View.VISIBLE);
            weekTv.setVisibility(View.VISIBLE);
            numDaysInput.setHint(R.string.fixed_workout_day_input_hint);
            workoutTypeSwitch.setChecked(true);
        }
        /*
            Setup view models
         */
        workoutModel = ViewModelProviders.of(getActivity()).get(WorkoutViewModel.class);
        metaModel = ViewModelProviders.of(getActivity()).get(MetaViewModel.class);
        exerciseModel = ViewModelProviders.of(getActivity()).get(ExerciseViewModel.class);
        getAllWorkoutsTask = new GetAllWorkoutsTask();
        getAllWorkoutsTask.execute();
        return view;
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
    }

    @Override
    public void onDestroy() {
        if (getAllWorkoutsTask != null) {
            getAllWorkoutsTask.cancel(true);
        }
        if (getAllExercisesTask != null) {
            getAllExercisesTask.cancel(true);
        }
        super.onDestroy();
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
            getAllExercisesTask = new GetAllExercisesTask();
            getAllExercisesTask.execute();
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
            initInputViews();
        }
    }

    private void initInputViews() {
        /*
            Initialize the edit texts and ensure that each validates the input correctly.
         */
        workoutNameInput = view.findViewById(R.id.workout_name_input);
        workoutNameInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_WORKOUT_NAME)});

        int maxWeekLength = String.valueOf(Variables.MAX_NUMBER_OF_WEEKS).length();
        numWeeksInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxWeekLength)});

        setDaysInputFilter();

        Button proceedButton = view.findViewById(R.id.next_button); // takes user to next stage of workout creation
        workoutNameInput.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent keyevent) {
                if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    String errorMsg = InputHelper.validWorkoutName(workoutNameInput.getText().toString().trim(), workoutNames);
                    if (errorMsg == null) {
                        modified = true;
                        return true;
                    } else {
                        workoutNameInput.setError(errorMsg);
                        workoutNameInput.setText("");
                    }
                }
                return false;
            }
        });
        numWeeksInput.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent keyevent) {
                if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    String errorMsg = InputHelper.validWeek(numWeeksInput.getText().toString());
                    if (errorMsg == null) {
                        modified = true;
                        return true;
                    } else {
                        numWeeksInput.setError(errorMsg);
                        numWeeksInput.setText("");
                    }
                }
                return false;
            }
        });
        numDaysInput.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent keyevent) {
                if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    String errorMsg = null;
                    if (workoutType.equals(Variables.WORKOUT_FLEXIBLE)) {
                        errorMsg = InputHelper.validDayFlexible(numDaysInput.getText().toString());
                    } else if (workoutType.equals(Variables.WORKOUT_FIXED)) {
                        errorMsg = InputHelper.validDayFixedWorkout(numDaysInput.getText().toString());
                    }
                    if (errorMsg == null) {
                        modified = true;
                        return true;
                    } else {
                        numDaysInput.setError(errorMsg);
                        numDaysInput.setText("");
                    }
                }
                return false;
            }
        });

        final SharedPreferences.Editor editor = pref.edit();
        final TextView weekTv = view.findViewById(R.id.week_TV);
        workoutTypeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                numDaysInput.setText("");
                numWeeksInput.setText("");
                if (isChecked) {
                    // we are making a fixed workout
                    workoutType = Variables.WORKOUT_FIXED;
                    numWeeksInput.setVisibility(View.VISIBLE);
                    weekTv.setVisibility(View.VISIBLE);
                    numDaysInput.setHint(R.string.fixed_workout_day_input_hint);
                    setDaysInputFilter();
                    editor.putString(Variables.WORKOUT_TYPE_PREF_KEY, Variables.WORKOUT_FIXED);
                    editor.apply();
                } else {
                    // we are making a flexible workout
                    workoutType = Variables.WORKOUT_FLEXIBLE;
                    numWeeksInput.setVisibility(View.GONE);
                    weekTv.setVisibility(View.GONE);
                    numDaysInput.setHint(R.string.flexible_workout_day_input_hint);
                    setDaysInputFilter();
                    editor.putString(Variables.WORKOUT_TYPE_PREF_KEY, Variables.WORKOUT_FLEXIBLE);
                    editor.apply();
                }
            }
        });

        ImageButton helpButton = view.findViewById(R.id.new_workout_help_btn);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupHelp();
            }
        });

        proceedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentName = workoutNameInput.getText().toString().trim();
                String currentWeeks = numWeeksInput.getText().toString().trim();
                String currentDays = numDaysInput.getText().toString().trim();
                if (workoutType.equals(Variables.WORKOUT_FIXED)) {
                    // need to validate weeks and days
                    if (validateFixedInput(currentName, currentWeeks, currentDays)) {
                        finalName = currentName.trim();
                        finalWeekNum = Integer.parseInt(currentWeeks.trim());
                        daysPerWeek = Integer.parseInt(currentDays.trim());
                        maxDayIndex = (finalWeekNum * daysPerWeek) - 1;
                        createWorkout();
                    }
                } else if (workoutType.equals(Variables.WORKOUT_FLEXIBLE)) {
                    // only need to validate the days
                    if (validateFlexibleInput(currentName, currentDays)) {
                        finalName = currentName.trim();
                        // for flexible workouts we just pretend it is a giant one week workout
                        daysPerWeek = Integer.parseInt(currentDays.trim());
                        maxDayIndex = daysPerWeek - 1;
                        createWorkout();
                    }
                }
            }
        });
    }

    private void popupHelp() {
        /*
            Shows a popup that displays some helpful information about the different workout types
         */
        alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                .setTitle("Workout Help")
                .setMessage(R.string.popup_message_create_workout_help)
                .setPositiveButton("Done", null)
                .create();
        alertDialog.show();
    }

    private void setDaysInputFilter() {
        if (workoutType.equals(Variables.WORKOUT_FIXED)) {
            int maxDaysLength = String.valueOf(Variables.FIXED_WORKOUT_MAX_NUMBER_OF_DAYS).length();
            numDaysInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxDaysLength)});
        } else if (workoutType.equals(Variables.WORKOUT_FLEXIBLE)) {
            int maxDaysLength = String.valueOf(Variables.MAX_NUMBER_OF_WEEKS * Variables.FIXED_WORKOUT_MAX_NUMBER_OF_DAYS).length();
            numDaysInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxDaysLength)});
        }
    }

    private boolean validateFixedInput(String name, String weeks, String days) {
        /*
            Validates input of the editexts and returns false if any of them have invalid input
         */
        boolean retVal = true;

        String nameError = InputHelper.validWorkoutName(name, workoutNames);
        String weekError = InputHelper.validWeek(weeks);
        String dayError = InputHelper.validDayFixedWorkout(days);

        if (workoutNames.size() >= Variables.MAX_NUMBER_OF_WORKOUTS) {
            Toast.makeText(getContext(), "Whoa there! You have too many workouts! Go delete some. " +
                    "Max amount is: " + Variables.MAX_NUMBER_OF_WORKOUTS, Toast.LENGTH_LONG).show();
            retVal = false;
        }
        if (nameError != null) {
            workoutNameInput.setError(nameError);
            workoutNameInput.setText("");
            retVal = false;
        }
        if (weekError != null) {
            numWeeksInput.setError(weekError);
            numWeeksInput.setText("");
            retVal = false;
        }
        if (dayError != null) {
            numDaysInput.setError(dayError);
            numDaysInput.setText("");
            retVal = false;
        }
        return retVal;
    }

    private boolean validateFlexibleInput(String name, String days) {
        /*
            Validates input of the editexts and returns false if any of them have invalid input
         */
        boolean retVal = true;

        String nameError = InputHelper.validWorkoutName(name, workoutNames);
        String dayError = InputHelper.validDayFlexible(days);

        if (workoutNames.size() >= Variables.MAX_NUMBER_OF_WORKOUTS) {
            Toast.makeText(getContext(), "Whoa there! You have too many workouts! Go delete some. " +
                    "Max amount is: " + Variables.MAX_NUMBER_OF_WORKOUTS, Toast.LENGTH_LONG).show();
            retVal = false;
        }
        if (nameError != null) {
            workoutNameInput.setError(nameError);
            workoutNameInput.setText("");
            retVal = false;
        }
        if (dayError != null) {
            numDaysInput.setError(dayError);
            numDaysInput.setText("");
            retVal = false;
        }
        return retVal;
    }

    private void createWorkout() {
        /*
            After parameters are validated, inflate the view that allows the user to start picking specific exercises for this
            new workout.
         */
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View createWorkoutView = inflater.inflate(R.layout.create_workout, fragmentContainer, false);
        ViewGroup rootView = (ViewGroup) getView();
        rootView.removeAllViews();
        rootView.addView(createWorkoutView);

        exerciseListView = view.findViewById(R.id.list_view);
        dayTitle = createWorkoutView.findViewById(R.id.day_text_view);
        for (int i = 0; i <= maxDayIndex; i++) {
            // create the hash map that maps day numbers to lists of exercises
            pendingWorkout.put(i, new ArrayList<String>());
        }
        initButtonListeners(createWorkoutView);
        updateWorkoutListUI();
    }

    private void initButtonListeners(View createWorkoutView) {
        /*
            Setup buttons to allow for cycling through all the days of the workout. Ensures that the user
            does not go beyond the bounds of the days specified by their input. Also ensures that each day has at
            least one exercise before allowing output to DB.
         */
        final FloatingActionButton addExercises = createWorkoutView.findViewById(R.id.add_exercises);
        addExercises.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupAddExercises();
            }
        });
        previousDayButton = createWorkoutView.findViewById(R.id.previous_day_button);
        nextDayButton = createWorkoutView.findViewById(R.id.next_day_button);

        dayTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jumpDaysPopup();
            }
        });

        previousDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentDayIndex > 0) {
                    currentDayIndex--;
                    updateWorkoutListUI();
                }
            }
        });
        nextDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentDayIndex < maxDayIndex) {
                    currentDayIndex++;
                    updateWorkoutListUI();
                } else if (currentDayIndex == maxDayIndex) {
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

    private void updateWorkoutListUI() {
        /*
            Updates the list of displayed exercises in the workout depending on the current day.
         */
        exerciseAdapter = new PendingExerciseAdapter(getContext(), pendingWorkout.get(currentDayIndex));
        exerciseListView.setAdapter(exerciseAdapter);
        dayTitle.setText(WorkoutHelper.generateDayTitle(currentDayIndex, daysPerWeek, workoutType));
        updateButtons();
    }

    private void updateButtons() {
        /*
            Updates the visibility and icon of the navigation buttons depending on the current day.
         */
        if (currentDayIndex == 0) {
            // means it's the first day, so hide the back button
            previousDayButton.setVisibility(View.INVISIBLE);
            nextDayButton.setVisibility(View.VISIBLE);
            nextDayButton.setImageResource(R.drawable.next_icon);
            if (currentDayIndex == maxDayIndex) {
                // in case some jabroni only wants to workout one day total
                nextDayButton.setImageResource(R.drawable.save_icon);
            }
        } else if (currentDayIndex != maxDayIndex) {
            // day in the middle, so just show the normal back and forward buttons
            previousDayButton.setVisibility(View.VISIBLE);
            nextDayButton.setVisibility(View.VISIBLE);
            nextDayButton.setImageResource(R.drawable.next_icon);
        } else {
            // lil hacky, but don't want the ripple showing when the icons switch
            nextDayButton.setVisibility(View.INVISIBLE);
            nextDayButton.setVisibility(View.VISIBLE);
            previousDayButton.setVisibility(View.VISIBLE);
            // last day so set the restart icon instead of next icon
            nextDayButton.setImageResource(R.drawable.save_icon);
        }
    }

    private void writeToDatabase() {
        /*
            Writes the new workout to both the meta table and workout table
         */
        // write the metadata to the meta table
        SimpleDateFormat formatter = new SimpleDateFormat(Variables.DATE_PATTERN);
        Date date = new Date();
        String mostFrequentFocus = ExerciseHelper.mostFrequentFocus(pendingWorkout, exerciseNameToEntity, focusList);
        MetaEntity metaEntity = new MetaEntity(finalName, 0, maxDayIndex, daysPerWeek, formatter.format(date), formatter.format(date),
                0, 0, firstWorkout, mostFrequentFocus, 0, 0, workoutType);
        // TODO put in async to get id
        metaModel.insert(metaEntity);
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

    private void jumpDaysPopup() {
        /*
            Allow the user to scroll through the list of days to quickly jump around in workout
         */
        String[] days = new String[maxDayIndex + 1];
        for (int i = 0; i <= maxDayIndex; i++) {
            days[i] = WorkoutHelper.generateDayTitle(i, daysPerWeek, workoutType);
        }
        View popupView = getLayoutInflater().inflate(R.layout.popup_jump_days, null);
        final NumberPicker dayPicker = popupView.findViewById(R.id.day_picker);
        dayPicker.setMinValue(0);
        dayPicker.setMaxValue(maxDayIndex);
        dayPicker.setValue(currentDayIndex);
        dayPicker.setWrapSelectorWheel(false);
        dayPicker.setDisplayedValues(days);

        alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                .setTitle("Jump to Day")
                .setView(popupView)
                .setPositiveButton("Go", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        currentDayIndex = dayPicker.getValue();
                        updateWorkoutListUI();
                    }
                })
                .create();
        alertDialog.show();
    }

    private void popupAddExercises() {
        /*
            User has indicated they wish to add exercises to this specific day. Show a popup that provides a spinner
            that is programmed to list all exercises for a given exercise focus.
         */
        View popupView = getLayoutInflater().inflate(R.layout.popup_add_exercise, null);
        addExercisesScrollView = popupView.findViewById(R.id.scroll_view);
        pickExerciseTable = popupView.findViewById(R.id.main_table);
        final Spinner focusSpinner = popupView.findViewById(R.id.focus_spinner);
        SearchView searchView = popupView.findViewById(R.id.search_input);
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // populate the list view with all exercises
                ArrayList<String> sortedExercises = new ArrayList<>();
                for (String focus : allExercises.keySet()) {
                    for (String exercise : allExercises.get(focus)) {
                        if (!sortedExercises.contains(exercise)) {
                            sortedExercises.add(exercise);
                        }
                    }
                }
                Collections.sort(sortedExercises, String.CASE_INSENSITIVE_ORDER);
                pickExerciseTable.removeAllViews();
                updateExercisesFromSearch(sortedExercises);
                focusSpinner.setVisibility(View.GONE);
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                focusSpinner.setVisibility(View.VISIBLE);
                pickExerciseTable.removeAllViews();
                updateExerciseChoices();
                return false;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                ArrayList<String> sortedExercises = new ArrayList<>();
                for (String focus : allExercises.keySet()) {
                    for (String exercise : allExercises.get(focus)) {
                        if (!sortedExercises.contains(exercise) &&
                                Pattern.compile(Pattern.quote(query), Pattern.CASE_INSENSITIVE).matcher(exercise).find()) {
                            sortedExercises.add(exercise);
                        }
                    }
                }
                Collections.sort(sortedExercises, String.CASE_INSENSITIVE_ORDER);
                pickExerciseTable.removeAllViews();
                updateExercisesFromSearch(sortedExercises);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // show suggestions as the user types
                ArrayList<String> sortedExercises = new ArrayList<>();
                for (String focus : allExercises.keySet()) {
                    for (String exercise : allExercises.get(focus)) {
                        if (!sortedExercises.contains(exercise) &&
                                Pattern.compile(Pattern.quote(newText), Pattern.CASE_INSENSITIVE).matcher(exercise).find()) {
                            sortedExercises.add(exercise);
                        }
                    }
                }
                Collections.sort(sortedExercises, String.CASE_INSENSITIVE_ORDER);
                pickExerciseTable.removeAllViews();
                updateExercisesFromSearch(sortedExercises);
                return false;
            }
        });

        Collections.sort(focusList, String.CASE_INSENSITIVE_ORDER);
        ArrayAdapter<String> focusAdapter = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_spinner_dropdown_item, focusList);
        focusSpinner.setAdapter(focusAdapter);
        focusSpinner.setOnItemSelectedListener(new SpinnerListener());
        // initially select first item from spinner, then always select the one the user last clicked
        focusSpinner.setSelection((spinnerFocus == null) ? 0 : focusList.indexOf(spinnerFocus));
        // view is all set up, so now create the dialog with it
        alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                .setTitle("Add Exercises")
                .setView(popupView)
                .setPositiveButton("Done", null)
                .create();
        alertDialog.show();
    }

    private void updateExercisesFromSearch(ArrayList<String> exercises) {
        /*
            Populates the list with all exercises that have matched a search input.
         */
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (exercises.isEmpty()) {
            TextView notFoundTV = new TextView(getActivity().getApplicationContext());
            notFoundTV.setText(getActivity().getResources().getString(R.string.exercise_not_found));
            pickExerciseTable.addView(notFoundTV, 0);
        }
        for (int i = 0; i < exercises.size(); i++) {
            final View row = inflater.inflate(R.layout.row_add_exercise, null);
            final CheckBox exercise = row.findViewById(R.id.exercise_checkbox);
            String exerciseName = exercises.get(i);
            exercise.setText(exerciseName);
            if (pendingWorkout.get(currentDayIndex).contains(exerciseName)) {
                exercise.setChecked(true);
            }
            exercise.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (exercise.isChecked()) {
                        pendingWorkout.get(currentDayIndex).add(exercise.getText().toString());
                    } else {
                        pendingWorkout.get(currentDayIndex).remove(exercise.getText().toString());
                    }
                    Collections.sort(pendingWorkout.get(currentDayIndex), String.CASE_INSENSITIVE_ORDER);
                    exerciseAdapter.notifyDataSetChanged();
                }
            });
            pickExerciseTable.addView(row, i);
        }
        // scroll to top of list view once all the displayed exercises are shown
        addExercisesScrollView.fullScroll(View.FOCUS_UP);
        addExercisesScrollView.smoothScrollTo(0, 0);
    }

    private void updateExerciseChoices() {
        /*
            Given the current focus spinner value, list all the selectedExercises associate with it.
         */
        ArrayList<String> sortedExercises = new ArrayList<>();
        for (String exercise : allExercises.get(spinnerFocus)) {
            sortedExercises.add(exercise);
        }
        Collections.sort(sortedExercises, String.CASE_INSENSITIVE_ORDER);
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (int i = 0; i < sortedExercises.size(); i++) {
            final View row = inflater.inflate(R.layout.row_add_exercise, null);
            final CheckBox exercise = row.findViewById(R.id.exercise_checkbox);
            String exerciseName = sortedExercises.get(i);
            exercise.setText(exerciseName);
            if (pendingWorkout.get(currentDayIndex).contains(exerciseName)) {
                exercise.setChecked(true);
            }
            exercise.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (exercise.isChecked()) {
                        pendingWorkout.get(currentDayIndex).add(exercise.getText().toString());
                    } else {
                        pendingWorkout.get(currentDayIndex).remove(exercise.getText().toString());
                    }
                    Collections.sort(pendingWorkout.get(currentDayIndex), String.CASE_INSENSITIVE_ORDER);
                    exerciseAdapter.notifyDataSetChanged();
                }
            });
            pickExerciseTable.addView(row, i);
        }
        // scroll to top of list view once all the displayed exercises are shown
        addExercisesScrollView.fullScroll(View.FOCUS_UP);
        addExercisesScrollView.smoothScrollTo(0, 0);
    }

    public boolean isModified() {
        /*
            Is used to check if the user has made any at all changes to their workout. If so, appropriate
            action (namely altering the text file) must be taken.
         */
        return modified;
    }

    private void resetFragment() {
        /*
            Resets the current fragment. Used after the workout is successfully created
         */
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new NewWorkoutFragment(), Variables.NEW_WORKOUT_TITLE).commit();
    }

    private class SpinnerListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            spinnerFocus = parent.getItemAtPosition(pos).toString();
            pickExerciseTable.removeAllViews();
            updateExerciseChoices();
        }

        public void onNothingSelected(AdapterView parent) {
        }
    }

    private class PendingExerciseAdapter extends ArrayAdapter<String> {
        private Context context;
        private List<String> exerciseList;

        private PendingExerciseAdapter(@NonNull Context context, ArrayList<String> list) {
            super(context, 0, list);
            this.context = context;
            this.exerciseList = list;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View listItem = convertView;
            if (listItem == null) {
                listItem = LayoutInflater.from(context).inflate(R.layout.row_exercise_pending, parent, false);
            }
            final String currentExercise = exerciseList.get(position);
            TextView exerciseTV = listItem.findViewById(R.id.exercise_name);
            exerciseTV.setText(currentExercise);

            ImageButton deleteButton = listItem.findViewById(R.id.delete_exercise);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pendingWorkout.get(currentDayIndex).remove(currentExercise);
                    notifyDataSetChanged();
                }
            });
            return listItem;
        }
    }
}
