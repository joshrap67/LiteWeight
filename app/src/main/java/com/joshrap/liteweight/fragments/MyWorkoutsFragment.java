package com.joshrap.liteweight.fragments;

import androidx.appcompat.app.AlertDialog;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.fragment.app.Fragment;

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
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.joshrap.liteweight.*;
import com.joshrap.liteweight.activities.MainActivity;
import com.joshrap.liteweight.adapters.WorkoutsAdapter;
import com.joshrap.liteweight.managers.SharedWorkoutManager;
import com.joshrap.liteweight.managers.WorkoutManager;
import com.joshrap.liteweight.models.user.Friend;
import com.joshrap.liteweight.managers.CurrentUserAndWorkoutProvider;
import com.joshrap.liteweight.utils.AndroidUtils;
import com.joshrap.liteweight.utils.ImageUtils;
import com.joshrap.liteweight.utils.TimeUtils;
import com.joshrap.liteweight.utils.ValidatorUtils;
import com.joshrap.liteweight.utils.StatisticsUtils;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.interfaces.FragmentWithDialog;
import com.joshrap.liteweight.models.Result;
import com.joshrap.liteweight.models.user.User;
import com.joshrap.liteweight.models.UserAndWorkout;
import com.joshrap.liteweight.models.workout.Workout;
import com.joshrap.liteweight.models.user.WorkoutInfo;
import com.joshrap.liteweight.utils.WorkoutUtils;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import static android.os.Looper.getMainLooper;

public class MyWorkoutsFragment extends Fragment implements FragmentWithDialog {
    private TextView selectedWorkoutTV, timesCompletedTV, completionRateTV, totalDaysTV, mostFrequentFocusTV;
    private ListView workoutListView;
    private AlertDialog alertDialog;
    //    private User user;
//    private Workout currentWorkout;
    private WorkoutInfo currentWorkout;
    private List<WorkoutInfo> workoutList;
    private WorkoutsAdapter workoutsAdapter;
    private boolean isPremium;
    private int workoutsSent;

    @Inject
    AlertDialog loadingDialog;
    @Inject
    WorkoutManager workoutManager;
    @Inject
    SharedWorkoutManager sharedWorkoutManager;
    @Inject
    CurrentUserAndWorkoutProvider currentUserAndWorkoutProvider;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        Injector.getInjector(getContext()).inject(this);

        ((MainActivity) getActivity()).updateToolbarTitle(Variables.MY_WORKOUT_TITLE);
        ((MainActivity) getActivity()).toggleBackButton(false);

        Workout workout = currentUserAndWorkoutProvider.provideCurrentWorkout();
        if (workout != null) {
            currentWorkout = currentUserAndWorkoutProvider.provideCurrentUser().getWorkout(workout.getId());

        }
        User user = currentUserAndWorkoutProvider.provideCurrentUser();
        isPremium = user.isPremium();
        workoutList = new ArrayList<>(user.getWorkouts());
        workoutsSent = user.getWorkoutsSent();

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
            ExtendedFloatingActionButton createWorkoutBtn = view.findViewById(R.id.create_workout_fab);
            createWorkoutBtn.setOnClickListener(v -> ((MainActivity) getActivity()).goToCreateWorkout());
            return;
        }

        initViews(view);
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
     * Once at least one workout is found, change layouts and initialize all views.
     *
     * @param view root view with all necessary widgets
     */
    private void initViews(View view) {
        ImageButton workoutOptionsButton = view.findViewById(R.id.workout_options_btn);
        PopupMenu dropDownMenu = new PopupMenu(getContext(), workoutOptionsButton);
        Menu menu = dropDownMenu.getMenu();
        final int editIndex = 0;
        final int sendIndex = 1;
        final int copyIndex = 2;
        final int renameIndex = 3;
        final int resetIndex = 4;
        final int deleteIndex = 5;
        menu.add(0, editIndex, 0, "Edit Workout");
        menu.add(0, sendIndex, 0, "Share Workout");
        menu.add(0, copyIndex, 0, "Copy Workout");
        menu.add(0, renameIndex, 0, "Rename Workout");
        menu.add(0, resetIndex, 0, "Reset Statistics");
        menu.add(0, deleteIndex, 0, "Delete Workout");

        dropDownMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case editIndex:
                    dropDownMenu.dismiss();
                    ((MainActivity) getActivity()).goToEditWorkout();
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
                    if (isPremium || workoutsSent < Variables.MAX_FREE_WORKOUTS_SENT) {
                        promptShare();
                    } else {
                        AndroidUtils.showErrorDialog("You have shared the maximum allowed amount of workouts.", getContext());
                    }
                    return true;
                case copyIndex:
                    if (!isPremium && workoutList.size() >= Variables.MAX_FREE_WORKOUTS) {
                        AndroidUtils.showErrorDialog("Copying this workout would put you over the maximum amount of workouts you can own. Delete some of your other ones if you wish to copy this workout.", getContext());
                    } else if (isPremium && workoutList.size() >= Variables.MAX_WORKOUTS) {
                        AndroidUtils.showErrorDialog("Copying this workout would put you over the maximum amount of workouts you can own. Delete some of your other ones if you wish to copy this workout.", getContext());
                    } else {
                        promptCopy();
                    }
                    return true;
            }
            return false;
        });
        workoutOptionsButton.setOnClickListener(v -> dropDownMenu.show());

        workoutListView = view.findViewById(R.id.workout_list_view);
        selectedWorkoutTV = view.findViewById(R.id.selected_workout_tv);
        totalDaysTV = view.findViewById(R.id.total_days_tv);
        mostFrequentFocusTV = view.findViewById(R.id.most_frequent_focus_tv);
        completionRateTV = view.findViewById(R.id.completion_rate_tv);
        timesCompletedTV = view.findViewById(R.id.times_completed_tv);
        selectedWorkoutTV.setText(currentWorkout.getWorkoutName());
        updateStatisticsTV();

        FloatingActionButton createWorkoutBtn = view.findViewById(R.id.new_workout_fab);
        createWorkoutBtn.setOnClickListener(v -> {
            if (!isPremium && workoutList.size() >= Variables.MAX_FREE_WORKOUTS) {
                AndroidUtils.showErrorDialog("You have reached the maximum amount of workouts allowed. Delete some of your other ones if you wish to create a new one.", getContext());
            } else if (isPremium && workoutList.size() >= Variables.MAX_WORKOUTS) {
                AndroidUtils.showErrorDialog("You have reached the maximum amount of workouts allowed. Delete some of your other ones if you wish to create a new one.", getContext());
            } else {
                // no errors so let user create new workout
                ((MainActivity) getActivity()).goToCreateWorkout();
            }
        });

        // initializes the main list view
        sortWorkouts();
        workoutsAdapter = new WorkoutsAdapter(getContext(), workoutList);
        workoutListView.setAdapter(workoutsAdapter);
        workoutListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        workoutListView.setOnItemClickListener((parent, _view, position, id) ->
                switchWorkout(workoutList.get(position)));
        workoutListView.setItemChecked(0, true); // programmatically select current workout in list
    }

    /**
     * Updates all UI with the newly changed current workout.
     */
    private void updateUI() {
        workoutList.clear();
        workoutList.addAll(user.getWorkouts());
        selectedWorkoutTV.setText(currentWorkout.getWorkoutName());
        sortWorkouts();
        workoutsAdapter.notifyDataSetChanged();
        updateStatisticsTV();
    }

    /**
     * Sorts workouts by date last accessed and ensures currently selected workout is at the top of the list.
     */
    private void sortWorkouts() {
        WorkoutInfo currentWorkoutInfo = user.getWorkout(currentWorkout.getWorkoutId());
        workoutList.remove(currentWorkoutInfo);
        workoutList.sort((r1, r2) -> {
            DateFormat dateFormatter = new SimpleDateFormat(TimeUtils.ZULU_TIME_FORMAT, Locale.ENGLISH);
            dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            int retVal = 0;
            try {
                Date date1 = dateFormatter.parse(r1.getLastSetAsCurrentUtc());
                Date date2 = dateFormatter.parse(r2.getLastSetAsCurrentUtc());
                retVal = date1 != null ? date2.compareTo(date1) : 0;
            } catch (ParseException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
            return retVal;
        });
        workoutList.add(0, currentWorkoutInfo); // selected always on top
        workoutListView.setItemChecked(0, true); // programmatically select current workout in list
    }

    /**
     * Fetches and displays statistics for the currently selected workout.
     */
    private void updateStatisticsTV() {
        int timesCompleted = user.getWorkout(currentWorkout.getId()).getTimesRestarted();
        double average = user.getWorkout(currentWorkout.getId()).getAverageExercisesCompleted();
        String formattedPercentage = StatisticsUtils.getFormattedAverageCompleted(average);

        timesCompletedTV.setText(Integer.toString(timesCompleted));
        totalDaysTV.setText(Integer.toString(currentWorkout.getRoutine().totalDays()));
        completionRateTV.setText(formattedPercentage);
        mostFrequentFocusTV.setText(WorkoutUtils.getMostFrequentFocus(user, currentWorkout.getRoutine()).replaceAll(",", ", "));

    }

    /**
     * Prompt the user if they actually want to reset the selected workout's statistics.
     */
    private void promptResetStatistics() {
        // workout name is italicized
        SpannableString span1 = new SpannableString("Are you sure you wish to reset the statistics for ");
        SpannableString span2 = new SpannableString(currentWorkout.getName());
        SpannableString span3 = new SpannableString("?\n\nDoing so will reset the total restarts and the exercise completion average.");
        span2.setSpan(new StyleSpan(Typeface.ITALIC), 0, span2.length(), 0);
        CharSequence title = TextUtils.concat(span1, span2, span3);

        alertDialog = new AlertDialog.Builder(getContext())
                .setTitle("Reset Statistics")
                .setMessage(title)
                .setPositiveButton("Yes", (dialog, which) -> resetWorkoutStatistics(currentWorkout.getId()))
                .setNegativeButton("No", null)
                .create();
        alertDialog.show();
    }

    private void resetWorkoutStatistics(String workoutId) {
        AndroidUtils.showLoadingDialog(loadingDialog, "Resetting...");
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Result<String> result = this.workoutManager.resetWorkoutStatistics(workoutId);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                loadingDialog.dismiss();
                if (result.isSuccess()) {
                    updateUI();
                } else {
                    AndroidUtils.showErrorDialog(result.getErrorMessage(), getContext());
                }
            });
        });
    }

    /**
     * Prompt the user if they want to rename the current workout.
     */
    private void promptRename() {
        View popupView = getLayoutInflater().inflate(R.layout.popup_rename_workout, null);
        EditText renameInput = popupView.findViewById(R.id.rename_workout_name_input);
        TextInputLayout workoutNameInputLayout = popupView.findViewById(R.id.rename_workout_name_input_layout);
        renameInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_WORKOUT_NAME)});
        renameInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(workoutNameInputLayout));

        // workout name is italicized
        SpannableString span1 = new SpannableString("Rename ");
        SpannableString span2 = new SpannableString(currentWorkout.getName());
        span2.setSpan(new StyleSpan(Typeface.ITALIC), 0, span2.length(), 0);
        CharSequence title = TextUtils.concat(span1, span2);

        alertDialog = new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setView(popupView)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create();
        alertDialog.setOnShowListener(dialogInterface -> {
            Button saveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            saveButton.setOnClickListener(view -> {
                String newName = renameInput.getText().toString().trim();
                List<String> workoutNames = new ArrayList<>();
                for (WorkoutInfo workoutInfo : workoutList) {
                    workoutNames.add(workoutInfo.getWorkoutName());
                }
                String errorMsg = ValidatorUtils.validWorkoutName(newName, workoutNames);
                if (errorMsg != null) {
                    workoutNameInputLayout.setError(errorMsg);
                } else {
                    alertDialog.dismiss();
                    renameWorkout(currentWorkout.getId(), newName);
                }
            });
        });
        alertDialog.show();
    }

    private void renameWorkout(String workoutId, String newWorkoutName) {
        AndroidUtils.showLoadingDialog(loadingDialog, "Renaming...");
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Result<String> result = this.workoutManager.renameWorkout(workoutId, newWorkoutName);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                loadingDialog.dismiss();
                if (result.isSuccess()) {
                    updateUI();
                } else {
                    AndroidUtils.showErrorDialog(result.getErrorMessage(), getContext());
                }
            });
        });
    }

    private void promptCopy() {
        View popupView = getLayoutInflater().inflate(R.layout.popup_copy_workout, null);
        EditText workoutNameInput = popupView.findViewById(R.id.workout_name_input);
        TextInputLayout workoutNameInputLayout = popupView.findViewById(R.id.workout_name_input_layout);
        workoutNameInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(workoutNameInputLayout));
        workoutNameInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_WORKOUT_NAME)});

        // workout name is italicized
        SpannableString span1 = new SpannableString("Copy ");
        SpannableString span2 = new SpannableString(currentWorkout.getName());
        SpannableString span3 = new SpannableString(" as a new workout");
        span2.setSpan(new StyleSpan(Typeface.ITALIC), 0, span2.length(), 0);
        CharSequence title = TextUtils.concat(span1, span2, span3);

        alertDialog = new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setView(popupView)
                .setPositiveButton("Copy", null)
                .setNegativeButton("Cancel", null)
                .create();
        alertDialog.setOnShowListener(dialogInterface -> {
            Button saveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            saveButton.setOnClickListener(view -> {
                String workoutName = workoutNameInput.getText().toString().trim();
                List<String> workoutNames = new ArrayList<>();
                for (WorkoutInfo workoutInfo : user.getWorkouts()) {
                    workoutNames.add(workoutInfo.getWorkoutName());
                }
                String errorMsg = ValidatorUtils.validWorkoutName(workoutName, workoutNames);
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

    private void copyWorkout(String workoutName) {
        AndroidUtils.showLoadingDialog(loadingDialog, "Copying...");
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Result<String> result = this.workoutManager.copyWorkout(currentWorkout, workoutName);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                loadingDialog.dismiss();
                if (result.isSuccess()) {
                    currentWorkout = currentUserAndWorkoutProvider.provideCurrentWorkout();
                    updateUI();
                } else {
                    AndroidUtils.showErrorDialog(result.getErrorMessage(), getContext());
                }
            });
        });
    }

    /**
     * Prompt user to send workout to a friend or any other user
     */
    private void promptShare() {
        View popupView = getLayoutInflater().inflate(R.layout.popup_send_workout_pick_user, null);
        TextInputLayout usernameInputLayout = popupView.findViewById(R.id.username_input_layout);
        TextView remainingToSendTv = popupView.findViewById(R.id.remaining_workouts_to_send_tv);
        int remainingAmount = Variables.MAX_FREE_WORKOUTS_SENT - workoutsSent;
        if (remainingAmount < 0) {
            remainingAmount = 0; // lol. Just to cover my ass in case
        }
        remainingToSendTv.setText(String.format("You can share a workout %d more times.", remainingAmount));

        List<Friend> friends = new ArrayList<>();
        for (Friend friend : user.getFriends()) {
            if (friend.isConfirmed()) {
                friends.add(friend);
            }
        }

        AutoCompleteTextView usernameInput = popupView.findViewById(R.id.username_input);
        SearchFriendArrayAdapter adapter = new SearchFriendArrayAdapter(getContext(), friends);
        usernameInput.setAdapter(adapter);
        usernameInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (v.hasFocus()) {
                // show suggestions when user clicks input for first time
                if (friends.size() > 0) {
                    usernameInput.showDropDown();
                }
            }
        });

        usernameInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(usernameInputLayout));
        usernameInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_USERNAME_LENGTH)});

        // workout name is italicized
        SpannableString span1 = new SpannableString("Share ");
        SpannableString span2 = new SpannableString(currentWorkout.getName());
        span2.setSpan(new StyleSpan(Typeface.ITALIC), 0, span2.length(), 0);
        CharSequence title = TextUtils.concat(span1, span2);

        alertDialog = new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setView(popupView)
                .setPositiveButton("Share", null)
                .setNegativeButton("Cancel", null)
                .create();
        alertDialog.setOnShowListener(dialogInterface -> {
            Button shareButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            shareButton.setOnClickListener(view -> {
                // usernames are case insensitive!
                String username = usernameInput.getText().toString().trim().toLowerCase();
                String errorMsg = ValidatorUtils.validUserToSendWorkout(user.getUsername(), username);
                if (errorMsg != null) {
                    usernameInputLayout.setError(errorMsg);
                } else {
                    // no problems so go ahead and send
                    alertDialog.dismiss();
                    if (!isPremium && workoutsSent >= Variables.MAX_FREE_WORKOUTS_SENT) {
                        AndroidUtils.showErrorDialog("You have reached the maximum amount of workouts allowed to share.", getContext());
                    } else {
                        shareWorkout(username, currentWorkout.getId());
                    }
                }
            });
        });
        alertDialog.show();
    }

    private void shareWorkout(String recipientUsername, String workoutId) {
        AndroidUtils.showLoadingDialog(loadingDialog, "Sharing...");
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Result<String> result = this.sharedWorkoutManager.shareWorkoutByUsername(recipientUsername, workoutId);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                loadingDialog.dismiss();
                if (result.isSuccess()) {
                    Toast.makeText(getContext(), "Workout successfully shared.", Toast.LENGTH_LONG).show();
                } else {
                    AndroidUtils.showErrorDialog(result.getErrorMessage(), getContext());
                }
            });
        });
    }

    /**
     * Prompt user if they actually want to delete the currently selected workout.
     */
    private void promptDelete() {
        // workout name is italicized
        SpannableString span1 = new SpannableString("Are you sure you wish to permanently delete ");
        SpannableString span2 = new SpannableString(currentWorkout.getName());
        SpannableString span3 = new SpannableString("?\n\nIf so, all statistics for it will also be deleted.");
        span2.setSpan(new StyleSpan(Typeface.ITALIC), 0, span2.length(), 0);
        CharSequence title = TextUtils.concat(span1, span2, span3);

        alertDialog = new AlertDialog.Builder(getContext())
                .setTitle("Delete Workout")
                .setMessage(title)
                .setPositiveButton("Yes", (dialog, which) -> {
                    String nextWorkoutId = null;
                    if (workoutList.size() >= 2) {
                        nextWorkoutId = workoutList.get(1).getWorkoutId(); // get next in list
                    }
                    deleteWorkout(currentWorkout.getId(), nextWorkoutId);
                })
                .setNegativeButton("No", null)
                .create();
        alertDialog.show();
    }

    private void deleteWorkout(String workoutId, String nextWorkoutId) {
        AndroidUtils.showLoadingDialog(loadingDialog, "Deleting...");
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Result<UserAndWorkout> result = this.workoutManager.deleteWorkoutThenFetchNext(workoutId, nextWorkoutId);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                loadingDialog.dismiss();
                if (result.isSuccess()) {
                    currentWorkout = currentUserAndWorkoutProvider.provideCurrentWorkout(); // todo use result instead of provider?
                    if (currentWorkout == null) {
                        // change view to tell user to create a workout
                        resetFragment();
                    } else {
                        updateUI();
                    }
                } else {
                    AndroidUtils.showErrorDialog(result.getErrorMessage(), getContext());
                }
            });
        });
    }

    private void switchWorkout(final WorkoutInfo selectedWorkout) {
        if (selectedWorkout.getWorkoutId().equals(currentWorkout.getId())) {
            return;
        }

        AndroidUtils.showLoadingDialog(loadingDialog, "Loading...");
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Result<String> result = this.workoutManager.switchWorkout(currentWorkout, selectedWorkout.getWorkoutId());
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                if (this.isResumed()) {
                    loadingDialog.dismiss();
                    if (result.isSuccess()) {
                        currentWorkout = currentUserAndWorkoutProvider.provideCurrentWorkout();
                        updateUI();
                    } else {
                        AndroidUtils.showErrorDialog(result.getErrorMessage(), getContext());
                        workoutListView.setItemChecked(0, true);
                    }
                }
            });
        });
    }

    private void resetFragment() {
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new MyWorkoutsFragment(), Variables.MY_WORKOUT_TITLE).commit();
    }

    public static class SearchFriendArrayAdapter extends ArrayAdapter<Friend> implements Filterable {
        private final Context context;
        private final List<Friend> allFriends;
        private final List<Friend> displayFriends;

        public SearchFriendArrayAdapter(Context context, List<Friend> friends) {
            super(context, 0, friends);
            this.context = context;
            this.allFriends = new ArrayList<>(friends);
            this.displayFriends = new ArrayList<>(friends);
        }

        @Override
        public Friend getItem(int position) {
            return this.displayFriends.get(position);
        }

        @Override
        public int getCount() {
            return this.displayFriends.size();
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            View listItem = convertView;
            if (listItem == null) {
                listItem = LayoutInflater.from(context).inflate(R.layout.row_search_friend, parent, false);
            }

            Friend friend = getItem(position);
            TextView usernameTV = listItem.findViewById(R.id.username_tv);
            usernameTV.setText(friend.getUsername());

            ImageView profilePicture = listItem.findViewById(R.id.profile_picture_image);
            Picasso.get()
                    .load(ImageUtils.getProfilePictureUrl(friend.getProfilePicture()))
                    .error(R.drawable.picture_load_error)
                    .networkPolicy(NetworkPolicy.NO_CACHE) // on first loading in app, always fetch online
                    .into(profilePicture);

            return listItem;
        }

        @NonNull
        @Override
        public Filter getFilter() {
            return friendFilter;
        }

        private final Filter friendFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<Friend> filteredFriends = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {
                    filteredFriends.addAll(allFriends);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();

                    for (Friend friend : allFriends) {
                        if (friend.getUsername().toLowerCase().contains(filterPattern)) {
                            filteredFriends.add(friend);
                        }
                    }
                }

                results.values = filteredFriends;
                results.count = filteredFriends.size();

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                displayFriends.clear();
                displayFriends.addAll((List) results.values);
                notifyDataSetChanged();
            }

            @Override
            public CharSequence convertResultToString(Object resultValue) {
                return ((Friend) resultValue).getUsername();
            }
        };
    }
}
