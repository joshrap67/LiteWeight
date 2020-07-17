package com.joshrap.liteweight.imports;

public class Variables {
    /*
        Contains read only static variables
     */
    public static final int
            NAME_INDEX = 0,
            VIDEO_INDEX = 1,
            FOCUS_INDEX_FILE = 2,
            IGNORE_WEIGHT_VALUE = -1,
            MAX_NUMBER_OF_WORKOUTS = 40,
            MAX_NUMBER_OF_CUSTOM_EXERCISES = 200,
            FIXED_WORKOUT_MAX_NUMBER_OF_DAYS = 7,
            MAX_NUMBER_OF_WEEKS = 10,
            MAX_WORKOUT_NAME = 40,
            MAX_EXERCISE_NAME = 40,
            MAX_WEIGHT_DIGITS = 5,
            MAX_URL_LENGTH = 1000,
            MAX_USERNAME_LENGTH = 40,
            MIN_PASSWORD_LENGTH = 8,
            MAX_STOPWATCH_TIME = 7200000,
            DEFAULT_TIMER_VALUE = 60000;

    public static final double KG = 0.45359237; // (kg / 1 lb)

    public static final String
            DEFAULT_EXERCISES_FILE = "DefaultExercises.txt",
            SPLIT_DELIM = "\\*",
            FOCUS_DELIM_DB = ",",
            SHARED_PREF_SETTINGS = "userSettings",
            VIDEO_KEY = "Videos",
            STOPWATCH = "Stopwatch",
            TIMER = "Timer",
            TIMER_DURATION = "TimerValue",
            LAST_CLOCK_MODE = "DefaultClock",
            TIMER_ENABLED = "TimerEnabled",
            STOPWATCH_ENABLED = "StopwatchEnabled",
            DATABASE_NAME = "workout_db",
            DB_EMPTY_KEY = "DB_EMPTY",
            UNIT_KEY = "METRIC",
            DATE_PATTERN = "MM/dd/yyyy HH:mm:ss",
            ABOUT_TITLE = "About",
            CURRENT_WORKOUT_TITLE = "Current Workout",
            MY_EXERCISES_TITLE = "My Exercises",
            MY_WORKOUT_TITLE = "My Workouts",
            NEW_WORKOUT_TITLE = "Create Workout",
            SETTINGS_TITLE = "Settings",
            EDIT_WORKOUT_TITLE = "Edit Workout",
            TIMER_RUNNING_CHANNEL = "timer_running_channel",
            TIMER_FINISHED_CHANNEL = "timer_finished_channel",
            STOPWATCH_RUNNING_CHANNEL = "stopwatch_running",
            WORKOUT_TYPE_PREF_KEY = "WorkoutType",
            WORKOUT_FIXED = "FixedWorkout",
            WORKOUT_FLEXIBLE = "FlexibleWorkout";

    // intent keys
    public static final String
            INTENT_TIMER_ABSOLUTE_START_TIME = "Absolute_Time",
            INTENT_TIMER_TIME_ON_CLOCK = "Time_on_clock",
            INTENT_TIMER_NOTIFICATION_CLICK = "Timer_notification_clicked",
            INTENT_STOPWATCH_NOTIFICATION_CLICK = "Stopwatch_notification_clicked";
}
