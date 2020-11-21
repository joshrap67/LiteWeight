package com.joshrap.liteweight.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.activities.WorkoutActivity;
import com.joshrap.liteweight.helpers.ImageHelper;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.interfaces.FragmentWithDialog;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.models.User;
import com.joshrap.liteweight.models.UserWithWorkout;
import com.joshrap.liteweight.network.repos.UserRepository;
import com.joshrap.liteweight.widgets.ErrorDialog;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import static android.os.Looper.getMainLooper;

public class MyAccountFragment extends Fragment implements FragmentWithDialog {
    private static final int PICK_PHOTO_FOR_AVATAR = 1;
    private User user;
    private ImageView profilePicture;
    private String profilePicUrl;
    private AlertDialog alertDialog;
    private TextView friendsListTV;
    @Inject
    UserRepository userRepository;

    private final BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                return;
            }
            if (action.equals(Variables.NEW_FRIEND_REQUEST_MODEL_UPDATED_BROADCAST) ||
                    action.equals(Variables.CANCELED_REQUEST_MODEL_UPDATED_BROADCAST)) {
                updateFriendsTvNotification();
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Injector.getInjector(getContext()).inject(this);
        ((WorkoutActivity) getActivity()).updateToolbarTitle(Variables.ACCOUNT_TITLE);
        ((WorkoutActivity) getActivity()).toggleBackButton(true);

        IntentFilter receiverActions = new IntentFilter();
        receiverActions.addAction(Variables.NEW_FRIEND_REQUEST_MODEL_UPDATED_BROADCAST);
        receiverActions.addAction(Variables.CANCELED_REQUEST_MODEL_UPDATED_BROADCAST);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(notificationReceiver, receiverActions);

        UserWithWorkout userWithWorkout = ((WorkoutActivity) getActivity()).getUserWithWorkout();
        user = userWithWorkout.getUser();
        return inflater.inflate(R.layout.fragment_my_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        final TextView usernameTV = view.findViewById(R.id.username_tv);
        usernameTV.setText(user.getUsername());
        final TextView changePictureTv = view.findViewById(R.id.change_picture_tv);
        changePictureTv.setVisibility(View.GONE);
        friendsListTV = view.findViewById(R.id.friends_list_tv);
        friendsListTV.setOnClickListener(v -> ((WorkoutActivity) getActivity()).goToFriendsList(null));
        final TextView accountPrefsTV = view.findViewById(R.id.account_preferences_tv);
        accountPrefsTV.setOnClickListener(v -> ((WorkoutActivity) getActivity()).goToAccountPreferences());
        final TextView blockedListTV = view.findViewById(R.id.blocked_list_tv);
        blockedListTV.setOnClickListener(view1 -> ((WorkoutActivity) getActivity()).goToBlockedList());
        Button logoutButton = view.findViewById(R.id.log_out_btn);
        logoutButton.setOnClickListener(view1 -> promptLogout());
        profilePicture = view.findViewById(R.id.profile_image);
        profilePicture.setOnClickListener(v -> launchPhotoPicker());
        updateFriendsTvNotification();

        profilePicUrl = ImageHelper.getIconUrl(user.getIcon());
        Picasso.get()
                .load(profilePicUrl)
                .error(R.drawable.picture_load_error)
                .networkPolicy(NetworkPolicy.NO_CACHE) // on first loading in app, always fetch online
                .into(profilePicture, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        if (MyAccountFragment.this.isResumed()) {
                            changePictureTv.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        if (MyAccountFragment.this.isResumed()) {
                            changePictureTv.setVisibility(View.VISIBLE);
                        }
                    }
                });
        super.onViewCreated(view, savedInstanceState);
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
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == PICK_PHOTO_FOR_AVATAR && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                // a picture was successfully picked, so no immediately send it to be cropped
                try {
                    final Uri selectedUri = data.getData();
                    performCrop(selectedUri);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (resultCode == Activity.RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            final Uri uri = UCrop.getOutput(data);
            if (uri != null) {
                profilePicture.setImageURI(uri);
                ((WorkoutActivity) getActivity()).updateUserIcon(uri); // update icon in nav view since it has old one
                try {
                    InputStream iStream = getActivity().getContentResolver().openInputStream(uri);
                    updateIcon(ImageHelper.getImageByteArray(iStream));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Picasso.get().invalidate(profilePicUrl); // since upload was successful,
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Updates whether an indicator should be shown on the view that takes users to their friend's list.
     */
    private void updateFriendsTvNotification() {
        int requestUnseenCount = 0;
        for (String username : user.getFriendRequests().keySet()) {
            if (!Objects.requireNonNull(user.getFriendRequests().get(username)).isSeen()) {
                requestUnseenCount++;
            }
        }
        if (requestUnseenCount > 0) {
            friendsListTV.setText(R.string.friends_list_alert);
        } else {
            friendsListTV.setText(R.string.friends_list);
        }
    }

    private void updateIcon(byte[] imageData) {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                final ResultStatus<String> resultStatus = this.userRepository.updateProfilePicture(new ObjectMapper().writeValueAsString(imageData));
                Handler handler = new Handler(getMainLooper());
                handler.post(() -> {
                    if (!resultStatus.isSuccess()) {
                        ErrorDialog.showErrorDialog("Upload Profile Picture Error", resultStatus.getErrorMessage(), getContext());
                    }
                });
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
    }

    private void launchPhotoPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_PHOTO_FOR_AVATAR);
    }

    private void performCrop(Uri picUri) {
        String destinationFileName = UUID.randomUUID().toString() + ".png";
        UCrop cropper = UCrop.of(picUri, Uri.fromFile(new File(getActivity().getCacheDir(), destinationFileName)));
        cropper.withAspectRatio(1, 1);
        cropper.withMaxResultSize(600, 600);

        UCrop.Options options = new UCrop.Options();
        options.setHideBottomControls(true);
        options.setToolbarColor(getResources().getColor(R.color.colorPrimary));
        options.setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        options.setToolbarWidgetColor(getResources().getColor(R.color.notification_color));
        options.setCompressionFormat(Bitmap.CompressFormat.PNG);
        options.setCompressionQuality(100);
        options.setToolbarTitle("Crop Profile Picture");

        cropper.withOptions(options);
        cropper.start(getActivity().getApplicationContext(), getFragmentManager().findFragmentByTag(Variables.ACCOUNT_TITLE));
    }

    private void promptLogout() {
        alertDialog = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out? If so, all your data will be saved in the cloud.")
                .setPositiveButton("Yes", (dialog, which) -> ((WorkoutActivity) getActivity()).logout())
                .setNegativeButton("No", null)
                .create();
        alertDialog.show();
    }
}
