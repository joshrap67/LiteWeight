using Google.Cloud.Firestore;
using LiteWeightAPI.Domain.Converters;
using NodaTime;

namespace LiteWeightAPI.Domain.Workouts;

[FirestoreData]
public class Workout
{
	[FirestoreDocumentId]
	public string Id { get; set; } = null!;

	[FirestoreProperty("name")]
	public required string Name { get; set; }

	[FirestoreProperty("creationUtc", ConverterType = typeof(InstantConverter))]
	public Instant CreationUtc { get; set; }

	[FirestoreProperty("creatorId")]
	public required string CreatorId { get; set; }

	[FirestoreProperty("routine")]
	public required Routine Routine { get; set; }
}