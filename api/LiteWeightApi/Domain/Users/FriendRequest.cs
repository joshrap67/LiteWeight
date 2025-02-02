using Google.Cloud.Firestore;
using LiteWeightAPI.Domain.Converters;
using NodaTime;

namespace LiteWeightAPI.Domain.Users;

[FirestoreData]
public class FriendRequest
{
	[FirestoreProperty("userId")]
	public required string UserId { get; set; }

	[FirestoreProperty("username")]
	public required string Username { get; set; }

	[FirestoreProperty("profilePicture")]
	public required string ProfilePicture { get; set; }

	[FirestoreProperty("seen")]
	public bool Seen { get; set; }

	[FirestoreProperty("sentUtc", ConverterType = typeof(InstantConverter))]
	public Instant SentUtc { get; set; }
}