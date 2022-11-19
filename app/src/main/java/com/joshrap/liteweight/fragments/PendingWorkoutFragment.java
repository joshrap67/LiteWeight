package com.joshrap.liteweight.fragments;

import static android.os.Looper.getMainLooper;

import androidx.appcompat.app.AlertDialog;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Parcelable;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.activities.WorkoutActivity;
import com.joshrap.liteweight.adapters.CustomSortAdapter;
import com.joshrap.liteweight.adapters.RoutineDayAdapter;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.interfaces.FragmentWithDialog;
import com.joshrap.liteweight.models.OwnedExercise;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.models.Routine;
import com.joshrap.liteweight.models.RoutineDay;
import com.joshrap.liteweight.models.RoutineExercise;
import com.joshrap.liteweight.models.RoutineWeek;
import com.joshrap.liteweight.models.User;
import com.joshrap.liteweight.models.UserWithWorkout;
import com.joshrap.liteweight.models.Workout;
import com.joshrap.liteweight.network.repos.WorkoutRepository;
import com.joshrap.liteweight.utils.AndroidUtils;
import com.joshrap.liteweight.utils.ValidatorUtils;
import com.joshrap.liteweight.utils.WorkoutUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

public class PendingWorkoutFragment extends Fragment implements FragmentWithDialog {

    private RecyclerView weekRecyclerView, routineDayRecyclerView, pickExerciseRecyclerView;
    private AlertDialog alertDialog;
    private TextView routineDayTitleTV, exerciseNotFoundTV, emptyDayTV, routineDayTagTV;
    private String spinnerFocus;
    private HashMap<String, List<OwnedExercise>> allOwnedExercises; // focus -> exercises
    private int currentWeekIndex, currentDayIndex;
    private User user;
    private Map<String, String> exerciseIdToName;
    private ImageButton sortExercisesButton, routineDayMoreIcon;
    private Routine pendingRoutine;
    private UserWithWorkout userWithWorkout;
    private boolean isRoutineDayViewShown, isSorting, isExistingWorkout, firstWorkout;
    private OnBackPressedCallback backPressedCallback;
    private ConstraintLayout routineDayView, routineView;
    private ExtendedFloatingActionButton saveWorkoutButton, addWeekButton, saveCustomSortButton, addExercisesButton;
    private AddExerciseAdapter addExerciseAdapter;
    private WeekAdapter weekAdapter;
    private RoutineDayAdapter routineDayAdapter;
    private Workout pendingWorkout;
    private Map<String, Double> exerciseIdToCurrentMaxWeight; // shortcut for first workout being created - prevents user from constantly having to change from 0lb

    @Inject
    AlertDialog loadingDialog;
    @Inject
    WorkoutRepository workoutRepository;
    @Inject
    SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        Injector.getInjector(getContext()).inject(this);
        ((WorkoutActivity) getActivity()).toggleBackButton(true);

        if (this.getArguments() != null) {
            isExistingWorkout = this.getArguments().getBoolean(Variables.EXISTING_WORKOUT);
        }

        currentDayIndex = 0;
        currentWeekIndex = 0;
        allOwnedExercises = new HashMap<>();
        userWithWorkout = ((WorkoutActivity) getActivity()).getUserWithWorkout();
        user = userWithWorkout.getUser();

        if (isExistingWorkout) {
            pendingWorkout = new Workout(userWithWorkout.getWorkout());
            pendingRoutine = pendingWorkout.getRoutine();
        } else {
            pendingRoutine = Routine.emptyRoutine();
            firstWorkout = !userWithWorkout.isWorkoutPresent();
        }

        setToolbarTitle();

        exerciseIdToName = new HashMap<>();
        exerciseIdToCurrentMaxWeight = new HashMap<>();
        for (String id : user.getOwnedExercises().keySet()) {
            exerciseIdToName.put(id, user.getOwnedExercises().get(id).getExerciseName());
            exerciseIdToCurrentMaxWeight.put(id, user.getOwnedExercises().get(id).getDefaultWeight());
        }

        return inflater.inflate(R.layout.fragment_pending_workout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        routineDayView = view.findViewById(R.id.routine_day_layout);
        routineView = view.findViewById(R.id.routine_week_layout);

        //region Views for routine day
        routineDayRecyclerView = view.findViewById(R.id.exercises_recycler_view);
        emptyDayTV = view.findViewById(R.id.empty_view_tv);
        routineDayTitleTV = view.findViewById(R.id.day_title_tv);
        routineDayTagTV = view.findViewById(R.id.day_tag_tv);

        saveCustomSortButton = view.findViewById(R.id.done_sorting_fab);
        saveCustomSortButton.setOnClickListener(v -> finishCustomSortMode());

        // set up sorting options
        sortExercisesButton = view.findViewById(R.id.sort_icon_button);
        final PopupMenu dropDownSortMenu = new PopupMenu(getContext(), sortExercisesButton);
        Menu sortMenu = dropDownSortMenu.getMenu();
        sortMenu.add(0, RoutineDay.alphabeticalSortAscending, 0, "Alphabetical (A-Z)");
        sortMenu.add(0, RoutineDay.alphabeticalSortDescending, 0, "Alphabetical (Z-A)");
        sortMenu.add(0, RoutineDay.weightSortAscending, 0, "Weight (Ascending)");
        sortMenu.add(0, RoutineDay.weightSortDescending, 0, "Weight (Descending)");
        sortMenu.add(0, RoutineDay.customSort, 0, "Drag 'n Drop");

        dropDownSortMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case RoutineDay.alphabeticalSortAscending:
                    this.pendingRoutine.sortDay(currentWeekIndex, currentDayIndex, RoutineDay.alphabeticalSortAscending, exerciseIdToName);
                    updateRoutineDayExerciseList();
                    return true;
                case RoutineDay.alphabeticalSortDescending:
                    this.pendingRoutine.sortDay(currentWeekIndex, currentDayIndex, RoutineDay.alphabeticalSortDescending, exerciseIdToName);
                    updateRoutineDayExerciseList();
                    return true;
                case RoutineDay.weightSortDescending:
                    this.pendingRoutine.sortDay(currentWeekIndex, currentDayIndex, RoutineDay.weightSortDescending, exerciseIdToName);
                    updateRoutineDayExerciseList();
                    return true;
                case RoutineDay.weightSortAscending:
                    this.pendingRoutine.sortDay(currentWeekIndex, currentDayIndex, RoutineDay.weightSortAscending, exerciseIdToName);
                    updateRoutineDayExerciseList();
                    return true;
                case RoutineDay.customSort:
                    enableCustomSortMode();
                    return true;
            }
            return false;
        });
        sortExercisesButton.setOnClickListener(v -> dropDownSortMenu.show());

        // set up more details for day
        routineDayMoreIcon = view.findViewById(R.id.day_more_icon_btn);
        final PopupMenu dropDownRoutineDayMenu = new PopupMenu(getContext(), routineDayMoreIcon);
        Menu routineDayMenu = dropDownRoutineDayMenu.getMenu();
        final int deleteDayId = 0;
        final int copyDayToWeekId = 1;
        final int copyDayToExistingId = 2;
        final int setDayTagId = 3;
        routineDayMenu.add(0, copyDayToWeekId, 0, "Copy To Week");
        routineDayMenu.add(0, copyDayToExistingId, 0, "Copy To Existing Day");
        routineDayMenu.add(0, deleteDayId, 0, "Delete Day");
        routineDayMenu.add(0, setDayTagId, 0, "Set Tag");

        dropDownRoutineDayMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case deleteDayId:
                    promptDeleteDay();
                    return true;
                case copyDayToExistingId:
                    promptCopyToExistingDay();
                    return true;
                case copyDayToWeekId:
                    copyDayToWeek();
                    return true;
                case setDayTagId:
                    promptSetDayTag();
                    return true;
            }
            return false;
        });
        routineDayMoreIcon.setOnClickListener(v -> {
            ((WorkoutActivity) getActivity()).hideKeyboard();
            dropDownRoutineDayMenu.show();
        });

        addExercisesButton = view.findViewById(R.id.add_exercises_fab);
        addExercisesButton.setOnClickListener(v -> {
            ((WorkoutActivity) getActivity()).hideKeyboard();
            popupAddExercises();
        });
        //endregion

        //region Views for routine
        weekRecyclerView = view.findViewById(R.id.week_recycler_view);
        setWeekAdapter();

        addWeekButton = view.findViewById(R.id.add_week_fab);
        addWeekButton.setOnClickListener(v -> {
            if (pendingRoutine.getNumberOfWeeks() >= Variables.MAX_NUMBER_OF_WEEKS) {
                // otherwise user can bypass by clicking quickly
                return;
            }
            pendingRoutine.addEmptyWeek();
            weekAdapter.notifyItemInserted(pendingRoutine.getNumberOfWeeks() - 1);
            if (pendingRoutine.getNumberOfWeeks() >= Variables.MAX_NUMBER_OF_WEEKS) {
                addWeekButton.hide();
            }

            // scroll to end when new week is added
            weekRecyclerView.post(() -> weekRecyclerView.scrollToPosition(weekAdapter.getItemCount() - 1));
        });

        saveWorkoutButton = view.findViewById(R.id.save_fab);
        if (!isExistingWorkout) {
            saveWorkoutButton.setText(R.string.create);
        }
        saveWorkoutButton.setOnClickListener(v -> {
            if (isExistingWorkout) {
                saveWorkout();
            } else {
                promptCreate();
            }

        });
        //endregion

        backPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isSorting) {
                    finishCustomSortMode();
                } else if (isRoutineDayViewShown) {
                    ((WorkoutActivity) getActivity()).hideKeyboard();
                    finishCustomSortMode(); // in case user was custom sorting need to reset day layout
                    switchToRoutineView();
                } else if (isRoutineModified()) {
                    hideAllDialogs(); // since user could spam back button and cause multiple ones to show
                    alertDialog = new AlertDialog.Builder(getContext())
                            .setTitle("Unsaved Changes")
                            .setMessage(R.string.unsaved_workout_msg)
                            .setPositiveButton("Yes", (dialog, which) -> {
                                remove();
                                requireActivity().onBackPressed();
                            })
                            .setNegativeButton("No", null)
                            .create();
                    alertDialog.show();
                } else {
                    remove();
                    requireActivity().onBackPressed();
                }
            }
        };

        super.onViewCreated(view, savedInstanceState);
    }

    private void addBackPressedCallback() {
        requireActivity().getOnBackPressedDispatcher().addCallback(backPressedCallback);
    }

    private void setWeekAdapter() {
        LinearLayoutManager weekLayoutManager = new LinearLayoutManager(getActivity());
        weekAdapter = new WeekAdapter(pendingRoutine);
        weekRecyclerView.setAdapter(weekAdapter);
        weekRecyclerView.setLayoutManager(weekLayoutManager);
    }

    private void setToolbarTitle() {
        ((WorkoutActivity) getActivity()).updateToolbarTitle(isExistingWorkout
                ? pendingWorkout.getWorkoutName()
                : Variables.CREATE_WORKOUT_TITLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (backPressedCallback != null) {
            addBackPressedCallback();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        hideAllDialogs();
        backPressedCallback.remove();
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

    private void switchToRoutineDayView(int week, int day) {
        isRoutineDayViewShown = true;
        routineDayView.setVisibility(View.VISIBLE);
        routineView.setVisibility(View.GONE);
        ((WorkoutActivity) getActivity()).updateToolbarTitle(getString(R.string.day_details));

        currentDayIndex = day;
        currentWeekIndex = week;
        routineDayTitleTV.setText(WorkoutUtils.generateDayTitle(currentWeekIndex, currentDayIndex));
        setRoutineDayTagTV(currentWeekIndex, currentDayIndex);
        updateRoutineDayExerciseList();
    }

    private void switchToRoutineView() {
        isRoutineDayViewShown = false;
        routineDayView.setVisibility(View.GONE);
        routineView.setVisibility(View.VISIBLE);
        setToolbarTitle();

        // this is required to get the exercise count to update
        weekAdapter.notifyItemChanged(currentWeekIndex, true);
    }

    private boolean isRoutineModified() {
        if (isExistingWorkout) {
            return Routine.routinesDifferent(pendingRoutine, userWithWorkout.getWorkout().getRoutine());
        }

        if (pendingRoutine.getTotalNumberOfDays() > 1) {
            return true;
        }

        // essentially routine is only not modified for new workout if not a single exercise or day has been added
        return pendingRoutine.getDay(0, 0).getExercises().size() != 0;
    }

    private void updateRoutineDayExerciseList() {
        routineDayAdapter = new RoutineDayAdapter(exerciseIdToName, exerciseIdToCurrentMaxWeight, pendingRoutine, currentWeekIndex, currentDayIndex, user.getUserPreferences().isMetricUnits(), getActivity());
        routineDayRecyclerView.setAdapter(routineDayAdapter);
        routineDayRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        routineDayAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            // since google is stupid af and doesn't have a simple setEmptyView for recyclerView...
            @Override
            public void onChanged() {
                super.onChanged();
                if (isRoutineDayViewShown) {
                    checkEmptyView();
                }
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                if (isRoutineDayViewShown) {
                    checkEmptyView();
                }
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                if (isRoutineDayViewShown) {
                    checkEmptyView();
                }
            }
        });
        routineDayTitleTV.setText(WorkoutUtils.generateDayTitle(currentWeekIndex, currentDayIndex));
        setRoutineDayTagTV(currentWeekIndex, currentDayIndex);
        checkEmptyView();
    }

    private void setRoutineDayTagTV(int weekIndex, int dayIndex) {
        RoutineDay day = pendingRoutine.getDay(weekIndex, dayIndex);
        routineDayTagTV.setVisibility(day.getTag() == null ? View.INVISIBLE : View.VISIBLE);
        routineDayTagTV.setText(day.getTag() + " "); // android cuts off italics on wrap content without trailing whitespace
    }

    private void checkEmptyView() {
        emptyDayTV.setVisibility(pendingRoutine.getExerciseListForDay(currentWeekIndex, currentDayIndex).isEmpty()
                ? View.VISIBLE : View.GONE);
    }

    /**
     * Allows the user to drag specific exercises within a day to the position of their liking.
     */
    private void enableCustomSortMode() {
        isSorting = true;
        addExercisesButton.hide();
        saveCustomSortButton.show();
        saveWorkoutButton.hide();
        sortExercisesButton.setVisibility(View.INVISIBLE);
        routineDayMoreIcon.setVisibility(View.INVISIBLE);

        CustomSortAdapter routineAdapter = new CustomSortAdapter(pendingRoutine.getExerciseListForDay(currentWeekIndex, currentDayIndex), exerciseIdToName, false);
        customSortDispatcher.attachToRecyclerView(routineDayRecyclerView);
        routineDayRecyclerView.setAdapter(routineAdapter);
    }

    private void finishCustomSortMode() {
        isSorting = false;
        saveCustomSortButton.hide();
        saveWorkoutButton.show();
        sortExercisesButton.setVisibility(View.VISIBLE);
        routineDayMoreIcon.setVisibility(View.VISIBLE);
        updateRoutineDayExerciseList();
        addExercisesButton.show();
        // needed to avoid weird bug that happens when user tries to sort again by dragging
        customSortDispatcher.attachToRecyclerView(null);
    }

    private final ItemTouchHelper customSortDispatcher = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
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


    private void promptDeleteWeek(int weekIndex) {
        alertDialog = new AlertDialog.Builder(getContext())
                .setTitle("Delete Week " + (weekIndex + 1))
                .setMessage(R.string.remove_week_warning_msg)
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (pendingRoutine.getNumberOfWeeks() > 1) {
                        deleteWeek(weekIndex);
                    } else {
                        Toast.makeText(getContext(), "Cannot delete only week from workout.", Toast.LENGTH_LONG).show();
                    }
                    alertDialog.dismiss();
                })
                .setNegativeButton("No", null)
                .create();
        alertDialog.show();
    }

    private void deleteWeek(int weekIndex) {
        pendingRoutine.deleteWeek(weekIndex);
        weekAdapter.notifyItemRemoved(weekIndex);
        weekAdapter.notifyItemRangeChanged(weekIndex, weekAdapter.getItemCount());
        addWeekButton.show();
    }

    private void promptSetDayTag() {
        View popupView = getLayoutInflater().inflate(R.layout.popup_set_routine_day_tag, null);
        EditText dayTagInput = popupView.findViewById(R.id.day_tag_input);
        TextInputLayout dayTagInputLayout = popupView.findViewById(R.id.day_tag_input_layout);
        dayTagInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_DAY_TAG_LENGTH)});
        dayTagInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(dayTagInputLayout));

        RoutineDay currentDay = pendingRoutine.getDay(currentWeekIndex, currentDayIndex);
        dayTagInput.setText(currentDay.getTag());

        alertDialog = new AlertDialog.Builder(getContext())
                .setTitle("Set Day Tag")
                .setView(popupView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String dayTag = dayTagInput.getText().toString().trim();
                    currentDay.setTag(dayTag);
                    setRoutineDayTagTV(currentWeekIndex, currentDayIndex);
                })
                .setNegativeButton("Cancel", null)
                .create();
        alertDialog.show();
    }

    private void promptDeleteDay() {
        alertDialog = new AlertDialog.Builder(getContext())
                .setTitle("Delete Day " + (currentDayIndex + 1))
                .setMessage(R.string.remove_day_warning_msg)
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (pendingRoutine.getWeek(currentWeekIndex).getNumberOfDays() > 1) {
                        deleteDay();
                    } else {
                        Toast.makeText(getContext(), "Cannot delete only day from week.", Toast.LENGTH_LONG).show();
                    }
                    alertDialog.dismiss();
                })
                .setNegativeButton("No", null)
                .create();
        alertDialog.show();
    }

    private void deleteDay() {
        pendingRoutine.deleteDay(currentWeekIndex, currentDayIndex);
        switchToRoutineView();
    }

    private void promptCopyToExistingDay() {
        View popupView = getLayoutInflater().inflate(R.layout.popup_copy_day_to_existing, null);
        List<String> dayLabels = new ArrayList<>();
        for (int weekIndex = 0; weekIndex < pendingRoutine.getNumberOfWeeks(); weekIndex++) {
            RoutineWeek week = pendingRoutine.getWeek(weekIndex);
            for (int dayIndex = 0; dayIndex < week.getNumberOfDays(); dayIndex++) {
                String dayTitle = WorkoutUtils.generateDayTitle(weekIndex, dayIndex);
                dayLabels.add(dayTitle);
            }
        }
        String[] dayLabelsArray = new String[pendingRoutine.getTotalNumberOfDays()];
        dayLabels.toArray(dayLabelsArray);

        NumberPicker dayPicker = popupView.findViewById(R.id.day_picker);
        dayPicker.setMinValue(0);
        dayPicker.setMaxValue(pendingRoutine.getTotalNumberOfDays() - 1);
        dayPicker.setValue(0);
        dayPicker.setWrapSelectorWheel(false);
        dayPicker.setDisplayedValues(dayLabelsArray);

        alertDialog = new AlertDialog.Builder(getContext())
                .setTitle(String.format("Copy %s", WorkoutUtils.generateDayTitle(currentWeekIndex, currentDayIndex)))
                .setView(popupView)
                .setPositiveButton("Copy", (dialog, which) -> {
                    int originalWeekIndex = currentWeekIndex;

                    final RoutineDay dayToBeCopied = pendingRoutine.getDay(currentWeekIndex, currentDayIndex).clone();
                    int count = 0;
                    for (int weekIndex = 0; weekIndex < pendingRoutine.getNumberOfWeeks(); weekIndex++) {
                        RoutineWeek week = pendingRoutine.getWeek(weekIndex);
                        for (int dayIndex = 0; dayIndex < week.getNumberOfDays(); dayIndex++) {
                            if (count == dayPicker.getValue()) {
                                currentWeekIndex = weekIndex;
                                currentDayIndex = dayIndex;
                            }
                            count++;
                        }
                    }

                    pendingRoutine.putDay(currentWeekIndex, currentDayIndex, dayToBeCopied);
                    // needed so the original day in the week list is updated
                    weekAdapter.notifyItemChanged(originalWeekIndex, 0);

                    updateRoutineDayExerciseList();
                    alertDialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .create();
        alertDialog.show();
    }

    private void copyDayToWeek() {
        View popupView = getLayoutInflater().inflate(R.layout.popup_copy_day_to_week, null);
        int totalWeeks = pendingRoutine.getNumberOfWeeks();

        String[] weekDisplays = new String[totalWeeks];
        for (int i = 0; i < totalWeeks; i++) {
            weekDisplays[i] = String.format(Locale.US, "Week %d", i + 1);
        }
        NumberPicker weekPicker = popupView.findViewById(R.id.week_picker);
        weekPicker.setMinValue(0);
        weekPicker.setMaxValue(totalWeeks - 1);
        weekPicker.setValue(currentWeekIndex);
        weekPicker.setWrapSelectorWheel(false);
        weekPicker.setDisplayedValues(weekDisplays);

        alertDialog = new AlertDialog.Builder(getContext())
                .setTitle(String.format("Copy %s", WorkoutUtils.generateDayTitle(currentWeekIndex, currentDayIndex)))
                .setView(popupView)
                .setPositiveButton("Copy", (dialog, which) -> {
                    int targetWeek = weekPicker.getValue();

                    if (pendingRoutine.getWeek(targetWeek).getNumberOfDays() >= Variables.WORKOUT_MAX_NUMBER_OF_DAYS) {
                        alertDialog.dismiss();
                        Toast.makeText(getContext(), "Copy would exceed maximum number of days allowed in week.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    int originalWeekIndex = currentWeekIndex;
                    currentWeekIndex = targetWeek;

                    final RoutineDay dayToBeCopied = pendingRoutine.getDay(originalWeekIndex, currentDayIndex).clone();
                    currentDayIndex = pendingRoutine.getWeek(currentWeekIndex).getNumberOfDays();
                    pendingRoutine.appendDay(currentWeekIndex, dayToBeCopied);

                    // needed so the copied day in the week list is updated (in case it was copied outside the original week)
                    weekAdapter.notifyItemChanged(originalWeekIndex, 0);
                    weekAdapter.notifyItemChanged(currentWeekIndex, 0);

                    updateRoutineDayExerciseList();
                    alertDialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .create();
        alertDialog.show();
    }

    private void promptCopyToExistingWeek(int selectedWeek) {
        View popupView = getLayoutInflater().inflate(R.layout.popup_copy_week_to_existing, null);
        int totalWeeks = pendingRoutine.getNumberOfWeeks();

        String[] weekDisplays = new String[totalWeeks];
        for (int i = 0; i < totalWeeks; i++) {
            weekDisplays[i] = String.format(Locale.US, "Week %d", i + 1);
        }
        NumberPicker weekPicker = popupView.findViewById(R.id.week_picker);
        weekPicker.setMinValue(0);
        weekPicker.setMaxValue(totalWeeks - 1);
        weekPicker.setValue(selectedWeek);
        weekPicker.setWrapSelectorWheel(false);
        weekPicker.setDisplayedValues(weekDisplays);

        alertDialog = new AlertDialog.Builder(getContext())
                .setTitle(String.format(Locale.US, "Copy Week %d", selectedWeek + 1))
                .setView(popupView)
                .setPositiveButton("Copy", (dialog, which) -> {
                    int targetWeek = weekPicker.getValue();

                    final RoutineWeek weekToBeCopied = pendingRoutine.getWeek(selectedWeek);
                    pendingRoutine.putWeek(targetWeek, weekToBeCopied.clone());
                    weekAdapter.notifyItemChanged(targetWeek);

                    alertDialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .create();
        alertDialog.show();
    }

    private void copyWeekAsNew(int weekIndex) {
        RoutineWeek weekToBeCopied = pendingRoutine.getWeek(weekIndex);
        pendingRoutine.addWeek(weekToBeCopied.clone());
        weekAdapter.notifyItemInserted(pendingRoutine.getNumberOfWeeks() - 1);
    }

    private void promptCreate() {
        View popupView = getLayoutInflater().inflate(R.layout.popup_save_workout, null);
        EditText workoutNameInput = popupView.findViewById(R.id.workout_name_input);
        TextInputLayout workoutNameInputLayout = popupView.findViewById(R.id.workout_name_input_layout);
        workoutNameInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(workoutNameInputLayout));
        workoutNameInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_WORKOUT_NAME)});

        alertDialog = new AlertDialog.Builder(getContext())
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
                for (String workoutId : user.getWorkoutMetas().keySet()) {
                    workoutNames.add(user.getWorkoutMetas().get(workoutId).getWorkoutName());
                }
                String errorMsg = ValidatorUtils.validWorkoutName(workoutName, workoutNames);
                if (errorMsg != null) {
                    workoutNameInputLayout.setError(errorMsg);
                } else {
                    alertDialog.dismiss();
                    createWorkout(workoutName);
                }
            });
        });
        alertDialog.show();
    }

    private void createWorkout(String workoutName) {
        AndroidUtils.showLoadingDialog(loadingDialog, "Creating...");
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ResultStatus<UserWithWorkout> resultStatus = this.workoutRepository.createWorkout(pendingRoutine, workoutName);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                loadingDialog.dismiss();
                if (resultStatus.isSuccess()) {
                    String newWorkoutId = resultStatus.getData().getWorkout().getWorkoutId();
                    user.setCurrentWorkout(resultStatus.getData().getUser().getCurrentWorkout());
                    user.getWorkoutMetas().put(newWorkoutId,
                            resultStatus.getData().getUser().getWorkoutMetas().get(newWorkoutId));
                    user.updateOwnedExercises(resultStatus.getData().getUser().getOwnedExercises());

                    userWithWorkout.setWorkout(resultStatus.getData().getWorkout());

                    // once created, treat it as an existing workout
                    isExistingWorkout = true;
                    pendingWorkout = new Workout(userWithWorkout.getWorkout());
                    pendingRoutine = pendingWorkout.getRoutine();
                    setToolbarTitle();
                    setWeekAdapter(); // since adapter holds old references to weeks
                    saveWorkoutButton.setText(R.string.save);
                } else {
                    AndroidUtils.showErrorDialog(resultStatus.getErrorMessage(), getContext());
                }
            });
        });
    }

    private void saveWorkout() {
        AndroidUtils.showLoadingDialog(loadingDialog, "Saving...");
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ResultStatus<UserWithWorkout> resultStatus = this.workoutRepository.editWorkout(pendingWorkout.getWorkoutId(), pendingWorkout);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                loadingDialog.dismiss();
                if (resultStatus.isSuccess()) {
                    user.updateOwnedExercises(resultStatus.getData().getUser().getOwnedExercises());
                    userWithWorkout.setWorkout(resultStatus.getData().getWorkout());
                    pendingWorkout = new Workout(userWithWorkout.getWorkout());
                    pendingRoutine = pendingWorkout.getRoutine();

                    setWeekAdapter(); // since adapter holds old references to weeks
                    Toast.makeText(getContext(), "Workout saved.", Toast.LENGTH_LONG).show();
                } else {
                    AndroidUtils.showErrorDialog(resultStatus.getErrorMessage(), getContext());
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
        exerciseNotFoundTV = popupView.findViewById(R.id.exercise_not_found_tv);
        Spinner focusSpinner = popupView.findViewById(R.id.focus_spinner);

        allOwnedExercises = new HashMap<>();
        List<String> focusList = Variables.FOCUS_LIST;
        for (String focus : focusList) {
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

        SearchView searchExerciseView = popupView.findViewById(R.id.exercise_search_view);
        searchExerciseView.setOnSearchClickListener(v -> {
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
            addExerciseAdapter = new PendingWorkoutFragment.AddExerciseAdapter(sortedExercises);
            pickExerciseRecyclerView.setAdapter(addExerciseAdapter);
            pickExerciseRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

            focusSpinner.setVisibility(View.GONE);
        });
        searchExerciseView.setOnCloseListener(() -> {
            focusSpinner.setVisibility(View.VISIBLE);
            exerciseNotFoundTV.setVisibility(View.GONE);
            updateExerciseChoices();
            return false;
        });

        searchExerciseView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchExerciseView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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

        focusList.sort(String.CASE_INSENSITIVE_ORDER);
        ArrayAdapter<String> focusAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, focusList);
        focusSpinner.setAdapter(focusAdapter);
        focusSpinner.setOnItemSelectedListener(new PendingWorkoutFragment.FocusSpinnerListener());
        // initially select first item from spinner, then always select the one the user last clicked. Note this auto calls the method to update exercises for this focus
        focusSpinner.setSelection((spinnerFocus == null) ? 0 : focusList.indexOf(spinnerFocus));

        alertDialog = new AlertDialog.Builder(getContext())
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
        addExerciseAdapter = new PendingWorkoutFragment.AddExerciseAdapter(sortedExercises);
        pickExerciseRecyclerView.setAdapter(addExerciseAdapter);
        pickExerciseRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    //region Classes/Adapters

    private class FocusSpinnerListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            spinnerFocus = parent.getItemAtPosition(pos).toString();
            updateExerciseChoices(); // update choices for exercise based on this newly selected focus
        }

        public void onNothingSelected(AdapterView parent) {
        }
    }

    private class AddExerciseAdapter extends RecyclerView.Adapter<PendingWorkoutFragment.AddExerciseAdapter.ViewHolder> implements Filterable {
        class ViewHolder extends RecyclerView.ViewHolder {
            private final CheckBox exerciseCheckbox;

            ViewHolder(View itemView) {
                super(itemView);
                exerciseCheckbox = itemView.findViewById(R.id.exercise_checkbox);
            }
        }

        private final List<OwnedExercise> allExercises;
        private final List<OwnedExercise> displayList;

        AddExerciseAdapter(List<OwnedExercise> exercises) {
            this.allExercises = exercises;
            displayList = new ArrayList<>(this.allExercises);
        }


        @NonNull
        @Override
        public PendingWorkoutFragment.AddExerciseAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View exerciseView = inflater.inflate(R.layout.row_add_exercise, parent, false);
            return new PendingWorkoutFragment.AddExerciseAdapter.ViewHolder(exerciseView);
        }

        @Override
        public void onBindViewHolder(PendingWorkoutFragment.AddExerciseAdapter.ViewHolder holder, int position) {
            final OwnedExercise ownedExercise = displayList.get(position);
            CheckBox exerciseCheckbox = holder.exerciseCheckbox;
            exerciseCheckbox.setText(ownedExercise.getExerciseName());
            // check if the exercise is already in this specific day
            boolean isChecked = false;
            for (RoutineExercise routineExercise : pendingRoutine.getExerciseListForDay(currentWeekIndex, currentDayIndex)) {
                if (routineExercise.getExerciseId().equals(ownedExercise.getExerciseId())) {
                    isChecked = true;
                    break;
                }
            }
            exerciseCheckbox.setChecked(isChecked);

            exerciseCheckbox.setOnClickListener(v -> {
                if (exerciseCheckbox.isChecked()) {
                    RoutineExercise exercise = new RoutineExercise(ownedExercise, ownedExercise.getExerciseId());
                    pendingRoutine.addExercise(currentWeekIndex, currentDayIndex, exercise);
                    if (firstWorkout && exercise.getWeight() == 0 && exerciseIdToCurrentMaxWeight.containsKey(ownedExercise.getExerciseId())) {
                        exercise.setWeight(exerciseIdToCurrentMaxWeight.get(ownedExercise.getExerciseId()));
                    }
                    int newPosition = pendingRoutine.getExerciseListForDay(currentWeekIndex, currentDayIndex).size() - 1;
                    // adapter uses list of separate models to maintain expanded state, need to add it there too
                    routineDayAdapter.routineRowModels.add(new RoutineDayAdapter.RoutineRowModel(exercise, false));
                    routineDayAdapter.notifyItemInserted(newPosition);
                    routineDayRecyclerView.scrollToPosition(newPosition);
                } else {
                    pendingRoutine.removeExercise(currentWeekIndex, currentDayIndex, ownedExercise.getExerciseId());
                    // adapter uses list of separate models to maintain expanded state, need to remove it there too
                    routineDayAdapter.routineRowModels.removeIf(x -> x.getRoutineExercise().getExerciseId().equals(ownedExercise.getExerciseId()));
                    // too much of a pain to get the index in that adapter that this exercise could have been removed from
                    routineDayAdapter.notifyDataSetChanged();
                }
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

        private final Filter exerciseSearchFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<OwnedExercise> filteredList = new ArrayList<>();
                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(allExercises);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();
                    for (OwnedExercise ownedExercise : allExercises) {
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

    private class WeekAdapter extends RecyclerView.Adapter<WeekAdapter.WeekViewHolder> {

        class WeekViewHolder extends RecyclerView.ViewHolder {

            private final TextView weekTitle;
            private final RecyclerView dayRecyclerView;
            private final Button addDayButton;
            private final ImageButton weekMoreButton;

            WeekViewHolder(final View itemView) {
                super(itemView);

                weekTitle = itemView.findViewById(R.id.week_tv);
                dayRecyclerView = itemView.findViewById(R.id.day_recycler_view);
                addDayButton = itemView.findViewById(R.id.add_day_btn);
                weekMoreButton = itemView.findViewById(R.id.week_more_icon_btn);
            }
        }

        private final Routine routine;
        private final Map<Integer, Parcelable> weekScrollStates;

        WeekAdapter(Routine routine) {
            this.routine = routine;
            weekScrollStates = new HashMap<>();
        }

        @NonNull
        @Override
        public WeekViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.routine_week_card, viewGroup, false);
            return new WeekViewHolder(view);
        }

        @Override
        public void onViewRecycled(@NonNull WeekViewHolder holder) {
            // when week row is recycled, store the scroll state of the day list
            final int position = holder.getAdapterPosition();
            if (holder.dayRecyclerView.getLayoutManager() != null) {
                Parcelable layoutState = holder.dayRecyclerView.getLayoutManager().onSaveInstanceState();
                weekScrollStates.put(position, layoutState);
            }

            super.onViewRecycled(holder);
        }

        @Override
        public void onBindViewHolder(@NonNull WeekViewHolder weekViewHolder, int position, List<Object> payloads) {
            if (!payloads.isEmpty()) {
                RoutineWeek week = this.routine.getWeek(position);
                setAddDayButtonVisibility(week, weekViewHolder.addDayButton);
                // need to update all because no way of knowing which one had its number of exercises change
                weekViewHolder.dayRecyclerView.getAdapter().notifyDataSetChanged();

            } else {
                super.onBindViewHolder(weekViewHolder, position, payloads);
            }
        }

        private void setAddDayButtonVisibility(RoutineWeek week, Button addDayButton) {
            if (week.getNumberOfDays() >= Variables.WORKOUT_MAX_NUMBER_OF_DAYS) {
                addDayButton.setVisibility(View.INVISIBLE);
            } else {
                addDayButton.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull WeekViewHolder weekViewHolder, int weekPosition) {
            RoutineWeek week = this.routine.getWeek(weekPosition);
            weekViewHolder.weekTitle.setText("Week " + (weekPosition + 1));
            Button addDayButton = weekViewHolder.addDayButton;
            setAddDayButtonVisibility(week, addDayButton);

            RecyclerView dayRecyclerView = weekViewHolder.dayRecyclerView;
            DaysAdapter daysAdapter = new DaysAdapter(weekPosition, week.getDays());
            LinearLayoutManager layoutManager = new LinearLayoutManager(weekViewHolder.dayRecyclerView.getContext(), LinearLayoutManager.HORIZONTAL, false);
            dayRecyclerView.setLayoutManager(layoutManager);
            dayRecyclerView.setAdapter(daysAdapter);

            if (weekScrollStates.containsKey(weekPosition)) {
                // maintain scroll once this view is re bound from the recycler pool
                layoutManager.onRestoreInstanceState(weekScrollStates.get(weekPosition));
            }

            addDayButton.setOnClickListener(v -> {
                if (week.getNumberOfDays() >= Variables.WORKOUT_MAX_NUMBER_OF_DAYS) {
                    // a little paranoid, but once was able to click fast enough to get past max days
                    return;
                }

                this.routine.appendEmptyDay(weekPosition);
                setAddDayButtonVisibility(week, addDayButton);
                daysAdapter.notifyItemInserted(week.getNumberOfDays());

                setAddDayButtonVisibility(week, addDayButton);

                // scroll to end when new day is added
                dayRecyclerView.post(() -> dayRecyclerView.scrollToPosition(daysAdapter.getItemCount() - 1));
            });

            // set up more details for this week
            final PopupMenu dropDownWeekMenu = new PopupMenu(getContext(), weekViewHolder.weekMoreButton);
            Menu weekMenu = dropDownWeekMenu.getMenu();
            final int deleteWeekId = 0;
            final int copyAsNewWeekId = 1;
            final int copyToExistingWeekId = 2;
            weekMenu.add(0, copyAsNewWeekId, 0, "Copy As New Week");
            weekMenu.add(0, copyToExistingWeekId, 0, "Copy To Existing Week");
            weekMenu.add(0, deleteWeekId, 0, "Delete Week");

            dropDownWeekMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case deleteWeekId:
                        promptDeleteWeek(weekPosition);
                        return true;
                    case copyAsNewWeekId:
                        if (this.routine.getNumberOfWeeks() >= Variables.MAX_NUMBER_OF_WEEKS) {
                            Toast.makeText(getContext(), "Copy would exceed maximum number of weeks allowed in workout.", Toast.LENGTH_LONG).show();
                            return true;
                        }
                        copyWeekAsNew(weekPosition);
                        return true;
                    case copyToExistingWeekId:
                        promptCopyToExistingWeek(weekPosition);
                        return true;
                }
                return false;
            });
            weekViewHolder.weekMoreButton.setOnClickListener(v -> dropDownWeekMenu.show());
        }

        @Override
        public int getItemCount() {
            return this.routine.getNumberOfWeeks();
        }
    }

    private class DaysAdapter extends RecyclerView.Adapter<DaysAdapter.DayViewHolder> {

        class DayViewHolder extends RecyclerView.ViewHolder {

            private final TextView dayTitleTV;
            private final TextView exerciseCountTV;
            private final TextView dayTagTV;
            private final RelativeLayout dayCard;

            DayViewHolder(View itemView) {
                super(itemView);
                dayTitleTV = itemView.findViewById(R.id.day_title_tv);
                dayCard = itemView.findViewById(R.id.day_card);
                dayTagTV = itemView.findViewById(R.id.day_tag_tv);
                exerciseCountTV = itemView.findViewById(R.id.exercise_count_tv);
            }
        }

        private final int weekPosition;
        private final List<RoutineDay> days;

        DaysAdapter(int weekPosition, List<RoutineDay> days) {
            this.weekPosition = weekPosition;
            this.days = days;
        }

        @NonNull
        @Override
        public DayViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.routine_day_card, viewGroup, false);
            return new DayViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DayViewHolder dayViewHolder, int dayPosition) {
            RoutineDay day = days.get(dayPosition);
            String dayText = "Day " + (dayPosition + 1);
            TextView exerciseCountTV = dayViewHolder.exerciseCountTV;
            TextView dayTagTV = dayViewHolder.dayTagTV;
            exerciseCountTV.setText(Integer.toString(day.getNumberOfExercises()));
            dayViewHolder.dayTitleTV.setText(dayText);
            dayViewHolder.dayCard.setOnClickListener(v -> switchToRoutineDayView(weekPosition, dayPosition));

            if (day.getTag() != null) {
                dayTagTV.setText(day.getTag() + " "); // android cuts off italics on wrap content without trailing whitespace
            }
        }

        @Override
        public int getItemCount() {
            return days.size();
        }
    }
    //endregion
}

