package com.joshrap.liteweight.fragments;

import androidx.appcompat.app.AlertDialog;

import android.app.NotificationManager;
import android.content.Context;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.activities.MainActivity;
import com.joshrap.liteweight.adapters.SharedRoutineAdapter;
import com.joshrap.liteweight.managers.UserManager;
import com.joshrap.liteweight.managers.WorkoutManager;
import com.joshrap.liteweight.messages.fragmentmessages.ReceivedWorkoutFragmentMessage;
import com.joshrap.liteweight.models.SharedExercise;
import com.joshrap.liteweight.models.SharedWeek;
import com.joshrap.liteweight.models.WorkoutMeta;
import com.joshrap.liteweight.providers.CurrentUserAndWorkoutProvider;
import com.joshrap.liteweight.utils.AndroidUtils;
import com.joshrap.liteweight.utils.ValidatorUtils;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import static android.os.Looper.getMainLooper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class BrowseReceivedWorkoutFragment extends Fragment implements FragmentWithDialog {
    private User user;
    private ProgressBar loadingIcon;
    private RecyclerView browseRecyclerView;
    private SharedWorkout sharedWorkout;
    private SharedRoutine sharedRoutine;
    private TextView dayTV, dayTagTV;
    private String workoutName;
    private Button forwardButton, backButton;
    private int currentDayIndex;
    private int currentWeekIndex;
    private AlertDialog alertDialog;
    private RelativeLayout browseContainer;
    private String receivedWorkoutId;

    private enum AnimationDirection {NONE, FROM_LEFT, FROM_RIGHT}

    @Inject
    WorkoutManager workoutManager;
    @Inject
    UserManager userManager;
    @Inject
    CurrentUserAndWorkoutProvider currentUserAndWorkoutProvider;
    @Inject
    AlertDialog loadingDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        Injector.getInjector(getContext()).inject(this);

        receivedWorkoutId = null;
        if (getArguments() != null) {
            receivedWorkoutId = getArguments().getString(SharedWorkout.SHARED_WORKOUT_ID);
            workoutName = getArguments().getString(SharedWorkout.WORKOUT_NAME);
        } else {
            return null;
        }
        ((MainActivity) getActivity()).updateToolbarTitle(workoutName);
        ((MainActivity) getActivity()).toggleBackButton(true);

        user = currentUserAndWorkoutProvider.provideCurrentUser();
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

        Button respondIcon = view.findViewById(R.id.respond_btn);
        final PopupMenu dropDownRoutineDayMenu = new PopupMenu(getContext(), respondIcon);
        Menu moreMenu = dropDownRoutineDayMenu.getMenu();
        final int acceptWorkoutId = 0;
        final int declineWorkoutId = 1;
        moreMenu.add(0, acceptWorkoutId, 0, "Accept Workout");
        moreMenu.add(0, declineWorkoutId, 0, "Decline Workout");

        dropDownRoutineDayMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case acceptWorkoutId:
                    boolean workoutNameExists = user.getWorkoutMetas().values().stream().anyMatch(x -> x.getWorkoutName().equals(workoutName));
                    if (workoutNameExists) {
                        workoutNameAlreadyExistsPopup(sharedWorkout);
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
        respondIcon.setOnClickListener(v -> dropDownRoutineDayMenu.show());

        getReceivedWorkout(receivedWorkoutId);
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void hideAllDialogs() {
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleReceivedWorkoutMessage(ReceivedWorkoutFragmentMessage message) {
        SharedWorkoutMeta sharedWorkoutMeta = message.getSharedWorkoutMeta();

        // if id matches the one on this page, get rid of push notification
        NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null && sharedWorkoutMeta.getWorkoutId().equals(receivedWorkoutId)) {
            notificationManager.cancel(sharedWorkoutMeta.getWorkoutId().hashCode());
        }

        if (sharedWorkoutMeta.getWorkoutId().equals(receivedWorkoutId)) {
            workoutUpdatedPopup(sharedWorkoutMeta);
        }
    }

    private void workoutUpdatedPopup(SharedWorkoutMeta sharedWorkoutMeta) {
        alertDialog = new AlertDialog.Builder(getContext())
                .setTitle("Workout updated")
                .setMessage(String.format("%s has sent a newer version of this workout. Would you like to refresh in order to see the changes?", sharedWorkoutMeta.getSender()))
                .setPositiveButton("Yes", (dialogInterface, i) -> {
                    currentDayIndex = 0;
                    currentWeekIndex = 0;
                    // user has acknowledged this update, so mark it as seen
                    SharedWorkoutMeta workoutMeta = user.getReceivedWorkout(receivedWorkoutId);
                    workoutMeta.setSeen(true);
                    setReceivedWorkoutSeen(receivedWorkoutId);

                    getReceivedWorkout(receivedWorkoutId);
                })
                .setNegativeButton("No", null)
                .create();
        alertDialog.show();
    }

    private void setReceivedWorkoutSeen(String workoutId) {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            this.userManager.setReceivedWorkoutSeen(workoutId);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> ((MainActivity) getActivity()).updateReceivedWorkoutNotificationIndicator());
        });
    }

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

        alertDialog = new AlertDialog.Builder(getContext())
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
                for (WorkoutMeta workoutMeta : user.getWorkoutMetas().values()) {
                    workoutNames.add(workoutMeta.getWorkoutName());
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
            ResultStatus<AcceptWorkoutResponse> resultStatus = this.workoutManager.acceptReceivedWorkout(receivedWorkoutId, optionalName);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                loadingDialog.dismiss();
                if (resultStatus.isSuccess()) {
                    ((MainActivity) getActivity()).updateReceivedWorkoutNotificationIndicator();
                    ((MainActivity) getActivity()).finishFragment();
                } else {
                    AndroidUtils.showErrorDialog(resultStatus.getErrorMessage(), getContext());
                }
            });
        });
    }

    private void declineWorkout(String receivedWorkoutId) {
        AndroidUtils.showLoadingDialog(loadingDialog, "Declining...");
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ResultStatus<String> resultStatus = this.workoutManager.declineReceivedWorkout(receivedWorkoutId);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                loadingDialog.dismiss();
                if (resultStatus.isSuccess()) {
                    // if it was unread, then we need to make sure to decrease unseen count
                    ((MainActivity) getActivity()).updateReceivedWorkoutNotificationIndicator();
                    ((MainActivity) getActivity()).finishFragment();
                }
            });
        });
    }

    private void getReceivedWorkout(String sharedWorkoutId) {
        browseContainer.setVisibility(View.GONE);
        loadingIcon.setVisibility(View.VISIBLE);
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ResultStatus<SharedWorkout> resultStatus = this.workoutManager.getReceivedWorkout(sharedWorkoutId);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                if (this.isResumed()) {
                    loadingIcon.setVisibility(View.GONE);
                    if (resultStatus.isSuccess()) {
                        browseContainer.setVisibility(View.VISIBLE);
                        sharedWorkout = resultStatus.getData();
                        sharedRoutine = sharedWorkout.getRoutine();
                        setupButtons();
                        updateRoutineListUI(AnimationDirection.NONE);
                    } else {
                        AndroidUtils.showErrorDialog(resultStatus.getErrorMessage(), getContext());
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
        boolean metricUnits = user.getUserPreferences().isMetricUnits();

        List<SharedRoutineAdapter.SharedRoutineRowModel> sharedRoutineRowModels = new ArrayList<>();
        for (SharedExercise exercise : sharedRoutine.getExerciseListForDay(currentWeekIndex, currentDayIndex)) {
            SharedRoutineAdapter.SharedRoutineRowModel exerciseRowModel = new SharedRoutineAdapter.SharedRoutineRowModel(exercise, false);
            sharedRoutineRowModels.add(exerciseRowModel);
        }

        SharedRoutineAdapter routineAdapter = new SharedRoutineAdapter(sharedRoutineRowModels, metricUnits);
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
        dayTagTV.setText(dayTag + " "); // android cuts off italics on wrap content without trailing whitespace
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

        alertDialog = new AlertDialog.Builder(getContext())
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
