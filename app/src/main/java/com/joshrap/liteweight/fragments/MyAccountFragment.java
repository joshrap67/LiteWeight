package com.joshrap.liteweight.fragments;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.activities.MainActivity;
import com.joshrap.liteweight.managers.UserManager;
import com.joshrap.liteweight.managers.CurrentUserModule;
import com.joshrap.liteweight.utils.AndroidUtils;
import com.joshrap.liteweight.utils.FirebaseUtils;
import com.joshrap.liteweight.utils.ImageUtils;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.interfaces.FragmentWithDialog;
import com.joshrap.liteweight.models.Result;
import com.joshrap.liteweight.models.user.User;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import static android.os.Looper.getMainLooper;

public class MyAccountFragment extends Fragment implements FragmentWithDialog {
    private ImageView profilePicture;
    private String username, email, profilePictureUrl;
    private String profilePicUrl;
    private AlertDialog alertDialog;

    @Inject
    UserManager userManager;
    @Inject
    CurrentUserModule currentUserModule;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentActivity activity = requireActivity();
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        Injector.getInjector(getContext()).inject(this);
        ((MainActivity) activity).updateToolbarTitle(Variables.ACCOUNT_TITLE);
        ((MainActivity) activity).toggleBackButton(false);

        User user = currentUserModule.getUser();
        username = user.getUsername();
        profilePictureUrl = user.getProfilePicture();
        email = user.getEmail();
        return inflater.inflate(R.layout.fragment_my_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        TextView usernameTV = view.findViewById(R.id.username_tv);
        usernameTV.setText(username);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        LinearLayout passwordLayout = view.findViewById(R.id.password_layout);
        if (FirebaseUtils.userHasPassword(user)) {
            passwordLayout.setVisibility(View.VISIBLE);
            passwordLayout.setOnClickListener(v -> ((MainActivity) requireActivity()).goToChangePassword());
        } else {
            passwordLayout.setVisibility(View.GONE);
        }

        TextView emailTV = view.findViewById(R.id.email_tv);
        emailTV.setVisibility(email == null ? View.GONE : View.VISIBLE);
        emailTV.setText(email);

        LinearLayout settingsLayout = view.findViewById(R.id.settings_layout);
        settingsLayout.setOnClickListener(v -> ((MainActivity) requireActivity()).goToAccountPreferences());

        LinearLayout logoutLayout = view.findViewById(R.id.log_out_container);
        logoutLayout.setOnClickListener(view1 -> promptLogout());
        profilePicture = view.findViewById(R.id.profile_picture_image);
        profilePicture.setOnClickListener(v -> launchPhotoPicker());

        profilePicUrl = ImageUtils.getProfilePictureUrl(profilePictureUrl);
        Picasso.get()
                .load(profilePicUrl)
                .error(R.drawable.picture_load_error)
                .networkPolicy(NetworkPolicy.NO_CACHE) // on first loading in app, always fetch online
                .into(profilePicture);
        super.onViewCreated(view, savedInstanceState);
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
                            FirebaseCrashlytics.getInstance().recordException(e);
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
                        ((MainActivity) requireActivity()).updateProfilePicture(uri); // update icon in nav view since it has old one
                        try {
                            InputStream iStream = requireActivity().getContentResolver().openInputStream(uri);
                            updateProfilePicture(ImageUtils.getImageByteArray(iStream));
                        } catch (Exception e) {
                            FirebaseCrashlytics.getInstance().recordException(e);
                        }
                        Picasso.get().invalidate(profilePicUrl); // since upload was successful,
                    }
                }
            });

    private void updateProfilePicture(byte[] imageData) {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Result<String> result = this.userManager.updateProfilePicture(imageData);
            Handler handler = new Handler(getMainLooper());
            handler.post(() -> {
                if (result.isFailure()) {
                    AndroidUtils.showErrorDialog(result.getErrorMessage(), getContext());
                }
            });
        });
    }

    private void launchPhotoPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        pickPhotoLauncher.launch(intent);
    }

    private void performCrop(Uri picUri) {
        String destinationFileName = UUID.randomUUID().toString() + ".png";
        UCrop cropper = UCrop.of(picUri, Uri.fromFile(new File(requireActivity().getCacheDir(), destinationFileName)));
        cropper.withAspectRatio(1, 1);
        cropper.withMaxResultSize(600, 600);

        UCrop.Options options = new UCrop.Options();
        options.setHideBottomControls(true);

        Context context = requireContext();
        options.setToolbarColor(ContextCompat.getColor(context, R.color.color_primary));
        options.setStatusBarColor(ContextCompat.getColor(context, R.color.color_primary));
        options.setToolbarWidgetColor(ContextCompat.getColor(context, R.color.color_accent));
        options.setCompressionFormat(Bitmap.CompressFormat.PNG);
        options.setToolbarTitle("Crop Profile Picture");

        cropper.withOptions(options);
        cropper.getIntent(context);
        cropPhotoLauncher.launch(cropper.getIntent(context));
    }

    private void promptLogout() {
        alertDialog = new AlertDialog.Builder(requireContext())
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out? If so, all your data will be saved in the cloud.")
                .setPositiveButton("Yes", (dialog, which) -> ((MainActivity) requireActivity()).logout())
                .setNegativeButton("No", null)
                .create();
        alertDialog.show();
    }
}
