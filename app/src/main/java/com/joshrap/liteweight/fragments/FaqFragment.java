package com.joshrap.liteweight.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.joshrap.liteweight.R;
import com.joshrap.liteweight.activities.WorkoutActivity;
import com.joshrap.liteweight.imports.Variables;

public class FaqFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_faq, container, false);
        ((WorkoutActivity) getActivity()).toggleBackButton(true);
        ((WorkoutActivity) getActivity()).updateToolbarTitle(Variables.FAQ_TITLE);
        TextView faq_0_TV = view.findViewById(R.id.faq_0_tv);
        TextView faq_0 = view.findViewById(R.id.faq_0);
        faq_0_TV.setOnClickListener(view1 -> {
            if (faq_0.getVisibility() == View.GONE) {
                faq_0_TV.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.up_icon, 0);
                faq_0.setVisibility(View.VISIBLE);
            } else {
                faq_0_TV.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.down_icon, 0);
                faq_0.setVisibility(View.GONE);
            }
        });

        TextView faq_1_TV = view.findViewById(R.id.faq_1_tv);
        TextView faq_1 = view.findViewById(R.id.faq_1);
        faq_1_TV.setOnClickListener(view1 -> {
            if (faq_1.getVisibility() == View.GONE) {
                faq_1_TV.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.up_icon, 0);
                faq_1.setVisibility(View.VISIBLE);
            } else {
                faq_1_TV.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.down_icon, 0);
                faq_1.setVisibility(View.GONE);
            }
        });

        TextView faq_2_TV = view.findViewById(R.id.faq_2_tv);
        TextView faq_2 = view.findViewById(R.id.faq_2);
        faq_2_TV.setOnClickListener(view1 -> {
            if (faq_2.getVisibility() == View.GONE) {
                faq_2_TV.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.up_icon, 0);
                faq_2.setVisibility(View.VISIBLE);
            } else {
                faq_2_TV.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.down_icon, 0);
                faq_2.setVisibility(View.GONE);
            }
        });

        TextView faq_3_TV = view.findViewById(R.id.faq_3_tv);
        TextView faq_3 = view.findViewById(R.id.faq_3);
        faq_3_TV.setOnClickListener(view1 -> {
            if (faq_3.getVisibility() == View.GONE) {
                faq_3_TV.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.up_icon, 0);
                faq_3.setVisibility(View.VISIBLE);
            } else {
                faq_3_TV.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.down_icon, 0);
                faq_3.setVisibility(View.GONE);
            }
        });

        TextView faq_4_TV = view.findViewById(R.id.faq_4_tv);
        TextView faq_4 = view.findViewById(R.id.faq_4);
        faq_4_TV.setOnClickListener(view1 -> {
            if (faq_4.getVisibility() == View.GONE) {
                faq_4_TV.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.up_icon, 0);
                faq_4.setVisibility(View.VISIBLE);
            } else {
                faq_4_TV.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.down_icon, 0);
                faq_4.setVisibility(View.GONE);
            }
        });
        return view;
    }

}
