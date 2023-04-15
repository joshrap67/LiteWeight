package com.joshrap.liteweight.utils;

import com.joshrap.liteweight.imports.Variables;

import java.net.URL;
import java.util.List;
import java.util.regex.Pattern;

public class ValidatorUtils {

    // todo unit test all these
    private static final Pattern validUsername = Pattern.compile(".*[A-Z\\d._%+-].*", Pattern.CASE_INSENSITIVE);

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
            retVal = "URL cannot be empty.";
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
     * Ensures that the workout name is valid and doesn't already exist in a given list.
     *
     * @param workoutName      workout name getting validated.
     * @param workoutNamesList list of workout names to check against.
     * @return If no error, return null. Else return specific error.
     */
    public static String validWorkoutName(String workoutName, List<String> workoutNamesList) {
        workoutName = workoutName.trim();
        String retVal = null;
        if ((workoutName.length() > 0) && (workoutName.length() <= Variables.MAX_WORKOUT_NAME)) {
            // check if workout name has already been used before
            for (String workout : workoutNamesList) {
                if (workout.equals(workoutName)) {
                    retVal = "Workout name already exists.";
                    break;
                }
            }
        } else {
            retVal = String.format("Name must have 1-%s characters.", Variables.MAX_WORKOUT_NAME);
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
        exerciseName = exerciseName.trim();
        String retVal = null;
        if (exerciseName.isEmpty()) {
            retVal = "Name cannot be empty.";
        } else if (exerciseName.length() > Variables.MAX_EXERCISE_NAME) {
            retVal = String.format("Name must have 1-%s characters.", Variables.MAX_EXERCISE_NAME);
        } else {
            for (String exercise : totalExercises) {
                if (exercise.equals(exerciseName)) {
                    retVal = "Exercise already exists.";
                    break;
                }
            }
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
            retVal = "Weight cannot be empty.";
        } else {
            try {
                double weight = Double.parseDouble(weightString);
                if (weight < 0 || weight > Variables.MAX_WEIGHT) {
                    retVal = String.format("Enter value between 1-%s.", Variables.MAX_WEIGHT);
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
            retVal = "Sets cannot be empty.";
        } else {
            try {
                int sets = Integer.parseInt(setsString);
                if (sets < 0 || sets > Variables.MAX_SETS) {
                    retVal = String.format("Enter value between 1-%s.", Variables.MAX_SETS);
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
            retVal = "Reps cannot be empty.";
        } else {
            try {
                int reps = Integer.parseInt(repsString);
                if (reps < 0 || reps > Variables.MAX_REPS) {
                    retVal = String.format("Enter value between 1-%s.", Variables.MAX_REPS);
                }
            } catch (Exception e) {
                retVal = "Enter a valid number.";
            }
        }
        return retVal;
    }

    /**
     * Ensures that a given set of details are valid. Note that no input is valid.
     *
     * @param details details getting validated.
     * @return If no error, return null. Else return specific error.
     */
    public static String validDetails(String details) {
        details = details.trim();
        String retVal = null;
        if (details.length() > Variables.MAX_DETAILS_LENGTH) {
            retVal = String.format("Enter value between 0-%s.", Variables.MAX_DETAILS_LENGTH);
        }
        return retVal;
    }

    public static String validNewUsername(String username) {
        username = username.trim();
        String retVal = null;

        if (username.isEmpty()) {
            retVal = "Username cannot be empty.";
        } else if (username.contains("@")) {
            retVal = "Username cannot have \"@\" symbol.";
        } else if (!validUsername.matcher(username).find()) {
            retVal = "Invalid characters.";
        } else if (Pattern.compile("\\s").matcher(username).find()) {
            retVal = "Username cannot have any whitespace.";
        } else if (username.length() > Variables.MAX_USERNAME_LENGTH) {
            retVal = String.format("Enter value between 0-%s.", Variables.MAX_USERNAME_LENGTH);
        }
        return retVal;
    }

    public static String validNewPassword(String password) {
        password = password.trim();
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
        if (!Pattern.compile("^.*[\\^$*.\\[\\]{}()?\\-\"!@#%&/\\\\,><’:;|_~`+=].*").matcher(password).find()) {
            errorMsg += "Must have at least one special character.\n";
        }
        // invalid checks
        if (password.length() > Variables.MAX_PASSWORD_LENGTH) {
            errorMsg += "Too many characters.\n";
        }
        if (Pattern.compile("\\s").matcher(password).find()) {
            errorMsg += "Cannot have any whitespace.\n";
        }
        if (errorMsg.isEmpty() && !Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\^$*.\\[\\]{}()?\\-\"!@#%&/\\\\,><’:;|_~`+=])\\S{8,99}$").matcher(password).find()) {
            // sanity check to make sure no invalid characters.
            errorMsg += "Invalid character detected.";
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
            retVal = "Username cannot be empty.";
        } else if (!validUsername.matcher(username).find()) {
            retVal = "Invalid characters.";
        } else if (Pattern.compile("\\s").matcher(username).find()) {
            retVal = "Username cannot have any whitespace.";
        } else if (username.length() > Variables.MAX_USERNAME_LENGTH) {
            retVal = String.format("Enter value between 0-%s.", Variables.MAX_USERNAME_LENGTH);
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

    public static String validFeedback(String feedback) {
        feedback = feedback.trim();
        String retVal = null;
        if (feedback.isEmpty()) {
            retVal = "Feedback cannot be empty.";
        } else if (feedback.length() > Variables.MAX_FEEDBACK) {
            retVal = "Feedback is too large.";
        }
        return retVal;
    }
}
