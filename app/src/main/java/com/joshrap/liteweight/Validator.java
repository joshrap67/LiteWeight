package com.joshrap.liteweight;

import com.joshrap.liteweight.Database.Entities.ExerciseEntity;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Validator {

    public static String getFormattedWeight(double aWeight){
         /*
            Formats a weight to either be rounded to 0 decimal points if it's a whole number or 2 if a decimal
         */
        String retVal;
        if ((aWeight == Math.floor(aWeight)) && !Double.isInfinite(aWeight)) {
            // Weight is a whole number. don't want to show any decimals
            retVal = String.format("%.0f", aWeight);
        }
        else{
            retVal = String.format("%.2f", aWeight);
        }
        return retVal;
    }

    public static String checkValidURL(String potentialURL){
        /*
            Ensures that a URL has the correct format
         */
        potentialURL = potentialURL.trim();
        try {
            new URL(potentialURL).toURI();
            return null;
        }
        catch (Exception e) {
            return "Not a valid URL! Make sure to include protocol (i.e. https)";
        }
    }

    public static String checkValidName(String aName, ArrayList<String> nameList){
        /*
            Ensures that the name is valid and doesn't already exist in a given list
         */
        aName = aName.trim();
        if((aName.length() > 0) && (aName.length() < 500)){
            String[] letters = aName.split("");
            for(String letter : letters){
                if(letter.equalsIgnoreCase(".")){
                    return "No special characters allowed!";
                }
            }
            // check if workout name has already been used before
            for(String workout : nameList){
                if(workout.equals(aName)){
                    return "Workout name already exists!";
                }
            }
            return null;
        }
        return "Workout name has too few or too many characters!";
    }

    public static String checkValidWeek(String aWeek) {
        /*
            Ensures that an inputted week is valid
         */
        if(aWeek.length() == 0){
            return "Enter value between 1-8!";
        }
        int week = Integer.parseInt(aWeek);
        if (week > 0 && week < 11) {
            return null;
        }
        return "Enter value between 1-10!";
    }

    public static String checkValidDay(String aDay) {
        /*
            Ensures that an inputted day is valid
         */
        if(aDay.length() == 0){
            return "Enter value between 1-7!";
        }
        int day = Integer.parseInt(aDay);
        if (day > 0 && day < 8) {
            return null;
        }
        return "Enter value between 1-7!";
    }

    public static String mostFrequentFocus(HashMap<Integer, ArrayList<String>> totalExercises,
                                         HashMap<String, ExerciseEntity> exerciseNameToEntity,
                                         ArrayList<String> focusList){
        /*
            Finds the most common focus or focuses of a workout.
         */
        HashMap<String, Integer> focusCount = new HashMap<>();
        for(String focus : focusList){
            // init each focus to have a count of 0
            focusCount.put(focus,0);
        }
        for(int i =0;i<totalExercises.size();i++){
            for(String exercise : totalExercises.get(i)){
                // for each exercise in the workout, find all the focuses associated with it and update the focus count
                String[] focuses = exerciseNameToEntity.get(exercise).getFocus().split(Variables.FOCUS_DELIM_DB);
                for(String focus : focuses){
                    focusCount.put(focus, focusCount.get(focus)+1);
                }
            }
        }
        // now find the max count
        int maxCount = 0;
        for(String focus : focusCount.keySet()){
            if(focusCount.get(focus) > maxCount){
                maxCount = focusCount.get(focus);
            }
        }
        // build the return value, if a tie, delimit the focuses with commas
        ArrayList<String> maxFocuses = new ArrayList<>();
        for(String focus : focusCount.keySet()){
            if(focusCount.get(focus) == maxCount){
                maxFocuses.add(focus);
            }
        }
        Collections.sort(maxFocuses);
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<maxFocuses.size();i++){
            sb.append(maxFocuses.get(i) + ((i == maxFocuses.size() - 1) ? "" : ","));
        }
        return sb.toString();
    }
}
