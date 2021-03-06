package com.joshrap.liteweight.fragments;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputLayout;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.activities.WorkoutActivity;
import com.joshrap.liteweight.utils.AndroidUtils;
import com.joshrap.liteweight.utils.ImageUtils;
import com.joshrap.liteweight.utils.ValidatorUtils;
import com.joshrap.liteweight.utils.JsonUtils;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.interfaces.FragmentWithDialog;
import com.joshrap.liteweight.models.AcceptWorkoutResponse;
import com.joshrap.liteweight.models.SharedWorkoutMeta;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.models.User;
import com.joshrap.liteweight.models.UserWithWorkout;
import com.joshrap.liteweight.network.repos.UserRepository;
import com.joshrap.liteweight.network.repos.WorkoutRepository;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import static android.os.Looper.getMainLooper;

public class ReceivedWorkoutsFragment extends Fragment implements FragmentWithDialog {
    private User user;
    private List<SharedWorkoutMeta> receivedWorkouts;
    private ProgressBar listLoadingIcon;
    private RecyclerView receivedWorkoutsRecyclerView;
    private ImageButton markAllReceivedWorkoutsSeen;
    private TextView emptyView;
    private BottomSheetDialog bottomSheetDialog;
    private AlertDialog alertDialog;
    private ReceivedWorkoutsAdapter receivedWorkoutsAdapter;
    private boolean isGettingNextBatch;
    private UserWithWorkout userWithWorkout;

    @Inject
    ProgressDialog loadingDialog;
    @Inject
    WorkoutRepository workoutRepository;
    @Inject
    UserRepository userRepository;

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
                    receivedWorkouts = new ArrayList<>(user.getReceivedWorkouts().values());
                    sortReceivedWorkouts();
                    receivedWorkoutsAdapter.refresh(receivedWorkouts);
                    // get rid of push notification since user is currently on this page
                    NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                    if (notificationManager != null) {
                        notificationManager.cancel(sharedWorkoutMeta.getWorkoutId().hashCode());
                    }
                    updateAllSeenButton();
                    Toast.makeText(getContext(), "New workout: " + sharedWorkoutMeta.getWorkoutName(), Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((WorkoutActivity) getActivity()).updateToolbarTitle(Variables.RECEIVED_WORKOUTS_TITLE);
        ((WorkoutActivity) getActivity()).toggleBackButton(false);
        Injector.getInjector(getContext()).inject(this);
        userWithWorkout = ((WorkoutActivity) getActivity()).getUserWithWorkout();
        user = userWithWorkout.getUser();
        isGettingNextBatch = false;
        receivedWorkouts = new ArrayList<>();

        IntentFilter receiverActions = new IntentFilter();
        receiverActions.addAction(Variables.RECEIVED_WORKOUT_MODEL_UPDATED_BROADCAST);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(notificationReceiver, receiverActions);

        View view = inflater.inflate(R.layout.fragment_received_workouts, container, false);

        listLoadingIcon = view.findViewById(R.id.loading_icon_list);
        listLoadingIcon.setVisibility(View.INVISIBLE);
        receivedWorkoutsRecyclerView = view.findViewById(R.id.recycler_view);
        emptyView = view.findViewById(R.id.empty_view);
        markAllReceivedWorkoutsSeen = view.findViewById(R.id.mark_all_read_btn);
        updateAllSeenButton();
        markAllReceivedWorkoutsSeen.setOnClickListener(view1 -> setAllReceivedWorkoutsSeen());

        receivedWorkouts = new ArrayList<>(user.getReceivedWorkouts().values());
        sortReceivedWorkouts();
        removeReceivedWorkoutNotification();
        displayReceivedWorkouts();
        receivedWorkoutsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (!isGettingNextBatch && receivedWorkouts.size() < user.getTotalReceivedWorkouts()) {
                        isGettingNextBatch = true;
                        getNextBatch();
                    }
                }
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState); // used to restore any previous state
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
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
        if (bottomSheetDialog != null && bottomSheetDialog.isShowing()) {
            bottomSheetDialog.dismiss();
        }
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private void sortReceivedWorkouts() {
        Collections.sort(receivedWorkouts, (r1, r2) -> {
            // sort based on received workout sent time (newest at top)
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
            dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            int retVal = 0;
            try {
                Date date1 = dateFormatter.parse(r1.getDateSent());
                Date date2 = dateFormatter.parse(r2.getDateSent());
                retVal = date1 != null ? date2.compareTo(date1) : 0;
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return retVal;
        });
    }

    private void removeReceivedWorkoutNotification() {
        // get rid of any push notification that might be in the status bar
        for (SharedWorkoutMeta sharedWorkoutMeta : receivedWorkouts) {
            if (!sharedWorkoutMeta.isSeen()) {
                // get rid of any push notification that might be there
                NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null) {
                    notificationManager.cancel(sharedWorkoutMeta.getWorkoutId().hashCode());
                }
            }
        }
    }

    private void getNextBatch() {
        listLoadingIcon.setVisibility(View.VISIBLE);
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            int batchNumber = receivedWorkouts.size() / Variables.BATCH_SIZE;
            ResultStatus<List<SharedWorkoutMeta>> resultStatus = this.workoutRepository.getReceivedWorkouts(batchNumber);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                if (this.isResumed()) {
                    listLoadingIcon.setVisibility(View.INVISIBLE);
                    isGettingNextBatch = false;
                    if (resultStatus.isSuccess()) {
                        for (SharedWorkoutMeta sharedWorkoutMeta : resultStatus.getData()) {
                            user.getReceivedWorkouts().put(sharedWorkoutMeta.getWorkoutId(), sharedWorkoutMeta);
                        }
                        // this is honestly unapologetically hacky but i don't care anymore
                        receivedWorkouts = new ArrayList<>(user.getReceivedWorkouts().values());
                        sortReceivedWorkouts();
                        receivedWorkoutsAdapter.refresh(receivedWorkouts);
                        updateAllSeenButton();
                    } else {
                        AndroidUtils.showErrorDialog("Load Received Workouts Error", resultStatus.getErrorMessage(), getContext());
                    }
                }
            });
        });
    }

    private void displayReceivedWorkouts() {
        receivedWorkoutsRecyclerView.setVisibility(View.VISIBLE);
        receivedWorkoutsAdapter = new ReceivedWorkoutsAdapter(receivedWorkouts);
        receivedWorkoutsAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            // since google is stupid af and doesn't have a simple setEmptyView for recyclerView...
            @Override
            public void onChanged() {
                super.onChanged();
                checkEmpty();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                checkEmpty();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                checkEmpty();
            }
        });
        receivedWorkoutsRecyclerView.setAdapter(receivedWorkoutsAdapter);
        receivedWorkoutsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        checkEmpty();
    }

    /**
     * Used to check if there are any received workouts.
     */
    private void checkEmpty() {
        if (receivedWorkouts.isEmpty() && user.getTotalReceivedWorkouts() > 0 && !isGettingNextBatch) {
            // still workouts left so load next batch
            isGettingNextBatch = true;
            getNextBatch();
        } else {
            emptyView.setVisibility(receivedWorkouts.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void updateAllSeenButton() {
        markAllReceivedWorkoutsSeen.setVisibility(user.getUnseenReceivedWorkouts() > 0 ? View.VISIBLE : View.GONE);
    }

    private void blockUserPopup(String username) {
        alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                .setTitle("Block User")
                .setMessage(String.format("Are you sure you wish to block \"%s\"? They will no longer be able to add you as a friend or send you any workouts.", username))
                .setPositiveButton("Yes", (dialog, which) -> blockUser(username))
                .setNegativeButton("No", null)
                .create();
        alertDialog.show();
    }

    private void blockUser(String username) {
        AndroidUtils.showLoadingDialog(loadingDialog, "Blocking user...");
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ResultStatus<String> resultStatus = this.userRepository.blockUser(username);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                loadingDialog.dismiss();
                if (resultStatus.isSuccess()) {
                    user.getBlocked().put(username, resultStatus.getData());
                    user.getFriendRequests().remove(username);
                    user.getFriends().remove(username);
                } else {
                    AndroidUtils.showErrorDialog("Error", resultStatus.getErrorMessage(), getContext());
                }
            });
        });
    }

    private void acceptWorkout(final SharedWorkoutMeta workoutToAccept, final String optionalName) {
        AndroidUtils.showLoadingDialog(loadingDialog, "Accepting...");

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ResultStatus<AcceptWorkoutResponse> resultStatus = this.workoutRepository.acceptReceivedWorkout(workoutToAccept.getWorkoutId(), optionalName);
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
                    if (!workoutToAccept.isSeen()) {
                        // this workout was not seen, so make sure to decrease the unseen count since it is no longer in the list
                        user.setUnseenReceivedWorkouts(user.getUnseenReceivedWorkouts() - 1);
                    }
                    user.getReceivedWorkouts().remove(workoutToAccept.getWorkoutId());

                    // this is honestly unapologetically hacky but i don't care anymore
                    receivedWorkouts = new ArrayList<>(user.getReceivedWorkouts().values());
                    sortReceivedWorkouts();
                    receivedWorkoutsAdapter.refresh(receivedWorkouts);
                    updateAllSeenButton();
                    ((WorkoutActivity) getActivity()).updateReceivedWorkoutNotificationIndicator();
                } else {
                    AndroidUtils.showErrorDialog("Error", resultStatus.getErrorMessage(), getContext());
                }
            });
        });
    }

    private void declineWorkout(SharedWorkoutMeta sharedWorkoutMeta) {
        AndroidUtils.showLoadingDialog(loadingDialog, "Declining...");

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ResultStatus<String> resultStatus = this.workoutRepository.declineReceivedWorkout(sharedWorkoutMeta.getWorkoutId());
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                loadingDialog.dismiss();
                if (resultStatus.isSuccess() && ReceivedWorkoutsFragment.this.isResumed()) {
                    receivedWorkouts.remove(sharedWorkoutMeta);
                    user.getReceivedWorkouts().remove(sharedWorkoutMeta.getWorkoutId());
                    if (!sharedWorkoutMeta.isSeen()) {
                        // if it was unread, then we need to make sure to decrease unseen count
                        user.setUnseenReceivedWorkouts(user.getUnseenReceivedWorkouts() - 1);
                        ((WorkoutActivity) getActivity()).updateReceivedWorkoutNotificationIndicator();
                        updateAllSeenButton();
                    }
                    user.setTotalReceivedWorkouts(user.getTotalReceivedWorkouts() - 1);
                    receivedWorkoutsAdapter.notifyDataSetChanged();
                } else {
                    AndroidUtils.showErrorDialog("Error", resultStatus.getErrorMessage(), getContext());
                }
            });
        });
    }

    /**
     * Prompts user to rename a workout before accepting it since a workout already exists with the current name.
     *
     * @param sharedWorkoutMeta workout that is about to be accepted.
     */
    private void workoutNameAlreadyExistsPopup(final SharedWorkoutMeta sharedWorkoutMeta) {
        View popupView = getLayoutInflater().inflate(R.layout.popup_workout_name_exists, null);
        EditText renameInput = popupView.findViewById(R.id.rename_workout_name_input);
        TextInputLayout workoutNameInputLayout = popupView.findViewById(R.id.rename_workout_name_input_layout);
        renameInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_WORKOUT_NAME)});
        renameInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(workoutNameInputLayout));
        alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                .setTitle("\"" + sharedWorkoutMeta.getWorkoutName() + "\" already exists")
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
                    acceptWorkout(sharedWorkoutMeta, newName);
                    alertDialog.dismiss();
                } else {
                    workoutNameInputLayout.setError(errorMsg);
                }
            });
        });
        alertDialog.show();
    }

    private void setReceivedWorkoutSeen(String workoutId) {
        // blind send for marking a workout read for now
        user.setUnseenReceivedWorkouts(user.getUnseenReceivedWorkouts() - 1);

        ((WorkoutActivity) getActivity()).updateReceivedWorkoutNotificationIndicator();
        updateAllSeenButton();
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> this.workoutRepository.setReceivedWorkoutSeen(workoutId));
    }

    private void setAllReceivedWorkoutsSeen() {
        // blind send for marking all workout read for now
        user.setUnseenReceivedWorkouts(0);
        for (SharedWorkoutMeta sharedWorkoutMeta : receivedWorkouts) {
            sharedWorkoutMeta.setSeen(true);
        }
        ((WorkoutActivity) getActivity()).updateReceivedWorkoutNotificationIndicator();
        displayReceivedWorkouts(); // to remove any unseen indicators
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            this.workoutRepository.setAllReceivedWorkoutsSeen();
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                if (this.isResumed()) {
                    markAllReceivedWorkoutsSeen.setVisibility(View.GONE);
                }
            });
        });
    }

    private void showBlownUpProfilePic(String username, String iconUrl) {
        View popupView = getLayoutInflater().inflate(R.layout.popup_blown_up_profile_picture, null);
        ImageView profilePicture = popupView.findViewById(R.id.profile_picture);
        Picasso.get()
                .load(ImageUtils.getIconUrl(iconUrl))
                .error(R.drawable.picture_load_error)
                .networkPolicy(NetworkPolicy.NO_CACHE) // on first loading in app, always fetch online
                .into(profilePicture);

        alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                .setTitle(username)
                .setView(popupView)
                .setPositiveButton("Done", null)
                .create();
        alertDialog.show();
    }

    private class ReceivedWorkoutsAdapter extends RecyclerView.Adapter<ReceivedWorkoutsAdapter.ViewHolder> {
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView workoutNameTV;
            TextView dateSentTV;
            TextView senderTV;
            Button acceptButton;
            Button declineButton;
            RelativeLayout rootLayout;

            ViewHolder(View itemView) {
                super(itemView);
                workoutNameTV = itemView.findViewById(R.id.workout_name_tv);
                dateSentTV = itemView.findViewById(R.id.date_sent_tv);
                senderTV = itemView.findViewById(R.id.sender_tv);
                rootLayout = itemView.findViewById(R.id.root_layout);
                acceptButton = itemView.findViewById(R.id.accept_workout_btn);
                declineButton = itemView.findViewById(R.id.decline_workout_btn);
            }
        }

        private List<SharedWorkoutMeta> receivedWorkouts;

        ReceivedWorkoutsAdapter(List<SharedWorkoutMeta> receivedWorkouts) {
            this.receivedWorkouts = receivedWorkouts;
        }


        @Override
        public ReceivedWorkoutsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View exerciseView = inflater.inflate(R.layout.row_received_workout, parent, false);
            return new ViewHolder(exerciseView);
        }

        @Override
        public void onBindViewHolder(ReceivedWorkoutsAdapter.ViewHolder holder, int position) {
            final SharedWorkoutMeta receivedWorkout = receivedWorkouts.get(position);

            TextView workoutNameTV = holder.workoutNameTV;
            TextView senderTV = holder.senderTV;
            TextView dateSentTv = holder.dateSentTV;
            Button acceptButton = holder.acceptButton;
            Button declineButton = holder.declineButton;

            declineButton.setOnClickListener(view -> declineWorkout(receivedWorkout));

            acceptButton.setOnClickListener(view -> {
                boolean workoutNameExists = false;
                for (String workoutId : user.getWorkoutMetas().keySet()) {
                    if (user.getWorkoutMetas().get(workoutId).getWorkoutName().equals(receivedWorkout.getWorkoutName())) {
                        workoutNameExists = true;
                        break;
                    }
                }
                if (workoutNameExists) {
                    workoutNameAlreadyExistsPopup(receivedWorkout);
                } else {
                    acceptWorkout(receivedWorkout, null);
                }
            });
            senderTV.setText(String.format("Sent by: %s", receivedWorkout.getSender()));

            DateFormat dateFormatInput = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.ENGLISH);
            dateFormatInput.setTimeZone(TimeZone.getTimeZone("UTC"));
            try {
                Date date = dateFormatInput.parse(receivedWorkout.getDateSent());

                // we have the date as a proper object, so now format it to the user's local timezone
                DateFormat dateFormatOutput = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.ENGLISH);
                dateFormatOutput.setTimeZone(TimeZone.getDefault());
                dateSentTv.setText(dateFormatOutput.format(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String workoutName = receivedWorkout.getWorkoutName();
            if (!receivedWorkout.isSeen()) {
                // if unseen, add ! to catch user's attention
                workoutName += "  (!)";
            }
            workoutNameTV.setText(workoutName);
            RelativeLayout rootLayout = holder.rootLayout;
            rootLayout.setOnClickListener(v -> {
                if (!receivedWorkout.isSeen()) {
                    // when user clicks on the workout, mark it as seen
                    setReceivedWorkoutSeen(receivedWorkout.getWorkoutId());
                    receivedWorkout.setSeen(true);
                    workoutNameTV.setText(receivedWorkout.getWorkoutName());
                }

                bottomSheetDialog = new BottomSheetDialog(getActivity(), R.style.BottomSheetDialogTheme);
                View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_received_workout, null);
                TextView browseWorkout = sheetView.findViewById(R.id.browse_workout_tv);
                TextView workoutNameBottomSheetTV = sheetView.findViewById(R.id.workout_name_tv);
                workoutNameBottomSheetTV.setText(receivedWorkout.getWorkoutName());

                TextView workoutMetaTV = sheetView.findViewById(R.id.workout_meta_tv);
                workoutMetaTV.setText(String.format("Most frequent focus: %s\nNumber of days: %d",
                        receivedWorkout.getMostFrequentFocus().replaceAll(",", ", "), receivedWorkout.getTotalDays()));
                browseWorkout.setOnClickListener(v1 -> {
                    ((WorkoutActivity) getActivity()).goToBrowseReceivedWorkout(receivedWorkout.getWorkoutId(), receivedWorkout.getWorkoutName());
                    bottomSheetDialog.dismiss();
                });


                TextView blockUserTV = sheetView.findViewById(R.id.block_user_tv);
                if (user.getBlocked().containsKey(receivedWorkout.getSender())) {
                    blockUserTV.setVisibility(View.GONE);
                }
                blockUserTV.setOnClickListener(view -> {
                    bottomSheetDialog.dismiss();
                    blockUserPopup(receivedWorkout.getSender());
                });

                RelativeLayout relativeLayout = sheetView.findViewById(R.id.username_pic_container);
                relativeLayout.setOnClickListener(v1 -> showBlownUpProfilePic(receivedWorkout.getSender(), receivedWorkout.getSenderIcon()));
                TextView usernameTV = sheetView.findViewById(R.id.username_tv);
                ImageView profilePicture = sheetView.findViewById(R.id.profile_picture);
                usernameTV.setText(receivedWorkout.getSender());

                Picasso.get()
                        .load(ImageUtils.getIconUrl(receivedWorkout.getSenderIcon()))
                        .error(R.drawable.picture_load_error)
                        .networkPolicy(NetworkPolicy.NO_CACHE) // on first loading in app, always fetch online
                        .into(profilePicture, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                if (!ReceivedWorkoutsFragment.this.isResumed()) {
                                    return;
                                }
                                Bitmap imageBitmap = ((BitmapDrawable) profilePicture.getDrawable()).getBitmap();
                                RoundedBitmapDrawable imageDrawable = RoundedBitmapDrawableFactory.create(getResources(), imageBitmap);
                                imageDrawable.setCircular(true);
                                imageDrawable.setCornerRadius(Math.max(imageBitmap.getWidth(), imageBitmap.getHeight()) / 2.0f);
                                profilePicture.setImageDrawable(imageDrawable);
                            }

                            @Override
                            public void onError(Exception e) {
                            }
                        });

                bottomSheetDialog.setContentView(sheetView);
                bottomSheetDialog.show();
            });
        }

        void refresh(List<SharedWorkoutMeta> receivedWorkouts) {
            this.receivedWorkouts = receivedWorkouts;
            notifyDataSetChanged();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return receivedWorkouts.size();
        }
    }
}
