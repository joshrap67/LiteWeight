package com.joshrap.liteweight.helpers;

import com.joshrap.liteweight.imports.Variables;

public class WorkoutHelper {
    public static String generateDayTitle(int currentDayIndex, int daysPerWeek, String workoutType) {
        /*
            Generates a day title for a workout.
            If a fixed workout then it will be in the format: W<#>:D<#>.
            If flexible, then it will be in the format: Day:<#>
         */
        String retVal = null;
        if (workoutType.equals(Variables.WORKOUT_FLEXIBLE)) {
            // for flexible workouts we just pretend it is a giant one week workout
            retVal = "Day " + (currentDayIndex + 1);
        } else if (workoutType.equals(Variables.WORKOUT_FIXED)) {
            int weekNum = (currentDayIndex / daysPerWeek) + 1;
            int dayNum = (currentDayIndex % daysPerWeek) + 1;
            retVal = "W" + weekNum + ":D" + dayNum;
        }
        return retVal;
    }
}
