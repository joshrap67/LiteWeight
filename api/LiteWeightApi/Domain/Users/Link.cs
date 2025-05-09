using Google.Cloud.Firestore;

namespace LiteWeightAPI.Domain.Users;

[FirestoreData]
public class Link
{
	[FirestoreProperty("url")]
	public required string Url { get; set; }

	[FirestoreProperty("label")]
	public required string? Label { get; set; }
}