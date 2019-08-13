package com.joshrap.liteweight;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.joshrap.liteweight.Database.Entities.ExerciseEntity;
import com.joshrap.liteweight.Database.Entities.WorkoutEntity;
import com.joshrap.liteweight.Database.ViewModels.ExerciseViewModel;
import com.joshrap.liteweight.Database.ViewModels.WorkoutViewModel;
import com.joshrap.liteweight.Fragments.*;
import android.widget.Toast;

public class Exercise implements Comparable<Exercise>{
    private Context context;
    private Activity activity;
    private String name, videoURL;
    private boolean status, videos, ignoreWeight, metricUnits;
    private Fragment fragment;
    private WorkoutViewModel workoutViewModel;
    private ExerciseViewModel exerciseViewModel;
    private WorkoutEntity workoutEntity;
    private ExerciseEntity exerciseEntity;
    private double weight;
    private String formattedWeight;

    public Exercise(final WorkoutEntity workoutEntity, ExerciseEntity exerciseEntity, Context context, Activity activity,
                    Fragment fragment, boolean videos, boolean metricUnits, WorkoutViewModel workoutViewModel,
                    ExerciseViewModel exerciseViewModel){
        /*
            Constructor utilized for database stuff
         */
        this.workoutEntity = workoutEntity;
        this.exerciseEntity = exerciseEntity;
        this.context = context;
        this.activity = activity;
        this.fragment = fragment;
        this.videos = videos;
        this.metricUnits = metricUnits;
        this.workoutViewModel = workoutViewModel;
        this.exerciseViewModel = exerciseViewModel;
        if(workoutEntity.getStatus()){
            if(fragment instanceof CurrentWorkoutFragment){
                ((CurrentWorkoutFragment) fragment).setPreviouslyModified(true);
            }
            this.status = true;
        }
        else{
            this.status = false;
        }
        this.name = workoutEntity.getExercise();
    }

    public Exercise(String exerciseName){
        /*
            Constructor utilized by the new workout fragment when writing to a file
         */
        name = exerciseName;
        status = false;
    }

    public void setStatus(boolean aStatus){
            /*
                Sets the status of the exercise as either being complete or incomplete.
             */
        status = aStatus;
        workoutEntity.setStatus(aStatus);
    }

    public WorkoutEntity getWorkoutEntity(){
        return this.workoutEntity;
    }

    public String getName(){
        return this.name;
    }

    public View getDisplayedRow(){
            /*
                Takes all of the information from the instance variables of this exercise and puts it into a row to be displayed
                by the main table.
             */
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View row = inflater.inflate(R.layout.exercise_row,null);
        final CheckBox exerciseName = row.findViewById(R.id.exercise_name);
        final Button weightButton = row.findViewById(R.id.weight_button);
        // setup checkbox
        exerciseName.setText(name);
        if(status){
            exerciseName.setChecked(true);
        }
        exerciseName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(status){
                    workoutEntity.setStatus(false);
                    workoutViewModel.update(workoutEntity);
                    status = false;
                }
                else{
                    workoutEntity.setStatus(true);
                    workoutViewModel.update(workoutEntity);
                    status = true;
                }
                if(fragment instanceof CurrentWorkoutFragment){
                    ((CurrentWorkoutFragment) fragment).setModified(true);
                    ((CurrentWorkoutFragment) fragment).setPreviouslyModified(true);
                }
            }
        });
        // set up weight button
        if(metricUnits){
            // value in DB is always in murican units
            weight = exerciseEntity.getCurrentWeight()*Variables.KG;
        }
        else{
            weight = exerciseEntity.getCurrentWeight();
        }
        formattedWeight = Validator.getFormattedWeight(weight);
        if(weight >= 0){
            weightButton.setText(formattedWeight+(metricUnits?" kg":" lb"));
        }
        else{
            weightButton.setText("N/A");
        }
        weightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                final AlertDialog alertDialog = alertDialogBuilder.create();
                View popupView = activity.getLayoutInflater().inflate(R.layout.popup_edit_weight, null);
                alertDialog.setView(popupView);
                alertDialog.setCanceledOnTouchOutside(true);
                alertDialog.show();

                TextView exerciseName = popupView.findViewById(R.id.exercise_name);
                exerciseName.setText(name);
                final EditText weightInput = popupView.findViewById(R.id.name_input);
                weightInput.setHint(formattedWeight);
                final Switch ignoreWeightSwitch = popupView.findViewById(R.id.ignore_weight_switch);
                if(weight < 0){
                    ignoreWeightSwitch.setChecked(true);
                    ignoreWeight = true;
                    weightInput.setVisibility(View.GONE);
                }
                else{
                    ignoreWeight = false;
                    ignoreWeightSwitch.setChecked(false);
                }
                ignoreWeightSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        ignoreWeight = isChecked;
                        if(ignoreWeight){
                            weightInput.setVisibility(View.GONE);
                        }
                        else{
                            weightInput.setHint(Integer.toString(0)); // to get rid of sentinel value from Database
                            weightInput.setVisibility(View.VISIBLE);
                        }
                    }
                });
                Button doneButton = popupView.findViewById(R.id.done_btn);
                Button backButton = popupView.findViewById(R.id.back_btn);
                backButton.setVisibility(View.GONE); // this button isn't needed for this part of the app
                doneButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(ignoreWeight){
                            weight = Variables.IGNORE_WEIGHT_VALUE;
                            exerciseEntity.setCurrentWeight(weight);
                            weightButton.setText("N/A");
                            exerciseViewModel.update(exerciseEntity);
                            alertDialog.dismiss();
                        }
                        else if(!weightInput.getText().toString().equals("")){
                            weight = Double.parseDouble(weightInput.getText().toString());
                            formattedWeight = Validator.getFormattedWeight(weight);
                            weightButton.setText(formattedWeight+(metricUnits?" kg":" lb"));
                            if(metricUnits){
                                // convert if in metric
                                weight /= Variables.KG;
                            }
                            if(weight > exerciseEntity.getMaxWeight()){
                                exerciseEntity.setMaxWeight(weight);
                            }
                            else if(weight < exerciseEntity.getMinWeight()){
                                exerciseEntity.setMinWeight(weight);
                            }
                            exerciseEntity.setCurrentWeight(weight);
                            exerciseViewModel.update(exerciseEntity);
                            alertDialog.dismiss();
                        }
                        else{
                            Toast.makeText(activity,"Enter a valid weight!",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        // setup video button
        if(videos){
            ImageButton videoButton = row.findViewById(R.id.launch_video);
            videoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String errorMsg = Validator.checkValidURL(exerciseEntity.getUrl());
                    if(errorMsg==null){
                        // found on SO
                        videoURL = exerciseEntity.getUrl();
                        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoURL));
                        Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoURL));
                        try{
                            context.startActivity(appIntent);
                        }
                        catch(ActivityNotFoundException ex) {
                            context.startActivity(webIntent);
                        }
                    }
                    else{
                        Log.d("TAG","Error with URL:\n"+errorMsg);
                        Toast.makeText(activity, "No URL found!", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
        else{
            ImageView videoButton = row.findViewById(R.id.launch_video);
            videoButton.setVisibility(View.GONE);
        }
        return row;
    }

    @Override
    public int compareTo(Exercise o) {
        // used for sorting purposes
        return this.name.compareTo(o.name);
    }
}
