package com.joshrap.liteweight.fragments;

import static android.os.Looper.getMainLooper;

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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputLayout;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.activities.MainActivity;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.interfaces.FragmentWithDialog;
import com.joshrap.liteweight.managers.SharedWorkoutManager;
import com.joshrap.liteweight.managers.UserManager;
import com.joshrap.liteweight.managers.WorkoutManager;
import com.joshrap.liteweight.messages.fragmentmessages.AcceptedFriendRequestFragmentMessage;
import com.joshrap.liteweight.messages.fragmentmessages.CanceledFriendRequestFragmentMessage;
import com.joshrap.liteweight.messages.fragmentmessages.DeclinedFriendRequestFragmentMessage;
import com.joshrap.liteweight.messages.fragmentmessages.NewFriendRequestFragmentMessage;
import com.joshrap.liteweight.messages.fragmentmessages.RemovedFriendFragmentMessage;
import com.joshrap.liteweight.models.user.Friend;
import com.joshrap.liteweight.models.user.FriendRequest;
import com.joshrap.liteweight.models.Result;
import com.joshrap.liteweight.models.user.User;
import com.joshrap.liteweight.models.user.WorkoutInfo;
import com.joshrap.liteweight.providers.CurrentUserAndWorkoutProvider;
import com.joshrap.liteweight.utils.AndroidUtils;
import com.joshrap.liteweight.utils.TimeUtils;
import com.joshrap.liteweight.utils.ImageUtils;
import com.joshrap.liteweight.utils.ValidatorUtils;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

public class FriendsListFragment extends Fragment implements FragmentWithDialog {
    private static final int FRIENDS_POSITION = 0;
    public static final int REQUESTS_POSITION = 1;

    private User user;
    private FloatingActionButton floatingActionButton;
    private TextView emptyView;
    private RecyclerView recyclerView;
    private AlertDialog alertDialog;
    private BottomSheetDialog bottomSheetDialog;
    private List<Friend> friends;
    private List<FriendRequest> friendRequests;
    private FriendsAdapter friendsAdapter;
    private FriendRequestsAdapter friendRequestsAdapter;
    private TabLayout tabLayout;
    private int currentIndex;
    private NotificationManager notificationManager;

    @Inject
    AlertDialog loadingDialog;
    @Inject
    WorkoutManager workoutManager;
    @Inject
    SharedWorkoutManager sharedWorkoutManager;
    @Inject
    UserManager userManager;
    @Inject
    CurrentUserAndWorkoutProvider currentUserAndWorkoutProvider;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        Injector.getInjector(getContext()).inject(this);
        ((MainActivity) getActivity()).updateToolbarTitle(Variables.FRIENDS_LIST_TITLE);
        ((MainActivity) getActivity()).toggleBackButton(true);

        user = currentUserAndWorkoutProvider.provideCurrentUser();
        notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);

        Bundle args = getArguments();
        if (args != null) {
            // there are args which indicates user clicked on a notification, so bring them to the requests position
            currentIndex = REQUESTS_POSITION;
        } else {
            currentIndex = FRIENDS_POSITION;
        }
        return inflater.inflate(R.layout.fragment_friends_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        friends = new ArrayList<>(user.getFriends());
        friendRequests = new ArrayList<>(user.getFriendRequests());

        sortFriendRequestList();
        sortFriendsList();

        friendRequestsAdapter = new FriendRequestsAdapter();
        friendsAdapter = new FriendsAdapter();

        emptyView = view.findViewById(R.id.empty_view_tv);
        recyclerView = view.findViewById(R.id.friends_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        floatingActionButton = view.findViewById(R.id.add_friend_fab);
        floatingActionButton.setOnClickListener(v -> sendFriendRequestPopup());
        tabLayout = view.findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Friends"), FRIENDS_POSITION);
        tabLayout.addTab(tabLayout.newTab().setText("Friend Requests"), REQUESTS_POSITION);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == FRIENDS_POSITION) {
                    switchToFriendsList();
                } else if (tab.getPosition() == REQUESTS_POSITION) {
                    switchToRequestsList();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab.getPosition() == REQUESTS_POSITION) {
                    markAllFriendRequestsSeen();
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        if (currentIndex == REQUESTS_POSITION) {
            tabLayout.getTabAt(REQUESTS_POSITION).select();
        } else {
            switchToFriendsList();
        }
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        // sanity check to determine if user has any unseen requests after this fragment is paused
        markAllFriendRequestsSeen();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        clearFriendsNotifications();
        // when resuming, a notification could have affected these data. Need to populate what is missing into the local view variables
        if (user.getFriendRequests().size() != friendRequests.size()) {
            friendRequests = new ArrayList<>(user.getFriendRequests());
            sortFriendRequestList();
            friendRequestsAdapter.notifyDataSetChanged();
        }
        if (user.getFriends().size() != friends.size()) {
            friends = new ArrayList<>(user.getFriends());
            sortFriendsList();
            friendsAdapter.notifyDataSetChanged();
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
    public void handleNewFriendRequestMessage(NewFriendRequestFragmentMessage message) {
        FriendRequest newFriendRequest = message.getFriendRequest();
        friendRequests.add(0, newFriendRequest);
        friendRequestsAdapter.notifyItemInserted(0);

        Toast.makeText(getContext(), newFriendRequest.getUsername() + " sent you a friend request.", Toast.LENGTH_LONG).show();
        if (tabLayout.getSelectedTabPosition() == FRIENDS_POSITION) {
            tabLayout.getTabAt(REQUESTS_POSITION).setText("Friend Requests (!)");
        }

        // user is on this page, so no need to show a push notification
        if (notificationManager != null) {
            notificationManager.cancel(newFriendRequest.getUsername().hashCode());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleCanceledFriendRequestMessage(CanceledFriendRequestFragmentMessage message) {
        String usernameToRemove = message.getUserIdToRemove();
        int index = getFriendRequestPosition(usernameToRemove);

        if (index >= 0) {
            friendRequests.remove(index);
            friendRequestsAdapter.notifyItemRemoved(index);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleDeclinedFriendRequestMessage(DeclinedFriendRequestFragmentMessage message) {
        removeFriendFromList(message.getUserIdToRemove());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleRemovedFriendMessage(RemovedFriendFragmentMessage message) {
        removeFriendFromList(message.getUserIdToRemove());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleAcceptedFriendRequestMessage(AcceptedFriendRequestFragmentMessage message) {
        int index = getFriendPosition(message.getAcceptedUserId());
        if (index >= 0) {
            friendsAdapter.notifyItemChanged(index);
        }

        // user is on this page, so no need to show a push notification
        clearFriendsNotifications();
    }

    private void clearFriendsNotifications() {
        if (notificationManager == null) {
            return;
        }

        for (Friend friend : user.getFriends()) {
            notificationManager.cancel(friend.getUserId().hashCode());
        }
    }

    private void clearFriendRequestNotifications() {
        if (notificationManager == null) {
            return;
        }

        for (FriendRequest friendRequest : friendRequests) {
            if (!friendRequest.isSeen()) {
                notificationManager.cancel(friendRequest.getUsername().hashCode());
            }
        }
    }

    private int getFriendRequestPosition(String userId) {
        int index = -1;
        for (int i = 0; i < friendRequests.size(); i++) {
            FriendRequest friendRequest = friendRequests.get(i);
            if (friendRequest.getUserId().equals(userId)) {
                index = i;
                break;
            }
        }
        return index;
    }

    private int getFriendPosition(String userId) {
        int index = -1;
        for (int i = 0; i < friends.size(); i++) {
            Friend friend = friends.get(i);
            if (friend.getUserId().equals(userId)) {
                index = i;
                break;
            }
        }
        return index;
    }

    private void removeFriendFromList(String usernameToRemove) {
        int index = getFriendPosition(usernameToRemove);

        if (index >= 0) {
            friends.remove(index);
            friendsAdapter.notifyItemRemoved(index);
        }
    }

    private void sortFriendsList() {
        friends.sort(Comparator.comparing(friend -> friend.getUsername().toLowerCase()));
    }

    private void sortFriendRequestList() {
        // newest at the top
        friendRequests.sort((fr1, fr2) -> {
            DateFormat df = new SimpleDateFormat(TimeUtils.ZULU_TIME_FORMAT, Locale.ENGLISH);
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            int retVal = 0;
            try {
                Date date1 = df.parse(fr1.getSentUtc());
                Date date2 = df.parse(fr2.getSentUtc());
                retVal = date1 != null ? date2.compareTo(date1) : 0;
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return retVal;
        });
    }

    private void switchToFriendsList() {
        boolean requestsUnseen = friendRequests.stream().anyMatch(x -> !x.isSeen());
        tabLayout.getTabAt(REQUESTS_POSITION).setText(requestsUnseen ? "Friend Requests (!)" : "Friend Requests");
        floatingActionButton.show();
        checkEmptyList();
        friendsAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkEmptyList();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                checkEmptyList();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                checkEmptyList();
            }
        });
        recyclerView.setAdapter(friendsAdapter);
    }

    private void switchToRequestsList() {
        tabLayout.getTabAt(REQUESTS_POSITION).setText("Friend Requests"); // when user clicks on this tab, all requests are set to "seen"
        floatingActionButton.hide();
        checkEmptyList();
        friendRequestsAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkEmptyList();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                checkEmptyList();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                checkEmptyList();
            }
        });
        recyclerView.setAdapter(friendRequestsAdapter);
        clearFriendRequestNotifications();
    }

    private void checkEmptyList() {
        if (tabLayout.getSelectedTabPosition() == FRIENDS_POSITION) {
            emptyView.setVisibility(friends.isEmpty() ? View.VISIBLE : View.GONE);
            emptyView.setText(getString(R.string.empty_friend_list_msg));
        } else if (tabLayout.getSelectedTabPosition() == REQUESTS_POSITION) {
            emptyView.setVisibility(friendRequests.isEmpty() ? View.VISIBLE : View.GONE);
            emptyView.setText(getString(R.string.empty_friends_request_msg));
        }
    }

    private void markAllFriendRequestsSeen() {
        if (getActivity() != null) {
            ((MainActivity) getActivity()).clearAccountNotificationIndicator();
        }

        if (friendRequests.stream().anyMatch(x -> !x.isSeen())) {
            // prevents useless api calls
            clearFriendRequestNotifications();
            friendRequestsAdapter.notifyItemRangeChanged(0, friendRequests.size(), FriendRequestsAdapter.PAYLOAD_UPDATE_SEEN_STATUS);
            for (FriendRequest friendRequest : friendRequests) {
                // technically this is duplicated in below manager call. breaking the pattern to avoid my account page having unseen indicator if goes back before below api call finishes
                friendRequest.setSeen(true);
            }

            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> userManager.setAllFriendRequestsSeen());
        }
    }

    private void sendFriendRequestPopup() {
        View popupView = getLayoutInflater().inflate(R.layout.popup_add_friend, null);
        TextInputLayout friendNameLayout = popupView.findViewById(R.id.username_input_layout);
        EditText friendInput = popupView.findViewById(R.id.username_input);
        friendInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(friendNameLayout));
        friendInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_USERNAME_LENGTH)});
        alertDialog = new AlertDialog.Builder(getContext())
                .setTitle("Add Friend")
                .setView(popupView)
                .setPositiveButton("Send Request", null)
                .setNegativeButton("Close", null)
                .create();
        alertDialog.setOnShowListener(dialogInterface -> {
            Button saveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            saveButton.setOnClickListener(view -> {
                // usernames are case insensitive!
                String friendUsername = friendInput.getText().toString().trim().toLowerCase();
                List<String> existingFriends = new ArrayList<>();
                List<String> existingFriendRequests = new ArrayList<>();
                for (Friend friend : friends) {
                    existingFriends.add(friend.getUsername());
                }
                for (FriendRequest friendRequest : friendRequests) {
                    existingFriendRequests.add(friendRequest.getUsername());
                }
                String errorMsg = ValidatorUtils.validNewFriend(user.getUsername(), friendUsername, existingFriends, existingFriendRequests);
                if (errorMsg != null) {
                    friendNameLayout.setError(errorMsg);
                } else {
                    // no problems so go ahead and send friend request
                    alertDialog.dismiss();
                    sendFriendRequest(friendUsername);
                }
            });
        });
        alertDialog.show();
    }

    private void sendFriendRequest(String username) {
        AndroidUtils.showLoadingDialog(loadingDialog, "Sending request...");
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Result<Friend> result = this.userManager.sendFriendRequest(username);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                loadingDialog.dismiss();
                if (result.isSuccess() && FriendsListFragment.this.isResumed()) {
                    friends.add(result.getData());
                    sortFriendsList();
                    int index = getFriendPosition(username);
                    friendsAdapter.notifyItemInserted(index);
                } else {
                    AndroidUtils.showErrorDialog(result.getErrorMessage(), getContext());
                }
            });
        });
    }

    private void acceptFriendRequest(String userId) {
        int index = getFriendRequestPosition(userId);
        if (index < 0) {
            return;
        }

        // we assume it always succeeds
        FriendRequest friendRequest = friendRequests.remove(index);
        friendRequestsAdapter.notifyItemRemoved(index);

        Friend friend = new Friend(friendRequest.getUserId(), friendRequest.getUsername(), friendRequest.getUserIcon(), true);
        friends.add(friend);
        sortFriendsList();

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Result<String> result = this.userManager.acceptFriendRequest(userId);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                if (result.isFailure() && FriendsListFragment.this.isResumed()) {
                    // on off chance it failed, put the request back
                    friendRequests.add(index, friendRequest);
                    friendRequestsAdapter.notifyItemInserted(index);

                    friends.remove(friend);
                    AndroidUtils.showErrorDialog(result.getErrorMessage(), getContext());
                }
            });
        });
    }

    private void declineFriendRequest(String userId) {
        int index = getFriendRequestPosition(userId);
        if (index < 0) {
            return;
        }

        // we assume it always succeeds
        FriendRequest friendRequest = friendRequests.remove(index);
        friendRequestsAdapter.notifyItemRemoved(index);

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Result<String> result = this.userManager.declineFriendRequest(userId);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                if (result.isFailure() && FriendsListFragment.this.isResumed()) {
                    // on off chance it failed, put the request back
                    friendRequests.add(index, friendRequest);
                    friendRequestsAdapter.notifyItemInserted(index);

                    AndroidUtils.showErrorDialog(result.getErrorMessage(), getContext());
                }
            });
        });
    }

    private void removeFriend(String userId) {
        int index = getFriendPosition(userId);
        if (index < 0) {
            return;
        }

        // we assume it always succeeds
        Friend friend = friends.remove(index);
        friendsAdapter.notifyItemRemoved(index);

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Result<String> result = this.userManager.removeFriend(userId);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                if (result.isFailure() && FriendsListFragment.this.isResumed()) {
                    // on off chance it failed, put the friend back
                    friends.add(index, friend);
                    friendsAdapter.notifyItemInserted(index);

                    AndroidUtils.showErrorDialog(result.getErrorMessage(), getContext());
                }
            });
        });
    }

    private void cancelFriendRequest(String userId) {
        int index = getFriendPosition(userId);
        if (index < 0) {
            return;
        }

        // we assume it always succeeds
        Friend friend = friends.remove(index);
        friendsAdapter.notifyItemRemoved(index);

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Result<String> result = this.userManager.cancelFriendRequest(userId);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                if (result.isFailure()) {
                    // on off chance it failed, put the friend back
                    friends.add(index, friend);
                    friendsAdapter.notifyItemInserted(index);
                    AndroidUtils.showErrorDialog(result.getErrorMessage(), getContext());
                }
            });
        });
    }

    private void promptShareWorkout(String friendUsername) {
        View popupView = getLayoutInflater().inflate(R.layout.popup_send_workout_pick_workout, null);
        Spinner workoutSpinner = popupView.findViewById(R.id.workouts_spinner);
        TextView remainingToSendTv = popupView.findViewById(R.id.remaining_workouts_to_send_tv);
        int remainingAmount = Variables.MAX_FREE_WORKOUTS_SENT - user.getWorkoutsSent();
        if (remainingAmount < 0) {
            remainingAmount = 0; // lol. Just to cover my ass in case
        }
        remainingToSendTv.setText(String.format("You can share a workout %d more times.", remainingAmount));

        List<String> workoutNames = new ArrayList<>();
        Map<String, String> workoutNameToId = new HashMap<>();
        for (WorkoutInfo workoutInfo : user.getWorkouts()) {
            workoutNameToId.put(workoutInfo.getWorkoutName(), workoutInfo.getWorkoutId());
            workoutNames.add(workoutInfo.getWorkoutName());
        }
        ArrayAdapter<String> workoutsAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, workoutNames);
        workoutSpinner.setAdapter(workoutsAdapter);

        TextView workoutTV = popupView.findViewById(R.id.my_workouts_tv);
        if (workoutNames.isEmpty()) {
            // user has no workouts to send
            workoutTV.setText(R.string.no_workouts_to_send);
            remainingToSendTv.setVisibility(View.GONE);
            workoutSpinner.setVisibility(View.GONE);
        } else {
            workoutTV.setVisibility(View.GONE);
        }
        workoutNames.sort(String::compareToIgnoreCase);

        // username is italicized
        SpannableString span1 = new SpannableString("Share a workout with ");
        SpannableString span2 = new SpannableString(friendUsername);
        span2.setSpan(new StyleSpan(Typeface.ITALIC), 0, span2.length(), 0);
        CharSequence title = TextUtils.concat(span1, span2);

        alertDialog = new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setView(popupView)
                .setPositiveButton("Share", null)
                .setNegativeButton("Cancel", null)
                .create();
        alertDialog.setOnShowListener(dialogInterface -> {
            Button sendButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (workoutNames.isEmpty()) {
                sendButton.setVisibility(View.GONE);
            }
            sendButton.setOnClickListener(view -> {
                String selectedName = (String) workoutSpinner.getSelectedItem();
                if (selectedName == null) {
                    // no workout is selected
                    Toast.makeText(getContext(), "Please select a workout to share.", Toast.LENGTH_LONG).show();
                } else {
                    if (user.getPremiumToken() == null && user.getWorkoutsSent() >= Variables.MAX_FREE_WORKOUTS_SENT) {
                        AndroidUtils.showErrorDialog("You have shared the maximum allowed amount of workouts.", getContext());
                    } else {
                        shareWorkout(friendUsername, workoutNameToId.get(selectedName));
                    }
                    alertDialog.dismiss();
                }
            });
        });
        alertDialog.show();
    }

    private void shareWorkout(String recipientUsername, String workoutId) {
        AndroidUtils.showLoadingDialog(loadingDialog, "Sharing...");
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Result<String> result = this.sharedWorkoutManager.shareWorkout(recipientUsername, workoutId);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                loadingDialog.dismiss();
                if (result.isSuccess()) {
                    Toast.makeText(getContext(), "Workout successfully sent.", Toast.LENGTH_LONG).show();
                } else {
                    AndroidUtils.showErrorDialog(result.getErrorMessage(), getContext());
                }
            });
        });
    }

    private void showBlownUpProfilePic(String username, String iconUrl) {
        View popupView = getLayoutInflater().inflate(R.layout.popup_blown_up_profile_picture, null);
        ImageView profilePicture = popupView.findViewById(R.id.profile_picture_image);
        Picasso.get()
                .load(ImageUtils.getProfilePictureUrl(iconUrl))
                .error(R.drawable.picture_load_error)
                .networkPolicy(NetworkPolicy.NO_CACHE) // on first loading in app, always fetch online
                .into(profilePicture);

        alertDialog = new AlertDialog.Builder(getContext())
                .setTitle(username)
                .setView(popupView)
                .setPositiveButton("Done", null)
                .create();
        alertDialog.show();
    }

    // region Adapters
    private class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {
        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView usernameTV;
            final TextView pendingTV;
            final ImageView profilePicture;
            final ConstraintLayout rootLayout;

            ViewHolder(View itemView) {
                super(itemView);
                pendingTV = itemView.findViewById(R.id.pending_request_tv);
                rootLayout = itemView.findViewById(R.id.root_layout);
                usernameTV = itemView.findViewById(R.id.username_tv);
                profilePicture = itemView.findViewById(R.id.profile_picture_image);
            }
        }

        @NonNull
        @Override
        public FriendsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View friendView = inflater.inflate(R.layout.row_friend, parent, false);
            return new FriendsAdapter.ViewHolder(friendView);
        }

        @Override
        public void onBindViewHolder(FriendsAdapter.ViewHolder holder, int position) {
            final Friend friend = friends.get(position);

            ConstraintLayout rootLayout = holder.rootLayout;
            rootLayout.setOnClickListener(v -> {
                bottomSheetDialog = new BottomSheetDialog(getActivity());
                View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_accepted_friend, null);
                TextView sendWorkout = sheetView.findViewById(R.id.share_workout_tv);
                TextView removeFriend = sheetView.findViewById(R.id.remove_friend_tv);
                TextView cancelRequest = sheetView.findViewById(R.id.cancel_friend_request_tv);

                sendWorkout.setVisibility((friend.isConfirmed() ? View.VISIBLE : View.GONE));
                sendWorkout.setOnClickListener(view -> {
                    bottomSheetDialog.dismiss();
                    promptShareWorkout(friend.getUsername());
                });
                removeFriend.setVisibility((friend.isConfirmed() ? View.VISIBLE : View.GONE));
                removeFriend.setOnClickListener(view -> {
                    bottomSheetDialog.dismiss();
                    removeFriend(friend.getUserId());
                });
                cancelRequest.setVisibility((friend.isConfirmed() ? View.GONE : View.VISIBLE));
                cancelRequest.setOnClickListener(view -> {
                    bottomSheetDialog.dismiss();
                    cancelFriendRequest(friend.getUserId());
                });

                RelativeLayout relativeLayout = sheetView.findViewById(R.id.username_pic_container);
                relativeLayout.setOnClickListener(v1 -> showBlownUpProfilePic(friend.getUsername(), friend.getUserIcon()));
                TextView usernameTV = sheetView.findViewById(R.id.username_tv);
                usernameTV.setText(friend.getUsername());

                ImageView profilePicture = sheetView.findViewById(R.id.profile_picture_image);
                Picasso.get()
                        .load(ImageUtils.getProfilePictureUrl(friend.getUserIcon()))
                        .error(R.drawable.picture_load_error)
                        .networkPolicy(NetworkPolicy.NO_CACHE) // on first loading in app, always fetch online
                        .into(profilePicture, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                if (!FriendsListFragment.this.isResumed()) {
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
            TextView usernameTV = holder.usernameTV;
            TextView pendingTV = holder.pendingTV;
            pendingTV.setVisibility(friend.isConfirmed() ? View.GONE : View.VISIBLE);
            ImageView profilePicture = holder.profilePicture;
            usernameTV.setText(friend.getUsername());
            Picasso.get()
                    .load(ImageUtils.getProfilePictureUrl(friend.getUserIcon()))
                    .error(R.drawable.picture_load_error)
                    .networkPolicy(NetworkPolicy.NO_CACHE) // on first loading in app, always fetch online
                    .into(profilePicture, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            if (!FriendsListFragment.this.isResumed()) {
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
        }

        @Override
        public int getItemCount() {
            return friends.size();
        }
    }

    private class FriendRequestsAdapter extends RecyclerView.Adapter<FriendRequestsAdapter.ViewHolder> {
        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView usernameTV;
            final TextView unseenTV;
            final ImageView profilePicture;
            final ConstraintLayout rootLayout;

            ViewHolder(View itemView) {
                super(itemView);
                usernameTV = itemView.findViewById(R.id.username_tv);
                profilePicture = itemView.findViewById(R.id.profile_picture_image);
                unseenTV = itemView.findViewById(R.id.unseen_tv);
                rootLayout = itemView.findViewById(R.id.root_layout);
            }
        }

        public static final String PAYLOAD_UPDATE_SEEN_STATUS = "UPDATE_SEEN_STATUS";

        @NonNull
        @Override
        public FriendRequestsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View friendView = inflater.inflate(R.layout.row_friend_request, parent, false);
            return new FriendRequestsAdapter.ViewHolder(friendView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
            if (!payloads.isEmpty()) {
                for (Object payload : payloads) {
                    if (payload.equals(PAYLOAD_UPDATE_SEEN_STATUS)) {
                        holder.unseenTV.setVisibility(View.GONE);
                    }
                }
            } else {
                super.onBindViewHolder(holder, position, payloads);
            }
        }

        @Override
        public void onBindViewHolder(FriendRequestsAdapter.ViewHolder holder, int position) {
            final FriendRequest friendRequest = friendRequests.get(position);
            TextView usernameTV = holder.usernameTV;
            ImageView profilePicture = holder.profilePicture;
            TextView unseenTV = holder.unseenTV;

            unseenTV.setVisibility(friendRequest.isSeen() ? View.GONE : View.VISIBLE);
            profilePicture.setOnClickListener(v -> showBlownUpProfilePic(friendRequest.getUsername(), friendRequest.getUserIcon()));
            usernameTV.setText(friendRequest.getUsername());

            Picasso.get()
                    .load(ImageUtils.getProfilePictureUrl(friendRequest.getUserIcon()))
                    .error(R.drawable.picture_load_error)
                    .networkPolicy(NetworkPolicy.NO_CACHE) // on first loading in app, always fetch online
                    .into(profilePicture, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            if (!FriendsListFragment.this.isResumed()) {
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

            holder.rootLayout.setOnClickListener(v -> {
                friendRequest.setSeen(true);
                unseenTV.setVisibility(View.GONE);

                bottomSheetDialog = new BottomSheetDialog(getActivity());
                View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_friend_request, null);
                TextView acceptFriendRequestTV = sheetView.findViewById(R.id.accept_friend_request_tv);
                TextView declineFriendRequestTV = sheetView.findViewById(R.id.decline_friend_request_tv);
                TextView dateReceivedTV = sheetView.findViewById(R.id.date_received_tv);
                TextView dialogUsernameTV = sheetView.findViewById(R.id.username_tv);

                dialogUsernameTV.setText(friendRequest.getUsername());
                dateReceivedTV.setText(TimeUtils.getFormattedLocalDateTime(friendRequest.getSentUtc()));
                acceptFriendRequestTV.setOnClickListener(view -> {
                    bottomSheetDialog.dismiss();
                    acceptFriendRequest(friendRequest.getUsername());
                });
                declineFriendRequestTV.setOnClickListener(view -> {
                    bottomSheetDialog.dismiss();
                    declineFriendRequest(friendRequest.getUsername());
                });

                RelativeLayout relativeLayout = sheetView.findViewById(R.id.username_pic_container);
                relativeLayout.setOnClickListener(v1 -> showBlownUpProfilePic(friendRequest.getUsername(), friendRequest.getUserIcon()));
                ImageView dialogProfilePicture = sheetView.findViewById(R.id.profile_picture_image);

                Picasso.get()
                        .load(ImageUtils.getProfilePictureUrl(friendRequest.getUserIcon()))
                        .error(R.drawable.picture_load_error)
                        .networkPolicy(NetworkPolicy.NO_CACHE) // on first loading in app, always fetch online
                        .into(dialogProfilePicture, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                if (!FriendsListFragment.this.isResumed()) {
                                    return;
                                }
                                Bitmap imageBitmap = ((BitmapDrawable) dialogProfilePicture.getDrawable()).getBitmap();
                                RoundedBitmapDrawable imageDrawable = RoundedBitmapDrawableFactory.create(getResources(), imageBitmap);
                                imageDrawable.setCircular(true);
                                imageDrawable.setCornerRadius(Math.max(imageBitmap.getWidth(), imageBitmap.getHeight()) / 2.0f);
                                dialogProfilePicture.setImageDrawable(imageDrawable);
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
            return friendRequests.size();
        }
    }
    //endregion
}
