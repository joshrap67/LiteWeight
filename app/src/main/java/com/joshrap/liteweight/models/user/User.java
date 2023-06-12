package com.joshrap.liteweight.models.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private String id;
    private String username;
    private String email;
    private String profilePicture;
    private String firebaseMessagingToken;
    private String premiumToken;
    private String currentWorkoutId;
    private int workoutsSent;
    private UserSettings settings;
    private List<WorkoutInfo> workouts = new ArrayList<>();
    private List<OwnedExercise> exercises = new ArrayList<>();
    private List<Friend> friends = new ArrayList<>();
    private List<FriendRequest> friendRequests = new ArrayList<>();
    private List<SharedWorkoutInfo> receivedWorkouts = new ArrayList<>();

    public boolean isPremium() {
        return this.premiumToken != null;
    }

    public SharedWorkoutInfo getReceivedWorkout(String sharedWorkoutId) {
        Optional<SharedWorkoutInfo> sharedWorkoutInfo = this.receivedWorkouts.stream().filter(x -> x.getSharedWorkoutId().equals(sharedWorkoutId)).findFirst();
        return sharedWorkoutInfo.orElse(null);
    }

    public void addReceivedWorkout(SharedWorkoutInfo sharedWorkoutInfo) {
        this.receivedWorkouts.add(sharedWorkoutInfo);
    }

    public void removeReceivedWorkout(String sharedWorkoutId) {
        this.receivedWorkouts.removeIf(x -> x.getSharedWorkoutId().equals(sharedWorkoutId));
    }

    public void removeExercise(String exerciseId) {
        this.exercises.removeIf(x -> x.getId().equals(exerciseId));
    }

    public OwnedExercise getExercise(String exerciseId) {
        Optional<OwnedExercise> exercise = this.exercises.stream().filter(x -> x.getId().equals(exerciseId)).findFirst();
        return exercise.orElse(null);
    }

    public void addExercise(OwnedExercise exercise) {
        this.exercises.add(exercise);
    }

    public int getTotalExerciseCount() {
        return this.exercises.size();
    }

    public void removeFriend(String userId) {
        this.friends.removeIf(x -> x.getUserId().equals(userId));
    }

    public Friend getFriend(String userId) {
        Optional<Friend> friend = this.friends.stream().filter(x -> x.getUserId().equals(userId)).findFirst();
        return friend.orElse(null);
    }

    public void addFriend(Friend friend) {
        this.friends.add(friend);
    }

    public void removeFriendRequest(String userId) {
        this.friendRequests.removeIf(x -> x.getUserId().equals(userId));
    }

    public FriendRequest getFriendRequest(String userId) {
        Optional<FriendRequest> friendRequest = this.friendRequests.stream().filter(x -> x.getUserId().equals(userId)).findFirst();
        return friendRequest.orElse(null);
    }

    public void addFriendRequest(FriendRequest friendRequest) {
        this.friendRequests.add(friendRequest);
    }

    public WorkoutInfo getWorkout(String workoutId) {
        Optional<WorkoutInfo> workout = this.workouts.stream().filter(x -> x.getWorkoutId().equals(workoutId)).findFirst();
        return workout.orElse(null);
    }

    public void addWorkout(WorkoutInfo workoutInfo) {
        this.workouts.add(workoutInfo);
    }

    public void removeWorkout(String workoutId) {
        this.workouts.removeIf(x -> x.getWorkoutId().equals(workoutId));
    }

    public long totalUnseenWorkouts() {
        return this.receivedWorkouts.stream().filter(x -> !x.isSeen()).count();
    }

    public int totalReceivedWorkouts() {
        return this.receivedWorkouts.size();
    }

}
