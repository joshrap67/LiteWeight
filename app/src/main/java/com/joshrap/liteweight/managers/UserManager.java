package com.joshrap.liteweight.managers;

import com.joshrap.liteweight.models.ErrorTypes;
import com.joshrap.liteweight.models.LiteWeightNetworkException;
import com.joshrap.liteweight.models.user.Friend;
import com.joshrap.liteweight.models.user.FriendRequest;
import com.joshrap.liteweight.models.user.OwnedExercise;
import com.joshrap.liteweight.models.Result;
import com.joshrap.liteweight.models.user.SharedWorkoutInfo;
import com.joshrap.liteweight.models.user.User;
import com.joshrap.liteweight.models.user.UserPreferences;
import com.joshrap.liteweight.models.UserAndWorkout;
import com.joshrap.liteweight.models.workout.Workout;
import com.joshrap.liteweight.repositories.currentUser.CurrentUserRepository;
import com.joshrap.liteweight.repositories.exercises.ExerciseRepository;
import com.joshrap.liteweight.repositories.users.UsersRepository;
import com.joshrap.liteweight.repositories.workouts.WorkoutRepository;
import com.joshrap.liteweight.providers.CurrentUserAndWorkoutProvider;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

public class UserManager {

    @Inject
    CurrentUserRepository currentUserRepository;
    @Inject
    UsersRepository usersRepository;
    @Inject
    ExerciseRepository exerciseRepository;
    @Inject
    WorkoutRepository workoutRepository;
    @Inject
    CurrentUserAndWorkoutProvider currentUserAndWorkoutProvider;

    @Inject
    public UserManager(CurrentUserRepository currentUserRepository, CurrentUserAndWorkoutProvider currentUserAndWorkoutProvider,
                       UsersRepository usersRepository, ExerciseRepository exerciseRepository, WorkoutRepository workoutRepository) {
        this.currentUserAndWorkoutProvider = currentUserAndWorkoutProvider;
        this.currentUserRepository = currentUserRepository;
        this.usersRepository = usersRepository;
        this.exerciseRepository = exerciseRepository;
        this.workoutRepository = workoutRepository;
    }

    public Result<UserAndWorkout> getUserAndCurrentWorkout() {
        Result<UserAndWorkout> result = new Result<>();

        try {
            UserAndWorkout userAndWorkout = new UserAndWorkout();
            User user = this.currentUserRepository.getUser();
            userAndWorkout.setUser(user);
            if (user != null && user.getCurrentWorkoutId() != null) {
                Workout workout = this.workoutRepository.getWorkout(user.getCurrentWorkoutId());
                userAndWorkout.setWorkout(workout);
            }
            currentUserAndWorkoutProvider.setCurrentUserAndWorkout(userAndWorkout);
            result.setData(userAndWorkout);
        } catch (IOException e) {
            result.setErrorMessage("There was a problem getting your data.");
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    public Result<User> getUser() {
        Result<User> result = new Result<>();

        try {
            User user = this.currentUserRepository.getUser();
            result.setData(user);
        } catch (Exception e) {
            result.setErrorMessage("There was a problem getting your data.");
        }

        return result;
    }

    public Result<User> createUser(String username, boolean metricUnits) {
        Result<User> result = new Result<>();

        try {
            User user = this.currentUserRepository.createUser(username, metricUnits);
            result.setData(user);
        } catch (LiteWeightNetworkException e) {
            if (e.getErrorType().equals(ErrorTypes.alreadyExists)) {
                result.setErrorMessage("Invalid username"); // todo? enumeration vulnerability?
            } else {
                result.setErrorMessage("There was a problem creating the user.");
            }
        } catch (IOException | ExecutionException | InterruptedException e) {
            result.setErrorMessage("There was a problem creating the user.");
        }

        return result;
    }

    public Result<String> updateExercise(String exerciseId, OwnedExercise updatedExercise) {
        Result<String> result = new Result<>();

        try {
            User user = currentUserAndWorkoutProvider.provideCurrentUser();

            this.exerciseRepository.updateExercise(exerciseId, updatedExercise);
            OwnedExercise exercise = user.getExercise(exerciseId);
            exercise.update(updatedExercise);
        } catch (Exception e) {
            result.setErrorMessage("There was a problem updating the exercise.");
        }

        return result;
    }

    public Result<OwnedExercise> newExercise(String exerciseName, List<String> focuses, double weight, int sets, int reps, String details, String videoURL) {
        Result<OwnedExercise> result = new Result<>();

        try {
            User user = currentUserAndWorkoutProvider.provideCurrentUser();

            OwnedExercise newExercise = exerciseRepository.newExercise(exerciseName, focuses, weight, sets, reps, details, videoURL);
            user.addExercise(newExercise);
            result.setData(newExercise);
        } catch (Exception e) {
            result.setErrorMessage("There was a problem creating the exercise.");
        }

        return result;
    }

    public Result<String> deleteExercise(String exerciseId) {
        Result<String> result = new Result<>();

        try {
            User user = currentUserAndWorkoutProvider.provideCurrentUser();
            UserAndWorkout currentUserAndWorkout = currentUserAndWorkoutProvider.provideCurrentUserAndWorkout();

            this.exerciseRepository.deleteExercise(exerciseId);
            user.removeExercise(exerciseId);
            if (currentUserAndWorkout.getWorkout() != null) {
                currentUserAndWorkout.getWorkout().getRoutine().deleteExerciseFromRoutine(exerciseId);
            }
        } catch (Exception e) {
            result.setErrorMessage("There was a problem deleting the exercise.");
        }

        return result;
    }

    public Result<String> updateProfilePicture(byte[] pictureData) {
        Result<String> result = new Result<>();

        try {
            this.currentUserRepository.updateProfilePicture(pictureData);
        } catch (Exception e) {
            result.setErrorMessage("There was a problem updating your profile picture.");
        }

        return result;
    }

    public Result<String> setFirebaseToken(String firebaseToken) {
        Result<String> result = new Result<>();

        try {
            this.currentUserRepository.linkFirebaseToken(firebaseToken);
        } catch (Exception e) {
            result.setErrorMessage("There was a problem linking the firebase token.");
        }

        return result;
    }

    public Result<String> unlinkFirebaseToken() {
        Result<String> result = new Result<>();

        try {
            this.currentUserRepository.unlinkFirebaseToken();
        } catch (Exception e) {
            result.setErrorMessage("There was a problem unlinking the firebase token.");
        }

        return result;
    }

    public Result<Friend> sendFriendRequest(String username) {
        Result<Friend> result = new Result<>();

        try {
            User user = currentUserAndWorkoutProvider.provideCurrentUser();

            Friend friend = this.usersRepository.sendFriendRequest(username);
            user.addFriend(friend);
            result.setData(friend);
        } catch (IOException e) {
            result.setErrorMessage("There was a problem sending the friend request.");
        } catch (LiteWeightNetworkException e) {
            if (e.getErrorType().equals(ErrorTypes.userNotFound)) {
                result.setErrorMessage("User does not exist.");
            } else {
                result.setErrorMessage("There was a problem sending the friend request.");
            }
        }

        return result;
    }

    public Result<String> cancelFriendRequest(String userId) {
        Result<String> result = new Result<>();

        try {
            User user = currentUserAndWorkoutProvider.provideCurrentUser();

            this.usersRepository.cancelFriendRequest(userId);
            user.removeFriend(userId);
        } catch (Exception e) {
            result.setErrorMessage("There was a problem canceling the friend request.");
        }

        return result;
    }

    public Result<String> setAllFriendRequestsSeen() {
        Result<String> result = new Result<>();

        try {
            User user = currentUserAndWorkoutProvider.provideCurrentUser();

            this.currentUserRepository.setAllFriendRequestsSeen();
            for (FriendRequest friendRequest : user.getFriendRequests()) {
                friendRequest.setSeen(true);
            }
        } catch (Exception e) {
            result.setErrorMessage("There was a problem setting all friend requests seen.");
        }

        return result;
    }

    public Result<String> updateUserPreferences(UserPreferences userPreferences) {
        Result<String> result = new Result<>();

        try {
            User user = currentUserAndWorkoutProvider.provideCurrentUser();

            this.currentUserRepository.setUserPreferences(userPreferences);
            user.setUserPreferences(userPreferences);
        } catch (Exception e) {
            result.setErrorMessage("There was a problem updating user preferences.");
        }

        return result;
    }

    public Result<String> acceptFriendRequest(String userId) {
        Result<String> result = new Result<>();

        try {
            User user = currentUserAndWorkoutProvider.provideCurrentUser();

            this.usersRepository.acceptFriendRequest(userId);

            FriendRequest friendRequest = user.getFriendRequest(userId);
            user.removeFriendRequest(userId);

            Friend friend = new Friend(friendRequest.getUserId(), friendRequest.getUsername(), friendRequest.getUserIcon(), true);
            user.addFriend(friend);
        } catch (Exception e) {
            result.setErrorMessage("There was a problem accepting the friend request.");
        }

        return result;
    }

    public Result<String> removeFriend(String userId) {
        Result<String> result = new Result<>();

        try {
            User user = currentUserAndWorkoutProvider.provideCurrentUser();

            this.usersRepository.removeFriend(userId);
            user.removeFriend(userId);
        } catch (Exception e) {
            result.setErrorMessage("There was a problem removing the friend.");
        }

        return result;
    }

    public Result<String> declineFriendRequest(String userId) {
        Result<String> result = new Result<>();

        try {
            User user = currentUserAndWorkoutProvider.provideCurrentUser();

            this.usersRepository.declineFriendRequest(userId);
            user.removeFriendRequest(userId);
        } catch (Exception e) {
            result.setErrorMessage("There was a problem declining the friend request.");
        }

        return result;
    }

    public Result<String> sendFeedback(String feedback, String feedbackTime) {
        Result<String> result = new Result<>();
        return this.currentUserRepository.sendFeedback(feedback, feedbackTime);
    }

    public Result<String> setAllReceivedWorkoutsSeen() {
        Result<String> result = new Result<>();

        try {
            User user = currentUserAndWorkoutProvider.provideCurrentUser();

            this.currentUserRepository.setAllReceivedWorkoutsSeen();
            for (SharedWorkoutInfo sharedWorkoutInfo : user.getReceivedWorkouts()) {
                sharedWorkoutInfo.setSeen(true);
            }
        } catch (Exception e) {
            result.setErrorMessage("There was a problem setting all received workouts to seen.");
        }

        return result;
    }

    public Result<String> setReceivedWorkoutSeen(String workoutId) {
        Result<String> result = new Result<>();

        try {
            User user = currentUserAndWorkoutProvider.provideCurrentUser();

            this.currentUserRepository.setReceivedWorkoutSeen(workoutId);
            SharedWorkoutInfo sharedWorkoutInfo = user.getReceivedWorkout(workoutId);
            sharedWorkoutInfo.setSeen(true);
        } catch (Exception e) {
            result.setErrorMessage("There was a problem setting the received workout to seen.");
        }

        return result;
    }
}
