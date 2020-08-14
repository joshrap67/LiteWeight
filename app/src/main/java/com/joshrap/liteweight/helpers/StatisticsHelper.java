package com.joshrap.liteweight.helpers;

import com.joshrap.liteweight.database.entities.MetaEntity;
import com.joshrap.liteweight.database.viewModels.MetaViewModel;

public class StatisticsHelper {

    public static void workoutResetStatistics(MetaEntity metaEntity, MetaViewModel metaViewModel,
                                              int exercisesCompleted, int totalExercises) {
        /*
            Calculates the relevant statistics for when a workout is restarted.
         */
        // calculate average percentage of exercises completed over all times the workout has been completed
        int totalCompleted = metaEntity.getCompletedSum() + exercisesCompleted;
        int totalSum = metaEntity.getTotalSum() + totalExercises;
        if (totalSum == 0) {
            // only happens if user somehow deletes all exercises and never restarted the workout before
            double percentage = 0.0;

            metaEntity.setCurrentDay(0);
            metaEntity.setTimesCompleted(metaEntity.getTimesCompleted() + 1);
            metaEntity.setTotalSum(totalSum);
            metaEntity.setCompletedSum(totalCompleted);
            metaEntity.setPercentageExercisesCompleted(percentage);
            metaViewModel.update(metaEntity);
        } else {
            double percentage = ((double) totalCompleted / (double) totalSum) * 100;

            metaEntity.setCurrentDay(0);
            metaEntity.setTimesCompleted(metaEntity.getTimesCompleted() + 1);
            metaEntity.setTotalSum(totalSum);
            metaEntity.setCompletedSum(totalCompleted);
            metaEntity.setPercentageExercisesCompleted(percentage);
            metaViewModel.update(metaEntity);
        }
    }

    public static String getFormattedPercentageCompleted(double percentageCompleted) {
        String retVal;
        if ((percentageCompleted == Math.floor(percentageCompleted)) && !Double.isInfinite(percentageCompleted)) {
            // Percentage is a whole number. don't want to show any decimals
            retVal = String.format("%s%%", String.format("%.0f", percentageCompleted));
        } else {
            retVal = String.format("%s%%", String.format("%.3f", percentageCompleted));
        }
        return retVal;
    }

    public static void resetEntireWorkout(MetaEntity metaEntity, MetaViewModel metaViewModel) {
        /*
            Resets all statistics of a given workout.
         */
        metaEntity.setPercentageExercisesCompleted(0.0);
        metaEntity.setCompletedSum(0);
        metaEntity.setTimesCompleted(0);
        metaEntity.setTotalSum(0);
        metaViewModel.update(metaEntity);
    }
}
