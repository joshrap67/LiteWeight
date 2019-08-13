package com.joshrap.liteweight.Fragments;

import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class MyWorkoutFragment extends Fragment {
    private View view;
    private TextView selectedWorkoutTV, statisticsTV, defaultTV;
    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;
    private MetaEntity selectedWorkout;
    private Button resetStatisticsBtn, editBtn, deleteBtn;
    private HashMap<String, MetaEntity> workoutNameToEntity = new HashMap<>();
    private WorkoutViewModel workoutModel;
    private MetaViewModel metaModel;
    private ExerciseViewModel exerciseViewModel;
    private ArrayList<MetaEntity> metaEntities = new ArrayList<>();
    private ArrayList<String> workoutNames = new ArrayList<>();
    private ArrayList<String> focusList = new ArrayList<>();
    private ArrayList<String> checkedExercises = new ArrayList<>();
    private HashMap<Integer, ArrayList<WorkoutEntity>> totalWorkoutEntities = new HashMap<>();
    private HashMap<Integer, ArrayList<String>> pendingWorkout = new HashMap<>();
    private HashMap<String, ArrayList<String>> exercises = new HashMap<>();
    private HashMap<String, ExerciseEntity> exerciseNameToEntity = new HashMap<>();
    private HashMap<Integer, ArrayList<String>> originalWorkout = new HashMap<>();
    private HashMap<Integer, ArrayList<String>> deletedExercises = new HashMap<>();
    private HashMap<Integer, ArrayList<String>> newExercises = new HashMap<>();
    private ViewGroup fragmentContainer;
    private boolean firstDay, lastDay, editing;
    private int maxDayIndex, currentDayIndex, finalDayNum;
    private TableLayout displayedExercisesTable, pickExerciseTable;
    private AlertDialog alertDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.default_layout,container,false);
        fragmentContainer = container;
        defaultTV = view.findViewById(R.id.default_tv);
        defaultTV.setVisibility(View.GONE);
        ((MainActivity)getActivity()).updateToolbarTitle("My Workouts");
        metaModel = ViewModelProviders.of(getActivity()).get(MetaViewModel.class);
        workoutModel = ViewModelProviders.of(getActivity()).get(WorkoutViewModel.class);
        exerciseViewModel = ViewModelProviders.of(getActivity()).get(ExerciseViewModel.class);
        GetAllMetaTask task = new GetAllMetaTask();
        task.execute();
        // TODO add sorting for listview?
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
                Log.d("TAG","Result size: "+result.size());
                metaEntities = result;
                initViews();
            }
            else{
                defaultTV.setVisibility(View.VISIBLE);
                Log.d("TAG","No workouts found!");
            }
        }
    }

    public void initViews(){
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.fragment_my_workouts, fragmentContainer,false);
        ViewGroup rootView = (ViewGroup) getView();
        rootView.removeAllViews();
        rootView.addView(view);
        listView = view.findViewById(R.id.workout_list);
        selectedWorkoutTV = view.findViewById(R.id.selected_workout_text_view);
        statisticsTV = view.findViewById(R.id.stat_text_view);
        for(MetaEntity entity : metaEntities){
            Log.d("TAG","Meta entity: "+entity.toString());
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
        deleteBtn = view.findViewById(R.id.delete_button);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!metaEntities.isEmpty()){
                    promptDelete();
                }
            }
        });
        editBtn = view.findViewById(R.id.edit_button);
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getExercises();
            }
        });
        // TODO sort by date
        sortWorkouts();
        arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, workoutNames);
        listView.setAdapter(arrayAdapter);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectWorkout(listView.getItemAtPosition(position).toString());
            }
        });
    }

    public void sortWorkouts(){
        Collections.sort(workoutNames);
//        Collections.addAll(sortedWorkoutNames,workoutNames)
//        sortedWorkoutNames.push(selectedWorkout.getWorkoutName());
        // TODO sort by date and possible give other options?
    }

    public void selectWorkout(String workoutName){
        // TODO update the entity with new date
        workoutNames.remove(workoutName);
        // would do this part differently if different sorting method
        workoutNames.add(0,selectedWorkout.getWorkoutName());
        selectedWorkout.setCurrentWorkout(false);
        metaModel.update(selectedWorkout);
        selectedWorkout = workoutNameToEntity.get(workoutName);
        selectedWorkout.setCurrentWorkout(true);
        metaModel.update(selectedWorkout);
        if(selectedWorkout==null){
            // uh oh
            Log.d("TAG","Selected workout was somehow null");
        }
        selectedWorkoutTV.setText(workoutName);
        arrayAdapter.notifyDataSetChanged();
        updateStatistics();
    }
    public void updateStatistics(){
        /*
            Displays statistics for the currently selected workout
         */
        int timesCompleted = selectedWorkout.getTimesCompleted();
        double percentage = selectedWorkout.getPercentageExercisesCompleted();
        int days = selectedWorkout.getTotalDays()+1;
        String msg = "Times Completed: "+timesCompleted+"\n" +
                "Average Percentage of Exercises Completed: "+percentage+"%\n" +
                "Number of Days in Workout: "+days+"\n"+
                "Most worked focus: "+selectedWorkout.getMostFrequentFocus();
        statisticsTV.setText(msg);
    }

    public void promptDelete(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        final AlertDialog alertDialog = alertDialogBuilder.create();
        final View popupView = getLayoutInflater().inflate(R.layout.delete_popup, null);
        Button confirmButton = popupView.findViewById(R.id.popupYes);
        TextView workoutName = popupView.findViewById(R.id.workout_name);
        workoutName.setText(selectedWorkout.getWorkoutName());
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteWorkout();
                alertDialog.dismiss();
            }
        });
        Button quitButton = popupView.findViewById(R.id.popupNo);
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
        metaEntities.remove(selectedWorkout);
        DeleteWorkoutAsync task = new DeleteWorkoutAsync();
        task.execute(selectedWorkout);

    }

    private class DeleteWorkoutAsync extends AsyncTask<MetaEntity, Void, Void> {

        @Override
        protected Void doInBackground(MetaEntity... param) {
            // TODO put a lock here that prevents user from leaving until it's done
            metaModel.delete(param[0]);
            workoutModel.deleteEntireWorkout(param[0].getWorkoutName());
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if(!workoutNames.isEmpty()){
                selectedWorkout = workoutNameToEntity.get(workoutNames.get(0)); // get the top of the list
                workoutNames.remove(selectedWorkout.getWorkoutName());
                selectedWorkout.setCurrentWorkout(true);
                // TODO update the date last of this workout
                metaModel.update(selectedWorkout);

                selectedWorkoutTV.setText(selectedWorkout.getWorkoutName());
                arrayAdapter.notifyDataSetChanged();
            }
            else{
                // signal to go make a new workout, all workouts have been deleted
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.default_layout, fragmentContainer,false);
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
            // get the current workout from the database
            return exerciseViewModel.getAllExercises();
        }

        @Override
        protected void onPostExecute(ArrayList<ExerciseEntity> result) {
            if(!result.isEmpty()) {
                for(ExerciseEntity entity : result){
                    String[] focuses = entity.getFocus().split(Variables.FOCUS_DELIM_DB);
                    for(String focus : focuses){
                        if(!focusList.contains(focus)){
                            focusList.add(focus);
                            exercises.put(focus,new ArrayList<String>());
                        }
                        exercises.get(focus).add(entity.getExerciseName());
                    }
                    exerciseNameToEntity.put(entity.getExerciseName(),entity);
                }
                getWorkouts();
            }
            else{
                // no workouts found
                Log.d("TAG","Get all exercises result was empty!");
            }
        }
    }

    public void getWorkouts(){
        GetAllWorkoutsTask task = new GetAllWorkoutsTask();
        task.execute();
    }

    private class GetAllWorkoutsTask extends AsyncTask<Void, Void, ArrayList<WorkoutEntity>> {

        @Override
        protected ArrayList<WorkoutEntity> doInBackground(Void... voids) {
            // get the current workout from the database
            return workoutModel.getExercises(selectedWorkout.getWorkoutName());
        }

        @Override
        protected void onPostExecute(ArrayList<WorkoutEntity> result) {
            if(!result.isEmpty()) {
                initEdit(result);
            }
            else{
                Log.d("TAG","Selected workout was not found!");
            }
        }
    }

    public void initEdit(ArrayList<WorkoutEntity> rawData){
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
            pendingWorkout.get(entity.getDay()).add(entity.getExercise());
            originalWorkout.get(entity.getDay()).add(entity.getExercise());
        }
        addExercisesToTable(); // load first day into the displayed table
        setButtons(editWorkoutView);

    }

    public void setButtons(View view){
        /*
            Setup buttons to allow for cycling through all the days of the workout. Logic is included to ensure that the user
            does not go beyond the bounds of the days specified by their input. Also ensure that each day has at
            least one exercise before allowing output to a file.
         */
        firstDay = true;
        final Button addExercises = view.findViewById(R.id.add_exercises);
        addExercises.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupExercises();
            }
        });
        final Button backBtn = view.findViewById(R.id.back_button);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO show popup?
                // TODO check if modified, then go reload the fragment
            }
        });
        final Button previousDayBtn = view.findViewById(R.id.previousDayButton);
        final Button nextDayBtn = view.findViewById(R.id.nextDayButton);
        final TextView dayTitle = view.findViewById(R.id.dayTextView);
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
                addExercisesToTable();
                dayTitle.setText(Variables.generateDayTitle(currentDayIndex,finalDayNum));
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
                    addExercisesToTable();
                    dayTitle.setText(Variables.generateDayTitle(currentDayIndex,finalDayNum));
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
                    // restart this fragment
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new MyWorkoutFragment(), "MY_WORKOUTS").commit();
                }
                else{
                    Toast.makeText(getContext(),"Ensure each day has at least one exercise!",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void writeToDatabase(){
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
        String mostCommonFocus = Validator.mostFrequentFocus(pendingWorkout,exerciseNameToEntity,focusList);
        selectedWorkout.setMostFrequentFocus(mostCommonFocus);
        metaModel.update(selectedWorkout);

    }

    public void popupExercises(){
        /*
            User has indicated they wish to add selectedExercises to this specific day. Show a popup that provides a spinner
            that is programmed to list all exercises for a given exercise focus.
         */
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialog = alertDialogBuilder.create();
        View popupView = getLayoutInflater().inflate(R.layout.exercise_popup, null);
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
                editing = true;
                for(String exercise : checkedExercises){
                    pendingWorkout.get(currentDayIndex).add(exercise);
                    newExercises.get(currentDayIndex).add(exercise);
                    deletedExercises.get(currentDayIndex).remove(exercise);
                }
                checkedExercises.clear();
                displayedExercisesTable.removeAllViews();
                addExercisesToTable();
                alertDialog.dismiss();
            }
        });
    }

    public void addExercisesToTable(){
        /*
            Adds an exercise of the workout to the main displayed table and allows for specific exercises to be deleted
         */
        Collections.sort(pendingWorkout.get(currentDayIndex));
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int count = 0;
        for(final String exercise : pendingWorkout.get(currentDayIndex)){
            final View row = inflater.inflate(R.layout.list_row,null);
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
            displayedExercisesTable.addView(row,count);
            count++;
        }
    }

    public void updateExerciseChoices(String exerciseFocus){
        /*
            Given a value from the exercise focus spinner, list all the exercises associated with it.
         */
        ArrayList<String> sortedExercises = new ArrayList<>();
        for(String exercise : exercises.get(exerciseFocus)){
            sortedExercises.add(exercise);
        }
        Collections.sort(sortedExercises);
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for(int i=0;i<sortedExercises.size();i++){
            final View row = inflater.inflate(R.layout.row_add_exercise,null);
            final CheckBox exercise = row.findViewById(R.id.exercise_checkbox);
            String exerciseName = sortedExercises.get(i);
            exercise.setText(exerciseName);
            if(checkedExercises.contains(exerciseName) || pendingWorkout.get(currentDayIndex).contains(exerciseName)){
                exercise.setChecked(true);
            }
            exercise.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!checkedExercises.contains(exercise.getText().toString()) &&
                            !pendingWorkout.get(currentDayIndex).contains(exercise.getText().toString())){
                        // prevents exercise from being added twice
                        checkedExercises.add(exercise.getText().toString());
                    }
                    else{
                        checkedExercises.remove(exercise.getText().toString());
                    }

                }
            });
            pickExerciseTable.addView(row,i);
        }
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
