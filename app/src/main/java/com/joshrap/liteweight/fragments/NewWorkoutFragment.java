package com.joshrap.liteweight.fragments;

import android.app.AlertDialog;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.joshrap.liteweight.*;
import com.joshrap.liteweight.activities.WorkoutActivity;
import com.joshrap.liteweight.adapters.PendingRoutineAdapter;
import com.joshrap.liteweight.imports.Globals;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.interfaces.FragmentWithDialog;
import com.joshrap.liteweight.models.ExerciseRoutine;
import com.joshrap.liteweight.models.ExerciseUser;
import com.joshrap.liteweight.models.Routine;
import com.joshrap.liteweight.models.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class NewWorkoutFragment extends Fragment implements FragmentWithDialog {
    private ScrollView addExercisesScrollView;
    private RecyclerView exerciseRecyclerView;
    private AlertDialog alertDialog;
    private TableLayout pickExerciseTable;
    private TextView dayTitle;
    private String spinnerFocus;
    private HashMap<String, ArrayList<ExerciseUser>> allExercises = new HashMap<>();
    private Routine pendingRoutine;
    private List<String> weekSpinnerValues;
    private List<String> daySpinnerValues;
    private int currentWeekIndex;
    private int currentDayIndex;
    private ArrayAdapter<String> weekAdapter;
    private ArrayAdapter<String> dayAdapter;
    private Spinner weekSpinner;
    private Spinner daySpinner;
    private User activeUser;
    private PendingRoutineAdapter routineAdapter;
    private TextView emptyView;
    private Button addDayButton, addWeekButton;
    private boolean addMode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_workout_new, container, false);
        ((WorkoutActivity) getActivity()).enableBackButton(true);
        ((WorkoutActivity) getActivity()).updateToolbarTitle(Variables.NEW_WORKOUT_TITLE);
        pendingRoutine = new Routine();
        pendingRoutine.appendNewDay(0, 0);
        weekSpinnerValues = new ArrayList<>();
        daySpinnerValues = new ArrayList<>();
        activeUser = Globals.user; // TODO dependency injection
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        currentDayIndex = 0;
        currentWeekIndex = 0;
        exerciseRecyclerView = view.findViewById(R.id.recyclerView);
        emptyView = view.findViewById(R.id.empty_view);
        dayTitle = view.findViewById(R.id.day_text_view);
        addWeekButton = view.findViewById(R.id.add_week_btn);
        addDayButton = view.findViewById(R.id.add_day_btn);

        addMode = true;
        setDayTitleNew();
        setSpinners(view);
        setButtons();
        updateButtonTexts();
        updateWorkoutListUI();
        RadioButton addRadioButton = view.findViewById(R.id.add_radio_btn);
        addRadioButton.setOnClickListener(v -> {
            if (!addMode) {
                // prevent useless function call if already in this mode
                addMode = true;
                setButtons();
                updateButtonTexts();
            }
        });
        addRadioButton.toggle();
        RadioButton deleteRadioButton = view.findViewById(R.id.delete_radio_btn);
        deleteRadioButton.setOnClickListener(v -> {
            if (addMode) {
                // prevent useless function call if already in this mode
                addMode = false;
                setButtons();
                updateButtonTexts();
            }
        });
        FloatingActionButton addExercisesButton = view.findViewById(R.id.add_exercises);
        addExercisesButton.setOnClickListener(v -> popupAddExercises());
        super.onViewCreated(view, savedInstanceState);

    }

    private void setButtons() {
        addWeekButton.setOnClickListener((v -> {
            if (addMode) {
                currentDayIndex = 0;
                // for now only allow for weeks to be appended not insert
                currentWeekIndex = pendingRoutine.getRoutine().size();
                pendingRoutine.appendNewDay(currentWeekIndex, currentDayIndex);
                weekSpinner.setSelection(currentWeekIndex);
                daySpinner.setSelection(0);
                updateWeekSpinnerValues();
                updateDaySpinnerValues();
                weekAdapter.notifyDataSetChanged();
                dayAdapter.notifyDataSetChanged();
                setDayTitleNew();
                updateWorkoutListUI();
                updateButtonTexts();
            } else {
                promptDeleteWeek();
            }

        }));
        addDayButton.setOnClickListener((v -> {
            if (addMode) {
                // for now only allow for weeks to be appended not insert
                currentDayIndex = pendingRoutine.getRoutine().get(currentWeekIndex).size();
                pendingRoutine.appendNewDay(currentWeekIndex, currentDayIndex);
                updateWeekSpinnerValues();
                updateDaySpinnerValues();
                daySpinner.setSelection(currentDayIndex);
                dayAdapter.notifyDataSetChanged();
                setDayTitleNew();
                updateWorkoutListUI();
                updateButtonTexts();
            } else {
                promptDeleteDay();
            }
        }));
    }

    private void updateButtonTexts() {
        if (addMode) {
            if (this.pendingRoutine.getRoutine().keySet().size() >= Variables.MAX_NUMBER_OF_WEEKS) {
                addWeekButton.setText("MAX REACHED");
                addWeekButton.setEnabled(false);
            } else {
                addWeekButton.setText("ADD WEEK");
                addWeekButton.setEnabled(true);
            }
            if (this.pendingRoutine.getRoutine().get(currentWeekIndex).keySet().size() >=
                    Variables.FIXED_WORKOUT_MAX_NUMBER_OF_DAYS) {
                addDayButton.setText("MAX REACHED");
                addDayButton.setEnabled(false);
            } else {
                addDayButton.setText("ADD DAY");
                addDayButton.setEnabled(true);
            }
        } else {
            addDayButton.setEnabled(true);
            addWeekButton.setEnabled(true);
            addDayButton.setText("REMOVE DAY");
            addWeekButton.setText("REMOVE WEEK");
        }
    }

    private void setSpinners(View view) {
        // setup the week spinner
        weekAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, weekSpinnerValues);
        weekAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        weekSpinner = view.findViewById(R.id.week_spinner);
        weekSpinner.setAdapter(weekAdapter);
        weekSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentWeekIndex = position;
                currentDayIndex = 0;
                daySpinner.setSelection(0);
                updateDaySpinnerValues();
                updateWorkoutListUI();
                setDayTitleNew();
                updateButtonTexts();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // setup the day spinner
        dayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, daySpinnerValues);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner = view.findViewById(R.id.day_spinner);
        daySpinner.setAdapter(dayAdapter);
        daySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentDayIndex = position;
                updateWorkoutListUI();
                setDayTitleNew();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        updateWeekSpinnerValues();
        updateDaySpinnerValues();
    }

    private void updateWeekSpinnerValues() {
        weekSpinnerValues.clear();
        for (int i = 0; i < pendingRoutine.getRoutine().keySet().size(); i++) {
            weekSpinnerValues.add("Week " + (i + 1));
        }
        weekAdapter.notifyDataSetChanged();
    }

    private void updateDaySpinnerValues() {
        daySpinnerValues.clear();
        for (int i = 0; i < pendingRoutine.getRoutine().get(currentWeekIndex).size(); i++) {
            daySpinnerValues.add("Day " + (i + 1));
        }
        dayAdapter.notifyDataSetChanged();
    }

    private void setDayTitleNew() {
        dayTitle.setText("W" + (currentWeekIndex + 1) + ":D" + (currentDayIndex + 1));
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

    private void updateWorkoutListUI() {
        /*
            Updates the list of displayed exercises in the workout depending on the current day.
         */

        Map<String, String> exerciseIdToName = new HashMap<>();
        for (String id : activeUser.getUserExercises().keySet()) {
            exerciseIdToName.put(id, activeUser.getUserExercises().get(id).getExerciseName());
        }
        routineAdapter = new PendingRoutineAdapter
                (pendingRoutine.getExerciseListForDay(currentWeekIndex, currentDayIndex),
                        exerciseIdToName, pendingRoutine, currentWeekIndex, currentDayIndex, false);
        routineAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            // since google is stupid af and doesn't have a simple setEmptyView for recyclerView...
            @Override
            public void onChanged() {
                super.onChanged();
                checkEmpty();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                checkEmpty();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                checkEmpty();
            }
        });
        exerciseRecyclerView.setAdapter(routineAdapter);
        exerciseRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        setDayTitleNew();
        checkEmpty();
    }

    private void promptDeleteWeek() {
        String message = "Are you sure you wish to delete week " + (currentWeekIndex + 1) + "?\n\n" +
                "Doing so will delete ALL days associated with this week, and this action cannot be reversed!";
        alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                .setTitle("Delete Week " + (currentWeekIndex + 1))
                .setMessage(message)
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (pendingRoutine.getRoutine().keySet().size() > 1) {
                        deleteWeek();
                        alertDialog.dismiss();
                    } else {
                        // don't allow for week to be deleted if it is the only one
                        Toast.makeText(getContext(), "Cannot delete only week from routine.", Toast.LENGTH_LONG).show();
                        alertDialog.dismiss();
                    }

                })
                .setNegativeButton("No", null)
                .create();
        alertDialog.show();
        // make the message font a little bigger than the default one provided by the alertdialog
        TextView messageTV = alertDialog.getWindow().findViewById(android.R.id.message);
        messageTV.setTextSize(18);
    }

    private void promptDeleteDay() {
        String message = "Are you sure you wish to delete day " + (currentDayIndex + 1) + "?\n\n" +
                "Doing so will delete ALL exercises associated with this day, and this action cannot be reversed!";
        alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                .setTitle("Delete Day " + (currentDayIndex + 1))
                .setMessage(message)
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (pendingRoutine.getRoutine().get(currentWeekIndex).keySet().size() > 1) {
                        deleteDay();
                        alertDialog.dismiss();
                    } else {
                        // don't allow for week to be deleted if it is the only one
                        Toast.makeText(getContext(), "Cannot delete only day from routine.", Toast.LENGTH_LONG).show();
                        alertDialog.dismiss();
                    }

                })
                .setNegativeButton("No", null)
                .create();
        alertDialog.show();
        // make the message font a little bigger than the default one provided by the alertdialog
        TextView messageTV = alertDialog.getWindow().findViewById(android.R.id.message);
        messageTV.setTextSize(18);
    }

    private void deleteDay() {
        pendingRoutine.deleteDay(currentWeekIndex, currentDayIndex);

        if (currentDayIndex != 0) {
            // if on the first day, then move the user forward to the old day 2
            currentDayIndex--;
        }
        daySpinner.setSelection(currentDayIndex);
        updateWeekSpinnerValues();
        updateDaySpinnerValues();
        weekAdapter.notifyDataSetChanged();
        dayAdapter.notifyDataSetChanged();
        updateWorkoutListUI();
        updateButtonTexts();
    }

    private void deleteWeek() {
        pendingRoutine.deleteWeek(currentWeekIndex);

        if (currentWeekIndex != 0) {
            // if on the first week, then move the user forward to the old week 2
            currentWeekIndex--;
        }
        currentDayIndex = 0;
        daySpinner.setSelection(0);
        weekSpinner.setSelection(currentWeekIndex);
        updateWeekSpinnerValues();
        updateDaySpinnerValues();
        weekAdapter.notifyDataSetChanged();
        dayAdapter.notifyDataSetChanged();
        setDayTitleNew();
        updateWorkoutListUI();
        updateButtonTexts();
    }

    private void checkEmpty() {
        /*
            Used to check if the specific day has exercises in it or not. If not, show a textview alerting user
         */
        emptyView.setVisibility(pendingRoutine.getExerciseListForDay(currentWeekIndex, currentDayIndex).isEmpty()
                ? View.VISIBLE : View.GONE);
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
        allExercises = new HashMap<>();
        ArrayList<String> focusList = new ArrayList<>();
        for (String exerciseId : activeUser.getUserExercises().keySet()) {
            ExerciseUser exerciseUser = activeUser.getUserExercises().get(exerciseId);
            Map<String, Boolean> focuses = exerciseUser.getFocuses();
            for (String focus : focuses.keySet()) {
                if (!allExercises.containsKey(focus)) {
                    // focus hasn't been added before
                    focusList.add(focus);
                    allExercises.put(focus, new ArrayList<>());
                }
                allExercises.get(focus).add(exerciseUser);
            }
        }

        SearchView searchView = popupView.findViewById(R.id.search_input);
        searchView.setOnSearchClickListener(v -> {
            // populate the list view with all exercises
            ArrayList<ExerciseUser> sortedExercises = new ArrayList<>();
            for (String focus : allExercises.keySet()) {
                for (ExerciseUser exercise : allExercises.get(focus)) {
                    if (!sortedExercises.contains(exercise)) {
                        sortedExercises.add(exercise);
                    }
                }
            }
            Collections.sort(sortedExercises);
            pickExerciseTable.removeAllViews();
            updateExercisesFromSearch(sortedExercises);
            focusSpinner.setVisibility(View.GONE);
        });
        searchView.setOnCloseListener(() -> {
            focusSpinner.setVisibility(View.VISIBLE);
            pickExerciseTable.removeAllViews();
            updateExerciseChoices();
            return false;
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                ArrayList<ExerciseUser> sortedExercises = new ArrayList<>();
                for (String focus : allExercises.keySet()) {
                    for (ExerciseUser exercise : allExercises.get(focus)) {
                        if (!sortedExercises.contains(exercise) &&
                                Pattern.compile(Pattern.quote(query), Pattern.CASE_INSENSITIVE).matcher(exercise.getExerciseName()).find()) {
                            sortedExercises.add(exercise);
                        }
                    }
                }
                Collections.sort(sortedExercises);
                pickExerciseTable.removeAllViews();
                updateExercisesFromSearch(sortedExercises);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // show suggestions as the user types
                ArrayList<ExerciseUser> sortedExercises = new ArrayList<>();
                for (String focus : allExercises.keySet()) {
                    for (ExerciseUser exercise : allExercises.get(focus)) {
                        if (!sortedExercises.contains(exercise) &&
                                Pattern.compile(Pattern.quote(newText), Pattern.CASE_INSENSITIVE).matcher(exercise.getExerciseName()).find()) {
                            sortedExercises.add(exercise);
                        }
                    }
                }
                Collections.sort(sortedExercises);
                pickExerciseTable.removeAllViews();
                updateExercisesFromSearch(sortedExercises);
                return false;
            }
        });

        Collections.sort(focusList, String.CASE_INSENSITIVE_ORDER);
        ArrayAdapter<String> focusAdapter = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_spinner_dropdown_item, focusList);
        focusSpinner.setAdapter(focusAdapter);
        focusSpinner.setOnItemSelectedListener(new FocusSpinnerListener());
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

    private void updateExercisesFromSearch(ArrayList<ExerciseUser> exercises) {
        /*
            Populates the list with all exercises that have matched a search input.
         */
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (exercises.isEmpty()) {
            TextView notFoundTV = new TextView(getActivity().getApplicationContext());
            TableRow.LayoutParams paramsExample = new TableRow.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            notFoundTV.setGravity(Gravity.CENTER);
            notFoundTV.setTextColor(getResources().getColor(android.R.color.black));
            notFoundTV.setPadding(20, 20, 20, 20);
            notFoundTV.setTextSize(25);
            notFoundTV.setLayoutParams(paramsExample);
            notFoundTV.setText(getActivity().getResources().getString(R.string.exercise_not_found));
            pickExerciseTable.addView(notFoundTV, 0);
        }
        for (int i = 0; i < exercises.size(); i++) {
            final ExerciseUser exerciseUser = exercises.get(i);
            final View row = inflater.inflate(R.layout.row_add_exercise, null);
            final CheckBox exercise = row.findViewById(R.id.exercise_checkbox);
            String exerciseName = exercises.get(i).getExerciseName();
            exercise.setText(exerciseName);
            // check if the exercise is already in this specific day
            boolean isChecked = false;
            for (ExerciseRoutine exerciseRoutine : pendingRoutine.getExerciseListForDay(currentWeekIndex, currentDayIndex)) {
                if (exerciseRoutine.getExerciseId().equals(exerciseUser.getExerciseId())) {
                    isChecked = true;
                    break;
                }
            }
            exercise.setChecked(isChecked);

            exercise.setOnClickListener(v -> {
                if (exercise.isChecked()) {
                    pendingRoutine.insertExercise(currentWeekIndex, currentDayIndex,
                            new ExerciseRoutine(exerciseUser, exerciseUser.getExerciseId()));
                } else {
                    pendingRoutine.removeExercise(currentWeekIndex, currentDayIndex, exerciseUser.getExerciseId());
                }
                routineAdapter.notifyDataSetChanged();
                updateWorkoutListUI();
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
        ArrayList<ExerciseUser> sortedExercises = new ArrayList<>(allExercises.get(spinnerFocus));
        Collections.sort(sortedExercises);
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (int i = 0; i < sortedExercises.size(); i++) {
            final ExerciseUser exerciseUser = sortedExercises.get(i);
            final View row = inflater.inflate(R.layout.row_add_exercise, null);
            final CheckBox exercise = row.findViewById(R.id.exercise_checkbox);
            String exerciseName = exerciseUser.getExerciseName();
            exercise.setText(exerciseName);

            // check if the exercise is already in this specific day
            boolean isChecked = false;
            for (ExerciseRoutine exerciseRoutine : pendingRoutine.getExerciseListForDay(currentWeekIndex, currentDayIndex)) {
                if (exerciseRoutine.getExerciseId().equals(exerciseUser.getExerciseId())) {
                    isChecked = true;
                    break;
                }
            }
            exercise.setChecked(isChecked);
            exercise.setOnClickListener(v -> {
                if (exercise.isChecked()) {
                    pendingRoutine.insertExercise(currentWeekIndex, currentDayIndex,
                            new ExerciseRoutine(exerciseUser, exerciseUser.getExerciseId()));
                } else {
                    pendingRoutine.removeExercise(currentWeekIndex, currentDayIndex, exerciseUser.getExerciseId());
                }
                routineAdapter.notifyDataSetChanged();
                updateWorkoutListUI();
            });

            pickExerciseTable.addView(row, i);
        }
        // scroll to top of list view once all the displayed exercises are shown
        addExercisesScrollView.fullScroll(View.FOCUS_UP);
        addExercisesScrollView.smoothScrollTo(0, 0);
    }

    private class FocusSpinnerListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            spinnerFocus = parent.getItemAtPosition(pos).toString();
            pickExerciseTable.removeAllViews();
            updateExerciseChoices();
        }

        public void onNothingSelected(AdapterView parent) {
        }
    }
}
