package com.joshrap.liteweight.Helpers;

import java.net.URL;
import java.util.ArrayList;

public class InputHelper {

    public static String checkValidURL(String potentialURL) {
        /*
            Ensures that a URL has the correct format
         */
        potentialURL = potentialURL.trim();
        try {
            new URL(potentialURL).toURI();
            return null;
        } catch (Exception e) {
            return "Not a valid URL! Make sure to include protocol (i.e. https)";
        }
    }

    public static String checkValidName(String aName, ArrayList<String> nameList) {
        /*
            Ensures that the name is valid and doesn't already exist in a given list
         */
        aName = aName.trim();
        if ((aName.length() > 0) && (aName.length() < 500)) {
            String[] letters = aName.split("");
            for (String letter : letters) {
                if (letter.equalsIgnoreCase(".")) {
                    return "No special characters allowed!";
                }
            }
            // check if workout name has already been used before
            for (String workout : nameList) {
                if (workout.equals(aName)) {
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
        if (aWeek.length() == 0) {
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
        if (aDay.length() == 0) {
            return "Enter value between 1-7!";
        }
        int day = Integer.parseInt(aDay);
        if (day > 0 && day < 8) {
            return null;
        }
        return "Enter value between 1-7!";
    }
}
