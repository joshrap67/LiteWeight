package com.joshrap.liteweight.utils;

public class StatisticsUtils {

    public static String getFormattedAverageCompleted(double averageCompleted) {
        // todo unit test
        String retVal;
        averageCompleted = averageCompleted * 100; // make average a percentage
        if ((averageCompleted == Math.floor(averageCompleted)) && !Double.isInfinite(averageCompleted)) {
            // Percentage is a whole number. don't want to show any decimals
            retVal = String.format("%s%%", String.format("%.0f", averageCompleted));
        } else {
            retVal = String.format("%s%%", String.format("%.2f", averageCompleted));
        }
        return retVal;
    }
}
