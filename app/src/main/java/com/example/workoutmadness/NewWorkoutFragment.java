package com.example.workoutmadness;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class NewWorkoutFragment extends Fragment {
    private boolean modified = false, firstDay, lastDay;
    private EditText workoutNameInput, numWeeksInput, numDaysInput;
    private Button previousDayBtn, nextDayBtn;
    private int finalDayNum, finalWeekNum;
    private String finalName;
    private View view, popupView;
    private AlertDialog alertDialog;
    private ArrayList<String> checkedExercises = new ArrayList<>();
    private TableLayout pickExerciseTable, displayedExercisesTable;
    private int currentDayIndex, maxDayIndex;
    private TextView dayTitle;
    private ViewGroup fragmentContainer;
    private HashMap<Integer, ArrayList<String>> exercises = new HashMap<>();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentContainer=container;
        view = inflater.inflate(R.layout.fragment_new, container, false);
        String toolbarName = "Workout Creator";
        ((MainActivity) getActivity()).updateToolbarTitle(toolbarName);
        currentDayIndex=0;
        initViews();
        return view;
    }

    public void initViews() {
        workoutNameInput = view.findViewById(R.id.workoutNameInput);
        numWeeksInput = view.findViewById(R.id.weekInput);
        numDaysInput = view.findViewById(R.id.dayInput);
        Button nextButton = view.findViewById(R.id.nextButton);
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
                    maxDayIndex=(finalWeekNum*finalDayNum)-1;
                    createWorkout();
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
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View createWorkoutView = inflater.inflate(R.layout.create_workout, fragmentContainer,false);
        ViewGroup rootView = (ViewGroup) getView();
        rootView.removeAllViews();
        rootView.addView(createWorkoutView);
        displayedExercisesTable = createWorkoutView.findViewById(R.id.main_table);
        dayTitle = createWorkoutView.findViewById(R.id.dayTextView);
        updateDayTitle();
        for(int i=0;i<=maxDayIndex;i++){
            exercises.put(i, new ArrayList<String>());
        }
        setButtons(createWorkoutView);
    }

    public void updateDayTitle(){
        /*
            Updates the day title in a cyclic fashion given from user input
         */
        int weekNum = (currentDayIndex/finalDayNum)+1;
        int dayNum = (currentDayIndex%finalDayNum)+1;
        String displayTitle="W"+weekNum+":D"+dayNum;
        dayTitle.setText(displayTitle);
    }

    public void setButtons(View _view){
        final Button addExercises = _view.findViewById(R.id.add_exercises);
        addExercises.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupExercises();
            }
        });
        firstDay=true;
        previousDayBtn=_view.findViewById(R.id.previousDayButton);
        nextDayBtn=_view.findViewById(R.id.nextDayButton);
        previousDayBtn.setVisibility(View.INVISIBLE);
        if(maxDayIndex==0){
            // in case some jabroni only wants to workout one day total
            nextDayBtn.setText("FINISH");
        }
        previousDayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentDayIndex>0){
                    displayedExercisesTable.removeAllViews();
                    currentDayIndex--;
                }
                if(lastDay){
                    lastDay=false;
                    nextDayBtn.setText("NEXT");
                }

                if(currentDayIndex==0){
                    previousDayBtn.setVisibility(View.INVISIBLE);
                    firstDay=true;
                }
                addExercises();
                updateDayTitle();
            }
        });
        nextDayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentDayIndex<maxDayIndex) {
                    displayedExercisesTable.removeAllViews();
                    currentDayIndex++;
                    if(firstDay){
                        firstDay=false;
                        previousDayBtn.setVisibility(View.VISIBLE);
                    }
                    if(currentDayIndex==maxDayIndex){
                        lastDay=true;
                        nextDayBtn.setText("FINISH");
                    }
                    addExercises();
                    updateDayTitle();
                }
                else{
                    // on the last day so check if every day has at least one exercise in it before writing to file
                    boolean ready = true;
                    for(int i=0;i<exercises.size();i++){
                        if(exercises.get(i)==null){
                            ready =false;
                        }
                        else if(exercises.get(i).isEmpty()){
                            ready=false;
                        }
                    }
                    if(ready){
                        //todo write to file and exit
                    }
                    else{
                        Toast.makeText(getContext(),"Ensure each day has at least one exercise!",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    public void addExercises(){
        Collections.sort(exercises.get(currentDayIndex));
        int count = 0;
        for (final String exercise : exercises.get(currentDayIndex)){
            final TableRow row = new TableRow(getActivity());
            row.setBackgroundResource(R.drawable.border);
            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
            row.setLayoutParams(lp);
            TextView name = new TextView(getContext());
            name.setText(exercise);
            name.setTextSize(30);
            row.addView(name);
            ImageView deleteRowIcon = new ImageView(getContext());
            deleteRowIcon.setImageResource(R.drawable.delete_icon);
            deleteRowIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    displayedExercisesTable.removeView(row);
                    exercises.get(currentDayIndex).remove(exercise);
                }
            });
            row.addView(deleteRowIcon);
            displayedExercisesTable.addView(row,count);
            count++;
        }
    }

    public void popupExercises(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialog = alertDialogBuilder.create();
        popupView = getLayoutInflater().inflate(R.layout.exercise_popup, null);
        Spinner clusterSpinner=popupView.findViewById(R.id.clusterSpinner);
        pickExerciseTable = popupView.findViewById(R.id.main_table);
        clusterSpinner.setOnItemSelectedListener(new SpinnerListener());
        alertDialog.setView(popupView);
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
        ImageView done = popupView.findViewById(R.id.imageView);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // done adding
                if(checkedExercises.isEmpty()){
                    alertDialog.dismiss();
                    return;
                }
                for(String exercise : checkedExercises){
                    exercises.get(currentDayIndex).add(exercise);
                }
                checkedExercises.clear();
                displayedExercisesTable.removeAllViews();
                addExercises();
                alertDialog.dismiss();
            }
        });
    }

    public void updateListView(String exerciseCluster){
        String[] exerciseValues;
        switch (exerciseCluster){
            case "Chest":
                exerciseValues=getContext().getResources().getStringArray(R.array.chest_day);
                break;
            case "Legs":
                exerciseValues=getContext().getResources().getStringArray(R.array.leg_day);
                break;
            default:
                exerciseValues = new String[] {"somebody","once","told","me"};
        }
        ArrayList<String> sortedExercises = new ArrayList<>();
        for(int i =0;i<exerciseValues.length;i++){
            sortedExercises.add(exerciseValues[i]);
        }
        Collections.sort(sortedExercises);
        for(int i=0;i<exerciseValues.length;i++){
            TableRow row = new TableRow(getActivity());
            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
            row.setLayoutParams(lp);
            final CheckBox exercise = new CheckBox(getActivity());
            String exerciseName = sortedExercises.get(i);
            exercise.setText(exerciseName);
            if(checkedExercises.contains(exerciseName) || exercises.get(currentDayIndex).contains(exerciseName)){
                exercise.setChecked(true);
            }
            exercise.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!checkedExercises.contains(exercise.getText().toString())&&
                    !exercises.get(currentDayIndex).contains(exercise.getText().toString())){
                        // prevents exercise from being added twice
                        checkedExercises.add(exercise.getText().toString());
                    }
                    else{
                        checkedExercises.remove(exercise.getText().toString());
                    }

                }
            });
            row.addView(exercise);
            pickExerciseTable.addView(row,i);
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

    private class SpinnerListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            String selectedExerciseCluster = parent.getItemAtPosition(pos).toString();
            pickExerciseTable.removeAllViews();
            updateListView(selectedExerciseCluster);
        }

        public void onNothingSelected(AdapterView parent) {
        }
    }
}
