package com.joshrap.liteweight.fragments;

import android.app.AlertDialog;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.models.User;
import com.joshrap.liteweight.network.repos.UserRepository;
import com.joshrap.liteweight.widgets.ErrorDialog;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import static android.os.Looper.getMainLooper;

public class BlockedListFragment extends Fragment implements FragmentWithDialog {
    private User user;
    private TextView emptyView;
    private AlertDialog alertDialog;
    private ProgressDialog loadingDialog;
    private BottomSheetDialog bottomSheetDialog;
    private List<String> blocked;
    private BlockedAdapter blockedAdapter;
    @Inject
    UserRepository userRepository;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_blocked_list, container, false);
        ((WorkoutActivity) getActivity()).updateToolbarTitle(Variables.BLOCKED_LIST_TITLE);
        ((WorkoutActivity) getActivity()).toggleBackButton(true);
        Injector.getInjector(getContext()).inject(this);
        user = Globals.user;

        loadingDialog = new ProgressDialog(getContext());
        loadingDialog.setCancelable(false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        /*
            Init all views and buttons when view is loaded onto screen
         */
        blocked = new ArrayList<>();

        blocked.addAll(user.getBlocked().keySet());
        blockedAdapter = new BlockedAdapter(blocked, user.getBlocked());

        emptyView = view.findViewById(R.id.empty_view);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        RecyclerView recyclerView = view.findViewById(R.id.blocked_recycler_view);
        recyclerView.setLayoutManager(llm);
        blockedAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            // since google is stupid af and doesn't have a simple setEmptyView for recyclerView...
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
        recyclerView.setAdapter(blockedAdapter);

        FloatingActionButton floatingActionButton = view.findViewById(R.id.floating_action_btn);
        floatingActionButton.setOnClickListener(v -> addFriendPopup());
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

    private void checkEmptyList() {
         /*
            Used to check if the user has any blocked. If not, show a textview alerting user
         */
        emptyView.setVisibility(blocked.isEmpty() ? View.VISIBLE : View.GONE);
        emptyView.setText(getString(R.string.empty_blocked_list_msg));
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
                List<String> existingUsernames = new ArrayList<>(blocked);
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
                    blocked.add(username);
                    blockedAdapter.notifyDataSetChanged();
                } else {
                    ErrorDialog.showErrorDialog("Error", resultStatus.getErrorMessage(), getContext());
                }
            });
        });
    }

    private void unblockUser(String username) {
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

    private void showBlownUpProfilePic(String username, String icon) {
        View popupView = getLayoutInflater().inflate(R.layout.popup_blown_up_profile_picture, null);
        final ImageView profilePicture = popupView.findViewById(R.id.profile_picture);
        Picasso.get()
                .load(ImageHelper.getIconUrl(icon))
                .error(R.drawable.new_icon_round)
                .networkPolicy(NetworkPolicy.NO_CACHE) // on first loading in app, always fetch online
                .into(profilePicture);

        alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                .setTitle(username)
                .setView(popupView)
                .setPositiveButton("Done", null)
                .create();
        alertDialog.show();
    }

    // region Adapters

    private class BlockedAdapter extends RecyclerView.Adapter<BlockedAdapter.ViewHolder> {
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView usernameTV;
            ImageView profilePicture;
            RelativeLayout rootLayout;

            ViewHolder(View itemView) {
                super(itemView);
                rootLayout = itemView.findViewById(R.id.username_pic_container);
                usernameTV = itemView.findViewById(R.id.username_tv);
                profilePicture = itemView.findViewById(R.id.profile_picture);
            }
        }

        private List<String> blockedList;
        private Map<String, String> blockedMap;

        BlockedAdapter(List<String> blockedList, Map<String, String> blockedMap) {
            this.blockedMap = blockedMap;
            this.blockedList = blockedList;
        }

        @Override
        public BlockedAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);

            // Inflate the custom layout
            View friendView = inflater.inflate(R.layout.row_friend, parent, false);

            // Return a new holder instance
            return new BlockedAdapter.ViewHolder(friendView);
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
        public void onBindViewHolder(BlockedAdapter.ViewHolder holder, int position) {
            // Get the data model based on position
            final String blockedUser = blockedList.get(position);
            final String icon = blockedMap.get(blockedUser);

            final RelativeLayout rootLayout = holder.rootLayout;
            rootLayout.setOnClickListener(v -> {
                bottomSheetDialog = new BottomSheetDialog(getActivity());
                View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_blocked_list, null);
                final TextView unblockTV = sheetView.findViewById(R.id.unblock_tv);
                unblockTV.setOnClickListener(view -> {
                    // todo unblock
//                    cancelFriendRequest(friend.getUsername());
//                    bottomSheetDialog.dismiss();
//                    user.getFriends().remove(friend.getUsername());
//                    blockedList.remove(friend);
                    notifyDataSetChanged();
                });

                final RelativeLayout relativeLayout = sheetView.findViewById(R.id.username_pic_container);
                relativeLayout.setOnClickListener(v1 -> showBlownUpProfilePic(blockedUser, icon));
                final TextView usernameTV = sheetView.findViewById(R.id.username_tv);
                final ImageView profilePicture = sheetView.findViewById(R.id.profile_picture);
                usernameTV.setText(blockedUser);

                Picasso.get()
                        .load(ImageHelper.getIconUrl(icon))
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
            final TextView usernameTV = holder.usernameTV;
            final ImageView profilePicture = holder.profilePicture;
            usernameTV.setText(blockedUser);
            Picasso.get()
                    .load(ImageHelper.getIconUrl(icon))
                    .error(R.drawable.new_icon_round)
                    .networkPolicy(NetworkPolicy.NO_CACHE) // on first loading in app, always fetch online
                    .into(profilePicture, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            if (BlockedListFragment.this.isResumed()) {
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
            return blockedList.size();
        }
    }

    //endregion
}
