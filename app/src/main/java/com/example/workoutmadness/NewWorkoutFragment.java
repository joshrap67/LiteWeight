package com.example.workoutmadness;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class NewWorkoutFragment extends Fragment {
    private boolean modified = false;
    private EditText workoutName, numWeeks, numDays;
    private Button nextButton;
    private View view;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_new,container,false);
        initInputs();
        return view;
    }
    public void initInputs(){
        workoutName = view.findViewById(R.id.workoutNameInput);
        numWeeks = view.findViewById(R.id.weekInput);
        numDays = view.findViewById(R.id.dayInput);
        workoutName.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
//                numWeeks.setVisibility(View.VISIBLE);
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                numWeeks.setVisibility(View.VISIBLE);
            }
        });
    }
    public void createWorkout(){

    }

    public boolean checkModified(){
        return modified;
    }
}
