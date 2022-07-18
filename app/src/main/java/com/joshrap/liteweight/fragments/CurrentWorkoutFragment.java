package com.joshrap.liteweight.fragments;

import android.animation.LayoutTransition;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import com.joshrap.liteweight.activities.WorkoutActivity;
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
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import static android.os.Looper.getMainLooper;

public class CurrentWorkoutFragment extends Fragment implements FragmentWithDialog {
    private TextView dayTV;
    private ImageButton forwardButton, backButton;
    private int currentDayIndex;
    private int currentWeekIndex;
    private Timer timer;
    private Stopwatch stopwatch;
    private Workout currentWorkout;
    private User user;
    private Routine routine;
    private AlertDialog alertDialog;
    private RecyclerView recyclerView;
    private ProgressBar workoutProgressBar;
    private TextView workoutProgressTV;
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
            FloatingActionButton createWorkoutBtn = view.findViewById(R.id.create_workout_btn);
            createWorkoutBtn.setOnClickListener(v -> ((WorkoutActivity) getActivity()).goToNewWorkout());
            return;
        }

        routine = currentWorkout.getRoutine();
        currentWeekIndex = currentWorkout.getCurrentWeek();
        currentDayIndex = currentWorkout.getCurrentDay();

        timer = ((WorkoutActivity) getActivity()).getTimer();
        stopwatch = ((WorkoutActivity) getActivity()).getStopwatch();

        recyclerView = view.findViewById(R.id.recycler_view);
        forwardButton = view.findViewById(R.id.next_day_button);
        backButton = view.findViewById(R.id.previous_day_button);
        dayTV = view.findViewById(R.id.day_text_view);

        workoutProgressBar = view.findViewById(R.id.progress_bar);
        workoutProgressTV = view.findViewById(R.id.progress_bar_TV);
        if (!sharedPreferences.getBoolean(Variables.WORKOUT_PROGRESS_KEY, true)) {
            RelativeLayout progressBarLayout = view.findViewById(R.id.relative_layout_progress);
            progressBarLayout.setVisibility(View.GONE);
        }

        setupChronometer(view);
        setupButtons();
        updateRoutineListUI();
        updateWorkoutProgressBar();
    }

    @Override
    public void onResume() {
        // when this fragment is visible again, the timer/stopwatch service is no longer needed so cancel it
        if (timer != null && timer.isTimerRunning()) {
            timer.cancelService();
        }

        if (stopwatch != null && stopwatch.isStopwatchRunning()) {
            stopwatch.cancelService();
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        hideAllDialogs();
        // as soon as this fragment isn't visible, start any running clock as a service
        if (timer != null && timer.isTimerRunning()) {
            timer.startService();
        }

        if (stopwatch != null && stopwatch.isStopwatchRunning()) {
            stopwatch.startService();
        }
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
     * Sets up whether the stopwatch or timer are displayed.
     *
     * @param view fragment view.
     */
    private void setupChronometer(View view) {
        RelativeLayout stopwatchContainer = view.findViewById(R.id.stopwatch_container);
        RelativeLayout timerContainer = view.findViewById(R.id.timer_container);

        boolean timerEnabled = sharedPreferences.getBoolean(Variables.TIMER_ENABLED, true);
        boolean stopwatchEnabled = sharedPreferences.getBoolean(Variables.STOPWATCH_ENABLED, true);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (timerEnabled && stopwatchEnabled) {
            // both are enabled, so use whatever was last used
            timer.initTimerUI(view, getActivity(), true);
            stopwatch.initStopwatchUI(view, getActivity(), true);
            String lastMode = sharedPreferences.getString(Variables.LAST_CLOCK_MODE, Variables.TIMER);
            switch (lastMode) {
                case Variables.TIMER:
                    stopwatchContainer.setVisibility(View.GONE);
                    break;
                case Variables.STOPWATCH:
                    timerContainer.setVisibility(View.GONE);
                    break;
            }
        } else if (timerEnabled) {
            // only the timer is enabled, hide the stopwatch
            editor.putString(Variables.LAST_CLOCK_MODE, Variables.TIMER);
            editor.apply();

            timerContainer.setVisibility(View.VISIBLE);
            stopwatchContainer.setVisibility(View.GONE);
            timer.initTimerUI(view, getActivity(), false);
        } else if (stopwatchEnabled) {
            // only the stopwatch is enabled, hide the timer
            editor.putString(Variables.LAST_CLOCK_MODE, Variables.STOPWATCH);
            editor.apply();

            timerContainer.setVisibility(View.GONE);
            stopwatchContainer.setVisibility(View.VISIBLE);
            stopwatch.initStopwatchUI(view, getActivity(), false);
        } else {
            // none are enabled so don't show any
            stopwatchContainer.setVisibility(View.GONE);
            timerContainer.setVisibility(View.GONE);
        }
    }

    /**
     * Setup button listeners for moving forward and backwards throughout the routine.
     */
    private void setupButtons() {
        dayTV.setOnClickListener(v -> showJumpDaysPopup());
        backButton.setOnClickListener(v -> {
            if (currentDayIndex > 0) {
                // if on this week there are more days, just decrease the current day index
                currentDayIndex--;
                updateRoutineListUI();
            } else if (currentWeekIndex > 0) {
                // there are more previous weeks
                currentWeekIndex--;
                currentDayIndex = routine.getWeek(currentWeekIndex).getNumberOfDays() - 1;
                updateRoutineListUI();
            }
            currentWorkout.setCurrentDay(currentDayIndex);
            currentWorkout.setCurrentWeek(currentWeekIndex);
        });
        forwardButton.setOnClickListener(v -> {
            if (currentDayIndex + 1 < routine.getWeek(currentWeekIndex).getNumberOfDays()) {
                // if can progress further in this week, do so
                currentDayIndex++;
                updateRoutineListUI();
            } else if (currentWeekIndex + 1 < routine.getNumberOfWeeks()) {
                // there are more weeks, so go to the next week
                currentDayIndex = 0;
                currentWeekIndex++;
                updateRoutineListUI();
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
            forwardButton.setImageResource(R.drawable.next_icon);
            if (currentWeekIndex + 1 == routine.getNumberOfWeeks() && routine.getWeek(currentWeekIndex).getNumberOfDays() == 1) {
                // a one day workout, must show the restart button
                forwardButton.setImageResource(R.drawable.restart_icon);
            }
        } else if (currentWeekIndex + 1 == routine.getNumberOfWeeks()
                && currentDayIndex + 1 == routine.getWeek(currentWeekIndex).getNumberOfDays()) {
            // last day, so show reset icon
            backButton.setVisibility(View.VISIBLE);
            // lil hacky, but don't want the ripple showing when the icons switch
            forwardButton.setVisibility(View.INVISIBLE);
            forwardButton.setVisibility(View.VISIBLE);
            // last day so set the restart icon instead of next icon
            forwardButton.setImageResource(R.drawable.restart_icon);
        } else if (currentWeekIndex < routine.getNumberOfWeeks()) {
            // not first day, not last. So show back and forward button
            backButton.setVisibility(View.VISIBLE);
            forwardButton.setVisibility(View.VISIBLE);
            forwardButton.setImageResource(R.drawable.next_icon);
        }
    }

    /**
     * Updates the list of displayed exercises in the workout depending on the current day.
     */
    private void updateRoutineListUI() {
        boolean videosEnabled = sharedPreferences.getBoolean(Variables.VIDEO_KEY, true);
        boolean metricUnits = user.getUserPreferences().isMetricUnits();
        RoutineAdapter routineAdapter = new RoutineAdapter(routine.getExerciseListForDay(currentWeekIndex, currentDayIndex),
                user.getOwnedExercises(), metricUnits, videosEnabled);

        recyclerView.setAdapter(routineAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        dayTV.setText(WorkoutUtils.generateDayTitle(currentWeekIndex, currentDayIndex));
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

                    updateRoutineListUI();
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
        for (Integer week : routine) {
            for (Integer day : routine.getWeek(week)) {
                for (RoutineExercise routineExercise : routine.getExerciseListForDay(week, day)) {
                    totalExercises++;
                    if (routineExercise.isCompleted()) {
                        exercisesCompleted++;
                    }
                }
            }
        }
        int percentage = (int) (((double) exercisesCompleted / (double) totalExercises) * 100);
        workoutProgressBar.setProgress(percentage);
        workoutProgressTV.setText(String.format("Workout Progress - %d %%", percentage));
    }

    /**
     * Allow the user to scroll through the list of days to quickly jump around in workout.
     */
    private void showJumpDaysPopup() {
        int totalDays = 0;
        int selectedVal = 0;
        List<String> days = new ArrayList<>();
        for (Integer week : routine) {
            for (Integer day : routine.getWeek(week)) {
                if (week == currentWeekIndex && day == currentDayIndex) {
                    // for highlighting what day the user is currently on
                    selectedVal = totalDays;
                }
                String dayTitle = WorkoutUtils.generateDayTitle(week, day);
                days.add(dayTitle);
                totalDays++;
            }
        }
        String[] daysAsArray = new String[totalDays];
        for (int i = 0; i < totalDays; i++) {
            daysAsArray[i] = days.get(i);
        }
        View popupView = getLayoutInflater().inflate(R.layout.popup_jump_days, null);
        NumberPicker dayPicker = popupView.findViewById(R.id.day_picker);
        dayPicker.setMinValue(0);
        dayPicker.setMaxValue(totalDays - 1);
        dayPicker.setValue(selectedVal);
        dayPicker.setWrapSelectorWheel(false);
        dayPicker.setDisplayedValues(daysAsArray);

        alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                .setTitle("Jump to Day")
                .setView(popupView)
                .setPositiveButton("Go", (dialog, which) -> {
                    int count = 0;
                    for (Integer week : routine) {
                        for (Integer day : routine.getWeek(week)) {
                            if (count == dayPicker.getValue()) {
                                currentWeekIndex = week;
                                currentDayIndex = day;
                            }
                            count++;
                        }
                    }
                    currentWorkout.setCurrentDay(currentDayIndex);
                    currentWorkout.setCurrentWeek(currentWeekIndex);
                    updateRoutineListUI();
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
        for (Integer week : routine) {
            for (Integer day : routine.getWeek(week)) {
                for (RoutineExercise routineExercise : routine.getExerciseListForDay(week, day)) {
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
        TextView progressTV = popupView.findViewById(R.id.progress_percentage_TV);
        progressTV.setText(String.format("%d %%", percentage));
        // color the percentage/percentage bar based on how much has been done
        int color;
        if (percentage <= 20) {
            color = R.color.workout_very_low_percentage;
        } else if (percentage <= 40) {
            color = R.color.workout_low_percentage;
        } else if (percentage <= 60) {
            color = R.color.workout_medium_percentage;
        } else if (percentage <= 80) {
            color = R.color.workout_high_percentage;
        } else {
            color = R.color.workout_very_high_percentage;
        }
        ;
        progressBar.setProgressTintList(ColorStateList.valueOf(getResources().getColor(color)));
        progressTV.setTextColor(ContextCompat.getColor(getContext(), color));

        alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                .setTitle("Restart Workout")
                .setView(popupView)
                .setPositiveButton("Yes", (dialog, which) -> {
                    restartWorkout();
                    updateRoutineListUI();
                })
                .setNegativeButton("No", null)
                .create();
        alertDialog.show();
    }

    private class RoutineAdapter extends RecyclerView.Adapter<RoutineAdapter.ViewHolder> {

        class ViewHolder extends RecyclerView.ViewHolder {
            CheckBox exerciseCheckbox;
            Button weightButton;
            ImageButton saveButton;
            ImageButton videoButton;
            LinearLayout extraInfo;

            EditText detailsInput;
            EditText weightInput;
            EditText setsInput;
            EditText repsInput;

            TextInputLayout weightInputLayout;
            TextInputLayout setsInputLayout;
            TextInputLayout repsInputLayout;
            TextInputLayout detailsInputLayout;
            LinearLayout rootLayout;

            ViewHolder(View itemView) {
                super(itemView);

                rootLayout = itemView.findViewById(R.id.root_layout);

                exerciseCheckbox = itemView.findViewById(R.id.exercise_name);
                weightButton = itemView.findViewById(R.id.weight_btn);
                extraInfo = itemView.findViewById(R.id.extra_info_layout);
                saveButton = itemView.findViewById(R.id.save_button);
                videoButton = itemView.findViewById(R.id.launch_video);

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

        private final List<RoutineExercise> exercises;
        private final Map<String, OwnedExercise> exerciseUserMap;
        private final boolean metricUnits;
        private final boolean videosEnabled;

        RoutineAdapter(List<RoutineExercise> routineExercises, Map<String, OwnedExercise> exerciseIdToName, boolean metricUnits, boolean videosEnabled) {
            this.exercises = routineExercises;
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
        public long getItemId(int position) {
            // not using the real power of recycler views since it's extremely cumbersome to deal with the "recycling" in this case...
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            // not using the real power of recycler views since it's extremely cumbersome to deal with the "recycling" in this case...
            return position;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position, List<Object> payloads) {
            // imma be real i have no idea why using this overload fixes the weird animation bugs
            if (!payloads.isEmpty()) {
                // this exercise has been updated, clear errors, set values, and animate back to single row
                final RoutineExercise exercise = exercises.get(position);

                // remove any errors
                holder.weightInputLayout.setError(null);
                holder.setsInputLayout.setError(null);
                holder.repsInputLayout.setError(null);
                holder.detailsInput.setError(null);
                // hide extra layout
                holder.weightButton.setVisibility(View.VISIBLE);
                holder.extraInfo.setVisibility(View.GONE);
                holder.saveButton.setVisibility(View.GONE);
                holder.videoButton.setVisibility((videosEnabled) ? View.VISIBLE : View.GONE);

                setInputs(holder, exercise);
            } else {
                super.onBindViewHolder(holder, position, payloads);
            }
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final RoutineExercise exercise = exercises.get(position);

            LinearLayout rootLayout = holder.rootLayout;
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

            Button weightButton = holder.weightButton;
            EditText weightInput = holder.weightInput;
            EditText detailsInput = holder.detailsInput;
            EditText repsInput = holder.repsInput;
            EditText setsInput = holder.setsInput;

            TextInputLayout detailsInputLayout = holder.detailsInputLayout;
            TextInputLayout setsInputLayout = holder.setsInputLayout;
            TextInputLayout repsInputLayout = holder.repsInputLayout;
            TextInputLayout weightInputLayout = holder.weightInputLayout;

            LinearLayout extraInfo = holder.extraInfo;
            ImageButton saveButton = holder.saveButton;
            ImageButton videoButton = holder.videoButton;
            videoButton.setVisibility((videosEnabled) ? View.VISIBLE : View.GONE);

            weightInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_WEIGHT_DIGITS)});
            setsInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_SETS_DIGITS)});
            repsInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_REPS_DIGITS)});
            detailsInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_DETAILS_LENGTH)});

            setInputs(holder, exercise);

            weightButton.setOnClickListener((v) -> {
                // show all the extra details for this exercise so the user can edit/read them
                weightButton.setVisibility(View.INVISIBLE);
                videoButton.setVisibility(View.GONE);
                extraInfo.setVisibility(View.VISIBLE);
                saveButton.setVisibility(View.VISIBLE);
            });

            saveButton.setOnClickListener(view -> {
                // first check if input on all fields is valid
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

                    notifyItemChanged(position, true);
                }
            });
            videoButton.setOnClickListener(v ->
                    ExerciseUtils.launchVideo(this.exerciseUserMap.get(exercise.getExerciseId()).getVideoUrl(), getContext()));
        }

        private void setInputs(ViewHolder holder, RoutineExercise exercise) {
            double weight = WeightUtils.getConvertedWeight(metricUnits, exercise.getWeight());
            String formattedWeight = WeightUtils.getFormattedWeightWithUnits(weight, metricUnits);
            holder.weightButton.setText(formattedWeight);
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
                weightLayout.setError("Weight cannot be empty.");
            } else if (weightInput.getText().toString().trim().length() > Variables.MAX_WEIGHT_DIGITS) {
                weightLayout.setError("Weight is too large.");
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
                detailsLayout.setError("Too many characters.");
                valid = false;
            }

            return valid;
        }

        @Override
        public int getItemCount() {
            return exercises.size();
        }
    }
}
