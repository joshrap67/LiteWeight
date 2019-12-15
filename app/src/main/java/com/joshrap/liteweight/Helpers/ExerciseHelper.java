package com.joshrap.liteweight.Helpers;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.joshrap.liteweight.Database.Entities.ExerciseEntity;
import com.joshrap.liteweight.Validator;

public class ExerciseHelper {
    public static void launchVideo(ExerciseEntity exerciseEntity, Context context, Activity activity) {
        String errorMsg = Validator.checkValidURL(exerciseEntity.getUrl());
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
}
