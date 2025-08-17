using Google.Cloud.Firestore;

namespace LiteWeightAPI.Domain.Workouts;

[FirestoreData]
public class RoutineExercise
{
	[FirestoreProperty("completed")]
	public bool Completed { get; set; }

	[FirestoreProperty("exerciseId")]
	public required string ExerciseId { get; set; }

	[FirestoreProperty("weight")]
	public double Weight { get; set; } // stored in lbs

	[FirestoreProperty("sets")]
	public int Sets { get; set; }

	[FirestoreProperty("reps")]
	public int Reps { get; set; }

	[FirestoreProperty("instructions")]
	public string? Instructions { get; set; }

	public RoutineExercise Clone(bool copyCompleted = false)
	{
		var copy = new RoutineExercise
		{
			Completed = copyCompleted && Completed,
			Instructions = Instructions,
			Reps = Reps,
			Sets = Sets,
			Weight = Weight,
			ExerciseId = ExerciseId
		};
		return copy;
	}
}