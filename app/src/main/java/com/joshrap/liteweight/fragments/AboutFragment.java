package com.joshrap.liteweight.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.joshrap.liteweight.*;
import com.joshrap.liteweight.activities.MainActivity;
import com.joshrap.liteweight.imports.BackendConfig;
import com.joshrap.liteweight.imports.Variables;
import com.joshrap.liteweight.injection.Injector;

public class AboutFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        View view = inflater.inflate(R.layout.fragment_about, container, false);
        ((MainActivity) getActivity()).toggleBackButton(false);
        ((MainActivity) getActivity()).updateToolbarTitle(Variables.ABOUT_TITLE);
        String version = null;
        try {
            version = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0).versionName;
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        if (version != null) {
            TextView versionTV = view.findViewById(R.id.version_number_tv);
            String displayText = getResources().getString(R.string.version_number) + " " + version;
            versionTV.setText(displayText);
        }
        TextView faqTV = view.findViewById(R.id.faq_tv);
        faqTV.setOnClickListener(view1 -> ((MainActivity) getActivity()).goToFaq());

        TextView termsConditionsTV = view.findViewById(R.id.terms_conditions_tv);
        termsConditionsTV.setOnClickListener(view1 -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(BackendConfig.termsConditionsUrl));
            startActivity(browserIntent);
        });
        TextView privacyPolicyTV = view.findViewById(R.id.privacy_policy_tv);
        privacyPolicyTV.setOnClickListener(view1 -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(BackendConfig.privacyPolicyUrl));
            startActivity(browserIntent);
        });

        return view;
    }
}
