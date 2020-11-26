package com.joshrap.liteweight.utils;

import com.joshrap.liteweight.models.Routine;

public class WorkoutUtils {

    public static void deleteExerciseFromRoutine(final String exerciseId, final Routine routine) {
        if (routine == null) {
            return;
        }
        for (int week : routine) {
            for (int day : routine.getWeek(week)) {
                routine.removeExercise(week, day, exerciseId);
            }
        }
    }

    /**
     * Generates a day title in a standard format. E.g. W1:D2
     *
     * @param currentWeekIndex current week index of the routine.
     * @param currentDayIndex  current day index of the routine.
     * @return formatted day title.
     */
    public static String generateDayTitle(int currentWeekIndex, int currentDayIndex) {
        return "W" + (currentWeekIndex + 1) + ":D" + (currentDayIndex + 1);
    }
}
