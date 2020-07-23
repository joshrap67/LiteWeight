package com.joshrap.liteweight.helpers;

import com.joshrap.liteweight.database.entities.ExerciseEntity;
import com.joshrap.liteweight.imports.Variables;

public class WeightHelper {
    public static double getConvertedWeight(boolean metricUnits, ExerciseEntity exercise) {
        // returns current weight of exercise either in lbs or kgs
        return (metricUnits) ? exercise.getCurrentWeight() * Variables.KG : exercise.getCurrentWeight();
    }

    public static String validWeight(String weightString) {
        /*
            Checks if a given weight (from a text input so it is a string) is valid. Return null if valid
         */
        String retVal = null;
        weightString = weightString.trim();
        if (weightString.isEmpty()) {
            retVal = "Weight cannot be empty.";
        } else {
            try {
                Double.parseDouble(weightString);
            } catch (Exception e) {
                retVal = "Invalid weight.";
            }
        }
        return retVal;
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
