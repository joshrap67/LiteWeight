using Google.Cloud.Firestore;

namespace LiteWeightAPI.Domain.Users;

[FirestoreData]
public class User
{
	[FirestoreDocumentId]
	public string Id { get; set; } = null!;

	[FirestoreProperty("username")]
	public required string Username { get; set; }

	[FirestoreProperty("email")]
	public required string Email { get; set; }

	[FirestoreProperty("profilePicture")]
	public required string ProfilePicture { get; set; }

	[FirestoreProperty("firebaseMessagingToken")]
	public string? FirebaseMessagingToken { get; set; }

	[FirestoreProperty("premiumToken")]
	public string? PremiumToken { get; set; }

	[FirestoreProperty("currentWorkoutId")]
	public string? CurrentWorkoutId { get; set; }

	[FirestoreProperty("workoutsSent")]
	public int WorkoutsSent { get; set; }

	[FirestoreProperty("settings")]
	public required UserSettings Settings { get; set; }

	[FirestoreProperty("workouts")]
	public List<WorkoutInfo> Workouts { get; set; } = [];

	[FirestoreProperty("exercises")]
	public List<OwnedExercise> Exercises { get; set; } = [];

	[FirestoreProperty("friends")]
	public List<Friend> Friends { get; set; } = [];

	[FirestoreProperty("friendRequests")]
	public List<FriendRequest> FriendRequests { get; set; } = [];

	[FirestoreProperty("receivedWorkouts")]
	public List<ReceivedWorkoutInfo> ReceivedWorkouts { get; set; } = [];
}