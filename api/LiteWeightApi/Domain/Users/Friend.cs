using Google.Cloud.Firestore;

namespace LiteWeightAPI.Domain.Users;

[FirestoreData]
public class Friend
{
	[FirestoreProperty("userId")]
	public required string UserId { get; set; }

	[FirestoreProperty("username")]
	public required string Username { get; set; }

	[FirestoreProperty("profilePicture")]
	public required string ProfilePicture { get; set; }

	[FirestoreProperty("confirmed")]
	public bool Confirmed { get; set; }
}