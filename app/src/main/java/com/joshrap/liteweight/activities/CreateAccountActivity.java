package com.joshrap.liteweight.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.managers.SelfManager;
import com.joshrap.liteweight.models.Result;
import com.joshrap.liteweight.models.user.User;
import com.joshrap.liteweight.managers.CurrentUserModule;
import com.joshrap.liteweight.utils.AndroidUtils;
import com.joshrap.liteweight.utils.ImageUtils;
import com.joshrap.liteweight.utils.ValidatorUtils;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

public class CreateAccountActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private ImageView profilePicture;
    private boolean metricUnits, shouldFinish;
    private byte[] profileImageData;

    @Inject
    AlertDialog loadingDialog;
    @Inject
    SelfManager selfManager;
    @Inject
    CurrentUserModule currentUserModule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Injector.getInjector(this).inject(this);

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            launchSignInActivity("There was a problem with your account. Please try and login again.");
            shouldFinish = true;
            return;
        }

        setContentView(R.layout.activity_create_account);

        Button logoutButton = findViewById(R.id.log_out_btn);
        Button createUserButton = findViewById(R.id.create_user_btn);
        TextView signedInAsTV = findViewById(R.id.signed_in_as_tv);
        signedInAsTV.setText(String.format("%s %s", getString(R.string.signed_in_as), user.getEmail()));

        SwitchCompat metricSwitch = findViewById(R.id.metric_switch);
        LinearLayout metricLayout = findViewById(R.id.metric_container);
        metricLayout.setOnClickListener(view1 -> metricSwitch.performClick());
        metricSwitch.setChecked(metricUnits);
        metricSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> metricUnits = isChecked);

        EditText usernameInput = findViewById(R.id.username_input);
        TextInputLayout usernameInputLayout = findViewById(R.id.username_input_layout);
        usernameInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.MAX_URL_LENGTH)});
        usernameInput.setOnKeyListener((View view, int keyCode, KeyEvent keyevent) -> {
            if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                String errorMsg = ValidatorUtils.validNewUsername(usernameInput.getText().toString().trim());
                if (errorMsg == null) {
                    usernameInputLayout.setError(null);
                    return true;
                } else {
                    usernameInputLayout.setError(errorMsg);
                }
            }
            return false;
        });
        usernameInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(usernameInputLayout));

        createUserButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String usernameErrorMsg = ValidatorUtils.validNewUsername(username);
            if (usernameErrorMsg != null) {
                usernameInputLayout.setError(usernameErrorMsg);
                usernameInputLayout.startAnimation(AndroidUtils.shakeError(2));
                return;
            }

            hideKeyboard();
            AndroidUtils.showLoadingDialog(loadingDialog, "Creating account...");
            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                Result<User> userResult = this.selfManager.createUser(username, profileImageData, metricUnits);
                Handler handler = new Handler(getMainLooper());
                handler.post(() -> {
                    loadingDialog.dismiss();

                    if (userResult.isSuccess() && userResult.getData() != null) {
                        launchMainActivity();
                    } else {
                        AndroidUtils.showErrorDialog(userResult.getErrorMessage(), this);
                    }
                });
            });
        });

        profilePicture = findViewById(R.id.profile_picture_image);
        profilePicture.setOnClickListener(v -> launchPhotoPicker());

        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            launchSignInActivity(null);
        });

        if (getIntent().getExtras() != null) {
            String errorMessage = getIntent().getExtras().getString(Variables.INTENT_ERROR_MESSAGE);
            if (errorMessage != null) {
                AndroidUtils.showErrorDialog(errorMessage, this);
            }
        }
    }

    private final ActivityResultLauncher<Intent> pickPhotoLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
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
                    try {
                        final Uri uri = UCrop.getOutput(result.getData());
                        if (uri != null) {
                            profilePicture.setImageURI(uri);
                            InputStream iStream = getContentResolver().openInputStream(uri);
                            profileImageData = ImageUtils.getImageByteArray(iStream);
                        }
                    } catch (Exception e) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                    }

                }
            });

    private void launchPhotoPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        pickPhotoLauncher.launch(intent);
    }

    private void performCrop(Uri picUri) {
        String destinationFileName = UUID.randomUUID().toString() + ".png";
        UCrop cropper = UCrop.of(picUri, Uri.fromFile(new File(getCacheDir(), destinationFileName)));
        cropper.withAspectRatio(1, 1);
        cropper.withMaxResultSize(600, 600);

        UCrop.Options options = new UCrop.Options();
        options.setHideBottomControls(true);

        options.setToolbarColor(ContextCompat.getColor(this, R.color.color_primary));
        options.setStatusBarColor(ContextCompat.getColor(this, R.color.color_primary));
        options.setToolbarWidgetColor(ContextCompat.getColor(this, R.color.color_accent));
        options.setCompressionFormat(Bitmap.CompressFormat.PNG);
        options.setToolbarTitle("Crop Profile Picture");

        cropper.withOptions(options);
        cropper.getIntent(this);
        cropPhotoLauncher.launch(cropper.getIntent(this));
    }

    private void launchMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        shouldFinish = true;
    }

    private void launchSignInActivity(String errorMessage) {
        Intent intent = new Intent(this, SignInActivity.class);
        if (errorMessage != null) {
            intent.putExtra(Variables.INTENT_ERROR_MESSAGE, errorMessage);
        }
        startActivity(intent);
        shouldFinish = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (shouldFinish) {
            finishAffinity();
        }
    }

    private void hideKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }
}
