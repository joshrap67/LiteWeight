package com.example.workoutmadness;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class UserSettingsFragment extends Fragment {
    private View view;
    private boolean modifed;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_user_settings,container,false);
        ((MainActivity)getActivity()).updateToolbarTitle("Settings");
        return view;
    }

    public boolean isModified(){
        return modifed;
    }
}
