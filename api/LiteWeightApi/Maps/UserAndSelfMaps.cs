using LiteWeightAPI.Api.Self.Requests;
using LiteWeightAPI.Api.Self.Responses;
using LiteWeightAPI.Api.Users.Responses;
using LiteWeightAPI.Commands.Self;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Utils;

namespace LiteWeightAPI.Maps;

public static class UserAndSelfMaps
{
	public static UserResponse ToResponse(this User user)
	{
		return new UserResponse
		{
			Username = user.Username,
			ProfilePicture = user.ProfilePicture,
			Exercises = user.Exercises.Select(x => x.ToResponse()).ToList(),
			Email = user.Email,
			Friends = user.Friends.Select(x => x.ToResponse()).ToList(),
			Id = user.Id,
			Workouts = user.Workouts.Select(x => x.ToResponse()).ToList(),
			Settings = user.Settings.ToResponse(),
			FriendRequests = user.FriendRequests.Select(x => x.ToResponse()).ToList(),
			ReceivedWorkouts = user.ReceivedWorkouts.Select(x => x.ToResponse()).ToList(),
			PremiumToken = user.PremiumToken,
			WorkoutsSent = user.WorkoutsSent,
			CurrentWorkoutId = user.CurrentWorkoutId,
			FirebaseMessagingToken = user.FirebaseMessagingToken
		};
	}

	public static SearchUserResponse ToSearchResponse(this User user)
	{
		return new SearchUserResponse
		{
			Id = user.Id,
			Username = user.Username,
			ProfilePicture = user.ProfilePicture
		};
	}

	public static WorkoutInfoResponse ToResponse(this WorkoutInfo workoutInfo)
	{
		return new WorkoutInfoResponse
		{
			CurrentDay = workoutInfo.CurrentDay,
			CurrentWeek = workoutInfo.CurrentWeek,
			WorkoutId = workoutInfo.WorkoutId,
			TimesRestarted = workoutInfo.TimesRestarted,
			WorkoutName = workoutInfo.WorkoutName,
			AverageWorkoutCompletion = workoutInfo.AverageWorkoutCompletion,
			LastSetAsCurrentUtc = ParsingUtils.ConvertInstantToString(workoutInfo.LastSetAsCurrentUtc)
		};
	}

	public static FriendResponse ToResponse(this Friend friend)
	{
		return new FriendResponse
		{
			UserId = friend.UserId,
			Confirmed = friend.Confirmed,
			Username = friend.Username,
			ProfilePicture = friend.ProfilePicture
		};
	}

	private static FriendRequestResponse ToResponse(this FriendRequest friendRequest)
	{
		return new FriendRequestResponse
		{
			ProfilePicture = friendRequest.ProfilePicture,
			Username = friendRequest.Username,
			UserId = friendRequest.UserId,
			Seen = friendRequest.Seen,
			SentUtc = ParsingUtils.ConvertInstantToString(friendRequest.SentUtc)
		};
	}

	private static ReceivedWorkoutInfoResponse ToResponse(this ReceivedWorkoutInfo receivedWorkoutInfo)
	{
		return new ReceivedWorkoutInfoResponse
		{
			Seen = receivedWorkoutInfo.Seen,
			WorkoutName = receivedWorkoutInfo.WorkoutName,
			ReceivedUtc = ParsingUtils.ConvertInstantToString(receivedWorkoutInfo.ReceivedUtc),
			SenderId = receivedWorkoutInfo.SenderId,
			SenderUsername = receivedWorkoutInfo.SenderUsername,
			TotalDays = receivedWorkoutInfo.TotalDays,
			MostFrequentFocus = receivedWorkoutInfo.MostFrequentFocus,
			ReceivedWorkoutId = receivedWorkoutInfo.ReceivedWorkoutId,
			SenderProfilePicture = receivedWorkoutInfo.SenderProfilePicture
		};
	}

	private static UserSettingsResponse ToResponse(this UserSettings userSettings)
	{
		return new UserSettingsResponse
		{
			MetricUnits = userSettings.MetricUnits,
			PrivateAccount = userSettings.PrivateAccount,
			UpdateDefaultWeightOnRestart = userSettings.UpdateDefaultWeightOnRestart,
			UpdateDefaultWeightOnSave = userSettings.UpdateDefaultWeightOnSave
		};
	}

	public static CreateSelf ToCommand(this CreateSelfRequest request, string id, string email)
	{
		return new CreateSelf
		{
			UserId = id,
			Username = request.Username,
			UserEmail = email,
			MetricUnits = request.MetricUnits,
			ProfilePictureData = request.ProfilePictureData
		};
	}

	public static SetSettings ToCommand(this UserSettingsResponse settings, string userId)
	{
		return new SetSettings
		{
			MetricUnits = settings.MetricUnits,
			UserId = userId,
			UpdateDefaultWeightOnSave = settings.UpdateDefaultWeightOnSave,
			UpdateDefaultWeightOnRestart = settings.UpdateDefaultWeightOnRestart,
			PrivateAccount = settings.PrivateAccount
		};
	}
}