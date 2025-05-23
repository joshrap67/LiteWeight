package com.joshrap.liteweight.fragments;

import static android.os.Looper.getMainLooper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.credentials.ClearCredentialStateRequest;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.joshrap.liteweight.R;
import com.joshrap.liteweight.activities.MainActivity;
import com.joshrap.liteweight.imports.BackendConfig;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.interfaces.FragmentWithDialog;
import com.joshrap.liteweight.managers.CurrentUserModule;
import com.joshrap.liteweight.managers.SelfManager;
import com.joshrap.liteweight.models.Result;
import com.joshrap.liteweight.models.user.UserSettings;
import com.joshrap.liteweight.utils.AndroidUtils;
import com.joshrap.liteweight.utils.FirebaseUtils;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;

public class SettingsFragment extends Fragment implements FragmentWithDialog {

    private UserSettings userSettings;
    private boolean metricChanged, privateChanged, saveChanged, restartChanged;
    private int dangerZoneRotationAngle;
    private AlertDialog alertDialog;
    private CredentialManager credentialManager;
    private FirebaseAuth auth;

    @Inject
    SelfManager selfManager;
    @Inject
    SharedPreferences sharedPreferences;
    @Inject
    CurrentUserModule currentUserModule;
    @Inject
    AlertDialog loadingDialog;
    private SwitchCompat privateSwitch, metricSwitch, updateOnSaveSwitch, updateOnRestartSwitch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentActivity fragmentActivity = requireActivity();
        fragmentActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        ((MainActivity) fragmentActivity).updateToolbarTitle(Variables.SETTINGS_TITLE);
        ((MainActivity) fragmentActivity).toggleBackButton(true);
        Injector.getInjector(getContext()).inject(this);
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        credentialManager = CredentialManager.create(requireActivity());
        auth = FirebaseAuth.getInstance();

        userSettings = currentUserModule.getUser().getSettings();
        metricChanged = false;
        privateChanged = false;
        saveChanged = false;
        restartChanged = false;

        // account settings
        privateSwitch = view.findViewById(R.id.private_account_switch);
        metricSwitch = view.findViewById(R.id.metric_switch);
        updateOnSaveSwitch = view.findViewById(R.id.update_on_save_switch);
        updateOnRestartSwitch = view.findViewById(R.id.update_on_restart_switch);

        LinearLayout metricLayout = view.findViewById(R.id.metric_container);
        metricLayout.setOnClickListener(view1 -> metricSwitch.performClick());
        metricSwitch.setChecked(userSettings.isMetricUnits());
        metricSwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) -> metricChanged = isChecked != userSettings.isMetricUnits());

        LinearLayout privateLayout = view.findViewById(R.id.private_account_container);
        privateLayout.setOnClickListener(view1 -> privateSwitch.performClick());
        privateSwitch.setChecked(userSettings.isPrivateAccount());
        privateSwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) -> privateChanged = isChecked != userSettings.isPrivateAccount());

        LinearLayout updateOnRestartLayout = view.findViewById(R.id.update_on_restart_container);
        updateOnRestartLayout.setOnClickListener(view1 -> updateOnRestartSwitch.performClick());
        updateOnRestartSwitch.setChecked(userSettings.isUpdateDefaultWeightOnRestart());
        updateOnRestartSwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) -> restartChanged = isChecked != userSettings.isUpdateDefaultWeightOnRestart());

        LinearLayout updateOnSaveLayout = view.findViewById(R.id.update_on_save_container);
        updateOnSaveLayout.setOnClickListener(view1 -> updateOnSaveSwitch.performClick());
        updateOnSaveSwitch.setChecked(userSettings.isUpdateDefaultWeightOnSave());
        updateOnSaveSwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) -> saveChanged = isChecked != userSettings.isUpdateDefaultWeightOnSave());

        // app settings
        SwitchCompat stopwatchSwitch = view.findViewById(R.id.stopwatch_switch);
        SwitchCompat timerSwitch = view.findViewById(R.id.timer_switch);
        SwitchCompat darkThemeSwitch = view.findViewById(R.id.dark_theme_switch);
        SwitchCompat workoutProgressSwitch = view.findViewById(R.id.workout_progress_switch);
        final SharedPreferences.Editor editor = sharedPreferences.edit();

        LinearLayout darkThemeLayout = view.findViewById(R.id.dark_theme_layout);
        darkThemeLayout.setOnClickListener(view1 -> darkThemeSwitch.performClick());
        darkThemeSwitch.setChecked(sharedPreferences.getBoolean(Variables.DARK_THEME_ENABLED, true));
        darkThemeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean(Variables.DARK_THEME_ENABLED, isChecked);
            editor.apply();
        });

        LinearLayout stopwatchLayout = view.findViewById(R.id.stopwatch_container);
        stopwatchLayout.setOnClickListener(view1 -> stopwatchSwitch.performClick());
        stopwatchSwitch.setChecked(sharedPreferences.getBoolean(Variables.STOPWATCH_ENABLED, true));
        stopwatchSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ((MainActivity) fragmentActivity).cancelStopwatchService();
            ((MainActivity) fragmentActivity).getStopwatch().stopStopwatch();
            editor.putBoolean(Variables.STOPWATCH_ENABLED, isChecked);
            editor.apply();
        });
        LinearLayout timerLayout = view.findViewById(R.id.timer_layout);
        timerLayout.setOnClickListener(view1 -> timerSwitch.performClick());
        timerSwitch.setChecked(sharedPreferences.getBoolean(Variables.TIMER_ENABLED, true));
        timerSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ((MainActivity) fragmentActivity).cancelTimerService();
            ((MainActivity) fragmentActivity).getTimer().stopTimer();
            editor.putBoolean(Variables.TIMER_ENABLED, isChecked);
            editor.apply();
        });

        LinearLayout workoutProgressLayout = view.findViewById(R.id.workout_progress_container);
        workoutProgressLayout.setOnClickListener(view1 -> workoutProgressSwitch.performClick());
        workoutProgressSwitch.setChecked(sharedPreferences.getBoolean(Variables.WORKOUT_PROGRESS_KEY, true));
        workoutProgressSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean(Variables.WORKOUT_PROGRESS_KEY, isChecked);
            editor.apply();
        });

        TextView manageNotificationsTV = view.findViewById(R.id.manage_notifications_tv);
        manageNotificationsTV.setOnClickListener(view1 -> {
            Intent intent = new Intent();
            Context context = requireContext();
            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
            context.startActivity(intent);
        });

        // danger zone
        RelativeLayout dangerZoneLayout = view.findViewById(R.id.danger_zone_container);
        TextView deleteAccountTV = view.findViewById(R.id.delete_account_tv);
        ImageButton dangerZoneIcon = view.findViewById(R.id.danger_zone_icon_btn);
        View.OnClickListener dangerZoneClicked = v -> {
            boolean visible = deleteAccountTV.getVisibility() == View.VISIBLE;
            deleteAccountTV.setVisibility(visible ? View.GONE : View.VISIBLE);
            dangerZoneRotationAngle = dangerZoneRotationAngle == 0 ? 180 : 0;
            dangerZoneIcon.animate().rotation(dangerZoneRotationAngle).setDuration(400).start();
            TransitionManager.beginDelayedTransition(container, new AutoTransition());
        };
        deleteAccountTV.setOnClickListener(v -> promptDeleteAccount());
        dangerZoneLayout.setOnClickListener(dangerZoneClicked);
        dangerZoneIcon.setOnClickListener(dangerZoneClicked);
        return view;
    }

    private void promptDeleteAccount() {
        boolean requirePassword = FirebaseUtils.userHasPassword(auth.getCurrentUser());
        View popupView = getLayoutInflater().inflate(R.layout.popup_delete_account, null);
        EditText passwordInput = popupView.findViewById(R.id.password_input);
        TextInputLayout passwordInputLayout = popupView.findViewById(R.id.password_input_layout);
        passwordInput.addTextChangedListener(AndroidUtils.hideErrorTextWatcher(passwordInputLayout));

        TextView deleteAccountTV = popupView.findViewById(R.id.delete_account_msg_tv);
        if (requirePassword) {
            deleteAccountTV.setText(R.string.delete_account_with_password_msg);
            passwordInputLayout.setVisibility(View.VISIBLE);
        } else {
            deleteAccountTV.setText(R.string.delete_account_with_social_msg);
            passwordInputLayout.setVisibility(View.GONE);
        }

        alertDialog = new AlertDialog.Builder(requireContext())
                .setTitle("Delete Account")
                .setView(popupView)
                .setPositiveButton("Delete", null)
                .setNegativeButton("Cancel", null)
                .create();

        alertDialog.setOnShowListener(dialogInterface -> {
            Button deleteButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button cancelButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            cancelButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_danger_zone));
            deleteButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_danger_zone));
            deleteButton.setOnClickListener(view -> {
                String password = passwordInput.getText().toString().trim();
                if (requirePassword && password.isEmpty()) {
                    passwordInputLayout.setError("Required");
                } else if (requirePassword) {
                    alertDialog.dismiss();
                    deleteAccount(EmailAuthProvider.getCredential(currentUserModule.getUser().getEmail(), password));
                } else {
                    alertDialog.dismiss();
                    googleSignIn();
                }
            });
        });
        alertDialog.show();
    }

    private void googleSignIn() {
        GetSignInWithGoogleOption googleIdOption = new GetSignInWithGoogleOption(BackendConfig.googleSignInClientId, null, null);

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();
        credentialManager.getCredentialAsync(requireActivity(),
                request,
                new CancellationSignal(),
                Executors.newSingleThreadExecutor(),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.getCredential().getData());

                        requireActivity().runOnUiThread(() -> handleGoogleSignInResult(googleIdTokenCredential));
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException exception) {
                        requireActivity().runOnUiThread(() -> AndroidUtils.showErrorDialog("There was an error signing in with Google.", requireActivity()));
                    }
                }
        );
    }

    private void handleGoogleSignInResult(GoogleIdTokenCredential googleIdTokenCredential) {
        try {
            if (!googleIdTokenCredential.getId().equals(currentUserModule.getUser().getEmail())) {
                Toast.makeText(getContext(), "Gmail does not match email of user.", Toast.LENGTH_SHORT).show();
                googleSignOut();
                return;
            }

            String idToken = googleIdTokenCredential.getIdToken();
            AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
            googleSignOut();
            deleteAccount(firebaseCredential);
        } catch (Exception e) {
            AndroidUtils.showErrorDialog("There was an error signing in with Google.", requireActivity());
        }
    }

    private void googleSignOut() {
        // only using google sign in for getting id token to link to firebase. Can immediately log out once getting that token
        Continuation<Unit> continuation = new Continuation<Unit>() {
            @NonNull
            @Override
            public CoroutineContext getContext() {
                return EmptyCoroutineContext.INSTANCE;
            }

            @Override
            public void resumeWith(@NonNull Object o) {

            }
        };
        credentialManager.clearCredentialState(new ClearCredentialStateRequest(), continuation);
    }

    private void deleteAccount(AuthCredential credential) {
        AndroidUtils.showLoadingDialog(loadingDialog, "Deleting account...");
        FirebaseUser user = auth.getCurrentUser();

        user.reauthenticate(credential).addOnCompleteListener(reAuthTask -> {
            if (reAuthTask.isSuccessful()) {
                Executor executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    Result<String> result = this.selfManager.deleteSelf();
                    Handler handler = new Handler(getMainLooper());
                    handler.post(() -> {
                        loadingDialog.dismiss();
                        if (result.isSuccess()) {
                            ((MainActivity) requireActivity()).forceKill();
                        } else {
                            AndroidUtils.showErrorDialog(result.getErrorMessage(), getContext());
                        }
                    });
                });
            } else {
                loadingDialog.dismiss();
                AndroidUtils.showErrorDialog("There was a problem with authentication.", getContext());
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (metricChanged || privateChanged || restartChanged || saveChanged) {
            // one or more settings has changed, so update it in the DB and the local user settings
            userSettings.setMetricUnits(metricSwitch.isChecked());
            userSettings.setPrivateAccount(privateSwitch.isChecked());
            userSettings.setUpdateDefaultWeightOnSave(updateOnSaveSwitch.isChecked());
            userSettings.setUpdateDefaultWeightOnRestart(updateOnRestartSwitch.isChecked());
            // note that due to blind send the work above breaks the manager pattern a little bit since code is duplicated in manager call. but idc i don't want a loading dialog for this

            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                Result<String> result = this.selfManager.setUserSettings(userSettings);
                Handler handler = new Handler(getMainLooper());
                handler.post(() -> {
                    if (result.isFailure()) {
                        AndroidUtils.showErrorDialog(result.getErrorMessage(), getContext());
                    }
                });
            });
        }
    }

    @Override
    public void hideAllDialogs() {
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }
}
