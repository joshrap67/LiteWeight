package com.example.workoutmadness;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TableRow;
import android.widget.Toast;

public class Exercise{
    private Context context;
    private Activity activity;
    private String name;
    private String videoURL;
    private boolean status, previouslyModified;
    private TableRow displayedRow;
    private static final int TIME_INDEX = 0, TIME_TITLE_INDEX = 1, NAME_INDEX = 0, STATUS_INDEX = 1, VIDEO_INDEX = 2,
            WORKOUT_NAME_INDEX = 0, CURRENT_DAY_INDEX = 1;
    private String WORKOUT_DIRECTORY, CURRENT_WORKOUT_LOG, STATISTICS_DIRECTORY, SPLIT_DELIM="\\*", selectedWorkout,
            DAY_DELIM="TIME",EXERCISE_COMPLETE ="COMPLETE", EXERCISE_INCOMPLETE = "INCOMPLETE";

    private Exercise(final String[] rawText, Context aContext, Activity anActivity){
        context=aContext;
        activity=anActivity;
        if(rawText[STATUS_INDEX].equals(EXERCISE_COMPLETE)){
            // means that the exercise has already been done, so make sure to set status as so
            previouslyModified=true;
            status=true;
        }
        else{
            status=false;
        }
        name=rawText[NAME_INDEX];
        videoURL=rawText[VIDEO_INDEX];
    }
    private void setStatus(boolean aStatus){
            /*
                Sets the status of the exercise as either being complete or incomplete.
             */
        status=aStatus;
    }
    public boolean isPreviouslyModified(){
        // allows for a fragment to determine if this workout has been started already
        return previouslyModified;
    }
    private TableRow getDisplayedRow(){
            /*
                Takes all of the information from the instance variables of this exercise and puts it into a row to be displayed
                by the main table.
             */
        displayedRow = new TableRow(activity);
        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
        displayedRow.setLayoutParams(lp);

        final CheckBox exercise = new CheckBox(activity);
        if(status){
            exercise.setChecked(true);
        }
        exercise.setOnClickListener(new View.OnClickListener() {
            boolean checked = exercise.isChecked();

            @Override
            public void onClick(View v) {
                if(checked){
                    status=false;
                }
                else{
                    status=true;
                }
//                modified=true;
            }
        });
        exercise.setText(name);
        displayedRow.addView(exercise);

        Button videoButton = new Button(activity);
        videoButton.setText("Video");
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
        displayedRow.addView(videoButton);
        return displayedRow;
    }

    private String getFormattedLine(){
            /*
                Utilized whenever writing to a file. This method formats the information of the exercise
                instance into the proper format specified in this project.
             */
        String retVal;
        if(status){
            retVal = name+"*"+EXERCISE_COMPLETE+"*"+videoURL;
        }
        else{
            retVal = name+"*"+EXERCISE_INCOMPLETE+"*"+videoURL;
        }
        return retVal;
    }
}
