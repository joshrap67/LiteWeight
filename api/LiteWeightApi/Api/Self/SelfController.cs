using System.Text.Json.Nodes;
using LiteWeightAPI.Api.Self.Requests;
using LiteWeightAPI.Api.Self.Responses;
using LiteWeightAPI.Commands;
using LiteWeightAPI.Commands.Self;
using LiteWeightAPI.Errors.Attributes;
using LiteWeightAPI.Imports;
using LiteWeightAPI.Maps;
using LiteWeightAPI.Utils;
using Microsoft.AspNetCore.Mvc;

namespace LiteWeightAPI.Api.Self;

[Route("self")]
[ApiController]
public class SelfController : BaseController
{
	private readonly ICommandDispatcher _dispatcher;

	public SelfController(ICommandDispatcher dispatcher)
	{
		_dispatcher = dispatcher;
	}

	/// <summary>Get Self</summary>
	/// <remarks>Returns the user that is currently authenticated.</remarks>
	[HttpGet]
	[ProducesResponseType(StatusCodes.Status200OK)]
	[ProducesResponseType(StatusCodes.Status404NotFound)]
	public async Task<ActionResult<UserResponse>> GetSelf()
	{
		var user = await _dispatcher.DispatchAsync<GetSelf, UserResponse>(new GetSelf
		{
			UserId = CurrentUserId
		});
		return user;
	}

	/// <summary>Create Self</summary>
	/// <remarks>
	/// Creates a user in the database using the email/firebase id in the authenticated token.
	/// <br/><br/>Note that a single verified, authenticated user can only exist once in the database - this is determined by the firebase UUID in the authenticated token.
	/// </remarks>
	[HttpPost]
	[AlreadyExists, InvalidRequest]
	[ProducesResponseType(StatusCodes.Status201Created)]
	public async Task<ActionResult<UserResponse>> CreateSelf(CreateSelfRequest request)
	{
		var firebaseClaim = HttpContext.User.Claims.ToList().FirstOrDefault(x => x.Type == "firebase");
		var currentUserEmail = "";
		if (firebaseClaim != null)
		{
			var deserializedToken = JsonUtils.Deserialize<JsonNode>(firebaseClaim.Value);
			var email = deserializedToken?["identities"]?["email"]?[0]?.GetValue<string>();
			currentUserEmail = email ?? "";
		}

		var command = request.ToCommand(CurrentUserId, currentUserEmail);

		var user = await _dispatcher.DispatchAsync<CreateSelf, UserResponse>(command);
		return new CreatedResult(new Uri("/self", UriKind.Relative), user);
	}

	/// <summary>Update Profile Picture</summary>
	/// <remarks>Updates the user's profile picture. Note that it simply replaces the old picture using the same image url.</remarks>
	[HttpPut("profile-picture")]
	[InvalidRequest]
	[ProducesResponseType(StatusCodes.Status204NoContent)]
	[ProducesResponseType(StatusCodes.Status404NotFound)]
	public async Task<ActionResult> UpdateProfilePicture(UpdateProfilePictureRequest request)
	{
		await _dispatcher.DispatchAsync<UpdateProfilePicture, bool>(new UpdateProfilePicture
		{
			UserId = CurrentUserId, ImageData = request.ProfilePictureData
		});
		return NoContent();
	}

	/// <summary>Set Firebase Messaging Token</summary>
	/// <remarks>Sets the firebase messaging token to the authenticated user. This enables the authenticated user's ability to receive push notifications, or removes it if the token is null.</remarks>
	[HttpPut("set-firebase-messaging-token")]
	[ProducesResponseType(StatusCodes.Status204NoContent)]
	[ProducesResponseType(StatusCodes.Status404NotFound)]
	public async Task<ActionResult> SetFirebaseMessagingToken(SetFirebaseMessagingTokenRequest request)
	{
		await _dispatcher.DispatchAsync<SetFirebaseMessagingToken, bool>(new SetFirebaseMessagingToken
		{
			UserId = CurrentUserId, Token = request.FirebaseMessagingToken
		});
		return NoContent();
	}

	/// <summary>Set All Friend Requests Seen</summary>
	/// <remarks>Sets all friend requests on the authenticated user as seen.</remarks>
	[HttpPut("all-friend-requests-seen")]
	[ProducesResponseType(StatusCodes.Status204NoContent)]
	[ProducesResponseType(StatusCodes.Status404NotFound)]
	public async Task<ActionResult> SetAllFriendRequestsSeen()
	{
		await _dispatcher.DispatchAsync<SetAllFriendRequestsSeen, bool>(new SetAllFriendRequestsSeen
		{
			UserId = CurrentUserId
		});
		return NoContent();
	}

	/// <summary>Set Settings</summary>
	/// <remarks>Sets the settings on the authenticated user.</remarks>
	[HttpPut("settings")]
	[InvalidRequest]
	[ProducesResponseType(StatusCodes.Status204NoContent)]
	[ProducesResponseType(StatusCodes.Status404NotFound)]
	public async Task<ActionResult> SetSettings(UserSettingsResponse request)
	{
		var command = request.ToCommand(CurrentUserId);

		await _dispatcher.DispatchAsync<SetSettings, bool>(command);
		return NoContent();
	}

	/// <summary>Set Current Workout</summary>
	/// <remarks>Sets the current workout of the authenticated user to the specified workout, if it exists.</remarks>
	[HttpPut("current-workout")]
	[WorkoutNotFound]
	[ProducesResponseType(StatusCodes.Status204NoContent)]
	[ProducesResponseType(StatusCodes.Status404NotFound)]
	public async Task<ActionResult> SetCurrentWorkout(SetCurrentWorkoutRequest request)
	{
		await _dispatcher.DispatchAsync<SetCurrentWorkout, bool>(new SetCurrentWorkout
		{
			UserId = CurrentUserId, CurrentWorkoutId = request.CurrentWorkoutId
		});
		return NoContent();
	}

	/// <summary>Delete Self</summary>
	/// <remarks>
	/// Deletes a user and all data associated with it.<br/><br/>
	/// Data deleted: user, workouts belonging to user, images belonging to user. The user is also removed as a friend from any other users, and any friend requests they sent are canceled.
	/// </remarks>
	[HttpDelete]
	[PushNotification]
	[ProducesResponseType(StatusCodes.Status204NoContent)]
	[ProducesResponseType(StatusCodes.Status404NotFound)]
	public async Task<ActionResult> DeleteSelf()
	{
		await _dispatcher.DispatchAsync<DeleteSelf, bool>(new DeleteSelf { UserId = CurrentUserId });
		return NoContent();
	}
}