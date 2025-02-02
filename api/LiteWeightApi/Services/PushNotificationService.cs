using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Services.Notifications;
using LiteWeightAPI.Services.Notifications.Responses;
using LiteWeightAPI.Utils;

namespace LiteWeightAPI.Services;

public interface IPushNotificationService
{
	Task SendReceivedWorkoutPushNotification(User recipientUser, ReceivedWorkoutInfo receivedWorkoutInfo);
	Task SendNewFriendRequestNotification(User recipientUser, FriendRequest friendRequest);
	Task SendFriendRequestAcceptedNotification(User acceptedUser, User initiator);
	Task SendFriendRequestCanceledNotification(User canceledUser, User initiator);
	Task SendFriendRequestDeclinedNotification(User declinedUser, User initiator);
	Task SendRemovedAsFriendNotification(User removedFriend, User initiator);
}

public class PushNotificationService : IPushNotificationService
{
	private readonly IFcmService _fcmService;

	public PushNotificationService(IFcmService fcmService)
	{
		_fcmService = fcmService;
	}

	private const string FriendRequestAction = "friendRequest";
	private const string CanceledFriendRequestAction = "canceledFriendRequest";
	private const string AcceptedFriendRequestAction = "acceptedFriendRequest";
	private const string DeclinedFriendRequestAction = "declinedFriendRequest";
	private const string RemovedAsFriendAction = "removedAsFriend";
	private const string ReceivedWorkoutAction = "receivedWorkout";

	public async Task SendReceivedWorkoutPushNotification(User recipientUser, ReceivedWorkoutInfo receivedWorkoutInfo)
	{
		await _fcmService.SendPushNotification(recipientUser.FirebaseMessagingToken,
			new NotificationData
			{
				Action = ReceivedWorkoutAction,
				JsonPayload = JsonUtils.Serialize(receivedWorkoutInfo)
			});
	}

	public async Task SendNewFriendRequestNotification(User recipientUser, FriendRequest friendRequest)
	{
		await _fcmService.SendPushNotification(recipientUser.FirebaseMessagingToken,
			new NotificationData
			{
				Action = FriendRequestAction,
				JsonPayload = JsonUtils.Serialize(friendRequest)
			});
	}

	public async Task SendFriendRequestAcceptedNotification(User acceptedUser, User initiator)
	{
		await _fcmService.SendPushNotification(acceptedUser.FirebaseMessagingToken,
			new NotificationData
			{
				Action = AcceptedFriendRequestAction,
				JsonPayload = JsonUtils.Serialize(new AcceptedFriendRequestResponse
					{ UserId = initiator.Id, Username = initiator.Username })
			});
	}

	public async Task SendFriendRequestCanceledNotification(User canceledUser, User initiator)
	{
		await _fcmService.SendPushNotification(canceledUser.FirebaseMessagingToken,
			new NotificationData
			{
				Action = CanceledFriendRequestAction,
				JsonPayload = JsonUtils.Serialize(new CanceledFriendRequestResponse
					{ UserId = initiator.Id, Username = initiator.Username })
			});
	}

	public async Task SendFriendRequestDeclinedNotification(User declinedUser, User initiator)
	{
		await _fcmService.SendPushNotification(declinedUser.FirebaseMessagingToken,
			new NotificationData
			{
				Action = DeclinedFriendRequestAction,
				JsonPayload = JsonUtils.Serialize(new DeclinedFriendRequestResponse
					{ UserId = initiator.Id, Username = initiator.Username })
			});
	}

	public async Task SendRemovedAsFriendNotification(User removedFriend, User initiator)
	{
		await _fcmService.SendPushNotification(removedFriend.FirebaseMessagingToken,
			new NotificationData
			{
				Action = RemovedAsFriendAction,
				JsonPayload = JsonUtils.Serialize(new RemovedAsFriendResponse
					{ UserId = initiator.Id, Username = initiator.Username })
			});
	}
}