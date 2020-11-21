package com.joshrap.liteweight.helpers;

import com.joshrap.liteweight.imports.Variables;

import java.net.URL;
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
            retVal = "URL cannot be empty.";
        } else if (potentialURL.length() > Variables.MAX_URL_LENGTH) {
            retVal = "URL is too large. Compress it and try again.";
        } else {
            try {
                new URL(potentialURL).toURI();
            } catch (Exception e) {
                retVal = "Not a valid URL! Make sure to include protocol (i.e. https).";
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
                    retVal = "Workout name already exists.";
                }
            }
        } else {
            retVal = String.format("Name must have 1-%s characters.", Variables.MAX_WORKOUT_NAME);
        }
        return retVal;
    }

    public static String validNewExerciseName(String exerciseName, List<String> totalExercises) {
        /*
            Ensures the name is the valid number of characters and that the exercise name doesn't already exist for a focus.
            If no error, return null.
         */
        exerciseName = exerciseName.trim();
        String retVal = null;
        if (exerciseName.isEmpty()) {
            retVal = "Name cannot be empty.";
        } else if (exerciseName.length() > Variables.MAX_EXERCISE_NAME) {
            retVal = String.format("Name must have 1-%s characters.", Variables.MAX_EXERCISE_NAME);
        } else {
            // loop over default to see if this exercise already exists in some focus
            for (String exercise : totalExercises) {
                if (exercise.equals(exerciseName)) {
                    retVal = "Exercise already exists.";
                }
            }
        }
        return retVal;
    }

    public static String validWeight(String aWeight) {
        /*
            Ensures that an inputted weight is valid. If no error, return null.
         */
        aWeight = aWeight.trim();
        String retVal = null;
        if (aWeight.isEmpty()) {
            retVal = "Weight cannot be empty";
        } else {
            try {
                double weight = Double.parseDouble(aWeight);
                if (weight < 0 || weight > Variables.MAX_WEIGHT) {
                    retVal = String.format("Enter value between 1-%s.", Variables.MAX_WEIGHT);
                }
            } catch (Exception e) {
                retVal = "Enter a valid number.";
            }
        }
        return retVal;
    }

    public static String validSets(String aSets) {
        /*
            Ensures that an inputted sets is valid. If no error, return null.
         */
        aSets = aSets.trim();
        String retVal = null;
        if (aSets.isEmpty()) {
            retVal = "Sets cannot be empty";
        } else {
            try {
                int sets = Integer.parseInt(aSets);
                if (sets < 0 || sets > Variables.MAX_SETS) {
                    retVal = String.format("Enter value between 1-%s.", Variables.MAX_SETS);
                }
            } catch (Exception e) {
                retVal = "Enter a valid number.";
            }
        }
        return retVal;
    }

    public static String validReps(String aReps) {
        /*
            Ensures that an inputted reps is valid. If no error, return null.
         */
        aReps = aReps.trim();
        String retVal = null;
        if (aReps.isEmpty()) {
            retVal = "Sets cannot be empty";
        } else {
            try {
                int reps = Integer.parseInt(aReps);
                if (reps < 0 || reps > Variables.MAX_REPS) {
                    retVal = String.format("Enter value between 1-%s.", Variables.MAX_REPS);
                }
            } catch (Exception e) {
                retVal = "Enter a valid number.";
            }
        }
        return retVal;
    }

    public static String validDetails(String details) {
        /*
            Ensures that an inputted details is valid. Note no input is valid. If no error, return null.
         */
        details = details.trim();
        String retVal = null;
        if (details.length() > Variables.MAX_DETAILS_LENGTH) {
            retVal = String.format("Enter value between 0-%s.", Variables.MAX_DETAILS_LENGTH);
        }
        return retVal;
    }

    public static String validNewUsername(String username) {
        // TODO don't allow @ symbol
        // Todo check length
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
        String errorMsg = "";
        if (password.length() < Variables.MIN_PASSWORD_LENGTH) {
            errorMsg += "Must have at least " + Variables.MIN_PASSWORD_LENGTH + " characters.\n";
        }
        if (!Pattern.compile("^.*[a-z].*").matcher(password).find()) {
            errorMsg += "Must have at least one lowercase letter.\n";
        }
        if (!Pattern.compile("^.*[A-Z].*").matcher(password).find()) {
            errorMsg += "Must have at least one uppercase letter.\n";
        }
        if (!Pattern.compile("^.*\\d.*").matcher(password).find()) {
            errorMsg += "Must have at least one number.\n";
        }
        // TODO fix this regex to actually include them all
        // TODO consider invalid chars too
        if (!Pattern.compile("^.*[!@#$%^&*()_].*").matcher(password).find()) {
            errorMsg += "Must have at least one special character.\n";
        }
        if (!errorMsg.isEmpty()) {
            retVal = errorMsg;
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
            retVal = "Invalid email.";
        }
        return retVal;
    }

    public static String validNewFriend(String activeUser, String username,
                                        List<String> existingFriends, List<String> existingFriendRequests) {
        username = username.trim();
        String retVal = validUsername(username);
        if (retVal == null) {
            // means no issue with base username
            if (existingFriendRequests.contains(username)) {
                retVal = "Request already sent.";
            } else if (existingFriends.contains(username)) {
                retVal = "Already friends with this user.";
            } else if (activeUser.equals(username)) {
                retVal = "Can't be friends with yourself. Sorry.";
            }
        }
        return retVal;
    }

    public static String validUserToBlock(String activeUser, String username, List<String> users) {
        username = username.trim();
        String retVal = validUsername(username);
        if (retVal == null) {
            // means no issue with base username
            if (users.contains(username)) {
                retVal = "User already blocked.";
            } else if (activeUser.equals(username)) {
                retVal = "If only it were that simple to block yourself.";
            }
        }
        return retVal;
    }

    public static String validUserToSendWorkout(String activeUser, String username) {
        username = username.trim();
        String retVal = validUsername(username);
        if (retVal == null) {
            // means no issue with base username
            if (activeUser.equals(username)) {
                retVal = "Cannot send a workout to yourself.";
            }
        }
        return retVal;
    }
}
