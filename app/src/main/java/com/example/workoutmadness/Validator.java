package com.example.workoutmadness;

import android.app.Activity;

import java.io.File;
import java.util.ArrayList;

public class Validator {
    // TODO will use this class for any and all user input validation
    private Activity activity;

    public Validator(Activity anActivity){
        activity=anActivity;
    }

    public String checkValidName(String aName, ArrayList<String> workoutNames){
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

    public String checkValidWeek(String aWeek) {
        if(aWeek.length() == 0){
            return "Enter value between 1-8!";
        }
        int week = Integer.parseInt(aWeek);
        if (week > 0 && week < 9) {
            return null;
        }
        return "Enter value between 1-8!";
    }

    public String checkValidDay(String aDay) {
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
