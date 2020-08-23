package com.joshrap.liteweight.fragments;

import android.app.AlertDialog;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.joshrap.liteweight.*;
import com.joshrap.liteweight.activities.WorkoutActivity;
import com.joshrap.liteweight.adapters.CustomSortAdapter;
import com.joshrap.liteweight.adapters.PendingRoutineAdapter;
import com.joshrap.liteweight.helpers.InputHelper;
import com.joshrap.liteweight.helpers.WorkoutHelper;
import com.joshrap.liteweight.imports.Globals;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.interfaces.FragmentWithDialog;
import com.joshrap.liteweight.models.ExerciseRoutine;
import com.joshrap.liteweight.models.ExerciseUser;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.models.Routine;
import com.joshrap.liteweight.models.RoutineDayMap;
import com.joshrap.liteweight.models.User;
import com.joshrap.liteweight.models.UserWithWorkout;
import com.joshrap.liteweight.network.repos.WorkoutRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import static android.os.Looper.getMainLooper;

public class NewWorkoutFragment extends Fragment implements FragmentWithDialog {
    private RecyclerView routineRecyclerView, pickExerciseRecyclerView;
    private AlertDialog alertDialog;
    private TextView dayTitleTV, exerciseNotFoundTV, emptyDayView;
    private String spinnerFocus;
    private HashMap<String, ArrayList<ExerciseUser>> allUserExercises; // focus -> exercises
    private Routine pendingRoutine;
    private List<String> weekSpinnerValues, daySpinnerValues;
    private int currentWeekIndex, currentDayIndex, mode;
    private ArrayAdapter<String> weekAdapter, dayAdapter;
    private Spinner weekSpinner, daySpinner;
    private User activeUser;
    private Button dayButton, weekButton, saveButton, addExercisesButton;
    private Map<String, String> exerciseIdToName;
    private ImageButton sortButton;
    private LinearLayout radioLayout, customSortLayout;
    private RelativeLayout relativeLayout;
    private AddExerciseAdapter addExerciseAdapter;
    private ProgressDialog loadingDialog;
    @Inject
    WorkoutRepository workoutRepository;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_workout, container, false);
        Injector.getInjector(getContext()).inject(this);
        ((WorkoutActivity) getActivity()).enableBackButton(true);
        ((WorkoutActivity) getActivity()).updateToolbarTitle(Variables.NEW_WORKOUT_TITLE);
        pendingRoutine = new Routine();
        currentDayIndex = 0;
        currentWeekIndex = 0;
        pendingRoutine.appendNewDay(currentWeekIndex, currentDayIndex);
        weekSpinnerValues = new ArrayList<>();
        loadingDialog = new ProgressDialog(getActivity());
        loadingDialog.setCancelable(false);
        allUserExercises = new HashMap<>();
        daySpinnerValues = new ArrayList<>();
        mode = Variables.ADD_MODE;
        activeUser = Globals.user; // TODO dependency injection?

        exerciseIdToName = new HashMap<>();
        for (String id : activeUser.getUserExercises().keySet()) {
            exerciseIdToName.put(id, activeUser.getUserExercises().get(id).getExerciseName());
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        /*
            Init all views and buttons when view is loaded onto screen
         */
        routineRecyclerView = view.findViewById(R.id.recycler_view);
        emptyDayView = view.findViewById(R.id.empty_view);
        dayTitleTV = view.findViewById(R.id.day_text_view);
        weekButton = view.findViewById(R.id.add_week_btn);
        dayButton = view.findViewById(R.id.add_day_btn);
        customSortLayout = view.findViewById(R.id.custom_sort_layout);
        Button saveSortButton = view.findViewById(R.id.done_sorting_btn);
        radioLayout = view.findViewById(R.id.mode_linear_layout);
        relativeLayout = view.findViewById(R.id.button_spinner_layout);
        saveButton = view.findViewById(R.id.save_button);

        dayTitleTV.setText(WorkoutHelper.generateDayTitleNew(currentWeekIndex, currentDayIndex));
        setSpinnerListeners(view);
        setButtonListeners();
        updateButtonTexts();
        updateRoutineListUI();
        saveSortButton.setOnClickListener(v -> {
            customSortLayout.setVisibility(View.GONE);
            saveButton.setVisibility(View.VISIBLE);
            sortButton.setVisibility(View.VISIBLE);
            addExercisesButton.setVisibility(View.VISIBLE);
            relativeLayout.setVisibility(View.VISIBLE);
            radioLayout.setVisibility(View.VISIBLE);
            updateRoutineListUI();
            // needed to avoid weird bug that happens when user tries to sort again by dragging
            customSortDispatcher.attachToRecyclerView(null);
        });
        // set up mode options
        RadioButton addRadioButton = view.findViewById(R.id.add_radio_btn);
        addRadioButton.setOnClickListener(v -> {
            if (mode != Variables.ADD_MODE) {
                // prevent useless function call if already in this mode
                mode = Variables.ADD_MODE;
                addExercisesButton.setVisibility(View.VISIBLE);
                setButtonListeners();
                updateButtonTexts();
                updateRoutineListUI();
            }
        });
        RadioButton deleteRadioButton = view.findViewById(R.id.delete_radio_btn);
        deleteRadioButton.setOnClickListener(v -> {
            if (mode != Variables.DELETE_MODE) {
                // prevent useless function call if already in this mode
                mode = Variables.DELETE_MODE;
                addExercisesButton.setVisibility(View.INVISIBLE);
                setButtonListeners();
                updateButtonTexts();
                updateRoutineListUI();
            }
        });
        RadioButton copyRadioButton = view.findViewById(R.id.copy_radio_btn);
        copyRadioButton.setOnClickListener(v -> {
            if (mode != Variables.COPY_MODE) {
                // prevent useless function call if already in this mode
                mode = Variables.COPY_MODE;
                addExercisesButton.setVisibility(View.VISIBLE);
                setButtonListeners();
                updateButtonTexts();
                updateRoutineListUI();
            }
        });
        addExercisesButton = view.findViewById(R.id.add_exercises);
        addExercisesButton.setOnClickListener(v -> popupAddExercises());
        // set up sorting options
        sortButton = view.findViewById(R.id.sort_button);
        final PopupMenu dropDownMenu = new PopupMenu(getContext(), sortButton);
        final Menu menu = dropDownMenu.getMenu();
        menu.add(0, RoutineDayMap.alphabeticalSortAscending, 0, "Sort Alphabetical (A-Z)");
        menu.add(0, RoutineDayMap.alphabeticalSortDescending, 0, "Sort Alphabetical (Z-A)");
        menu.add(0, RoutineDayMap.weightSortAscending, 0, "Sort by Weight (Ascending)");
        menu.add(0, RoutineDayMap.weightSortDescending, 0, "Sort by Weight (Descending)");
        menu.add(0, RoutineDayMap.customSort, 0, "Custom Sort (Drag 'n Drop)");

        dropDownMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case RoutineDayMap.alphabeticalSortAscending:
                    this.pendingRoutine.sortDay(currentWeekIndex, currentDayIndex, RoutineDayMap.alphabeticalSortAscending, exerciseIdToName);
                    updateRoutineListUI();
                    return true;
                case RoutineDayMap.alphabeticalSortDescending:
                    this.pendingRoutine.sortDay(currentWeekIndex, currentDayIndex, RoutineDayMap.alphabeticalSortDescending, exerciseIdToName);
                    updateRoutineListUI();
                    return true;
                case RoutineDayMap.weightSortDescending:
                    this.pendingRoutine.sortDay(currentWeekIndex, currentDayIndex, RoutineDayMap.weightSortDescending, exerciseIdToName);
                    updateRoutineListUI();
                    return true;
                case RoutineDayMap.weightSortAscending:
                    this.pendingRoutine.sortDay(currentWeekIndex, currentDayIndex, RoutineDayMap.weightSortAscending, exerciseIdToName);
                    updateRoutineListUI();
                    return true;
                case RoutineDayMap.customSort:
                    if (pendingRoutine.getExerciseListForDay(currentWeekIndex, currentDayIndex).size() < 2) {
                        Toast.makeText(getContext(), "Add at least two exercises.", Toast.LENGTH_LONG).show();
                    } else {
                        enableCustomSortMode();
                    }
                    return true;
            }
            return false;
        });
        sortButton.setOnClickListener(v -> dropDownMenu.show());
        super.onViewCreated(view, savedInstanceState);
    }

    private void setButtonListeners() {
        /*
            Set the onclicklisteners of the week and day button depending on the mode
         */
        weekButton.setOnClickListener((v -> {
            if (mode == Variables.ADD_MODE) {
                currentDayIndex = 0;
                // for now only allow for weeks to be appended not inserted
                currentWeekIndex = pendingRoutine.size();
                pendingRoutine.appendNewDay(currentWeekIndex, currentDayIndex);
                weekSpinner.setSelection(currentWeekIndex);
                daySpinner.setSelection(0);
                updateWeekSpinnerValues();
                updateDaySpinnerValues();
                weekAdapter.notifyDataSetChanged();
                dayAdapter.notifyDataSetChanged();
                dayTitleTV.setText(WorkoutHelper.generateDayTitleNew(currentWeekIndex, currentDayIndex));
                updateRoutineListUI();
                updateButtonTexts();
            } else if (mode == Variables.DELETE_MODE) {
                promptDeleteWeek();
            } else if (mode == Variables.COPY_MODE) {
                promptCopyWeek();
            }

        }));
        saveButton.setOnClickListener(v -> {
            boolean validRoutine = true;
            for (int week = 0; week < pendingRoutine.size(); week++) {
                for (int day = 0; day < pendingRoutine.getWeek(week).size(); day++) {
                    if (pendingRoutine.getExerciseListForDay(week, day).isEmpty()) {
                        validRoutine = false;
                    }
                }
            }
            if (validRoutine) {
                promptSave();
            } else {
                Toast.makeText(getContext(), "Each day must have at least one exercise!", Toast.LENGTH_LONG).show();
            }
        });
        dayButton.setOnClickListener((v -> {
            if (mode == Variables.ADD_MODE) {
                // for now only allow for weeks to be appended not insert
                currentDayIndex = pendingRoutine.getWeek(currentWeekIndex).size();
                pendingRoutine.appendNewDay(currentWeekIndex, currentDayIndex);
                updateWeekSpinnerValues();
                updateDaySpinnerValues();
                daySpinner.setSelection(currentDayIndex);
                dayAdapter.notifyDataSetChanged();
                dayTitleTV.setText(WorkoutHelper.generateDayTitleNew(currentWeekIndex, currentDayIndex));
                updateRoutineListUI();
                updateButtonTexts();
            } else if (mode == Variables.DELETE_MODE) {
                promptDeleteDay();
            } else if (mode == Variables.COPY_MODE) {
                if (pendingRoutine.getDay(currentWeekIndex, currentDayIndex).getExercisesForDay().isEmpty()) {
                    Toast.makeText(getContext(), "Must have at least one exercise in this day to copy it.", Toast.LENGTH_LONG).show();
                } else {
                    promptCopyDay();
                }
            }
        }));
    }

    private void updateButtonTexts() {
        /*
            Updates the button texts of the week/day buttons depending on the current mode
         */
        if (mode == Variables.ADD_MODE) {
            if (this.pendingRoutine.size() >= Variables.MAX_NUMBER_OF_WEEKS) {
                weekButton.setText(getString(R.string.max_reached_msg));
                weekButton.setEnabled(false);
            } else {
                weekButton.setText(getString(R.string.add_week_msg));
                weekButton.setEnabled(true);
            }
            if (this.pendingRoutine.getWeek(currentWeekIndex).size() >=
                    Variables.WORKOUT_MAX_NUMBER_OF_DAYS) {
                dayButton.setText(getString(R.string.max_reached_msg));
                dayButton.setEnabled(false);
            } else {
                dayButton.setText(getString(R.string.add_day_msg));
                dayButton.setEnabled(true);
            }
        } else if (mode == Variables.DELETE_MODE) {
            dayButton.setEnabled(true);
            weekButton.setEnabled(true);
            dayButton.setText(getString(R.string.remove_day_msg));
            weekButton.setText(getString(R.string.remove_week_msg));
        } else if (mode == Variables.COPY_MODE) {
            dayButton.setEnabled(true);
            weekButton.setEnabled(true);
            dayButton.setText(getString(R.string.copy_day_msg));
            weekButton.setText(getString(R.string.copy_week_msg));
        }
    }

    private void setSpinnerListeners(View view) {
        /*
            Enable the spinners to provide a means of navigating the routine by week and day
         */
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
                updateRoutineListUI();
                dayTitleTV.setText(WorkoutHelper.generateDayTitleNew(currentWeekIndex, currentDayIndex));
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
                updateRoutineListUI();
                dayTitleTV.setText(WorkoutHelper.generateDayTitleNew(currentWeekIndex, currentDayIndex));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        updateWeekSpinnerValues();
        updateDaySpinnerValues();
    }

    private void updateWeekSpinnerValues() {
        /*
            Update spinner list with all the available weeks to progress
         */
        weekSpinnerValues.clear();
        for (int i = 0; i < pendingRoutine.size(); i++) {
            weekSpinnerValues.add("Week " + (i + 1));
        }
        weekAdapter.notifyDataSetChanged();
    }

    private void updateDaySpinnerValues() {
        /*
            Update spinner list with all the available day to progress
         */
        daySpinnerValues.clear();
        for (int i = 0; i < pendingRoutine.getWeek(currentWeekIndex).size(); i++) {
            daySpinnerValues.add("Day " + (i + 1));
        }
        dayAdapter.notifyDataSetChanged();
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
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private void updateRoutineListUI() {
        /*
            Updates the list of displayed exercises in the routine depending on the current day.
         */

        PendingRoutineAdapter routineAdapter = new PendingRoutineAdapter
                (pendingRoutine.getExerciseListForDay(currentWeekIndex, currentDayIndex),
                        exerciseIdToName, pendingRoutine, currentWeekIndex, currentDayIndex,
                        false, mode);
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
        routineRecyclerView.setAdapter(routineAdapter);
        routineRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        dayTitleTV.setText(WorkoutHelper.generateDayTitleNew(currentWeekIndex, currentDayIndex));
        checkEmpty();
    }

    private void enableCustomSortMode() {
        /*
            Allow the user to drag on specific exercises within a day to the position of their liking.
         */
        customSortLayout.setVisibility(View.VISIBLE);
        saveButton.setVisibility(View.GONE);
        sortButton.setVisibility(View.GONE);
        relativeLayout.setVisibility(View.GONE);
        addExercisesButton.setVisibility(View.GONE);
        radioLayout.setVisibility(View.GONE);

        CustomSortAdapter routineAdapter = new CustomSortAdapter(
                pendingRoutine.getExerciseListForDay(currentWeekIndex, currentDayIndex),
                exerciseIdToName, false);

        customSortDispatcher.attachToRecyclerView(routineRecyclerView);
        routineRecyclerView.setAdapter(routineAdapter);
    }

    private ItemTouchHelper customSortDispatcher = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder dragged, @NonNull RecyclerView.ViewHolder target) {
            int fromPosition = dragged.getAdapterPosition();
            int toPosition = target.getAdapterPosition();
            pendingRoutine.swapExerciseOrder(currentWeekIndex, currentDayIndex, fromPosition, toPosition);
            recyclerView.getAdapter().notifyItemMoved(fromPosition, toPosition);
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

        }
    });


    private void promptDeleteWeek() {
        String message = "Are you sure you wish to delete week " + (currentWeekIndex + 1) + "?\n\n" +
                "Doing so will delete ALL days associated with this week, and this action cannot be reversed!";
        alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                .setTitle("Delete Week " + (currentWeekIndex + 1))
                .setMessage(message)
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (pendingRoutine.size() > 1) {
                        deleteWeek();
                        alertDialog.dismiss();
                    } else {
                        // don't allow for week to be deleted if it is the only one
                        Toast.makeText(getContext(), "Cannot delete only week from workout.", Toast.LENGTH_LONG).show();
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
                    if (pendingRoutine.getWeek(currentWeekIndex).size() > 1) {
                        deleteDay();
                        alertDialog.dismiss();
                    } else {
                        // don't allow for week to be deleted if it is the only one
                        Toast.makeText(getContext(), "Cannot delete only day from week.", Toast.LENGTH_LONG).show();
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

    private void promptCopyDay() {
        View popupView = getLayoutInflater().inflate(R.layout.popup_copy_day_week, null);
        int totalDays = 0;
        List<String> days = new ArrayList<>();
        for (int week = 0; week < pendingRoutine.size(); week++) {
            for (int day = 0; day < pendingRoutine.getWeek(week).size(); day++) {
                String dayTitle = WorkoutHelper.generateDayTitleNew(week, day);
                days.add(dayTitle);
                totalDays++;
            }
        }
        String[] daysAsArray = new String[totalDays];
        for (int i = 0; i < totalDays; i++) {
            daysAsArray[i] = days.get(i);
        }
        final NumberPicker dayPicker = popupView.findViewById(R.id.day_picker);
        dayPicker.setMinValue(0);
        dayPicker.setMaxValue(totalDays - 1);
        dayPicker.setValue(0);
        dayPicker.setWrapSelectorWheel(false);
        dayPicker.setDisplayedValues(daysAsArray);

        TextView copyToExistingTv = popupView.findViewById(R.id.copy_to_existing_tv);
        String copyToExistingMsg = String.format("Copy %s to one of the following days. Note that doing this will overwrite all the existing exercises in the target day.",
                WorkoutHelper.generateDayTitleNew(currentWeekIndex, currentDayIndex));
        copyToExistingTv.setText(copyToExistingMsg);
        Button copyToExistingButton = popupView.findViewById(R.id.copy_to_existing_btn);
        copyToExistingButton.setOnClickListener(v -> {
            RoutineDayMap dayToBeCopied = pendingRoutine.getDay(currentWeekIndex, currentDayIndex).clone();
            int count = 0;
            for (int week = 0; week < pendingRoutine.size(); week++) {
                for (int day = 0; day < pendingRoutine.getWeek(week).size(); day++) {
                    if (count == dayPicker.getValue()) {
                        currentWeekIndex = week;
                        currentDayIndex = day;
                    }
                    count++;
                }
            }
            // do the copy
            pendingRoutine.getWeek(currentWeekIndex).put(currentDayIndex, dayToBeCopied);
            daySpinner.setSelection(currentDayIndex);
            weekSpinner.setSelection(currentWeekIndex);
            updateRoutineListUI();
            updateButtonTexts();
            alertDialog.dismiss();
        });

        TextView copyAsNewTV = popupView.findViewById(R.id.copy_as_new_tv);
        String copyAsNewMsg = String.format("Copy %s as a new day at the end of the current week.",
                WorkoutHelper.generateDayTitleNew(currentWeekIndex, currentDayIndex));
        copyAsNewTV.setText(copyAsNewMsg);
        Button copyAsNewButton = popupView.findViewById(R.id.copy_as_new_btn);
        copyAsNewButton.setOnClickListener(v -> {
            RoutineDayMap routineToBeCopied = pendingRoutine.getDay(currentWeekIndex, currentDayIndex).clone();
            currentDayIndex = pendingRoutine.getWeek(currentWeekIndex).size();
            pendingRoutine.getWeek(currentWeekIndex).put(currentDayIndex, routineToBeCopied);

            daySpinner.setSelection(currentDayIndex);
            updateDaySpinnerValues();
            updateRoutineListUI();
            updateButtonTexts();
            alertDialog.dismiss();
        });
        if (pendingRoutine.getWeek(currentWeekIndex).size() >= Variables.WORKOUT_MAX_NUMBER_OF_DAYS) {
            copyAsNewTV.setVisibility(View.GONE);
            copyAsNewButton.setVisibility(View.GONE);
        }


        alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                .setTitle(String.format("Copy %s", WorkoutHelper.generateDayTitleNew(currentWeekIndex, currentDayIndex)))
                .setView(popupView)
                .setPositiveButton("Return", null)
                .create();
        alertDialog.show();
    }

    private void promptCopyWeek() {
        View popupView = getLayoutInflater().inflate(R.layout.popup_copy_day_week, null);
        int totalWeeks = pendingRoutine.size();

        String[] daysAsArray = new String[totalWeeks];
        for (int i = 0; i < totalWeeks; i++) {
            daysAsArray[i] = String.format("Week %d", i + 1);
        }
        final NumberPicker dayPicker = popupView.findViewById(R.id.day_picker);
        dayPicker.setMinValue(0);
        dayPicker.setMaxValue(totalWeeks - 1);
        dayPicker.setValue(0);
        dayPicker.setWrapSelectorWheel(false);
        dayPicker.setDisplayedValues(daysAsArray);

        TextView copyToExistingTv = popupView.findViewById(R.id.copy_to_existing_tv);
        String copyToExistingMsg = String.format("Copy all of Week %d to one of the following weeks." +
                "\n\nNote that doing this will overwrite ALL existing days in the target wek", currentWeekIndex + 1);
        copyToExistingTv.setText(copyToExistingMsg);
        Button copyToExistingButton = popupView.findViewById(R.id.copy_to_existing_btn);
        copyToExistingButton.setOnClickListener(v -> {
            int targetWeek = dayPicker.getValue();
            Map<Integer, RoutineDayMap> daysToBeCopied = pendingRoutine.getWeek(currentWeekIndex);
            Map<Integer, RoutineDayMap> targetDays = new HashMap<>();
            for (Integer day : daysToBeCopied.keySet()) {
                targetDays.put(day, pendingRoutine.getDay(currentWeekIndex, currentDayIndex).clone());
            }

            // do the copy
            pendingRoutine.putWeek(targetWeek, targetDays);
            currentWeekIndex = targetWeek;
            currentDayIndex = 0;
            daySpinner.setSelection(currentDayIndex);
            weekSpinner.setSelection(currentWeekIndex);
            updateRoutineListUI();
            updateButtonTexts();
            alertDialog.dismiss();
        });

        TextView copyAsNewTV = popupView.findViewById(R.id.copy_as_new_tv);
        String copyAsNewMsg = String.format("Copy Week %d as a new week.", currentWeekIndex + 1);
        copyAsNewTV.setText(copyAsNewMsg);
        Button copyAsNewButton = popupView.findViewById(R.id.copy_as_new_btn);
        copyAsNewButton.setOnClickListener(v -> {
            Map<Integer, RoutineDayMap> daysToBeCopied = pendingRoutine.getWeek(currentWeekIndex);
            Map<Integer, RoutineDayMap> targetDays = new HashMap<>();
            for (Integer day : daysToBeCopied.keySet()) {
                targetDays.put(day, pendingRoutine.getDay(currentWeekIndex, currentDayIndex).clone());
            }

            // do the copy
            currentWeekIndex = pendingRoutine.size();
            pendingRoutine.putWeek(currentWeekIndex, targetDays);
            currentDayIndex = 0;
            daySpinner.setSelection(currentDayIndex);
            weekSpinner.setSelection(currentWeekIndex);
            updateDaySpinnerValues();
            updateWeekSpinnerValues();
            updateRoutineListUI();
            updateButtonTexts();
            alertDialog.dismiss();
        });
        if (pendingRoutine.size() >= Variables.MAX_NUMBER_OF_WEEKS) {
            copyAsNewTV.setVisibility(View.GONE);
            copyAsNewButton.setVisibility(View.GONE);
        }


        alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                .setTitle(String.format("Copy Week %d", currentWeekIndex + 1))
                .setView(popupView)
                .setPositiveButton("Return", null)
                .create();
        alertDialog.show();
    }

    private void promptSave() {
        View popupView = getLayoutInflater().inflate(R.layout.popup_save_workout, null);
        final EditText workoutNameInput = popupView.findViewById(R.id.workout_name_input);
        final TextInputLayout workoutNameInputLayout = popupView.findViewById(R.id.workout_name_input_layout);
        workoutNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (workoutNameInputLayout.isErrorEnabled()) {
                    // if an error is present, stop showing the error message once the user types (acknowledged it)
                    workoutNameInputLayout.setErrorEnabled(false);
                    workoutNameInputLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        workoutNameInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_WORKOUT_NAME)});
        alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                .setTitle("Save workout")
                .setView(popupView)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create();
        alertDialog.setCancelable(false);
        alertDialog.setOnShowListener(dialogInterface -> {
            Button saveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            saveButton.setOnClickListener(view -> {
                String workoutName = workoutNameInput.getText().toString().trim();
                List<String> workoutNames = new ArrayList<>();
                for (String workoutId : activeUser.getUserWorkouts().keySet()) {
                    workoutNames.add(activeUser.getUserWorkouts().get(workoutId).getWorkoutName());
                }
                String errorMsg = InputHelper.validWorkoutName(workoutName, workoutNames);
                if (errorMsg != null) {
                    workoutNameInputLayout.setError(errorMsg);
                } else {
                    // no problems so go ahead and save
                    alertDialog.dismiss();
                    createWorkout(workoutName);
                }
            });
        });
        alertDialog.show();
    }

    private void createWorkout(String workoutName) {
        showLoadingDialog();
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ResultStatus<UserWithWorkout> resultStatus = this.workoutRepository.createWorkout(pendingRoutine, workoutName);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                loadingDialog.dismiss();
                if (resultStatus.isSuccess()) {
                    Globals.user = resultStatus.getData().getUser();
                    Globals.activeWorkout = resultStatus.getData().getWorkout();
                    ((WorkoutActivity) getActivity()).finishCreateWorkout();
                } else {
                    showErrorMessage(resultStatus.getErrorMessage());
                }
            });
        });
    }

    private void showLoadingDialog() {
        loadingDialog.setMessage("Saving...");
        loadingDialog.show();
    }

    private void showErrorMessage(String message) {
        alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                .setTitle("Save workout error")
                .setMessage(message)
                .setPositiveButton("Ok", null)
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
        updateRoutineListUI();
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
        updateRoutineListUI();
        updateButtonTexts();
    }

    private void checkEmpty() {
        /*
            Used to check if the specific day has exercises in it or not. If not, show a textview alerting user
         */
        emptyDayView.setVisibility(pendingRoutine.getExerciseListForDay(currentWeekIndex, currentDayIndex).isEmpty()
                ? View.VISIBLE : View.GONE);
        if (mode == Variables.DELETE_MODE) {
            emptyDayView.setText(getString(R.string.empty_workout_day_delete_mode));
        } else {
            emptyDayView.setText(getString(R.string.empty_workout_day));
        }
    }

    private void popupAddExercises() {
        /*
            User has indicated they wish to add exercises to this specific day. Show a popup that provides a spinner
            that is programmed to list all exercises for a given exercise focus.
         */
        View popupView = getLayoutInflater().inflate(R.layout.popup_pick_exercise, null);
        pickExerciseRecyclerView = popupView.findViewById(R.id.pick_exercises_recycler_view);
        exerciseNotFoundTV = popupView.findViewById(R.id.search_not_found_TV);
        final Spinner focusSpinner = popupView.findViewById(R.id.focus_spinner);
        allUserExercises = new HashMap<>();
        ArrayList<String> focusList = new ArrayList<>();
        for (String exerciseId : activeUser.getUserExercises().keySet()) {
            ExerciseUser exerciseUser = activeUser.getUserExercises().get(exerciseId);
            List<String> focuses = exerciseUser.getFocuses();
            for (String focus : focuses) {
                if (!allUserExercises.containsKey(focus)) {
                    // focus hasn't been added before
                    focusList.add(focus);
                    allUserExercises.put(focus, new ArrayList<>());
                }
                allUserExercises.get(focus).add(exerciseUser);
            }
        }

        SearchView searchView = popupView.findViewById(R.id.search_input);
        searchView.setOnSearchClickListener(v -> {
            // populate the list view with all exercises
            ArrayList<ExerciseUser> sortedExercises = new ArrayList<>();
            for (String focus : allUserExercises.keySet()) {
                for (ExerciseUser exercise : allUserExercises.get(focus)) {
                    if (!sortedExercises.contains(exercise)) {
                        sortedExercises.add(exercise);
                    }
                }
            }
            Collections.sort(sortedExercises);
            addExerciseAdapter = new AddExerciseAdapter(sortedExercises);
            pickExerciseRecyclerView.setAdapter(addExerciseAdapter);
            pickExerciseRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

            focusSpinner.setVisibility(View.GONE);
        });
        searchView.setOnCloseListener(() -> {
            focusSpinner.setVisibility(View.VISIBLE);
            exerciseNotFoundTV.setVisibility(View.GONE);
            updateExerciseChoices();
            return false;
        });

        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                addExerciseAdapter.getFilter().filter(newText);
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

    private void updateExerciseChoices() {
        /*
            Given the current focus spinner value, list all the exercises associated with it.
         */
        ArrayList<ExerciseUser> sortedExercises = new ArrayList<>(allUserExercises.get(spinnerFocus));
        Collections.sort(sortedExercises);
        addExerciseAdapter = new AddExerciseAdapter(sortedExercises);
        pickExerciseRecyclerView.setAdapter(addExerciseAdapter);
        pickExerciseRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    public boolean isModified() {
        boolean modified = false;
        for (int week = 0; week < pendingRoutine.size(); week++) {
            for (int day = 0; day < pendingRoutine.getWeek(week).size(); day++) {
                if (!pendingRoutine.getExerciseListForDay(week, day).isEmpty()) {
                    modified = true;
                }
            }
        }
        return modified;
    }

    private class FocusSpinnerListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            spinnerFocus = parent.getItemAtPosition(pos).toString();
            updateExerciseChoices();
        }

        public void onNothingSelected(AdapterView parent) {
        }
    }

    private class AddExerciseAdapter extends
            RecyclerView.Adapter<AddExerciseAdapter.ViewHolder> implements Filterable {
        class ViewHolder extends RecyclerView.ViewHolder {
            CheckBox exercise;

            ViewHolder(View itemView) {
                super(itemView);
                exercise = itemView.findViewById(R.id.exercise_checkbox);
            }
        }

        private List<ExerciseUser> exercises;
        private List<ExerciseUser> displayList;

        AddExerciseAdapter(List<ExerciseUser> exerciseRoutines) {
            this.exercises = exerciseRoutines;
            displayList = new ArrayList<>(this.exercises);
        }


        @Override
        public AddExerciseAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View exerciseView = inflater.inflate(R.layout.row_add_exercise, parent, false);
            return new ViewHolder(exerciseView);
        }

        @Override
        public void onBindViewHolder(AddExerciseAdapter.ViewHolder holder, int position) {
            final ExerciseUser exerciseUser = displayList.get(position);
            final CheckBox exercise = holder.exercise;
            exercise.setText(exerciseUser.getExerciseName());
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
                updateRoutineListUI();
            });
        }

        @Override
        public int getItemCount() {
            return displayList.size();
        }

        @Override
        public Filter getFilter() {
            return exerciseSearchFilter;
        }

        private Filter exerciseSearchFilter = new Filter() {
            // responsible for filtering the search of the user in the add user popup (by exercise name)
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<ExerciseUser> filteredList = new ArrayList<>();
                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(exercises);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();
                    for (ExerciseUser exerciseUser : exercises) {
                        if (exerciseUser.getExerciseName().toLowerCase().contains(filterPattern)) {
                            filteredList.add(exerciseUser);
                        }
                    }
                }
                FilterResults results = new FilterResults();
                results.values = filteredList;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                displayList.clear();
                displayList.addAll((List) results.values);
                if (displayList.isEmpty()) {
                    exerciseNotFoundTV.setVisibility(View.VISIBLE);
                } else {
                    exerciseNotFoundTV.setVisibility(View.GONE);
                }
                notifyDataSetChanged();
            }
        };
    }
}
