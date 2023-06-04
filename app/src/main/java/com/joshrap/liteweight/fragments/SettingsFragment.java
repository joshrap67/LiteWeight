package com.joshrap.liteweight.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import com.joshrap.liteweight.R;
import com.joshrap.liteweight.activities.MainActivity;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;
import com.joshrap.liteweight.managers.UserManager;
import com.joshrap.liteweight.models.Result;
import com.joshrap.liteweight.models.user.UserPreferences;
import com.joshrap.liteweight.managers.CurrentUserAndWorkoutProvider;
import com.joshrap.liteweight.utils.AndroidUtils;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import static android.os.Looper.getMainLooper;

public class SettingsFragment extends Fragment {

    private UserPreferences userPreferences;
    private boolean metricChanged, privateChanged, saveChanged, restartChanged;
    private int dangerZoneRotationAngle;

    @Inject
    UserManager userManager;
    @Inject
    SharedPreferences sharedPreferences;
    @Inject
    CurrentUserAndWorkoutProvider currentUserAndWorkoutProvider;
    private SwitchCompat privateSwitch, metricSwitch, updateOnSaveSwitch, updateOnRestartSwitch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        ((MainActivity) getActivity()).updateToolbarTitle(Variables.SETTINGS_TITLE);
        ((MainActivity) getActivity()).toggleBackButton(true);
        Injector.getInjector(getContext()).inject(this);
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        userPreferences = currentUserAndWorkoutProvider.provideCurrentUser().getPreferences();
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
        metricSwitch.setChecked(userPreferences.isMetricUnits());
        metricSwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) -> metricChanged = isChecked != userPreferences.isMetricUnits());

        LinearLayout privateLayout = view.findViewById(R.id.private_account_container);
        privateLayout.setOnClickListener(view1 -> privateSwitch.performClick());
        privateSwitch.setChecked(userPreferences.isPrivateAccount());
        privateSwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) -> privateChanged = isChecked != userPreferences.isPrivateAccount());

        LinearLayout updateOnRestartLayout = view.findViewById(R.id.update_on_restart_container);
        updateOnRestartLayout.setOnClickListener(view1 -> updateOnRestartSwitch.performClick());
        updateOnRestartSwitch.setChecked(userPreferences.isUpdateDefaultWeightOnRestart());
        updateOnRestartSwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) -> restartChanged = isChecked != userPreferences.isUpdateDefaultWeightOnRestart());

        LinearLayout updateOnSaveLayout = view.findViewById(R.id.update_on_save_container);
        updateOnSaveLayout.setOnClickListener(view1 -> updateOnSaveSwitch.performClick());
        updateOnSaveSwitch.setChecked(userPreferences.isUpdateDefaultWeightOnSave());
        updateOnSaveSwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) -> saveChanged = isChecked != userPreferences.isUpdateDefaultWeightOnSave());

        // app settings todo it might be confusing that these are not saved in cloud. at least perhaps tell users this
        SwitchCompat videoSwitch = view.findViewById(R.id.video_switch);
        SwitchCompat stopwatchSwitch = view.findViewById(R.id.stopwatch_switch);
        SwitchCompat timerSwitch = view.findViewById(R.id.timer_switch);
        SwitchCompat workoutProgressSwitch = view.findViewById(R.id.workout_progress_switch);
        final SharedPreferences.Editor editor = sharedPreferences.edit();

        LinearLayout stopwatchLayout = view.findViewById(R.id.stopwatch_container);
        stopwatchLayout.setOnClickListener(view1 -> stopwatchSwitch.performClick());
        stopwatchSwitch.setChecked(sharedPreferences.getBoolean(Variables.STOPWATCH_ENABLED, true));
        stopwatchSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ((MainActivity) getActivity()).cancelStopwatchService();
            ((MainActivity) getActivity()).getStopwatch().stopStopwatch();
            editor.putBoolean(Variables.STOPWATCH_ENABLED, isChecked);
            editor.apply();
        });
        LinearLayout timerLayout = view.findViewById(R.id.timer_layout);
        timerLayout.setOnClickListener(view1 -> timerSwitch.performClick());
        timerSwitch.setChecked(sharedPreferences.getBoolean(Variables.TIMER_ENABLED, true));
        timerSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ((MainActivity) getActivity()).cancelTimerService();
            ((MainActivity) getActivity()).getTimer().stopTimer();
            editor.putBoolean(Variables.TIMER_ENABLED, isChecked);
            editor.apply();
        });
        LinearLayout videoLayout = view.findViewById(R.id.video_layout);
        videoLayout.setOnClickListener(view1 -> videoSwitch.performClick());
        videoSwitch.setChecked(sharedPreferences.getBoolean(Variables.VIDEO_KEY, true));
        videoSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean(Variables.VIDEO_KEY, isChecked);
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
            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, getContext().getPackageName());
            getContext().startActivity(intent);
        });

        // danger zone
        RelativeLayout dangerZoneLayout = view.findViewById(R.id.danger_zone_container);
        TextView deleteAccountTV = view.findViewById(R.id.delete_account_tv); // todo perform delete
        ImageButton dangerZoneIcon = view.findViewById(R.id.danger_zone_icon_btn);
        View.OnClickListener dangerZoneClicked = v -> {
            boolean visible = deleteAccountTV.getVisibility() == View.VISIBLE;
            deleteAccountTV.setVisibility(visible ? View.GONE : View.VISIBLE);
            dangerZoneRotationAngle = dangerZoneRotationAngle == 0 ? 180 : 0;
            dangerZoneIcon.animate().rotation(dangerZoneRotationAngle).setDuration(400).start();
            TransitionManager.beginDelayedTransition(container, new AutoTransition());
        };
        dangerZoneLayout.setOnClickListener(dangerZoneClicked);
        dangerZoneIcon.setOnClickListener(dangerZoneClicked);
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (metricChanged || privateChanged || restartChanged || saveChanged) {
            // one or more settings has changed, so update it in the DB and the local user settings
            userPreferences.setMetricUnits(metricSwitch.isChecked());
            userPreferences.setPrivateAccount(privateSwitch.isChecked());
            userPreferences.setUpdateDefaultWeightOnSave(updateOnSaveSwitch.isChecked());
            userPreferences.setUpdateDefaultWeightOnRestart(updateOnRestartSwitch.isChecked());
            // note that due to blind send the work above breaks the manager pattern a little bit since code is duplicated in manager call. but idc i don't want a loading dialog for this

            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                Result<String> result = this.userManager.updateUserPreferences(userPreferences);
                Handler handler = new Handler(getMainLooper());
                handler.post(() -> {
                    if (result.isFailure()) {
                        AndroidUtils.showErrorDialog(result.getErrorMessage(), getContext());
                    }
                });
            });
        }
    }
}
