package com.joshrap.liteweight.helpers;

public class StatisticsHelper {

    public static String getFormattedAverageCompleted(double averageCompleted) {
        String retVal;
        averageCompleted = averageCompleted * 100; // make average a percentage
        if ((averageCompleted == Math.floor(averageCompleted)) && !Double.isInfinite(averageCompleted)) {
            // Percentage is a whole number. don't want to show any decimals
            retVal = String.format("%s%%", String.format("%.0f", averageCompleted));
        } else {
            retVal = String.format("%s%%", String.format("%.3f", averageCompleted));
        }
        return retVal;
    }
}
