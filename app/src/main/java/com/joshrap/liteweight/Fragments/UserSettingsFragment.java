package com.joshrap.liteweight.Fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.support.v7.widget.SwitchCompat;

import com.joshrap.liteweight.*;
import com.joshrap.liteweight.Globals.Variables;

public class UserSettingsFragment extends Fragment {
    private SharedPreferences.Editor editor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((MainActivity) getActivity()).updateToolbarTitle(Variables.SETTINGS_TITLE);
        View view = inflater.inflate(R.layout.fragment_user_settings, container, false);
        SwitchCompat videoSwitch = view.findViewById(R.id.video_switch);
        SwitchCompat timerSwitch = view.findViewById(R.id.timer_switch);
        SwitchCompat unitSwitch = view.findViewById(R.id.metric_switch);
        SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences(Variables.SHARED_PREF_NAME, 0);
        editor = pref.edit();
        timerSwitch.setChecked(pref.getBoolean(Variables.TIMER_KEY, true));
        timerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean(Variables.TIMER_KEY, isChecked);
                editor.apply();
            }
        });
        videoSwitch.setChecked(pref.getBoolean(Variables.VIDEO_KEY, true));
        videoSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean(Variables.VIDEO_KEY, isChecked);
                editor.apply();
            }
        });
        unitSwitch.setChecked(pref.getBoolean(Variables.UNIT_KEY, false));
        unitSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean(Variables.UNIT_KEY, isChecked);
                editor.apply();
            }
        });
        return view;
    }

}
