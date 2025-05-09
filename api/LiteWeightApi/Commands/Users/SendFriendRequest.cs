using LiteWeightAPI.Api.Self.Responses;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Errors.Exceptions;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;
using LiteWeightAPI.Imports;
using LiteWeightAPI.Maps;
using LiteWeightAPI.Services;
using NodaTime;

namespace LiteWeightAPI.Commands.Users;

public class SendFriendRequest : ICommand<FriendResponse>
{
	public required string SenderId { get; init; }
	public required string RecipientId { get; init; }
}

public class SendFriendRequestHandler : ICommandHandler<SendFriendRequest, FriendResponse>
{
	private readonly IRepository _repository;
	private readonly IClock _clock;
	private readonly IPushNotificationService _pushNotificationService;

	public SendFriendRequestHandler(IRepository repository, IClock clock,
		IPushNotificationService pushNotificationService)
	{
		_repository = repository;
		_clock = clock;
		_pushNotificationService = pushNotificationService;
	}

	public async Task<FriendResponse> HandleAsync(SendFriendRequest command)
	{
		var recipientId = command.RecipientId;
		var senderId = command.SenderId;
		if (recipientId.Equals(command.SenderId, StringComparison.InvariantCultureIgnoreCase))
		{
			throw new MiscErrorException("Cannot send a friend request to yourself");
		}

		var senderUser = (await _repository.GetUser(senderId))!;
		var recipientUser = await _repository.GetUser(recipientId);

		// validation
		if (recipientUser == null)
		{
			throw new ResourceNotFoundException("User");
		}

		var senderUserId = senderUser.Id;

		if (recipientUser.Settings.PrivateAccount)
		{
			throw new ResourceNotFoundException("User");
		}

		if (senderUser.Friends.Count >= Globals.MaxNumberFriends)
		{
			throw new MaxLimitException("Max number of friends would be exceeded");
		}

		if (recipientUser.FriendRequests.Count >= Globals.MaxFriendRequests)
		{
			throw new MaxLimitException($"{recipientId} has too many pending requests");
		}

		if (senderUser.FriendRequests.Any(x => x.UserId == recipientId))
		{
			throw new MiscErrorException("This user has already sent you a friend request");
		}

		// if already sent or friends, fail gracefully
		var existingFriend = senderUser.Friends.FirstOrDefault(x => x.UserId == recipientId);
		if (existingFriend != null)
		{
			return existingFriend.ToResponse();
		}

		var friendToAdd = new Friend
		{
			Username = recipientUser.Username,
			UserId = recipientUser.Id,
			ProfilePicture = recipientUser.ProfilePicture
		};
		var now = _clock.GetCurrentInstant();
		var friendRequest = new FriendRequest
		{
			UserId = senderUserId,
			Username = senderUser.Username,
			ProfilePicture = senderUser.ProfilePicture,
			SentUtc = now
		};
		senderUser.Friends.Add(friendToAdd);
		recipientUser.FriendRequests.Add(friendRequest);

		await _repository.ExecuteBatchWrite(usersToPut: new List<User> { senderUser, recipientUser });

		// send a notification to the user that received the friend request
		await _pushNotificationService.SendNewFriendRequestNotification(recipientUser, friendRequest);

		return friendToAdd.ToResponse();
	}
}