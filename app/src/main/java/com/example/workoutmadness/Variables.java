package com.example.workoutmadness;

public class Variables {
    /*
        Contains static variables and helper methods
     */
    public static final int
            NAME_INDEX = 0,
            VIDEO_INDEX = 1,
            FOCUS_INDEX_FILE = 2,
            IGNORE_WEIGHT_VALUE = -1;
    public static final double KG = 0.45359237;
    public static final String
            DEFAULT_EXERCISES_FILE = "DefaultExercises.txt",
            SPLIT_DELIM = "\\*",
            FOCUS_DELIM_DB = ",",
            SHARED_PREF_NAME = "userSettings",
            VIDEO_KEY = "Videos",
            TIMER_KEY = "Timer",
            DB_KEY = "DB_EMPTY",
            UNIT_KEY = "METRIC";
    public static String generateDayTitle(int num, int maxDayIndex){
        int weekNum = (num / (maxDayIndex+1))+1;
        int dayNum = (num % (maxDayIndex+1))+1;
        return "W"+weekNum+":D"+dayNum;
    }
}
