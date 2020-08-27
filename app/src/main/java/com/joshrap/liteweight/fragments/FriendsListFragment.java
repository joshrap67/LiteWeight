package com.joshrap.liteweight.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.joshrap.liteweight.helpers.InputHelper;
import com.joshrap.liteweight.imports.Globals;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.interfaces.FragmentWithDialog;
import com.joshrap.liteweight.models.Friend;
import com.joshrap.liteweight.models.User;
import com.joshrap.liteweight.network.repos.UserRepository;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class FriendsListFragment extends Fragment implements FragmentWithDialog {
    private User user;
    private FloatingActionButton floatingActionButton;
    private TextView emptyView;
    private static final int FRIENDS_POSITION = 0;
    private static final int REQUESTS_POSITION = 1;
    private RecyclerView recyclerView;
    private AlertDialog alertDialog;
    private BottomSheetDialog bottomSheetDialog;
    private List<Friend> friends;
    @Inject
    UserRepository userRepository;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends_list, container, false); // TODO separate layout?
        ((WorkoutActivity) getActivity()).updateToolbarTitle(Variables.FRIENDS_LIST_TITLE);
        ((WorkoutActivity) getActivity()).enableBackButton(true);
        Injector.getInjector(getContext()).inject(this);
        user = Globals.user;
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        /*
            Init all views and buttons when view is loaded onto screen
         */
        // TODO rename deleteWorkout as popWorkout???
        friends = new ArrayList<>();
        for (int i = 0; i < 0; i++) {
            friends.add(new Friend("User " + (i + 1), "https://yt3.ggpht.com/-7QXQsjZ-1SE/AAAAAAAAAAI/AAAAAAAAAAA/zIGE4BY7zLs/s900-c-k-no-mo-rj-c0xffffff/photo.jpg",
                    (i % 2 == 0)));
        }
        emptyView = view.findViewById(R.id.empty_view);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        recyclerView = view.findViewById(R.id.friends_recycler_view);
        recyclerView.setLayoutManager(llm);

        floatingActionButton = view.findViewById(R.id.floating_action_btn);
        floatingActionButton.setOnClickListener(v -> addFriendPopup());
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Friends"), FRIENDS_POSITION);
        tabLayout.addTab(tabLayout.newTab().setText("Friend Requests"), REQUESTS_POSITION);
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

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        // always default to friends list?
        switchToFriendsList();
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
    }

    private void switchToFriendsList() {
        floatingActionButton.show();
        FriendsAdapter adapter = new FriendsAdapter(friends);
        checkEmptyList(FRIENDS_POSITION);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
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
        recyclerView.setAdapter(adapter);
    }

    private void switchToRequestsList() {
        floatingActionButton.hide();
        FriendRequestsAdapter adapter = new FriendRequestsAdapter(friends);
        checkEmptyList(REQUESTS_POSITION);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
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
        recyclerView.setAdapter(adapter);
    }

    private void checkEmptyList(int position) {
         /*
            Used to check if the user has any friends. If not, show a textview alerting user
         */
        // todo need to separate between requests
        emptyView.setVisibility(friends.isEmpty() ? View.VISIBLE : View.GONE);
        if (position == FRIENDS_POSITION) {
            emptyView.setText(getString(R.string.empty_friend_list_msg));
        } else if (position == REQUESTS_POSITION) {
            emptyView.setText(getString(R.string.empty_friends_request_msg));
        }
    }

    private void addFriendPopup() {
        View popupView = getLayoutInflater().inflate(R.layout.popup_add_friend, null);
        final TextInputLayout friendNameLayout = popupView.findViewById(R.id.friend_name_input_layout);
        final EditText friendInput = popupView.findViewById(R.id.friend_name_input);
        friendInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (friendNameLayout.isErrorEnabled()) {
                    // if an error is present, stop showing the error message once the user types (acknowledged it)
                    friendNameLayout.setErrorEnabled(false);
                    friendNameLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
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
                List<String> friendsList = new ArrayList<>();
                for (Friend friend : friends) {
                    friendsList.add(friend.getUsername());
                }
                String errorMsg = InputHelper.validNewFriend(user.getUsername(), friendUsername, friendsList);
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

    private void sendFriendRequest(String username) {

    }

    private void showBlownUpProfilePic(Friend friend) {
        View popupView = getLayoutInflater().inflate(R.layout.popup_blown_up_profile_picture, null);
        final ImageView profilePicture = popupView.findViewById(R.id.profile_picture);
        Picasso.get()
                .load(friend.getIcon())
                .error(R.drawable.ic_launcher_round)
                .networkPolicy(NetworkPolicy.NO_CACHE) // on first loading in app, always fetch online
                .into(profilePicture);

        alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                .setTitle(friend.getUsername())
                .setView(popupView)
                .setPositiveButton("Done", null)
                .create();
        alertDialog.show();
    }

    /*
        These are all in here since Java doesn't allow for functions to easily be passed.
     */

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

                final RelativeLayout relativeLayout = sheetView.findViewById(R.id.username_pic_container);
                relativeLayout.setOnClickListener(v1 -> showBlownUpProfilePic(friend));
                final TextView exerciseTV = sheetView.findViewById(R.id.username_tv);
                final ImageView profilePicture = sheetView.findViewById(R.id.profile_picture);
                exerciseTV.setText(friend.getUsername());

                Picasso.get()
                        .load(friend.getIcon())
                        .error(R.drawable.ic_launcher_round)
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
                    .load(friend.getIcon())
                    .error(R.drawable.ic_launcher_round)
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
            ImageView profilePicture;
            Button acceptRequestButton;
            Button declineRequestButton;

            ViewHolder(View itemView) {
                super(itemView);
                usernameTV = itemView.findViewById(R.id.username_tv);
                profilePicture = itemView.findViewById(R.id.profile_picture);
                acceptRequestButton = itemView.findViewById(R.id.accept_request_btn);
                declineRequestButton = itemView.findViewById(R.id.decline_request_btn);
            }
        }

        private List<Friend> friendRequests;

        FriendRequestsAdapter(List<Friend> friends) {
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
            final Friend friend = friendRequests.get(position);
            final TextView exerciseTV = holder.usernameTV;
            final ImageView profilePicture = holder.profilePicture;
            profilePicture.setOnClickListener(v -> showBlownUpProfilePic(friend));
            exerciseTV.setText(friend.getUsername());
            Picasso.get()
                    .load(friend.getIcon())
                    .error(R.drawable.ic_launcher_round)
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
        }

        // Returns the total count of items in the list
        @Override
        public int getItemCount() {
            return friendRequests.size();
        }
    }
}
