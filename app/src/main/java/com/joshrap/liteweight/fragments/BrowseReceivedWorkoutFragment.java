package com.joshrap.liteweight.fragments;

import androidx.appcompat.app.AlertDialog;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.activities.MainActivity;
import com.joshrap.liteweight.adapters.SharedRoutineAdapter;
import com.joshrap.liteweight.managers.SharedWorkoutManager;
import com.joshrap.liteweight.managers.UserManager;
import com.joshrap.liteweight.managers.WorkoutManager;
import com.joshrap.liteweight.models.sharedWorkout.SharedExercise;
import com.joshrap.liteweight.models.sharedWorkout.SharedWeek;
import com.joshrap.liteweight.models.user.WorkoutInfo;
import com.joshrap.liteweight.managers.CurrentUserModule;
import com.joshrap.liteweight.utils.AndroidUtils;
import com.joshrap.liteweight.utils.ValidatorUtils;
import com.joshrap.liteweight.utils.WorkoutUtils;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.interfaces.FragmentWithDialog;
import com.joshrap.liteweight.models.Result;
import com.joshrap.liteweight.models.sharedWorkout.SharedRoutine;
import com.joshrap.liteweight.models.sharedWorkout.SharedWorkout;
import com.joshrap.liteweight.models.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import static android.os.Looper.getMainLooper;

public class BrowseReceivedWorkoutFragment extends Fragment implements FragmentWithDialog {
    private ProgressBar loadingIcon;
    private RecyclerView browseRecyclerView;
    private SharedRoutine sharedRoutine;
    private TextView dayTV, dayTagTV;
    private String workoutName;
    private Button forwardButton, backButton;
    private int currentDayIndex;
    private int currentWeekIndex;
    private AlertDialog alertDialog;
    private RelativeLayout browseContainer;
    private String receivedWorkoutId;
    private boolean isMetricUnits;
    private final List<String> existingWorkoutNames = new ArrayList<>();

    private enum AnimationDirection {NONE, FROM_LEFT, FROM_RIGHT}

    @Inject
    WorkoutManager workoutManager;
    @Inject
    SharedWorkoutManager sharedWorkoutManager;
    @Inject
    UserManager userManager;
    @Inject
    CurrentUserModule currentUserModule;
    @Inject
    AlertDialog loadingDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentActivity activity = requireActivity();
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        Injector.getInjector(getContext()).inject(this);

        receivedWorkoutId = null;
        if (getArguments() != null) {
            receivedWorkoutId = getArguments().getString(Variables.SHARED_WORKOUT_ID);
            workoutName = getArguments().getString(Variables.WORKOUT_NAME);
        } else {
            return null;
        }
        ((MainActivity) activity).updateToolbarTitle(workoutName);
        ((MainActivity) activity).toggleBackButton(true);

        User user = currentUserModule.getUser();
        for (WorkoutInfo workoutInfo : user.getWorkouts()) {
            existingWorkoutNames.add(workoutInfo.getWorkoutName());
        }
        isMetricUnits = user.getSettings().isMetricUnits();
        currentDayIndex = 0;
        currentWeekIndex = 0;
        View view = inflater.inflate(R.layout.fragment_browse_received_workout, container, false);

        loadingIcon = view.findViewById(R.id.loading_progress_bar);
        browseRecyclerView = view.findViewById(R.id.browse_recycler_view);
        browseContainer = view.findViewById(R.id.browse_container);
        dayTV = view.findViewById(R.id.day_title_tv);
        dayTagTV = view.findViewById(R.id.day_tag_tv);
        forwardButton = view.findViewById(R.id.next_day_btn);
        backButton = view.findViewById(R.id.previous_day_btn);

        Button respondButton = view.findViewById(R.id.respond_btn);
        final PopupMenu dropDownRoutineDayMenu = new PopupMenu(getContext(), respondButton);
        Menu moreMenu = dropDownRoutineDayMenu.getMenu();
        final int acceptWorkoutId = 0;
        final int declineWorkoutId = 1;
        moreMenu.add(0, acceptWorkoutId, 0, "Accept Workout");
        moreMenu.add(0, declineWorkoutId, 0, "Decline Workout");

        dropDownRoutineDayMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case acceptWorkoutId:
                    boolean workoutNameExists = user.getWorkouts().stream().anyMatch(x -> x.getWorkoutName().equals(workoutName));
                    if (workoutNameExists) {
                        workoutNameAlreadyExistsPopup();
                    } else {
                        acceptWorkout(null);
                    }
                    return true;
                case declineWorkoutId:
                    declineWorkout(receivedWorkoutId);
                    return true;
            }
            return false;
        });
        respondButton.setOnClickListener(v -> dropDownRoutineDayMenu.show());

        getReceivedWorkout(receivedWorkoutId);
        return view;
    }

    @Override
    public void hideAllDialogs() {
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }

    private void workoutNameAlreadyExistsPopup() {
        View popupView = getLayoutInflater().inflate(R.layout.popup_workout_name_exists, null);
        EditText renameInput = popupView.findViewById(R.id.rename_workout_name_input);
        TextInputLayout workoutNameInputLayout = popupView.findViewById(R.id.rename_workout_name_input_layout);
        renameInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_WORKOUT_NAME)});
        renameInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(workoutNameInputLayout));

        // workout name is italicized
        SpannableString span1 = new SpannableString(workoutName);
        SpannableString span2 = new SpannableString(" already exists");
        span1.setSpan(new StyleSpan(Typeface.ITALIC), 0, span1.length(), 0);
        CharSequence title = TextUtils.concat(span1, span2);

        alertDialog = new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setView(popupView)
                .setPositiveButton("Submit", null)
                .setNegativeButton("Cancel", null)
                .create();
        alertDialog.setOnShowListener(dialogInterface -> {
            Button saveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            saveButton.setOnClickListener(view -> {
                String newName = renameInput.getText().toString().trim();
                String errorMsg = ValidatorUtils.validWorkoutName(newName, existingWorkoutNames);
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
            Result<String> result = this.sharedWorkoutManager.acceptReceivedWorkout(receivedWorkoutId, optionalName);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                loadingDialog.dismiss();
                if (result.isSuccess()) {
                    ((MainActivity) requireActivity()).updateReceivedWorkoutNotificationIndicator();
                    ((MainActivity) requireActivity()).finishFragment();
                } else {
                    AndroidUtils.showErrorDialog(result.getErrorMessage(), getContext());
                }
            });
        });
    }

    private void declineWorkout(String receivedWorkoutId) {
        AndroidUtils.showLoadingDialog(loadingDialog, "Declining...");
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Result<String> result = this.sharedWorkoutManager.declineReceivedWorkout(receivedWorkoutId);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                loadingDialog.dismiss();
                if (result.isSuccess()) {
                    // if it was unread, then we need to make sure to decrease unseen count
                    ((MainActivity) requireActivity()).updateReceivedWorkoutNotificationIndicator();
                    ((MainActivity) requireActivity()).finishFragment();
                }
            });
        });
    }

    private void getReceivedWorkout(String sharedWorkoutId) {
        browseContainer.setVisibility(View.GONE);
        loadingIcon.setVisibility(View.VISIBLE);
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Result<SharedWorkout> result = this.sharedWorkoutManager.getReceivedWorkout(sharedWorkoutId);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                if (this.isResumed()) {
                    loadingIcon.setVisibility(View.GONE);
                    if (result.isSuccess()) {
                        browseContainer.setVisibility(View.VISIBLE);
                        sharedRoutine = result.getData().getRoutine();
                        setupButtons();
                        updateRoutineListUI(AnimationDirection.NONE);
                    } else {
                        AndroidUtils.showErrorDialog(result.getErrorMessage(), getContext());
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
        dayTagTV.setOnClickListener(v -> jumpDaysPopup());
        backButton.setOnClickListener(v -> {
            if (currentDayIndex > 0) {
                // if on this week there are more days, just decrease the current day index
                currentDayIndex--;
                updateRoutineListUI(AnimationDirection.FROM_LEFT);
            } else if (currentWeekIndex > 0) {
                // there are more previous weeks
                currentWeekIndex--;
                currentDayIndex = sharedRoutine.getWeek(currentWeekIndex).getNumberOfDays() - 1;
                updateRoutineListUI(AnimationDirection.FROM_LEFT);
            }
        });
        forwardButton.setOnClickListener(v -> {
            if (currentDayIndex + 1 < sharedRoutine.getWeek(currentWeekIndex).getNumberOfDays()) {
                // if can progress further in this week, do so
                currentDayIndex++;
                updateRoutineListUI(AnimationDirection.FROM_RIGHT);
            } else if (currentWeekIndex + 1 < sharedRoutine.getNumberOfWeeks()) {
                // there are more weeks
                currentDayIndex = 0;
                currentWeekIndex++;
                updateRoutineListUI(AnimationDirection.FROM_RIGHT);
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
            if (currentWeekIndex + 1 == sharedRoutine.getNumberOfWeeks() && sharedRoutine.getWeek(currentWeekIndex).getNumberOfDays() == 1) {
                // a one day workout
                forwardButton.setVisibility(View.INVISIBLE);
            }
        } else if (currentWeekIndex + 1 == sharedRoutine.getNumberOfWeeks()
                && currentDayIndex + 1 == sharedRoutine.getWeek(currentWeekIndex).getNumberOfDays()) {
            // last day, so hide forward button
            backButton.setVisibility(View.VISIBLE);
            forwardButton.setVisibility(View.INVISIBLE);
        } else if (currentWeekIndex < sharedRoutine.getNumberOfWeeks()) {
            // not first day, not last. So show back and forward button
            backButton.setVisibility(View.VISIBLE);
            forwardButton.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Updates the list of displayed exercises in the workout depending on the current day.
     */
    private void updateRoutineListUI(AnimationDirection animationDirection) {
        List<SharedRoutineAdapter.SharedRoutineRowModel> sharedRoutineRowModels = new ArrayList<>();
        for (SharedExercise exercise : sharedRoutine.getExerciseListForDay(currentWeekIndex, currentDayIndex)) {
            SharedRoutineAdapter.SharedRoutineRowModel exerciseRowModel = new SharedRoutineAdapter.SharedRoutineRowModel(exercise, false);
            sharedRoutineRowModels.add(exerciseRowModel);
        }

        SharedRoutineAdapter routineAdapter = new SharedRoutineAdapter(sharedRoutineRowModels, isMetricUnits);
        browseRecyclerView.setAdapter(routineAdapter);
        browseRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        LayoutAnimationController animation = null;
        switch (animationDirection) {
            case FROM_LEFT:
                animation = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_animation_from_left);
                break;
            case FROM_RIGHT:
                animation = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_animation_from_right);
                break;
        }

        if (animation != null) {
            browseRecyclerView.setLayoutAnimation(animation);
        }

        dayTV.setText(WorkoutUtils.generateDayTitle(currentWeekIndex, currentDayIndex));
        String dayTag = sharedRoutine.getDay(currentWeekIndex, currentDayIndex).getTag();
        dayTagTV.setVisibility(dayTag == null ? View.INVISIBLE : View.VISIBLE);
        dayTagTV.setText(dayTag);
        updateButtonViews();
    }

    /**
     * Allow the user to scroll through the list of days to quickly jump around in workout.
     */
    private void jumpDaysPopup() {
        int totalDays = 0;
        int selectedVal = 0;
        List<String> days = new ArrayList<>();
        for (int weekIndex = 0; weekIndex < sharedRoutine.getNumberOfWeeks(); weekIndex++) {
            SharedWeek week = sharedRoutine.getWeek(weekIndex);
            for (int dayIndex = 0; dayIndex < week.getNumberOfDays(); dayIndex++) {
                if (weekIndex == currentWeekIndex && dayIndex == currentDayIndex) {
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

        alertDialog = new AlertDialog.Builder(requireContext())
                .setTitle("Jump to Day")
                .setView(popupView)
                .setPositiveButton("Go", (dialog, which) -> {
                    int count = 0;
                    for (int weekIndex = 0; weekIndex < sharedRoutine.getNumberOfWeeks(); weekIndex++) {
                        SharedWeek week = sharedRoutine.getWeek(weekIndex);
                        for (int dayIndex = 0; dayIndex < week.getNumberOfDays(); dayIndex++) {
                            if (count == dayPicker.getValue()) {
                                currentWeekIndex = weekIndex;
                                currentDayIndex = dayIndex;
                            }
                            count++;
                        }
                    }
                    updateRoutineListUI(AnimationDirection.FROM_RIGHT);
                })
                .create();
        alertDialog.show();
    }

}
