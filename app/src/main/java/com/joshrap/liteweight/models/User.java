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
    public static final String FRIENDS = "friends";
    public static final String FRIENDS_REQUESTS = "friendRequests";
    public static final String RECEIVED_WORKOUTS = "receivedWorkouts";
    public static final String USER_PREFERENCES = "preferences";
    public static final String BLOCKED = "blocked";

    private String username;
    private String icon;
    private String premiumToken;
    private String currentWorkout;
    private int workoutsSent;
    private UserPreferences userPreferences;

    @Setter(AccessLevel.NONE)
    private Map<String, String> blocked;
    @Setter(AccessLevel.NONE)
    private Map<String, WorkoutUser> userWorkouts;
    @Setter(AccessLevel.NONE)
    private Map<String, ExerciseUser> userExercises;
    @Setter(AccessLevel.NONE)
    private Map<String, Friend> friends;
    @Setter(AccessLevel.NONE)
    private Map<String, FriendRequest> friendRequests;
    @Setter(AccessLevel.NONE)
    private Map<String, String> receivedWorkouts;


    public User(Map<String, Object> json) {
        this.setUsername((String) json.get(USERNAME));
        this.setIcon((String) json.get(ICON));
        this.setPremiumToken((String) json.get(PREMIUM_TOKEN));
        this.setCurrentWorkout((String) json.get(CURRENT_WORKOUT));
        this.setWorkoutsSent((Integer) json.get(WORKOUTS_SENT));
        this.setUserWorkouts((Map<String, Object>) json.get(WORKOUTS));
        this.setUserExercises((Map<String, Object>) json.get(EXERCISES));
        this.setFriends((Map<String, Object>) json.get(FRIENDS));
        this.setUserPreferences(new UserPreferences((Map<String, Object>) json.get(USER_PREFERENCES)));
        this.setFriendRequests((Map<String, Object>) json.get(FRIENDS_REQUESTS));
        this.setReceivedWorkouts((Map<String, Object>) json.get(RECEIVED_WORKOUTS));
        this.setBlocked((Map<String, Object>) json.get(BLOCKED));
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

    private void setBlocked(Map<String, Object> json) {
        if (json == null) {
            this.blocked = null;
        } else {
            this.blocked = new HashMap<>();
            for (String username : json.keySet()) {
                this.blocked.put(username, (String) json.get(ICON));
            }
        }
    }

    private void setFriends(Map<String, Object> json) {
        if (json == null) {
            this.friends = null;
        } else {
            this.friends = new HashMap<>();
            for (String username : json.keySet()) {
                this.friends.put(username, new Friend((Map<String, Object>) json.get(username), username));
            }
        }
    }

    public void setFriendRequests(Map<String, Object> json) {
        if (json == null) {
            this.friendRequests = null;
        } else {
            this.friendRequests = new HashMap<>();
            for (String username : json.keySet()) {
                this.friendRequests.put(username, new FriendRequest(
                        (Map<String, Object>) json.get(username), username));
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
        retVal.put(PREMIUM_TOKEN, this.premiumToken);
        retVal.put(CURRENT_WORKOUT, this.currentWorkout);
        retVal.put(WORKOUTS_SENT, this.workoutsSent);
        retVal.put(USER_PREFERENCES, this.userPreferences.asMap());
        retVal.put(WORKOUTS, this.getUserWorkoutsMap());
        retVal.put(EXERCISES, this.getUserExercisesMap());
        retVal.put(FRIENDS, this.getFriendsMap());
        retVal.put(FRIENDS_REQUESTS, this.getFriendRequestsMap());
        retVal.put(RECEIVED_WORKOUTS, this.receivedWorkouts);
        retVal.put(BLOCKED, this.blocked);
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

    private Map<String, Map<String, Object>> getFriendRequestsMap() {
        if (this.friendRequests == null) {
            return null;
        }

        Map<String, Map<String, Object>> retVal = new HashMap<>();
        for (String username : this.friendRequests.keySet()) {
            retVal.put(username, this.friendRequests.get(username).asMap());
        }
        return retVal;
    }

}
