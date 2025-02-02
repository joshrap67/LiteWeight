namespace LiteWeightAPI.Api.Self.Responses;

public class FriendResponse
{
	/// <summary>
	/// Unique identifier of the user who is a friend.
	/// </summary>
	/// <example>53c68af6-9400-438b-aece-344d4d2024c6</example>
	public string UserId { get; set; } = null!;

	/// <summary>
	/// Username of the friend.
	/// </summary>
	/// <example>greg_egg</example>
	public string Username { get; set; } = null!;

	/// <summary>
	/// File path of the friend's profile picture.
	/// </summary>
	/// <example>66fcc4c3-700e-41e3-b0e5-9f121eb97fa9.jpg</example>
	public string ProfilePicture { get; set; } = null!;

	/// <summary>
	/// Is the friend confirmed? If yes they are a friend, else they are a pending friend (pending until this user accepts the friend request).
	/// </summary>
	public bool Confirmed { get; set; }
}