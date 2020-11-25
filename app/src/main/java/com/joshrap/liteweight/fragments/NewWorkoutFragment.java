package com.joshrap.liteweight.fragments;

import android.app.AlertDialog;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
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
import com.joshrap.liteweight.helpers.AndroidHelper;
import com.joshrap.liteweight.helpers.InputHelper;
import com.joshrap.liteweight.helpers.WorkoutHelper;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.interfaces.FragmentWithDialog;
import com.joshrap.liteweight.models.RoutineDay;
import com.joshrap.liteweight.models.RoutineExercise;
import com.joshrap.liteweight.models.OwnedExercise;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.models.Routine;
import com.joshrap.liteweight.models.RoutineWeek;
import com.joshrap.liteweight.models.User;
import com.joshrap.liteweight.models.UserWithWorkout;
import com.joshrap.liteweight.network.repos.WorkoutRepository;
import com.joshrap.liteweight.widgets.ErrorDialog;

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
    private HashMap<String, List<OwnedExercise>> allOwnedExercises; // focus -> exercises
    private Routine pendingRoutine;
    private List<String> weekSpinnerValues, daySpinnerValues;
    private int currentWeekIndex, currentDayIndex, mode;
    private ArrayAdapter<String> weekAdapter, dayAdapter;
    private Spinner weekSpinner, daySpinner;
    private User user;
    private Button dayButton, weekButton, saveButton, addExercisesButton;
    private Map<String, String> exerciseIdToName;
    private ImageButton sortButton;
    private LinearLayout radioLayout, customSortLayout;
    private RelativeLayout mainRelativeLayout;
    private AddExerciseAdapter addExerciseAdapter;
    private UserWithWorkout userWithWorkout;

    @Inject
    ProgressDialog loadingDialog;
    @Inject
    SharedPreferences sharedPreferences;
    @Inject
    WorkoutRepository workoutRepository;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Injector.getInjector(getContext()).inject(this);
        ((WorkoutActivity) getActivity()).toggleBackButton(true);
        ((WorkoutActivity) getActivity()).updateToolbarTitle(Variables.NEW_WORKOUT_TITLE);

        pendingRoutine = new Routine();
        currentDayIndex = 0;
        currentWeekIndex = 0;
        pendingRoutine.appendNewDay(currentWeekIndex, currentDayIndex);
        weekSpinnerValues = new ArrayList<>();
        allOwnedExercises = new HashMap<>();
        daySpinnerValues = new ArrayList<>();
        mode = Variables.ADD_MODE;
        userWithWorkout = ((WorkoutActivity) getActivity()).getUserWithWorkout();
        user = userWithWorkout.getUser();

        exerciseIdToName = new HashMap<>();
        for (String id : user.getOwnedExercises().keySet()) {
            exerciseIdToName.put(id, user.getOwnedExercises().get(id).getExerciseName());
        }

        return inflater.inflate(R.layout.fragment_new_workout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        routineRecyclerView = view.findViewById(R.id.recycler_view);
        emptyDayView = view.findViewById(R.id.empty_view);
        dayTitleTV = view.findViewById(R.id.day_text_view);
        weekButton = view.findViewById(R.id.add_week_btn);
        dayButton = view.findViewById(R.id.add_day_btn);
        customSortLayout = view.findViewById(R.id.custom_sort_layout);
        Button saveSortButton = view.findViewById(R.id.done_sorting_btn);
        radioLayout = view.findViewById(R.id.mode_linear_layout);
        mainRelativeLayout = view.findViewById(R.id.button_spinner_layout);
        saveButton = view.findViewById(R.id.save_button);

        saveButton.setText(R.string.create);
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
            mainRelativeLayout.setVisibility(View.VISIBLE);
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
        menu.add(0, RoutineDay.alphabeticalSortAscending, 0, "Alphabetical (A-Z)");
        menu.add(0, RoutineDay.alphabeticalSortDescending, 0, "Alphabetical (Z-A)");
        menu.add(0, RoutineDay.weightSortAscending, 0, "Weight (Ascending)");
        menu.add(0, RoutineDay.weightSortDescending, 0, "Weight (Descending)");
        menu.add(0, RoutineDay.customSort, 0, "Drag 'n Drop");

        dropDownMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case RoutineDay.alphabeticalSortAscending:
                    this.pendingRoutine.sortDay(currentWeekIndex, currentDayIndex, RoutineDay.alphabeticalSortAscending, exerciseIdToName);
                    updateRoutineListUI();
                    return true;
                case RoutineDay.alphabeticalSortDescending:
                    this.pendingRoutine.sortDay(currentWeekIndex, currentDayIndex, RoutineDay.alphabeticalSortDescending, exerciseIdToName);
                    updateRoutineListUI();
                    return true;
                case RoutineDay.weightSortDescending:
                    this.pendingRoutine.sortDay(currentWeekIndex, currentDayIndex, RoutineDay.weightSortDescending, exerciseIdToName);
                    updateRoutineListUI();
                    return true;
                case RoutineDay.weightSortAscending:
                    this.pendingRoutine.sortDay(currentWeekIndex, currentDayIndex, RoutineDay.weightSortAscending, exerciseIdToName);
                    updateRoutineListUI();
                    return true;
                case RoutineDay.customSort:
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

    @Override
    public void onPause() {
        hideAllDialogs();
        super.onPause();
    }

    @Override
    public void hideAllDialogs() {
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    /**
     * Sets the onclicklisteners of the week and day button depending on the current mode.
     */
    private void setButtonListeners() {
        weekButton.setOnClickListener((v -> {
            if (mode == Variables.ADD_MODE) {
                currentDayIndex = 0;
                // for now only allow for weeks to be appended not inserted
                currentWeekIndex = pendingRoutine.getNumberOfWeeks();
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
            for (Integer week : pendingRoutine) {
                for (Integer day : pendingRoutine.getWeek(week)) {
                    if (pendingRoutine.getExerciseListForDay(week, day).isEmpty()) {
                        validRoutine = false;
                    }
                }
            }
            if (validRoutine) {
                promptCreate();
            } else {
                Toast.makeText(getContext(), "Each day must have at least one exercise!", Toast.LENGTH_LONG).show();
            }
        });
        dayButton.setOnClickListener((v -> {
            if (mode == Variables.ADD_MODE) {
                // for now only allow for weeks to be appended not insert
                currentDayIndex = pendingRoutine.getWeek(currentWeekIndex).getNumberOfDays();
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
                if (pendingRoutine.getExerciseListForDay(currentWeekIndex, currentDayIndex).isEmpty()) {
                    Toast.makeText(getContext(), "Must have at least one exercise in this day to copy it.", Toast.LENGTH_LONG).show();
                } else {
                    promptCopyDay();
                }
            }
        }));
    }

    /**
     * Updates the button texts of the week/day buttons depending on the current mode.
     */
    private void updateButtonTexts() {
        if (mode == Variables.ADD_MODE) {
            if (this.pendingRoutine.getNumberOfWeeks() >= Variables.MAX_NUMBER_OF_WEEKS) {
                weekButton.setText(getString(R.string.max_reached_msg));
                weekButton.setEnabled(false);
            } else {
                weekButton.setText(getString(R.string.add_week_msg));
                weekButton.setEnabled(true);
            }
            if (this.pendingRoutine.getWeek(currentWeekIndex).getNumberOfDays() >= Variables.WORKOUT_MAX_NUMBER_OF_DAYS) {
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

    /**
     * Enables the spinners to provide a means of navigating the weeks by week and day.
     *
     * @param view root view of the fragment
     */
    private void setSpinnerListeners(View view) {
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

    /**
     * Updates spinner list with all the available weeks to progress.
     */
    private void updateWeekSpinnerValues() {
        weekSpinnerValues.clear();
        for (Integer weekIndex : pendingRoutine) {
            weekSpinnerValues.add("Week " + (weekIndex + 1));
        }
        weekAdapter.notifyDataSetChanged();
    }

    /**
     * Updates spinner list with all the available day to progress.
     */
    private void updateDaySpinnerValues() {
        daySpinnerValues.clear();
        for (Integer dayIndex : pendingRoutine.getWeek(currentWeekIndex)) {
            daySpinnerValues.add("Day " + (dayIndex + 1));
        }
        dayAdapter.notifyDataSetChanged();
    }

    /**
     * Updates the displayed list of exercises of a given day in the pending routine.
     */
    private void updateRoutineListUI() {
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

    /**
     * Checks if the specific day has exercises in it or not. If not, show a textview alerting user
     */
    private void checkEmpty() {
        emptyDayView.setVisibility(pendingRoutine.getExerciseListForDay(currentWeekIndex, currentDayIndex).isEmpty()
                ? View.VISIBLE : View.GONE);
        if (mode == Variables.DELETE_MODE) {
            emptyDayView.setText(getString(R.string.empty_workout_day_delete_mode));
        } else {
            emptyDayView.setText(getString(R.string.empty_workout_day));
        }
    }

    /**
     * Allows the user to drag on specific exercises within a day to the position of their liking.
     */
    private void enableCustomSortMode() {
        customSortLayout.setVisibility(View.VISIBLE);
        saveButton.setVisibility(View.GONE);
        sortButton.setVisibility(View.GONE);
        mainRelativeLayout.setVisibility(View.GONE);
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
                    if (pendingRoutine.getNumberOfWeeks() > 1) {
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

    private void promptDeleteDay() {
        String message = "Are you sure you wish to delete day " + (currentDayIndex + 1) + "?\n\n" +
                "Doing so will delete ALL exercises associated with this day, and this action cannot be reversed!";
        alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                .setTitle("Delete Day " + (currentDayIndex + 1))
                .setMessage(message)
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (pendingRoutine.getWeek(currentWeekIndex).getNumberOfDays() > 1) {
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

    private void promptCopyDay() {
        View popupView = getLayoutInflater().inflate(R.layout.popup_copy_day_week, null);
        int totalDays = 0;
        List<String> days = new ArrayList<>();
        for (Integer week : pendingRoutine) {
            for (Integer day : pendingRoutine.getWeek(week)) {
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
            final RoutineDay dayToBeCopied = pendingRoutine.getDay(currentWeekIndex, currentDayIndex).clone();
            int count = 0;
            for (Integer week : pendingRoutine) {
                for (Integer day : pendingRoutine.getWeek(week)) {
                    if (count == dayPicker.getValue()) {
                        currentWeekIndex = week;
                        currentDayIndex = day;
                        break;
                    }
                    count++;
                }
            }
            // do the copy
            pendingRoutine.putDay(currentWeekIndex, currentDayIndex, dayToBeCopied);

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
            final RoutineDay dayToBeCopied = pendingRoutine.getDay(currentWeekIndex, currentDayIndex).clone();
            currentDayIndex = pendingRoutine.getWeek(currentWeekIndex).getNumberOfDays();
            pendingRoutine.putDay(currentWeekIndex, currentDayIndex, dayToBeCopied);

            daySpinner.setSelection(currentDayIndex);
            updateDaySpinnerValues();
            updateRoutineListUI();
            updateButtonTexts();
            alertDialog.dismiss();
        });
        if (pendingRoutine.getWeek(currentWeekIndex).getNumberOfDays() >= Variables.WORKOUT_MAX_NUMBER_OF_DAYS) {
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
        final View popupView = getLayoutInflater().inflate(R.layout.popup_copy_day_week, null);
        int totalWeeks = pendingRoutine.getNumberOfWeeks();

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
        String copyToExistingMsg = String.format("Copy Week %d to one of the following weeks." +
                "\n\nNote that doing this will overwrite ALL existing days in the target week", currentWeekIndex + 1);
        copyToExistingTv.setText(copyToExistingMsg);
        Button copyToExistingButton = popupView.findViewById(R.id.copy_to_existing_btn);
        copyToExistingButton.setOnClickListener(v -> {
            int targetWeek = dayPicker.getValue();
            final RoutineWeek weekToBeCopied = pendingRoutine.getWeek(currentWeekIndex);

            // do the copy
            pendingRoutine.putWeek(targetWeek, weekToBeCopied.clone());
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
            RoutineWeek weekToBeCopied = pendingRoutine.getWeek(currentWeekIndex);
            // do the copy
            currentWeekIndex = pendingRoutine.getNumberOfWeeks();
            pendingRoutine.putWeek(currentWeekIndex, weekToBeCopied.clone());
            currentDayIndex = 0;

            daySpinner.setSelection(currentDayIndex);
            weekSpinner.setSelection(currentWeekIndex);
            updateDaySpinnerValues();
            updateWeekSpinnerValues();
            updateRoutineListUI();
            updateButtonTexts();
            alertDialog.dismiss();
        });
        if (pendingRoutine.getNumberOfWeeks() >= Variables.MAX_NUMBER_OF_WEEKS) {
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

    private void promptCreate() {
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
                .setTitle("Create workout")
                .setView(popupView)
                .setPositiveButton("Create", null)
                .setNegativeButton("Cancel", null)
                .create();
        alertDialog.setOnShowListener(dialogInterface -> {
            Button createButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            createButton.setOnClickListener(view -> {
                String workoutName = workoutNameInput.getText().toString().trim();
                List<String> workoutNames = new ArrayList<>();
                for (String workoutId : user.getUserWorkouts().keySet()) {
                    workoutNames.add(user.getUserWorkouts().get(workoutId).getWorkoutName());
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
        AndroidHelper.showLoadingDialog(loadingDialog, "Creating...");
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ResultStatus<UserWithWorkout> resultStatus = this.workoutRepository.createWorkout(pendingRoutine, workoutName);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                loadingDialog.dismiss();
                if (resultStatus.isSuccess()) {
                    String newWorkoutId = resultStatus.getData().getWorkout().getWorkoutId();
                    user.setCurrentWorkout(resultStatus.getData().getUser().getCurrentWorkout());
                    user.getUserWorkouts().put(newWorkoutId,
                            resultStatus.getData().getUser().getUserWorkouts().get(newWorkoutId));
                    user.updateOwnedExercises(resultStatus.getData().getUser().getOwnedExercises());

                    userWithWorkout.setWorkout(resultStatus.getData().getWorkout());
                    ((WorkoutActivity) getActivity()).finishFragment();
                } else {
                    ErrorDialog.showErrorDialog("Create Workout Error", resultStatus.getErrorMessage(), getContext());
                }
            });
        });
    }

    /**
     * Shows a popup that lists all exercises for a given exercise focus. Used to add exercises to a given day
     * in the routine.
     */
    private void popupAddExercises() {
        View popupView = getLayoutInflater().inflate(R.layout.popup_pick_exercise, null);
        pickExerciseRecyclerView = popupView.findViewById(R.id.pick_exercises_recycler_view);
        exerciseNotFoundTV = popupView.findViewById(R.id.search_not_found_TV);
        final Spinner focusSpinner = popupView.findViewById(R.id.focus_spinner);
        allOwnedExercises = new HashMap<>();
        List<String> focusList = Variables.FOCUS_LIST;
        for (String focus : focusList) {
            // init the map of a specific focus to the list of exercises it contains
            allOwnedExercises.put(focus, new ArrayList<>());
        }

        for (String exerciseId : user.getOwnedExercises().keySet()) {
            OwnedExercise ownedExercise = user.getOwnedExercises().get(exerciseId);
            List<String> focusesOfExercise = ownedExercise.getFocuses();
            for (String focus : focusesOfExercise) {
                if (!allOwnedExercises.containsKey(focus)) {
                    // focus somehow hasn't been added before
                    focusList.add(focus);
                    allOwnedExercises.put(focus, new ArrayList<>());
                }
                allOwnedExercises.get(focus).add(ownedExercise);
            }
        }

        SearchView searchView = popupView.findViewById(R.id.search_input);
        searchView.setOnSearchClickListener(v -> {
            // populate the list view with all exercises
            ArrayList<OwnedExercise> sortedExercises = new ArrayList<>();
            for (String focus : allOwnedExercises.keySet()) {
                for (OwnedExercise exercise : allOwnedExercises.get(focus)) {
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
        // initially select first item from spinner, then always select the one the user last clicked. Note this auto calls the method to update exercises for this focus
        focusSpinner.setSelection((spinnerFocus == null) ? 0 : focusList.indexOf(spinnerFocus));
        // view is all set up, so now create the dialog with it
        alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                .setTitle("Add Exercises")
                .setView(popupView)
                .setPositiveButton("Done", null)
                .create();
        alertDialog.show();
    }

    /**
     * Displays all the exercises associated with the currently selected focus.
     */
    private void updateExerciseChoices() {
        ArrayList<OwnedExercise> sortedExercises = new ArrayList<>(allOwnedExercises.get(spinnerFocus));
        Collections.sort(sortedExercises);
        addExerciseAdapter = new AddExerciseAdapter(sortedExercises);
        pickExerciseRecyclerView.setAdapter(addExerciseAdapter);
        pickExerciseRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    public boolean isModified() {
        boolean modified = false; // assume that if workout is totally empty they aren't in progress of making one
        for (int week = 0; week < pendingRoutine.getNumberOfWeeks(); week++) {
            for (int day = 0; day < pendingRoutine.getWeek(week).getNumberOfDays(); day++) {
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
            updateExerciseChoices(); // update choices for exercise based on this newly selected focus
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

        private List<OwnedExercise> exercises;
        private List<OwnedExercise> displayList;

        AddExerciseAdapter(List<OwnedExercise> exerciseRoutines) {
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
            final OwnedExercise ownedExercise = displayList.get(position);
            final CheckBox exercise = holder.exercise;
            exercise.setText(ownedExercise.getExerciseName());
            // check if the exercise is already in this specific day
            boolean isChecked = false;
            for (RoutineExercise routineExercise : pendingRoutine.getExerciseListForDay(currentWeekIndex, currentDayIndex)) {
                if (routineExercise.getExerciseId().equals(ownedExercise.getExerciseId())) {
                    isChecked = true;
                    break;
                }
            }
            exercise.setChecked(isChecked);

            exercise.setOnClickListener(v -> {
                if (exercise.isChecked()) {
                    pendingRoutine.addExercise(currentWeekIndex, currentDayIndex,
                            new RoutineExercise(ownedExercise, ownedExercise.getExerciseId()));
                } else {
                    pendingRoutine.removeExercise(currentWeekIndex, currentDayIndex, ownedExercise.getExerciseId());
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
                List<OwnedExercise> filteredList = new ArrayList<>();
                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(exercises);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();
                    for (OwnedExercise ownedExercise : exercises) {
                        if (ownedExercise.getExerciseName().toLowerCase().contains(filterPattern)) {
                            filteredList.add(ownedExercise);
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
