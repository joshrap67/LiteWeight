package com.joshrap.liteweight.Helpers;

import com.joshrap.liteweight.Database.Entities.ExerciseEntity;
import com.joshrap.liteweight.Globals.Variables;

public class WeightHelper {
    public static double convertWeight(boolean metricUnits, ExerciseEntity exercise) {
        double weight;
        if (metricUnits) {
            // value in DB is always in murican units
            weight = exercise.getCurrentWeight() * Variables.KG;
        } else {
            weight = exercise.getCurrentWeight();
        }
        return weight;
    }

    public static String getFormattedWeight(double aWeight) {
         /*
            Formats a weight to either be rounded to 0 decimal points if it's a whole number or 2 if a decimal
         */
        String retVal;
        String[] decimalPoints = Double.toString(aWeight).split("\\.");
        if ((aWeight == Math.floor(aWeight)) && !Double.isInfinite(aWeight)) {
            // Weight is a whole number. don't want to show any decimals
            retVal = String.format("%.0f", aWeight);
        } else if (decimalPoints[1].length() == 1) {
            // lil hacky, but prevents trailing zeros if user only enters one value after decimal point
            retVal = String.format("%.1f", aWeight);
        } else {
            retVal = String.format("%.2f", aWeight);
        }
        return retVal;
    }
}
