package com.example.workoutmadness;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class NewWorkoutFragment extends Fragment {
    private boolean modified = false;
    private EditText workoutNameInput, numWeeks, numDays;
    private TextView numWeeksTV, numDaysTV;
    private Button nextButton;
    private View view;
    private String lastName, lastWeek, lastDay;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_new,container,false);
        initViews();
        return view;
    }
    public void initViews(){
        workoutNameInput = view.findViewById(R.id.workoutNameInput);
        numWeeks = view.findViewById(R.id.weekInput);
        numDays = view.findViewById(R.id.dayInput);
        numWeeksTV = view.findViewById(R.id.weekTV);
        numDaysTV = view.findViewById(R.id.dayTV);
        nextButton = view.findViewById(R.id.nextButton);
        // TODO hide keyboard when clicking elsewhere
        // TODO check if workout name already exists
        // TODO check if user deleted the name, same with number of weeks and days at the end
        workoutNameInput.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent keyevent) {
                if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    if(!workoutNameInput.getText().toString().equalsIgnoreCase("")){
                        lastName = workoutNameInput.getText().toString();
                        numWeeks.setVisibility(View.VISIBLE);
                        numWeeksTV.setVisibility(View.VISIBLE);
                        return true;
                    }
                    else{
                        workoutNameInput.setText(lastName);
                        Toast.makeText(getActivity(), "Enter valid name", Toast.LENGTH_LONG).show();
                    }
                }
                return false;
            }
        });

        workoutNameInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        numWeeks.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent keyevent) {
                if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    if(!numWeeks.getText().toString().equalsIgnoreCase("")){
                        boolean validWeek = checkValidWeek(numWeeks.getText().toString());
                        if(validWeek){
                            lastWeek = numWeeks.getText().toString();
                            numDays.setVisibility(View.VISIBLE);
                            numDaysTV.setVisibility(View.VISIBLE);
                            return true;
                        }
                        else{
                            numWeeks.setText(lastWeek);
                            Toast.makeText(getActivity(), "Enter value between 1-8", Toast.LENGTH_LONG).show();
                        }
                    }
                }
                return false;
            }
        });

        numDays.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent keyevent) {
                if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    if(!numDays.getText().toString().equalsIgnoreCase("")){
                        boolean validDay = checkValidDay(numDays.getText().toString());
                        if(validDay){
                            lastDay = numDays.getText().toString();
                            nextButton.setVisibility(View.VISIBLE);
                            return true;
                        }
                        else{
                            numDays.setText(lastDay);
                            Toast.makeText(getActivity(), "Enter value between 1-7", Toast.LENGTH_LONG).show();
                        }
                    }
                }
                return false;
            }
        });
    }

    public boolean checkValidWeek(String aWeek){
        int week = Integer.parseInt(aWeek);
        if(week>0&&week<9){
            return true;
        }
        return false;
    }

    public void hideKeyboard(View view) {
        /*
            Found on SO
         */
        InputMethodManager inputMethodManager =(InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public boolean checkValidDay(String aDay){
        int day = Integer.parseInt(aDay);
        if(day>0&&day<8){
            return true;
        }
        return false;
    }
    public void createWorkout(){

    }

    public boolean checkModified(){
        return modified;
    }
}
