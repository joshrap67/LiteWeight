package com.joshrap.liteweight.fragments;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.activities.WorkoutActivity;
import com.joshrap.liteweight.adapters.SharedRoutineAdapter;
import com.joshrap.liteweight.utils.AndroidUtils;
import com.joshrap.liteweight.utils.ValidatorUtils;
import com.joshrap.liteweight.utils.JsonUtils;
import com.joshrap.liteweight.utils.WorkoutUtils;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.interfaces.FragmentWithDialog;
import com.joshrap.liteweight.models.AcceptWorkoutResponse;
import com.joshrap.liteweight.models.SharedWorkoutMeta;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.models.SharedRoutine;
import com.joshrap.liteweight.models.SharedWorkout;
import com.joshrap.liteweight.models.User;
import com.joshrap.liteweight.models.UserWithWorkout;
import com.joshrap.liteweight.network.repos.WorkoutRepository;

import java.io.IOException;
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
    private SharedWorkout sharedWorkout;
    private SharedRoutine sharedRoutine;
    private TextView dayTV;
    private String workoutName;
    private ImageButton forwardButton, backButton;
    private int currentDayIndex;
    private int currentWeekIndex;
    private AlertDialog alertDialog;
    private ConstraintLayout mainLayout;
    private String receivedWorkoutId;
    private UserWithWorkout userWithWorkout;
    @Inject
    WorkoutRepository workoutRepository;
    @Inject
    ProgressDialog loadingDialog;

    private final BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                return;
            }
            if (action.equals(Variables.RECEIVED_WORKOUT_MODEL_UPDATED_BROADCAST)) {
                try {
                    SharedWorkoutMeta sharedWorkoutMeta = new SharedWorkoutMeta(JsonUtils.deserialize((String) intent.getExtras().get(Variables.INTENT_NOTIFICATION_DATA)));
                    // if id matches the one on this page, get rid of push notification
                    NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                    if (notificationManager != null && sharedWorkoutMeta.getWorkoutId().equals(receivedWorkoutId)) {
                        notificationManager.cancel(sharedWorkoutMeta.getWorkoutId().hashCode());
                    }
                    if (sharedWorkoutMeta.getWorkoutId().equals(receivedWorkoutId)) {
                        workoutUpdatedPopup(sharedWorkoutMeta);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Injector.getInjector(getContext()).inject(this);

        receivedWorkoutId = null;
        if (getArguments() != null) {
            receivedWorkoutId = getArguments().getString(SharedWorkout.SHARED_WORKOUT_ID);
            workoutName = getArguments().getString(SharedWorkout.WORKOUT_NAME);
        } else {
            return null;
        }
        ((WorkoutActivity) getActivity()).updateToolbarTitle(workoutName);
        ((WorkoutActivity) getActivity()).toggleBackButton(true);

        IntentFilter receiverActions = new IntentFilter();
        receiverActions.addAction(Variables.RECEIVED_WORKOUT_MODEL_UPDATED_BROADCAST);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(notificationReceiver, receiverActions);

        userWithWorkout = ((WorkoutActivity) getActivity()).getUserWithWorkout();
        user = userWithWorkout.getUser();
        currentDayIndex = 0;
        currentWeekIndex = 0;
        View view = inflater.inflate(R.layout.fragment_browse_received_workout, container, false);

        loadingIcon = view.findViewById(R.id.loading_icon);
        recyclerView = view.findViewById(R.id.recycler_view);
        mainLayout = view.findViewById(R.id.main_layout);
        dayTV = view.findViewById(R.id.day_text_view);
        forwardButton = view.findViewById(R.id.next_day_button);
        backButton = view.findViewById(R.id.previous_day_button);
        Button acceptWorkoutButton = view.findViewById(R.id.accept_workout_btn);
        acceptWorkoutButton.setOnClickListener(view1 -> {
            boolean workoutNameExists = false;
            for (String workoutId : user.getWorkoutMetas().keySet()) {
                if (user.getWorkoutMetas().get(workoutId).getWorkoutName().equals(workoutName)) {
                    workoutNameExists = true;
                    break;
                }
            }
            if (workoutNameExists) {
                workoutNameAlreadyExistsPopup(sharedWorkout);
            } else {
                acceptWorkout(null);
            }
        });
        Button declineWorkoutButton = view.findViewById(R.id.decline_workout_btn);
        declineWorkoutButton.setOnClickListener(view1 -> declineWorkout(receivedWorkoutId));

        getReceivedWorkout(receivedWorkoutId);
        return view;
    }

    @Override
    public void onPause() {
        hideAllDialogs();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(notificationReceiver);
        super.onPause();
    }

    @Override
    public void hideAllDialogs() {
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }

    private void workoutUpdatedPopup(SharedWorkoutMeta sharedWorkoutMeta) {
        alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                .setTitle("Workout updated")
                .setMessage(String.format("%s has sent a newer version of this workout. Would you like to refresh in order to see the changes?", sharedWorkoutMeta.getSender()))
                .setPositiveButton("Yes", (dialogInterface, i) -> {
                    currentDayIndex = 0;
                    currentWeekIndex = 0;
                    getReceivedWorkout(receivedWorkoutId);
                })
                .setNegativeButton("No", null)
                .create();
        alertDialog.show();
    }

    /**
     * Prompt the user if they want to rename the current workout.
     *
     * @param receivedWorkout workout that is currently being browsed.
     */
    private void workoutNameAlreadyExistsPopup(final SharedWorkout receivedWorkout) {
        View popupView = getLayoutInflater().inflate(R.layout.popup_workout_name_exists, null);
        EditText renameInput = popupView.findViewById(R.id.rename_workout_name_input);
        TextInputLayout workoutNameInputLayout = popupView.findViewById(R.id.rename_workout_name_input_layout);
        renameInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_WORKOUT_NAME)});
        renameInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(workoutNameInputLayout));

        // workout name is italicized
        SpannableString span1 = new SpannableString(receivedWorkout.getWorkoutName());
        SpannableString span2 = new SpannableString(" already exists");
        span1.setSpan(new StyleSpan(Typeface.ITALIC), 0, span1.length(), 0);
        CharSequence title = TextUtils.concat(span1, span2);

        alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                .setTitle(title)
                .setView(popupView)
                .setPositiveButton("Submit", null)
                .setNegativeButton("Cancel", null)
                .create();
        alertDialog.setOnShowListener(dialogInterface -> {
            Button saveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            saveButton.setOnClickListener(view -> {
                String newName = renameInput.getText().toString().trim();
                List<String> workoutNames = new ArrayList<>();
                for (String workoutId : user.getWorkoutMetas().keySet()) {
                    workoutNames.add(user.getWorkoutMetas().get(workoutId).getWorkoutName());
                }
                String errorMsg = ValidatorUtils.validWorkoutName(newName, workoutNames);
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

    private void acceptWorkout(String optionalName) {
        AndroidUtils.showLoadingDialog(loadingDialog, "Accepting...");

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
                        userWithWorkout.setWorkout(response.getWorkout());
                    }
                    user.getWorkoutMetas().put(response.getWorkoutId(), response.getWorkoutMeta());
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
                    AndroidUtils.showErrorDialog("Error", resultStatus.getErrorMessage(), getContext());
                }
            });
        });
    }

    private void declineWorkout(String receivedWorkoutId) {
        AndroidUtils.showLoadingDialog(loadingDialog, "Declining...");
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ResultStatus<String> resultStatus = this.workoutRepository.declineReceivedWorkout(receivedWorkoutId);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                loadingDialog.dismiss();
                if (resultStatus.isSuccess()) {
                    SharedWorkoutMeta sharedWorkoutMeta = user.getReceivedWorkouts().get(receivedWorkoutId);
                    user.getReceivedWorkouts().remove(receivedWorkoutId);
                    if (!sharedWorkoutMeta.isSeen()) {
                        // if it was unread, then we need to make sure to decrease unseen count
                        user.setUnseenReceivedWorkouts(user.getUnseenReceivedWorkouts() - 1);
                        ((WorkoutActivity) getActivity()).updateReceivedWorkoutNotificationIndicator();
                    }
                    user.setTotalReceivedWorkouts(user.getTotalReceivedWorkouts() - 1);
                    ((WorkoutActivity) getActivity()).finishFragment();
                }
            });
        });
    }

    private void getReceivedWorkout(String sharedWorkoutId) {
        mainLayout.setVisibility(View.GONE);
        loadingIcon.setVisibility(View.VISIBLE);
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ResultStatus<SharedWorkout> resultStatus = this.workoutRepository.getReceivedWorkout(sharedWorkoutId);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                if (this.isResumed()) {
                    loadingIcon.setVisibility(View.GONE);
                    if (resultStatus.isSuccess()) {
                        mainLayout.setVisibility(View.VISIBLE);
                        sharedWorkout = resultStatus.getData();
                        sharedRoutine = sharedWorkout.getRoutine();
                        setupButtons();
                        updateRoutineListUI();
                    } else {
                        AndroidUtils.showErrorDialog("Load Workout Error", resultStatus.getErrorMessage(), getContext());
                    }
                }
            });
        });
    }

    /**
     * Sets up listeners for the forward and backwards buttons.
     */
    private void setupButtons() {
        dayTV.setOnClickListener(v -> jumpDaysPopup());
        backButton.setOnClickListener(v -> {
            if (currentDayIndex > 0) {
                // if on this week there are more days, just decrease the current day index
                currentDayIndex--;
                updateRoutineListUI();
            } else if (currentWeekIndex > 0) {
                // there are more previous weeks
                currentWeekIndex--;
                currentDayIndex = sharedRoutine.getWeek(currentWeekIndex).getNumberOfDays() - 1;
                updateRoutineListUI();
            }
        });
        forwardButton.setOnClickListener(v -> {
            if (currentDayIndex + 1 < sharedRoutine.getWeek(currentWeekIndex).getNumberOfDays()) {
                // if can progress further in this week, do so
                currentDayIndex++;
                updateRoutineListUI();
            } else if (currentWeekIndex + 1 < sharedRoutine.getNumberOfWeeks()) {
                // there are more weeks
                currentDayIndex = 0;
                currentWeekIndex++;
                updateRoutineListUI();
            }
        });
    }

    /**
     * Updates the visibility and icon of the navigation buttons depending on the current day.
     */
    private void updateButtonViews() {
        if (currentDayIndex == 0 && currentWeekIndex == 0) {
            // means it's the first day in weeks, so hide the back button
            backButton.setVisibility(View.INVISIBLE);
            forwardButton.setVisibility(View.VISIBLE);
            forwardButton.setImageResource(R.drawable.next_icon);
            if (currentWeekIndex + 1 == sharedRoutine.getNumberOfWeeks() && sharedRoutine.getWeek(currentWeekIndex).getNumberOfDays() == 1) {
                // a one day workout
                forwardButton.setVisibility(View.INVISIBLE);
            }
        } else if (currentWeekIndex + 1 == sharedRoutine.getNumberOfWeeks()
                && currentDayIndex + 1 == sharedRoutine.getWeek(currentWeekIndex).getNumberOfDays()) {
            // last day, so hide forward button
            backButton.setVisibility(View.VISIBLE);
            // lil hacky, but don't want the ripple showing when the icons switch
            forwardButton.setVisibility(View.INVISIBLE);
        } else if (currentWeekIndex < sharedRoutine.getNumberOfWeeks()) {
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
        boolean metricUnits = user.getUserPreferences().isMetricUnits();

        SharedRoutineAdapter routineAdapter = new SharedRoutineAdapter(sharedRoutine.getExerciseListForDay(currentWeekIndex, currentDayIndex), metricUnits, recyclerView, getContext());
        recyclerView.setAdapter(routineAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        dayTV.setText(WorkoutUtils.generateDayTitle(currentWeekIndex, currentDayIndex));
        updateButtonViews();
    }

    /**
     * Allow the user to scroll through the list of days to quickly jump around in workout.
     */
    private void jumpDaysPopup() {
        int totalDays = 0;
        int selectedVal = 0;
        List<String> days = new ArrayList<>();
        for (Integer week : sharedRoutine) {
            for (Integer day : sharedRoutine.getWeek(week)) {
                if (week == currentWeekIndex && day == currentDayIndex) {
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
                    for (Integer week : sharedRoutine) {
                        for (Integer day : sharedRoutine.getWeek(week)) {
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

}
