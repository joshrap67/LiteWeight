using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;
using LiteWeightAPI.Services;

namespace LiteWeightAPI.Commands.Users;

public class CancelFriendRequest : ICommand<bool>
{
	public required string InitiatorUserId { get; set; }
	public required string UserIdToCancel { get; set; }
}

public class CancelFriendRequestHandler : ICommandHandler<CancelFriendRequest, bool>
{
	private readonly IRepository _repository;
	private readonly IPushNotificationService _pushNotificationService;

	public CancelFriendRequestHandler(IRepository repository, IPushNotificationService pushNotificationService)
	{
		_repository = repository;
		_pushNotificationService = pushNotificationService;
	}

	public async Task<bool> HandleAsync(CancelFriendRequest command)
	{
		var initiator = (await _repository.GetUser(command.InitiatorUserId))!;
		var userToCancel = await _repository.GetUser(command.UserIdToCancel);

		if (userToCancel == null)
		{
			throw new ResourceNotFoundException("User");
		}

		var pendingFriend = initiator.Friends.FirstOrDefault(x => x.UserId == command.UserIdToCancel);
		if (pendingFriend == null) return false;

		initiator.Friends.Remove(pendingFriend);

		var initiatorFriendRequest =
			userToCancel.FriendRequests.FirstOrDefault(x => x.UserId == command.InitiatorUserId);
		if (initiatorFriendRequest == null) return false;

		userToCancel.FriendRequests.Remove(initiatorFriendRequest);

		await _repository.ExecuteBatchWrite(usersToPut: new List<User> { initiator, userToCancel });

		// send a notification to the canceled user
		await _pushNotificationService.SendFriendRequestCanceledNotification(userToCancel, initiator);

		return true;
	}
}