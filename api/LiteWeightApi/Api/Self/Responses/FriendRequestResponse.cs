namespace LiteWeightAPI.Api.Self.Responses;

public class FriendRequestResponse
{
	/// <summary>
	/// Unique identifier of the user who sent the friend request.
	/// </summary>
	/// <example>dcceafca-7055-4c0d-81f8-0e9ef16c7bdc</example>
	public string UserId { get; set; } = null!;

	/// <summary>
	/// Username of the user who sent the friend request.
	/// </summary>
	/// <example>arthur_v</example>
	public string Username { get; set; } = null!;

	/// <summary>
	/// File path of the sender's profile picture.
	/// </summary>
	/// <example>f5b17d02-0a8f-45b5-a2c9-3410fceb5cd3.jpg</example>
	public string ProfilePicture { get; set; } = null!;

	/// <summary>
	/// Is this friend request seen by the user?
	/// </summary>
	public bool Seen { get; set; }

	/// <summary>
	/// Timestamp of when the request was sent (UTC).
	/// </summary>
	/// <example>2023-04-19T13:43:44.685341Z</example>
	public string SentUtc { get; set; } = null!;
}