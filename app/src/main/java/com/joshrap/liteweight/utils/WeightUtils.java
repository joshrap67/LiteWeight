package com.joshrap.liteweight.utils;

import com.joshrap.liteweight.imports.Variables;

public class WeightUtils {
    /**
     * Returns the converted weight of the exercise according to whether metric units was specified.
     * Note that in DB it is stored as imperial.
     *
     * @param isMetricUnits if true, then the return value will be in metrics
     * @param weight        weight of the exercise. Always in imperial units
     * @return returns converted weight in kg if specified to return so with above parameters.
     */
    public static double getConvertedWeight(boolean isMetricUnits, double weight) {
        return (isMetricUnits) ? weight * Variables.KG_PER_LB : weight;
    }

    /**
     * Used when saving inputted metric weight to DB (stored as imperial weight).
     *
     * @param weight weight of an exercise in metric units.
     * @return converted weight in imperial units.
     */
    public static double metricWeightToImperial(double weight) {
        return weight / Variables.KG_PER_LB;
    }

    /**
     * Formats a weight to either be rounded to 0 decimal points if it's a whole number or 2 if a decimal.
     * Used as default text in EditTexts.
     *
     * @param weight weight of exercise (units not important)
     * @return formatted weight.
     */
    public static String getFormattedWeightForEditText(double weight) {
        String retVal;
        String roundedWeight = String.format("%.2f", weight);
        double roundedWeightDouble = Double.parseDouble(roundedWeight);

        String[] fractionalPoints = Double.toString(roundedWeightDouble).split("\\.");
        if ((roundedWeightDouble == Math.floor(roundedWeightDouble)) && !Double.isInfinite(roundedWeightDouble)) {
            // Weight is a whole number. don't want to show any decimals
            retVal = String.format("%.0f", roundedWeightDouble);
        } else if (fractionalPoints[1].length() == 1) {
            // lil hacky, but prevents trailing zeros if user only enters one value after decimal point
            retVal = String.format("%.1f", roundedWeightDouble);
        } else {
            retVal = roundedWeight;
        }

        return retVal;
    }

    /**
     * Returns formatted weight to at least two decimal points with units to be used on a button (e.g. 23.5 lb).
     *
     * @param weight      weight to be formatted
     * @param metricUnits whether the weight is in metric or imperial.
     * @return formatted weight with units attached to it.
     */
    public static String getFormattedWeightWithUnits(double weight, boolean metricUnits) {
        String formattedWeight = getFormattedWeightForEditText(weight);
        return String.format("%s%s", formattedWeight, metricUnits ? " kg" : " lb");
    }
}
