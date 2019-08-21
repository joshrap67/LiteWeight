package com.joshrap.liteweight.Fragments;

import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.joshrap.liteweight.*;
import com.joshrap.liteweight.Database.Entities.ExerciseEntity;
import com.joshrap.liteweight.Database.Entities.MetaEntity;
import com.joshrap.liteweight.Database.Entities.WorkoutEntity;
import com.joshrap.liteweight.Database.ViewModels.ExerciseViewModel;
import com.joshrap.liteweight.Database.ViewModels.MetaViewModel;
import com.joshrap.liteweight.Database.ViewModels.WorkoutViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

public class MyWorkoutFragment extends Fragment {
    private View view;
    private TextView selectedWorkoutTV, statisticsTV, defaultTV;
    private ListView listView;
    private ViewGroup fragmentContainer;
    private TableLayout displayedExercisesTable, pickExerciseTable;
    private AlertDialog alertDialog;
    private ArrayAdapter<String> arrayAdapter;
    private MetaEntity selectedWorkout;
    private HashMap<String, MetaEntity> workoutNameToEntity = new HashMap<>();
    private WorkoutViewModel workoutModel;
    private MetaViewModel metaModel;
    private ExerciseViewModel exerciseViewModel;
    private boolean firstDay, lastDay, editing;
    private int maxDayIndex, currentDayIndex;
    private ArrayList<MetaEntity> metaEntities = new ArrayList<>();
    private ArrayList<String> workoutNames = new ArrayList<>();
    private ArrayList<String> focusList = new ArrayList<>();
    private ArrayList<String> checkedExercises = new ArrayList<>();
    private HashMap<Integer, ArrayList<String>> pendingWorkout = new HashMap<>();
    private HashMap<String, ArrayList<String>> allExercises = new HashMap<>();
    private HashMap<String, ExerciseEntity> exerciseNameToEntity = new HashMap<>();
    private HashMap<Integer, ArrayList<String>> originalWorkout = new HashMap<>();
    private HashMap<Integer, ArrayList<String>> deletedExercises = new HashMap<>();
    private HashMap<Integer, ArrayList<String>> newExercises = new HashMap<>();
    private SimpleDateFormat formatter = new SimpleDateFormat(Variables.DATE_PATTERN);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.default_layout, container, false);
        fragmentContainer = container;
        defaultTV = view.findViewById(R.id.default_text_view);
        defaultTV.setVisibility(View.GONE);
        ((MainActivity) getActivity()).updateToolbarTitle(Variables.MY_WORKOUT_TITLE);
        metaModel = ViewModelProviders.of(getActivity()).get(MetaViewModel.class);
        workoutModel = ViewModelProviders.of(getActivity()).get(WorkoutViewModel.class);
        exerciseViewModel = ViewModelProviders.of(getActivity()).get(ExerciseViewModel.class);
        GetAllMetaTask task = new GetAllMetaTask();
        task.execute();
        return view;
    }

    private class GetAllMetaTask extends AsyncTask<Void, Void, ArrayList<MetaEntity>> {

        @Override
        protected ArrayList<MetaEntity> doInBackground(Void... voids) {
            // get the current workout from the database
            return metaModel.getAllMetadata();
        }

        @Override
        protected void onPostExecute(ArrayList<MetaEntity> result) {
            if(!result.isEmpty()) {
                metaEntities = result;
                ((MainActivity) getActivity()).setProgressBar(false);
                initViews();
            }
            else{
                defaultTV.setVisibility(View.VISIBLE);
            }
        }
    }

    public void initViews(){
        /*
            Once at least one workout is found, change layouts and initialize all views
         */
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.fragment_my_workouts, fragmentContainer, false);
        ViewGroup rootView = (ViewGroup) getView();
        rootView.removeAllViews();
        rootView.addView(view);
        listView = view.findViewById(R.id.workout_list);
        selectedWorkoutTV = view.findViewById(R.id.selected_workout_text_view);
        statisticsTV = view.findViewById(R.id.stat_text_view);

        for(MetaEntity entity : metaEntities){
            if(entity.getCurrentWorkout()){
                selectedWorkout = entity;
                selectedWorkoutTV.setText(selectedWorkout.getWorkoutName());
                updateStatistics();
            }
            else{
                workoutNames.add(entity.getWorkoutName());
            }
            workoutNameToEntity.put(entity.getWorkoutName(),entity);
        }
        sortWorkouts();
        // set up the buttons
        Button deleteButton = view.findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!metaEntities.isEmpty()){
                    promptDelete();
                }
            }
        });
        Button editButton = view.findViewById(R.id.edit_button);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getExercises();
            }
        });
        Button resetStatisticsButton = view.findViewById(R.id.reset_statistics);
        resetStatisticsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptReset();
            }
        });
        // set up the list view
        arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_activated_1, workoutNames);
        listView.setAdapter(arrayAdapter);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectWorkout(listView.getItemAtPosition(position).toString());
            }
        });
        listView.setItemChecked(0, true); // programmatically select current workout in list
    }

    public void sortWorkouts(){
        /*
            Currently sorts by date last accessed
         */
        workoutNames.clear();
        Collections.sort(metaEntities, Collections.reverseOrder());
        for(MetaEntity entity : metaEntities) {
            if (!entity.getWorkoutName().equals(selectedWorkout.getWorkoutName())) {
                workoutNames.add(entity.getWorkoutName());
            }
        }
        workoutNames.add(0,selectedWorkout.getWorkoutName()); // selected always on top
    }

    public void selectWorkout(String workoutName){
        /*
            Selects a workout from the list and handles any updates to the DB
         */
        // handle the currently selected workout
        selectedWorkout.setCurrentWorkout(false);
        Date date = new Date();
        selectedWorkout.setDateLast(formatter.format(date));
        metaModel.update(selectedWorkout);
        // handle the newly selected workout
        selectedWorkout = workoutNameToEntity.get(workoutName);
        if(selectedWorkout == null) {
            // This should never happen, but if so just reset the fragment
            resetFragment();
        }
        selectedWorkout.setCurrentWorkout(true);
        metaModel.update(selectedWorkout);
        selectedWorkoutTV.setText(workoutName);
        sortWorkouts();
        arrayAdapter.notifyDataSetChanged();
        updateStatistics();
        listView.setItemChecked(0, true); // programmatically select current workout in list
    }

    public void updateStatistics(){
        /*
            Displays statistics for the currently selected workout
         */
        int timesCompleted = selectedWorkout.getTimesCompleted();
        double percentage = selectedWorkout.getPercentageExercisesCompleted();
        String formattedPercentage;
        if(percentage > 0.0 && percentage < 100.0) {
            formattedPercentage = String.format("%.3f", percentage) + "%";
        }
        else if(percentage == 0.0){
            formattedPercentage = "0%";
        }
        else{
            formattedPercentage = "100%";
        }
        int days = selectedWorkout.getTotalDays()+1;
        String msg = "Times Completed: " + timesCompleted + "\n" +
                "Average Percentage of Exercises Completed: " + formattedPercentage + "\n" +
                "Number of Days in Workout: " + days + "\n" +
                "Most Worked Focus: " + selectedWorkout.getMostFrequentFocus();
        statisticsTV.setText(msg);
    }

    public void promptReset(){
        /*
            Prompt the user if they actually want to reset the selected workout
         */
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        final AlertDialog alertDialog = alertDialogBuilder.create();
        final View popupView = getLayoutInflater().inflate(R.layout.popup_reset_statistics, null);
        Button confirmButton = popupView.findViewById(R.id.reset_confirm);
        TextView workoutName = popupView.findViewById(R.id.workout_name);
        workoutName.setText(selectedWorkout.getWorkoutName());
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetStatistics();
                alertDialog.dismiss();
            }
        });
        Button quitButton = popupView.findViewById(R.id.reset_denial);
        quitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        alertDialog.setView(popupView);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }
    public void resetStatistics(){
        /*
            Resets the statistics of the currently selected workout and updates it in the DB
         */
        selectedWorkout.setPercentageExercisesCompleted(0.0);
        selectedWorkout.setCompletedSum(0);
        selectedWorkout.setTimesCompleted(0);
        selectedWorkout.setTotalSum(0);
        metaModel.update(selectedWorkout);
        updateStatistics();
    }
    public void promptDelete(){
        /*
            Prompt user if they actually want to delete the currently selected workout
         */
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        final AlertDialog alertDialog = alertDialogBuilder.create();
        final View popupView = getLayoutInflater().inflate(R.layout.popup_delete_workout, null);
        Button confirmButton = popupView.findViewById(R.id.popup_yes);
        TextView workoutName = popupView.findViewById(R.id.workout_name);
        workoutName.setText(selectedWorkout.getWorkoutName());
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteWorkout();
                alertDialog.dismiss();
            }
        });
        Button quitButton = popupView.findViewById(R.id.popup_no);
        quitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        alertDialog.setView(popupView);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }
    public void deleteWorkout(){
        /*
            Deletes the currently selected workout from the DB by using an async task
         */
        metaEntities.remove(selectedWorkout);
        DeleteWorkoutAsync task = new DeleteWorkoutAsync();
        task.execute(selectedWorkout);
    }

    private class DeleteWorkoutAsync extends AsyncTask<MetaEntity, Void, Void> {

        @Override
        protected Void doInBackground(MetaEntity... param) {
            metaModel.delete(param[0]);
            workoutModel.deleteEntireWorkout(param[0].getWorkoutName());
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            workoutNames.remove(0); // remove the old selected workout
            if(!workoutNames.isEmpty()){
                selectedWorkout = workoutNameToEntity.get(workoutNames.get(0)); // get the top of the list
                selectedWorkout.setCurrentWorkout(true);
                Date date = new Date();
                selectedWorkout.setDateLast(formatter.format(date));
                metaModel.update(selectedWorkout);
                selectedWorkoutTV.setText(selectedWorkout.getWorkoutName());
                arrayAdapter.notifyDataSetChanged();
            }
            else{
                // signal to go make a new workout, all workouts have been deleted
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.default_layout, fragmentContainer, false);
                ViewGroup rootView = (ViewGroup) getView();
                rootView.removeAllViews();
                rootView.addView(view);
            }
        }
    }

    //region
    // Edit workout methods

    public void getExercises(){
        GetAllExercisesTask task = new GetAllExercisesTask();
        task.execute();
    }

    private class GetAllExercisesTask extends AsyncTask<Void, Void, ArrayList<ExerciseEntity>> {

        @Override
        protected ArrayList<ExerciseEntity> doInBackground(Void... voids) {
            return exerciseViewModel.getAllExercises();
        }

        @Override
        protected void onPostExecute(ArrayList<ExerciseEntity> result) {
            if(!result.isEmpty()) {
                for(ExerciseEntity entity : result) {
                    String[] focuses = entity.getFocus().split(Variables.FOCUS_DELIM_DB);
                    for(String focus : focuses) {
                        // need to populate all focuses that are in the DB
                        if(!focusList.contains(focus)) {
                            focusList.add(focus);
                            allExercises.put(focus, new ArrayList<String>());
                        }
                        allExercises.get(focus).add(entity.getExerciseName());
                    }
                    exerciseNameToEntity.put(entity.getExerciseName(), entity);
                }
                GetWorkoutTask task = new GetWorkoutTask();
                task.execute();
            }
            else{
                // this shouldn't happen, but if so just reset fragment
                resetFragment();
            }
        }
    }

    private class GetWorkoutTask extends AsyncTask<Void, Void, ArrayList<WorkoutEntity>> {

        @Override
        protected ArrayList<WorkoutEntity> doInBackground(Void... voids) {
            // get the entirety of the current workout from the database
            return workoutModel.getExercises(selectedWorkout.getWorkoutName());
        }

        @Override
        protected void onPostExecute(ArrayList<WorkoutEntity> result) {
            initEdit(result);
        }
    }

    public void initEdit(ArrayList<WorkoutEntity> rawData){
        /*
            Received the workout from DB, so now change layouts to the edit one and init all views and variables
         */
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View editWorkoutView = inflater.inflate(R.layout.edit_workout, fragmentContainer,false);
        ViewGroup rootView = (ViewGroup) getView();
        rootView.removeAllViews();
        rootView.addView(editWorkoutView);
        displayedExercisesTable = editWorkoutView.findViewById(R.id.main_table);
        currentDayIndex = 0;
        maxDayIndex = selectedWorkout.getTotalDays();
        for(int i =0;i<=maxDayIndex;i++){
            // init the hash tables
            pendingWorkout.put(i,new ArrayList<String>());
            deletedExercises.put(i, new ArrayList<String>());
            newExercises.put(i, new ArrayList<String>());
            originalWorkout.put(i, new ArrayList<String>());
        }
        for(WorkoutEntity entity : rawData){
            // add the exercises to the hash tables
            pendingWorkout.get(entity.getDay()).add(entity.getExercise());
            originalWorkout.get(entity.getDay()).add(entity.getExercise());
        }
        addExercisesToMainTable(); // load first day into the displayed table
        setButtons(editWorkoutView);

    }

    public void setButtons(View view){
        /*
            Setup buttons to allow for cycling through all the days of the workout. Logic is included to ensure that the user
            does not go beyond the bounds of the days specified by their input. Also ensure that each day has at
            least one exercise before allowing output to DB.
         */
        firstDay = true;
        final Button addExercises = view.findViewById(R.id.add_exercises);
        addExercises.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupAddExercises();
            }
        });
        final Button backBtn = view.findViewById(R.id.back_button);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editing){
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                    final AlertDialog alertDialog = alertDialogBuilder.create();
                    final View popupView = getLayoutInflater().inflate(R.layout.popup_edit_workout_in_progress, null);
                    Button confirmButton = popupView.findViewById(R.id.popup_yes);
                    confirmButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            resetFragment();
                            alertDialog.dismiss();
                        }
                    });
                    Button quitButton = popupView.findViewById(R.id.popup_no);

                    quitButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                        }
                    });
                    alertDialog.setView(popupView);
                    alertDialog.setCanceledOnTouchOutside(false);
                    alertDialog.show();
                }
                else{
                    resetFragment();
                }

            }
        });
        final Button previousDayBtn = view.findViewById(R.id.previous_day_button);
        final Button nextDayBtn = view.findViewById(R.id.next_day_button);
        final TextView dayTitle = view.findViewById(R.id.day_text_view);
        dayTitle.setText(Variables.generateDayTitle(currentDayIndex, maxDayIndex));
        previousDayBtn.setVisibility(View.INVISIBLE);
        if(maxDayIndex == 0){
            // in case some jabroni only wants to workout one day total
            nextDayBtn.setVisibility(View.INVISIBLE);
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
                    nextDayBtn.setVisibility(View.VISIBLE);
                }

                if(currentDayIndex == 0){
                    previousDayBtn.setVisibility(View.INVISIBLE);
                    firstDay = true;
                }
                addExercisesToMainTable();
                dayTitle.setText(Variables.generateDayTitle(currentDayIndex,maxDayIndex));
            }
        });
        nextDayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentDayIndex < maxDayIndex) {
                    displayedExercisesTable.removeAllViews();
                    currentDayIndex++;
                    if(firstDay){
                        firstDay = false;
                        previousDayBtn.setVisibility(View.VISIBLE);
                    }
                    if(currentDayIndex == maxDayIndex){
                        lastDay = true;
                        nextDayBtn.setVisibility(View.INVISIBLE);
                    }
                    addExercisesToMainTable();
                    dayTitle.setText(Variables.generateDayTitle(currentDayIndex,maxDayIndex));
                }
            }
        });
        final Button doneEditingBtn = view.findViewById(R.id.done_editing);
        doneEditingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean ready = true;
                for(int i = 0; i< pendingWorkout.size(); i++){
                    if(pendingWorkout.get(i) == null){
                        ready = false;
                    }
                    else if(pendingWorkout.get(i).isEmpty()){
                        ready = false;
                    }
                }
                if(ready){
                    writeToDatabase();
                    Toast.makeText(getContext(),"Successfully edited!",Toast.LENGTH_SHORT).show();
                    // restart this fragment
                    resetFragment();
                }
                else{
                    Toast.makeText(getContext(),"Ensure each day has at least one exercise!",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void writeToDatabase(){
        /*
            Updates the workout in the DB
         */
        for(int i=0;i<=maxDayIndex;i++){
            for(String exercise : deletedExercises.get(i)){
                workoutModel.deleteSpecificExerciseFromWorkout(selectedWorkout.getWorkoutName(),exercise,i);
            }
        }

        for(int i=0;i<=maxDayIndex;i++){
            for(String exercise : newExercises.get(i)){
                if(!originalWorkout.get(i).contains(exercise)){
                    // don't if for some reason they removed it and added it back, want to keep the original status
                    WorkoutEntity newEntity = new WorkoutEntity(exercise,selectedWorkout.getWorkoutName(),i,false);
                    workoutModel.insert(newEntity);
                }
            }
        }
        String mostCommonFocus = Validator.mostFrequentFocus(pendingWorkout, exerciseNameToEntity, focusList);
        selectedWorkout.setMostFrequentFocus(mostCommonFocus);
        metaModel.update(selectedWorkout);
    }

    public void popupAddExercises(){
        /*
            User has indicated they wish to add exercises to this specific day. Show a popup that provides a spinner
            that is programmed to list all exercises for a given exercise focus.
         */
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialog = alertDialogBuilder.create();
        View popupView = getLayoutInflater().inflate(R.layout.popup_add_exercise, null);
        pickExerciseTable = popupView.findViewById(R.id.main_table);
        Spinner focusSpinner = popupView.findViewById(R.id.focus_spinner);
        Collections.sort(focusList);
        ArrayAdapter<String> focusAdapter = new ArrayAdapter<String>(
                getContext(), android.R.layout.simple_spinner_dropdown_item, focusList);
        focusSpinner.setAdapter(focusAdapter);
        focusSpinner.setOnItemSelectedListener(new SpinnerListener());
        alertDialog.setView(popupView);
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
        Button doneBtn = popupView.findViewById(R.id.done_btn);
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // done adding
                editing = true;
                for(String exercise : checkedExercises){
                    pendingWorkout.get(currentDayIndex).add(exercise);
                    newExercises.get(currentDayIndex).add(exercise);
                    deletedExercises.get(currentDayIndex).remove(exercise);
                }
                checkedExercises.clear();
                displayedExercisesTable.removeAllViews();
                addExercisesToMainTable();
                alertDialog.dismiss();
            }
        });
    }

    public void addExercisesToMainTable(){
        /*
            Adds an exercise of the workout to the main displayed table and allows for specific exercises to be deleted
         */
        Collections.sort(pendingWorkout.get(currentDayIndex));
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int count = 0;
        for(final String exercise : pendingWorkout.get(currentDayIndex)){
            final View row = inflater.inflate(R.layout.row_list_view_element, null);
            TextView exerciseName = row.findViewById(R.id.exercise_name);
            exerciseName.setText(exercise);
            ImageButton deleteIcon = row.findViewById(R.id.delete_exercise);
            deleteIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editing = true;
                    displayedExercisesTable.removeView(row);
                    pendingWorkout.get(currentDayIndex).remove(exercise);
                    newExercises.get(currentDayIndex).remove(exercise);
                    deletedExercises.get(currentDayIndex).add(exercise);
                }
            });
            displayedExercisesTable.addView(row, count);
            count++;
        }
    }

    public void updateExerciseChoices(String exerciseFocus){
        /*
            Given a value from the exercise focus spinner, list all the exercises associated with it.
         */
        ArrayList<String> sortedExercises = new ArrayList<>();
        for(String exercise : allExercises.get(exerciseFocus)){
            sortedExercises.add(exercise);
        }
        Collections.sort(sortedExercises);
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for(int i=0;i<sortedExercises.size();i++){
            final View row = inflater.inflate(R.layout.row_add_exercise, null);
            final CheckBox exercise = row.findViewById(R.id.exercise_checkbox);
            String exerciseName = sortedExercises.get(i);
            exercise.setText(exerciseName);
            if (checkedExercises.contains(exerciseName) || pendingWorkout.get(currentDayIndex).contains(exerciseName)) {
                exercise.setChecked(true);
            }
            exercise.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (exercise.isChecked()) {
                        checkedExercises.add(exercise.getText().toString());
                    } else {
                        pendingWorkout.get(currentDayIndex).remove(exercise.getText().toString());
                        deletedExercises.get(currentDayIndex).add(exercise.getText().toString());
                        checkedExercises.remove(exercise.getText().toString());
                    }
                }
            });
            pickExerciseTable.addView(row,i);
        }
    }

    public void resetFragment(){
        /*
            Resets the current fragment entirely.
         */
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new MyWorkoutFragment(), Variables.MY_WORKOUT_TITLE).commit();
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
    //endregion
}
