package com.joshrap.liteweight.models;

import com.joshrap.liteweight.interfaces.Model;

import java.util.HashMap;
import java.util.Map;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
public class User implements Model {

    // Database keys
    public static final String USERNAME = "username";
    public static final String PREMIUM_TOKEN = "premiumToken";
    public static final String ICON = "icon";
    public static final String CURRENT_WORKOUT = "currentWorkout";
    public static final String WORKOUTS = "workouts";
    public static final String WORKOUTS_SENT = "workoutsSent";
    public static final String EXERCISES = "exercises";
    public static final String PRIVATE_ACCOUNT = "private_account";
    public static final String NOTIFICATION_PREFERENCES = "notificationPreferences";
    public static final String PUSH_ENDPOINT_ARN = "pushEndpointArn";
    public static final String FRIENDS = "friends";
    public static final String FRIENDS_OF = "friendsOf";
    public static final String RECEIVED_WORKOUTS = "receivedWorkouts";

    private String username;
    private String icon;
    private String pushEndpointArn;
    private String premiumToken;
    private String currentWorkout;
    private int workoutsSent;
    private boolean privateAccount;
    private int notificationPreferences;

    @Setter(AccessLevel.NONE)
    private Map<String, WorkoutUser> userWorkouts;
    @Setter(AccessLevel.NONE)
    private Map<String, ExerciseUser> userExercises;
    @Setter(AccessLevel.NONE)
    private Map<String, Friend> friends;
    @Setter(AccessLevel.NONE)
    private Map<String, Boolean> friendsOf;
    @Setter(AccessLevel.NONE)
    private Map<String, String> receivedWorkouts;


    public User(Map<String, Object> json) {
        this.setUsername((String) json.get(USERNAME));
        this.setIcon((String) json.get(ICON));
        this.setPushEndpointArn((String) json.get(PUSH_ENDPOINT_ARN));
        this.setPremiumToken((String) json.get(PREMIUM_TOKEN));
        this.setCurrentWorkout((String) json.get(CURRENT_WORKOUT));
        this.setWorkoutsSent((Integer) json.get(WORKOUTS_SENT));
        this.setPrivateAccount((Boolean) json.get(PRIVATE_ACCOUNT));
        this.setNotificationPreferences((Integer) json.get(NOTIFICATION_PREFERENCES));
        this.setUserWorkouts((Map<String, Object>) json.get(WORKOUTS));
        this.setUserExercises((Map<String, Object>) json.get(EXERCISES));
        this.setFriends((Map<String, Object>) json.get(FRIENDS));
        this.setFriendsOf((Map<String, Object>) json.get(FRIENDS_OF));
        this.setReceivedWorkouts((Map<String, Object>) json.get(RECEIVED_WORKOUTS));
    }

    // Setters
    private void setUserExercises(Map<String, Object> json) {
        if (json == null) {
            this.userExercises = null;
        } else {
            this.userExercises = new HashMap<>();
            for (String exerciseId : json.keySet()) {
                this.userExercises
                        .put(exerciseId, new ExerciseUser((Map<String, Object>) json.get(exerciseId), exerciseId));
            }
        }
    }

    private void setFriends(Map<String, Object> json) {
        if (json == null) {
            this.friends = null;
        } else {
            this.friends = new HashMap<>();
            for (String username : json.keySet()) {
                this.friends.put(username, new Friend((Map<String, Object>) json.get(username)));
            }
        }
    }

    private void setFriendsOf(Map<String, Object> json) {
        if (json == null) {
            this.friendsOf = null;
        } else {
            this.friendsOf = new HashMap<>();
            for (String username : json.keySet()) {
                this.friendsOf.put(username, (Boolean) json.get(username));
            }
        }
    }

    private void setReceivedWorkouts(Map<String, Object> json) {
        if (json == null) {
            this.receivedWorkouts = null;
        } else {
            this.receivedWorkouts = new HashMap<>();
            for (String workoutId : json.keySet()) {
                this.receivedWorkouts.put(workoutId, (String) json.get(workoutId));
            }
        }
    }

    private void setUserWorkouts(Map<String, Object> json) {
        if (json == null) {
            this.userWorkouts = null;
        } else {
            this.userWorkouts = new HashMap<>();
            for (String workoutId : json.keySet()) {
                this.userWorkouts.put(workoutId, new WorkoutUser(
                        (Map<String, Object>) json.get(workoutId), workoutId));
            }
        }
    }

    @Override
    public Map<String, Object> asMap() {
        Map<String, Object> retVal = new HashMap<>();
        retVal.put(USERNAME, this.username);
        retVal.put(ICON, this.icon);
        retVal.put(PUSH_ENDPOINT_ARN, this.pushEndpointArn);
        retVal.put(PREMIUM_TOKEN, this.premiumToken);
        retVal.put(CURRENT_WORKOUT, this.currentWorkout);
        retVal.put(WORKOUTS_SENT, this.workoutsSent);
        retVal.put(PRIVATE_ACCOUNT, this.privateAccount);
        retVal.put(NOTIFICATION_PREFERENCES, this.notificationPreferences);
        retVal.put(WORKOUTS, this.getUserWorkoutsMap());
        retVal.put(EXERCISES, this.getUserExercisesMap());
        retVal.put(FRIENDS, this.getFriendsMap());
        retVal.put(FRIENDS_OF, this.friendsOf);
        retVal.put(RECEIVED_WORKOUTS, this.receivedWorkouts);
        return retVal;
    }

    private Map<String, Map<String, Object>> getUserWorkoutsMap() {
        if (this.userWorkouts == null) {
            return null;
        }
        Map<String, Map<String, Object>> retVal = new HashMap<>();
        for (String workoutId : this.userWorkouts.keySet()) {
            retVal.put(workoutId, this.userWorkouts.get(workoutId).asMap());
        }
        return retVal;
    }

    private Map<String, Map<String, Object>> getUserExercisesMap() {
        if (this.userExercises == null) {
            return null;
        }

        Map<String, Map<String, Object>> retVal = new HashMap<>();
        for (String exerciseId : this.userExercises.keySet()) {
            retVal.put(exerciseId, this.userExercises.get(exerciseId).asMap());
        }
        return retVal;
    }

    private Map<String, Map<String, Object>> getFriendsMap() {
        if (this.friends == null) {
            return null;
        }

        Map<String, Map<String, Object>> retVal = new HashMap<>();
        for (String username : this.friends.keySet()) {
            retVal.put(username, this.friends.get(username).asMap());
        }
        return retVal;
    }

}
