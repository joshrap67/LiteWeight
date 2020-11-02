package com.joshrap.liteweight.helpers;

import com.joshrap.liteweight.imports.Variables;

public class WeightHelper {
    public static double getConvertedWeight(boolean isMetricUnits, double weight) {
        // the weight is in imperial units
        // returns current weight of exercise either in lbs or kgs (since in DB its stored in lbs)
        return (isMetricUnits) ? weight * Variables.KG_PER_LB : weight;
    }

    public static double metricWeightToImperial(double weight) {
        // used when saving inputted metric weight to DB
        return weight / Variables.KG_PER_LB;
    }

    public static String getFormattedWeightForInput(double aWeight) {
         /*
            Formats a weight to either be rounded to 0 decimal points if it's a whole number or 2 if a decimal.
            To be used as default text in editTexts
         */
        // todo just do it the long way since if i have a weight as 10.000005, it will display 10.00
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

    public static String getFormattedWeightWithUnits(double aWeight, boolean metricUnits) {
         /*
            Formats a weight to either be rounded to 0 decimal points if it's a whole number or 2 if a decimal
         */
        String retVal;
        String[] decimalPoints = Double.toString(aWeight).split("\\.");
        if (aWeight < 0) {
            retVal = "None";
        } else if ((aWeight == Math.floor(aWeight)) && !Double.isInfinite(aWeight)) {
            // Weight is a whole number. don't want to show any decimals
            retVal = String.format("%s%s", String.format("%.0f", aWeight), metricUnits ? " kg" : " lb");
        } else if (decimalPoints[1].length() == 1) {
            // lil hacky, but prevents trailing zeros if user only enters one value after decimal point
            retVal = String.format("%s%s", String.format("%.1f", aWeight), metricUnits ? " kg" : " lb");
        } else {
            retVal = String.format("%s%s", String.format("%.2f", aWeight), metricUnits ? " kg" : " lb");
        }
        return retVal;
    }
}
