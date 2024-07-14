package com.joshrap.liteweight.fragments;

import static android.os.Looper.getMainLooper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.activities.MainActivity;
import com.joshrap.liteweight.adapters.CustomSortAdapter;
import com.joshrap.liteweight.adapters.FocusAdapter;
import com.joshrap.liteweight.adapters.RoutineDayAdapter;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.inject.Inject;

@SuppressLint("NotifyDataSetChanged")
public class PendingWorkoutFragment extends Fragment implements FragmentWithDialog {

    private RecyclerView weeksRecyclerView, routineDayRecyclerView, pickExerciseRecyclerView;
    private AlertDialog alertDialog, createExerciseDialog;
    private TextView routineDayTitleTV, emptyDayTV, routineDayTagTV, rearrangeHintTV;
    private String selectedFocus;
    private HashMap<String, List<OwnedExercise>> allOwnedExercises; // focus -> exercises
    private int currentWeekIndex, currentDayIndex;
    private Map<String, String> exerciseIdToName;
    private ImageButton sortExercisesButton, routineDayMoreIcon;
    private Routine pendingRoutine;
    private boolean isRoutineDayViewShown, isSortingExercises, isRearranging, isExistingWorkout, firstWorkout, isSearchingExercises, isMetricUnits;
    private OnBackPressedCallback backPressedCallback;
    private ConstraintLayout routineDayView, routineView;
    private Button addWeekButton, saveWorkoutButton, rearrangeButton, doneRearrangingButton;
    private ExtendedFloatingActionButton saveCustomSortButton, addExercisesButton;
    private AddExerciseAdapter addExerciseAdapter;
    private WeekAdapter weekAdapter;
    private RoutineDayAdapter routineDayAdapter;
    private Workout originalWorkout; // used to determine if workout changed
    private EditText searchExerciseInput;
    private Map<String, Double> exerciseIdToCurrentMaxWeight; // shortcut for first workout being created - prevents user from constantly having to change from 0lb

    private final String AllFocus = "All"; // bit of a hack for sure

    @Inject
    AlertDialog loadingDialog;
    @Inject
    WorkoutManager workoutManager;
    @Inject
    SelfManager selfManager;
    @Inject
    SharedPreferences sharedPreferences;
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
        allOwnedExercises = new HashMap<>();
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
            popupAddExercises();
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
                            .setMessage(R.string.unsaved_workout_msg)
                            .setPositiveButton("Yes", (dialog, which) -> {
                                remove();
                                activity.getOnBackPressedDispatcher().onBackPressed();
                            })
                            .setNegativeButton("No", null)
                            .create();
                    alertDialog.show();
                } else {
                    remove();
                    activity.getOnBackPressedDispatcher().onBackPressed();
                }
            }
        };

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
        if (createExerciseDialog != null && createExerciseDialog.isShowing()) {
            createExerciseDialog.dismiss();
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

    private void updateRoutineDayExerciseList() {
        routineDayAdapter = new RoutineDayAdapter(exerciseIdToName, exerciseIdToCurrentMaxWeight, pendingRoutine, currentWeekIndex, currentDayIndex, isMetricUnits, getActivity());
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
            int fromPosition = dragged.getAdapterPosition();
            int toPosition = target.getAdapterPosition();
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
            int fromPosition = dragged.getAdapterPosition();
            int toPosition = target.getAdapterPosition();
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

    /**
     * Shows a popup that lists all exercises for a given exercise focus and an ability to search all exercises.
     * Adds the exercises to a given day in the routine.
     */
    private void popupAddExercises() {
        View popupView = getLayoutInflater().inflate(R.layout.popup_pick_exercise, null);
        pickExerciseRecyclerView = popupView.findViewById(R.id.pick_exercises_recycler_view);
        Spinner focusSpinner = popupView.findViewById(R.id.focus_spinner);

        allOwnedExercises = new HashMap<>();
        List<String> focusList = new ArrayList<>(Variables.FOCUS_LIST);
        for (String focus : focusList) {
            allOwnedExercises.put(focus, new ArrayList<>());
        }

        for (OwnedExercise exercise : currentUserModule.getUser().getExercises()) {
            List<String> focusesOfExercise = exercise.getFocuses();
            for (String focus : focusesOfExercise) {
                if (!allOwnedExercises.containsKey(focus)) {
                    // focus somehow hasn't been added before
                    focusList.add(focus);
                    allOwnedExercises.put(focus, new ArrayList<>());
                }
                allOwnedExercises.get(focus).add(exercise);
            }
        }

        // albeit more verbose than SearchView, but this allows more granular control
        searchExerciseInput = popupView.findViewById(R.id.search_exercises_input);
        TextInputLayout searchExerciseInputLayout = popupView.findViewById(R.id.search_exercises_input_layout);
        ImageButton searchButton = popupView.findViewById(R.id.search_icon_button);

        searchButton.setOnClickListener(v -> {
            isSearchingExercises = !isSearchingExercises;
            if (isSearchingExercises) {
                searchButton.setImageResource(R.drawable.close_icon);

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

                focusSpinner.setVisibility(View.INVISIBLE);
                searchExerciseInputLayout.setVisibility(View.VISIBLE);
                searchExerciseInput.requestFocus();

                // android is so beautiful. Show keyboard after requesting focus
                InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(searchExerciseInput, 0);
            } else {
                // reset all search views
                searchExerciseInput.clearFocus();

                // can't use shared hide keyboard method since this is in an alertdialog apparently
                InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchExerciseInput.getWindowToken(), 0);

                searchButton.setImageResource(R.drawable.search_icon);

                focusSpinner.setVisibility(View.VISIBLE);
                searchExerciseInputLayout.setVisibility(View.INVISIBLE);
                updateExerciseChoices();
            }
        });

        searchExerciseInput.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchExerciseInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                addExerciseAdapter.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        focusList.sort(String.CASE_INSENSITIVE_ORDER);
        focusList.add(0, AllFocus);

        ArrayAdapter<String> focusAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, focusList);
        focusSpinner.setAdapter(focusAdapter);
        focusSpinner.setOnItemSelectedListener(new PendingWorkoutFragment.FocusSpinnerListener());
        // initially select first item from spinner, then always select the one the user last clicked. Note this auto calls the method to update exercises for this focus
        focusSpinner.setSelection((selectedFocus == null) ? 0 : focusList.indexOf(selectedFocus));

        alertDialog = new AlertDialog.Builder(requireContext())
                .setTitle("Add Exercises To Day")
                .setView(popupView)
                .setPositiveButton("Done", null)
                .setOnDismissListener(dialogInterface -> isSearchingExercises = false)
                .create();

        alertDialog.show();
    }

    /*
        Shortcut to create an exercise. An argument could be made the new exercise fragment should instead be reused, but in
        my opinion that would break the flow for creating a workout as it would take the user to a whole new page.
     */
    private void popupCreateExercise() {
        if (currentUserModule.getUser().getTotalExerciseCount() >= Variables.MAX_NUMBER_OF_EXERCISES) {
            Toast.makeText(getContext(), "You already have the maximum number of exercises allowed. To create more, delete some in the My Exercises page.", Toast.LENGTH_LONG).show();
            return;
        }

        View popupView = getLayoutInflater().inflate(R.layout.popup_create_exercise, null);

        EditText exerciseNameInput = popupView.findViewById(R.id.exercise_name_input);
        TextInputLayout exerciseNameLayout = popupView.findViewById(R.id.exercise_name_input_layout);
        TextView focusTV = popupView.findViewById(R.id.focus_tv);
        ProgressBar loadingBar = popupView.findViewById(R.id.loading_progress_bar);

        exerciseNameInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_EXERCISE_NAME)});
        exerciseNameInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(exerciseNameLayout));
        // nice little shortcut to not make the user type out a non-existent exercise they were looking for
        String defaultName = searchExerciseInput != null ? searchExerciseInput.getText().toString().trim() : "";
        exerciseNameInput.setText(defaultName);

        exerciseNameInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // hack as usual to get android to show keyboard when input is focused
                createExerciseDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
        });

        RecyclerView focusRecyclerView = popupView.findViewById(R.id.pick_focuses_recycler_view);

        List<String> focusList = new ArrayList<>(Variables.FOCUS_LIST);
        List<String> selectedFocuses = new ArrayList<>();
        FocusAdapter addFocusAdapter = new FocusAdapter(focusList, selectedFocuses, null);
        focusRecyclerView.setAdapter(addFocusAdapter);
        focusRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        createExerciseDialog = new AlertDialog.Builder(requireContext())
                .setTitle("Create New Exercise")
                .setView(popupView)
                .setPositiveButton("Create and Add", null)
                .setNegativeButton("Cancel", null)
                .create();
        createExerciseDialog.setOnShowListener(dialogInterface -> {
            alertDialog.dismiss();
            if (defaultName.isEmpty()) {
                // if not pre-filling name, bring focus to name input to save user a click
                exerciseNameInput.requestFocus();
            }
        });
        createExerciseDialog.show();

        createExerciseDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String nameError;
            boolean focusError = false;

            List<String> exerciseNames = new ArrayList<>();
            for (OwnedExercise exercise : currentUserModule.getUser().getExercises()) {
                exerciseNames.add(exercise.getName());
            }
            nameError = ValidatorUtils.validNewExerciseName(exerciseNameInput.getText().toString().trim(), exerciseNames);
            exerciseNameLayout.setError(nameError);

            if (selectedFocuses.isEmpty()) {
                focusError = true;
                focusTV.startAnimation(AndroidUtils.shakeError(4));
                Toast.makeText(getContext(), "Must select at least one focus.", Toast.LENGTH_LONG).show();
            }

            if (nameError == null && !focusError) {
                String exerciseName = exerciseNameInput.getText().toString().trim();
                createExerciseDialog.setCancelable(false);
                loadingBar.setVisibility(View.VISIBLE);

                Executor executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    Result<OwnedExercise> result = selfManager.newExercise(
                            exerciseName, selectedFocuses, Variables.DEFAULT_WEIGHT, Variables.DEFAULT_SETS, Variables.DEFAULT_REPS, "", "");
                    Handler handler = new Handler(getMainLooper());
                    handler.post(() -> {
                        loadingBar.setVisibility(View.GONE);
                        createExerciseDialog.setCancelable(true);
                        if (result.isSuccess()) {
                            OwnedExercise newExercise = result.getData();

                            exerciseIdToName.putIfAbsent(newExercise.getId(), newExercise.getName());
                            exerciseIdToCurrentMaxWeight.putIfAbsent(newExercise.getId(), newExercise.getDefaultWeight());
                            addOwnedExerciseToRoutine(newExercise);

                            createExerciseDialog.dismiss();
                        } else {
                            AndroidUtils.showErrorDialog(result.getErrorMessage(), getContext());
                        }
                    });
                });
            }
        });
    }

    private void addOwnedExerciseToRoutine(OwnedExercise ownedExercise) {
        RoutineExercise exercise = new RoutineExercise(ownedExercise, ownedExercise.getId());
        pendingRoutine.addExercise(currentWeekIndex, currentDayIndex, exercise);

        // shortcut for first users so their exercises don't all just have 0 for default weight even after creating a workout
        if (firstWorkout && exercise.getWeight() == 0 && exerciseIdToCurrentMaxWeight.containsKey(ownedExercise.getId())) {
            exercise.setWeight(exerciseIdToCurrentMaxWeight.get(ownedExercise.getId()));
        }

        int newPosition = pendingRoutine.exerciseListForDay(currentWeekIndex, currentDayIndex).size() - 1;
        // adapter uses list of separate models to maintain expanded state, need to add it there too
        routineDayAdapter.routineRowModels.add(new RoutineDayAdapter.RoutineRowModel(exercise, false));
        routineDayAdapter.notifyItemInserted(newPosition);
        routineDayRecyclerView.scrollToPosition(newPosition);
    }

    /**
     * Displays all the exercises associated with the currently selected focus.
     */
    private void updateExerciseChoices() {
        List<OwnedExercise> sortedExercises = new ArrayList<>();
        if (selectedFocus.equals(AllFocus)) {
            Set<OwnedExercise> ownedExercisesSet = new HashSet<>();
            for (String focus : allOwnedExercises.keySet()) {
                List<OwnedExercise> exercises = allOwnedExercises.get(focus);
                ownedExercisesSet.addAll(exercises);
            }
            sortedExercises.addAll(ownedExercisesSet);
        } else {
            sortedExercises.addAll(allOwnedExercises.get(selectedFocus));
        }
        Collections.sort(sortedExercises);
        addExerciseAdapter = new PendingWorkoutFragment.AddExerciseAdapter(sortedExercises);
        pickExerciseRecyclerView.setAdapter(addExerciseAdapter);
        pickExerciseRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    //region Classes/Adapters

    private class FocusSpinnerListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            selectedFocus = parent.getItemAtPosition(pos).toString();
            updateExerciseChoices(); // update choices for exercise based on this newly selected focus
        }

        public void onNothingSelected(AdapterView parent) {
        }
    }

    private class AddExerciseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {
        private static final int FOOTER_VIEW = 1;

        class AddExerciseViewHolder extends RecyclerView.ViewHolder {
            private final CheckBox exerciseCheckbox;

            AddExerciseViewHolder(View itemView) {
                super(itemView);
                exerciseCheckbox = itemView.findViewById(R.id.exercise_checkbox);
            }
        }

        class FooterViewHolder extends RecyclerView.ViewHolder {
            private final Button createExerciseBtn;

            FooterViewHolder(View itemView) {
                super(itemView);
                createExerciseBtn = itemView.findViewById(R.id.create_exercise_btn);
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
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            if (viewType == FOOTER_VIEW) {
                View exerciseView = inflater.inflate(R.layout.exercise_not_found_footer, parent, false);
                return new PendingWorkoutFragment.AddExerciseAdapter.FooterViewHolder(exerciseView);
            } else {
                View exerciseView = inflater.inflate(R.layout.row_add_exercise, parent, false);
                return new AddExerciseViewHolder(exerciseView);
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == displayList.size()) {
                return FOOTER_VIEW;
            }

            return super.getItemViewType(position);
        }

        @Override
        public int getItemCount() {
            if (displayList.isEmpty()) {
                // always want one item for the footer
                return 1;
            }
            return displayList.size() + 1;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof AddExerciseViewHolder) {
                AddExerciseViewHolder viewHolder = (AddExerciseViewHolder) holder;
                final OwnedExercise ownedExercise = displayList.get(position);
                CheckBox exerciseCheckbox = viewHolder.exerciseCheckbox;
                exerciseCheckbox.setText(ownedExercise.getName());
                // check if the exercise is already in this specific day
                boolean isChecked = pendingRoutine.exerciseListForDay(currentWeekIndex, currentDayIndex)
                        .stream().anyMatch(x -> x.getExerciseId().equals(ownedExercise.getId()));
                exerciseCheckbox.setChecked(isChecked);

                exerciseCheckbox.setOnClickListener(v -> {
                    if (exerciseCheckbox.isChecked()) {
                        addOwnedExerciseToRoutine(ownedExercise);
                    } else {
                        pendingRoutine.removeExercise(currentWeekIndex, currentDayIndex, ownedExercise.getId());
                        // adapter uses list of separate models to maintain expanded state, need to remove it there too
                        routineDayAdapter.routineRowModels.removeIf(x -> x.getRoutineExercise().getExerciseId().equals(ownedExercise.getId()));
                        // too much of a pain to get the index in that adapter that this exercise could have been removed from
                        routineDayAdapter.notifyDataSetChanged();
                    }
                });
            } else if (holder instanceof FooterViewHolder) {
                FooterViewHolder viewHolder = (FooterViewHolder) holder;
                viewHolder.createExerciseBtn.setOnClickListener(view -> popupCreateExercise());
            }
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
                        if (ownedExercise.getName().toLowerCase().contains(filterPattern)) {
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
                displayList.addAll((List<? extends OwnedExercise>) results.values);
                notifyDataSetChanged();
            }
        };
    }

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
            final int position = holder.getAdapterPosition();
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
            final RoutineWeek week = this.routine.get(weekViewHolder.getAdapterPosition());
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
                        int fromPosition = dragged.getAdapterPosition();
                        int toPosition = target.getAdapterPosition();
                        pendingRoutine.swapDaysOrder(weekViewHolder.getAdapterPosition(), fromPosition, toPosition);
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


            if (weekScrollStates.containsKey(weekViewHolder.getAdapterPosition())) {
                // maintain scroll position once this view is re bound from the recycler pool
                layoutManager.onRestoreInstanceState(weekScrollStates.get(weekViewHolder.getAdapterPosition()));
            }

            addDayButton.setOnClickListener(v -> {
                if (week.totalNumberOfDays() >= Variables.WORKOUT_MAX_NUMBER_OF_DAYS) {
                    // a little paranoid, but once was able to click fast enough to get past max days
                    return;
                }

                this.routine.appendEmptyDay(weekViewHolder.getAdapterPosition());
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
                        promptDeleteWeek(weekViewHolder.getAdapterPosition());
                        return true;
                    case copyAsNewWeekId:
                        if (this.routine.totalWeeks() >= Variables.MAX_NUMBER_OF_WEEKS) {
                            Toast.makeText(getContext(), "Max weeks already reached.", Toast.LENGTH_LONG).show();
                            return true;
                        }
                        copyWeekAsNew(weekViewHolder.getAdapterPosition());
                        return true;
                    case copyToExistingWeekId:
                        promptCopyToExistingWeek(weekViewHolder.getAdapterPosition());
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
            weekViewHolder.weekTitle.setText(String.format(Locale.getDefault(), "Week %d", weekViewHolder.getAdapterPosition() + 1));
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
                    int dayPosition = dayViewHolder.getAdapterPosition();
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
                int dayPosition = dayViewHolder.getAdapterPosition();
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

