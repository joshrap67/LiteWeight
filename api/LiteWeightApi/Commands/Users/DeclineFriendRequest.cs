using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;
using LiteWeightAPI.Services;

namespace LiteWeightAPI.Commands.Users;

public class DeclineFriendRequest : ICommand<bool>
{
	public required string InitiatorUserId { get; set; }
	public required string UserIdToDecline { get; set; }
}

public class DeclineFriendRequestHandler : ICommandHandler<DeclineFriendRequest, bool>
{
	private readonly IRepository _repository;
	private readonly IPushNotificationService _pushNotificationService;

	public DeclineFriendRequestHandler(IRepository repository, IPushNotificationService pushNotificationService)
	{
		_repository = repository;
		_pushNotificationService = pushNotificationService;
	}

	public async Task<bool> HandleAsync(DeclineFriendRequest command)
	{
		var initiator = (await _repository.GetUser(command.InitiatorUserId))!;
		var userToDecline = await _repository.GetUser(command.UserIdToDecline);

		if (userToDecline == null)
		{
			throw new ResourceNotFoundException("User");
		}

		var friendRequest = initiator.FriendRequests.FirstOrDefault(x => x.UserId == command.UserIdToDecline);
		if (friendRequest == null) return false;
		initiator.FriendRequests.Remove(friendRequest);

		var initiatorToRemove = userToDecline.Friends.FirstOrDefault(x => x.UserId == command.InitiatorUserId);
		if (initiatorToRemove == null) return false;
		userToDecline.Friends.Remove(initiatorToRemove);

		await _repository.ExecuteBatchWrite(usersToPut: new List<User> { initiator, userToDecline });

		// send a notification to the user whose friend request was declined
		await _pushNotificationService.SendFriendRequestDeclinedNotification(userToDecline, initiator);

		return true;
	}
}