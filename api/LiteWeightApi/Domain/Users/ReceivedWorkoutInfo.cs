using Google.Cloud.Firestore;
using LiteWeightAPI.Domain.Converters;
using NodaTime;

namespace LiteWeightAPI.Domain.Users;

[FirestoreData]
public class ReceivedWorkoutInfo
{
	[FirestoreProperty("receivedWorkoutId")]
	public required string ReceivedWorkoutId { get; set; }

	[FirestoreProperty("workoutName")]
	public required string WorkoutName { get; set; }

	[FirestoreProperty("receivedUtc", ConverterType = typeof(InstantConverter))]
	public Instant ReceivedUtc { get; set; }

	[FirestoreProperty("seen")]
	public bool Seen { get; set; }

	[FirestoreProperty("senderId")]
	public required string SenderId { get; set; }

	[FirestoreProperty("senderUsername")]
	public required string SenderUsername { get; set; }

	[FirestoreProperty("senderProfilePicture")]
	public required string SenderProfilePicture { get; set; }

	[FirestoreProperty("totalDays")]
	public int TotalDays { get; set; }

	[FirestoreProperty("mostFrequentFocus")]
	public required string MostFrequentFocus { get; set; }
}