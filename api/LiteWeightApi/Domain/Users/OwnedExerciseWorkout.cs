using Google.Cloud.Firestore;

namespace LiteWeightAPI.Domain.Users;

[FirestoreData]
public class OwnedExerciseWorkout
{
	[FirestoreProperty("workoutId")]
	public required string WorkoutId { get; set; }

	[FirestoreProperty("workoutName")]
	public required string WorkoutName { get; set; }
}