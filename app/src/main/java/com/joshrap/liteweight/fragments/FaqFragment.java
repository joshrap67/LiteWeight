package com.joshrap.liteweight.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import com.joshrap.liteweight.R;
import com.joshrap.liteweight.activities.WorkoutActivity;
import com.joshrap.liteweight.imports.Variables;

public class FaqFragment extends Fragment {

    private int rotationAngle0, rotationAngle1, rotationAngle2, rotationAngle3, rotationAngle4;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        View view = inflater.inflate(R.layout.fragment_faq, container, false);
        ((WorkoutActivity) getActivity()).toggleBackButton(true);
        ((WorkoutActivity) getActivity()).updateToolbarTitle(Variables.FAQ_TITLE);

        RelativeLayout faq0Layout = view.findViewById(R.id.faq_0_container);
        TextView faq0 = view.findViewById(R.id.faq_0_tv);
        ImageButton faq0Icon = view.findViewById(R.id.faq_0_icon_btn);
        View.OnClickListener faq0Clicked = v -> {
            boolean visible = faq0.getVisibility() == View.VISIBLE;
            faq0.setVisibility(visible ? View.GONE : View.VISIBLE);
            rotationAngle0 = rotationAngle0 == 0 ? 180 : 0;
            faq0Icon.animate().rotation(rotationAngle0).setDuration(400).start();
            TransitionManager.beginDelayedTransition(container, new AutoTransition());
        };
        faq0Layout.setOnClickListener(faq0Clicked);
        faq0Icon.setOnClickListener(faq0Clicked);

        RelativeLayout faq1Layout = view.findViewById(R.id.faq_1_container);
        TextView faq1 = view.findViewById(R.id.faq_1_tv);
        ImageButton faq1Icon = view.findViewById(R.id.faq_1_icon_btn);
        View.OnClickListener faq1Clicked = v -> {
            boolean visible = faq1.getVisibility() == View.VISIBLE;
            faq1.setVisibility(visible ? View.GONE : View.VISIBLE);
            rotationAngle1 = rotationAngle1 == 0 ? 180 : 0;
            faq1Icon.animate().rotation(rotationAngle1).setDuration(400).start();
            TransitionManager.beginDelayedTransition(container, new AutoTransition());
        };
        faq1Layout.setOnClickListener(faq1Clicked);
        faq1Icon.setOnClickListener(faq1Clicked);

        RelativeLayout faq2Layout = view.findViewById(R.id.faq_2_container);
        TextView faq2 = view.findViewById(R.id.faq_2_tv);
        ImageButton faq2Icon = view.findViewById(R.id.faq_2_icon_btn);
        View.OnClickListener faq2Clicked = v -> {
            boolean visible = faq2.getVisibility() == View.VISIBLE;
            faq2.setVisibility(visible ? View.GONE : View.VISIBLE);
            rotationAngle2 = rotationAngle2 == 0 ? 180 : 0;
            faq2Icon.animate().rotation(rotationAngle2).setDuration(400).start();
            TransitionManager.beginDelayedTransition(container, new AutoTransition());
        };
        faq2Layout.setOnClickListener(faq2Clicked);
        faq2Icon.setOnClickListener(faq2Clicked);

        RelativeLayout faq3Layout = view.findViewById(R.id.faq_3_container);
        TextView faq3 = view.findViewById(R.id.faq_3_tv);
        ImageButton faq3Icon = view.findViewById(R.id.faq_3_icon_btn);
        View.OnClickListener faq3Clicked = v -> {
            boolean visible = faq3.getVisibility() == View.VISIBLE;
            faq3.setVisibility(visible ? View.GONE : View.VISIBLE);
            rotationAngle3 = rotationAngle3 == 0 ? 180 : 0;
            faq3Icon.animate().rotation(rotationAngle3).setDuration(400).start();
            TransitionManager.beginDelayedTransition(container, new AutoTransition());
        };
        faq3Layout.setOnClickListener(faq3Clicked);
        faq3Icon.setOnClickListener(faq3Clicked);

        RelativeLayout faq4Layout = view.findViewById(R.id.faq_4_container);
        TextView faq4 = view.findViewById(R.id.faq_4_tv);
        ImageButton faq4Icon = view.findViewById(R.id.faq_4_icon_btn);
        View.OnClickListener faq4Clicked = v -> {
            boolean visible = faq4.getVisibility() == View.VISIBLE;
            faq4.setVisibility(visible ? View.GONE : View.VISIBLE);
            rotationAngle4 = rotationAngle4 == 0 ? 180 : 0;
            faq4Icon.animate().rotation(rotationAngle4).setDuration(400).start();
            TransitionManager.beginDelayedTransition(container, new AutoTransition());
        };
        faq4Layout.setOnClickListener(faq4Clicked);
        faq4Icon.setOnClickListener(faq4Clicked);

        return view;
    }

}
