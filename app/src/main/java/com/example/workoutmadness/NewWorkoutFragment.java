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
import android.widget.Toast;

public class NewWorkoutFragment extends Fragment {
    private boolean modified = false;
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
        workoutNameInput.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent keyevent) {
                if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    boolean validName = checkValidName(workoutNameInput.getText().toString());
                    if (validName) {
                        modified=true;
                        return true;
                    } else {
                        displayNameError();
                    }
                }
                return false;
            }
        });

        numWeeksInput.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent keyevent) {
                if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    boolean validWeek = checkValidWeek(numWeeksInput.getText().toString());
                    if (validWeek) {
                        modified=true;
                        return true;
                    }
                    else {
                        displayWeeksError();
                    }
                }
                return false;
            }
        });
        numDaysInput.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent keyevent) {
                if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    boolean validDay = checkValidDay(numDaysInput.getText().toString());
                    if (validDay) {
                        modified=true;
                        return true;
                    }
                    else {
                        displayDaysError();
                    }
                }
                return false;
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentName=workoutNameInput.getText().toString();
                String currentWeeks=numWeeksInput.getText().toString();
                String currentDays=numDaysInput.getText().toString();
                boolean validName = checkValidName(currentName);
                boolean validWeeks=checkValidWeek(currentWeeks);
                boolean validDays=checkValidDay(currentDays);

                if(validName&&validWeeks&&validDays){
                    finalName=currentName.trim();
                    finalWeekNum=Integer.parseInt(currentWeeks);
                    finalDayNum=Integer.parseInt(currentDays);
                    createWorkout();
                }
                else{
                    Toast.makeText(getContext(), "Ensure all fields above are valid!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public boolean checkValidName(String aName){
        if((aName.length()>0)&&(aName.length()<500)){
            return true;
        }
        // TODO check if workout name already exists
        displayNameError();
        return false;
    }

    public boolean checkValidWeek(String aWeek) {
        if(aWeek.length()==0){
            displayWeeksError();
            return false;
        }
        int week = Integer.parseInt(aWeek);
        if (week > 0 && week < 9) {
            return true;
        }
        displayWeeksError();
        return false;
    }

    public boolean checkValidDay(String aDay) {
        if(aDay.length()==0){
            displayDaysError();
            return false;
        }
        int day = Integer.parseInt(aDay);
        if (day > 0 && day < 8) {
            return true;
        }
        displayDaysError();
        return false;
    }

    public void displayNameError(){
        workoutNameInput.setError("Enter a valid name");
        workoutNameInput.setText("");
    }

    public void displayWeeksError(){
        numWeeksInput.setError("Enter value between 1-8!");
        numWeeksInput.setText("");
    }

    public void displayDaysError(){
        numDaysInput.setError("Enter value between 1-7!");
        numDaysInput.setText("");
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
