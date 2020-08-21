package com.joshrap.liteweight.imports;

import com.joshrap.liteweight.models.User;
import com.joshrap.liteweight.models.Workout;

public class Globals {
    public static boolean timerServiceRunning = false;
    public static boolean stopwatchServiceRunning = false;
    public static String refreshToken = null;
    public static String idToken = null;
    public static User user;
    public static Workout activeWorkout;
}
