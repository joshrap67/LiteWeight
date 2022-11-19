package com.joshrap.liteweight.fragments;

import android.app.Activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.activities.WorkoutActivity;
import com.joshrap.liteweight.models.Tokens;
import com.joshrap.liteweight.utils.AndroidUtils;
import com.joshrap.liteweight.utils.ImageUtils;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.interfaces.FragmentWithDialog;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.models.User;
import com.joshrap.liteweight.models.UserWithWorkout;
import com.joshrap.liteweight.network.repos.UserRepository;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import static android.os.Looper.getMainLooper;

public class MyAccountFragment extends Fragment implements FragmentWithDialog {
    private User user;
    private ImageView profilePicture;
    private String profilePicUrl;
    private AlertDialog alertDialog;
    private TextView friendsListTV;
    @Inject
    UserRepository userRepository;
    @Inject
    Tokens tokens;

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
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        Injector.getInjector(getContext()).inject(this);
        ((WorkoutActivity) getActivity()).updateToolbarTitle(Variables.ACCOUNT_TITLE);
        ((WorkoutActivity) getActivity()).toggleBackButton(false);

        UserWithWorkout userWithWorkout = ((WorkoutActivity) getActivity()).getUserWithWorkout();
        user = userWithWorkout.getUser();
        return inflater.inflate(R.layout.fragment_my_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        TextView usernameTV = view.findViewById(R.id.username_tv);
        usernameTV.setText(user.getUsername());
        Button changePictureButton = view.findViewById(R.id.change_picture_btn);
        changePictureButton.setVisibility(View.GONE);
        changePictureButton.setOnClickListener(v -> launchPhotoPicker());

        String email = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            // yes this is a war crime but it beats refactoring the database to add emails to it
            String[] jwtParts = tokens.getIdToken().split("\\.");
            Base64.Decoder decoder = Base64.getUrlDecoder();
            String payloadJson = new String(decoder.decode(jwtParts[1]));
            ObjectMapper mapper = new ObjectMapper();
            try {
                Map<String, String> map = mapper.readValue(payloadJson, Map.class);
                email = map.get("email");
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        TextView emailTV = view.findViewById(R.id.email_tv);
        emailTV.setVisibility(email == null ? View.GONE : View.VISIBLE);
        emailTV.setText(email);

        friendsListTV = view.findViewById(R.id.friends_list_tv);
        friendsListTV.setOnClickListener(v -> ((WorkoutActivity) getActivity()).goToFriendsList(null));
        TextView accountPrefsTV = view.findViewById(R.id.account_preferences_tv);
        accountPrefsTV.setOnClickListener(v -> ((WorkoutActivity) getActivity()).goToAccountPreferences());
        TextView blockedListTV = view.findViewById(R.id.blocked_list_tv);
        blockedListTV.setOnClickListener(view1 -> ((WorkoutActivity) getActivity()).goToBlockedList());

        Button logoutButton = view.findViewById(R.id.log_out_btn);
        logoutButton.setOnClickListener(view1 -> promptLogout());
        profilePicture = view.findViewById(R.id.profile_picture_image);
        profilePicture.setOnClickListener(v -> launchPhotoPicker());
        updateFriendsTvNotification();

        profilePicUrl = ImageUtils.getIconUrl(user.getIcon());
        Picasso.get()
                .load(profilePicUrl)
                .error(R.drawable.picture_load_error)
                .networkPolicy(NetworkPolicy.NO_CACHE) // on first loading in app, always fetch online
                .into(profilePicture, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        changePictureButton.setVisibility(View.VISIBLE);
                        setCircularImage(profilePicture);
                    }

                    @Override
                    public void onError(Exception e) {
                        changePictureButton.setVisibility(View.VISIBLE);
                    }
                });
        super.onViewCreated(view, savedInstanceState);
    }

    private void setCircularImage(ImageView profilePictureImageView) {
        Bitmap imageBitmap = ((BitmapDrawable) profilePictureImageView.getDrawable()).getBitmap();
        RoundedBitmapDrawable imageDrawable = RoundedBitmapDrawableFactory.create(getResources(), imageBitmap);
        imageDrawable.setCircular(true);
        imageDrawable.setCornerRadius(Math.max(imageBitmap.getWidth(), imageBitmap.getHeight()) / 2.0f);
        profilePictureImageView.setImageDrawable(imageDrawable);
    }

    @Override
    public void onPause() {
        super.onPause();
        hideAllDialogs();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(notificationReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter receiverActions = new IntentFilter();
        receiverActions.addAction(Variables.NEW_FRIEND_REQUEST_MODEL_UPDATED_BROADCAST);
        receiverActions.addAction(Variables.CANCELED_REQUEST_MODEL_UPDATED_BROADCAST);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(notificationReceiver, receiverActions);
    }

    @Override
    public void hideAllDialogs() {
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }

    private final ActivityResultLauncher<Intent> pickPhotoLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result == null)
                    return;

                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (result.getData() != null) {
                        // a picture was successfully picked, so immediately send it to be cropped
                        try {
                            final Uri selectedUri = result.getData().getData();
                            performCrop(selectedUri);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

    private final ActivityResultLauncher<Intent> cropPhotoLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result == null)
                    return;

                if (result.getResultCode() == Activity.RESULT_OK) {
                    final Uri uri = UCrop.getOutput(result.getData());
                    if (uri != null) {
                        profilePicture.setImageURI(uri);
                        setCircularImage(profilePicture);
                        ((WorkoutActivity) getActivity()).updateUserIcon(uri); // update icon in nav view since it has old one
                        try {
                            InputStream iStream = getActivity().getContentResolver().openInputStream(uri);
                            updateIcon(ImageUtils.getImageByteArray(iStream));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Picasso.get().invalidate(profilePicUrl); // since upload was successful,
                    }
                }
            });

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
                ResultStatus<String> resultStatus = this.userRepository.updateProfilePicture(new ObjectMapper().writeValueAsString(imageData));
                Handler handler = new Handler(getMainLooper());
                handler.post(() -> {
                    if (!resultStatus.isSuccess()) {
                        AndroidUtils.showErrorDialog(resultStatus.getErrorMessage(), getContext());
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
        pickPhotoLauncher.launch(intent);
    }

    private void performCrop(Uri picUri) {
        String destinationFileName = UUID.randomUUID().toString() + ".png";
        UCrop cropper = UCrop.of(picUri, Uri.fromFile(new File(getActivity().getCacheDir(), destinationFileName)));
        cropper.withAspectRatio(1, 1);
        cropper.withMaxResultSize(600, 600);

        UCrop.Options options = new UCrop.Options();
        options.setHideBottomControls(true);

        options.setToolbarColor(ContextCompat.getColor(getContext(), R.color.color_primary));
        options.setStatusBarColor(ContextCompat.getColor(getContext(), R.color.color_primary));
        options.setToolbarWidgetColor(ContextCompat.getColor(getContext(), R.color.color_accent));
        options.setCompressionFormat(Bitmap.CompressFormat.PNG);
        options.setToolbarTitle("Crop Profile Picture");

        cropper.withOptions(options);
        cropper.getIntent(getContext());
        cropPhotoLauncher.launch(cropper.getIntent(getContext()));
    }

    private void promptLogout() {
        alertDialog = new AlertDialog.Builder(getContext())
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out? If so, all your data will be saved in the cloud.")
                .setPositiveButton("Yes", (dialog, which) -> ((WorkoutActivity) getActivity()).logout())
                .setNegativeButton("No", null)
                .create();
        alertDialog.show();
    }
}
