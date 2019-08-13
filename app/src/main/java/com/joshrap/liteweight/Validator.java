package com.joshrap.liteweight;

import android.util.Log;

import java.net.URL;
import java.util.ArrayList;

public class Validator {
    // TODO will use this class for any and all user input validation
    public static String getFormattedWeight(double aWeight){
         /*
            Formats a weight to either be rounded to 0 decimal points if it's a whole number or 2 if a decimal
         */
        String retVal;
        if ((aWeight == Math.floor(aWeight)) && !Double.isInfinite(aWeight)) {
            // integer type
            retVal = String.format("%.0f", aWeight);
        }
        else{
            retVal = String.format("%.2f", aWeight);
        }
        return retVal;
    }
    public static String checkValidURL(String potentialURL){
        try {
            new URL(potentialURL).toURI();
            return null;
        }
        catch (Exception e) {
            Log.d("TAG","Exception "+e);
            return "Not a valid URL! Make sure to include protocol (i.e. https)";
        }
    }
    public static String checkValidName(String aName, ArrayList<String> workoutNames){
        aName = aName.trim();
        if((aName.length() > 0) && (aName.length() < 500)){
            String[] letters = aName.split("");
            for(String letter : letters){
                if(letter.equalsIgnoreCase(".")){
                    return "No special characters allowed!";
                }
            }
            // check if workout name has already been used before
            for(String workout : workoutNames){
                if(workout.equals(aName)){
                    return "Workout name already exists!";
                }
            }
            return null;
        }
        return "Workout name has too few or too many characters!";
    }

    public static String checkValidWeek(String aWeek) {
        if(aWeek.length() == 0){
            return "Enter value between 1-8!";
        }
        int week = Integer.parseInt(aWeek);
        if (week > 0 && week < 9) {
            return null;
        }
        return "Enter value between 1-8!";
    }

    public static String checkValidDay(String aDay) {
        if(aDay.length() == 0){
            return "Enter value between 1-7!";
        }
        int day = Integer.parseInt(aDay);
        if (day > 0 && day < 8) {
            return null;
        }
        return "Enter value between 1-7!";
    }
}
