package com.joshrap.liteweight.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;

import com.joshrap.liteweight.*;
import com.joshrap.liteweight.activities.WorkoutActivity;
import com.joshrap.liteweight.imports.Globals;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;

import javax.inject.Inject;

public class AppSettingsFragment extends Fragment {

    @Inject
    SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((WorkoutActivity) getActivity()).updateToolbarTitle(Variables.SETTINGS_TITLE);
        ((WorkoutActivity) getActivity()).toggleBackButton(false);
        Injector.getInjector(getContext()).inject(this);
        View view = inflater.inflate(R.layout.fragment_app_settings, container, false);
        SwitchCompat videoSwitch = view.findViewById(R.id.video_switch);
        SwitchCompat stopwatchSwitch = view.findViewById(R.id.stopwatch_switch);
        SwitchCompat timerSwitch = view.findViewById(R.id.timer_switch);
        SwitchCompat workoutProgressSwitch = view.findViewById(R.id.workout_progress_switch);

        final SharedPreferences.Editor editor = sharedPreferences.edit();

        LinearLayout stopwatchLayout = view.findViewById(R.id.stopwatch_layout);
        stopwatchLayout.setOnClickListener(view1 -> stopwatchSwitch.performClick());
        stopwatchSwitch.setChecked(sharedPreferences.getBoolean(Variables.STOPWATCH_ENABLED, true));
        stopwatchSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (Globals.stopwatchServiceRunning) {
                ((WorkoutActivity) getActivity()).getStopwatch().cancelService();
                ((WorkoutActivity) getActivity()).getStopwatch().stopStopwatch();
            }
            editor.putBoolean(Variables.STOPWATCH_ENABLED, isChecked);
            editor.apply();
        });
        LinearLayout timerLayout = view.findViewById(R.id.timer_layout);
        timerLayout.setOnClickListener(view1 -> timerSwitch.performClick());
        timerSwitch.setChecked(sharedPreferences.getBoolean(Variables.TIMER_ENABLED, true));
        timerSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (Globals.timerServiceRunning) {
                ((WorkoutActivity) getActivity()).getTimer().cancelService();
                ((WorkoutActivity) getActivity()).getTimer().stopTimer();
            }
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, getContext().getPackageName());
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                intent.putExtra("app_package", getContext().getPackageName());
                intent.putExtra("app_uid", getContext().getApplicationInfo().uid);
            } else {
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse("package:" + getContext().getPackageName()));
            }
            getContext().startActivity(intent);
        });
        return view;
    }

}
