package com.joshrap.liteweight.helpers;

public class WorkoutHelper {
    public static String generateDayTitle(int num, int numDays) {
        /*
            Generates a day title for a workout given the day number and the total number of days in the workout
         */
        int weekNum = (num / numDays) + 1;
        int dayNum = (num % numDays) + 1;
        return "W" + weekNum + ":D" + dayNum;
    }
}
