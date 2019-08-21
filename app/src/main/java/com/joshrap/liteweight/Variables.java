package com.joshrap.liteweight;

public class Variables {
    /*
        Contains static variables and helper methods
     */
    public static final int
            NAME_INDEX = 0,
            VIDEO_INDEX = 1,
            FOCUS_INDEX_FILE = 2,
            IGNORE_WEIGHT_VALUE = -1,
            MAX_NUMBER_OF_WORKOUTS = 50,
            MAX_NUMBER_OF_CUSTOM_EXERCISES = 200;

    public static final double KG = 0.45359237;

    public static final String
            DEFAULT_EXERCISES_FILE = "DefaultExercises.txt",
            SPLIT_DELIM = "\\*",
            FOCUS_DELIM_DB = ",",
            SHARED_PREF_NAME = "userSettings",
            VIDEO_KEY = "Videos",
            TIMER_KEY = "Timer",
            DATABASE_NAME = "workout_db",
            DB_EMPTY_KEY = "DB_EMPTY",
            UNIT_KEY = "METRIC",
            DATE_PATTERN = "MM/dd/yyyy HH:mm:ss",
            ABOUT_TITLE = "About",
            CURRENT_WORKOUT_TITLE = "Current Workout",
            MY_WORKOUT_TITLE = "My Workouts",
            NEW_WORKOUT_TITLE = "Workout Creator",
            SETTINGS_TITLE = "Settings";

    public static String generateDayTitle(int num, int numDays) {
        int weekNum = (num / numDays) + 1;
        int dayNum = (num % numDays) + 1;
        return "W" + weekNum + ":D" + dayNum;
    }
}
