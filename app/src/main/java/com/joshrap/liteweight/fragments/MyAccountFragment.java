package com.joshrap.liteweight.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.activities.WorkoutActivity;
import com.joshrap.liteweight.helpers.ImageHelper;
import com.joshrap.liteweight.imports.Globals;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.models.User;
import com.joshrap.liteweight.network.repos.UserRepository;
import com.joshrap.liteweight.widgets.ErrorDialog;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import static android.os.Looper.getMainLooper;

public class MyAccountFragment extends Fragment {
    private static final int PICK_PHOTO_FOR_AVATAR = 1;
    private User user;
    private ImageView profilePicture;
    private String url;
    @Inject
    UserRepository userRepository;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_account, container, false);
        ((WorkoutActivity) getActivity()).updateToolbarTitle(Variables.ACCOUNT_TITLE);
        ((WorkoutActivity) getActivity()).toggleBackButton(true);
        Injector.getInjector(getContext()).inject(this);
        user = Globals.user;
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        /*
            Init all views and buttons when view is loaded onto screen
         */
        final TextView usernameTV = view.findViewById(R.id.username_tv);
        final TextView changePictureTv = view.findViewById(R.id.change_picture_tv);
        final TextView friendsListTV = view.findViewById(R.id.friends_list_tv);
        friendsListTV.setOnClickListener(v -> ((WorkoutActivity) getActivity()).goToFriendsList(null));
        final TextView accountPrefsTV = view.findViewById(R.id.account_preferences_tv);
        accountPrefsTV.setOnClickListener(v -> ((WorkoutActivity) getActivity()).goToAccountPreferences());
        final TextView blockedListTV = view.findViewById(R.id.blocked_list_tv);
        blockedListTV.setOnClickListener(view1 -> ((WorkoutActivity) getActivity()).goToBlockedList());
        changePictureTv.setVisibility(View.GONE);
        usernameTV.setText(user.getUsername());
        profilePicture = view.findViewById(R.id.profile_image);
        profilePicture.setOnClickListener(v -> getImage());
        final TextView receivedWorkoutsTV = view.findViewById(R.id.received_workouts_tv);
        int receivedUnseenCount = 0;
        for (String workoutId : user.getReceivedWorkouts().keySet()) {
            // todo actually do this
            receivedUnseenCount++;
        }
        int requestUnseenCount = 0;
        for (String username : user.getFriendRequests().keySet()) {
            if (!Objects.requireNonNull(user.getFriendRequests().get(username)).isSeen()) {
                requestUnseenCount++;
            }
        }
        if (requestUnseenCount > 0) {
            friendsListTV.setText(R.string.friends_list_alert);
        }

        url = ImageHelper.getIconUrl(user.getIcon());
        Picasso.get()
                .load(url)
                .error(R.drawable.new_icon_round)
                .networkPolicy(NetworkPolicy.NO_CACHE) // on first loading in app, always fetch online
                .into(profilePicture, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        changePictureTv.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError(Exception e) {
                        changePictureTv.setVisibility(View.VISIBLE);
                    }
                });
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == PICK_PHOTO_FOR_AVATAR && resultCode == Activity.RESULT_OK) {
            if (data != null) {
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
                try {
                    InputStream iStream = getActivity().getContentResolver().openInputStream(uri);
                    updateIcon(ImageHelper.getImageByteArray(iStream));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Picasso.get().invalidate(url); // since upload was successful,
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void updateIcon(byte[] imageData) {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ResultStatus<String> resultStatus = null;
            try {
                resultStatus = this.userRepository.updateProfilePicture(new ObjectMapper().writeValueAsString(imageData));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            Handler handler = new Handler(getMainLooper());
            ResultStatus<String> finalResultStatus = resultStatus;
            handler.post(() -> {
                // todo send image data back to workout activity for the icon in the drawer menu
                if (!finalResultStatus.isSuccess()) {
                    ErrorDialog.showErrorDialog("Upload Profile Picture Error", finalResultStatus.getErrorMessage(), getContext());
                }
            });
        });
    }

    private void getImage() {
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
}
