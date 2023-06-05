package com.joshrap.liteweight.managers;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.joshrap.liteweight.models.ErrorTypes;
import com.joshrap.liteweight.models.LiteWeightNetworkException;
import com.joshrap.liteweight.models.user.Friend;
import com.joshrap.liteweight.models.user.FriendRequest;
import com.joshrap.liteweight.models.user.OwnedExercise;
import com.joshrap.liteweight.models.Result;
import com.joshrap.liteweight.models.user.SharedWorkoutInfo;
import com.joshrap.liteweight.models.user.User;
import com.joshrap.liteweight.models.user.UserSettings;
import com.joshrap.liteweight.models.UserAndWorkout;
import com.joshrap.liteweight.models.workout.Workout;
import com.joshrap.liteweight.repositories.self.SelfRepository;
import com.joshrap.liteweight.repositories.exercises.ExerciseRepository;
import com.joshrap.liteweight.repositories.users.UsersRepository;
import com.joshrap.liteweight.repositories.users.responses.ReportUserResponse;
import com.joshrap.liteweight.repositories.users.responses.SearchByUsernameResponse;
import com.joshrap.liteweight.repositories.workouts.WorkoutRepository;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

public class UserManager {

    @Inject
    SelfRepository selfRepository;
    @Inject
    UsersRepository usersRepository;
    @Inject
    ExerciseRepository exerciseRepository;
    @Inject
    WorkoutRepository workoutRepository;
    @Inject
    CurrentUserModule currentUserModule;

    @Inject
    public UserManager(SelfRepository selfRepository, CurrentUserModule currentUserModule,
                       UsersRepository usersRepository, ExerciseRepository exerciseRepository, WorkoutRepository workoutRepository) {
        this.currentUserModule = currentUserModule;
        this.selfRepository = selfRepository;
        this.usersRepository = usersRepository;
        this.exerciseRepository = exerciseRepository;
        this.workoutRepository = workoutRepository;
    }

    public Result<UserAndWorkout> getUserAndCurrentWorkout() {
        Result<UserAndWorkout> result = new Result<>();

        try {
            UserAndWorkout userAndWorkout = new UserAndWorkout();
            User user = this.selfRepository.getSelf();
            userAndWorkout.setUser(user);
            if (user != null && user.getCurrentWorkoutId() != null) {
                Workout workout = this.workoutRepository.getWorkout(user.getCurrentWorkoutId());
                userAndWorkout.setWorkout(workout);
            }
            currentUserModule.setCurrentUserAndWorkout(userAndWorkout);
            result.setData(userAndWorkout);
        } catch (IOException | ExecutionException | InterruptedException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            result.setErrorMessage("There was a problem getting your data.");
        }

        return result;
    }

    public Result<User> createUser(String username, byte[] imageData, boolean metricUnits) {
        Result<User> result = new Result<>();

        try {
            User user = this.selfRepository.createSelf(username, imageData, metricUnits);
            result.setData(user);
        } catch (Exception e) {
            if (e instanceof LiteWeightNetworkException) {
                if (((LiteWeightNetworkException) e).getErrorType().equals(ErrorTypes.alreadyExists)) {
                    result.setErrorMessage("Invalid username");
                }
            } else {
                FirebaseCrashlytics.getInstance().recordException(e);
                result.setErrorMessage("There was a problem creating the user.");
            }
        }

        return result;
    }

    public Result<OwnedExercise> updateExercise(String exerciseId, OwnedExercise updatedExercise) {
        Result<OwnedExercise> result = new Result<>();

        try {
            User user = currentUserModule.getUser();

            this.exerciseRepository.updateExercise(exerciseId, updatedExercise);
            OwnedExercise exercise = user.getExercise(exerciseId);
            exercise.update(updatedExercise);

            result.setData(exercise);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            result.setErrorMessage("There was a problem updating the exercise.");
        }

        return result;
    }

    public Result<OwnedExercise> newExercise(String exerciseName, List<String> focuses, double weight, int sets, int reps, String details, String videoURL) {
        Result<OwnedExercise> result = new Result<>();

        try {
            User user = currentUserModule.getUser();

            OwnedExercise newExercise = exerciseRepository.newExercise(exerciseName, focuses, weight, sets, reps, details, videoURL);
            user.addExercise(newExercise);
            result.setData(newExercise);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            result.setErrorMessage("There was a problem creating the exercise.");
        }

        return result;
    }

    public Result<String> deleteExercise(String exerciseId) {
        Result<String> result = new Result<>();

        try {
            User user = currentUserModule.getUser();
            UserAndWorkout currentUserAndWorkout = currentUserModule.getCurrentUserAndWorkout();

            this.exerciseRepository.deleteExercise(exerciseId);
            user.removeExercise(exerciseId);
            if (currentUserAndWorkout.getWorkout() != null) {
                currentUserAndWorkout.getWorkout().getRoutine().deleteExerciseFromRoutine(exerciseId);
            }
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            result.setErrorMessage("There was a problem deleting the exercise.");
        }

        return result;
    }

    public Result<String> updateProfilePicture(byte[] pictureData) {
        Result<String> result = new Result<>();

        try {
            this.selfRepository.updateProfilePicture(pictureData);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            result.setErrorMessage("There was a problem updating your profile picture.");
        }

        return result;
    }

    public Result<String> setFirebaseToken(String firebaseToken) {
        Result<String> result = new Result<>();

        // todo write directly to firebase since it is just one property?
        try {
            this.selfRepository.linkFirebaseToken(firebaseToken);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            result.setErrorMessage("There was a problem linking the firebase token.");
        }

        return result;
    }

    public Result<String> unlinkFirebaseToken() {
        Result<String> result = new Result<>();

        try {
            this.selfRepository.unlinkFirebaseToken();
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            result.setErrorMessage("There was a problem unlinking the firebase token.");
        }

        return result;
    }

    public Result<Friend> sendFriendRequest(String username) {
        Result<Friend> result = new Result<>();

        try {
            User user = currentUserModule.getUser();

            SearchByUsernameResponse searchResult = this.usersRepository.searchByUsername(username);
            Friend friend = this.usersRepository.sendFriendRequest(searchResult.getId());
            user.addFriend(friend);
            result.setData(friend);
        } catch (Exception e) {
            if (e instanceof LiteWeightNetworkException) {
                if (((LiteWeightNetworkException) e).getErrorType().equals(ErrorTypes.userNotFound)) {
                    result.setErrorMessage("User does not exist.");
                }
            } else {
                FirebaseCrashlytics.getInstance().recordException(e);
                result.setErrorMessage("There was a problem sending the friend request.");
            }
        }

        return result;
    }

    public Result<String> cancelFriendRequest(String userId) {
        Result<String> result = new Result<>();

        try {
            User user = currentUserModule.getUser();

            this.usersRepository.cancelFriendRequest(userId);
            user.removeFriend(userId);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            result.setErrorMessage("There was a problem canceling the friend request.");
        }

        return result;
    }

    public Result<String> setAllFriendRequestsSeen() {
        Result<String> result = new Result<>();

        try {
            User user = currentUserModule.getUser();

            this.selfRepository.setAllFriendRequestsSeen();
            for (FriendRequest friendRequest : user.getFriendRequests()) {
                friendRequest.setSeen(true);
            }
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            result.setErrorMessage("There was a problem setting all friend requests seen.");
        }

        return result;
    }

    public Result<String> updateUserPreferences(UserSettings userSettings) {
        Result<String> result = new Result<>();

        try {
            User user = currentUserModule.getUser();

            this.selfRepository.setSettings(userSettings);
            user.setSettings(userSettings);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            result.setErrorMessage("There was a problem updating user preferences.");
        }

        return result;
    }

    public Result<String> acceptFriendRequest(String userId) {
        Result<String> result = new Result<>();

        try {
            User user = currentUserModule.getUser();

            this.usersRepository.acceptFriendRequest(userId);

            FriendRequest friendRequest = user.getFriendRequest(userId);
            user.removeFriendRequest(userId);

            Friend friend = new Friend(friendRequest.getUserId(), friendRequest.getUsername(), friendRequest.getProfilePicture(), true);
            user.addFriend(friend);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            result.setErrorMessage("There was a problem accepting the friend request.");
        }

        return result;
    }

    public Result<String> removeFriend(String userId) {
        Result<String> result = new Result<>();

        try {
            User user = currentUserModule.getUser();

            this.usersRepository.removeFriend(userId);
            user.removeFriend(userId);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            result.setErrorMessage("There was a problem removing the friend.");
        }

        return result;
    }

    public Result<String> declineFriendRequest(String userId) {
        Result<String> result = new Result<>();

        try {
            User user = currentUserModule.getUser();

            this.usersRepository.declineFriendRequest(userId);
            user.removeFriendRequest(userId);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            result.setErrorMessage("There was a problem declining the friend request.");
        }

        return result;
    }

    public Result<String> setAllReceivedWorkoutsSeen() {
        Result<String> result = new Result<>();

        try {
            User user = currentUserModule.getUser();

            this.selfRepository.setAllReceivedWorkoutsSeen();
            for (SharedWorkoutInfo sharedWorkoutInfo : user.getReceivedWorkouts()) {
                sharedWorkoutInfo.setSeen(true);
            }
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            result.setErrorMessage("There was a problem setting all received workouts to seen.");
        }

        return result;
    }

    public Result<String> setReceivedWorkoutSeen(String workoutId) {
        Result<String> result = new Result<>();

        try {
            User user = currentUserModule.getUser();

            this.selfRepository.setReceivedWorkoutSeen(workoutId);
            SharedWorkoutInfo sharedWorkoutInfo = user.getReceivedWorkout(workoutId);
            sharedWorkoutInfo.setSeen(true);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            result.setErrorMessage("There was a problem setting the received workout to seen.");
        }

        return result;
    }

    public Result<String> reportUser(String userId, String complaint) {
        Result<String> result = new Result<>();

        try {
            ReportUserResponse response = this.usersRepository.reportUser(userId, complaint);
            result.setData(response.getId());
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            result.setErrorMessage("There was a problem reporting the user.");
        }

        return result;
    }
}
