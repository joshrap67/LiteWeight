package com.joshrap.liteweight.fragments;

import android.app.AlertDialog;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;
import com.joshrap.liteweight.*;
import com.joshrap.liteweight.activities.WorkoutActivity;
import com.joshrap.liteweight.helpers.InputHelper;
import com.joshrap.liteweight.imports.Globals;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.interfaces.FragmentWithDialog;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.models.User;
import com.joshrap.liteweight.models.UserWithWorkout;
import com.joshrap.liteweight.models.Workout;
import com.joshrap.liteweight.models.WorkoutUser;
import com.joshrap.liteweight.network.repos.WorkoutRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static android.os.Looper.getMainLooper;

public class MyWorkoutsFragment extends Fragment implements FragmentWithDialog {
    private TextView selectedWorkoutTV, statisticsTV;
    private ListView workoutListView;
    private AlertDialog alertDialog;
    private ArrayAdapter<String> arrayAdapter;
    private HashMap<String, String> workoutNameToId = new HashMap<>();
    private ArrayList<String> workoutNames = new ArrayList<>();
    private User user;
    private Workout currentWorkout;
    private WorkoutUser currentWorkoutMeta;
    private ProgressDialog loadingDialog;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // TODO injection or view model for these two???
        currentWorkout = Globals.activeWorkout;
        user = Globals.user;
        ((WorkoutActivity) getActivity()).updateToolbarTitle(Variables.MY_WORKOUT_TITLE);

        View view;
        if (currentWorkout == null) {
            view = inflater.inflate(R.layout.default_layout, container, false);
        } else {
            view = inflater.inflate(R.layout.fragment_my_workouts, container, false);
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (currentWorkout == null) {
            FloatingActionButton createWorkoutBtn = view.findViewById(R.id.create_workout_btn);
            createWorkoutBtn.setOnClickListener(v -> ((WorkoutActivity) getActivity()).goToNewWorkout());
            return;
        }
        loadingDialog = new ProgressDialog(getActivity());
        loadingDialog.setCancelable(false);
        for (String workoutId : user.getUserWorkouts().keySet()) {
            if (workoutId.equals(user.getCurrentWorkout())) {
                currentWorkoutMeta = user.getUserWorkouts().get(workoutId);
            }
            workoutNameToId.put(user.getUserWorkouts().get(workoutId).getWorkoutName(), workoutId);
            workoutNames.add(user.getUserWorkouts().get(workoutId).getWorkoutName());
        }
        initViews(view);
    }

    @Override
    public void hideAllDialogs() {
        /*
            Close any dialogs that might be showing. This is essential when clicking a notification that takes
            the user to a new page.
         */
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }

        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private void initViews(View view) {
        /*
            Once at least one workout is found, change layouts and initialize all views
         */

        ImageButton workoutOptionsButton = view.findViewById(R.id.workout_options_btn);
        final PopupMenu dropDownMenu = new PopupMenu(getContext(), workoutOptionsButton);
        final Menu menu = dropDownMenu.getMenu();
        final int editIndex = 0;
        final int renameIndex = 1;
        final int copyIndex = 2;
        final int resetIndex = 3;
        final int deleteIndex = 4;
        menu.add(0, editIndex, 0, "Edit Workout");
        menu.add(0, renameIndex, 0, "Rename Workout");
        menu.add(0, copyIndex, 0, "Copy Current Workout");
        menu.add(0, resetIndex, 0, "Reset Statistics");
        menu.add(0, deleteIndex, 0, "Delete Workout");

        dropDownMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case editIndex:
                    ((WorkoutActivity) getActivity()).goToEditWorkout();
                    return true;
                case renameIndex:
                    promptRename();
                    return true;
                case resetIndex:
                    promptResetStatistics();
                    return true;
                case deleteIndex:
                    promptDelete();
                    return true;
                case copyIndex:
                    if (user.getUserWorkouts().size() < Variables.MAX_NUMBER_OF_WORKOUTS) {
                        promptCopy();
                    } else {
                        showErrorMessage("Too many workouts", "Copying this workout would put you over the maximum amount of workouts you can own. Delete some of your other ones if you wish to copy this workout");
                    }
                    return true;
            }
            return false;
        });
        workoutOptionsButton.setOnClickListener(v -> dropDownMenu.show());

        workoutListView = view.findViewById(R.id.workout_list);
        selectedWorkoutTV = view.findViewById(R.id.selected_workout_text_view);
        statisticsTV = view.findViewById(R.id.stat_text_view);
        selectedWorkoutTV.setText(currentWorkout.getWorkoutName());
        updateStatisticsTV();

        sortWorkouts();
        // set up the create workout button
        FloatingActionButton createWorkoutBtn = view.findViewById(R.id.new_workout_btn);
        createWorkoutBtn.setOnClickListener(v -> ((WorkoutActivity) getActivity()).goToNewWorkout());

        // set up the list view
        arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_activated_1, workoutNames);
        workoutListView.setAdapter(arrayAdapter);
        workoutListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        workoutListView.setOnItemClickListener((parent, _view, position, id) ->
                switchWorkout(workoutNameToId.get(workoutListView.getItemAtPosition(position).toString())));
        workoutListView.setItemChecked(0, true); // programmatically select current workout in list
    }

    private void sortWorkouts() {
        /*
            Currently sorts by date last accessed
         */
        workoutNames.clear();
        List<WorkoutUser> workoutUsers = new ArrayList<>(Globals.user.getUserWorkouts().values());
        Collections.sort(workoutUsers, (o1, o2) -> o2.getDateLast().compareTo(o1.getDateLast()));
        for (WorkoutUser workoutUser : workoutUsers) {
            if (!workoutUser.getWorkoutName().equals(Globals.activeWorkout.getWorkoutName())) {
                workoutNames.add(workoutUser.getWorkoutName());
            }
        }
        workoutNames.add(0, Globals.activeWorkout.getWorkoutName()); // selected always on top
    }

    private void switchWorkout(String workoutId) {
        showLoadingDialog();
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ResultStatus<UserWithWorkout> resultStatus = WorkoutRepository.switchWorkout(currentWorkout, workoutId);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                if (this.isResumed()) {
                    // not sure if this does what i think it does? But would prevent potential memory leaks
                    loadingDialog.dismiss();
                    if (resultStatus.isSuccess()) {
                        // set new active workout
                        Globals.activeWorkout = resultStatus.getData().getWorkout();
                        currentWorkout = resultStatus.getData().getWorkout();
                        Globals.user.setCurrentWorkout(currentWorkout.getWorkoutId());
                        currentWorkoutMeta = resultStatus.getData().getUser().getUserWorkouts().get(currentWorkout.getWorkoutId());
                        Globals.user.getUserWorkouts().put(currentWorkout.getWorkoutId(), currentWorkoutMeta);

                        selectedWorkoutTV.setText(Globals.activeWorkout.getWorkoutName());
                        // since we only sort by date last modified, just put selected at index 1 to push other elements down
                        workoutNames.remove(resultStatus.getData().getWorkout().getWorkoutName());
                        workoutNames.add(0, resultStatus.getData().getWorkout().getWorkoutName());
                        arrayAdapter.notifyDataSetChanged();
                        updateStatisticsTV();
                        workoutListView.setItemChecked(0, true); // programmatically select current workout in list
                    } else {
                        showErrorMessage("Switch Workout Error", resultStatus.getErrorMessage());
                        workoutListView.setItemChecked(0, true);
                    }
                }
            });
        });
    }

    private void showLoadingDialog() {
        loadingDialog.setMessage("Loading...");
        loadingDialog.show();
    }

    private void updateStatisticsTV() {
        /*
            Displays statistics for the currently selected workout
         */
        int timesCompleted = currentWorkoutMeta.getTimesCompleted();
        double percentage = currentWorkoutMeta.getAverageExercisesCompleted();
        String formattedPercentage;
        if (percentage > 0.0 && percentage < 100.0) {
            formattedPercentage = String.format("%.3f", percentage) + "%";
        } else if (percentage == 0.0) {
            formattedPercentage = "0%";
        } else {
            formattedPercentage = "100%";
        }
        int days = 0;
        for (Integer week : currentWorkout.getRoutine().getRoutine().keySet()) {
            days += currentWorkout.getRoutine().getRoutine().get(week).keySet().size();
        }
        String msg = "Times Completed: " + timesCompleted + "\n" +
                "Average Percentage of Exercises Completed: " + formattedPercentage + "\n" +
                "Total Number of Days in Workout: " + days + "\n" +
                "Most Worked Focus: " + currentWorkout.getMostFrequentFocus().replaceAll(",", ", ");
        statisticsTV.setText(msg);
    }

    private void promptResetStatistics() {
        /*
            Prompt the user if they actually want to reset the selected workout's statistics
         */
        String message = "Are you sure you wish to reset the statistics for \"" +
                Globals.activeWorkout.getWorkoutName() + "\"?\n\n" +
                "Doing so will reset the times completed and the percentage of exercises completed.";
        alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                .setTitle("Reset Statistics")
                .setMessage(message)
                .setPositiveButton("Yes", (dialog, which) -> {
                    // TODO reset api
                    updateStatisticsTV();
                })
                .setNegativeButton("No", null)
                .create();
        alertDialog.show();
        // make the message font a little bigger than the default one provided by the alertdialog
        TextView messageTV = alertDialog.getWindow().findViewById(android.R.id.message);
        messageTV.setTextSize(18);
    }

    private void promptRename() {
        /*
            Prompt the user if they want to rename the current workout
         */
        View popupView = getActivity().getLayoutInflater().inflate(R.layout.popup_rename_workout, null);
        final EditText renameInput = popupView.findViewById(R.id.rename_workout_name_input);
        final TextInputLayout workoutNameInputLayout = popupView.findViewById(R.id.rename_workout_name_input_layout);
        renameInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_WORKOUT_NAME)});
        renameInput.addTextChangedListener(new TextWatcher() {
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
        alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                .setTitle("Rename \"" + currentWorkout.getWorkoutName() + "\"")
                .setView(popupView)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create();
        alertDialog.setOnShowListener(dialogInterface -> {
            Button saveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            saveButton.setOnClickListener(view -> {
                String oldName = currentWorkout.getWorkoutName();
                String newName = renameInput.getText().toString().trim();
                String errorMsg = InputHelper.validWorkoutName(newName, workoutNames);
                if (errorMsg != null) {
                    workoutNameInputLayout.setError(errorMsg);
                } else {
                    alertDialog.dismiss();
                    renameWorkout(workoutNameToId.get(currentWorkout.getWorkoutName()), newName, oldName);
                }
            });
        });
        alertDialog.show();

        // make the message font a little bigger than the default one provided by the alertdialog
        TextView messageTV = alertDialog.getWindow().findViewById(android.R.id.message);
        messageTV.setTextSize(18);
    }

    private void promptCopy() {
        View popupView = getActivity().getLayoutInflater().inflate(R.layout.popup_copy_workout, null);
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
                .setTitle(String.format("Copy \"%s\" as new workout", currentWorkoutMeta.getWorkoutName()))
                .setView(popupView)
                .setPositiveButton("Copy", null)
                .setNegativeButton("Cancel", null)
                .create();
        alertDialog.setOnShowListener(dialogInterface -> {
            Button saveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            saveButton.setOnClickListener(view -> {
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
                    copyWorkout(workoutName);
                }
            });
        });
        alertDialog.show();
    }

    private void promptDelete() {
        /*
            Prompt user if they actually want to delete the currently selected workout
         */
        String message = "Are you sure you wish to permanently delete \"" + currentWorkout.getWorkoutName() + "\"?" +
                "\n\nIf so, all statistics for it will also be deleted.";
        alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                .setTitle("Delete Workout")
                .setMessage(message)
                .setPositiveButton("Yes", (dialog, which) -> deleteWorkout(currentWorkout.getWorkoutId()))
                .setNegativeButton("No", null)
                .create();
        alertDialog.show();
        // make the message font a little bigger than the default one provided by the alertdialog
        TextView messageTV = alertDialog.getWindow().findViewById(android.R.id.message);
        messageTV.setTextSize(18);
    }

    private void renameWorkout(String workoutId, String newWorkoutName, String oldWorkoutName) {
        showLoadingDialog();
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ResultStatus<User> resultStatus = WorkoutRepository.renameWorkout(workoutId, newWorkoutName);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                loadingDialog.dismiss();
                if (resultStatus.isSuccess()) {
                    Globals.user = resultStatus.getData();
                    Globals.activeWorkout.setWorkoutName(newWorkoutName);

                    selectedWorkoutTV.setText(newWorkoutName);
                    workoutNameToId.remove(oldWorkoutName);
                    workoutNameToId.put(newWorkoutName, workoutId);
                    // since we only sort by date last modified, just newly copied workout at top of list
                    workoutNames.remove(oldWorkoutName);
                    workoutNames.add(0, newWorkoutName);
                    arrayAdapter.notifyDataSetChanged();
                    updateStatisticsTV();
                } else {
                    showErrorMessage("Rename Workout Error", resultStatus.getErrorMessage());
                }
            });
        });
    }

    private void copyWorkout(String workoutName) {
        showLoadingDialog();
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ResultStatus<UserWithWorkout> resultStatus = WorkoutRepository.copyWorkout(currentWorkout, workoutName);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                loadingDialog.dismiss();
                if (resultStatus.isSuccess()) {
                    Globals.user = resultStatus.getData().getUser();

                    currentWorkout = resultStatus.getData().getWorkout();
                    Globals.activeWorkout = currentWorkout;
                    currentWorkoutMeta = resultStatus.getData().getUser().getUserWorkouts().get(currentWorkout.getWorkoutId());

                    selectedWorkoutTV.setText(Globals.activeWorkout.getWorkoutName());
                    // since we only sort by date last modified, just newly copied workout at top of list
                    workoutNames.add(0, Globals.activeWorkout.getWorkoutName());
                    arrayAdapter.notifyDataSetChanged();
                    updateStatisticsTV();
                    workoutListView.setItemChecked(0, true); // programmatically select current workout in list

                } else {
                    showErrorMessage("Copy Workout Error", resultStatus.getErrorMessage());
                }
            });
        });
    }

    private void deleteWorkout(String workoutId) {
        showLoadingDialog();
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ResultStatus<UserWithWorkout> resultStatus = WorkoutRepository.deleteWorkout(workoutId);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                loadingDialog.dismiss();
                if (resultStatus.isSuccess()) {
                    Globals.user = resultStatus.getData().getUser();
                    if (resultStatus.getData().getWorkout() == null) {
                        // means there are no workouts left, so change view to tell user to create a workout
                        resetFragment();
                    } else {
                        workoutNames.remove(Globals.activeWorkout.getWorkoutName());
                        workoutNameToId.remove(workoutId);

                        currentWorkout = resultStatus.getData().getWorkout();
                        Globals.activeWorkout = currentWorkout;
                        currentWorkoutMeta = resultStatus.getData().getUser().getUserWorkouts().get(currentWorkout.getWorkoutId());

                        workoutNames.remove(Globals.activeWorkout.getWorkoutName());
                        workoutNames.add(0, Globals.activeWorkout.getWorkoutName());
                        arrayAdapter.notifyDataSetChanged();
                        selectedWorkoutTV.setText(Globals.activeWorkout.getWorkoutName());
                        updateStatisticsTV();
                    }
                } else {
                    showErrorMessage("Delete Workout Error", resultStatus.getErrorMessage());
                }
            });
        });

    }

    private void showErrorMessage(String title, String message) {
        alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Ok", null)
                .create();
        alertDialog.show();
        // make the message font a little bigger than the default one provided by the alertdialog
        TextView messageTV = alertDialog.getWindow().findViewById(android.R.id.message);
        messageTV.setTextSize(18);
    }

    private void resetFragment() {
        /*
            Resets the current fragment. Used after the workout is successfully created
         */
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new MyWorkoutsFragment(), Variables.MY_WORKOUT_TITLE).commit();
    }
}
