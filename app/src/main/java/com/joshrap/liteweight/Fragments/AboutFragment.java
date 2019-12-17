package com.joshrap.liteweight.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.joshrap.liteweight.*;
import com.joshrap.liteweight.Globals.Variables;

public class AboutFragment extends Fragment {
    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_about, container, false);
        ((MainActivity) getActivity()).updateToolbarTitle(Variables.ABOUT_TITLE);
        return view;
    }

}
