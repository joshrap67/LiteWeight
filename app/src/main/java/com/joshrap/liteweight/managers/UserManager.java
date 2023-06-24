package com.joshrap.liteweight.managers;

import static com.joshrap.liteweight.utils.NetworkUtils.getLiteWeightError;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.joshrap.liteweight.models.ErrorTypes;
import com.joshrap.liteweight.models.LiteWeightNetworkException;
import com.joshrap.liteweight.models.user.Friend;
import com.joshrap.liteweight.models.user.FriendRequest;
import com.joshrap.liteweight.models.Result;
import com.joshrap.liteweight.models.user.User;
import com.joshrap.liteweight.repositories.self.SelfRepository;
import com.joshrap.liteweight.repositories.exercises.ExerciseRepository;
import com.joshrap.liteweight.repositories.users.UsersRepository;
import com.joshrap.liteweight.repositories.users.responses.ReportUserResponse;
import com.joshrap.liteweight.repositories.users.responses.SearchByUsernameResponse;
import com.joshrap.liteweight.repositories.workouts.WorkoutRepository;

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
