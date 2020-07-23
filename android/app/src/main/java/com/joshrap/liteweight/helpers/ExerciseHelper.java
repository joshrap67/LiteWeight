package com.joshrap.liteweight.helpers;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.joshrap.liteweight.database.entities.ExerciseEntity;
import com.joshrap.liteweight.imports.Variables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class ExerciseHelper {

    public static void launchVideo(ExerciseEntity exerciseEntity, Context context, Activity activity) {
        /*
            Attempts to launch a video of a given exercise with either the built in video app (like Youtube) or browser
         */
        String errorMsg = InputHelper.validUrl(exerciseEntity.getUrl());
        if (errorMsg == null) {
            String videoURL = exerciseEntity.getUrl();
            Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoURL));
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoURL));
            try {
                context.startActivity(appIntent);
            } catch (ActivityNotFoundException ex) {
                context.startActivity(webIntent);
            }
        } else {
            Toast.makeText(activity, "No valid URL found!", Toast.LENGTH_LONG).show();
        }
    }

    public static String mostFrequentFocus(HashMap<Integer, ArrayList<String>> totalExercises,
                                           HashMap<String, ExerciseEntity> exerciseNameToEntity,
                                           ArrayList<String> focusList) {
        /*
            Finds the most common focus or focuses of a workout.
         */
        HashMap<String, Integer> focusCount = new HashMap<>();
        for (String focus : focusList) {
            // init each focus to have a count of 0
            focusCount.put(focus, 0);
        }
        for (int i = 0; i < totalExercises.size(); i++) {
            for (String exercise : totalExercises.get(i)) {
                // for each exercise in the workout, find all the focuses associated with it and update the focus count
                String[] focuses = exerciseNameToEntity.get(exercise).getFocus().split(Variables.FOCUS_DELIM_DB);
                for (String focus : focuses) {
                    focusCount.put(focus, focusCount.get(focus) + 1);
                }
            }
        }
        // now find the max count
        int maxCount = 0;
        for (String focus : focusCount.keySet()) {
            if (focusCount.get(focus) > maxCount) {
                maxCount = focusCount.get(focus);
            }
        }
        // build the return value, if a tie, delimit the focuses with commas
        ArrayList<String> maxFocuses = new ArrayList<>();
        for (String focus : focusCount.keySet()) {
            if (focusCount.get(focus) == maxCount) {
                maxFocuses.add(focus);
            }
        }
        Collections.sort(maxFocuses);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < maxFocuses.size(); i++) {
            sb.append(maxFocuses.get(i) + ((i == maxFocuses.size() - 1) ? "" : ","));
        }
        return sb.toString();
    }
}
