package com.joshrap.liteweight.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.activities.WorkoutActivity;
import com.joshrap.liteweight.imports.Globals;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.interfaces.FragmentWithDialog;
import com.joshrap.liteweight.models.AcceptWorkoutResponse;
import com.joshrap.liteweight.models.ReceivedWorkoutMeta;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.models.User;
import com.joshrap.liteweight.models.Workout;
import com.joshrap.liteweight.network.repos.UserRepository;
import com.joshrap.liteweight.network.repos.WorkoutRepository;
import com.joshrap.liteweight.widgets.ErrorDialog;

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
    private Workout currentWorkout;
    private List<ReceivedWorkoutMeta> receivedWorkouts;
    private int currentBatch;
    private ProgressBar loadingIcon;
    private RecyclerView receivedWorkoutsRecyclerView;
    private ImageButton markAllReceivedWorkoutsSeen;
    private TextView emptyView;
    private BottomSheetDialog bottomSheetDialog;
    private AlertDialog alertDialog;
    private ReceivedWorkoutsAdapter receivedWorkoutsAdapter;
    @Inject
    ProgressDialog loadingDialog;
    @Inject
    WorkoutRepository workoutRepository;
    @Inject
    UserRepository userRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((WorkoutActivity) getActivity()).updateToolbarTitle(Variables.RECEIVED_WORKOUTS_TITLE);
        ((WorkoutActivity) getActivity()).toggleBackButton(true);
        Injector.getInjector(getContext()).inject(this);
        user = Globals.user;
        currentWorkout = Globals.activeWorkout;
        currentBatch = 0;
        receivedWorkouts = new ArrayList<>();
        final View view = inflater.inflate(R.layout.fragment_received_workouts, container, false);

        loadingIcon = view.findViewById(R.id.loading_icon);
        loadingIcon.setVisibility(View.VISIBLE);
        receivedWorkoutsRecyclerView = view.findViewById(R.id.recycler_view);
        receivedWorkoutsRecyclerView.setVisibility(View.GONE);
        emptyView = view.findViewById(R.id.empty_view);
        emptyView.setVisibility(View.GONE);
        markAllReceivedWorkoutsSeen = view.findViewById(R.id.mark_all_read_btn);
        markAllReceivedWorkoutsSeen.setVisibility(View.GONE);
        markAllReceivedWorkoutsSeen.setOnClickListener(view1 -> setAllReceivedWorkoutsSeen());

        getReceivedWorkouts();
        return view;
    }

    private void getReceivedWorkouts() {
        loadingIcon.setVisibility(View.VISIBLE);
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ResultStatus<List<ReceivedWorkoutMeta>> resultStatus = this.workoutRepository.getReceivedWorkouts(currentBatch);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                if (this.isResumed()) {
                    // not sure if this does what i think it does? But would prevent potential memory leaks
                    loadingIcon.setVisibility(View.GONE);
                    if (resultStatus.isSuccess()) {

                        receivedWorkouts = resultStatus.getData();
                        Collections.sort(receivedWorkouts, (r1, r2) -> {
                            // sanity sort based on received workout sent time
                            DateFormat dateFormattter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.ENGLISH);
                            dateFormattter.setTimeZone(TimeZone.getTimeZone("UTC"));
                            int retVal = 0;
                            try {
                                Date date1 = dateFormattter.parse(r1.getDateSent());
                                Date date2 = dateFormattter.parse(r2.getDateSent());
                                retVal = date1 != null ? date2.compareTo(date1) : 0;
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            return retVal;
                        });
                        checkAllSeenButton();
                        displayReceivedWorkouts();
                    } else {
                        ErrorDialog.showErrorDialog("Load Received Workouts Error", resultStatus.getErrorMessage(), getContext());
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

    private void checkEmpty() {
        /*
            Used to check if there are any received workouts
         */
        emptyView.setVisibility(receivedWorkouts.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void checkAllSeenButton() {
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
        showLoadingDialog("Blocking user...");
        // todo also just go ahead and decline the workout?

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
                    ErrorDialog.showErrorDialog("Error", resultStatus.getErrorMessage(), getContext());
                }
            });
        });
    }

    private void acceptWorkout(final ReceivedWorkoutMeta workoutToAccept) {
        showLoadingDialog("Accepting...");

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ResultStatus<AcceptWorkoutResponse> resultStatus = this.workoutRepository.acceptReceivedWorkout(workoutToAccept.getWorkoutId());
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
                    if (!workoutToAccept.isSeen()) {
                        // this workout was not seen, so make sure to decrease the unseen count since it is no longer in the list
                        user.setUnseenReceivedWorkouts(user.getUnseenReceivedWorkouts() - 1);
                    }
                    receivedWorkouts.remove(workoutToAccept);
                    receivedWorkoutsAdapter.notifyDataSetChanged();
                    checkAllSeenButton();
                } else {
                    ErrorDialog.showErrorDialog("Error", resultStatus.getErrorMessage(), getContext());
                }
            });
        });
    }

    private void workoutNameAlreadyExistsPopup(){

    }

    private void setReceivedWorkoutSeen(String workoutId) {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            // blind send for marking a workout read
            this.workoutRepository.setReceivedWorkoutSeen(workoutId);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                user.setUnseenReceivedWorkouts(user.getUnseenReceivedWorkouts() - 1);
                checkAllSeenButton();
            });
        });
    }

    private void setAllReceivedWorkoutsSeen() {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            // blind send for marking all workout read
            this.workoutRepository.setAllReceivedWorkoutsSeen();
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                markAllReceivedWorkoutsSeen.setVisibility(View.GONE);
                user.setUnseenReceivedWorkouts(0);
                for (ReceivedWorkoutMeta receivedWorkoutMeta : receivedWorkouts) {
                    receivedWorkoutMeta.setSeen(true);
                }
                displayReceivedWorkouts(); // to remove any unseen indicators
            });
        });
    }

    private void showLoadingDialog(String message) {
        // todo make this a static method
        loadingDialog.setMessage(message);
        loadingDialog.show();
    }

    @Override
    public void onPause() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
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

        private List<ReceivedWorkoutMeta> receivedWorkouts;

        ReceivedWorkoutsAdapter(List<ReceivedWorkoutMeta> receivedWorkouts) {
            this.receivedWorkouts = receivedWorkouts;
        }


        @Override
        public ReceivedWorkoutsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View exerciseView = inflater.inflate(R.layout.row_received_workout, parent, false);
            return new ViewHolder(exerciseView);
        }

        // Involves populating data into the item through holder
        @Override
        public void onBindViewHolder(ReceivedWorkoutsAdapter.ViewHolder holder, int position) {
            // Get the data model based on position
            final ReceivedWorkoutMeta receivedWorkout = receivedWorkouts.get(position);

            final TextView workoutNameTV = holder.workoutNameTV;
            final TextView senderTV = holder.senderTV;
            final TextView dateSentTv = holder.dateSentTV;
            final Button acceptButton = holder.acceptButton;
            final Button declineButton = holder.declineButton;

            acceptButton.setOnClickListener(view -> acceptWorkout(receivedWorkout));
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
            if (!receivedWorkout.isSeen()) {
                // if unseen, make it underlined and bold to catch user's attention
                dateSentTv.setPaintFlags(dateSentTv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                dateSentTv.setTypeface(dateSentTv.getTypeface(), Typeface.BOLD);
            }
            workoutNameTV.setText(receivedWorkout.getWorkoutName());
            final RelativeLayout rootLayout = holder.rootLayout;
            rootLayout.setOnClickListener(v -> {
                if (!receivedWorkout.isSeen()) {
                    // when user clicks on the workout, mark it as seen
                    setReceivedWorkoutSeen(receivedWorkout.getWorkoutId());
                    receivedWorkout.setSeen(true);
                    dateSentTv.setTypeface(null, Typeface.NORMAL);
                    dateSentTv.setPaintFlags(0);
                }

                bottomSheetDialog = new BottomSheetDialog(getActivity(), R.style.BottomSheetDialogTheme);
                View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_received_workout, null);
                final TextView browseWorkout = sheetView.findViewById(R.id.browse_workout_tv);
                final TextView workoutNameBottomSheetTV = sheetView.findViewById(R.id.workout_name_tv);
                workoutNameBottomSheetTV.setText(receivedWorkout.getWorkoutName());
                final TextView workoutMetaTV = sheetView.findViewById(R.id.workout_meta_tv);
                workoutMetaTV.setText(String.format("Most frequent focus: %s\nNumber of days: %d",
                        receivedWorkout.getMostFrequentFocus().replaceAll(",", ", "), receivedWorkout.getTotalDays()));
                browseWorkout.setOnClickListener(v1 -> {
                    ((WorkoutActivity) getActivity()).goToBrowseReceivedWorkout(receivedWorkout.getWorkoutId(), receivedWorkout.getWorkoutName());
                    bottomSheetDialog.dismiss();
                });
                final TextView blockUser = sheetView.findViewById(R.id.block_user_tv);
                blockUser.setOnClickListener(view -> {
                    bottomSheetDialog.dismiss();
                    blockUserPopup(receivedWorkout.getSender());
                });

                bottomSheetDialog.setContentView(sheetView);
                bottomSheetDialog.show();
            });
        }

        @Override
        public int getItemCount() {
            return receivedWorkouts.size();
        }
    }
}
