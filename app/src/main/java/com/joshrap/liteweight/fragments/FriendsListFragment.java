package com.joshrap.liteweight.fragments;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.joshrap.liteweight.activities.WorkoutActivity;
import com.joshrap.liteweight.helpers.AndroidHelper;
import com.joshrap.liteweight.helpers.ImageHelper;
import com.joshrap.liteweight.helpers.InputHelper;
import com.joshrap.liteweight.imports.Globals;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.interfaces.FragmentWithDialog;
import com.joshrap.liteweight.models.Friend;
import com.joshrap.liteweight.models.FriendRequest;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.models.User;
import com.joshrap.liteweight.network.repos.UserRepository;
import com.joshrap.liteweight.widgets.ErrorDialog;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import static android.os.Looper.getMainLooper;

public class FriendsListFragment extends Fragment implements FragmentWithDialog {
    private User user;
    private FloatingActionButton floatingActionButton;
    private TextView emptyView;
    private static final int FRIENDS_POSITION = 0;
    public static final int REQUESTS_POSITION = 1;
    private RecyclerView recyclerView;
    private AlertDialog alertDialog;
    private ProgressDialog loadingDialog;
    private BottomSheetDialog bottomSheetDialog;
    private List<Friend> friends;
    private List<FriendRequest> friendRequests;
    private FriendsAdapter friendsAdapter;
    private FriendRequestsAdapter friendRequestsAdapter;
    private TabLayout tabLayout;
    private int currentIndex;
    @Inject
    UserRepository userRepository;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends_list, container, false);
        ((WorkoutActivity) getActivity()).updateToolbarTitle(Variables.FRIENDS_LIST_TITLE);
        ((WorkoutActivity) getActivity()).toggleBackButton(true);
        Injector.getInjector(getContext()).inject(this);
        user = Globals.user;

        Bundle args = getArguments();
        if (args != null) {
            // don't need to consume the extras, as long as args are there we know we are starting on friend request page
            currentIndex = REQUESTS_POSITION;
        } else {
            currentIndex = FRIENDS_POSITION;
        }
        loadingDialog = new ProgressDialog(getContext());
        loadingDialog.setCancelable(false);
        return view;
    }

    @Override
    public void onPause() {
        // sanity check to determine if user has any unseen requests after this fragment is paused
        if (tabLayout.getSelectedTabPosition() == REQUESTS_POSITION) {
            markAllRequestsSeen();
        }
        super.onPause();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        /*
            Init all views and buttons when view is loaded onto screen
         */
        friends = new ArrayList<>();
        friendRequests = new ArrayList<>();

        for (String username : user.getFriends().keySet()) {
            friends.add(user.getFriends().get(username));
        }
        for (String username : user.getFriendRequests().keySet()) {
            friendRequests.add(user.getFriendRequests().get(username));
        }
        friendRequestsAdapter = new FriendRequestsAdapter(friendRequests);
        friendsAdapter = new FriendsAdapter(friends);

        emptyView = view.findViewById(R.id.empty_view);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        recyclerView = view.findViewById(R.id.friends_recycler_view);
        recyclerView.setLayoutManager(llm);

        floatingActionButton = view.findViewById(R.id.floating_action_btn);
        floatingActionButton.setOnClickListener(v -> addFriendPopup());
        tabLayout = view.findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Friends"), FRIENDS_POSITION);
        boolean requestsUnseen = false;
        for (FriendRequest friendRequest : friendRequests) {
            if (!friendRequest.isSeen()) {
                requestsUnseen = true;
                break;
            }
        }
        tabLayout.addTab(tabLayout.newTab().setText(
                requestsUnseen ? "Friend Requests (!)" : "Friend Requests"), REQUESTS_POSITION);

        tabLayout.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
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
                    markAllRequestsSeen();
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

    private void switchToFriendsList() {
        floatingActionButton.show();
        checkEmptyList(FRIENDS_POSITION);
        friendsAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            // since google is stupid af and doesn't have a simple setEmptyView for recyclerView...
            @Override
            public void onChanged() {
                super.onChanged();
                checkEmptyList(FRIENDS_POSITION);
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                checkEmptyList(FRIENDS_POSITION);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                checkEmptyList(FRIENDS_POSITION);
            }
        });
        recyclerView.setAdapter(friendsAdapter);
    }

    private void switchToRequestsList() {
        tabLayout.getTabAt(REQUESTS_POSITION).setText("Friend Requests"); // when user clicks on this tab, all requests are set to "seen"
        floatingActionButton.hide();
        checkEmptyList(REQUESTS_POSITION);
        friendsAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            // since google is stupid af and doesn't have a simple setEmptyView for recyclerView...
            @Override
            public void onChanged() {
                super.onChanged();
                checkEmptyList(REQUESTS_POSITION);
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                checkEmptyList(REQUESTS_POSITION);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                checkEmptyList(REQUESTS_POSITION);
            }
        });
        recyclerView.setAdapter(friendRequestsAdapter);
        deleteNotifications();
    }

    private void checkEmptyList(int position) {
         /*
            Used to check if the user has any friends. If not, show a textview alerting user
         */
        if (position == FRIENDS_POSITION) {
            emptyView.setVisibility(friends.isEmpty() ? View.VISIBLE : View.GONE);
            emptyView.setText(getString(R.string.empty_friend_list_msg));
        } else if (position == REQUESTS_POSITION) {
            emptyView.setVisibility(friendRequests.isEmpty() ? View.VISIBLE : View.GONE);
            emptyView.setText(getString(R.string.empty_friends_request_msg));
        }
    }

    private void deleteNotifications() {
        for (FriendRequest friendRequest : friendRequests) {
            if (!friendRequest.isSeen()) {
                // get rid of any push notification that might be there
                NotificationManager mNotificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                if (mNotificationManager != null) {
                    mNotificationManager.cancel(friendRequest.getUsername().hashCode());
                }
            }
        }
    }

    private void markAllRequestsSeen() {
        int unseenCount = 0;
        for (FriendRequest friendRequest : friendRequests) {
            if (!friendRequest.isSeen()) {
                unseenCount++;
                friendRequest.setSeen(true);
            }
        }
        if (unseenCount > 0) {
            // prevents useless api calls to update unseen friend requests
            if (getActivity() != null) {
                deleteNotifications();
                ((WorkoutActivity) getActivity()).updateNotificationIndicator();
            }
            // marking all requests seen is not critical at all, so if it fails no need to alarm user
            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> userRepository.setAllRequestsSeen());
        }
    }

    private void addFriendPopup() {
        final View popupView = getLayoutInflater().inflate(R.layout.popup_add_friend, null);
        final TextInputLayout friendNameLayout = popupView.findViewById(R.id.friend_name_input_layout);
        final EditText friendInput = popupView.findViewById(R.id.friend_name_input);
        friendInput.addTextChangedListener(AndroidHelper.hideErrorTextWatcher(friendNameLayout));
        friendInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_USERNAME_LENGTH)});
        alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                .setTitle("Add Friend")
                .setView(popupView)
                .setPositiveButton("Send Request", null)
                .setNegativeButton("Close", null)
                .create();
        alertDialog.setOnShowListener(dialogInterface -> {
            Button saveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            saveButton.setOnClickListener(view -> {
                String friendUsername = friendInput.getText().toString().trim();
                List<String> existingUsernames = new ArrayList<>();
                for (Friend friend : friends) {
                    existingUsernames.add(friend.getUsername());
                }
                for (FriendRequest friendRequest : friendRequests) {
                    existingUsernames.add(friendRequest.getUsername());
                }
                String errorMsg = InputHelper.validNewFriend(user.getUsername(), friendUsername, existingUsernames);
                if (errorMsg != null) {
                    friendNameLayout.setError(errorMsg);
                } else {
                    // no problems so go ahead and save
                    alertDialog.dismiss();
                    sendFriendRequest(friendUsername);
                }
            });
        });
        alertDialog.show();
    }

    private void showLoadingDialog(String message) {
        loadingDialog.setMessage(message);
        loadingDialog.show();
    }

    private void sendFriendRequest(String username) {
        showLoadingDialog("Sending request...");
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ResultStatus<Friend> resultStatus = this.userRepository.sendFriendRequest(username);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                loadingDialog.dismiss();
                if (resultStatus.isSuccess()) {
                    user.getFriends().put(resultStatus.getData().getUsername(), resultStatus.getData());
                    friends.add(user.getFriends().get(username));
                    friendsAdapter.notifyDataSetChanged();
                } else {
                    ErrorDialog.showErrorDialog("Error", resultStatus.getErrorMessage(), getContext());
                }
            });
        });
    }

    private void cancelFriendRequest(String username) {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ResultStatus<String> resultStatus = this.userRepository.cancelFriendRequest(username);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                // not critical to show any type of loading dialog/handle errors for this action
                if (!resultStatus.isSuccess()) {
                    ErrorDialog.showErrorDialog("Error", resultStatus.getErrorMessage(), getContext());
                }
            });
        });
    }

    public void removeFriendRequestFromList(String username) {
        FriendRequest friendRequestToRemove = null;
        for (FriendRequest friendRequest : friendRequests) {
            if (friendRequest.getUsername().equals(username)) {
                friendRequestToRemove = friendRequest;
                break;
            }
        }
        if (friendRequestToRemove != null) {
            friendRequests.remove(friendRequestToRemove);
            friendRequestsAdapter.notifyDataSetChanged();
            checkEmptyList(tabLayout.getSelectedTabPosition());
        }
    }

    public void addFriendRequestToList(FriendRequest friendRequest) {
        if (friendRequest != null) {
            friendRequests.add(0, friendRequest);
            Toast.makeText(getContext(), friendRequest.getUsername() + " sent you a friend request.", Toast.LENGTH_LONG).show();
            friendRequestsAdapter.notifyDataSetChanged();
        }
    }

    private void showBlownUpProfilePic(Friend friend) {
        View popupView = getLayoutInflater().inflate(R.layout.popup_blown_up_profile_picture, null);
        final ImageView profilePicture = popupView.findViewById(R.id.profile_picture);
        Picasso.get()
                .load(ImageHelper.getIconUrl(friend.getIcon()))
                .error(R.drawable.new_icon_round)
                .networkPolicy(NetworkPolicy.NO_CACHE) // on first loading in app, always fetch online
                .into(profilePicture);

        alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                .setTitle(friend.getUsername())
                .setView(popupView)
                .setPositiveButton("Done", null)
                .create();
        alertDialog.show();
    }

    private void showBlownUpProfilePic(FriendRequest friend) {
        // todo use same method for both friend and friend request
        View popupView = getLayoutInflater().inflate(R.layout.popup_blown_up_profile_picture, null);
        final ImageView profilePicture = popupView.findViewById(R.id.profile_picture);
        Picasso.get()
                .load(ImageHelper.getIconUrl(friend.getIcon()))
                .error(R.drawable.new_icon_round)
                .networkPolicy(NetworkPolicy.NO_CACHE) // on first loading in app, always fetch online
                .into(profilePicture);

        alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                .setTitle(friend.getUsername())
                .setView(popupView)
                .setPositiveButton("Done", null)
                .create();
        alertDialog.show();
    }

    // region Adapters

    private class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView usernameTV;
            TextView pendingTV;
            ImageView profilePicture;
            ConstraintLayout rootLayout;

            ViewHolder(View itemView) {
                super(itemView);
                pendingTV = itemView.findViewById(R.id.pending_request_tv);
                rootLayout = itemView.findViewById(R.id.root_layout);
                usernameTV = itemView.findViewById(R.id.username_tv);
                profilePicture = itemView.findViewById(R.id.profile_picture);
            }
        }

        private List<Friend> friends;

        FriendsAdapter(List<Friend> friends) {
            this.friends = friends;
        }

        @Override
        public FriendsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);

            // Inflate the custom layout
            View friendView = inflater.inflate(R.layout.row_friend, parent, false);

            // Return a new holder instance
            return new FriendsAdapter.ViewHolder(friendView);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        // Involves populating data into the item through holder
        @Override
        public void onBindViewHolder(FriendsAdapter.ViewHolder holder, int position) {
            // Get the data model based on position
            final Friend friend = friends.get(position);

            final ConstraintLayout rootLayout = holder.rootLayout;
            rootLayout.setOnClickListener(v -> {
                bottomSheetDialog = new BottomSheetDialog(getActivity());
                View sheetView = getLayoutInflater().inflate(R.layout.friend_list_bottom_sheet, null);
                final TextView sendWorkout = sheetView.findViewById(R.id.send_friend_workout_tv);
                final TextView removeFriend = sheetView.findViewById(R.id.remove_friend_tv);
                final TextView blockFriend = sheetView.findViewById(R.id.block_friend_tv);
                final TextView cancelRequest = sheetView.findViewById(R.id.cancel_friend_request_tv);
                sendWorkout.setVisibility((friend.isConfirmed() ? View.VISIBLE : View.GONE));
                removeFriend.setVisibility((friend.isConfirmed() ? View.VISIBLE : View.GONE));
                blockFriend.setVisibility((friend.isConfirmed() ? View.VISIBLE : View.GONE));
                cancelRequest.setVisibility((friend.isConfirmed() ? View.GONE : View.VISIBLE));
                cancelRequest.setOnClickListener(view -> {
                    cancelFriendRequest(friend.getUsername());
                    bottomSheetDialog.dismiss();
                    user.getFriends().remove(friend.getUsername());
                    friends.remove(friend);
                    notifyDataSetChanged();
                });

                final RelativeLayout relativeLayout = sheetView.findViewById(R.id.username_pic_container);
                relativeLayout.setOnClickListener(v1 -> showBlownUpProfilePic(friend));
                final TextView exerciseTV = sheetView.findViewById(R.id.username_tv);
                final ImageView profilePicture = sheetView.findViewById(R.id.profile_picture);
                exerciseTV.setText(friend.getUsername());

                Picasso.get()
                        .load(ImageHelper.getIconUrl(friend.getIcon()))
                        .error(R.drawable.new_icon_round)
                        .networkPolicy(NetworkPolicy.NO_CACHE) // on first loading in app, always fetch online
                        .into(profilePicture, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
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
            final TextView exerciseTV = holder.usernameTV;
            final TextView pendingTV = holder.pendingTV;
            pendingTV.setVisibility(friend.isConfirmed() ? View.GONE : View.VISIBLE);
            final ImageView profilePicture = holder.profilePicture;
            exerciseTV.setText(friend.getUsername());
            Picasso.get()
                    .load(ImageHelper.getIconUrl(friend.getIcon()))
                    .error(R.drawable.new_icon_round)
                    .networkPolicy(NetworkPolicy.NO_CACHE) // on first loading in app, always fetch online
                    .into(profilePicture, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            if (FriendsListFragment.this.isResumed()) {
                                Bitmap imageBitmap = ((BitmapDrawable) profilePicture.getDrawable()).getBitmap();
                                RoundedBitmapDrawable imageDrawable = RoundedBitmapDrawableFactory.create(getResources(), imageBitmap);
                                imageDrawable.setCircular(true);
                                imageDrawable.setCornerRadius(Math.max(imageBitmap.getWidth(), imageBitmap.getHeight()) / 2.0f);
                                profilePicture.setImageDrawable(imageDrawable);
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                        }
                    });
        }

        // Returns the total count of items in the list
        @Override
        public int getItemCount() {
            return friends.size();
        }
    }

    private class FriendRequestsAdapter extends RecyclerView.Adapter<FriendRequestsAdapter.ViewHolder> {
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView usernameTV;
            TextView unseenTV;
            ImageView profilePicture;
            Button acceptRequestButton;
            Button declineRequestButton;

            ViewHolder(View itemView) {
                super(itemView);
                usernameTV = itemView.findViewById(R.id.username_tv);
                profilePicture = itemView.findViewById(R.id.profile_picture);
                acceptRequestButton = itemView.findViewById(R.id.accept_request_btn);
                declineRequestButton = itemView.findViewById(R.id.decline_request_btn);
                unseenTV = itemView.findViewById(R.id.unseen_tv);
            }
        }

        private List<FriendRequest> friendRequests;

        FriendRequestsAdapter(List<FriendRequest> friends) {
            this.friendRequests = friends;
        }

        @Override
        public FriendRequestsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View friendView = inflater.inflate(R.layout.row_friend_request, parent, false);
            return new FriendRequestsAdapter.ViewHolder(friendView);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        // Involves populating data into the item through holder
        @Override
        public void onBindViewHolder(FriendRequestsAdapter.ViewHolder holder, int position) {
            // Get the data model based on position
            final FriendRequest friend = friendRequests.get(position);
            final TextView exerciseTV = holder.usernameTV;
            final ImageView profilePicture = holder.profilePicture;
            final TextView unseenTV = holder.unseenTV;
            unseenTV.setVisibility(friend.isSeen() ? View.GONE : View.VISIBLE);
            profilePicture.setOnClickListener(v -> showBlownUpProfilePic(friend));
            exerciseTV.setText(friend.getUsername());
            Picasso.get()
                    .load(ImageHelper.getIconUrl(friend.getIcon()))
                    .error(R.drawable.new_icon_round)
                    .networkPolicy(NetworkPolicy.NO_CACHE) // on first loading in app, always fetch online
                    .into(profilePicture, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            if (FriendsListFragment.this.isResumed()) {
                                Bitmap imageBitmap = ((BitmapDrawable) profilePicture.getDrawable()).getBitmap();
                                RoundedBitmapDrawable imageDrawable = RoundedBitmapDrawableFactory.create(getResources(), imageBitmap);
                                imageDrawable.setCircular(true);
                                imageDrawable.setCornerRadius(Math.max(imageBitmap.getWidth(), imageBitmap.getHeight()) / 2.0f);
                                profilePicture.setImageDrawable(imageDrawable);
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                        }
                    });
        }

        // Returns the total count of items in the list
        @Override
        public int getItemCount() {
            return friendRequests.size();
        }
    }
    //endregion
}
