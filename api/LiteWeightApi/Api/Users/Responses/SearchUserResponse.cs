namespace LiteWeightAPI.Api.Users.Responses;

public class SearchUserResponse
{
	/// <summary>
	/// Id of the user.
	/// </summary>
	/// <example>27342d1c-8324-448c-8d57-9c345d319953</example>
	public string Id { get; set; } = null!;

	/// <summary>
	/// Username of the user
	/// </summary>
	/// <example>greg_egg</example>
	public string Username { get; set; } = null!;

	/// <summary>
	/// File path of the user's profile picture.
	/// </summary>
	/// <example>0f1d96c3-ca22-4657-9f9b-136bb4621985.jpg</example>
	public string ProfilePicture { get; set; } = null!;
}