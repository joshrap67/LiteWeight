package com.joshrap.liteweight.fragments;

import android.app.AlertDialog;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.joshrap.liteweight.*;
import com.joshrap.liteweight.activities.WorkoutActivity;
import com.joshrap.liteweight.adapters.WorkoutsAdapter;
import com.joshrap.liteweight.helpers.AndroidHelper;
import com.joshrap.liteweight.helpers.InputHelper;
import com.joshrap.liteweight.helpers.StatisticsHelper;
import com.joshrap.liteweight.imports.Globals;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.interfaces.FragmentWithDialog;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.models.User;
import com.joshrap.liteweight.models.UserWithWorkout;
import com.joshrap.liteweight.models.Workout;
import com.joshrap.liteweight.models.WorkoutMeta;
import com.joshrap.liteweight.network.repos.WorkoutRepository;
import com.joshrap.liteweight.widgets.ErrorDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import static android.os.Looper.getMainLooper;

public class MyWorkoutsFragment extends Fragment implements FragmentWithDialog {
    private TextView selectedWorkoutTV, statisticsTV;
    private ListView workoutListView;
    private AlertDialog alertDialog;
    private User user;
    private Workout currentWorkout;
    private List<WorkoutMeta> workoutList;
    private WorkoutsAdapter workoutsAdapter;
    @Inject
    ProgressDialog loadingDialog;
    @Inject
    WorkoutRepository workoutRepository;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((WorkoutActivity) getActivity()).updateToolbarTitle(Variables.MY_WORKOUT_TITLE);
        ((WorkoutActivity) getActivity()).toggleBackButton(false);
        Injector.getInjector(getContext()).inject(this);
        // TODO injection or view model for these two???
        currentWorkout = Globals.activeWorkout;
        user = Globals.user;

        View view;
        if (currentWorkout == null) {
            view = inflater.inflate(R.layout.no_workouts_found_layout, container, false);
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
        workoutList = new ArrayList<>(user.getUserWorkouts().values());
        initViews(view);
    }

    @Override
    public void onPause() {
        // todo do this for all fragments
        loadingDialog.dismiss();
        super.onPause();
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
        final int sendIndex = 1;
        final int renameIndex = 2;
        final int copyIndex = 3;
        final int resetIndex = 4;
        final int deleteIndex = 5;
        menu.add(0, editIndex, 0, "Edit Workout");
        menu.add(0, sendIndex, 0, "Send Workout");
        menu.add(0, renameIndex, 0, "Rename Workout");
        menu.add(0, copyIndex, 0, "Copy Workout");
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
                case sendIndex:
                    if (user.getWorkoutsSent() < Variables.MAX_FREE_WORKOUTS_SENT) {
                        promptSend();
                    } else {
                        showErrorMessage("Too many workouts sent", "You have reached the maximum number of workouts that you can send for free. Upgrade to premium to have unlimited sending access!");
                    }
                    break;
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
        workoutsAdapter = new WorkoutsAdapter(getContext(), workoutList);
        workoutListView.setAdapter(workoutsAdapter);
        workoutListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        workoutListView.setOnItemClickListener((parent, _view, position, id) ->
                switchWorkout(workoutList.get(position)));
        workoutListView.setItemChecked(0, true); // programmatically select current workout in list
    }

    private void updateUI() {
        /*
            Updates all the UI with the newly changed current workout
         */
        workoutList.clear();
        workoutList.addAll(user.getUserWorkouts().values());
        selectedWorkoutTV.setText(currentWorkout.getWorkoutName());
        sortWorkouts();
        workoutsAdapter.notifyDataSetChanged();
        updateStatisticsTV();
    }

    private void sortWorkouts() {
        /*
            Currently sorts by date last accessed
         */
        workoutList.remove(user.getUserWorkouts().get(currentWorkout.getWorkoutId()));
        // todo this isn't actually going by dates...
        Collections.sort(workoutList, (o1, o2) -> o2.getDateLast().compareTo(o1.getDateLast()));
        workoutList.add(0, user.getUserWorkouts().get(currentWorkout.getWorkoutId())); // selected always on top
        workoutListView.setItemChecked(0, true); // programmatically select current workout in list
    }

    private void updateStatisticsTV() {
        /*
            Displays statistics for the currently selected workout
         */
        int timesCompleted = user.getUserWorkouts().get(currentWorkout.getWorkoutId()).getTimesCompleted();
        double average = user.getUserWorkouts().get(currentWorkout.getWorkoutId()).getAverageExercisesCompleted();
        String formattedPercentage = StatisticsHelper.getFormattedAverageCompleted(average);
        String msg = "Times Completed: " + timesCompleted + "\n" +
                "Average Percentage of Exercises Completed: " + formattedPercentage + "\n" +
                "Total Number of Days in Workout: " + currentWorkout.getRoutine().getTotalNumberOfDays() + "\n" +
                "Most Worked Focus: " + currentWorkout.getMostFrequentFocus().replaceAll(",", ", ");
        statisticsTV.setText(msg);
    }

    private void promptResetStatistics() {
        /*
            Prompt the user if they actually want to reset the selected workout's statistics
         */
        String message = "Are you sure you wish to reset the statistics for \"" +
                currentWorkout.getWorkoutName() + "\"?\n\n" +
                "Doing so will reset the times completed and the percentage of exercises completed.";
        alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                .setTitle("Reset Statistics")
                .setMessage(message)
                .setPositiveButton("Yes", (dialog, which) -> resetWorkoutStatistics(currentWorkout.getWorkoutId()))
                .setNegativeButton("No", null)
                .create();
        alertDialog.show();
    }

    private void promptRename() {
        /*
            Prompt the user if they want to rename the current workout
         */
        View popupView = getLayoutInflater().inflate(R.layout.popup_rename_workout, null);
        final EditText renameInput = popupView.findViewById(R.id.rename_workout_name_input);
        final TextInputLayout workoutNameInputLayout = popupView.findViewById(R.id.rename_workout_name_input_layout);
        renameInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_WORKOUT_NAME)});
        renameInput.addTextChangedListener(AndroidHelper.hideErrorTextWatcher(workoutNameInputLayout));
        alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                .setTitle("Rename \"" + currentWorkout.getWorkoutName() + "\"")
                .setView(popupView)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create();
        alertDialog.setOnShowListener(dialogInterface -> {
            Button saveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            saveButton.setOnClickListener(view -> {
                String newName = renameInput.getText().toString().trim();
                List<String> workoutNames = new ArrayList<>();
                for (WorkoutMeta workoutMeta : workoutList) {
                    workoutNames.add(workoutMeta.getWorkoutName());
                }
                String errorMsg = InputHelper.validWorkoutName(newName, workoutNames);
                if (errorMsg != null) {
                    workoutNameInputLayout.setError(errorMsg);
                } else {
                    alertDialog.dismiss();
                    renameWorkout(currentWorkout.getWorkoutId(), newName);
                }
            });
        });
        alertDialog.show();
    }

    private void promptCopy() {
        final View popupView = getLayoutInflater().inflate(R.layout.popup_copy_workout, null);
        final EditText workoutNameInput = popupView.findViewById(R.id.workout_name_input);
        final TextInputLayout workoutNameInputLayout = popupView.findViewById(R.id.workout_name_input_layout);
        workoutNameInput.addTextChangedListener(AndroidHelper.hideErrorTextWatcher(workoutNameInputLayout));
        workoutNameInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_WORKOUT_NAME)});
        alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                .setTitle(String.format("Copy \"%s\" as new workout", currentWorkout.getWorkoutName()))
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

    private void promptSend() {
        /*
            Prompt user to send workout to a friend or any other user
         */
        final View popupView = getLayoutInflater().inflate(R.layout.popup_send_workout_pick_user, null);
        final EditText usernameInput = popupView.findViewById(R.id.username_input);
        final TextInputLayout usernameInputLayout = popupView.findViewById(R.id.username_input_layout);
        final ListView friendsListView = popupView.findViewById(R.id.friends_list_view);
        List<String> friendsUsernames = new ArrayList<>();
        for (String username : user.getFriends().keySet()) {
            if (user.getFriends().get(username).isConfirmed()) {
                friendsUsernames.add(username);
            }
        }
        if (friendsUsernames.isEmpty()) {
            // user has not friends, so hide the TV and listview that displays the friends
            popupView.findViewById(R.id.friends_text_view).setVisibility(View.GONE);
            friendsListView.setVisibility(View.GONE);
        }
        Collections.sort(friendsUsernames, String::compareToIgnoreCase);
        ArrayAdapter<String> friendsAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, friendsUsernames);
        friendsListView.setAdapter(friendsAdapter);
        // when user clicks on one of their friends, put that friend's username into the input
        friendsListView.setOnItemClickListener((adapterView, view, i, l) -> usernameInput.setText(friendsUsernames.get(i)));


        usernameInput.addTextChangedListener(AndroidHelper.hideErrorTextWatcher(usernameInputLayout));
        usernameInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_USERNAME_LENGTH)});
        alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                .setTitle(String.format("Send \"%s\" to another user", currentWorkout.getWorkoutName()))
                .setView(popupView)
                .setPositiveButton("Send", null)
                .setNegativeButton("Cancel", null)
                .create();
        alertDialog.setOnShowListener(dialogInterface -> {
            final Button saveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            saveButton.setOnClickListener(view -> {
                String username = usernameInput.getText().toString().trim();
                String errorMsg = InputHelper.validUserToSendWorkout(user.getUsername(), username);
                if (errorMsg != null) {
                    usernameInputLayout.setError(errorMsg);
                } else {
                    // no problems so go ahead and send
                    alertDialog.dismiss();
                    sendWorkout(username, currentWorkout.getWorkoutId());
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
                .setPositiveButton("Yes", (dialog, which) -> {
                    String nextWorkoutId = null;
                    if (workoutList.size() >= 2) {
                        // there's at least two elements so next workout after deleting current is the second element in current list
                        nextWorkoutId = workoutList.get(1).getWorkoutId(); // get next in list
                    }
                    deleteWorkout(currentWorkout.getWorkoutId(), nextWorkoutId);
                })
                .setNegativeButton("No", null)
                .create();
        alertDialog.show();
    }

    private void switchWorkout(final WorkoutMeta selectedWorkout) {
        if (selectedWorkout.getWorkoutId().equals(currentWorkout.getWorkoutId())) {
            // don't allow user to switch to current workout since they are already on it
            return;
        }
        showLoadingDialog();
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ResultStatus<UserWithWorkout> resultStatus = this.workoutRepository.switchWorkout(currentWorkout, selectedWorkout.getWorkoutId());
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                if (this.isResumed()) {
                    // not sure if this does what i think it does? But would prevent potential memory leaks
                    loadingDialog.dismiss();
                    if (resultStatus.isSuccess()) {
                        // set new active workout and update user
                        currentWorkout = resultStatus.getData().getWorkout();
                        Globals.activeWorkout = currentWorkout;
                        user.setCurrentWorkout(currentWorkout.getWorkoutId());
                        user.getUserWorkouts().put(currentWorkout.getWorkoutId(),
                                resultStatus.getData().getUser().getUserWorkouts().get(currentWorkout.getWorkoutId()));
                        updateUI();
                    } else {
                        ErrorDialog.showErrorDialog("Switch Workout Error", resultStatus.getErrorMessage(), getContext());
                        workoutListView.setItemChecked(0, true);
                    }
                }
            });
        });
    }

    private void renameWorkout(final String workoutId, final String newWorkoutName) {
        showLoadingDialog();
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ResultStatus<User> resultStatus = this.workoutRepository.renameWorkout(workoutId, newWorkoutName);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                loadingDialog.dismiss();
                if (resultStatus.isSuccess()) {
                    user = resultStatus.getData();
                    Globals.user = user;
                    currentWorkout.setWorkoutName(newWorkoutName);

                    updateUI();
                } else {
                    showErrorMessage("Rename Workout Error", resultStatus.getErrorMessage());
                }
            });
        });
    }

    private void copyWorkout(final String workoutName) {
        showLoadingDialog();
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ResultStatus<UserWithWorkout> resultStatus = this.workoutRepository.copyWorkout(currentWorkout, workoutName);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                loadingDialog.dismiss();
                if (resultStatus.isSuccess()) {
                    user = resultStatus.getData().getUser();
                    Globals.user = user;

                    currentWorkout = resultStatus.getData().getWorkout();
                    Globals.activeWorkout = currentWorkout;

                    updateUI();
                } else {
                    showErrorMessage("Copy Workout Error", resultStatus.getErrorMessage());
                }
            });
        });
    }

    private void sendWorkout(final String recipientUsername, String workoutId) {
        showLoadingDialog();
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ResultStatus<String> resultStatus = this.workoutRepository.sendWorkout(recipientUsername, workoutId);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                loadingDialog.dismiss();
                if (resultStatus.isSuccess()) {
                    Toast.makeText(getContext(), "Workout successfully sent.", Toast.LENGTH_LONG).show();
                    user.setWorkoutsSent(user.getWorkoutsSent() + 1);

                } else {
                    showErrorMessage("Copy Workout Error", resultStatus.getErrorMessage());
                }
            });
        });
    }

    private void deleteWorkout(final String workoutId, final String nextWorkoutId) {
        showLoadingDialog();
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ResultStatus<UserWithWorkout> resultStatus = this.workoutRepository.popWorkout(workoutId, nextWorkoutId);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                loadingDialog.dismiss();
                if (resultStatus.isSuccess()) {
                    user = resultStatus.getData().getUser();
                    Globals.user = user;
                    if (resultStatus.getData().getWorkout() == null) {
                        Globals.activeWorkout = null;
                        // means there are no workouts left, so change view to tell user to create a workout
                        resetFragment();
                    } else {
                        currentWorkout = resultStatus.getData().getWorkout();
                        Globals.activeWorkout = currentWorkout;
                        updateUI();
                    }
                } else {
                    showErrorMessage("Delete Workout Error", resultStatus.getErrorMessage());
                }
            });
        });

    }

    private void resetWorkoutStatistics(final String workoutId) {
        showLoadingDialog();
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ResultStatus<User> resultStatus = this.workoutRepository.resetWorkoutStatistics(workoutId);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                loadingDialog.dismiss();
                if (resultStatus.isSuccess()) {
                    user = resultStatus.getData();
                    Globals.user = user;
                    updateUI();
                } else {
                    showErrorMessage("Reset Workout Error", resultStatus.getErrorMessage());
                }
            });
        });

    }

    private void showLoadingDialog() {
        loadingDialog.setMessage("Loading...");
        loadingDialog.show();
    }

    private void showErrorMessage(String title, String message) {
        alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Ok", null)
                .create();
        alertDialog.show();
    }

    private void resetFragment() {
        /*
            Resets the current fragment. Used after the workout is successfully created
         */
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new MyWorkoutsFragment(), Variables.MY_WORKOUT_TITLE).commit();
    }
}
