package com.example.workoutmadness;

public class Variables {
    /*
        Contains static variables and helper methods
     */
    public static final int TIME_INDEX = 0,
            TIME_TITLE_INDEX = 1,
            NAME_INDEX = 0,
            STATUS_INDEX = 1,
            VIDEO_INDEX = 1,
            WORKOUT_NAME_INDEX = 0,
            CURRENT_DAY_INDEX = 1, // TODO rename
            FOCUS_INDEX = 0,
            FOCUS_NAME_INDEX = 1,
            SETTINGS_INDEX = 0,
            SETTINGS_VALUE_INDEX = 1,
            FOCUS_INDEX_FILE = 2,
            IGNORE_WEIGHT_VALUE = -1;
    public static final double KG = 0.45359237;
    public static final String WORKOUT_DIRECTORY="Workouts",
            CURRENT_WORKOUT_LOG="currentWorkout.log",
            DEFAULT_EXERCISES_FILE ="DefaultExercises.txt",
            DEFAULT_EXERCISE_VIDEOS = "DefaultVideos.txt",
            CUSTOM_EXERCISES="CustomExercises.txt",
            EXERCISE_VIDEOS="ExerciseVideos.txt",
            STATISTICS_DIRECTORY="Statistics",
            USER_SETTINGS_DIRECTORY_NAME="UserSettings",
            USER_SETTINGS_FILE="Settings.txt",
            SPLIT_DELIM="\\*",
            FOCUS_DELIM_DB=",",
            DAY_DELIM="TIME",
            FOCUS_DELIM="FOCUS",
            VIDEO_DELIM="SHOW_VIDEOS",
            TIMER_DELIM="SHOW_TIMER",
            EXERCISE_COMPLETE ="COMPLETE",
            EXERCISE_INCOMPLETE = "INCOMPLETE",
            WORKOUT_EXT = ".txt",
            STATISTICS_EXT = ".stat",
            SHARED_PREF_NAME = "userSettings",
            VIDEO_KEY = "Videos",
            TIMER_KEY = "Timer",
            DB_KEY = "DB_EMPTY",
            UNIT_KEY="METRIC";
    public static String generateDayTitle(int num, int maxDayIndex){
        int weekNum = (num / (maxDayIndex+1))+1;
        int dayNum = (num % (maxDayIndex+1))+1;
        return "W"+weekNum+":D"+dayNum;
    }
}
