package com.joshrap.liteweight.helpers;

import com.joshrap.liteweight.database.entities.ExerciseEntity;
import com.joshrap.liteweight.imports.Variables;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class InputHelper {

    public static String validUrl(String potentialURL) {
        /*
            Ensures that a URL has the correct format. If no error, return null
         */
        potentialURL = potentialURL.trim();
        String retVal = null;
        if (potentialURL.isEmpty()) {
            retVal = "Cannot be empty!";
        } else if (potentialURL.length() > Variables.MAX_URL_LENGTH) {
            retVal = "URL is too large. Compress it and try again";
        } else {
            try {
                new URL(potentialURL).toURI();
            } catch (Exception e) {
                retVal = "Not a valid URL! Make sure to include protocol (i.e. https)";
            }
        }
        return retVal;
    }

    public static String validWorkoutName(String aName, List<String> nameList) {
        /*
            Ensures that the name is valid and doesn't already exist in a given list. If no error, return null
         */
        aName = aName.trim();
        String retVal = null;
        if ((aName.length() > 0) && (aName.length() <= Variables.MAX_WORKOUT_NAME)) {
            // check if workout name has already been used before
            for (String workout : nameList) {
                if (workout.equals(aName)) {
                    retVal = "Workout name already exists!";
                }
            }
        } else {
            retVal = String.format("Name must have 1-%s characters!", Variables.MAX_WORKOUT_NAME);
        }
        return retVal;
    }

    public static String validNewExerciseName(String exerciseName, HashMap<String, ArrayList<ExerciseEntity>> totalExercises) {
        /*
            Ensures the name is the valid number of characters and that the exercise name doesn't already exist for a focus.
            If no error, return null.
         */
        exerciseName = exerciseName.trim();
        String retVal = null;
        if (exerciseName.isEmpty()) {
            retVal = "Name cannot be empty!";
        } else if (exerciseName.length() > Variables.MAX_EXERCISE_NAME) {
            retVal = String.format("Name must have 1-%s characters!", Variables.MAX_EXERCISE_NAME);
        } else {
            // loop over default to see if this exercise already exists in some focus
            for (String focus : totalExercises.keySet()) {
                for (ExerciseEntity exercise : totalExercises.get(focus)) {
                    if (exercise.getExerciseName().equals(exerciseName)) {
                        retVal = "Exercise already exists!";
                    }
                }
            }
        }
        return retVal;
    }

    public static String validWeek(String aWeek) {
        /*
            Ensures that an inputted week is valid. If no error, return null.
         */
        aWeek = aWeek.trim();
        String retVal = null;
        if (aWeek.isEmpty()) {
            retVal = String.format("Enter value between 1-%s!", Variables.MAX_NUMBER_OF_WEEKS);
        } else {
            try {
                int week = Integer.parseInt(aWeek);
                if (week <= 0 || week > Variables.MAX_NUMBER_OF_WEEKS) {
                    retVal = String.format("Enter value between 1-%s!", Variables.MAX_NUMBER_OF_WEEKS);
                }
            } catch (Exception e) {
                retVal = "Enter a valid number.";
            }
        }
        return retVal;
    }

    public static String validDayFixedWorkout(String aDay) {
        /*
            Ensures that an inputted day is valid for a fixed workout. If no error, return null
         */
        aDay = aDay.trim();
        String retVal = null;
        if (aDay.isEmpty()) {
            retVal = String.format("Enter value between 1-%s!", Variables.WORKOUT_MAX_NUMBER_OF_DAYS);
        } else {
            try {
                int day = Integer.parseInt(aDay);
                if (day <= 0 || day > Variables.WORKOUT_MAX_NUMBER_OF_DAYS) {
                    retVal = String.format("Enter value between 1-%s!", Variables.WORKOUT_MAX_NUMBER_OF_DAYS);
                }
            } catch (Exception e) {
                retVal = "Enter a valid number.";
            }
        }
        return retVal;
    }

    public static String validDayFlexibleWorkout(String aDay) {
        /*
            Ensures that an inputted day is valid for a flexible workout. If no error, return null
         */
        aDay = aDay.trim();
        String retVal = null;
        if (aDay.isEmpty()) {
            retVal = String.format("Enter value between 1-%s!",
                    Variables.WORKOUT_MAX_NUMBER_OF_DAYS * Variables.MAX_NUMBER_OF_WEEKS);
        } else {
            try {
                int day = Integer.parseInt(aDay);
                if (day <= 0 || day > Variables.WORKOUT_MAX_NUMBER_OF_DAYS * Variables.MAX_NUMBER_OF_WEEKS) {
                    retVal = String.format("Enter value between 1-%s!",
                            Variables.WORKOUT_MAX_NUMBER_OF_DAYS * Variables.MAX_NUMBER_OF_WEEKS);
                }
            } catch (Exception e) {
                retVal = "Enter a valid number.";
            }
        }
        return retVal;
    }

    public static String validNewUsername(String username) {
        // TODO don't allow @ symbol
        username = username.trim();
        String retVal = null;
        Pattern validUsername = Pattern.compile(".*[A-Z0-9._%+-].*", Pattern.CASE_INSENSITIVE);

        if (username.isEmpty()) {
            retVal = "Username cannot be empty.";
        } else if (!validUsername.matcher(username).find()) {
            retVal = "Invalid characters.";
        }
        return retVal;
    }

    public static String validUsername(String username) {
        username = username.trim();
        String retVal = null;
        if (username.isEmpty()) {
            retVal = "Username cannot be empty.";
        }
        return retVal;
    }

    public static String validNewPassword(String password) {
        String retVal = null;
        boolean flag = false;
        if (password.length() < Variables.MIN_PASSWORD_LENGTH) {
            flag = true;
        }
        if (!Pattern.compile("^.*[a-z].*").matcher(password).find()) {
            flag = true;
        }
        if (!Pattern.compile("^.*[A-Z].*").matcher(password).find()) {
            flag = true;
        }
        if (!Pattern.compile("^.*\\d.*").matcher(password).find()) {
            flag = true;
        }
        // TODO fix this regex to actually include them all
        // TODO consider invalid chars too
        if (!Pattern.compile("^.*[!@#$%^&*()_].*").matcher(password).find()) {
            flag = true;
        }
        if (flag) {
            retVal = "Invalid password.";
        }
        return retVal;
    }

    public static String validPassword(String password) {
        password = password.trim();
        String retVal = null;
        if (password.isEmpty()) {
            retVal = "Password cannot be empty.";
        }
        return retVal;
    }

    public static String validNewEmail(String email) {
        email = email.trim();
        // regex found on SO
        Pattern validEmail = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
        String retVal = null;
        if (!validEmail.matcher(email).find()) {
            retVal = "Invalid email";
        }
        return retVal;
    }
}
