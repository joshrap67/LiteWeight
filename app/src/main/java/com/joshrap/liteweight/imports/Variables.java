package com.joshrap.liteweight.imports;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Variables {
    /*
        Contains read only static variables
     */
    public static final int
            MAX_NUMBER_OF_WORKOUTS = 40,
            MAX_NUMBER_OF_FREE_EXERCISES = 100,
            WORKOUT_MAX_NUMBER_OF_DAYS = 7,
            MAX_NUMBER_OF_WEEKS = 10,
            MAX_WORKOUT_NAME = 40,
            MAX_EXERCISE_NAME = 40,
            MAX_WEIGHT = 99999,
            MAX_WEIGHT_DIGITS = 5,
            MAX_SETS = 99,
            MAX_SETS_DIGITS = 2,
            MAX_REPS = 999,
            MAX_REPS_DIGITS = 3,
            MAX_DETAILS_LENGTH = 120,
            MAX_URL_LENGTH = 200,
            EMAIL_CODE_LENGTH = 6,
            MAX_USERNAME_LENGTH = 40,
            MIN_PASSWORD_LENGTH = 8,
            MAX_STOPWATCH_TIME = 7200000,
            DEFAULT_TIMER_VALUE = 60000,
            MAX_FREE_WORKOUTS_SENT = 1500, // todo change after dev work is done
            ADD_MODE = 0,
            DELETE_MODE = 1,
            COPY_MODE = 2;

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

    public static final String
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
            ACCOUNT_TITLE = "My Account",
            ACCOUNT_PREFS_TITLE = "Account Preferences",
            RECEIVED_WORKOUTS_TITLE = "Received Workouts",
            FRIENDS_LIST_TITLE = "Friends List",
            BLOCKED_LIST_TITLE = "Blocked List",
            RECEIVED_WORKOUT_TITLE = "Received Workout",
            SETTINGS_TITLE = "Settings",
            EXERCISE_DETAILS_TITLE = "Exercise Details",
            EDIT_WORKOUT_TITLE = "Edit Workout",
            TIMER_RUNNING_CHANNEL = "timer_running_channel",
            TIMER_FINISHED_CHANNEL = "timer_finished_channel",
            FRIEND_REQUEST_CHANNEL = "friend_request_channel",
            ACCEPTED_FRIEND_CHANNEL = "accepted_friend_channel",
            RECEIVED_WORKOUT_CHANNEL = "received_workout_channel",
            STOPWATCH_RUNNING_CHANNEL = "stopwatch_running",
            FRIEND_LIST_POSITION = "friendsListPosition",
            WORKOUT_PROGRESS_KEY = "WorkoutProgressKey",
            EXERCISE_ID = "exerciseId";

    public static final String
            ID_TOKEN_KEY = "IdToken",
            REFRESH_TOKEN_KEY = "RefreshToken";

    // intent keys
    public static final String
            INTENT_TIMER_ABSOLUTE_START_TIME = "Absolute_Time",
            INTENT_TIMER_TIME_ON_CLOCK = "Time_on_clock",
            INTENT_TIMER_NOTIFICATION_CLICK = "Timer_notification_clicked",
            INTENT_STOPWATCH_NOTIFICATION_CLICK = "Stopwatch_notification_clicked",
            NEW_FRIEND_REQUEST_CLICK = "Friend_Request_Clicked",
            ACCEPTED_FRIEND_REQUEST_CLICK = "Accepted_Request_Clicked",
            INTENT_NOTIFICATION_DATA = "Notification_Data",
            RECEIVED_WORKOUT_CLICK = "Received_Workout_Clicked",
            INTENT_ID_TOKEN = "idToken",
            INTENT_REFRESH_TOKEN = "refreshToken";

    public static final String
            CANCELED_FRIEND_REQUEST_BROADCAST = "cancelFriendRequest",
            ACCEPTED_FRIEND_REQUEST_BROADCAST = "acceptedFriendRequestBroadcast",
            REMOVE_FRIEND_BROADCAST = "removeFriendBroadcast",
            DECLINED_FRIEND_REQUEST_BROADCAST = "declinedFriendRequest",
            NEW_FRIEND_REQUEST_BROADCAST = "friendRequestBroadcast",
            RECEIVED_WORKOUT_BROADCAST = "receivedWorkoutBroadcast",
            RECEIVED_WORKOUT_MODEL_UPDATED_BROADCAST = "receivedWorkoutModelUpdatedBroadcast";
    public static final int BATCH_SIZE = 10;
}
