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
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.joshrap.liteweight.*;
import com.joshrap.liteweight.activities.WorkoutActivity;
import com.joshrap.liteweight.adapters.PendingRoutineAdapter;
import com.joshrap.liteweight.adapters.RoutineAdapter;
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
    private int currentWeek;
    private int currentDay;
    private ArrayAdapter<String> weekAdapter;
    private ArrayAdapter<String> dayAdapter;
    private Spinner weekSpinner;
    private Spinner daySpinner;
    private User activeUser;
    private PendingRoutineAdapter routineAdapter;
    private TextView emptyView;
    private Button addDayButton, addWeekButton;

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
        currentDay = 0;
        currentWeek = 0;
        exerciseRecyclerView = view.findViewById(R.id.recyclerView);
        emptyView = view.findViewById(R.id.empty_view);
        dayTitle = view.findViewById(R.id.day_text_view);
        setDayTitleNew();
        setSpinners(view);
        setButtons(view);
        updateWorkoutListUI();
        super.onViewCreated(view, savedInstanceState);

    }

    private void setButtons(View view) {
        addWeekButton = view.findViewById(R.id.add_week_btn);
        addWeekButton.setOnClickListener((v -> {
            currentDay = 0;
            // for now only allow for weeks to be appended not insert
            currentWeek = pendingRoutine.getRoutine().size();
            pendingRoutine.appendNewDay(currentWeek, currentDay);
            weekSpinner.setSelection(currentWeek);
            daySpinner.setSelection(0);
            updateWeekSpinnerValues();
            updateDaySpinnerValues();
            weekAdapter.notifyDataSetChanged();
            dayAdapter.notifyDataSetChanged();
            setDayTitleNew();
            updateWorkoutListUI();
            checkAddButtons();
        }));
        addDayButton = view.findViewById(R.id.add_day_btn);
        addDayButton.setOnClickListener((v -> {
            // for now only allow for weeks to be appended not insert
            currentDay = pendingRoutine.getRoutine().get(currentWeek).size();
            pendingRoutine.appendNewDay(currentWeek, currentDay);
            updateWeekSpinnerValues();
            updateDaySpinnerValues();
            daySpinner.setSelection(currentDay);
            dayAdapter.notifyDataSetChanged();
            setDayTitleNew();
            updateWorkoutListUI();
            checkAddButtons();
        }));

        Button addExercisesButton = view.findViewById(R.id.add_exercises);
        addExercisesButton.setOnClickListener(v -> popupAddExercises());
        checkAddButtons(); // todo use this in the edit
    }

    private void checkAddButtons() {
        // TODO show text saying max reached? Probably would look better
        if (this.pendingRoutine.getRoutine().keySet().size() >= Variables.MAX_NUMBER_OF_WEEKS) {
            addWeekButton.setVisibility(View.INVISIBLE);
        } else {
            addWeekButton.setVisibility(View.VISIBLE);
        }
        if (this.pendingRoutine.getRoutine().get(currentWeek).keySet().size() >=
                Variables.FIXED_WORKOUT_MAX_NUMBER_OF_DAYS) {
            addDayButton.setVisibility(View.INVISIBLE);
        } else {
            addDayButton.setVisibility(View.VISIBLE);
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
                currentWeek = position;
                currentDay = 0;
                daySpinner.setSelection(0);
                updateDaySpinnerValues();
                updateWorkoutListUI();
                setDayTitleNew();
                checkAddButtons();
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
                currentDay = position;
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
        for (int i = 0; i < pendingRoutine.getRoutine().get(currentWeek).size(); i++) {
            daySpinnerValues.add("Day " + (i + 1));
        }
        dayAdapter.notifyDataSetChanged();
    }

    private void setDayTitleNew() {
        dayTitle.setText("W" + (currentWeek + 1) + ":D" + (currentDay + 1));
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
                (pendingRoutine.getExerciseListForDay(currentWeek, currentDay),
                        exerciseIdToName, pendingRoutine, currentWeek, currentDay, false);
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

    private void checkEmpty() {
        /*
            Used to check if the specific day has exercises in it or not. If not, show a textview alerting user
         */
        emptyView.setVisibility(pendingRoutine.getExerciseListForDay(currentWeek, currentDay).isEmpty()
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
            for (ExerciseRoutine exerciseRoutine : pendingRoutine.getExerciseListForDay(currentWeek, currentDay)) {
                if (exerciseRoutine.getExerciseId().equals(exerciseUser.getExerciseId())) {
                    isChecked = true;
                    break;
                }
            }
            exercise.setChecked(isChecked);

            exercise.setOnClickListener(v -> {
                if (exercise.isChecked()) {
                    pendingRoutine.insertExercise(currentWeek, currentDay,
                            new ExerciseRoutine(exerciseUser, exerciseUser.getExerciseId()));
                } else {
                    pendingRoutine.removeExercise(currentWeek, currentDay, exerciseUser.getExerciseId());
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
            for (ExerciseRoutine exerciseRoutine : pendingRoutine.getExerciseListForDay(currentWeek, currentDay)) {
                if (exerciseRoutine.getExerciseId().equals(exerciseUser.getExerciseId())) {
                    isChecked = true;
                    break;
                }
            }
            exercise.setChecked(isChecked);
            exercise.setOnClickListener(v -> {
                if (exercise.isChecked()) {
                    pendingRoutine.insertExercise(currentWeek, currentDay,
                            new ExerciseRoutine(exerciseUser, exerciseUser.getExerciseId()));
                } else {
                    pendingRoutine.removeExercise(currentWeek, currentDay, exerciseUser.getExerciseId());
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

    private class SpinnerListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            spinnerFocus = parent.getItemAtPosition(pos).toString();
            pickExerciseTable.removeAllViews();
            updateExerciseChoices();
        }

        public void onNothingSelected(AdapterView parent) {
        }
    }
}
