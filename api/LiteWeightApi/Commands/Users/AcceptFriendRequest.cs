using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Errors.Exceptions;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;
using LiteWeightAPI.Imports;
using LiteWeightAPI.Services;

namespace LiteWeightAPI.Commands.Users;

public class AcceptFriendRequest : ICommand<bool>
{
	public required string InitiatorUserId { get; set; }
	public required string AcceptedUserId { get; set; }
}

public class AcceptFriendRequestHandler : ICommandHandler<AcceptFriendRequest, bool>
{
	private readonly IRepository _repository;
	private readonly IPushNotificationService _pushNotificationService;

	public AcceptFriendRequestHandler(IRepository repository, IPushNotificationService pushNotificationService)
	{
		_repository = repository;
		_pushNotificationService = pushNotificationService;
	}

	public async Task<bool> HandleAsync(AcceptFriendRequest command)
	{
		var initiator = (await _repository.GetUser(command.InitiatorUserId))!;
		var acceptedUser = await _repository.GetUser(command.AcceptedUserId);

		if (acceptedUser == null)
		{
			throw new ResourceNotFoundException("User");
		}

		if (initiator.Friends.Count >= Globals.MaxNumberFriends)
		{
			throw new MaxLimitException("Max number of friends reached.");
		}

		var friendRequest = initiator.FriendRequests.FirstOrDefault(x => x.UserId == command.AcceptedUserId);
		if (friendRequest == null) return false;

		// remove request from user who initiated accepting, and add the new friend
		var newFriend = new Friend
		{
			UserId = acceptedUser.Id,
			Username = acceptedUser.Username,
			ProfilePicture = acceptedUser.ProfilePicture,
			Confirmed = true
		};
		initiator.FriendRequests.Remove(friendRequest);
		initiator.Friends.Add(newFriend);
		// update friend to accepted for the user who sent the request
		var initiatorToRemove = acceptedUser.Friends.FirstOrDefault(x => x.UserId == command.InitiatorUserId);
		if (initiatorToRemove != null) initiatorToRemove.Confirmed = true;

		await _repository.ExecuteBatchWrite(usersToPut: new List<User> { initiator, acceptedUser });

		// send a notification to the user who was accepted as a friend
		await _pushNotificationService.SendFriendRequestAcceptedNotification(acceptedUser, initiator);

		return true;
	}
}