package com.joshrap.liteweight.Fragments;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.joshrap.liteweight.*;
import com.joshrap.liteweight.Globals.Variables;

public class AboutFragment extends Fragment {
    private View view;
    private String version;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_about, container, false);
        ((MainActivity) getActivity()).updateToolbarTitle(Variables.ABOUT_TITLE);
        try {
            version = getContext().getPackageManager()
                    .getPackageInfo(getContext().getPackageName(), 0).versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (version != null) {
            TextView versionTV = view.findViewById(R.id.version_number);
            String displayText = getResources().getString(R.string.version_number) + " " + version;
            versionTV.setText(displayText);
        }
        TextView acknowledgementsTV = view.findViewById(R.id.acknowledgments);
        acknowledgementsTV.setMovementMethod(LinkMovementMethod.getInstance()); // makes links clickable
        return view;
    }

}
