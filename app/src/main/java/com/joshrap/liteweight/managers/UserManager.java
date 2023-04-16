package com.joshrap.liteweight.managers;

import com.joshrap.liteweight.models.Friend;
import com.joshrap.liteweight.models.FriendRequest;
import com.joshrap.liteweight.models.OwnedExercise;
import com.joshrap.liteweight.models.ResultStatus;
import com.joshrap.liteweight.models.SharedWorkoutMeta;
import com.joshrap.liteweight.models.User;
import com.joshrap.liteweight.models.UserPreferences;
import com.joshrap.liteweight.models.UserAndWorkout;
import com.joshrap.liteweight.network.repos.UserRepository;
import com.joshrap.liteweight.providers.CurrentUserAndWorkoutProvider;

import java.util.List;

import javax.inject.Inject;

public class UserManager {

    @Inject
    UserRepository userRepository;
    @Inject
    CurrentUserAndWorkoutProvider currentUserAndWorkoutProvider;

    @Inject
    public UserManager(UserRepository userRepository, CurrentUserAndWorkoutProvider currentUserAndWorkoutProvider) {
        this.currentUserAndWorkoutProvider = currentUserAndWorkoutProvider;
        this.userRepository = userRepository;
    }

    public ResultStatus<UserAndWorkout> getUserAndCurrentWorkout() {
        return this.userRepository.getUserAndCurrentWorkout();
    }

    public ResultStatus<User> updateExercise(String exerciseId, OwnedExercise ownedExercise) {
        User user = currentUserAndWorkoutProvider.provideCurrentUser();
        ResultStatus<User> resultStatus = this.userRepository.updateExercise(exerciseId, ownedExercise);
        if (resultStatus.isSuccess()) {
            user.putExercise(resultStatus.getData().getExercise(exerciseId));
        }
        return resultStatus;
    }

    public ResultStatus<OwnedExercise> newExercise(String exerciseName, List<String> focuses, double weight, int sets, int reps, String details, String videoURL) {
        User user = currentUserAndWorkoutProvider.provideCurrentUser();
        ResultStatus<OwnedExercise> resultStatus = userRepository.newExercise(exerciseName, focuses, weight, sets, reps, details, videoURL);
        if (resultStatus.isSuccess()) {
            OwnedExercise newExercise = resultStatus.getData();
            user.putExercise(newExercise);
        }
        return resultStatus;
    }

    public ResultStatus<String> deleteExercise(String exerciseId) {
        User user = currentUserAndWorkoutProvider.provideCurrentUser();
        UserAndWorkout currentUserAndWorkout = currentUserAndWorkoutProvider.provideCurrentUserAndWorkout();
        ResultStatus<String> resultStatus = this.userRepository.deleteExercise(exerciseId);

        if (resultStatus.isSuccess()) {
            user.removeExercise(exerciseId);
            if (currentUserAndWorkout.getWorkout() != null) {
                currentUserAndWorkout.getWorkout().getRoutine().deleteExerciseFromRoutine(exerciseId);
            }
        }
        return resultStatus;
    }

    public ResultStatus<String> updateProfilePicture(String pictureData) {
        return this.userRepository.updateProfilePicture(pictureData);
    }

    public ResultStatus<String> updatePushEndpointId(String tokenId) {
        return this.userRepository.updatePushEndpointId(tokenId);
    }

    public ResultStatus<String> removePushEndpointId() {
        return this.userRepository.removePushEndpointId();
    }

    public ResultStatus<Friend> sendFriendRequest(String username) {
        User user = currentUserAndWorkoutProvider.provideCurrentUser();
        ResultStatus<Friend> resultStatus = this.userRepository.sendFriendRequest(username);
        if (resultStatus.isSuccess()) {
            user.addFriend(resultStatus.getData());
        }
        return resultStatus;
    }

    public ResultStatus<String> cancelFriendRequest(String username) {
        User user = currentUserAndWorkoutProvider.provideCurrentUser();
        ResultStatus<String> resultStatus = this.userRepository.cancelFriendRequest(username);
        if (resultStatus.isSuccess()) {
            user.removeFriend(username);
        }
        return resultStatus;
    }

    public ResultStatus<String> setAllRequestsSeen() {
        User user = currentUserAndWorkoutProvider.provideCurrentUser();
        ResultStatus<String> resultStatus = this.userRepository.setAllRequestsSeen();
        if (resultStatus.isSuccess()) {
            for (FriendRequest friendRequest : user.getFriendRequests().values()) {
                friendRequest.setSeen(true);
            }
        }
        return resultStatus;
    }

    public ResultStatus<String> updateUserPreferences(UserPreferences userPreferences) {
        User user = currentUserAndWorkoutProvider.provideCurrentUser();
        ResultStatus<String> resultStatus = this.userRepository.updateUserPreferences(userPreferences);
        if (resultStatus.isSuccess()) {
            user.setUserPreferences(userPreferences);
        }
        return resultStatus;
    }

    public ResultStatus<String> acceptFriendRequest(String username) {
        User user = currentUserAndWorkoutProvider.provideCurrentUser();
        ResultStatus<String> resultStatus = this.userRepository.acceptFriendRequest(username);
        if (resultStatus.isSuccess()) {
            FriendRequest friendRequest = user.getFriendRequest(username);
            user.removeFriendRequest(username);

            Friend friend = new Friend(friendRequest.getIcon(), true, username);
            user.addFriend(friend);
        }
        return resultStatus;
    }

    public ResultStatus<String> removeFriend(String username) {
        User user = currentUserAndWorkoutProvider.provideCurrentUser();
        ResultStatus<String> resultStatus = this.userRepository.removeFriend(username);
        if (resultStatus.isSuccess()) {
            user.removeFriend(username);
        }
        return resultStatus;
    }

    public ResultStatus<String> declineFriendRequest(String username) {
        User user = currentUserAndWorkoutProvider.provideCurrentUser();
        ResultStatus<String> resultStatus = this.userRepository.declineFriendRequest(username);
        if (resultStatus.isSuccess()) {
            user.removeFriendRequest(username);
        }
        return resultStatus;
    }

    public ResultStatus<String> unblockUser(String username) {
        User user = currentUserAndWorkoutProvider.provideCurrentUser();
        user.removeBlockedUser(username);

        ResultStatus<String> resultStatus = userRepository.unblockUser(username);
        if (resultStatus.isSuccess()) {
            user.removeBlockedUser(username);
        }
        return resultStatus;
    }

    public ResultStatus<String> sendFeedback(String feedback, String feedbackTime) {
        return this.userRepository.sendFeedback(feedback, feedbackTime);
    }

    public ResultStatus<String> blockUser(String username) {
        User user = currentUserAndWorkoutProvider.provideCurrentUser();

        ResultStatus<String> resultStatus = this.userRepository.blockUser(username);
        if (resultStatus.isSuccess()) {
            user.putBlocked(username, resultStatus.getData());
            user.removeFriendRequest(username);
            user.removeFriend(username);
        }
        return resultStatus;
    }

    public ResultStatus<String> setAllReceivedWorkoutsSeen() {
        User user = currentUserAndWorkoutProvider.provideCurrentUser();
        ResultStatus<String> resultStatus = this.userRepository.setAllReceivedWorkoutsSeen();
        if (resultStatus.isSuccess()) {
            user.setUnseenReceivedWorkouts(0);
            for (SharedWorkoutMeta sharedWorkoutMeta : user.getReceivedWorkouts().values()) {
                sharedWorkoutMeta.setSeen(true);
            }
        }
        return resultStatus;
    }

    public ResultStatus<String> setReceivedWorkoutSeen(String workoutId) {
        User user = currentUserAndWorkoutProvider.provideCurrentUser();
        ResultStatus<String> resultStatus = this.userRepository.setReceivedWorkoutSeen(workoutId);
        if (resultStatus.isSuccess()) {
            SharedWorkoutMeta sharedWorkoutMeta = user.getReceivedWorkout(workoutId);
            sharedWorkoutMeta.setSeen(true);
            user.setUnseenReceivedWorkouts(user.getUnseenReceivedWorkouts() - 1);
        }
        return resultStatus;
    }
}
