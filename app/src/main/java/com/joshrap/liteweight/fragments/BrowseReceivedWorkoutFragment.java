package com.joshrap.liteweight.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.activities.WorkoutActivity;
import com.joshrap.liteweight.adapters.RoutineAdapter;
import com.joshrap.liteweight.adapters.SentRoutineAdapter;
import com.joshrap.liteweight.helpers.AndroidHelper;
import com.joshrap.liteweight.helpers.InputHelper;
import com.joshrap.liteweight.helpers.WorkoutHelper;
import com.joshrap.liteweight.imports.Globals;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.interfaces.FragmentWithDialog;
import com.joshrap.liteweight.models.AcceptWorkoutResponse;
import com.joshrap.liteweight.models.ReceivedWorkoutMeta;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.models.SentRoutine;
import com.joshrap.liteweight.models.SentWorkout;
import com.joshrap.liteweight.models.User;
import com.joshrap.liteweight.network.repos.WorkoutRepository;
import com.joshrap.liteweight.widgets.ErrorDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import static android.os.Looper.getMainLooper;

public class BrowseReceivedWorkoutFragment extends Fragment implements FragmentWithDialog {
    private User user;
    private ProgressBar loadingIcon;
    private RecyclerView recyclerView;
    private SentWorkout sentWorkout;
    private SentRoutine sentRoutine;
    private TextView dayTV;
    private String workoutName;
    private ImageButton forwardButton, backButton;
    private int currentDayIndex;
    private int currentWeekIndex;
    private AlertDialog alertDialog;
    private ConstraintLayout mainLayout;
    private String receivedWorkoutId;
    @Inject
    WorkoutRepository workoutRepository;
    @Inject
    ProgressDialog loadingDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Injector.getInjector(getContext()).inject(this);
        receivedWorkoutId = null;
        if (this.getArguments() != null) {
            receivedWorkoutId = this.getArguments().getString(SentWorkout.SENT_WORKOUT_ID);
            workoutName = this.getArguments().getString(SentWorkout.WORKOUT_NAME);
            // todo return if these aren't here?
        }
        ((WorkoutActivity) getActivity()).updateToolbarTitle(workoutName);
        ((WorkoutActivity) getActivity()).toggleBackButton(true);
        user = Globals.user;
        final View view = inflater.inflate(R.layout.fragment_browse_received_workout, container, false);

        loadingIcon = view.findViewById(R.id.loading_icon);
        recyclerView = view.findViewById(R.id.recycler_view);
        mainLayout = view.findViewById(R.id.main_layout);
        dayTV = view.findViewById(R.id.day_text_view);
        forwardButton = view.findViewById(R.id.next_day_button);
        backButton = view.findViewById(R.id.previous_day_button);
        Button acceptWorkoutButton = view.findViewById(R.id.accept_workout_btn);
        acceptWorkoutButton.setOnClickListener(view1 -> {
            boolean workoutNameExists = false;
            for (String workoutId : user.getUserWorkouts().keySet()) {
                if (user.getUserWorkouts().get(workoutId).getWorkoutName().equals(workoutName)) {
                    workoutNameExists = true;
                    break;
                }
            }
            if (workoutNameExists) {
                workoutNameAlreadyExistsPopup(sentWorkout);
            } else {
                acceptWorkout(null);
            }
        });
        Button declineWorkoutButton = view.findViewById(R.id.decline_workout_btn);

        getReceivedWorkout(receivedWorkoutId);
        return view;
    }

    private void workoutNameAlreadyExistsPopup(final SentWorkout receivedWorkout) {
        /*
            Prompt the user if they want to rename the current workout
         */
        View popupView = getLayoutInflater().inflate(R.layout.popup_workout_name_exists, null);
        final EditText renameInput = popupView.findViewById(R.id.rename_workout_name_input);
        final TextInputLayout workoutNameInputLayout = popupView.findViewById(R.id.rename_workout_name_input_layout);
        renameInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_WORKOUT_NAME)});
        renameInput.addTextChangedListener(AndroidHelper.hideErrorTextWatcher(workoutNameInputLayout));
        alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                .setTitle("\"" + receivedWorkout.getWorkoutName() + "\" already exists")
                .setView(popupView)
                .setPositiveButton("Submit", null)
                .setNegativeButton("Cancel", null)
                .create();
        alertDialog.setOnShowListener(dialogInterface -> {
            Button saveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            saveButton.setOnClickListener(view -> {
                String newName = renameInput.getText().toString().trim();
                List<String> workoutNames = new ArrayList<>();
                for (String workoutId : user.getUserWorkouts().keySet()) {
                    workoutNames.add(user.getUserWorkouts().get(workoutId).getWorkoutName());
                }
                String errorMsg = InputHelper.validWorkoutName(newName, workoutNames);
                if (errorMsg == null) {
                    acceptWorkout(newName);
                    alertDialog.dismiss();
                } else {
                    workoutNameInputLayout.setError(errorMsg);
                }
            });
        });
        alertDialog.show();
    }

    private void acceptWorkout(final String optionalName) {
        AndroidHelper.showLoadingDialog(loadingDialog, "Accepting...");

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ResultStatus<AcceptWorkoutResponse> resultStatus = this.workoutRepository.acceptReceivedWorkout(receivedWorkoutId, optionalName);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                loadingDialog.dismiss();
                if (resultStatus.isSuccess()) {
                    AcceptWorkoutResponse response = resultStatus.getData();
                    if (user.getCurrentWorkout() == null) {
                        // this newly accepted workout is the only workout the user now owns, so make it the current one
                        user.setCurrentWorkout(response.getWorkoutId());
                        Globals.activeWorkout = response.getWorkout();
                    }
                    user.getUserWorkouts().put(response.getWorkoutId(), response.getWorkoutMeta());
                    user.setTotalReceivedWorkouts(user.getTotalReceivedWorkouts() - 1);
                    user.addNewExercises(response.getExercises());
                    if (!user.getReceivedWorkouts().get(receivedWorkoutId).isSeen()) {
                        // this workout was not seen, so make sure to decrease the unseen count since it is no longer in the list
                        user.setUnseenReceivedWorkouts(user.getUnseenReceivedWorkouts() - 1);
                    }
                    user.getReceivedWorkouts().remove(receivedWorkoutId);
                    ((WorkoutActivity) getActivity()).updateReceivedWorkoutNotificationIndicator();
                    ((WorkoutActivity) getActivity()).finishFragment();
                } else {
                    ErrorDialog.showErrorDialog("Error", resultStatus.getErrorMessage(), getContext());
                }
            });
        });
    }

    private void getReceivedWorkout(String sentWorkoutId) {
        mainLayout.setVisibility(View.GONE);
        loadingIcon.setVisibility(View.VISIBLE);
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ResultStatus<SentWorkout> resultStatus = this.workoutRepository.getReceivedWorkout(sentWorkoutId);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                if (this.isResumed()) {
                    // not sure if this does what i think it does? But would prevent potential memory leaks
                    loadingIcon.setVisibility(View.GONE);
                    if (resultStatus.isSuccess()) {
                        mainLayout.setVisibility(View.VISIBLE);
                        sentWorkout = resultStatus.getData();
                        sentRoutine = sentWorkout.getRoutine();
                        setupButtons();
                        updateRoutineListUI();
                    } else {
                        ErrorDialog.showErrorDialog("Load Received Workouts Error", resultStatus.getErrorMessage(), getContext());
                    }
                }
            });
        });
    }

    private void setupButtons() {
        /*
            Setup button listeners.
         */
        dayTV.setOnClickListener(v -> jumpDaysPopup());
        backButton.setOnClickListener(v -> {
            if (currentDayIndex > 0) {
                // if on this week there are more days, just decrease the current day index
                currentDayIndex--;
                updateRoutineListUI();
            } else if (currentWeekIndex > 0) {
                // there are more previous weeks
                currentWeekIndex--;
                currentDayIndex = sentRoutine.getWeek(currentWeekIndex).getNumberOfDays() - 1;
                updateRoutineListUI();
            }
        });
        forwardButton.setOnClickListener(v -> {
            if (currentDayIndex + 1 < sentRoutine.getWeek(currentWeekIndex).getNumberOfDays()) {
                // if can progress further in this week, do so
                currentDayIndex++;
                updateRoutineListUI();
            } else if (currentWeekIndex + 1 < sentRoutine.getNumberOfWeeks()) {
                // there are more weeks
                currentDayIndex = 0;
                currentWeekIndex++;
                updateRoutineListUI();
            }
        });
    }

    private void updateButtonViews() {
        /*
            Updates the visibility and icon of the navigation buttons depending on the current day.
         */
        if (currentDayIndex == 0 && currentWeekIndex == 0) {
            // means it's the first day in weeks, so hide the back button
            backButton.setVisibility(View.INVISIBLE);
            forwardButton.setVisibility(View.VISIBLE);
            forwardButton.setImageResource(R.drawable.next_icon);
            if (currentWeekIndex + 1 == sentRoutine.getNumberOfWeeks() && sentRoutine.getWeek(currentWeekIndex).getNumberOfDays() == 1) {
                // a one day workout
                forwardButton.setVisibility(View.INVISIBLE);
            }
        } else if (currentWeekIndex + 1 == sentRoutine.getNumberOfWeeks()
                && currentDayIndex + 1 == sentRoutine.getWeek(currentWeekIndex).getNumberOfDays()) {
            // last day, so hide forward button
            backButton.setVisibility(View.VISIBLE);
            // lil hacky, but don't want the ripple showing when the icons switch
            forwardButton.setVisibility(View.INVISIBLE);
        } else if (currentWeekIndex < sentRoutine.getNumberOfWeeks()) {
            // not first day, not last. So show back and forward button
            backButton.setVisibility(View.VISIBLE);
            forwardButton.setVisibility(View.VISIBLE);
            forwardButton.setImageResource(R.drawable.next_icon);
        }
    }

    private void updateRoutineListUI() {
        /*
            Updates the list of displayed exercises in the workout depending on the current day.
         */
        boolean metricUnits = user.getUserPreferences().isMetricUnits();

        SentRoutineAdapter routineAdapter = new SentRoutineAdapter(sentRoutine.getExerciseListForDay(currentWeekIndex, currentDayIndex), metricUnits);
        recyclerView.setAdapter(routineAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        dayTV.setText(WorkoutHelper.generateDayTitleNew(currentWeekIndex, currentDayIndex));
        updateButtonViews();
    }

    private void jumpDaysPopup() {
        /*
            Allow the user to scroll through the list of days to quickly jump around in workout
         */
        int totalDays = 0;
        int selectedVal = 0;
        List<String> days = new ArrayList<>();
        for (Integer week : sentRoutine) {
            for (Integer day : sentRoutine.getWeek(week)) {
                if (week == currentWeekIndex && day == currentDayIndex) {
                    selectedVal = totalDays;
                }
                String dayTitle = WorkoutHelper.generateDayTitleNew(week, day);
                days.add(dayTitle);
                totalDays++;
            }
        }
        String[] daysAsArray = new String[totalDays];
        for (int i = 0; i < totalDays; i++) {
            daysAsArray[i] = days.get(i);
        }
        View popupView = getLayoutInflater().inflate(R.layout.popup_jump_days, null);
        final NumberPicker dayPicker = popupView.findViewById(R.id.day_picker);
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
                    for (Integer week : sentRoutine) {
                        for (Integer day : sentRoutine.getWeek(week)) {
                            if (count == dayPicker.getValue()) {
                                currentWeekIndex = week;
                                currentDayIndex = day;
                            }
                            count++;
                        }
                    }
                    updateRoutineListUI();
                })
                .create();
        alertDialog.show();
    }

    @Override
    public void hideAllDialogs() {
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }

}
