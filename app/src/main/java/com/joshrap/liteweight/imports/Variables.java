package com.joshrap.liteweight.imports;

import java.util.Arrays;
import java.util.List;

public class Variables {

    public static final int MAX_NUMBER_OF_FREE_EXERCISES = 100;
    public static final int MAX_NUMBER_OF_EXERCISES = 200;
    public static final int WORKOUT_MAX_NUMBER_OF_DAYS = 7;
    public static final int MAX_NUMBER_OF_WEEKS = 10;
    public static final int MAX_WORKOUT_NAME = 40;
    public static final int MAX_REPORT_DESCRIPTION = 400;
    public static final int MAX_DAY_TAG_LENGTH = 50;
    public static final int MAX_EXERCISE_NAME = 40;
    public static final int MAX_WEIGHT = 99999;
    public static final int MAX_WEIGHT_DIGITS = Integer.toString(MAX_WEIGHT).length();
    public static final int MAX_SETS = 99;
    public static final int MAX_SETS_DIGITS = Integer.toString(MAX_SETS).length();
    public static final int MAX_REPS = 999;
    public static final int MAX_REPS_DIGITS = Integer.toString(MAX_REPS).length();
    public static final int MAX_INSTRUCTIONS_LENGTH = 500;
    public static final int MAX_NOTES_LENGTH = 3000;
    public static final int MAX_LINKS = 10;
    public static final int MAX_URL_LENGTH = 200;
    public static final int MAX_LABEL_LENGTH = 100;
    public static final int MAX_USERNAME_LENGTH = 50;
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 120;
    public static final int MAX_STOPWATCH_TIME = 7200000;
    public static final int DEFAULT_TIMER_VALUE = 60000;
    public static final int MAX_FREE_WORKOUTS_SENT = 50;
    public static final int MAX_FREE_WORKOUTS = 10;
    public static final int MAX_FRIENDS = 1000;
    public static final int MAX_WORKOUTS = 20;
    public static final int DEFAULT_WEIGHT = 0;
    public static final int DEFAULT_SETS = 3;
    public static final int DEFAULT_REPS = 15;

    public static final List<String> FOCUS_LIST = Arrays.asList(
            "Back",
            "Biceps",
            "Cardio",
            "Chest",
            "Core",
            "Forearms",
            "Legs",
            "Shoulders",
            "Strength Training",
            "Triceps"
    );

    public static final double KG_PER_LB = 0.45359237; // (kg / 1 lb)

    public static final String SHARED_PREF_SETTINGS = "userSettings";

    public static final String CURRENT_WORKOUT_TITLE = "Current Workout";
    public static final String MY_WORKOUT_TITLE = "My Workouts";
    public static final String CREATE_WORKOUT_TITLE = "Create Workout";
    public static final String EDIT_WORKOUT_TITLE = "Edit Workout";
    public static final String MY_EXERCISES_TITLE = "My Exercises";
    public static final String EXERCISE_DETAILS_TITLE = "Exercise Details";
    public static final String EDIT_EXERCISE_TITLE = "Edit Exercise";
    public static final String NEW_EXERCISE_TITLE = "New Exercise";
    public static final String ACCOUNT_TITLE = "My Account";
    public static final String SETTINGS_TITLE = "Settings";
    public static final String CHANGE_PASSWORD = "Change Password";
    public static final String FRIENDS_LIST_TITLE = "Friends List";
    public static final String RECEIVED_WORKOUTS_TITLE = "Received Workouts";
    public static final String RECEIVED_WORKOUT_TITLE = "Received Workout";
    public static final String ABOUT_TITLE = "About";
    public static final String FAQ_TITLE = "FAQ";

    // notification channels
    public static final String TIMER_RUNNING_CHANNEL = "timer_running_channel";
    public static final String TIMER_FINISHED_CHANNEL = "timer_finished_channel";
    public static final String FRIEND_REQUEST_CHANNEL = "friend_request_channel";
    public static final String ACCEPTED_FRIEND_CHANNEL = "accepted_friend_channel";
    public static final String RECEIVED_WORKOUT_CHANNEL = "received_workout_channel";
    public static final String STOPWATCH_RUNNING_CHANNEL = "stopwatch_running";

    // values for navigation purposes
    public static final String FRIEND_LIST_POSITION = "friendsListPosition";
    public static final String NOTIFICATION_ACTION = "NotificationAction";
    public static final String EXERCISE_ID = "exerciseId";
    public static final String EXISTING_WORKOUT = "existingWorkout";

    // shared pref keys/values
    public static final String TIMER_ENABLED = "TimerEnabled";
    public static final String STOPWATCH_ENABLED = "StopwatchEnabled";
    public static final String LAST_CLOCK_MODE = "DefaultClock";
    public static final String STOPWATCH = "Stopwatch";
    public static final String TIMER = "Timer";
    public static final String WORKOUT_PROGRESS_KEY = "WorkoutProgressKey";
    public static final String TIMER_DURATION = "TimerValue";
    public static final String DARK_THEME_ENABLED = "DarkThemeEnabled";

    // intent keys
    public static final String INTENT_ABSOLUTE_START_TIME = "Absolute_Time";
    public static final String INTENT_TIMER_INITIAL_TIME_REMAINING = "Initial_Time_Remaining";
    public static final String INTENT_STOPWATCH_INITIAL_ELAPSED_TIME = "Initial_Elapsed_Time";
    public static final String INTENT_TIMER_DURATION = "Timer_duration";
    public static final String INTENT_TIMER_NOTIFICATION_CLICK = "Timer_notification_clicked";
    public static final String INTENT_STOPWATCH_NOTIFICATION_CLICK = "Stopwatch_notification_clicked";
    public static final String NEW_FRIEND_REQUEST_CLICK = "Friend_Request_Clicked";
    public static final String ACCEPTED_FRIEND_REQUEST_CLICK = "Accepted_Request_Clicked";
    public static final String RECEIVED_WORKOUT_CLICK = "Received_Workout_Clicked";
    public static final String INTENT_ERROR_MESSAGE = "Error_Message";
    public static final String INTENT_WORKOUT = "Workout";
    public static final String INTENT_CURRENT_WEEK = "currentWeek";
    public static final String INTENT_CURRENT_DAY = "currentDay";
    public static final String WORKOUT_NAME = "Workout_Name";
    public static final String RECEIVED_WORKOUT_ID = "ReceivedWorkoutId";

    // request codes for pending intents. These values are arbitrary
    public static final int TIMER_RUNNING_REQUEST_CODE = 67;
    public static final int TIMER_FINISHED_REQUEST_CODE = 68;
    public static final int STOPWATCH_RUNNING_REQUEST_CODE = 69;
    public static final int STOPWATCH_FINISHED_REQUEST_CODE = 70;
    public static final int FRIEND_REQUEST_CODE = 71;
    public static final int ACCEPTED_REQUEST_CODE = 72;
    public static final int RECEIVED_WORKOUT_REQUEST_CODE = 73;
}
