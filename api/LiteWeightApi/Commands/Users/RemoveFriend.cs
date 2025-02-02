using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;
using LiteWeightAPI.Services;

namespace LiteWeightAPI.Commands.Users;

public class RemoveFriend : ICommand<bool>
{
	public required string InitiatorUserId { get; set; }
	public required string RemovedUserId { get; set; }
}

public class RemoveFriendHandler : ICommandHandler<RemoveFriend, bool>
{
	private readonly IRepository _repository;
	private readonly IPushNotificationService _pushNotificationService;

	public RemoveFriendHandler(IRepository repository, IPushNotificationService pushNotificationService)
	{
		_repository = repository;
		_pushNotificationService = pushNotificationService;
	}

	public async Task<bool> HandleAsync(RemoveFriend command)
	{
		var initiator = (await _repository.GetUser(command.InitiatorUserId))!;
		var removedFriend = await _repository.GetUser(command.RemovedUserId);

		if (removedFriend == null)
		{
			throw new ResourceNotFoundException("User");
		}

		var friendToRemove = initiator.Friends.FirstOrDefault(x => x.UserId == command.RemovedUserId);
		var initiatorToRemove = removedFriend.Friends.FirstOrDefault(x => x.UserId == command.InitiatorUserId);
		if (friendToRemove == null || initiatorToRemove == null) return false;

		initiator.Friends.Remove(friendToRemove);
		removedFriend.Friends.Remove(initiatorToRemove);

		await _repository.ExecuteBatchWrite(usersToPut: new List<User> { initiator, removedFriend });

		// send a notification to indicate the user has been removed as a friend
		await _pushNotificationService.SendRemovedAsFriendNotification(removedFriend, initiator);

		return true;
	}
}