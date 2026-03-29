package com.joshrap.liteweight.fragments;

import static android.os.Looper.getMainLooper;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.activities.MainActivity;
import com.joshrap.liteweight.adapters.CustomSortAdapter;
import com.joshrap.liteweight.adapters.RoutineDayAdapter;
import com.joshrap.liteweight.fragments.dialogs.PickExercisesDialog;
import com.joshrap.liteweight.fragments.dialogs.ReplaceExerciseDialog;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.interfaces.DraggableViewHolder;
import com.joshrap.liteweight.interfaces.FragmentWithDialog;
import com.joshrap.liteweight.managers.CurrentUserModule;
import com.joshrap.liteweight.managers.SelfManager;
import com.joshrap.liteweight.managers.WorkoutManager;
import com.joshrap.liteweight.models.Result;
import com.joshrap.liteweight.models.UserAndWorkout;
import com.joshrap.liteweight.models.user.OwnedExercise;
import com.joshrap.liteweight.models.user.User;
import com.joshrap.liteweight.models.user.WorkoutInfo;
import com.joshrap.liteweight.models.workout.Routine;
import com.joshrap.liteweight.models.workout.RoutineDay;
import com.joshrap.liteweight.models.workout.RoutineExercise;
import com.joshrap.liteweight.models.workout.RoutineWeek;
import com.joshrap.liteweight.models.workout.Workout;
import com.joshrap.liteweight.utils.AndroidUtils;
import com.joshrap.liteweight.utils.ValidatorUtils;
import com.joshrap.liteweight.utils.WorkoutUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.inject.Inject;

@SuppressLint("NotifyDataSetChanged")
public class PendingWorkoutFragment extends Fragment implements FragmentWithDialog {

    private RecyclerView weeksRecyclerView, routineDayRecyclerView;
    private AlertDialog alertDialog;
    private PickExercisesDialog pickExercisesDialog;
    private ReplaceExerciseDialog replaceExerciseDialog;
    private TextView routineDayTitleTV, emptyDayTV, routineDayTagTV, rearrangeHintTV;
    private int currentWeekIndex, currentDayIndex;
    private Map<String, String> exerciseIdToName;
    private ImageButton sortExercisesButton, routineDayMoreIcon;
    private Routine pendingRoutine;
    private boolean isRoutineDayViewShown, isSortingExercises, isRearranging, isExistingWorkout, firstWorkout, isMetricUnits;
    private OnBackPressedCallback backPressedCallback;
    private ConstraintLayout routineDayView, routineView;
    private Button addWeekButton, saveWorkoutButton, rearrangeButton, doneRearrangingButton;
    private ExtendedFloatingActionButton saveCustomSortButton, addExercisesButton;
    private WeekAdapter weekAdapter;
    private RoutineDayAdapter routineDayAdapter;
    private Workout originalWorkout; // used to determine if workout changed
    private Map<String, Double> exerciseIdToCurrentMaxWeight; // shortcut for first workout being created - prevents user from constantly having to change from 0lb

    @Inject
    AlertDialog loadingDialog;
    @Inject
    WorkoutManager workoutManager;
    @Inject
    SelfManager selfManager;
    @Inject
    CurrentUserModule currentUserModule;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentActivity activity = requireActivity();
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        Injector.getInjector(getContext()).inject(this);
        ((MainActivity) activity).toggleBackButton(true);

        if (this.getArguments() != null) {
            isExistingWorkout = this.getArguments().getBoolean(Variables.EXISTING_WORKOUT);
        }

        currentDayIndex = 0;
        currentWeekIndex = 0;
        User user = currentUserModule.getUser();
        isMetricUnits = user.getSettings().isMetricUnits();

        if (isExistingWorkout) {
            originalWorkout = new Workout(currentUserModule.getCurrentWorkout());
            pendingRoutine = new Routine(originalWorkout.getRoutine());
        } else {
            pendingRoutine = Routine.emptyRoutine();
            firstWorkout = !currentUserModule.isWorkoutPresent();
        }

        setToolbarTitle();

        exerciseIdToName = user.getExercises().stream().collect(Collectors.toMap(OwnedExercise::getId, OwnedExercise::getName));
        exerciseIdToCurrentMaxWeight = user.getExercises().stream().collect(Collectors.toMap(OwnedExercise::getId, OwnedExercise::getDefaultWeight));

        return inflater.inflate(R.layout.fragment_pending_workout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FragmentActivity activity = requireActivity();

        routineDayView = view.findViewById(R.id.routine_day_layout);
        routineView = view.findViewById(R.id.routine_week_layout);

        //region Views for routine day
        routineDayRecyclerView = view.findViewById(R.id.exercises_recycler_view);
        emptyDayTV = view.findViewById(R.id.empty_view_tv);
        routineDayTitleTV = view.findViewById(R.id.day_title_tv);
        routineDayTagTV = view.findViewById(R.id.day_tag_tv);

        saveCustomSortButton = view.findViewById(R.id.done_sorting_fab);
        saveCustomSortButton.setOnClickListener(v -> finishExerciseCustomSortMode());

        // set up sorting options
        sortExercisesButton = view.findViewById(R.id.sort_icon_button);
        final PopupMenu dropDownSortMenu = getSortMenu();
        sortExercisesButton.setOnClickListener(v -> dropDownSortMenu.show());

        // set up more details for day
        routineDayMoreIcon = view.findViewById(R.id.day_more_icon_btn);
        final PopupMenu dropDownRoutineDayMenu = getDayPopupMenu();
        routineDayMoreIcon.setOnClickListener(v -> {
            ((MainActivity) activity).hideKeyboard();
            dropDownRoutineDayMenu.show();
        });

        addExercisesButton = view.findViewById(R.id.add_exercises_fab);
        addExercisesButton.setOnClickListener(v -> {
            ((MainActivity) activity).hideKeyboard();
            popupSelectExercises();
        });
        //endregion

        //region Views for routine
        weeksRecyclerView = view.findViewById(R.id.week_recycler_view);
        setWeekAdapter();

        addWeekButton = view.findViewById(R.id.add_week_btn);
        addWeekButton.setOnClickListener(v -> {
            if (pendingRoutine.totalWeeks() >= Variables.MAX_NUMBER_OF_WEEKS) {
                // otherwise user can bypass by clicking quickly
                return;
            }
            pendingRoutine.addEmptyWeek();
            weekAdapter.notifyItemInserted(pendingRoutine.totalWeeks() - 1);
            if (pendingRoutine.totalWeeks() >= Variables.MAX_NUMBER_OF_WEEKS) {
                addWeekButton.setVisibility(View.GONE);
            }

            // scroll to end when new week is added
            weeksRecyclerView.post(() -> weeksRecyclerView.scrollToPosition(weekAdapter.getItemCount() - 1));
        });

        rearrangeButton = view.findViewById(R.id.rearrange_btn);
        rearrangeButton.setOnClickListener(v -> enableRearrangeMode());
        rearrangeHintTV = view.findViewById(R.id.rearrange_hint_tv);

        doneRearrangingButton = view.findViewById(R.id.done_rearranging_btn);
        doneRearrangingButton.setOnClickListener(v -> finishRearrangeMode());

        saveWorkoutButton = view.findViewById(R.id.save_btn);
        if (!isExistingWorkout) {
            saveWorkoutButton.setText(R.string.create_workout);
        }
        saveWorkoutButton.setOnClickListener(v -> {
            if (isExistingWorkout) {
                saveRoutine();
            } else {
                promptCreate();
            }

        });
        //endregion

        backPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isSortingExercises) {
                    finishExerciseCustomSortMode();
                } else if (isRearranging) {
                    finishRearrangeMode();
                } else if (isRoutineDayViewShown) {
                    ((MainActivity) activity).hideKeyboard();
                    switchToRoutineView();
                } else if (isRoutineModified()) {
                    hideAllDialogs(); // since user could spam back button and cause multiple ones to show
                    alertDialog = new AlertDialog.Builder(requireContext())
                            .setTitle("Unsaved Changes")
                            .setMessage(R.string.unsaved_changes_msg)
                            .setPositiveButton("Leave", (dialog, which) -> {
                                remove();
                                activity.getOnBackPressedDispatcher().onBackPressed();
                            })
                            .setNegativeButton("Stay", null)
                            .create();
                    alertDialog.show();
                } else {
                    remove();
                    activity.getOnBackPressedDispatcher().onBackPressed();
                }
            }
        };

        if (this.getArguments() != null && this.getArguments().containsKey(Variables.CURRENT_WEEK) && this.getArguments().containsKey(Variables.CURRENT_DAY)) {
            isExistingWorkout = this.getArguments().getBoolean(Variables.EXISTING_WORKOUT);
            int currentWeek = this.getArguments().getInt(Variables.CURRENT_WEEK, 0);
            int currentDay = this.getArguments().getInt(Variables.CURRENT_DAY, 0);
            switchToRoutineDayView(currentWeek, currentDay);
        }

        super.onViewCreated(view, savedInstanceState);
    }

    private @NonNull PopupMenu getSortMenu() {
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
                    enableExerciseCustomSortMode();
                    return true;
            }
            return false;
        });
        return dropDownSortMenu;
    }

    private PopupMenu getDayPopupMenu() {
        final PopupMenu dropDownRoutineDayMenu = new PopupMenu(getContext(), routineDayMoreIcon);
        Menu routineDayMenu = dropDownRoutineDayMenu.getMenu();
        final int deleteDayId = 0;
        final int copyDayToWeekId = 1;
        final int copyDayToExistingId = 2;
        final int setDayTagId = 3;
        final int moveDayId = 4;
        routineDayMenu.add(0, copyDayToExistingId, 0, "Copy To Day");
        routineDayMenu.add(0, copyDayToWeekId, 0, "Copy To Week");
        routineDayMenu.add(0, deleteDayId, 0, "Delete Day");
        routineDayMenu.add(0, moveDayId, 0, "Move To Week");
        routineDayMenu.add(0, setDayTagId, 0, "Set Tag");

        dropDownRoutineDayMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case deleteDayId:
                    promptDeleteDay(currentWeekIndex, currentDayIndex);
                    return true;
                case copyDayToExistingId:
                    promptCopyToExistingDay(currentWeekIndex, currentDayIndex);
                    return true;
                case copyDayToWeekId:
                    copyDayToWeek(currentWeekIndex, currentDayIndex);
                    return true;
                case setDayTagId:
                    promptSetDayTag(currentWeekIndex, currentDayIndex);
                    return true;
                case moveDayId:
                    if (pendingRoutine.get(currentWeekIndex).totalNumberOfDays() <= 1) {
                        Toast.makeText(getContext(), "Cannot move only day from week.", Toast.LENGTH_LONG).show();
                        return true;
                    }
                    promptMoveDay(currentWeekIndex, currentDayIndex);
                    return true;
            }
            return false;
        });
        return dropDownRoutineDayMenu;
    }

    private void addBackPressedCallback() {
        requireActivity().getOnBackPressedDispatcher().addCallback(backPressedCallback);
    }

    private void setWeekAdapter() {
        LinearLayoutManager weekLayoutManager = new LinearLayoutManager(getActivity());
        weekAdapter = new WeekAdapter(pendingRoutine);
        weeksRecyclerView.setAdapter(weekAdapter);
        weeksRecyclerView.setLayoutManager(weekLayoutManager);
    }

    private void setToolbarTitle() {
        ((MainActivity) requireActivity()).updateToolbarTitle(isExistingWorkout
                ? originalWorkout.getName()
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
        if (replaceExerciseDialog != null && replaceExerciseDialog.isVisible()) {
            replaceExerciseDialog.dismiss();
        }
        if (pickExercisesDialog != null && pickExercisesDialog.isVisible()) {
            pickExercisesDialog.dismiss();
        }
    }

    private void switchToRoutineDayView(int week, int day) {
        isRoutineDayViewShown = true;
        routineDayView.setVisibility(View.VISIBLE);
        routineView.setVisibility(View.GONE);
        ((MainActivity) requireActivity()).updateToolbarTitle(getString(R.string.day_details));

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
        weekAdapter.notifyItemChanged(currentWeekIndex, WeekAdapter.PAYLOAD_UPDATE_DAYS);
    }

    private boolean isRoutineModified() {
        if (isExistingWorkout) {
            return Routine.routinesDifferent(pendingRoutine, originalWorkout.getRoutine());
        }

        if (pendingRoutine.totalDays() > 1) {
            return true;
        }

        // essentially routine is only not modified for new workout if the first day has not been modified
        RoutineDay firstDay = pendingRoutine.get(0, 0);
        return !firstDay.getExercises().isEmpty() || firstDay.getTag() != null;
    }

    private void onExerciseClicked(int index, String exerciseId) {
        replaceExerciseDialog = new ReplaceExerciseDialog.Builder()
                .title("Set Exercise")
                .initialExerciseId(exerciseId)
                .callbacks(new ReplaceExerciseDialog.Callbacks() {
                    @Override
                    public void submit(OwnedExercise ownedExercise) {
                        replaceExercise(exerciseId, index, ownedExercise);
                    }

                    @Override
                    public void exerciseCreated(OwnedExercise exercise) {
                        onExerciseCreated(exercise);
                    }
                })
                .build();
        replaceExerciseDialog.show(getChildFragmentManager(), "replace-exercise");
    }

    private void replaceExercise(String originalExerciseId, int index, OwnedExercise ownedExercise) {
        if (originalExerciseId.equals(ownedExercise.getId())) {
            return;
        }

        RoutineExercise exercise = new RoutineExercise(ownedExercise, ownedExercise.getId());
        pendingRoutine.replaceExercise(currentWeekIndex, currentDayIndex, index, exercise);

        // shortcut for first users so their exercises don't all just have 0 for default weight even after creating a workout
        if (firstWorkout && exercise.getWeight() == 0 && exerciseIdToCurrentMaxWeight.containsKey(ownedExercise.getId())) {
            exercise.setWeight(exerciseIdToCurrentMaxWeight.get(ownedExercise.getId()));
        }
        routineDayAdapter.replaceExercise(index, pendingRoutine);
    }

    private void updateRoutineDayExerciseList() {
        routineDayAdapter = new RoutineDayAdapter(exerciseIdToName, exerciseIdToCurrentMaxWeight, pendingRoutine, currentWeekIndex, currentDayIndex, isMetricUnits, getActivity(), this::onExerciseClicked);
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
        RoutineDay day = pendingRoutine.get(weekIndex, dayIndex);
        routineDayTagTV.setVisibility(day.getTag() == null ? View.INVISIBLE : View.VISIBLE);
        routineDayTagTV.setText(day.getTag());
    }

    private void checkEmptyView() {
        emptyDayTV.setVisibility(pendingRoutine.exerciseListForDay(currentWeekIndex, currentDayIndex).isEmpty()
                ? View.VISIBLE : View.GONE);
    }

    private void enableExerciseCustomSortMode() {
        isSortingExercises = true;
        addExercisesButton.hide();
        saveCustomSortButton.show();
        sortExercisesButton.setVisibility(View.INVISIBLE);
        routineDayMoreIcon.setVisibility(View.INVISIBLE);

        CustomSortAdapter routineAdapter = new CustomSortAdapter(pendingRoutine.exerciseListForDay(currentWeekIndex, currentDayIndex), exerciseIdToName, false);
        customExerciseSortDispatcher.attachToRecyclerView(routineDayRecyclerView);
        routineDayRecyclerView.setAdapter(routineAdapter);
    }

    private void finishExerciseCustomSortMode() {
        isSortingExercises = false;
        saveCustomSortButton.hide();
        sortExercisesButton.setVisibility(View.VISIBLE);
        routineDayMoreIcon.setVisibility(View.VISIBLE);

        updateRoutineDayExerciseList();
        addExercisesButton.show();
        customExerciseSortDispatcher.attachToRecyclerView(null);
    }

    private void enableRearrangeMode() {
        isRearranging = true;
        saveWorkoutButton.setVisibility(View.GONE);
        addWeekButton.setVisibility(View.INVISIBLE);
        rearrangeButton.setVisibility(View.INVISIBLE);

        doneRearrangingButton.setVisibility(View.VISIBLE);
        rearrangeHintTV.setVisibility(View.VISIBLE);

        dragWeekDispatcher.attachToRecyclerView(weeksRecyclerView);
        weekAdapter.notifyDataSetChanged();
    }

    private void finishRearrangeMode() {
        isRearranging = false;
        saveWorkoutButton.setVisibility(View.VISIBLE);
        if (pendingRoutine.totalWeeks() < Variables.MAX_NUMBER_OF_WEEKS) {
            addWeekButton.setVisibility(View.VISIBLE);
        }
        rearrangeButton.setVisibility(View.VISIBLE);

        rearrangeHintTV.setVisibility(View.GONE);
        doneRearrangingButton.setVisibility(View.GONE);

        weekAdapter.clearItemTouchHelperMap();
        weekAdapter.notifyDataSetChanged();
        dragWeekDispatcher.attachToRecyclerView(null);
    }

    private final ItemTouchHelper customExerciseSortDispatcher = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder dragged, @NonNull RecyclerView.ViewHolder target) {
            int fromPosition = dragged.getBindingAdapterPosition();
            int toPosition = target.getBindingAdapterPosition();
            pendingRoutine.swapExerciseOrder(currentWeekIndex, currentDayIndex, fromPosition, toPosition);
            recyclerView.getAdapter().notifyItemMoved(fromPosition, toPosition);
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        }

        @Override
        public void onSelectedChanged(@Nullable @org.jetbrains.annotations.Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
            super.onSelectedChanged(viewHolder, actionState);

            if (viewHolder instanceof DraggableViewHolder) {
                DraggableViewHolder itemViewHolder = (DraggableViewHolder) viewHolder;
                itemViewHolder.onItemSelected();
            }
        }

        @Override
        public int interpolateOutOfBoundsScroll(@NonNull RecyclerView recyclerView, int viewSize, int viewSizeOutOfBounds, int totalSize, long msSinceStartScroll) {
            // allows for dragging speed to start off faster when dragging outside bounds of list

            final int direction = (int) Math.signum(viewSizeOutOfBounds);
            if (msSinceStartScroll <= 800) {
                // allow for smooth scroll at first to not be as jarring
                return 2 * direction;
            } else {
                return 15 * direction;
            }
        }

        @Override
        public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);

            if (viewHolder instanceof DraggableViewHolder) {
                DraggableViewHolder itemViewHolder = (DraggableViewHolder) viewHolder;
                itemViewHolder.onItemCleared();
            }
        }
    });

    private final ItemTouchHelper dragWeekDispatcher = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder dragged, @NonNull RecyclerView.ViewHolder target) {
            int fromPosition = dragged.getBindingAdapterPosition();
            int toPosition = target.getBindingAdapterPosition();
            pendingRoutine.swapWeeksOrder(fromPosition, toPosition);
            recyclerView.getAdapter().notifyItemMoved(fromPosition, toPosition); // this causes the animation of weeks being pushed over
            return true;
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            // last flag is important to prevent unwanted highlight when a day is being dragged. kind of a hack but couldn't find a better solution
            if (viewHolder instanceof DraggableViewHolder && actionState == ItemTouchHelper.ACTION_STATE_DRAG && isCurrentlyActive) {
                DraggableViewHolder itemViewHolder = (DraggableViewHolder) viewHolder;
                itemViewHolder.onItemSelected();
            }
        }

        @Override
        public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            if (viewHolder instanceof DraggableViewHolder) {
                DraggableViewHolder itemViewHolder = (DraggableViewHolder) viewHolder;
                itemViewHolder.onItemCleared();
            }
            recyclerView.getAdapter().notifyItemRangeChanged(0, pendingRoutine.totalWeeks(), WeekAdapter.PAYLOAD_UPDATE_ONLY_WEEK_LABEL); // ensure week numbers are updated
        }

        @Override
        public int interpolateOutOfBoundsScroll(@NonNull RecyclerView recyclerView, int viewSize, int viewSizeOutOfBounds, int totalSize, long msSinceStartScroll) {
            // allows for dragging speed to start off faster when dragging outside bounds of list
            final int direction = (int) Math.signum(viewSizeOutOfBounds);
            if (msSinceStartScroll <= 800) {
                // allow for smooth scroll at first to not be as jarring
                return 5 * direction;
            } else {
                return 15 * direction;
            }
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        }
    });

    private void promptDeleteWeek(int weekIndex) {
        alertDialog = new AlertDialog.Builder(requireContext())
                .setTitle("Delete Week " + (weekIndex + 1))
                .setMessage(R.string.remove_week_warning_msg)
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (pendingRoutine.totalWeeks() > 1) {
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
        addWeekButton.setVisibility(View.VISIBLE);
    }

    private void promptSetDayTag(final int weekIndex, final int dayIndex) {
        View popupView = getLayoutInflater().inflate(R.layout.popup_set_routine_day_tag, null);
        EditText dayTagInput = popupView.findViewById(R.id.day_tag_input);
        TextInputLayout dayTagInputLayout = popupView.findViewById(R.id.day_tag_input_layout);
        dayTagInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_DAY_TAG_LENGTH)});
        dayTagInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(dayTagInputLayout));

        RoutineDay currentDay = pendingRoutine.get(weekIndex, dayIndex);
        dayTagInput.setText(currentDay.getTag());

        alertDialog = new AlertDialog.Builder(requireContext())
                .setTitle("Set Day Tag")
                .setView(popupView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String dayTag = dayTagInput.getText().toString().trim();
                    currentDay.setTag(dayTag);
                    if (isRoutineDayViewShown) {
                        setRoutineDayTagTV(weekIndex, dayIndex);
                    } else {
                        weekAdapter.notifyItemChanged(weekIndex, WeekAdapter.PAYLOAD_UPDATE_DAYS);
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        alertDialog.show();
    }

    private void promptMoveDay(final int weekIndex, final int dayIndex) {
        View popupView = getLayoutInflater().inflate(R.layout.popup_move_routine_day, null);

        int totalWeeks = pendingRoutine.totalWeeks();

        String[] weekDisplays = new String[totalWeeks];
        for (int i = 0; i < totalWeeks; i++) {
            weekDisplays[i] = String.format(Locale.US, "Week %d", i + 1);
        }
        NumberPicker weekPicker = popupView.findViewById(R.id.week_picker);
        weekPicker.setMinValue(0);
        weekPicker.setMaxValue(totalWeeks - 1);
        weekPicker.setValue(weekIndex);
        weekPicker.setWrapSelectorWheel(false);
        weekPicker.setDisplayedValues(weekDisplays);

        alertDialog = new AlertDialog.Builder(requireContext())
                .setTitle("Move " + WorkoutUtils.generateDayTitle(weekIndex, dayIndex))
                .setView(popupView)
                .setPositiveButton("Move", (dialog, which) -> {
                    int targetWeekIndex = weekPicker.getValue();
                    if (targetWeekIndex == weekIndex) {
                        Toast.makeText(getContext(), "Day is already in that week.", Toast.LENGTH_LONG).show();
                    } else if (pendingRoutine.get(targetWeekIndex).totalNumberOfDays() >= Variables.WORKOUT_MAX_NUMBER_OF_DAYS) {
                        Toast.makeText(getContext(), "That week is full.", Toast.LENGTH_LONG).show();
                    } else {
                        RoutineDay currentDay = pendingRoutine.get(weekIndex, dayIndex);
                        int targetDayIndex = pendingRoutine.get(targetWeekIndex).totalNumberOfDays();
                        pendingRoutine.get(targetWeekIndex).addDay(currentDay);
                        pendingRoutine.get(weekIndex).removeDay(currentDay);

                        weekAdapter.notifyItemChanged(weekIndex, WeekAdapter.PAYLOAD_UPDATE_DAYS);
                        weekAdapter.notifyItemChanged(targetWeekIndex, WeekAdapter.PAYLOAD_UPDATE_DAYS);

                        currentWeekIndex = targetWeekIndex;
                        currentDayIndex = targetDayIndex;

                        updateRoutineDayExerciseList();
                        alertDialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        alertDialog.show();
    }

    private void promptDeleteDay(int weekIndex, int dayIndex) {
        alertDialog = new AlertDialog.Builder(requireContext())
                .setTitle("Delete " + (WorkoutUtils.generateDayTitle(weekIndex, dayIndex)))
                .setMessage(R.string.remove_day_warning_msg)
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (pendingRoutine.get(weekIndex).totalNumberOfDays() > 1) {
                        deleteDay(weekIndex, dayIndex);
                    } else {
                        Toast.makeText(getContext(), "Cannot delete only day from week.", Toast.LENGTH_LONG).show();
                    }
                    alertDialog.dismiss();
                })
                .setNegativeButton("No", null)
                .create();
        alertDialog.show();
    }

    private void deleteDay(final int weekIndex, final int dayIndex) {
        pendingRoutine.deleteDay(weekIndex, dayIndex);
        if (isRoutineDayViewShown) {
            switchToRoutineView();
        } else {
            weekAdapter.notifyItemChanged(weekIndex, WeekAdapter.PAYLOAD_UPDATE_DAYS);
        }
    }

    private void promptCopyToExistingDay(final int weekIndex, final int dayIndex) {
        View popupView = getLayoutInflater().inflate(R.layout.popup_copy_day_to_existing, null);
        List<String> dayLabels = new ArrayList<>();
        for (int weekPosition = 0; weekPosition < pendingRoutine.totalWeeks(); weekPosition++) {
            RoutineWeek week = pendingRoutine.get(weekPosition);
            for (int dayPosition = 0; dayPosition < week.totalNumberOfDays(); dayPosition++) {
                String dayTitle = WorkoutUtils.generateDayTitle(weekPosition, dayPosition);
                dayLabels.add(dayTitle);
            }
        }
        String[] dayLabelsArray = new String[pendingRoutine.totalDays()];
        dayLabels.toArray(dayLabelsArray);

        NumberPicker dayPicker = popupView.findViewById(R.id.day_picker);
        dayPicker.setMinValue(0);
        dayPicker.setMaxValue(pendingRoutine.totalDays() - 1);
        dayPicker.setValue(0);
        dayPicker.setWrapSelectorWheel(false);
        dayPicker.setDisplayedValues(dayLabelsArray);

        alertDialog = new AlertDialog.Builder(requireContext())
                .setTitle(String.format("Copy %s", WorkoutUtils.generateDayTitle(weekIndex, dayIndex)))
                .setView(popupView)
                .setPositiveButton("Copy", (dialog, which) -> {

                    final RoutineDay dayToBeCopied = pendingRoutine.get(weekIndex, dayIndex).clone();
                    int count = 0;
                    for (int weekPosition = 0; weekPosition < pendingRoutine.totalWeeks(); weekPosition++) {
                        RoutineWeek week = pendingRoutine.get(weekPosition);
                        for (int dayPosition = 0; dayPosition < week.totalNumberOfDays(); dayPosition++) {
                            if (count == dayPicker.getValue()) {
                                currentWeekIndex = weekPosition;
                                currentDayIndex = dayPosition;
                            }
                            count++;
                        }
                    }

                    pendingRoutine.putDay(currentWeekIndex, currentDayIndex, dayToBeCopied);

                    weekAdapter.notifyItemChanged(currentWeekIndex, WeekAdapter.PAYLOAD_UPDATE_DAYS);

                    updateRoutineDayExerciseList();
                    alertDialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .create();
        alertDialog.show();
    }

    private void copyDayToWeek(final int weekIndex, final int dayIndex) {
        View popupView = getLayoutInflater().inflate(R.layout.popup_copy_day_to_week, null);
        int totalWeeks = pendingRoutine.totalWeeks();

        String[] weekDisplays = new String[totalWeeks];
        for (int i = 0; i < totalWeeks; i++) {
            weekDisplays[i] = String.format(Locale.US, "Week %d", i + 1);
        }
        NumberPicker weekPicker = popupView.findViewById(R.id.week_picker);
        weekPicker.setMinValue(0);
        weekPicker.setMaxValue(totalWeeks - 1);
        weekPicker.setValue(weekIndex);
        weekPicker.setWrapSelectorWheel(false);
        weekPicker.setDisplayedValues(weekDisplays);

        alertDialog = new AlertDialog.Builder(requireContext())
                .setTitle(String.format("Copy %s", WorkoutUtils.generateDayTitle(weekIndex, dayIndex)))
                .setView(popupView)
                .setPositiveButton("Copy", (dialog, which) -> {
                    int targetWeek = weekPicker.getValue();

                    if (pendingRoutine.get(targetWeek).totalNumberOfDays() >= Variables.WORKOUT_MAX_NUMBER_OF_DAYS) {
                        alertDialog.dismiss();
                        Toast.makeText(getContext(), "Too many days in target week.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    currentWeekIndex = targetWeek;

                    final RoutineDay dayToBeCopied = pendingRoutine.get(weekIndex, dayIndex).clone();
                    currentDayIndex = pendingRoutine.get(currentWeekIndex).totalNumberOfDays();
                    pendingRoutine.appendDay(currentWeekIndex, dayToBeCopied);

                    weekAdapter.notifyItemChanged(weekIndex, WeekAdapter.PAYLOAD_UPDATE_DAYS);
                    weekAdapter.notifyItemChanged(currentWeekIndex, WeekAdapter.PAYLOAD_UPDATE_DAYS);

                    updateRoutineDayExerciseList();
                    alertDialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .create();
        alertDialog.show();
    }

    private void promptCopyToExistingWeek(int currentWeek) {
        View popupView = getLayoutInflater().inflate(R.layout.popup_copy_week_to_existing, null);
        int totalWeeks = pendingRoutine.totalWeeks();

        String[] weekDisplays = new String[totalWeeks];
        for (int i = 0; i < totalWeeks; i++) {
            weekDisplays[i] = String.format(Locale.US, "Week %d", i + 1);
        }
        NumberPicker weekPicker = popupView.findViewById(R.id.week_picker);
        weekPicker.setMinValue(0);
        weekPicker.setMaxValue(totalWeeks - 1);
        weekPicker.setValue(currentWeek);
        weekPicker.setWrapSelectorWheel(false);
        weekPicker.setDisplayedValues(weekDisplays);

        alertDialog = new AlertDialog.Builder(requireContext())
                .setTitle(String.format(Locale.US, "Copy Week %d", currentWeek + 1))
                .setView(popupView)
                .setPositiveButton("Copy", (dialog, which) -> {
                    int targetWeek = weekPicker.getValue();

                    final RoutineWeek weekToBeCopied = pendingRoutine.get(currentWeek);
                    pendingRoutine.putWeek(targetWeek, weekToBeCopied.clone());
                    weekAdapter.notifyItemChanged(targetWeek);

                    alertDialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .create();
        alertDialog.show();
    }

    private void copyWeekAsNew(int currentWeek) {
        RoutineWeek weekToBeCopied = pendingRoutine.get(currentWeek);
        pendingRoutine.addWeek(weekToBeCopied.clone());
        weekAdapter.notifyItemInserted(pendingRoutine.totalWeeks() - 1);
    }

    private void promptCreate() {
        View popupView = getLayoutInflater().inflate(R.layout.popup_save_workout, null);
        EditText workoutNameInput = popupView.findViewById(R.id.workout_name_input);
        TextInputLayout workoutNameInputLayout = popupView.findViewById(R.id.workout_name_input_layout);
        workoutNameInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(workoutNameInputLayout));
        workoutNameInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_WORKOUT_NAME)});

        alertDialog = new AlertDialog.Builder(requireContext())
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
                for (WorkoutInfo workoutInfo : currentUserModule.getUser().getWorkouts()) {
                    workoutNames.add(workoutInfo.getWorkoutName());
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
            Result<UserAndWorkout> result = this.workoutManager.createWorkout(pendingRoutine, workoutName);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                loadingDialog.dismiss();
                if (result.isSuccess()) {
                    isExistingWorkout = true;
                    originalWorkout = new Workout(result.getData().getWorkout());
                    pendingRoutine = new Routine(originalWorkout.getRoutine());

                    setToolbarTitle();
                    setWeekAdapter(); // since adapter holds old references to weeks
                    saveWorkoutButton.setText(R.string.save_workout);
                } else {
                    AndroidUtils.showErrorDialog(result.getErrorMessage(), getContext());
                }
            });
        });
    }

    private void saveRoutine() {
        AndroidUtils.showLoadingDialog(loadingDialog, "Saving...");
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Result<UserAndWorkout> result = this.workoutManager.setRoutine(originalWorkout.getId(), pendingRoutine);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                loadingDialog.dismiss();
                if (result.isSuccess()) {
                    originalWorkout = new Workout(result.getData().getWorkout());
                    pendingRoutine = new Routine(originalWorkout.getRoutine());

                    setWeekAdapter(); // since adapter holds old references to weeks
                    Toast.makeText(getContext(), "Workout saved.", Toast.LENGTH_LONG).show();
                } else {
                    AndroidUtils.showErrorDialog(result.getErrorMessage(), getContext());
                }
            });
        });
    }

    private void popupSelectExercises() {
        pickExercisesDialog = new PickExercisesDialog.Builder()
                .title("Select Exercises")
                .listener(new PickExercisesDialog.Listener() {
                    @Override
                    public void submit(List<OwnedExercise> pickedExercises) {
                        onExercisesPicked(pickedExercises);
                    }

                    @Override
                    public void exerciseCreated(OwnedExercise createdExercise) {
                        onExerciseCreated(createdExercise);
                    }
                })
                .build();
        pickExercisesDialog.show(getChildFragmentManager(), "select-exercises");
    }

    private void onExerciseCreated(OwnedExercise exercise) {
        exerciseIdToName.putIfAbsent(exercise.getId(), exercise.getName());
        exerciseIdToCurrentMaxWeight.putIfAbsent(exercise.getId(), exercise.getDefaultWeight());
    }

    private void onExercisesPicked(List<OwnedExercise> exercises) {
        if (exercises.isEmpty()) {
            return;
        }

        for (OwnedExercise pickedExercise : exercises) {
            if (!exerciseIdToName.containsKey(pickedExercise.getId())) {
                // this means the user created a new exercise while picking
                exerciseIdToName.putIfAbsent(pickedExercise.getId(), pickedExercise.getName());
                exerciseIdToCurrentMaxWeight.putIfAbsent(pickedExercise.getId(), pickedExercise.getDefaultWeight());
            }
            RoutineExercise exercise = new RoutineExercise(pickedExercise, pickedExercise.getId());
            pendingRoutine.addExercise(currentWeekIndex, currentDayIndex, exercise);

            // shortcut for first users so their exercises don't all just have 0 for default weight even after creating a workout
            if (firstWorkout && exercise.getWeight() == 0 && exerciseIdToCurrentMaxWeight.containsKey(pickedExercise.getId())) {
                exercise.setWeight(exerciseIdToCurrentMaxWeight.get(pickedExercise.getId()));
            }

            int newPosition = pendingRoutine.exerciseListForDay(currentWeekIndex, currentDayIndex).size() - 1;
            routineDayAdapter.addExercise(exercise);
            routineDayRecyclerView.scrollToPosition(newPosition);
        }
        routineDayAdapter.notifyDataSetChanged();
        int newPosition = pendingRoutine.exerciseListForDay(currentWeekIndex, currentDayIndex).size() - 1;
        routineDayRecyclerView.scrollToPosition(newPosition);
    }

    //region Classes/Adapters
    private class WeekAdapter extends RecyclerView.Adapter<WeekAdapter.WeekViewHolder> {

        class WeekViewHolder extends RecyclerView.ViewHolder implements DraggableViewHolder {

            private final TextView weekTitle;
            private final RecyclerView dayRecyclerView;
            private final Button addDayButton;
            private final ImageButton weekMoreButton;
            private final RelativeLayout rootLayout;

            WeekViewHolder(final View itemView) {
                super(itemView);

                weekTitle = itemView.findViewById(R.id.week_tv);
                dayRecyclerView = itemView.findViewById(R.id.day_recycler_view);
                addDayButton = itemView.findViewById(R.id.add_day_btn);
                weekMoreButton = itemView.findViewById(R.id.week_more_icon_btn);
                rootLayout = itemView.findViewById(R.id.week_card);
            }

            @Override
            public void onItemSelected() {
                rootLayout.setBackgroundResource(R.drawable.week_card_selected_background);
            }

            @Override
            public void onItemCleared() {
                rootLayout.setBackgroundResource(R.drawable.week_card_background);
            }
        }

        private final Routine routine;
        private final Map<Integer, Parcelable> weekScrollStates;
        private final Map<RecyclerView, ItemTouchHelper> recyclerViewItemTouchHelperMap;

        WeekAdapter(Routine routine) {
            this.routine = routine;
            weekScrollStates = new HashMap<>();
            recyclerViewItemTouchHelperMap = new HashMap<>();
        }

        @NonNull
        @Override
        public WeekViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.routine_week_card, viewGroup, false);
            return new WeekViewHolder(view);
        }

        @Override
        public void onViewRecycled(@NonNull WeekViewHolder holder) {
            // when week row is recycled, store the scroll state of its day list
            final int position = holder.getBindingAdapterPosition();
            if (holder.dayRecyclerView.getLayoutManager() != null) {
                Parcelable layoutState = holder.dayRecyclerView.getLayoutManager().onSaveInstanceState();
                weekScrollStates.put(position, layoutState);
            }

            if (recyclerViewItemTouchHelperMap.containsKey(holder.dayRecyclerView)) {
                // prevents memory leak happening when day recycler is recycled. the original dispatcher is still attached to old list once the viewHolder is created, causing weird graphical bugs
                ItemTouchHelper dispatcher = recyclerViewItemTouchHelperMap.get(holder.dayRecyclerView);
                if (dispatcher != null)
                    dispatcher.attachToRecyclerView(null);
            }

            super.onViewRecycled(holder);
        }

        public static final String PAYLOAD_UPDATE_ONLY_WEEK_LABEL = "UPDATE_ONLY_WEEK_LABEL";
        public static final String PAYLOAD_UPDATE_DAYS = "PAYLOAD_UPDATE";

        public void clearItemTouchHelperMap() {
            for (ItemTouchHelper view : recyclerViewItemTouchHelperMap.values()) {
                view.attachToRecyclerView(null);
            }
            recyclerViewItemTouchHelperMap.clear();
        }

        @Override
        public void onBindViewHolder(@NonNull WeekViewHolder weekViewHolder, int position, List<Object> payloads) {
            if (!payloads.isEmpty()) {
                final RoutineWeek week = this.routine.get(position);
                for (final Object payload : payloads) {
                    if (payload.equals(PAYLOAD_UPDATE_ONLY_WEEK_LABEL)) {
                        // very important to only update label. if day recycler view is refreshed then the day drag dispatcher won't work on it
                        setWeekLabel(weekViewHolder);
                    } else if (payload.equals(PAYLOAD_UPDATE_DAYS)) {
                        setWeekCardButtonsVisibility(week, weekViewHolder);
                        // need to update all because no clean way of knowing which one had its displayed data change
                        weekViewHolder.dayRecyclerView.getAdapter().notifyDataSetChanged();
                    }
                }
            } else {
                super.onBindViewHolder(weekViewHolder, position, payloads);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull WeekViewHolder weekViewHolder, int weekPosition) {
            // as a warning don't use weekPosition var since when dragging that variable can be outdated and can cause weird bugs
            final RoutineWeek week = this.routine.get(weekViewHolder.getBindingAdapterPosition());
            setWeekLabel(weekViewHolder);
            Button addDayButton = weekViewHolder.addDayButton;
            setWeekCardButtonsVisibility(week, weekViewHolder);

            // might be confusing to user if week card looks clickable but it does nothing in non rearrange mode
            weekViewHolder.rootLayout.setClickable(isRearranging);
            weekViewHolder.rootLayout.setFocusable(isRearranging);

            RecyclerView daysRecyclerView = weekViewHolder.dayRecyclerView;
            DaysAdapter daysAdapter = new DaysAdapter(week.getDays());
            LinearLayoutManager layoutManager = new LinearLayoutManager(weekViewHolder.dayRecyclerView.getContext(), LinearLayoutManager.HORIZONTAL, false);
            daysRecyclerView.setLayoutManager(layoutManager);
            daysRecyclerView.setAdapter(daysAdapter);

            if (isRearranging) {
                final ItemTouchHelper dragDayDispatcher = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, 0) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder dragged, @NonNull RecyclerView.ViewHolder target) {
                        int fromPosition = dragged.getBindingAdapterPosition();
                        int toPosition = target.getBindingAdapterPosition();
                        pendingRoutine.swapDaysOrder(weekViewHolder.getBindingAdapterPosition(), fromPosition, toPosition);
                        recyclerView.getAdapter().notifyItemMoved(fromPosition, toPosition); // this causes the animation of days being pushed over
                        return true;
                    }

                    @Override
                    public void onSelectedChanged(@Nullable @org.jetbrains.annotations.Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
                        super.onSelectedChanged(viewHolder, actionState);

                        if (viewHolder instanceof DraggableViewHolder) {
                            DraggableViewHolder itemViewHolder = (DraggableViewHolder) viewHolder;
                            itemViewHolder.onItemSelected();
                        }
                    }

                    @Override
                    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                        super.clearView(recyclerView, viewHolder);
                        if (viewHolder instanceof DraggableViewHolder) {
                            DraggableViewHolder itemViewHolder = (DraggableViewHolder) viewHolder;
                            itemViewHolder.onItemCleared();
                        }

                        recyclerView.getAdapter().notifyDataSetChanged(); // ensure day numbers are updated
                    }

                    @Override
                    public int interpolateOutOfBoundsScroll(@NonNull RecyclerView recyclerView, int viewSize, int viewSizeOutOfBounds, int totalSize, long msSinceStartScroll) {
                        // allows for dragging speed to start off faster when dragging outside bounds of list
                        final int direction = (int) Math.signum(viewSizeOutOfBounds);
                        if (msSinceStartScroll <= 800) {
                            // allow for smooth scroll at first to not be as jarring
                            return 5 * direction;
                        } else {
                            return 10 * direction;
                        }
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                    }
                });

                dragDayDispatcher.attachToRecyclerView(daysRecyclerView);
                recyclerViewItemTouchHelperMap.put(weekViewHolder.dayRecyclerView, dragDayDispatcher);
            }


            if (weekScrollStates.containsKey(weekViewHolder.getBindingAdapterPosition())) {
                // maintain scroll position once this view is re bound from the recycler pool
                layoutManager.onRestoreInstanceState(weekScrollStates.get(weekViewHolder.getBindingAdapterPosition()));
            }

            addDayButton.setOnClickListener(v -> {
                if (week.totalNumberOfDays() >= Variables.WORKOUT_MAX_NUMBER_OF_DAYS) {
                    // a little paranoid, but once was able to click fast enough to get past max days
                    return;
                }

                this.routine.appendEmptyDay(weekViewHolder.getBindingAdapterPosition());
                daysAdapter.notifyItemInserted(week.totalNumberOfDays());

                setWeekCardButtonsVisibility(week, weekViewHolder);

                // scroll to end when new day is added
                daysRecyclerView.post(() -> daysRecyclerView.scrollToPosition(daysAdapter.getItemCount() - 1));
            });

            // set up more details for this week
            final PopupMenu dropDownWeekMenu = getWeekPopupMenu(weekViewHolder);
            weekViewHolder.weekMoreButton.setOnClickListener(v -> dropDownWeekMenu.show());
        }

        private PopupMenu getWeekPopupMenu(@NonNull WeekViewHolder weekViewHolder) {
            final PopupMenu dropDownWeekMenu = new PopupMenu(getContext(), weekViewHolder.weekMoreButton);
            Menu weekMenu = dropDownWeekMenu.getMenu();
            final int deleteWeekId = 0;
            final int copyAsNewWeekId = 1;
            final int copyToExistingWeekId = 2;
            weekMenu.add(0, copyAsNewWeekId, 0, "Copy As New");
            weekMenu.add(0, copyToExistingWeekId, 0, "Copy To Week");
            weekMenu.add(0, deleteWeekId, 0, "Delete Week");

            dropDownWeekMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case deleteWeekId:
                        promptDeleteWeek(weekViewHolder.getBindingAdapterPosition());
                        return true;
                    case copyAsNewWeekId:
                        if (this.routine.totalWeeks() >= Variables.MAX_NUMBER_OF_WEEKS) {
                            Toast.makeText(getContext(), "Max weeks already reached.", Toast.LENGTH_LONG).show();
                            return true;
                        }
                        copyWeekAsNew(weekViewHolder.getBindingAdapterPosition());
                        return true;
                    case copyToExistingWeekId:
                        promptCopyToExistingWeek(weekViewHolder.getBindingAdapterPosition());
                        return true;
                }
                return false;
            });
            return dropDownWeekMenu;
        }

        private void setWeekCardButtonsVisibility(RoutineWeek week, WeekViewHolder weekViewHolder) {
            if (isRearranging) {
                weekViewHolder.addDayButton.setVisibility(View.INVISIBLE);
                weekViewHolder.weekMoreButton.setVisibility(View.INVISIBLE);
            } else {
                weekViewHolder.addDayButton.setVisibility(View.VISIBLE);
                weekViewHolder.weekMoreButton.setVisibility(View.VISIBLE);
            }

            if (week.totalNumberOfDays() >= Variables.WORKOUT_MAX_NUMBER_OF_DAYS) {
                weekViewHolder.addDayButton.setVisibility(View.INVISIBLE);
            }
        }

        private void setWeekLabel(@NonNull WeekViewHolder weekViewHolder) {
            weekViewHolder.weekTitle.setText(String.format(Locale.getDefault(), "Week %d", weekViewHolder.getBindingAdapterPosition() + 1));
        }

        @Override
        public int getItemCount() {
            return this.routine.totalWeeks();
        }
    }

    private class DaysAdapter extends RecyclerView.Adapter<DaysAdapter.DayViewHolder> {

        class DayViewHolder extends RecyclerView.ViewHolder implements DraggableViewHolder {

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

            @Override
            public void onItemSelected() {
                dayCard.setBackgroundResource(R.drawable.day_card_selected_background);
            }

            @Override
            public void onItemCleared() {
                dayCard.setBackgroundResource(R.drawable.day_card_background);
            }
        }

        private final List<RoutineDay> days;

        DaysAdapter(List<RoutineDay> days) {
            this.days = days;
        }

        @NonNull
        @Override
        public DayViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.routine_day_card, viewGroup, false);
            return new DayViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DayViewHolder dayViewHolder, int dayIndex) {
            final RoutineDay day = days.get(dayIndex);
            String dayText = "Day " + (dayIndex + 1);
            TextView exerciseCountTV = dayViewHolder.exerciseCountTV;
            TextView dayTagTV = dayViewHolder.dayTagTV;
            exerciseCountTV.setText(String.format(Locale.getDefault(), Integer.toString(day.totalNumberOfExercises())));
            dayViewHolder.dayTitleTV.setText(dayText);


            if (day.getTag() != null) {
                dayTagTV.setText(day.getTag());
            } else {
                dayTagTV.setText(null); // otherwise when recycled, days without tags may have a tag shown
            }

            PopupMenu dropDownRoutineDayMenu = getShortcutPopupMenu(dayViewHolder, day);

            if (isRearranging) {
                // disable listeners for dragging to prevent accidental clicks
                dayViewHolder.dayCard.setOnClickListener(null);
                dayViewHolder.dayCard.setOnLongClickListener(null);
            } else {
                dayViewHolder.dayCard.setOnClickListener(v -> {
                    int weekPosition = pendingRoutine.findWeekIndexOfDay(day);
                    int dayPosition = dayViewHolder.getBindingAdapterPosition();
                    if (weekPosition >= 0)
                        switchToRoutineDayView(weekPosition, dayPosition);
                });
                dayViewHolder.dayCard.setOnLongClickListener(view -> {
                    dropDownRoutineDayMenu.show();
                    return true;
                });
            }
        }

        private PopupMenu getShortcutPopupMenu(@NonNull DayViewHolder dayViewHolder, RoutineDay day) {
            PopupMenu dropDownRoutineDayMenu = new PopupMenu(getContext(), dayViewHolder.exerciseCountTV);
            Menu routineDayMenu = dropDownRoutineDayMenu.getMenu();

            final int deleteDayId = 0;
            final int copyDayToWeekId = 1;
            final int copyDayToExistingId = 2;
            final int setDayTagId = 3;
            final int moveDayId = 4;
            routineDayMenu.add(0, copyDayToExistingId, 0, "Copy To Day");
            routineDayMenu.add(0, copyDayToWeekId, 0, "Copy To Week");
            routineDayMenu.add(0, deleteDayId, 0, "Delete Day");
            routineDayMenu.add(0, moveDayId, 0, "Move To Week");
            routineDayMenu.add(0, setDayTagId, 0, "Set Tag");

            dropDownRoutineDayMenu.setOnMenuItemClickListener(item -> {
                int weekPosition = pendingRoutine.findWeekIndexOfDay(day);
                int dayPosition = dayViewHolder.getBindingAdapterPosition();
                switch (item.getItemId()) {
                    case deleteDayId:
                        promptDeleteDay(weekPosition, dayPosition);
                        return true;
                    case copyDayToExistingId:
                        promptCopyToExistingDay(weekPosition, dayPosition);
                        return true;
                    case copyDayToWeekId:
                        copyDayToWeek(weekPosition, dayPosition);
                        return true;
                    case setDayTagId:
                        promptSetDayTag(weekPosition, dayPosition);
                        return true;
                    case moveDayId:
                        if (pendingRoutine.get(weekPosition).totalNumberOfDays() <= 1) {
                            Toast.makeText(getContext(), "Cannot move only day from week.", Toast.LENGTH_LONG).show();
                            return true;
                        }
                        promptMoveDay(weekPosition, dayPosition);
                        return true;
                }
                return false;
            });
            return dropDownRoutineDayMenu;
        }

        @Override
        public int getItemCount() {
            return days.size();
        }
    }
    //endregion
}

