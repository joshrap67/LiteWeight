package com.joshrap.liteweight.helpers;

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

    public static void launchVideo(String url, Context context) {
        /*
            Attempts to launch a video of a given exercise with either the built in video app (like Youtube) or browser
         */
        String errorMsg = InputHelper.validUrl(url);
        if (errorMsg == null) {
            Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            try {
                context.startActivity(appIntent);
            } catch (ActivityNotFoundException ex) {
                context.startActivity(webIntent);
            }
        } else {
            Toast.makeText(context, "No valid URL found.", Toast.LENGTH_LONG).show();
        }
    }
}
