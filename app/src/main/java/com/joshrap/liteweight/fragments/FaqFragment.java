package com.joshrap.liteweight.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.joshrap.liteweight.R;
import com.joshrap.liteweight.activities.WorkoutActivity;
import com.joshrap.liteweight.imports.Variables;

public class FaqFragment extends Fragment {

    private int rotationAngle0, rotationAngle1, rotationAngle2, rotationAngle3, rotationAngle4;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_faq, container, false);
        ((WorkoutActivity) getActivity()).toggleBackButton(true);
        ((WorkoutActivity) getActivity()).updateToolbarTitle(Variables.FAQ_TITLE);

        TextView faq_0_TV = view.findViewById(R.id.faq_0_tv);
        TextView faq_0 = view.findViewById(R.id.faq_0);
        ImageButton faq_0_icon = view.findViewById(R.id.faq_0_icon);
        View.OnClickListener faq0Clicked = v -> {
            boolean visible = faq_0.getVisibility() == View.VISIBLE;
            faq_0.setVisibility(visible ? View.GONE : View.VISIBLE);
            rotationAngle0 = rotationAngle0 == 0 ? 180 : 0;
            faq_0_icon.animate().rotation(rotationAngle0).setDuration(500).start();
        };
        faq_0_TV.setOnClickListener(faq0Clicked);
        faq_0_icon.setOnClickListener(faq0Clicked);

        TextView faq_1_TV = view.findViewById(R.id.faq_1_tv);
        TextView faq_1 = view.findViewById(R.id.faq_1);
        ImageButton faq_1_icon = view.findViewById(R.id.faq_1_icon);
        View.OnClickListener faq1Clicked = v -> {
            boolean visible = faq_1.getVisibility() == View.VISIBLE;
            faq_1.setVisibility(visible ? View.GONE : View.VISIBLE);
            rotationAngle1 = rotationAngle1 == 0 ? 180 : 0;
            faq_1_icon.animate().rotation(rotationAngle1).setDuration(500).start();
        };
        faq_1_TV.setOnClickListener(faq1Clicked);
        faq_1_icon.setOnClickListener(faq1Clicked);

        TextView faq_2_TV = view.findViewById(R.id.faq_2_tv);
        TextView faq_2 = view.findViewById(R.id.faq_2);
        ImageButton faq_2_icon = view.findViewById(R.id.faq_2_icon);
        View.OnClickListener faq2Clicked = v -> {
            boolean visible = faq_2.getVisibility() == View.VISIBLE;
            faq_2.setVisibility(visible ? View.GONE : View.VISIBLE);
            rotationAngle2 = rotationAngle2 == 0 ? 180 : 0;
            faq_2_icon.animate().rotation(rotationAngle2).setDuration(500).start();
        };
        faq_2_TV.setOnClickListener(faq2Clicked);
        faq_2_icon.setOnClickListener(faq2Clicked);

        TextView faq_3_TV = view.findViewById(R.id.faq_3_tv);
        TextView faq_3 = view.findViewById(R.id.faq_3);
        ImageButton faq_3_icon = view.findViewById(R.id.faq_3_icon);
        View.OnClickListener faq3Clicked = v -> {
            boolean visible = faq_3.getVisibility() == View.VISIBLE;
            faq_3.setVisibility(visible ? View.GONE : View.VISIBLE);
            rotationAngle3 = rotationAngle3 == 0 ? 180 : 0;
            faq_3_icon.animate().rotation(rotationAngle3).setDuration(500).start();
        };
        faq_3_TV.setOnClickListener(faq3Clicked);
        faq_3_icon.setOnClickListener(faq3Clicked);

        TextView faq_4_TV = view.findViewById(R.id.faq_4_tv);
        TextView faq_4 = view.findViewById(R.id.faq_4);
        ImageButton faq_4_icon = view.findViewById(R.id.faq_4_icon);
        View.OnClickListener faq4Clicked = v -> {
            boolean visible = faq_4.getVisibility() == View.VISIBLE;
            faq_4.setVisibility(visible ? View.GONE : View.VISIBLE);
            rotationAngle4 = rotationAngle4 == 0 ? 180 : 0;
            faq_4_icon.animate().rotation(rotationAngle4).setDuration(500).start();
        };
        faq_4_TV.setOnClickListener(faq4Clicked);
        faq_4_icon.setOnClickListener(faq4Clicked);

        return view;
    }

}
