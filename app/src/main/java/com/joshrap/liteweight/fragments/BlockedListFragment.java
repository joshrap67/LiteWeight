package com.joshrap.liteweight.fragments;

import androidx.appcompat.app.AlertDialog;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
import com.joshrap.liteweight.activities.MainActivity;
import com.joshrap.liteweight.managers.UserManager;
import com.joshrap.liteweight.providers.CurrentUserAndWorkoutProvider;
import com.joshrap.liteweight.utils.AndroidUtils;
import com.joshrap.liteweight.utils.ImageUtils;
import com.joshrap.liteweight.utils.ValidatorUtils;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.interfaces.FragmentWithDialog;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.models.User;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
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
    private BottomSheetDialog bottomSheetDialog;
    private List<String> blocked;
    private BlockedAdapter blockedAdapter;

    @Inject
    AlertDialog loadingDialog;
    @Inject
    UserManager userManager;
    @Inject
    CurrentUserAndWorkoutProvider currentUserAndWorkoutProvider;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        ((MainActivity) getActivity()).updateToolbarTitle(Variables.BLOCKED_LIST_TITLE);
        ((MainActivity) getActivity()).toggleBackButton(true);
        Injector.getInjector(getContext()).inject(this);
        user = currentUserAndWorkoutProvider.provideCurrentUserAndWorkout().getUser();

        return inflater.inflate(R.layout.fragment_blocked_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        blocked = new ArrayList<>();
        blocked.addAll(user.getBlocked().keySet());
        Collections.sort(blocked);
        blockedAdapter = new BlockedAdapter(user.getBlocked());

        emptyView = view.findViewById(R.id.empty_view_tv);
        RecyclerView recyclerView = view.findViewById(R.id.blocked_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        blockedAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
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
        checkEmptyList();

        FloatingActionButton floatingActionButton = view.findViewById(R.id.block_user_fab);
        floatingActionButton.setOnClickListener(v -> blockUserPopup());
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
        emptyView.setVisibility(blocked.isEmpty() ? View.VISIBLE : View.GONE);
        emptyView.setText(getString(R.string.empty_blocked_list_msg));
    }

    private void blockUserPopup() {
        View popupView = getLayoutInflater().inflate(R.layout.popup_add_friend, null);
        TextInputLayout friendNameLayout = popupView.findViewById(R.id.username_input_layout);
        EditText usernameInput = popupView.findViewById(R.id.username_input);
        usernameInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(friendNameLayout));
        usernameInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_USERNAME_LENGTH)});
        alertDialog = new AlertDialog.Builder(getContext())
                .setTitle("Block User")
                .setView(popupView)
                .setPositiveButton("Block", null)
                .setNegativeButton("Close", null)
                .create();
        alertDialog.setOnShowListener(dialogInterface -> {
            Button saveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            saveButton.setOnClickListener(view -> {
                // usernames are case insensitive!
                String username = usernameInput.getText().toString().trim().toLowerCase();
                List<String> blockedUsers = new ArrayList<>(blocked);
                String errorMsg = ValidatorUtils.validUserToBlock(user.getUsername(), username, blockedUsers);
                if (errorMsg != null) {
                    friendNameLayout.setError(errorMsg);
                } else {
                    // no problems so go ahead and save
                    alertDialog.dismiss();
                    blockUser(username);
                }
            });
        });
        alertDialog.show();
    }

    private void blockUser(String username) {
        AndroidUtils.showLoadingDialog(loadingDialog, "Blocking user...");

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ResultStatus<String> resultStatus = userManager.blockUser(username);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                loadingDialog.dismiss();
                if (resultStatus.isSuccess() && BlockedListFragment.this.isResumed()) {
                    blocked.add(username);
                    Collections.sort(blocked);
                    blockedAdapter.notifyDataSetChanged();
                } else {
                    AndroidUtils.showErrorDialog(resultStatus.getErrorMessage(), getContext());
                }
            });
        });
    }

    private void unblockUser(String username) {
        // assume it always succeeds
        int index = blocked.indexOf(username);
        blocked.remove(index);
        blockedAdapter.notifyItemRemoved(index);

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ResultStatus<String> resultStatus = userManager.unblockUser(username);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                if (resultStatus.isFailure() && BlockedListFragment.this.isResumed()) {
                    // on off chance it failed add the blocked user back to the list
                    blocked.add(index, username);
                    blockedAdapter.notifyItemInserted(index);
                    checkEmptyList();
                    AndroidUtils.showErrorDialog(resultStatus.getErrorMessage(), getContext());
                }
            });
        });
    }

    private void showBlownUpProfilePic(String username, String icon) {
        View popupView = getLayoutInflater().inflate(R.layout.popup_blown_up_profile_picture, null);
        ImageView profilePicture = popupView.findViewById(R.id.profile_picture_image);
        Picasso.get()
                .load(ImageUtils.getIconUrl(icon))
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
    private class BlockedAdapter extends RecyclerView.Adapter<BlockedAdapter.ViewHolder> {
        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView usernameTV;
            final ImageView profilePicture;
            final RelativeLayout rootLayout;

            ViewHolder(View itemView) {
                super(itemView);
                rootLayout = itemView.findViewById(R.id.username_pic_container);
                usernameTV = itemView.findViewById(R.id.username_tv);
                profilePicture = itemView.findViewById(R.id.profile_picture_image);
            }
        }

        private final Map<String, String> usernameToIcon;

        BlockedAdapter(Map<String, String> usernameToIcon) {
            this.usernameToIcon = usernameToIcon;
        }

        @NonNull
        @Override
        public BlockedAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View blockedView = inflater.inflate(R.layout.row_blocked, parent, false);
            return new BlockedAdapter.ViewHolder(blockedView);
        }

        @Override
        public void onBindViewHolder(BlockedAdapter.ViewHolder holder, int position) {
            String blockedUser = blocked.get(position);
            String icon = usernameToIcon.get(blockedUser);

            RelativeLayout rootLayout = holder.rootLayout;
            rootLayout.setOnClickListener(v -> {
                // sets up a bottom dialog that is shown whenever a user clicks on the row
                bottomSheetDialog = new BottomSheetDialog(getActivity());
                View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_blocked_list, null);
                TextView unblockTV = sheetView.findViewById(R.id.unblock_tv);
                unblockTV.setOnClickListener(view -> {
                    unblockUser(blockedUser);
                    bottomSheetDialog.dismiss();
                });

                RelativeLayout relativeLayout = sheetView.findViewById(R.id.username_pic_container);
                relativeLayout.setOnClickListener(v1 -> showBlownUpProfilePic(blockedUser, icon));
                TextView usernameTV = sheetView.findViewById(R.id.username_tv);
                ImageView profilePicture = sheetView.findViewById(R.id.profile_picture_image);
                usernameTV.setText(blockedUser);
                Picasso.get()
                        .load(ImageUtils.getIconUrl(icon))
                        .error(R.drawable.picture_load_error)
                        .networkPolicy(NetworkPolicy.NO_CACHE) // on first loading in app, always fetch online
                        .into(profilePicture, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                if (!BlockedListFragment.this.isResumed()) {
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
            ImageView profilePicture = holder.profilePicture;
            usernameTV.setText(blockedUser);
            Picasso.get()
                    .load(ImageUtils.getIconUrl(icon))
                    .error(R.drawable.picture_load_error)
                    .networkPolicy(NetworkPolicy.NO_CACHE) // on first loading in app, always fetch online
                    .into(profilePicture, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            if (!BlockedListFragment.this.isResumed()) {
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
            return blocked.size();
        }
    }
    //endregion
}
