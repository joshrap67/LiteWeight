using Google.Cloud.Firestore;
using LiteWeightAPI.Domain.Users;

namespace LiteWeightAPI.Domain.ReceivedWorkouts;

[FirestoreData]
public class ReceivedWorkoutDistinctExercise
{
	// public ctor needed for firebase serialization
	public ReceivedWorkoutDistinctExercise()
	{
	}

	public ReceivedWorkoutDistinctExercise(OwnedExercise userExercise, string exerciseName)
	{
		ExerciseName = exerciseName;
		VideoUrl = userExercise.VideoUrl;
		Focuses = userExercise.Focuses;
	}

	[FirestoreProperty("exerciseName")]
	public string ExerciseName { get; set; } = null!;

	[FirestoreProperty("videoUrl")]
	public string? VideoUrl { get; set; }

	[FirestoreProperty("focuses")]
	public IList<string> Focuses { get; set; } = [];
}