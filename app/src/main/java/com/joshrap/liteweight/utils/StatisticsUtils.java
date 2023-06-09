package com.joshrap.liteweight.utils;

import java.util.Locale;

public class StatisticsUtils {

    public static String getFormattedAverageCompleted(double averageCompleted) {
        // todo unit test
        String retVal;
        averageCompleted = averageCompleted * 100; // make average a percentage
        if ((averageCompleted == Math.floor(averageCompleted)) && !Double.isInfinite(averageCompleted)) {
            // Percentage is a whole number. don't want to show any decimals
            retVal = String.format(Locale.getDefault(), "%s%%", String.format(Locale.getDefault(), "%.0f", averageCompleted));
        } else {
            retVal = String.format(Locale.getDefault(), "%s%%", String.format(Locale.getDefault(), "%.2f", averageCompleted));
        }
        return retVal;
    }
}
