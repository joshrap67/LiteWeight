package com.joshrap.liteweight.fragments;

import androidx.appcompat.app.AlertDialog;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputLayout;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.activities.MainActivity;
import com.joshrap.liteweight.managers.UserManager;
import com.joshrap.liteweight.managers.WorkoutManager;
import com.joshrap.liteweight.messages.fragmentmessages.ReceivedWorkoutFragmentMessage;
import com.joshrap.liteweight.models.AcceptWorkoutResponse;
import com.joshrap.liteweight.models.WorkoutMeta;
import com.joshrap.liteweight.providers.CurrentUserAndWorkoutProvider;
import com.joshrap.liteweight.utils.AndroidUtils;
import com.joshrap.liteweight.utils.TimeUtils;
import com.joshrap.liteweight.utils.ImageUtils;
import com.joshrap.liteweight.utils.ValidatorUtils;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.interfaces.FragmentWithDialog;
import com.joshrap.liteweight.models.SharedWorkoutMeta;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.models.User;
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

public class ReceivedWorkoutsFragment extends Fragment implements FragmentWithDialog {
    private User user;
    private List<SharedWorkoutMeta> receivedWorkouts;
    private ProgressBar listLoadingIcon;
    private RelativeLayout topContainer;
    private RecyclerView receivedWorkoutsRecyclerView;
    private Button markAllReceivedWorkoutsSeenButton;
    private TextView emptyViewTV, totalReceivedTV;
    private BottomSheetDialog bottomSheetDialog;
    private AlertDialog alertDialog;
    private ReceivedWorkoutsAdapter receivedWorkoutsAdapter;
    private boolean isGettingNextBatch;

    @Inject
    AlertDialog loadingDialog;
    @Inject
    WorkoutManager workoutManager;
    @Inject
    UserManager userManager;
    @Inject
    CurrentUserAndWorkoutProvider currentUserAndWorkoutProvider;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        ((MainActivity) getActivity()).updateToolbarTitle(Variables.RECEIVED_WORKOUTS_TITLE);
        ((MainActivity) getActivity()).toggleBackButton(false);
        Injector.getInjector(getContext()).inject(this);
        user = currentUserAndWorkoutProvider.provideCurrentUserAndWorkout().getUser();
        isGettingNextBatch = false;
        receivedWorkouts = new ArrayList<>();

        View view = inflater.inflate(R.layout.fragment_received_workouts, container, false);

        listLoadingIcon = view.findViewById(R.id.loading_progress_bar);
        listLoadingIcon.setVisibility(View.INVISIBLE);
        receivedWorkoutsRecyclerView = view.findViewById(R.id.received_workouts_recycler_view);
        emptyViewTV = view.findViewById(R.id.empty_view_tv);
        totalReceivedTV = view.findViewById(R.id.total_received_tv);
        topContainer = view.findViewById(R.id.top_container);
        markAllReceivedWorkoutsSeenButton = view.findViewById(R.id.mark_all_seen_btn);
        markAllReceivedWorkoutsSeenButton.setOnClickListener(view1 -> setAllReceivedWorkoutsSeen());

        setAndDisplayReceivedWorkouts();
        receivedWorkoutsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                // once reaching the end of list, check if more workouts are available to load
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
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (receivedWorkouts.size() != user.getReceivedWorkouts().size()) {
            // while app was in background, there was a notification which added a received workout - so update list
            setAndDisplayReceivedWorkouts();
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
        SharedWorkoutMeta sharedWorkoutMeta = message.getSharedWorkoutMeta();
        receivedWorkouts.removeIf(x -> x.getWorkoutId().equals(sharedWorkoutMeta.getWorkoutId()));
        receivedWorkouts.add(0, sharedWorkoutMeta);
        receivedWorkoutsAdapter.notifyDataSetChanged();

        // get rid of push notification since user is currently on this page
        NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(sharedWorkoutMeta.getWorkoutId().hashCode());
        }

        Toast.makeText(getContext(), "New workout: " + sharedWorkoutMeta.getWorkoutName(), Toast.LENGTH_LONG).show();
    }

    private void setAndDisplayReceivedWorkouts() {
        receivedWorkouts = new ArrayList<>(user.getReceivedWorkouts().values());
        sortReceivedWorkouts();
        removeReceivedWorkoutNotifications();
        displayReceivedWorkouts();
    }

    private void sortReceivedWorkouts() {
        receivedWorkouts.sort((r1, r2) -> {
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

    private void removeReceivedWorkoutNotifications() {
        // get rid of any push notification that might be in the status bar
        for (SharedWorkoutMeta sharedWorkoutMeta : user.getReceivedWorkouts().values()) {
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
            ResultStatus<List<SharedWorkoutMeta>> resultStatus = this.workoutManager.getReceivedWorkouts(batchNumber);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                if (this.isResumed()) {
                    listLoadingIcon.setVisibility(View.INVISIBLE);
                    isGettingNextBatch = false;
                    if (resultStatus.isSuccess()) {
                        if (resultStatus.getData().isEmpty()) {
                            return;
                        }
                        int startingIndex = this.receivedWorkouts.size();
                        this.receivedWorkouts.addAll(resultStatus.getData());
                        receivedWorkoutsAdapter.notifyItemChanged(startingIndex);

                        updateTopContainer();
                    } else {
                        AndroidUtils.showErrorDialog(resultStatus.getErrorMessage(), getContext());
                    }
                }
            });
        });
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
        updateTopContainer();
        if (receivedWorkouts.isEmpty() && user.getTotalReceivedWorkouts() > 0 && !isGettingNextBatch) {
            // still workouts left, so load next batch
            isGettingNextBatch = true;
            getNextBatch();
        } else {
            emptyViewTV.setVisibility(receivedWorkouts.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void updateTopContainer() {
        markAllReceivedWorkoutsSeenButton.setVisibility(user.getUnseenReceivedWorkouts() > 0 ? View.VISIBLE : View.INVISIBLE);
        topContainer.setVisibility(receivedWorkouts.isEmpty() ? View.GONE : View.VISIBLE);
        totalReceivedTV.setText(user.getTotalReceivedWorkouts() + (user.getTotalReceivedWorkouts() == 1 ? " WORKOUT" : " WORKOUTS"));
    }

    private void blockUserPopup(String username) {
        // username is italicized
        SpannableString span1 = new SpannableString("Are you sure you wish to block ");
        SpannableString span2 = new SpannableString(username);
        span2.setSpan(new StyleSpan(Typeface.ITALIC), 0, span2.length(), 0);
        SpannableString span3 = new SpannableString("? They will no longer be able to add you as a friend or send you any workouts.");
        CharSequence title = TextUtils.concat(span1, span2, span3);

        alertDialog = new AlertDialog.Builder(getContext())
                .setTitle("Block User")
                .setMessage(title)
                .setPositiveButton("Yes", (dialog, which) -> blockUser(username))
                .setNegativeButton("No", null)
                .create();
        alertDialog.show();
    }

    private void blockUser(String username) {
        AndroidUtils.showLoadingDialog(loadingDialog, "Blocking user...");
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ResultStatus<String> resultStatus = this.userManager.blockUser(username);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                loadingDialog.dismiss();
                if (resultStatus.isFailure()) {
                    AndroidUtils.showErrorDialog(resultStatus.getErrorMessage(), getContext());
                }
            });
        });
    }

    private void acceptWorkout(final SharedWorkoutMeta workoutToAccept, final String optionalName) {
        AndroidUtils.showLoadingDialog(loadingDialog, "Accepting...");

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ResultStatus<AcceptWorkoutResponse> resultStatus = this.workoutManager.acceptReceivedWorkout(workoutToAccept.getWorkoutId(), optionalName);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                loadingDialog.dismiss();
                if (resultStatus.isSuccess()) {
                    removeWorkout(workoutToAccept.getWorkoutId());
                    ((MainActivity) getActivity()).updateReceivedWorkoutNotificationIndicator();
                } else {
                    AndroidUtils.showErrorDialog(resultStatus.getErrorMessage(), getContext());
                }
            });
        });
    }

    private void removeWorkout(String workoutId) {
        int index = -1;
        for (int i = 0; i < receivedWorkouts.size(); i++) {
            SharedWorkoutMeta workoutMeta = receivedWorkouts.get(i);
            if (workoutMeta.getWorkoutId().equals(workoutId)) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            this.receivedWorkouts.remove(index);
            receivedWorkoutsAdapter.notifyItemRemoved(index);
        }
    }

    private void declineWorkout(SharedWorkoutMeta sharedWorkoutMeta) {
        AndroidUtils.showLoadingDialog(loadingDialog, "Declining...");

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ResultStatus<String> resultStatus = this.workoutManager.declineReceivedWorkout(sharedWorkoutMeta.getWorkoutId());
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                loadingDialog.dismiss();
                if (resultStatus.isSuccess() && ReceivedWorkoutsFragment.this.isResumed()) {
                    // if it was unread, then we need to make sure to decrease unseen count
                    ((MainActivity) getActivity()).updateReceivedWorkoutNotificationIndicator();
                    removeWorkout(sharedWorkoutMeta.getWorkoutId());
                } else {
                    AndroidUtils.showErrorDialog(resultStatus.getErrorMessage(), getContext());
                }
            });
        });
    }

    private void workoutNameAlreadyExistsPopup(final SharedWorkoutMeta sharedWorkoutMeta) {
        View popupView = getLayoutInflater().inflate(R.layout.popup_workout_name_exists, null);
        EditText renameInput = popupView.findViewById(R.id.rename_workout_name_input);
        TextInputLayout workoutNameInputLayout = popupView.findViewById(R.id.rename_workout_name_input_layout);
        renameInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_WORKOUT_NAME)});
        renameInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(workoutNameInputLayout));

        // username is italicized
        SpannableString span1 = new SpannableString(sharedWorkoutMeta.getWorkoutName());
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
                    acceptWorkout(sharedWorkoutMeta, newName);
                    alertDialog.dismiss();
                } else {
                    workoutNameInputLayout.setError(errorMsg);
                }
            });
        });
        alertDialog.show();
    }

    private void setReceivedWorkoutSeen(int position, String workoutId) {
        if (user.getUnseenReceivedWorkouts() == 1) {
            // this is the last unseen workout so hide mark all seen button
            markAllReceivedWorkoutsSeenButton.setVisibility(View.INVISIBLE);
        }

        receivedWorkouts.get(position).setSeen(true); // breaking pattern by duplicating the manager code due to blind send
        receivedWorkoutsAdapter.notifyItemChanged(position, ReceivedWorkoutsAdapter.PAYLOAD_UPDATE_SEEN_STATUS);
        ((MainActivity) getActivity()).updateReceivedWorkoutNotificationIndicator(user.getUnseenReceivedWorkouts() - 1);

        // blind send
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> this.userManager.setReceivedWorkoutSeen(workoutId));
    }

    private void setAllReceivedWorkoutsSeen() {
        markAllReceivedWorkoutsSeenButton.setVisibility(View.INVISIBLE);
        for (SharedWorkoutMeta sharedWorkoutMeta : receivedWorkouts) {
            // breaking pattern by duplicating the manager code due to blind send
            sharedWorkoutMeta.setSeen(true);
        }
        receivedWorkoutsAdapter.notifyItemRangeChanged(0, receivedWorkoutsAdapter.getItemCount(), ReceivedWorkoutsAdapter.PAYLOAD_UPDATE_SEEN_STATUS);
        ((MainActivity) getActivity()).updateReceivedWorkoutNotificationIndicator(0);

        // blind send
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> this.userManager.setAllReceivedWorkoutsSeen());
    }

    private void showBlownUpProfilePic(String username, String iconUrl) {
        View popupView = getLayoutInflater().inflate(R.layout.popup_blown_up_profile_picture, null);
        ImageView profilePicture = popupView.findViewById(R.id.profile_picture_image);
        Picasso.get()
                .load(ImageUtils.getIconUrl(iconUrl))
                .error(R.drawable.picture_load_error)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .into(profilePicture);

        alertDialog = new AlertDialog.Builder(getContext())
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
            final Button acceptButton;
            final Button declineButton;
            final RelativeLayout rootLayout;

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
                SharedWorkoutMeta receivedWorkout = receivedWorkouts.get(position);
                for (Object payload : payloads) {
                    if (payload.equals(PAYLOAD_UPDATE_SEEN_STATUS)) {
                        // remove unseen indicator
                        holder.dateSentTV.setText(TimeUtils.getFormattedLocalDateTime(receivedWorkout.getDateSent()));
                        holder.dateSentTV.setTypeface(null, Typeface.NORMAL);
                    }
                }
            } else {
                super.onBindViewHolder(holder, position, payloads);
            }
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
                for (WorkoutMeta workoutMeta : user.getWorkoutMetas().values()) {
                    if (workoutMeta.getWorkoutName().equals(receivedWorkout.getWorkoutName())) {
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

            String dateSent = TimeUtils.getFormattedLocalDateTime(receivedWorkout.getDateSent());
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
                    setReceivedWorkoutSeen(holder.getAdapterPosition(), receivedWorkout.getWorkoutId());
                }

                bottomSheetDialog = new BottomSheetDialog(getActivity());
                View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_received_workout, null);
                TextView browseWorkout = sheetView.findViewById(R.id.browse_workout_tv);
                TextView workoutNameBottomSheetTV = sheetView.findViewById(R.id.workout_name_tv);
                workoutNameBottomSheetTV.setText(receivedWorkout.getWorkoutName());

                TextView workoutMetaTV = sheetView.findViewById(R.id.workout_meta_tv);
                String mostFrequentFocus = receivedWorkout.getMostFrequentFocus();
                if (mostFrequentFocus != null) {
                    workoutMetaTV.setText(String.format(Locale.ENGLISH, "Most frequent focus: %s\nNumber of days: %d",
                            mostFrequentFocus.replaceAll(",", ", "),
                            receivedWorkout.getTotalDays()));
                }

                browseWorkout.setOnClickListener(v1 -> {
                    ((MainActivity) getActivity()).goToBrowseReceivedWorkout(receivedWorkout.getWorkoutId(), receivedWorkout.getWorkoutName());
                    bottomSheetDialog.dismiss();
                });


                TextView blockUserTV = sheetView.findViewById(R.id.block_user_tv);
                if (user.isBlocking(receivedWorkout.getSender())) {
                    blockUserTV.setVisibility(View.GONE);
                }
                blockUserTV.setOnClickListener(view -> {
                    bottomSheetDialog.dismiss();
                    blockUserPopup(receivedWorkout.getSender());
                });

                RelativeLayout relativeLayout = sheetView.findViewById(R.id.username_pic_container);
                relativeLayout.setOnClickListener(v1 -> showBlownUpProfilePic(receivedWorkout.getSender(), receivedWorkout.getSenderIcon()));
                TextView usernameTV = sheetView.findViewById(R.id.username_tv);
                ImageView profilePicture = sheetView.findViewById(R.id.profile_picture_image);
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

        @Override
        public int getItemCount() {
            return receivedWorkouts.size();
        }
    }
}
