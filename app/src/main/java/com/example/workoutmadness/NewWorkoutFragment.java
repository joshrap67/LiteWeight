package com.example.workoutmadness;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class NewWorkoutFragment extends Fragment {
    private boolean modified = false, dayValid=false, weekValid=false, nameValid=false;
    private EditText workoutNameInput, numWeeksInput, numDaysInput;
    private Button nextButton;
    private View view;
    private int finalDayNum, finalWeekNum;
    private String finalName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_new, container, false);
        String toolbarName = "Workout Creator";
        ((MainActivity) getActivity()).updateToolbarTitle(toolbarName);
        initViews();
        return view;
    }

    public void initViews() {
        workoutNameInput = view.findViewById(R.id.workoutNameInput);
        numWeeksInput = view.findViewById(R.id.weekInput);
        numDaysInput = view.findViewById(R.id.dayInput);
        nextButton = view.findViewById(R.id.nextButton);
        // TODO hide keyboard when clicking elsewhere
        // TODO check if workout name already exists
        workoutNameInput.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent keyevent) {
                if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    if (!workoutNameInput.getText().toString().equalsIgnoreCase("")) {
                        modified = true;
                        nameValid=true;
                        return true;
                    } else {
                        nameValid=false;
                        workoutNameInput.setError("Enter valid name!");
                    }
                }
                return false;
            }
        });

        numWeeksInput.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent keyevent) {
                if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    if (!numWeeksInput.getText().toString().equalsIgnoreCase("")) {
                        boolean validWeek = checkValidWeek(numWeeksInput.getText().toString());
                        if (validWeek) {
                            modified=true;
                            weekValid=true;
                            return true;
                        } else {
                            weekValid=false;
                            numWeeksInput.setError("Enter value between 1-8!");
                            numWeeksInput.setText("");
                        }
                    }
                    else{
                        weekValid=false;
                        numWeeksInput.setError("Enter value between 1-8!");
                        numWeeksInput.setText("");
                    }
                }
                return false;
            }
        });

        numDaysInput.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent keyevent) {
                if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    if (!numDaysInput.getText().toString().equalsIgnoreCase("")) {
                        boolean validDay = checkValidDay(numDaysInput.getText().toString());
                        if (validDay) {
                            modified=true;
                            dayValid=true;
                            return true;
                        } else {
                            dayValid=false;
                            numDaysInput.setError("Enter value between 1-7!");
                            numDaysInput.setText("");
                        }
                    }
                    else{
                        dayValid=false;
                        numDaysInput.setError("Enter value between 1-7!");
                        numDaysInput.setText("");
                    }
                }
                return false;
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dayValid && weekValid && nameValid){
                    finalName=workoutNameInput.getText().toString().trim();
                    finalDayNum=Integer.parseInt(numDaysInput.getText().toString());
                    finalWeekNum=Integer.parseInt(numWeeksInput.getText().toString());
                    createWorkout();
                }
                else{
                    Toast.makeText(getContext(), "Ensure all fields above are valid!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public boolean checkValidWeek(String aWeek) {
        int week = Integer.parseInt(aWeek);
        if (week > 0 && week < 9) {
            return true;
        }
        return false;
    }

    public boolean checkValidDay(String aDay) {
        int day = Integer.parseInt(aDay);
        if (day > 0 && day < 8) {
            return true;
        }
        return false;
    }

    public void createWorkout() {
        //TODO change the view
        for(int i=0;i<finalWeekNum;i++){
            for(int j=0;j<finalDayNum;j++){
                String displayedName = "W"+(i+1)+":D"+(j+1);
                Toast.makeText(getContext(), displayedName, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean isModified() {
        /*
            Is used to check if the user has made any at all changes to their workout. If so, appropriate
            action (namely altering the text file) must be taken.
         */
        return modified;
    }

    public void setModified(boolean status){
        modified=status;
    }
}
