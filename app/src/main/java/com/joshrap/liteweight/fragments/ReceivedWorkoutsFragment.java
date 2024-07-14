package com.joshrap.liteweight.fragments;

import androidx.appcompat.app.AlertDialog;

import android.annotation.SuppressLint;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.activities.MainActivity;
import com.joshrap.liteweight.managers.ReceivedWorkoutManager;
import com.joshrap.liteweight.managers.UserManager;
import com.joshrap.liteweight.managers.WorkoutManager;
import com.joshrap.liteweight.messages.fragmentmessages.ReceivedWorkoutFragmentMessage;
import com.joshrap.liteweight.models.user.WorkoutInfo;
import com.joshrap.liteweight.managers.CurrentUserModule;
import com.joshrap.liteweight.utils.AndroidUtils;
import com.joshrap.liteweight.utils.TimeUtils;
import com.joshrap.liteweight.utils.ImageUtils;
import com.joshrap.liteweight.utils.ValidatorUtils;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.interfaces.FragmentWithDialog;
import com.joshrap.liteweight.models.user.ReceivedWorkoutInfo;
import com.joshrap.liteweight.models.Result;
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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

@SuppressLint({"InflateParams", "NotifyDataSetChanged"})
public class ReceivedWorkoutsFragment extends Fragment implements FragmentWithDialog {
    private List<ReceivedWorkoutInfo> receivedWorkouts;
    private RelativeLayout topContainer;
    private RecyclerView receivedWorkoutsRecyclerView;
    private Button markAllReceivedWorkoutsSeenButton;
    private TextView emptyViewTV, totalReceivedTV;
    private BottomSheetDialog bottomSheetDialog;
    private AlertDialog alertDialog;
    private ReceivedWorkoutsAdapter receivedWorkoutsAdapter;

    @Inject
    AlertDialog loadingDialog;
    @Inject
    WorkoutManager workoutManager;
    @Inject
    ReceivedWorkoutManager receivedWorkoutManager;
    @Inject
    UserManager userManager;
    @Inject
    CurrentUserModule currentUserModule;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentActivity fragmentActivity = requireActivity();
        fragmentActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        ((MainActivity) fragmentActivity).updateToolbarTitle(Variables.RECEIVED_WORKOUTS_TITLE);
        ((MainActivity) fragmentActivity).toggleBackButton(false);
        Injector.getInjector(getContext()).inject(this);
        receivedWorkouts = new ArrayList<>();

        View view = inflater.inflate(R.layout.fragment_received_workouts, container, false);

        receivedWorkoutsRecyclerView = view.findViewById(R.id.received_workouts_recycler_view);
        emptyViewTV = view.findViewById(R.id.empty_view_tv);
        totalReceivedTV = view.findViewById(R.id.total_received_tv);
        topContainer = view.findViewById(R.id.top_container);
        markAllReceivedWorkoutsSeenButton = view.findViewById(R.id.mark_all_seen_btn);
        markAllReceivedWorkoutsSeenButton.setOnClickListener(view1 -> setAllReceivedWorkoutsSeen());

        setAndDisplayReceivedWorkouts();
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
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (receivedWorkouts.size() != currentUserModule.getUser().getReceivedWorkouts().size()) {
            // while app was in background, there was a notification which added a received workout - so update list
            setAndDisplayReceivedWorkouts();
        } else {
            // while app was in background, a workout that was already sent has been sent again and needs to be moved up in the list
            boolean needsUpdating = false;
            for (int i = 0; i < receivedWorkouts.size(); i++) {
                ReceivedWorkoutInfo fragmentWorkoutInfo = receivedWorkouts.get(i);
                ReceivedWorkoutInfo upToDateInfo = currentUserModule.getUser().getReceivedWorkout(fragmentWorkoutInfo.getReceivedWorkoutId());
                if (!fragmentWorkoutInfo.getReceivedUtc().equals(upToDateInfo.getReceivedUtc())) {
                    needsUpdating = true;
                    break;
                }
            }
            if (needsUpdating) {
                setAndDisplayReceivedWorkouts();
            }
        }

        EventBus.getDefault().register(this);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleReceivedWorkoutMessage(ReceivedWorkoutFragmentMessage message) {
        ReceivedWorkoutInfo receivedWorkoutInfo = message.getReceivedWorkoutInfo();
        receivedWorkouts.removeIf(x -> x.getReceivedWorkoutId().equals(receivedWorkoutInfo.getReceivedWorkoutId()));
        receivedWorkouts.add(0, receivedWorkoutInfo);
        receivedWorkoutsAdapter.notifyDataSetChanged();

        // get rid of push notification since user is currently on this page
        NotificationManager notificationManager = (NotificationManager) requireActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(receivedWorkoutInfo.getReceivedWorkoutId().hashCode());
        }

        Toast.makeText(getContext(), "New workout - " + receivedWorkoutInfo.getWorkoutName(), Toast.LENGTH_LONG).show();
    }

    private void setAndDisplayReceivedWorkouts() {
        receivedWorkouts = new ArrayList<>(currentUserModule.getUser().getReceivedWorkouts());
        sortReceivedWorkouts();
        removeReceivedWorkoutNotifications();
        displayReceivedWorkouts();
    }

    private void sortReceivedWorkouts() {
        receivedWorkouts.sort((r1, r2) -> {
            // sort based on received workout sent time (newest at top)
            DateFormat dateFormatter = new SimpleDateFormat(TimeUtils.UTC_TIME_FORMAT, Locale.ENGLISH);
            dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            int retVal = 0;
            try {
                Date date1 = dateFormatter.parse(r1.getReceivedUtc());
                Date date2 = dateFormatter.parse(r2.getReceivedUtc());
                retVal = date1 != null && date2 != null ? date2.compareTo(date1) : 0;
            } catch (ParseException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
            return retVal;
        });
    }

    private void removeReceivedWorkoutNotifications() {
        // get rid of any push notification that might be in the status bar
        for (ReceivedWorkoutInfo receivedWorkoutInfo : receivedWorkouts) {
            if (!receivedWorkoutInfo.isSeen()) {
                // get rid of any push notification that might be there
                NotificationManager notificationManager = (NotificationManager) requireActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null) {
                    notificationManager.cancel(receivedWorkoutInfo.getReceivedWorkoutId().hashCode());
                }
            }
        }
    }

    private void displayReceivedWorkouts() {
        receivedWorkoutsRecyclerView.setVisibility(View.VISIBLE);
        receivedWorkoutsAdapter = new ReceivedWorkoutsAdapter();
        receivedWorkoutsAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                updatePage();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                updatePage();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                updatePage();
            }
        });
        receivedWorkoutsRecyclerView.setAdapter(receivedWorkoutsAdapter);
        receivedWorkoutsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        updatePage();
    }

    private void updatePage() {
        long totalUnseenWorkouts = currentUserModule.getUser().totalUnseenWorkouts();
        long totalReceivedWorkouts = currentUserModule.getUser().totalReceivedWorkouts();

        markAllReceivedWorkoutsSeenButton.setVisibility(totalUnseenWorkouts > 0 ? View.VISIBLE : View.INVISIBLE);
        topContainer.setVisibility(receivedWorkouts.isEmpty() ? View.GONE : View.VISIBLE);
        totalReceivedTV.setText(String.format(Locale.getDefault(), "%d%s", totalReceivedWorkouts, totalReceivedWorkouts == 1 ? " WORKOUT" : " WORKOUTS"));
        emptyViewTV.setVisibility(receivedWorkouts.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void promptReportUser(String userId, String username) {
        View popupView = getLayoutInflater().inflate(R.layout.popup_report_user, null);
        EditText reportInput = popupView.findViewById(R.id.report_user_input);
        TextInputLayout reportInputLayout = popupView.findViewById(R.id.report_user_input_layout);
        reportInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_REPORT_DESCRIPTION)});
        reportInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(reportInputLayout));

        // username is italicized
        SpannableString span1 = new SpannableString("Report ");
        SpannableString span2 = new SpannableString(username);
        span2.setSpan(new StyleSpan(Typeface.ITALIC), 0, span2.length(), 0);
        CharSequence title = TextUtils.concat(span1, span2);

        alertDialog = new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setView(popupView)
                .setPositiveButton("Report", null)
                .setNegativeButton("Cancel", null)
                .create();

        alertDialog.setOnShowListener(dialogInterface -> {
            Button reportButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            reportButton.setOnClickListener(view -> {
                String reportDescription = reportInput.getText().toString();
                String errorMsg = ValidatorUtils.validReportUserDescription(reportDescription);
                if (errorMsg == null) {
                    reportUser(userId, reportInput.getText().toString());
                    alertDialog.dismiss();
                } else {
                    reportInputLayout.setError(errorMsg);
                }
            });
        });
        alertDialog.show();
    }

    private void reportUser(String userId, String complaint) {
        AndroidUtils.showLoadingDialog(loadingDialog, "Reporting...");

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Result<String> result = this.userManager.reportUser(userId, complaint);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                loadingDialog.dismiss();
                if (result.isSuccess()) {
                    alertDialog = new AlertDialog.Builder(requireContext())
                            .setTitle("User reported")
                            .setMessage("Complaint received. Save receipt below for your records.\n\n" + result.getData())
                            .setPositiveButton("Ok", null)
                            .create();
                    alertDialog.setOnShowListener(dialogInterface -> {
                        View messageView = alertDialog.findViewById(android.R.id.message);
                        if (messageView instanceof TextView) {
                            // allow the complaint id to be selected and copied
                            ((TextView) messageView).setTextIsSelectable(true);
                        }
                    });
                    alertDialog.show();
                } else {
                    AndroidUtils.showErrorDialog(result.getErrorMessage(), getContext());
                }
            });
        });
    }

    private void acceptWorkout(final ReceivedWorkoutInfo workoutToAccept, final String optionalName) {
        AndroidUtils.showLoadingDialog(loadingDialog, "Accepting...");

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Result<String> result = this.receivedWorkoutManager.acceptReceivedWorkout(workoutToAccept.getReceivedWorkoutId(), optionalName);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                loadingDialog.dismiss();
                if (result.isSuccess()) {
                    removeWorkout(workoutToAccept.getReceivedWorkoutId());
                    ((MainActivity) requireActivity()).updateReceivedWorkoutNotificationIndicator();
                } else {
                    AndroidUtils.showErrorDialog(result.getErrorMessage(), getContext());
                }
            });
        });
    }

    private void removeWorkout(String workoutId) {
        int index = -1;
        for (int i = 0; i < receivedWorkouts.size(); i++) {
            ReceivedWorkoutInfo workoutInfo = receivedWorkouts.get(i);
            if (workoutInfo.getReceivedWorkoutId().equals(workoutId)) {
                index = i;
                break;
            }
        }
        if (index >= 0) {
            this.receivedWorkouts.remove(index);
            receivedWorkoutsAdapter.notifyItemRemoved(index);
        }
    }

    private void declineWorkout(ReceivedWorkoutInfo receivedWorkoutInfo) {
        AndroidUtils.showLoadingDialog(loadingDialog, "Declining...");

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Result<String> result = this.receivedWorkoutManager.declineReceivedWorkout(receivedWorkoutInfo.getReceivedWorkoutId());
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                loadingDialog.dismiss();
                if (result.isSuccess() && ReceivedWorkoutsFragment.this.isResumed()) {
                    // if it was unread, then we need to make sure to decrease unseen count
                    ((MainActivity) requireActivity()).updateReceivedWorkoutNotificationIndicator();
                    removeWorkout(receivedWorkoutInfo.getReceivedWorkoutId());
                } else {
                    AndroidUtils.showErrorDialog(result.getErrorMessage(), getContext());
                }
            });
        });
    }

    private void workoutNameAlreadyExistsPopup(final ReceivedWorkoutInfo receivedWorkoutInfo) {
        View popupView = getLayoutInflater().inflate(R.layout.popup_workout_name_exists, null);
        EditText renameInput = popupView.findViewById(R.id.rename_workout_name_input);
        TextInputLayout workoutNameInputLayout = popupView.findViewById(R.id.rename_workout_name_input_layout);
        renameInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_WORKOUT_NAME)});
        renameInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(workoutNameInputLayout));

        // username is italicized
        SpannableString span1 = new SpannableString(receivedWorkoutInfo.getWorkoutName());
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
                List<String> workoutNames = new ArrayList<>();
                for (WorkoutInfo workoutInfo : currentUserModule.getUser().getWorkouts()) {
                    workoutNames.add(workoutInfo.getWorkoutName());
                }
                String errorMsg = ValidatorUtils.validWorkoutName(newName, workoutNames);
                if (errorMsg == null) {
                    acceptWorkout(receivedWorkoutInfo, newName);
                    alertDialog.dismiss();
                } else {
                    workoutNameInputLayout.setError(errorMsg);
                }
            });
        });
        alertDialog.show();
    }

    private void setReceivedWorkoutSeen(int position, String workoutId) {
        long totalUnseenWorkouts = currentUserModule.getUser().totalUnseenWorkouts();
        if (totalUnseenWorkouts == 1) {
            // this is the last unseen workout so hide mark all seen button
            markAllReceivedWorkoutsSeenButton.setVisibility(View.INVISIBLE);
        }

        receivedWorkouts.get(position).setSeen(true); // breaking pattern by duplicating the manager logic due to blind send
        receivedWorkoutsAdapter.notifyItemChanged(position, ReceivedWorkoutsAdapter.PAYLOAD_UPDATE_SEEN_STATUS);
        ((MainActivity) requireActivity()).updateReceivedWorkoutNotificationIndicator(totalUnseenWorkouts - 1);

        // blind send
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> this.receivedWorkoutManager.setReceivedWorkoutSeen(workoutId));
    }

    private void setAllReceivedWorkoutsSeen() {
        markAllReceivedWorkoutsSeenButton.setVisibility(View.INVISIBLE);
        for (ReceivedWorkoutInfo receivedWorkoutInfo : receivedWorkouts) {
            // breaking pattern by duplicating the manager logic due to blind send
            receivedWorkoutInfo.setSeen(true);
        }
        receivedWorkoutsAdapter.notifyItemRangeChanged(0, receivedWorkoutsAdapter.getItemCount(), ReceivedWorkoutsAdapter.PAYLOAD_UPDATE_SEEN_STATUS);
        ((MainActivity) requireActivity()).updateReceivedWorkoutNotificationIndicator(0);

        // blind send
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> this.receivedWorkoutManager.setAllReceivedWorkoutsSeen());
    }

    private void showBlownUpProfilePic(String username, String pfpUrl) {
        View popupView = getLayoutInflater().inflate(R.layout.popup_blown_up_profile_picture, null);
        ImageView profilePicture = popupView.findViewById(R.id.profile_picture_image);
        Picasso.get()
                .load(ImageUtils.getProfilePictureUrl(pfpUrl))
                .error(R.drawable.picture_load_error)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .into(profilePicture);

        alertDialog = new AlertDialog.Builder(requireContext())
                .setTitle(username)
                .setView(popupView)
                .setPositiveButton("Done", null)
                .create();
        alertDialog.show();
    }

    private class ReceivedWorkoutsAdapter extends RecyclerView.Adapter<ReceivedWorkoutsAdapter.ViewHolder> {

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView workoutNameTV;
            final TextView dateSentTV;
            final TextView senderTV;
            final Button respondButton;
            final RelativeLayout rootLayout;

            ViewHolder(View itemView) {
                super(itemView);
                workoutNameTV = itemView.findViewById(R.id.workout_name_tv);
                dateSentTV = itemView.findViewById(R.id.date_sent_tv);
                senderTV = itemView.findViewById(R.id.sender_tv);
                rootLayout = itemView.findViewById(R.id.root_layout);
                respondButton = itemView.findViewById(R.id.respond_btn);
            }
        }

        public static final String PAYLOAD_UPDATE_SEEN_STATUS = "UPDATE_SEEN_STATUS";

        @NonNull
        @Override
        public ReceivedWorkoutsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View exerciseView = inflater.inflate(R.layout.row_received_workout, parent, false);
            return new ViewHolder(exerciseView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
            if (!payloads.isEmpty()) {
                ReceivedWorkoutInfo receivedWorkout = receivedWorkouts.get(position);
                for (Object payload : payloads) {
                    if (payload.equals(PAYLOAD_UPDATE_SEEN_STATUS)) {
                        // remove unseen indicator
                        holder.dateSentTV.setText(TimeUtils.getFormattedLocalDateTime(receivedWorkout.getReceivedUtc()));
                        holder.dateSentTV.setTypeface(null, Typeface.NORMAL);
                    }
                }
            } else {
                super.onBindViewHolder(holder, position, payloads);
            }
        }

        @Override
        public void onBindViewHolder(ReceivedWorkoutsAdapter.ViewHolder holder, int position) {
            final ReceivedWorkoutInfo receivedWorkout = receivedWorkouts.get(position);

            TextView workoutNameTV = holder.workoutNameTV;
            TextView senderTV = holder.senderTV;
            TextView dateSentTv = holder.dateSentTV;
            Button respondButton = holder.respondButton;

	        final PopupMenu dropDownRoutineDayMenu = getPopupMenu(respondButton, receivedWorkout);
	        respondButton.setOnClickListener(v -> dropDownRoutineDayMenu.show());

            senderTV.setText(String.format("Sent by: %s", receivedWorkout.getSenderUsername()));

            String dateSent = TimeUtils.getFormattedLocalDateTime(receivedWorkout.getReceivedUtc());
            if (!receivedWorkout.isSeen()) {
                dateSent += "   *";
                dateSentTv.setTypeface(dateSentTv.getTypeface(), Typeface.BOLD);
            }
            dateSentTv.setText(dateSent);

            String workoutName = receivedWorkout.getWorkoutName();
            workoutNameTV.setText(workoutName);

            RelativeLayout rootLayout = holder.rootLayout;
            rootLayout.setOnClickListener(v -> {
                if (!receivedWorkout.isSeen()) {
                    // when user clicks on the workout, mark it as seen
                    setReceivedWorkoutSeen(holder.getAdapterPosition(), receivedWorkout.getReceivedWorkoutId());
                }

                bottomSheetDialog = new BottomSheetDialog(requireContext());
                View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_received_workout, null);
                TextView browseWorkout = sheetView.findViewById(R.id.browse_workout_tv);
                TextView workoutNameBottomSheetTV = sheetView.findViewById(R.id.workout_name_tv);
                workoutNameBottomSheetTV.setText(receivedWorkout.getWorkoutName());

                TextView workoutInfoTV = sheetView.findViewById(R.id.workout_info_tv);
                String mostFrequentFocus = receivedWorkout.getMostFrequentFocus();
                if (mostFrequentFocus != null) {
                    workoutInfoTV.setText(String.format(Locale.ENGLISH, "Most frequent focus: %s\nNumber of days: %d",
                            mostFrequentFocus.replaceAll(",", ", "),
                            receivedWorkout.getTotalDays()));
                }

                browseWorkout.setOnClickListener(v1 -> {
                    ((MainActivity) requireActivity()).goToBrowseReceivedWorkout(receivedWorkout.getReceivedWorkoutId(), receivedWorkout.getWorkoutName());
                    bottomSheetDialog.dismiss();
                });


                TextView reportUserTV = sheetView.findViewById(R.id.report_user_tv);
                reportUserTV.setOnClickListener(view -> {
                    bottomSheetDialog.dismiss();
                    promptReportUser(receivedWorkout.getSenderId(), receivedWorkout.getSenderUsername());
                });

                RelativeLayout relativeLayout = sheetView.findViewById(R.id.username_pic_container);
                relativeLayout.setOnClickListener(v1 -> showBlownUpProfilePic(receivedWorkout.getSenderUsername(), receivedWorkout.getSenderProfilePicture()));
                TextView usernameTV = sheetView.findViewById(R.id.username_tv);
                ImageView profilePicture = sheetView.findViewById(R.id.profile_picture_image);
                usernameTV.setText(receivedWorkout.getSenderUsername());

                Picasso.get()
                        .load(ImageUtils.getProfilePictureUrl(receivedWorkout.getSenderProfilePicture()))
                        .error(R.drawable.picture_load_error)
                        .networkPolicy(NetworkPolicy.NO_CACHE) // on first loading in app, always fetch online
                        .into(profilePicture);

                bottomSheetDialog.setContentView(sheetView);
                bottomSheetDialog.show();
            });
        }

	    private PopupMenu getPopupMenu(Button respondButton, ReceivedWorkoutInfo receivedWorkout) {
		    final PopupMenu dropDownRoutineDayMenu = new PopupMenu(getContext(), respondButton);
		    Menu moreMenu = dropDownRoutineDayMenu.getMenu();
		    final int acceptWorkoutId = 0;
		    final int declineWorkoutId = 1;
		    moreMenu.add(0, acceptWorkoutId, 0, "Accept Workout");
		    moreMenu.add(0, declineWorkoutId, 0, "Decline Workout");

		    dropDownRoutineDayMenu.setOnMenuItemClickListener(item -> {
		        switch (item.getItemId()) {
		            case acceptWorkoutId:
		                boolean workoutNameExists = currentUserModule.getUser().getWorkouts().stream().anyMatch(x -> x.getWorkoutName().equals(receivedWorkout.getWorkoutName()));
		                if (workoutNameExists) {
		                    workoutNameAlreadyExistsPopup(receivedWorkout);
		                } else {
		                    acceptWorkout(receivedWorkout, null);
		                }
		                return true;
		            case declineWorkoutId:
		                declineWorkout(receivedWorkout);
		                return true;
		        }
		        return false;
		    });
		    return dropDownRoutineDayMenu;
	    }

	    @Override
        public int getItemCount() {
            return receivedWorkouts.size();
        }
    }
}
