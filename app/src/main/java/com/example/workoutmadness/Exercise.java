package com.example.workoutmadness;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.workoutmadness.Database.Entities.WorkoutEntity;
import com.example.workoutmadness.Database.ViewModels.WorkoutViewModel;
import com.example.workoutmadness.Fragments.*;
import android.widget.Toast;

import java.util.ArrayList;

public class Exercise{
    private Context context;
    private Activity activity;
    private String name, videoURL;
    private boolean status, videos;
    private Fragment fragment;
    private WorkoutViewModel viewModel;

    public Exercise(final String[] rawText, Context aContext, Activity anActivity, Fragment aFragment, boolean videosEnabled, String URL){
        /*
            Constructor utilized by the current workout fragment
         */
        context = aContext;
        activity = anActivity;
        fragment = aFragment;
        videos = videosEnabled;
        if(rawText[Variables.STATUS_INDEX].equals(Variables.EXERCISE_COMPLETE)){
            // means that the exercise has already been done, so make sure to set status as so
            if(fragment instanceof CurrentWorkoutFragment){
                ((CurrentWorkoutFragment) fragment).setPreviouslyModified(true);
            }
            status=true;
        }
        else{
            status=false;
        }
        name = rawText[Variables.NAME_INDEX];
        videoURL = URL;
    }
    public Exercise(final WorkoutEntity entity, Context aContext, Activity anActivity,
                    Fragment aFragment, boolean videosEnabled, String URL, WorkoutViewModel model){
        /*
            Constructor utilized for database stuff
         */
        context = aContext;
        activity = anActivity;
        fragment = aFragment;
        videos = videosEnabled;
        viewModel = model;
        if(entity.getStatus()){
            if(fragment instanceof CurrentWorkoutFragment){
                ((CurrentWorkoutFragment) fragment).setPreviouslyModified(true);
            }
            status=true;
        }
        else{
            status=false;
        }
        name = entity.getExercise();
        videoURL = URL;
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
        status=aStatus;
    }

    public View getDisplayedRow(){
            /*
                Takes all of the information from the instance variables of this exercise and puts it into a row to be displayed
                by the main table.
             */
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View row = inflater.inflate(R.layout.exercise_row,null);
        final CheckBox exerciseName = row.findViewById(R.id.exercise_name);
        exerciseName.setText(name);
        if(status){
            exerciseName.setChecked(true);
        }
        exerciseName.setOnClickListener(new View.OnClickListener() {
            boolean checked = exerciseName.isChecked();
            @Override
            public void onClick(View v) {
                if(checked){
                    // TODO update the DB
                    status=false;
                }
                else{
                    status=true;
                }
                if(fragment instanceof CurrentWorkoutFragment){
                    ((CurrentWorkoutFragment) fragment).setModified(true);
                    ((CurrentWorkoutFragment) fragment).setPreviouslyModified(true);
                }
            }
        });
        if(videos){
            ImageButton videoButton = row.findViewById(R.id.launch_video);
            videoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!videoURL.equalsIgnoreCase("none")){
                        // found on SO
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
                        Toast.makeText(activity, "No video found", Toast.LENGTH_LONG).show();
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

    public String getFormattedLine(){
            /*
                Utilized whenever writing to a file. This method formats the information of the exercise
                instance into the proper format specified in this project.
             */
        String retVal;
        if(status){
            retVal = name+"*"+Variables.EXERCISE_COMPLETE;
        }
        else{
            retVal = name+"*"+Variables.EXERCISE_INCOMPLETE;
        }
        return retVal;
    }
}
