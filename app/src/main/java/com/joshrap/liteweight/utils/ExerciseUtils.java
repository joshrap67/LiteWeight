package com.joshrap.liteweight.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

public class ExerciseUtils {

    /**
     * Attempts to launch a video of a given exercise with either the built in video app (like Youtube) or browser.
     *
     * @param url     url of the video.
     * @param context current context of the calling component.
     */
    public static void launchVideo(String url, Context context) {
        String errorMsg = ValidatorUtils.validUrl(url);
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
