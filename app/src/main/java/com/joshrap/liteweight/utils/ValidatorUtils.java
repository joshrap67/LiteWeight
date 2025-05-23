package com.joshrap.liteweight.utils;

import com.joshrap.liteweight.imports.Variables;

import java.net.URL;
import java.util.List;
import java.util.regex.Pattern;

public class ValidatorUtils {

    // todo unit test all these
    private static final Pattern validUsername = Pattern.compile(".*[A-Z\\d._%+-].*", Pattern.CASE_INSENSITIVE);
    public static final String passwordNotMatchingMsg = "Passwords do not match.";

    /**
     * Ensures that a URL has the correct format.
     *
     * @param url url getting checked.
     * @return if null, no error. Else this is the specific error.
     */
    public static String validUrl(String url) {
        url = url.trim();
        String retVal = null;
        if (url.isEmpty()) {
            retVal = "URL required.";
        } else if (url.length() > Variables.MAX_URL_LENGTH) {
            retVal = "URL is too large. Compress it and try again.";
        } else {
            try {
                new URL(url).toURI();
            } catch (Exception e) {
                retVal = "Not a valid URL. Make sure to include protocol (i.e. https).";
            }
        }
        return retVal;
    }

    /**
     * Ensures that a label is valid. Note that no input is valid.
     *
     * @param label label getting validated.
     * @return If no error, return null. Else return specific error.
     */
    public static String validLinkLabel(String label) {
        label = label.trim();
        String retVal = null;
        if (label.length() > Variables.MAX_LABEL_LENGTH) {
            retVal = String.format("Enter value between 0-%s.", Variables.MAX_LABEL_LENGTH);
        }
        return retVal;
    }

    /**
     * Ensures that the workout name is valid and doesn't already exist in a given list.
     *
     * @param workoutName      workout name getting validated.
     * @param workoutNamesList list of workout names to check against.
     * @return If no error, return null. Else return specific error.
     */
    public static String validWorkoutName(String workoutName, List<String> workoutNamesList) {
        String retVal = null;
        if (workoutName.isEmpty()) {
            retVal = "Name required.";
        } else if (workoutName.length() > Variables.MAX_WORKOUT_NAME) {
            retVal = String.format("Name must have not exceed %s characters.", Variables.MAX_WORKOUT_NAME);
        } else if (workoutNamesList.stream().anyMatch(x -> x.equals(workoutName))) {
            retVal = "Name already exists.";
        }
        return retVal;
    }

    /**
     * Ensures that the report description is valid
     *
     * @param description description for the user's complaint.
     * @return If no error, return null. Else return specific error.
     */
    public static String validReportUserDescription(String description) {
        description = description.trim();
        String retVal = null;
        if (description.isEmpty()) {
            retVal = "Description required.";
        } else if (description.length() > Variables.MAX_REPORT_DESCRIPTION) {
            retVal = String.format("Description must not exceed %s characters.", Variables.MAX_REPORT_DESCRIPTION);
        }
        return retVal;
    }

    /**
     * Ensures the exercise name has the valid number of characters and that the exercise name
     * doesn't already exist for a given list of exercises.
     *
     * @param exerciseName   exercise name getting validated.
     * @param totalExercises list of exercise names to check against.
     * @return If no error, return null. Else return specific error.
     */
    public static String validNewExerciseName(String exerciseName, List<String> totalExercises) {
        String retVal = null;
        if (exerciseName.isEmpty()) {
            retVal = "Name required.";
        } else if (exerciseName.length() > Variables.MAX_EXERCISE_NAME) {
            retVal = String.format("Name must not exceed %s characters.", Variables.MAX_EXERCISE_NAME);
        } else if (totalExercises.stream().anyMatch(x -> x.equals(exerciseName))) {
            retVal = "Exercise already exists.";
        }
        return retVal;
    }

    /**
     * Ensures a weight is the right size and is indeed a number.
     *
     * @param weightString weight getting validated.
     * @return If no error, return null. Else return specific error.
     */
    public static String validWeight(String weightString) {
        weightString = weightString.trim();
        String retVal = null;
        if (weightString.isEmpty()) {
            retVal = "Weight required.";
        } else {
            try {
                double weight = Double.parseDouble(weightString);
                if (weight < 0 || weight > Variables.MAX_WEIGHT) {
                    retVal = String.format("Enter value between 0-%s.", Variables.MAX_WEIGHT);
                }
            } catch (Exception e) {
                retVal = "Enter a valid number.";
            }
        }
        return retVal;
    }

    /**
     * Ensures the sets are the right size and are indeed a number.
     *
     * @param setsString weight getting validated.
     * @return If no error, return null. Else return specific error.
     */
    public static String validSets(String setsString) {
        setsString = setsString.trim();
        String retVal = null;
        if (setsString.isEmpty()) {
            retVal = "Sets required.";
        } else {
            try {
                int sets = Integer.parseInt(setsString);
                if (sets < 0 || sets > Variables.MAX_SETS) {
                    retVal = String.format("Enter value between 0-%s.", Variables.MAX_SETS);
                }
            } catch (Exception e) {
                retVal = "Enter a valid number.";
            }
        }
        return retVal;
    }

    /**
     * Ensures the reps are the right size and are indeed a number.
     *
     * @param repsString weight getting validated.
     * @return If no error, return null. Else return specific error.
     */
    public static String validReps(String repsString) {
        repsString = repsString.trim();
        String retVal = null;
        if (repsString.isEmpty()) {
            retVal = "Reps required.";
        } else {
            try {
                int reps = Integer.parseInt(repsString);
                if (reps < 0 || reps > Variables.MAX_REPS) {
                    retVal = String.format("Enter value between 0-%s.", Variables.MAX_REPS);
                }
            } catch (Exception e) {
                retVal = "Enter a valid number.";
            }
        }
        return retVal;
    }

    /**
     * Ensures that a given set of instructions are valid. Note that no input is valid.
     *
     * @param instructions instructions getting validated.
     * @return If no error, return null. Else return specific error.
     */
    public static String validInstructions(String instructions) {
        instructions = instructions.trim();
        String retVal = null;
        if (instructions.length() > Variables.MAX_INSTRUCTIONS_LENGTH) {
            retVal = String.format("Enter value between 0-%s.", Variables.MAX_INSTRUCTIONS_LENGTH);
        }
        return retVal;
    }

    /**
     * Ensures that a given note is valid. No input is valid.
     *
     * @param notes notes getting validated.
     * @return If no error, return null. Else return specific error.
     */
    public static String validNotes(String notes) {
        notes = notes.trim();
        String retVal = null;
        if (notes.length() > Variables.MAX_NOTES_LENGTH) {
            retVal = String.format("Enter value between 0-%s.", Variables.MAX_NOTES_LENGTH);
        }
        return retVal;
    }

    public static String validNewUsername(String username) {
        username = username.trim();
        String retVal = null;

        if (username.isEmpty()) {
            retVal = "Username required.";
        } else if (!validUsername.matcher(username).find()) {
            retVal = "Invalid characters.";
        } else if (Pattern.compile("\\s").matcher(username).find()) {
            retVal = "Username cannot have any whitespace.";
        } else if (username.length() > Variables.MAX_USERNAME_LENGTH) {
            retVal = String.format("Enter value between 1-%s.", Variables.MAX_USERNAME_LENGTH);
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
        if (password.length() > Variables.MAX_PASSWORD_LENGTH) {
            errorMsg += "Too many characters.\n";
        }
        if (Pattern.compile("\\s").matcher(password).find()) {
            errorMsg += "Cannot have any whitespace.\n";
        }

        if (!errorMsg.isEmpty()) {
            retVal = errorMsg;
        }
        return retVal;
    }

    public static String validUsername(String username) {
        username = username.trim();
        String retVal = null;
        if (username.isEmpty()) {
            retVal = "Username required.";
        }
        return retVal;
    }

    public static String validEmail(String email) {
        email = email.trim();
        String retVal = null;
        if (email.isEmpty()) {
            retVal = "Email required.";
        }
        return retVal;
    }

    public static String validPassword(String password) {
        password = password.trim();
        String retVal = null;
        if (password.isEmpty()) {
            retVal = "Password required.";
        }
        return retVal;
    }

    public static String validNewEmail(String email) {
        email = email.trim();
        // regex found on SO
        Pattern validEmail = Pattern.compile("^[A-Z\\d._%+-]+@[A-Z\\d.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
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
            } else if (existingFriends.size() >= Variables.MAX_FRIENDS) {
                retVal = "You're too popular! You have reached the maximum number of friends allowed at this time.";
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
