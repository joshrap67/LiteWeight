package com.joshrap.liteweight.utils;

import com.joshrap.liteweight.models.user.WorkoutInfo;
import com.joshrap.liteweight.models.workout.Routine;
import com.joshrap.liteweight.models.workout.RoutineDay;
import com.joshrap.liteweight.models.workout.RoutineExercise;
import com.joshrap.liteweight.models.workout.RoutineWeek;
import com.joshrap.liteweight.models.user.User;

import java.util.HashMap;
import java.util.Map;

public class WorkoutUtils {
    /**
     * Generates a day title in a standard format. E.g. W1:D2
     *
     * @param currentWeekIndex current week index of the routine.
     * @param currentDayIndex  current day index of the routine.
     * @return formatted day title.
     */
    public static String generateDayTitle(int currentWeekIndex, int currentDayIndex) {
        return "Week " + (currentWeekIndex + 1) + " Day " + (currentDayIndex + 1);
    }

    // todo unit test

    /**
     * Gets the most frequent focus for a given routine.
     *
     * @param user    user containing the current exercises and their focuses
     * @param routine routine to get the most frequent focus for
     * @return comma separated string with most frequent focuses
     */
    public static String getMostFrequentFocus(final User user, final Routine routine) {
        Map<String, Integer> focusCount = new HashMap<>();
        for (RoutineWeek week : routine) {
            for (RoutineDay day : week) {
                for (RoutineExercise routineExercise : day) {
                    String exerciseId = routineExercise.getExerciseId();
                    for (String focus : user.getExercise(exerciseId).getFocuses()) {
                        focusCount.merge(focus, 1, Integer::sum);
                    }
                }
            }
        }

        String retVal = "";
        int max = 0;
        for (String focus : focusCount.keySet()) {
            int count = focusCount.get(focus);
            if (count > max) {
                max = count;
            }
        }
        for (String focus : focusCount.keySet()) {
            int count = focusCount.get(focus);
            if (count == max) {
                // previously I was merging all that matched the count but looks bad on UI so just return first occurrence of the max
                retVal = focus;
                break;
            }
        }
        return retVal;
    }

    // as a result of editing the routine, it is possible the current week/index are out of bounds
    public static void checkCurrentWeekAndDay(WorkoutInfo workoutInfo, Routine routine) {
        int currentWeek = workoutInfo.getCurrentWeek();
        int currentDay = workoutInfo.getCurrentDay();

        if (currentWeek >= routine.getWeeks().size()) {
            int newWeekIndex = routine.getWeeks().size() - 1;
            if (newWeekIndex < 0) {
                newWeekIndex = 0;
            }
            int newDayIndex = routine.get(newWeekIndex).getDays().size() - 1;
            if (newDayIndex < 0) {
                newDayIndex = 0;
            }
            workoutInfo.setCurrentDay(newDayIndex);
            workoutInfo.setCurrentWeek(newWeekIndex);
        } else if (currentDay >= routine.get(currentWeek).getDays().size()) {
            int newDayIndex = routine.get(currentWeek).getDays().size() - 1;
            if (newDayIndex < 0) {
                newDayIndex = 0;
            }
            workoutInfo.setCurrentDay(newDayIndex);
        }
    }
}
