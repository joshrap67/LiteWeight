package com.example.workoutmadness.Fragments;

import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.workoutmadness.*;
import com.example.workoutmadness.Database.Entities.*;
import com.example.workoutmadness.Database.ViewModels.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class NewWorkoutFragment extends Fragment {
    private boolean modified = false, firstDay, lastDay, firstWorkout = false;
    private EditText workoutNameInput, numWeeksInput, numDaysInput;
    private Button previousDayBtn, nextDayBtn;
    private int finalDayNum, finalWeekNum,currentDayIndex, maxDayIndex;
    private String finalName;
    private View view, popupView;
    private AlertDialog alertDialog;
    private TableLayout pickExerciseTable, displayedExercisesTable;
    private TextView dayTitle;
    private ViewGroup fragmentContainer;
    private WorkoutViewModel workoutModel;
    private MetaViewModel metaViewModel;
    private HashMap<Integer, ArrayList<String>> exercises = new HashMap<>();
    private ArrayList<String> checkedExercises = new ArrayList<>();
    private HashMap<String,ArrayList<String>> defaultExercises = new HashMap<>();
    private HashMap<String,ArrayList<String>> customExercises = new HashMap<>();
    private ArrayList<String> focusList = new ArrayList<>();
    private Validator validator;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentContainer = container;
        view = inflater.inflate(R.layout.fragment_new, container, false);
        ((MainActivity) getActivity()).updateToolbarTitle("Workout Creator");
        currentDayIndex = 0;
        validator= new Validator(getActivity());
        /*
            Setup view models
         */
        workoutModel = ViewModelProviders.of(getActivity()).get(WorkoutViewModel.class);
        metaViewModel = ViewModelProviders.of(getActivity()).get(MetaViewModel.class);
        GetAllWorkoutsTask task = new GetAllWorkoutsTask();
        task.execute();
        return view;
    }

    private class GetAllWorkoutsTask extends AsyncTask<Void, Void, List<MetaEntity>> {

        @Override
        protected List<MetaEntity> doInBackground(Void... voids) {
            // get the current workout from the database
            return metaViewModel.getAllMetadata();
        }

        @Override
        protected void onPostExecute(List<MetaEntity> result) {
            if(result!=null) {
                for(MetaEntity entity : result){
                    Log.d("TAG","Meta entity: "+entity.toString());
                    // TODO put in list to make sure same name isn't picked
                }
            }
            else{
                // no workouts found
                firstWorkout = true;
                Log.d("TAG","Get all metadata result was null!");
            }
            initViews();
        }
    }

    public void initViews() {
        /*
            Initialize the edit texts and ensure that each validates the input correctly.
         */
        workoutNameInput = view.findViewById(R.id.workoutNameInput);
        numWeeksInput = view.findViewById(R.id.weekInput);
        numDaysInput = view.findViewById(R.id.dayInput);
        Button nextButton = view.findViewById(R.id.nextButton);
        // TODO hide keyboard when clicking elsewhere
        workoutNameInput.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent keyevent) {
                if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    String errorMsg = validator.checkValidName(workoutNameInput.getText().toString());
                    if (errorMsg==null) {
                        modified = true;
                        return true;
                    }
                    else{
                        displayErrorMessage("Name",errorMsg);
                    }
                }
                return false;
            }
        });

        numWeeksInput.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent keyevent) {
                if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    String errorMsg = validator.checkValidWeek(numWeeksInput.getText().toString());
                    if (errorMsg==null) {
                        modified=true;
                        return true;
                    }
                    else{
                        displayErrorMessage("Weeks",errorMsg);
                    }
                }
                return false;
            }
        });
        numDaysInput.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent keyevent) {
                if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    String errorMsg = validator.checkValidDay(numDaysInput.getText().toString());
                    if (errorMsg==null) {
                        modified = true;
                        return true;
                    }
                    else{
                        displayErrorMessage("Days",errorMsg);
                    }
                }
                return false;
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentName = workoutNameInput.getText().toString();
                String currentWeeks = numWeeksInput.getText().toString();
                String currentDays = numDaysInput.getText().toString();
                String nameError = validator.checkValidName(currentName);
                String weekError = validator.checkValidWeek(currentWeeks);
                String dayError = validator.checkValidDay(currentDays);

                if(nameError==null && weekError==null && dayError==null){
                    finalName = currentName.trim();
                    finalWeekNum = Integer.parseInt(currentWeeks);
                    finalDayNum = Integer.parseInt(currentDays);
                    maxDayIndex = (finalWeekNum*finalDayNum)-1;
                    createWorkout();
                }
            }
        });
    }

    public void displayErrorMessage(String editText, String msg){
        switch (editText){
            case "Name":
                workoutNameInput.setError(msg);
                workoutNameInput.setText("");
                break;
            case "Weeks":
                numWeeksInput.setError(msg);
                numWeeksInput.setText("");
                break;
            case "Days":
                numDaysInput.setError(msg);
                numDaysInput.setText("");
        }
    }

    public void createWorkout() {
        /*
            After parameters are validated, inflate the view that allows the user to start picking specific exercises for this
            new workout.
         */
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View createWorkoutView = inflater.inflate(R.layout.create_workout, fragmentContainer,false);
        ViewGroup rootView = (ViewGroup) getView();
        rootView.removeAllViews();
        rootView.addView(createWorkoutView);
        displayedExercisesTable = createWorkoutView.findViewById(R.id.main_table);
        dayTitle = createWorkoutView.findViewById(R.id.dayTextView);
        updateDayTitle();
        for(int i=0;i<= maxDayIndex;i++){
            // create the hash map that maps day numbers to lists of exercises
            exercises.put(i, new ArrayList<String>());
        }
        populateDefaultExercises();
        setButtons(createWorkoutView);
    }

    public void updateDayTitle(){
        /*
            Updates the day title in a cyclic fashion given from user input
         */
        int weekNum = (currentDayIndex / finalDayNum)+1;
        int dayNum = (currentDayIndex % finalDayNum)+1;
        String displayTitle="W"+weekNum+":D"+dayNum;
        dayTitle.setText(displayTitle);
    }

    public void setButtons(View _view){
        /*
            Setup buttons to allow for cycling through all the days of the workout. Logic is included to ensure that the user
            does not go beyond the bounds of the days specified by their input. Also ensure that each day has at
            least one exercise before allowing output to a file.
         */
        final Button addExercises = _view.findViewById(R.id.add_exercises);
        addExercises.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupExercises();
            }
        });
        firstDay = true;
        previousDayBtn = _view.findViewById(R.id.previousDayButton);
        nextDayBtn = _view.findViewById(R.id.nextDayButton);
        previousDayBtn.setVisibility(View.INVISIBLE);
        if(maxDayIndex == 0){
            // in case some jabroni only wants to workout one day total
            nextDayBtn.setText("Finish");
        }
        previousDayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentDayIndex > 0){
                    displayedExercisesTable.removeAllViews();
                    currentDayIndex--;
                }
                if(lastDay){
                    lastDay = false;
                    nextDayBtn.setText("Next");
                }

                if(currentDayIndex == 0){
                    previousDayBtn.setVisibility(View.INVISIBLE);
                    firstDay = true;
                }
                addExercisesToTable();
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
                        firstDay = false;
                        previousDayBtn.setVisibility(View.VISIBLE);
                    }
                    if(currentDayIndex == maxDayIndex){
                        lastDay = true;
                        nextDayBtn.setText("Finish");
                    }
                    addExercisesToTable();
                    updateDayTitle();
                }
                else{
                    // on the last day so check if every day has at least one exercise in it before writing to file
                    boolean ready = true;
                    for(int i =0;i<exercises.size();i++){
                        if(exercises.get(i) == null){
                            ready = false;
                        }
                        else if(exercises.get(i).isEmpty()){
                            ready = false;
                        }
                    }
                    if(ready){
//                        writeToFile();
                        writeToDatabase();
                        modified = false;
                        Toast.makeText(getContext(), "Workout successfully created!",Toast.LENGTH_SHORT).show();
                        // restart this fragment
                        Fragment frag;
                        frag = getFragmentManager().findFragmentByTag("NEW_WORKOUT");
                        final FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.detach(frag);
                        ft.attach(frag);
                        ft.commit();
                    }
                    else{
                        Toast.makeText(getContext(),"Ensure each day has at least one exercise!",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    public void writeToDatabase(){
        // write the metadata to the meta table
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        Date date = new Date();
        MetaEntity log = new MetaEntity(finalName,0,maxDayIndex,formatter.format(date),formatter.format(date),
                0,0,firstWorkout);
        metaViewModel.insert(log);
        // write to the workout table
        for(int i=0;i<=maxDayIndex;i++){
            // loop through all the days of the workouts
            for(String exercise : exercises.get(i)){
                // loop through exercises of a specific day
                WorkoutEntity workoutEntity = new WorkoutEntity(exercise,finalName,i,false);
                workoutModel.insert(workoutEntity);
            }
        }


    }

    public void writeToFile(){
        /*
            Writes all of the exercises the user has picked into the correct file format
         */
        BufferedWriter writer = null;
        File fhandle = new File(getContext().getExternalFilesDir(Variables.WORKOUT_DIRECTORY), finalName+Variables.WORKOUT_EXT);
        try{
            writer = new BufferedWriter(new FileWriter(fhandle,false));
            for(int i=0;i<=maxDayIndex;i++){
                // loop through all the days of the workouts
                int weekNum = (i / finalDayNum)+1;
                int dayNum = (i % finalDayNum)+1;
                String dayTitle=Variables.DAY_DELIM+"*"+"W"+weekNum+":D"+dayNum+"\n";
                writer.write(dayTitle);
                int size = exercises.get(i).size();
                // loop through exercises of a specific day
                for(int j=0;j<size;j++){
                    String exerciseName = exercises.get(i).get(j);
                    Exercise exercise = new Exercise(exerciseName);
                    String exerciseLine = exercise.getFormattedLine()+((i == maxDayIndex && j == (size-1))?"":"\n"); // avoid new line at end of file
                    writer.write(exerciseLine);
                }
            }
            writer.close();
            updateCurrentWorkoutLog(finalName+Variables.WORKOUT_EXT);
        }
        catch (Exception e){
            Log.d("ERROR","Error when trying to record to workout file!\n"+e);
        }
    }

    public void addExercisesToTable(){
        /*
            After user has selected exercises from the popup, add them to the table view and allow for them to be deleted.
         */
        Collections.sort(exercises.get(currentDayIndex));
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int count = 0;
        for (final String exercise : exercises.get(currentDayIndex)){
            final View row = inflater.inflate(R.layout.list_row,null);
            TextView exerciseName = row.findViewById(R.id.exercise_name);
            exerciseName.setText(exercise);
            ImageButton deleteIcon = row.findViewById(R.id.delete_exercise);
            deleteIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    displayedExercisesTable.removeView(row);
                    exercises.get(currentDayIndex).remove(exercise);
                }
            });
            displayedExercisesTable.addView(row,count);
            count++;
        }
    }

    public void popupExercises(){
        /*
            User has indicated they wish to add exercises to this specific day. Show a popup that provides a spinner
            that is programmed to list all exercises for a given exercise focus.
         */
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialog = alertDialogBuilder.create();
        popupView = getLayoutInflater().inflate(R.layout.exercise_popup, null);
        pickExerciseTable = popupView.findViewById(R.id.main_table);
        Spinner focusSpinner = popupView.findViewById(R.id.focusSpinner);
        Collections.sort(focusList);
        ArrayAdapter<String> focusAdapter = new ArrayAdapter<String>(
                getContext(), android.R.layout.simple_spinner_dropdown_item, focusList);
        focusSpinner.setAdapter(focusAdapter);
        focusSpinner.setOnItemSelectedListener(new SpinnerListener());
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
                addExercisesToTable();
                alertDialog.dismiss();
            }
        });
    }

    public void populateDefaultExercises(){
        /*
            Obtain all the default exercises from the asset folder. The focus list is also populated here and the custom exercise hash
            table is initialized with empty array lists at each focus key.
         */
        BufferedReader reader;
        try{
            reader = new BufferedReader(new InputStreamReader(getActivity().getAssets().open(Variables.DEFAULT_EXERCISES_FILE)));
            String line;
            String focus=null;
            while((line=reader.readLine())!=null){
                if(line.split(Variables.SPLIT_DELIM)[Variables.FOCUS_INDEX].equals(Variables.FOCUS_DELIM)){
                    focus = line.split(Variables.SPLIT_DELIM)[Variables.FOCUS_NAME_INDEX];
                    focusList.add(focus);
                    defaultExercises.put(focus,new ArrayList<String>());
                    customExercises.put(focus,new ArrayList<String>());
                }
                else{
                    defaultExercises.get(focus).add(line);
                }
            }
            reader.close();
        }
        catch (Exception e){
            Log.d("ERROR","Error when trying to populate from default exercises file\n"+e);
        }
    }

    public void updateExerciseChoices(String exerciseFocus){
        /*
            Given a value from the exercise focus spinner, list all the exercises associate with it.
         */
        ArrayList<String> sortedExercises = new ArrayList<>();
        for(String exercise : defaultExercises.get(exerciseFocus)){
            sortedExercises.add(exercise);
        }
        for(String exercise : customExercises.get(exerciseFocus)){
            sortedExercises.add(exercise);
        }
        Collections.sort(sortedExercises);
        for(int i=0;i<sortedExercises.size();i++){
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
                    if(!checkedExercises.contains(exercise.getText().toString()) &&
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

    public void updateCurrentWorkoutLog(String workoutName){
        /*
            Add the new workout to the bottom of the workout log. Recall that each line of the log represents the file name
            of the workout followed by the current day of the workout. Thus it is imperative to not lose the old workouts when
            adding this new one.
         */
        BufferedReader reader;
        BufferedWriter writer;
        File fhandleOld = new File(getContext().getExternalFilesDir(Variables.WORKOUT_DIRECTORY), Variables.CURRENT_WORKOUT_LOG);
        File fhandleNew = new File(getContext().getExternalFilesDir(Variables.WORKOUT_DIRECTORY), "temp");
        try{
            // progress through the file until the correct spot is found
            writer = new BufferedWriter(new FileWriter(fhandleNew,true));
            FileReader fileR = new FileReader(fhandleOld);
            reader = new BufferedReader(fileR);
            String line;
            while((line=reader.readLine())!=null){
                writer.write(line+"\n");
            }
            // add the new workout to the log and start it at day 0
            writer.write(workoutName+"*0");
            reader.close();
            writer.close();
            fhandleOld.delete();
            fhandleNew.renameTo(fhandleOld);
        }
        catch (Exception e){
            Log.d("ERROR","Error when trying to updateWorkoutEntity current workout log!\n"+e);
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
            String selectedExerciseFocus = parent.getItemAtPosition(pos).toString();
            pickExerciseTable.removeAllViews();
            updateExerciseChoices(selectedExerciseFocus);
        }

        public void onNothingSelected(AdapterView parent) {
        }
    }

}
