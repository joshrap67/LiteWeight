package com.joshrap.liteweight.imports;

import com.joshrap.liteweight.database.entities.MetaEntity;
import com.joshrap.liteweight.models.User;

public class Globals {
    public static boolean timerServiceRunning = false;
    public static boolean stopwatchServiceRunning = false;
    public static MetaEntity currentWorkout = null;
    public static String refreshToken = null;
    public static String idToken = null;
    public static final String deploymentStage = "dev/";
    public static User user;
}
