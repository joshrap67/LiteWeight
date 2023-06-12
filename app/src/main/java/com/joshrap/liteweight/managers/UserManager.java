package com.joshrap.liteweight.managers;

import static com.joshrap.liteweight.utils.NetworkUtils.getLiteWeightError;

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

import java.util.List;

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

    // todo self manager?

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
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            result.setErrorMessage("There was a problem getting your data. Suggest upgrading your app if applicable to try and resolve the issue.");
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
                if (getLiteWeightError((LiteWeightNetworkException) e).equals(ErrorTypes.alreadyExists)) {
                    result.setErrorMessage("Invalid username");
                }
            } else {
                FirebaseCrashlytics.getInstance().recordException(e);
                result.setErrorMessage("There was a problem creating the user.");
            }
        }

        return result;
    }

    public Result<String> deleteSelf() {
        Result<String> result = new Result<>();
        try {
            this.selfRepository.deleteSelf();
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            result.setErrorMessage("There was a problem deleting your account.");
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

    public void setFirebaseMessagingToken(String firebaseToken) {
        try {
            this.selfRepository.linkFirebaseMessagingToken(firebaseToken);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    public void unlinkFirebaseMessagingToken() {
        try {
            this.selfRepository.linkFirebaseMessagingToken(null);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    public Result<Friend> sendFriendRequest(String username) {
        Result<Friend> result = new Result<>();

        try {
            User user = currentUserModule.getUser();

            SearchByUsernameResponse searchResult = this.usersRepository.searchByUsername(username);
            Friend friend = this.usersRepository.sendFriendRequest(searchResult.getId());
            user.addFriend(friend);
            result.setData(friend); // todo deep copy
        } catch (Exception e) {
            if (e instanceof LiteWeightNetworkException) {
                if (getLiteWeightError((LiteWeightNetworkException) e).equals(ErrorTypes.userNotFound)) {
                    result.setErrorMessage("User does not exist.");
                } else if (getLiteWeightError((LiteWeightNetworkException) e).equals(ErrorTypes.maxLimit)) {
                    result.setErrorMessage("Recipient user received too many friend requests.");
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

    public void setAllFriendRequestsSeen() {
        try {
            User user = currentUserModule.getUser();

            // todo blind sends should happen here instead of on views. essentially just swap these commands below
            this.selfRepository.setAllFriendRequestsSeen();
            for (FriendRequest friendRequest : user.getFriendRequests()) {
                friendRequest.setSeen(true);
            }
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    public Result<String> setUserSettings(UserSettings userSettings) {
        Result<String> result = new Result<>();

        try {
            User user = currentUserModule.getUser();

            this.selfRepository.setSettings(userSettings);
            user.setSettings(userSettings);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            result.setErrorMessage("There was a problem updating user settings.");
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
            if (e instanceof LiteWeightNetworkException) {
                if (getLiteWeightError((LiteWeightNetworkException) e).equals(ErrorTypes.maxLimit)) {
                    result.setErrorMessage("Accepting this would put you over the maximum allowed number of friends.");
                }
            } else {
                FirebaseCrashlytics.getInstance().recordException(e);
                result.setErrorMessage("There was a problem accepting the friend request.");
            }
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

    public void setAllReceivedWorkoutsSeen() {
        try {
            User user = currentUserModule.getUser();

            this.selfRepository.setAllReceivedWorkoutsSeen();
            for (SharedWorkoutInfo sharedWorkoutInfo : user.getReceivedWorkouts()) {
                sharedWorkoutInfo.setSeen(true);
            }
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    public void setReceivedWorkoutSeen(String workoutId) {
        try {
            User user = currentUserModule.getUser();

            this.selfRepository.setReceivedWorkoutSeen(workoutId);
            SharedWorkoutInfo sharedWorkoutInfo = user.getReceivedWorkout(workoutId);
            sharedWorkoutInfo.setSeen(true);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
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
