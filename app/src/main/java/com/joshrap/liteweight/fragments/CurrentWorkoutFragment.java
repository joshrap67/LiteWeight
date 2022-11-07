package com.joshrap.liteweight.fragments;

import android.animation.LayoutTransition;

import androidx.appcompat.app.AlertDialog;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import com.joshrap.liteweight.activities.WorkoutActivity;
import com.joshrap.liteweight.models.RoutineDay;
import com.joshrap.liteweight.models.RoutineWeek;
import com.joshrap.liteweight.utils.AndroidUtils;
import com.joshrap.liteweight.utils.ExerciseUtils;
import com.joshrap.liteweight.utils.WeightUtils;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.interfaces.FragmentWithDialog;
import com.joshrap.liteweight.models.OwnedExercise;
import com.joshrap.liteweight.models.RoutineExercise;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.models.Routine;
import com.joshrap.liteweight.models.User;
import com.joshrap.liteweight.models.UserWithWorkout;
import com.joshrap.liteweight.models.Workout;
import com.joshrap.liteweight.utils.WorkoutUtils;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.network.repos.WorkoutRepository;
import com.joshrap.liteweight.widgets.Stopwatch;
import com.joshrap.liteweight.widgets.Timer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import static android.os.Looper.getMainLooper;

public class CurrentWorkoutFragment extends Fragment implements FragmentWithDialog {
    private Button forwardButton, backButton;
    private int currentDayIndex, currentWeekIndex;
    private Timer timer;
    private Stopwatch stopwatch;
    private Workout currentWorkout;
    private User user;
    private Routine routine;
    private AlertDialog alertDialog;
    private RecyclerView recyclerView;
    private ProgressBar workoutProgressBar;
    private TextView workoutProgressTV, secondaryTimerTV, secondaryStopwatchTV, dayTV, dayTagTV;
    private UserWithWorkout userWithWorkout;
    private ClockBottomFragment clockBottomFragment;

    private enum RoutineListAnimateMode {NONE, FROM_LEFT, FROM_RIGHT} // todo kill these animations?


    @Inject
    AlertDialog loadingDialog;
    @Inject
    SharedPreferences sharedPreferences;
    @Inject
    WorkoutRepository workoutRepository;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        Injector.getInjector(getContext()).inject(this);

        userWithWorkout = ((WorkoutActivity) getActivity()).getUserWithWorkout();
        currentWorkout = userWithWorkout.getWorkout();
        user = userWithWorkout.getUser();
        ((WorkoutActivity) getActivity()).toggleBackButton(false);

        View view;
        if (!userWithWorkout.isWorkoutPresent()) {
            // user has no workouts, display special layout telling them to create one
            ((WorkoutActivity) getActivity()).updateToolbarTitle("LiteWeight");
            view = inflater.inflate(R.layout.no_workouts_found_layout, container, false);
        } else {
            ((WorkoutActivity) getActivity()).updateToolbarTitle(currentWorkout.getWorkoutName());
            view = inflater.inflate(R.layout.fragment_current_workout, container, false);
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!userWithWorkout.isWorkoutPresent()) {
            ExtendedFloatingActionButton createWorkoutBtn = view.findViewById(R.id.create_workout_btn);
            createWorkoutBtn.setOnClickListener(v -> ((WorkoutActivity) getActivity()).goToCreateWorkout());
            return;
        }

        routine = currentWorkout.getRoutine();
        currentWeekIndex = currentWorkout.getCurrentWeek();
        currentDayIndex = currentWorkout.getCurrentDay();

        timer = ((WorkoutActivity) getActivity()).getTimer();
        stopwatch = ((WorkoutActivity) getActivity()).getStopwatch();
        clockBottomFragment = ClockBottomFragment.newInstance();

        recyclerView = view.findViewById(R.id.recycler_view);
        forwardButton = view.findViewById(R.id.next_day_button);
        backButton = view.findViewById(R.id.previous_day_button);
        dayTV = view.findViewById(R.id.day_text_view);
        dayTagTV = view.findViewById(R.id.day_tag_text_view);

        ImageButton clockButton = view.findViewById(R.id.timer_icon_btn);
        clockButton.setOnClickListener(v -> clockBottomFragment.show(getActivity().getSupportFragmentManager(), ClockBottomFragment.TAG));
        clockButton.setOnLongClickListener(v -> {
            boolean useTimer = false;

            // determine what this long press does - start timer/stopwatch depending on what is in shared prefs
            boolean timerEnabled = sharedPreferences.getBoolean(Variables.TIMER_ENABLED, true);
            boolean stopwatchEnabled = sharedPreferences.getBoolean(Variables.STOPWATCH_ENABLED, true);
            final SharedPreferences.Editor editor = sharedPreferences.edit();
            if (timerEnabled && stopwatchEnabled) {
                // both are enabled, so use whatever was last used
                String lastMode = sharedPreferences.getString(Variables.LAST_CLOCK_MODE, Variables.TIMER);
                useTimer = lastMode.equals(Variables.TIMER);
            } else if (timerEnabled) {
                // only the timer is enabled, hide the stopwatch
                editor.putString(Variables.LAST_CLOCK_MODE, Variables.TIMER);
                editor.apply();

                useTimer = true;
            } else if (stopwatchEnabled) {
                // only the stopwatch is enabled, hide the timer
                editor.putString(Variables.LAST_CLOCK_MODE, Variables.STOPWATCH);
                editor.apply();
            } else {
                // shouldn't be reached, but none are enabled so don't show anything
                return true;
            }

            if (useTimer) {
                if (timer.isTimerRunning()) {
                    stopTimer();
                } else {
                    startTimer();
                }
            } else {
                if (stopwatch.isStopwatchRunning()) {
                    stopStopwatch();
                } else {
                    startStopwatch();
                }
            }
            return true;
        });

        secondaryTimerTV = view.findViewById(R.id.secondary_timer_tv);
        secondaryStopwatchTV = view.findViewById(R.id.secondary_stopwatch_tv);

        workoutProgressBar = view.findViewById(R.id.progress_bar);
        workoutProgressTV = view.findViewById(R.id.progress_bar_TV);
        if (!sharedPreferences.getBoolean(Variables.WORKOUT_PROGRESS_KEY, true)) {
            FrameLayout progressBarLayout = view.findViewById(R.id.workout_progress_layout);
            progressBarLayout.setVisibility(View.GONE);
        }

        setupDayButtons();
        updateRoutineListUI(RoutineListAnimateMode.NONE);
        updateWorkoutProgressBar();

        // setup clock UI
        boolean timerEnabled = sharedPreferences.getBoolean(Variables.TIMER_ENABLED, true);
        boolean stopwatchEnabled = sharedPreferences.getBoolean(Variables.STOPWATCH_ENABLED, true);
        if (!timerEnabled && !stopwatchEnabled) {
            // neither are enabled so hide button, invisible as to not mess up layout
            clockButton.setVisibility(View.INVISIBLE);
        }

        timer.displayTime.observe(getViewLifecycleOwner(), this::updateTimerDisplay);

        stopwatch.displayTime.observe(getViewLifecycleOwner(), this::updateStopwatchDisplay);

        timer.timerRunning.observe(getViewLifecycleOwner(), isRunning -> secondaryTimerTV.setVisibility(isRunning ? View.VISIBLE : View.INVISIBLE));
        stopwatch.stopwatchRunning.observe(getViewLifecycleOwner(), isRunning -> secondaryStopwatchTV.setVisibility(isRunning ? View.VISIBLE : View.INVISIBLE));
    }

    @Override
    public void onResume() {
        super.onResume();
        // when this fragment is visible again, the timer/stopwatch service is no longer needed so cancel it
        if (timer != null && timer.isTimerRunning()) {
            ((WorkoutActivity) getActivity()).cancelTimerService();
        }

        if (stopwatch != null && stopwatch.isStopwatchRunning()) {
            ((WorkoutActivity) getActivity()).cancelStopwatchService();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        hideAllDialogs();
        // as soon as this fragment isn't visible, start any running clock as a service
        if (timer != null && timer.isTimerRunning()) {
            ((WorkoutActivity) getActivity()).startTimerService();
        }

        if (stopwatch != null && stopwatch.isStopwatchRunning()) {
            ((WorkoutActivity) getActivity()).startStopwatchService();
        }
    }

    @Override
    public void hideAllDialogs() {
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
        if (clockBottomFragment != null && clockBottomFragment.isVisible()) {
            clockBottomFragment.dismiss();
        }
    }

    private void startStopwatch() {
        stopwatch.startStopwatch();
    }

    private void startTimer() {
        timer.startTimer();
    }

    private void stopTimer() {
        timer.stopTimer();
    }

    private void stopStopwatch() {
        stopwatch.stopStopwatch();
    }


    private void updateTimerDisplay(long elapsedTime) {
        int minutes = (int) (elapsedTime / (60 * Timer.timeUnit));
        int seconds = (int) (elapsedTime / Timer.timeUnit) % 60;
        String timeRemaining = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

        if (secondaryTimerTV != null) {
            secondaryTimerTV.setText(timeRemaining);
        }
    }

    private void updateStopwatchDisplay(long elapsedTime) {
        int minutes = (int) (elapsedTime / (60 * Stopwatch.timeUnit));
        int seconds = (int) (elapsedTime / Stopwatch.timeUnit) % 60;
        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

        if (secondaryStopwatchTV != null) {
            secondaryStopwatchTV.setText(timeFormatted);
        }
    }

    /**
     * Setup button listeners for moving forward and backwards throughout the routine.
     */
    private void setupDayButtons() {
        dayTV.setOnClickListener(v -> showJumpDaysPopup());
        dayTagTV.setOnClickListener(v -> showJumpDaysPopup());
        backButton.setOnClickListener(v -> {
            if (currentDayIndex > 0) {
                // if on this week there are more days, just decrease the current day index
                currentDayIndex--;
                updateRoutineListUI(RoutineListAnimateMode.FROM_RIGHT);
            } else if (currentWeekIndex > 0) {
                // there are more previous weeks
                currentWeekIndex--;
                currentDayIndex = routine.getWeek(currentWeekIndex).getNumberOfDays() - 1;
                updateRoutineListUI(RoutineListAnimateMode.FROM_RIGHT);
            }
            currentWorkout.setCurrentDay(currentDayIndex);
            currentWorkout.setCurrentWeek(currentWeekIndex);
        });
        forwardButton.setOnClickListener(v -> {
            if (currentDayIndex + 1 < routine.getWeek(currentWeekIndex).getNumberOfDays()) {
                // if can progress further in this week, do so
                currentDayIndex++;
                updateRoutineListUI(RoutineListAnimateMode.FROM_LEFT);
            } else if (currentWeekIndex + 1 < routine.getNumberOfWeeks()) {
                // there are more weeks, so go to the next week
                currentDayIndex = 0;
                currentWeekIndex++;
                updateRoutineListUI(RoutineListAnimateMode.FROM_LEFT);
            } else {
                // on last week, prompt user to restart the workout
                showRestartPopup();
            }
            currentWorkout.setCurrentDay(currentDayIndex);
            currentWorkout.setCurrentWeek(currentWeekIndex);
        });
    }

    /**
     * Updates the visibility and icon of the navigation buttons depending on the current day.
     */
    private void updateButtonViews() {
        if (currentDayIndex == 0 && currentWeekIndex == 0) {
            // it's the first day in the entire routine, so hide the back button
            backButton.setVisibility(View.INVISIBLE);
            forwardButton.setVisibility(View.VISIBLE);
            forwardButton.setText(R.string.next_day);
            if (currentWeekIndex + 1 == routine.getNumberOfWeeks() && routine.getWeek(currentWeekIndex).getNumberOfDays() == 1) {
                // a one day workout, must show the restart button
                forwardButton.setText(R.string.restart_workout);
            }
        } else if (currentWeekIndex + 1 == routine.getNumberOfWeeks()
                && currentDayIndex + 1 == routine.getWeek(currentWeekIndex).getNumberOfDays()) {
            // last day, so show reset icon
            backButton.setVisibility(View.VISIBLE);
            forwardButton.setVisibility(View.VISIBLE);
            // last day so set the restart icon instead of next icon
            forwardButton.setText(R.string.restart_workout);
        } else if (currentWeekIndex < routine.getNumberOfWeeks()) {
            // not first day, not last. So show back and forward button
            backButton.setVisibility(View.VISIBLE);
            forwardButton.setVisibility(View.VISIBLE);
            forwardButton.setText(R.string.next_day);
        }
    }

    /**
     * Updates the list of displayed exercises in the workout depending on the current day.
     */
    private void updateRoutineListUI(RoutineListAnimateMode mode) {
        boolean videosEnabled = sharedPreferences.getBoolean(Variables.VIDEO_KEY, true);
        boolean metricUnits = user.getUserPreferences().isMetricUnits();

        List<RoutineRowModel> routineRowModels = new ArrayList<>();
        for (RoutineExercise exercise : routine.getExerciseListForDay(currentWeekIndex, currentDayIndex)) {
            RoutineRowModel exerciseRowModel = new RoutineRowModel(exercise, false);
            routineRowModels.add(exerciseRowModel);
        }
        RoutineAdapter routineAdapter = new RoutineAdapter(routineRowModels, user.getOwnedExercises(), metricUnits, videosEnabled);

        recyclerView.setAdapter(routineAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        if (mode == RoutineListAnimateMode.FROM_LEFT) {
            recyclerView.startAnimation(AndroidUtils.wiggleFromLeft(1));
        } else if (mode == RoutineListAnimateMode.FROM_RIGHT) {
            recyclerView.startAnimation(AndroidUtils.wiggleFromRight(1));
        }
        dayTV.setText(WorkoutUtils.generateDayTitle(currentWeekIndex, currentDayIndex));
        String dayTag = routine.getDay(currentWeekIndex, currentDayIndex).getTag();
        dayTagTV.setVisibility(dayTag == null ? View.INVISIBLE : View.VISIBLE);
        dayTagTV.setText(dayTag + " "); // android cuts off italics on wrap content without trailing whitespace
        updateButtonViews();
    }

    /**
     * Reset all of the exercises to being incomplete and then write to the database with these changes.
     */
    private void restartWorkout() {
        AndroidUtils.showLoadingDialog(loadingDialog, "Restarting...");
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ResultStatus<UserWithWorkout> resultStatus = this.workoutRepository.restartWorkout(currentWorkout);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                loadingDialog.dismiss();
                if (resultStatus.isSuccess()) {
                    // update the statistics for this workout
                    user.getWorkoutMetas().put(currentWorkout.getWorkoutId(),
                            resultStatus.getData().getUser().getWorkoutMetas().get(currentWorkout.getWorkoutId()));
                    // in case any default weights were updated
                    user.updateOwnedExercises(resultStatus.getData().getUser().getOwnedExercises());

                    currentWorkout.setRoutine(resultStatus.getData().getWorkout().getRoutine());
                    routine = currentWorkout.getRoutine();
                    currentDayIndex = 0;
                    currentWeekIndex = 0;
                    currentWorkout.setCurrentDay(currentDayIndex);
                    currentWorkout.setCurrentWeek(currentWeekIndex);

                    updateRoutineListUI(RoutineListAnimateMode.NONE);
                    updateWorkoutProgressBar();
                } else {
                    AndroidUtils.showErrorDialog("Restart Error", resultStatus.getErrorMessage(), getContext());
                }
            });
        });
    }

    /**
     * Updates the progress of the current workout. Is called anytime an exercise is checked.
     */
    private void updateWorkoutProgressBar() {
        int exercisesCompleted = 0;
        int totalExercises = 0;
        for (RoutineWeek week : routine) {
            for (RoutineDay day : week) {
                for (RoutineExercise routineExercise : day) {
                    totalExercises++;
                    if (routineExercise.isCompleted()) {
                        exercisesCompleted++;
                    }
                }
            }
        }
        int percentage = (int) (((double) exercisesCompleted / (double) totalExercises) * 100);
        workoutProgressBar.setProgress(percentage, true);
        workoutProgressTV.setText(String.format("Workout Progress - %d %%", percentage));
    }

    /**
     * Allow the user to scroll through the list of days to quickly jump around in workout.
     */
    private void showJumpDaysPopup() {
        int totalDays = 0;
        int selectedVal = 0;
        List<String> days = new ArrayList<>();
        for (int weekIndex = 0; weekIndex < routine.getNumberOfWeeks(); weekIndex++) {
            RoutineWeek week = routine.getWeek(weekIndex);
            for (int dayIndex = 0; dayIndex < week.getNumberOfDays(); dayIndex++) {
                if (weekIndex == currentWeekIndex && dayIndex == currentDayIndex) {
                    // for highlighting what day the user is currently on
                    selectedVal = totalDays;
                }
                String dayTitle = WorkoutUtils.generateDayTitle(weekIndex, dayIndex);
                days.add(dayTitle);
                totalDays++;
            }
        }
        String[] daysAsArray = new String[totalDays];
        days.toArray(daysAsArray);

        View popupView = getLayoutInflater().inflate(R.layout.popup_jump_days, null);
        NumberPicker dayPicker = popupView.findViewById(R.id.day_picker);
        dayPicker.setMinValue(0);
        dayPicker.setMaxValue(totalDays - 1);
        dayPicker.setValue(selectedVal);
        dayPicker.setWrapSelectorWheel(false);
        dayPicker.setDisplayedValues(daysAsArray);

        alertDialog = new AlertDialog.Builder(getContext())
                .setTitle("Jump to Day")
                .setView(popupView)
                .setPositiveButton("Go", (dialog, which) -> {
                    int count = 0;
                    for (int weekIndex = 0; weekIndex < routine.getNumberOfWeeks(); weekIndex++) {
                        RoutineWeek week = routine.getWeek(weekIndex);
                        for (int dayIndex = 0; dayIndex < week.getNumberOfDays(); dayIndex++) {
                            if (count == dayPicker.getValue()) {
                                currentWeekIndex = weekIndex;
                                currentDayIndex = dayIndex;
                            }
                            count++;
                        }
                    }
                    currentWorkout.setCurrentDay(currentDayIndex);
                    currentWorkout.setCurrentWeek(currentWeekIndex);
                    updateRoutineListUI(RoutineListAnimateMode.NONE);
                })
                .create();
        alertDialog.show();
    }

    /**
     * Prompt the user if they wish to restart the current workout.
     */
    private void showRestartPopup() {
        int exercisesCompleted = 0;
        int totalExercises = 0;
        for (RoutineWeek week : routine) {
            for (RoutineDay day : week) {
                for (RoutineExercise routineExercise : day) {
                    totalExercises++;
                    if (routineExercise.isCompleted()) {
                        exercisesCompleted++;
                    }
                }
            }
        }
        int percentage = (int) (((double) exercisesCompleted / (double) totalExercises) * 100);

        View popupView = getLayoutInflater().inflate(R.layout.popup_restart_workout, null);
        ProgressBar progressBar = popupView.findViewById(R.id.progress_bar);
        progressBar.setProgress(percentage);
        TextView progressTV = popupView.findViewById(R.id.progress_bar_TV);
        progressTV.setText(String.format("%d %%", percentage));

        alertDialog = new AlertDialog.Builder(getContext())
                .setTitle("Restart Workout")
                .setView(popupView)
                .setPositiveButton("Yes", (dialog, which) -> {
                    restartWorkout();
                })
                .setNegativeButton("No", null)
                .create();
        alertDialog.show();
    }

    private class RoutineAdapter extends RecyclerView.Adapter<RoutineAdapter.ViewHolder> {

        class ViewHolder extends RecyclerView.ViewHolder {
            RelativeLayout rootLayout;

            CheckBox exerciseCheckbox;
            Button expandButton;
            RelativeLayout extraInfo;

            EditText detailsInput;
            EditText weightInput;
            EditText setsInput;
            EditText repsInput;
            Button videoButton;

            TextInputLayout weightInputLayout;
            TextInputLayout setsInputLayout;
            TextInputLayout repsInputLayout;
            TextInputLayout detailsInputLayout;

            ViewHolder(View itemView) {
                super(itemView);

                rootLayout = itemView.findViewById(R.id.root_layout);

                exerciseCheckbox = itemView.findViewById(R.id.exercise_name);
                expandButton = itemView.findViewById(R.id.expand_btn);
                extraInfo = itemView.findViewById(R.id.extra_info_layout);
                videoButton = itemView.findViewById(R.id.launch_video_button);

                weightInput = itemView.findViewById(R.id.weight_input);
                detailsInput = itemView.findViewById(R.id.details_input);
                setsInput = itemView.findViewById(R.id.sets_input);
                repsInput = itemView.findViewById(R.id.reps_input);

                weightInputLayout = itemView.findViewById(R.id.weight_input_layout);
                setsInputLayout = itemView.findViewById(R.id.sets_input_layout);
                repsInputLayout = itemView.findViewById(R.id.reps_input_layout);
                detailsInputLayout = itemView.findViewById(R.id.details_input_layout);
            }
        }

        private final List<RoutineRowModel> routineRowModels;
        private final Map<String, OwnedExercise> exerciseUserMap;
        private final boolean metricUnits;
        private final boolean videosEnabled;

        RoutineAdapter(List<RoutineRowModel> routineRowModels, Map<String, OwnedExercise> exerciseIdToName, boolean metricUnits, boolean videosEnabled) {
            this.routineRowModels = routineRowModels;
            this.exerciseUserMap = exerciseIdToName;
            this.metricUnits = metricUnits;
            this.videosEnabled = videosEnabled;
        }

        @NonNull
        @Override
        public RoutineAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View exerciseView = inflater.inflate(R.layout.row_exercise_active_workout, parent, false);
            return new RoutineAdapter.ViewHolder(exerciseView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position, List<Object> payloads) {
            // this overload is needed since if you rebind with the intention to only collapse, the linear layout is overridden causing weird animation bugs
            if (!payloads.isEmpty()) {
                final RoutineRowModel routineRowModel = routineRowModels.get(position);
                final RoutineExercise exercise = routineRowModel.routineExercise;
                boolean isExpanded = routineRowModel.isExpanded;

                if (isExpanded) {
                    setExpandedViews(holder, exercise);
                } else {
                    setCollapsedViews(holder, exercise);
                }
            } else {
                super.onBindViewHolder(holder, position, payloads);
            }
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final RoutineRowModel rowModel = routineRowModels.get(position);
            final RoutineExercise exercise = rowModel.routineExercise;
            boolean isExpanded = rowModel.isExpanded;

            RelativeLayout rootLayout = holder.rootLayout;
            LayoutTransition layoutTransition = rootLayout.getLayoutTransition();
            layoutTransition.addTransitionListener(new LayoutTransition.TransitionListener() {
                @Override
                public void endTransition(LayoutTransition transition, ViewGroup container, View view, int transitionType) {
                    RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(getContext()) {
                        @Override
                        protected int getVerticalSnapPreference() {
                            return LinearSmoothScroller.SNAP_TO_START;
                        }
                    };

                    if (transitionType == LayoutTransition.CHANGE_APPEARING &&
                            holder.itemView.getY() > recyclerView.getHeight() * .60) {
                        // start to scroll down if the view being expanded is a certain amount of distance from the top of the recycler view
                        smoothScroller.setTargetPosition(holder.getLayoutPosition());
                        recyclerView.getLayoutManager().startSmoothScroll(smoothScroller);
                    }
                }

                @Override
                public void startTransition(LayoutTransition transition, ViewGroup container, View view, int transitionType) {
                }
            });

            String currentExerciseName = this.exerciseUserMap.get(exercise.getExerciseId()).getExerciseName();
            CheckBox exerciseCheckbox = holder.exerciseCheckbox;
            exerciseCheckbox.setText(currentExerciseName);
            exerciseCheckbox.setChecked(exercise.isCompleted());
            exerciseCheckbox.setOnClickListener(v -> {
                exercise.setCompleted(exerciseCheckbox.isChecked());
                updateWorkoutProgressBar();
            });

            Button expandButton = holder.expandButton;
            EditText weightInput = holder.weightInput;
            EditText detailsInput = holder.detailsInput;
            EditText repsInput = holder.repsInput;
            EditText setsInput = holder.setsInput;

            TextInputLayout detailsInputLayout = holder.detailsInputLayout;
            TextInputLayout setsInputLayout = holder.setsInputLayout;
            TextInputLayout repsInputLayout = holder.repsInputLayout;
            TextInputLayout weightInputLayout = holder.weightInputLayout;

            Button videoButton = holder.videoButton;
            videoButton.setVisibility((videosEnabled) ? View.VISIBLE : View.GONE);

            weightInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_WEIGHT_DIGITS)});
            setsInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_SETS_DIGITS)});
            repsInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_REPS_DIGITS)});
            detailsInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_DETAILS_LENGTH)});

            if (isExpanded) {
                setExpandedViews(holder, exercise);
            } else {
                setCollapsedViews(holder, exercise);
            }

            expandButton.setOnClickListener((v) -> {
                ((WorkoutActivity) getActivity()).hideKeyboard();

                if (rowModel.isExpanded) {
                    boolean validInput = inputValid(weightInput, detailsInput, setsInput, repsInput,
                            weightInputLayout, detailsInputLayout, setsInputLayout, repsInputLayout);

                    if (validInput) {
                        double newWeight = Double.parseDouble(weightInput.getText().toString());
                        if (metricUnits) {
                            // convert back to imperial if in metric since weight is stored in imperial on backend
                            newWeight = WeightUtils.metricWeightToImperial(newWeight);
                        }

                        exercise.setWeight(newWeight);
                        exercise.setDetails(detailsInput.getText().toString().trim());
                        exercise.setReps(Integer.valueOf(repsInput.getText().toString().trim()));
                        exercise.setSets(Integer.valueOf(setsInput.getText().toString().trim()));

                        rowModel.isExpanded = false;

                        notifyItemChanged(position, true);
                        ((WorkoutActivity) getActivity()).hideKeyboard();
                    }

                } else {
                    // show all the extra details for this exercise so the user can edit/read them
                    rowModel.isExpanded = true;
                    notifyItemChanged(position, true);
                    setExpandedViews(holder, exercise); // this prevents weird flashing on expanded animation
                }
            });

            videoButton.setOnClickListener(v ->
                    ExerciseUtils.launchVideo(this.exerciseUserMap.get(exercise.getExerciseId()).getVideoUrl(), getContext()));
        }

        private void setExpandedViews(ViewHolder holder, RoutineExercise exercise) {
            holder.extraInfo.setVisibility(View.VISIBLE);
            holder.expandButton.setText(R.string.done_all_caps);
            holder.expandButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.small_up_arrow, 0);

            setInputs(holder, exercise);
        }

        private void setCollapsedViews(ViewHolder holder, RoutineExercise exercise) {
            holder.weightInputLayout.setError(null);
            holder.setsInputLayout.setError(null);
            holder.repsInputLayout.setError(null);
            holder.detailsInput.setError(null);

            // hide the extra layout
            holder.extraInfo.setVisibility(View.GONE);

            double weight = WeightUtils.getConvertedWeight(metricUnits, exercise.getWeight());
            String formattedWeight = WeightUtils.getFormattedWeightWithUnits(weight, metricUnits);
            holder.expandButton.setText(formattedWeight);
            holder.expandButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.small_down_arrow, 0);
        }

        private void setInputs(ViewHolder holder, RoutineExercise exercise) {
            double weight = WeightUtils.getConvertedWeight(metricUnits, exercise.getWeight());
            holder.weightInputLayout.setHint("Weight (" + (metricUnits ? "kg)" : "lb)"));

            holder.weightInput.setText(WeightUtils.getFormattedWeightForEditText(weight));
            holder.setsInput.setText(Integer.toString(exercise.getSets()));
            holder.repsInput.setText(Integer.toString(exercise.getReps()));
            holder.detailsInput.setText(exercise.getDetails());
        }

        private boolean inputValid(EditText weightInput, EditText detailsInput,
                                   EditText setsInput, EditText repsInput, TextInputLayout weightLayout,
                                   TextInputLayout detailsLayout, TextInputLayout setsLayout, TextInputLayout repsLayout) {
            boolean valid = true;
            if (weightInput.getText().toString().trim().isEmpty()) {
                valid = false;
                weightLayout.setError("Weight cannot be empty");
            } else if (weightInput.getText().toString().trim().length() > Variables.MAX_WEIGHT_DIGITS) {
                weightLayout.setError("Weight too large");
                valid = false;
            }

            if (setsInput.getText().toString().trim().isEmpty() ||
                    setsInput.getText().toString().length() > Variables.MAX_SETS_DIGITS) {
                setsLayout.setError("Invalid");
                valid = false;
            }

            if (repsInput.getText().toString().trim().isEmpty() ||
                    repsInput.getText().toString().length() > Variables.MAX_REPS_DIGITS) {
                repsLayout.setError("Invalid");
                valid = false;
            }

            if (detailsInput.getText().toString().length() > Variables.MAX_DETAILS_LENGTH) {
                detailsLayout.setError("Too many characters");
                valid = false;
            }

            return valid;
        }

        @Override
        public int getItemCount() {
            return routineRowModels.size();
        }
    }

    // separate class that wraps the routine exercise and holds data about the state of the row in the recycler view
    private class RoutineRowModel {
        private final RoutineExercise routineExercise;
        private boolean isExpanded;

        public RoutineRowModel(RoutineExercise routineExercise, boolean isExpanded) {
            this.routineExercise = routineExercise;
            this.isExpanded = isExpanded;
        }
    }
}
