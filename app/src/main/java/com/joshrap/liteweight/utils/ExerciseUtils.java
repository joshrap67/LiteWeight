package com.joshrap.liteweight.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import java.util.List;

public class ExerciseUtils {

    /**
     * Attempts to launch a link of a given exercise with either the built in app (like Youtube) or browser.
     *
     * @param url     url of the link.
     * @param context current context of the calling component.
     */
    public static void launchLink(String url, Context context) {
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

    public static final String FOCUS_DELIMITER = ", ";

    /**
     * Returns comma separated string of focuses.
     *
     * @param focuses list of focuses.
     */
    public static String getFocusTitle(List<String> focuses) {
        // todo unit test
        if (focuses.isEmpty()) {
            return null;
        }
        focuses.sort(String::compareToIgnoreCase);
        return String.join(FOCUS_DELIMITER, focuses);

    }
}
