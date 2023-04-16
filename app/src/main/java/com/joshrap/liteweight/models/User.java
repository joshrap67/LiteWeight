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
    public static final String UNSEEN_RECEIVED_WORKOUTS = "unseenReceivedWorkouts";
    public static final String TOTAL_RECEIVED_WORKOUTS = "totalReceivedWorkouts";

    private String username;
    private String icon;
    private String premiumToken;
    private String currentWorkout;
    private int workoutsSent;
    private UserPreferences userPreferences;
    private int unseenReceivedWorkouts;
    private int totalReceivedWorkouts;

    @Setter(AccessLevel.NONE)
    private Map<String, String> blocked;
    @Setter(AccessLevel.NONE)
    private Map<String, WorkoutMeta> workoutMetas; //todo rename this to workoutInfo? Or workoutDetails? workoutStatistics? anything other than meta lmfao
    @Setter(AccessLevel.NONE)
    private Map<String, OwnedExercise> ownedExercises;
    @Setter(AccessLevel.NONE)
    private Map<String, Friend> friends;
    @Setter(AccessLevel.NONE)
    private Map<String, FriendRequest> friendRequests;
    @Setter(AccessLevel.NONE)
    private Map<String, SharedWorkoutMeta> receivedWorkouts;


    public User(Map<String, Object> json) {
        this.setUsername((String) json.get(USERNAME));
        this.setIcon((String) json.get(ICON));
        this.setPremiumToken((String) json.get(PREMIUM_TOKEN));
        this.setCurrentWorkout((String) json.get(CURRENT_WORKOUT));
        this.setWorkoutsSent((Integer) json.get(WORKOUTS_SENT));
        this.setWorkoutMetas((Map<String, Object>) json.get(WORKOUTS));
        this.setOwnedExercises((Map<String, Object>) json.get(EXERCISES));
        this.setFriends((Map<String, Object>) json.get(FRIENDS));
        this.setUserPreferences(new UserPreferences((Map<String, Object>) json.get(USER_PREFERENCES)));
        this.setFriendRequests((Map<String, Object>) json.get(FRIENDS_REQUESTS));
        this.setBlocked((Map<String, Object>) json.get(BLOCKED));
        this.setUnseenReceivedWorkouts((Integer) json.get(UNSEEN_RECEIVED_WORKOUTS));
        this.setReceivedWorkouts((Map<String, Object>) json.get(RECEIVED_WORKOUTS));
        this.setTotalReceivedWorkouts((Integer) json.get(TOTAL_RECEIVED_WORKOUTS));
    }

    public void addNewExercises(Map<String, OwnedExercise> exercises) {
        for (String exerciseId : exercises.keySet()) {
            if (!this.ownedExercises.containsKey(exerciseId)) {
                this.ownedExercises.put(exerciseId, exercises.get(exerciseId));
            }
        }
    }

    public boolean doesNotContainReceivedWorkout(String receivedWorkoutId) {
        return !this.receivedWorkouts.containsKey(receivedWorkoutId);
    }

    public SharedWorkoutMeta getReceivedWorkout(String workoutMetaId) {
        return this.receivedWorkouts.get(workoutMetaId);
    }

    public void removeExercise(String exerciseId) {
        this.ownedExercises.remove(exerciseId);
    }

    public OwnedExercise getExercise(String exerciseId) {
        return this.ownedExercises.get(exerciseId);
    }

    public void putExercise(OwnedExercise exercise) {
        this.ownedExercises.put(exercise.getExerciseId(), exercise);
    }

    public int getTotalExerciseCount(){
        return this.ownedExercises.size();
    }

    public void removeFriend(String username) {
        this.friends.remove(username);
    }

    public Friend getFriend(String username) {
        return this.friends.get(username);
    }

    public void addFriend(Friend friend) {
        this.friends.put(friend.getUsername(), friend);
    }

    public void removeBlockedUser(String username) {
        this.blocked.remove(username);
    }

    public boolean isBlocking(String username){
        return this.blocked.containsKey(username);
    }

    public void putBlocked(String username, String icon) {
        this.blocked.put(username, icon);
    }

    public void removeFriendRequest(String username) {
        this.friendRequests.remove(username);
    }

    public FriendRequest getFriendRequest(String username) {
        return this.friendRequests.get(username);
    }

    public void addFriendRequest(FriendRequest friendRequest) {
        this.friendRequests.put(friendRequest.getUsername(), friendRequest);
    }

    public void putReceivedWorkout(SharedWorkoutMeta workoutMeta) {
        this.receivedWorkouts.put(workoutMeta.getWorkoutId(), workoutMeta);
    }

    public void removeReceivedWorkout(String workoutId) {
        this.receivedWorkouts.remove(workoutId);
    }

    public WorkoutMeta getWorkout(String workoutId) {
        return this.workoutMetas.get(workoutId);
    }

    public void putWorkout(WorkoutMeta workoutMeta) {
        this.workoutMetas.put(workoutMeta.getWorkoutId(), workoutMeta);
    }

    /**
     * Utilized when getting updated exercises from backend.
     *
     * @param newExercises exercises that may or may not all be updated.
     */
    public void updateOwnedExercises(Map<String, OwnedExercise> newExercises) {
        this.ownedExercises = newExercises;
    }

    /**
     * Utilized when getting updated workouts from backend.
     *
     * @param newWorkouts workouts that may or may not all be updated.
     */
    public void updateUserWorkouts(Map<String, WorkoutMeta> newWorkouts) {
        this.workoutMetas = newWorkouts;
    }

    // Setters
    private void setOwnedExercises(Map<String, Object> json) {
        if (json == null) {
            this.ownedExercises = null;
        } else {
            this.ownedExercises = new HashMap<>();
            for (String exerciseId : json.keySet()) {
                this.ownedExercises
                        .put(exerciseId, new OwnedExercise((Map<String, Object>) json.get(exerciseId), exerciseId));
            }
        }
    }

    private void setBlocked(Map<String, Object> json) {
        if (json == null) {
            this.blocked = null;
        } else {
            this.blocked = new HashMap<>();
            for (String username : json.keySet()) {
                this.blocked.put(username, (String) json.get(username));
            }
        }
    }

    private void setReceivedWorkouts(Map<String, Object> json) {
        if (json == null) {
            this.receivedWorkouts = null;
        } else {
            this.receivedWorkouts = new HashMap<>();
            for (String workoutId : json.keySet()) {
                this.receivedWorkouts.put(workoutId, new SharedWorkoutMeta(
                        (Map<String, Object>) json.get(workoutId), workoutId));
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

    private void setFriendRequests(Map<String, Object> json) {
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

    private void setWorkoutMetas(Map<String, Object> json) {
        if (json == null) {
            this.workoutMetas = null;
        } else {
            this.workoutMetas = new HashMap<>();
            for (String workoutId : json.keySet()) {
                this.workoutMetas.put(workoutId, new WorkoutMeta(
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
        retVal.put(BLOCKED, this.blocked);
        retVal.put(UNSEEN_RECEIVED_WORKOUTS, this.unseenReceivedWorkouts);
        retVal.put(TOTAL_RECEIVED_WORKOUTS, this.totalReceivedWorkouts);
        retVal.put(RECEIVED_WORKOUTS, this.getReceivedWorkoutsMap());
        return retVal;
    }

    private Map<String, Map<String, Object>> getUserWorkoutsMap() {
        if (this.workoutMetas == null) {
            return null;
        }
        Map<String, Map<String, Object>> retVal = new HashMap<>();
        for (String workoutId : this.workoutMetas.keySet()) {
            retVal.put(workoutId, this.workoutMetas.get(workoutId).asMap());
        }
        return retVal;
    }

    private Map<String, Object> getReceivedWorkoutsMap() {
        Map<String, Object> retMap = new HashMap<>();
        for (String workoutId : receivedWorkouts.keySet()) {
            retMap.put(workoutId, receivedWorkouts.get(workoutId).asMap());
        }
        return retMap;
    }

    private Map<String, Map<String, Object>> getUserExercisesMap() {
        if (this.ownedExercises == null) {
            return null;
        }

        Map<String, Map<String, Object>> retVal = new HashMap<>();
        for (String exerciseId : this.ownedExercises.keySet()) {
            retVal.put(exerciseId, this.ownedExercises.get(exerciseId).asMap());
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
