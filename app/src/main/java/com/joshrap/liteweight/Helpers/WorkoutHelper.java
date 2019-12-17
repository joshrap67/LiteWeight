package com.joshrap.liteweight.Helpers;

public class WorkoutHelper {
    public static String generateDayTitle(int num, int numDays) {
        int weekNum = (num / numDays) + 1;
        int dayNum = (num % numDays) + 1;
        return "W" + weekNum + ":D" + dayNum;
    }
}
