using LiteWeightAPI.Domain;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;
using LiteWeightAPI.Services;

namespace LiteWeightAPI.Commands.Self;

public class DeleteSelf : ICommand<bool>
{
	public required string UserId { get; init; }
}

public class DeleteSelfHandler : ICommandHandler<DeleteSelf, bool>
{
	private readonly IRepository _repository;
	private readonly IStorageService _storageService;
	private readonly IFirebaseAuthService _firebaseAuthService;
	private readonly IPushNotificationService _pushNotificationService;

	public DeleteSelfHandler(IRepository repository, IStorageService storageService,
		IFirebaseAuthService firebaseAuthService, IPushNotificationService pushNotificationService)
	{
		_repository = repository;
		_storageService = storageService;
		_firebaseAuthService = firebaseAuthService;
		_pushNotificationService = pushNotificationService;
	}

	public async Task<bool> HandleAsync(DeleteSelf command)
	{
		var user = await _repository.GetUser(command.UserId) ?? throw new ResourceNotFoundException("Self");
		await _storageService.DeleteProfilePicture(user.ProfilePicture);
		var workoutsToDelete = user.Workouts.Select(x => x.WorkoutId).ToList();
		var receivedWorkoutsToDelete = user.ReceivedWorkouts.Select(x => x.ReceivedWorkoutId).ToList();

		var usersWhoSentFriendRequests = user.FriendRequests.Select(x => x.UserId).ToList();
		var usersWhoAreFriends = user.Friends
			.Where(x => x.Confirmed)
			.Select(x => x.UserId)
			.ToList();
		var usersWhoReceivedFriendRequests = user.Friends
			.Where(x => !x.Confirmed)
			.Select(x => x.UserId)
			.ToList();

		// can't really rely on transactions here since the worst case scenario is the user could have thousands of users as friends (firebase has upper limit for batch writes)
		foreach (var workoutId in workoutsToDelete)
		{
			await _repository.DeleteWorkout(workoutId);
		}

		foreach (var workoutId in receivedWorkoutsToDelete)
		{
			await _repository.DeleteReceivedWorkout(workoutId);
		}

		foreach (var otherUserId in usersWhoSentFriendRequests)
		{
			var userToDecline = await _repository.GetUser(otherUserId);
			if (userToDecline == null)
			{
				continue;
			}

			userToDecline.Friends.RemoveAll(x => x.UserId == command.UserId);
			await _pushNotificationService.SendFriendRequestDeclinedNotification(userToDecline, user);
			await _repository.PutUser(userToDecline);
		}

		foreach (var otherUserId in usersWhoAreFriends)
		{
			var userToRemove = await _repository.GetUser(otherUserId);
			if (userToRemove == null)
			{
				continue;
			}

			userToRemove.Friends.RemoveAll(x => x.UserId == command.UserId);
			await _pushNotificationService.SendRemovedAsFriendNotification(userToRemove, user);
			await _repository.PutUser(userToRemove);
		}

		foreach (var otherUserId in usersWhoReceivedFriendRequests)
		{
			var userToCancel = await _repository.GetUser(otherUserId);
			if (userToCancel == null)
			{
				continue;
			}

			userToCancel.FriendRequests.RemoveAll(x => x.UserId == command.UserId);
			await _pushNotificationService.SendFriendRequestCanceledNotification(userToCancel, user);
			await _repository.PutUser(userToCancel);
		}

		await _repository.DeleteUser(command.UserId);
		await _firebaseAuthService.DeleteUser(command.UserId);

		return true;
	}
}