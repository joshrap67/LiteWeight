package com.joshrap.liteweight.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.joshrap.liteweight.*;
import com.joshrap.liteweight.activities.WorkoutActivity;
import com.joshrap.liteweight.imports.Variables;

public class AboutFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        ((WorkoutActivity) getActivity()).updateToolbarTitle(Variables.ABOUT_TITLE);
        String version = null;
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
