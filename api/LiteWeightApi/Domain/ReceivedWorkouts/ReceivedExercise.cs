using Google.Cloud.Firestore;
using LiteWeightAPI.Domain.Workouts;

namespace LiteWeightAPI.Domain.ReceivedWorkouts;

[FirestoreData]
public class ReceivedExercise
{
	// public ctor needed for firebase serialization
	public ReceivedExercise()
	{
	}

	public ReceivedExercise(RoutineExercise exercise, string exerciseName)
	{
		ExerciseName = exerciseName;
		Weight = exercise.Weight;
		Sets = exercise.Sets;
		Reps = exercise.Reps;
		Instructions = exercise.Instructions;
	}

	[FirestoreProperty("exerciseName")]
	public string ExerciseName { get; set; } = null!;

	[FirestoreProperty("weight")]
	public double Weight { get; set; }

	[FirestoreProperty("sets")]
	public int Sets { get; set; }

	[FirestoreProperty("reps")]
	public int Reps { get; set; }

	[FirestoreProperty("instructions")]
	public string? Instructions { get; set; }
}