package com.joshrap.liteweight.helpers;

import com.joshrap.liteweight.models.Routine;

public class WorkoutHelper {
    public static void deleteExerciseFromRoutine(final String exerciseId, final Routine routine) {
        if (routine == null) {
            return;
        }
        for (int week = 0; week < routine.size(); week++) {
            for (int day = 0; day < routine.getWeek(week).size(); day++) {
                routine.removeExercise(week, day, exerciseId);
            }
        }
    }

    public static String generateDayTitleNew(int currentWeekIndex, int currentDayIndex) {
        /*
            Generates a day title for a workout.
         */
        return "W" + (currentWeekIndex + 1) + ":D" + (currentDayIndex + 1);
    }
}
