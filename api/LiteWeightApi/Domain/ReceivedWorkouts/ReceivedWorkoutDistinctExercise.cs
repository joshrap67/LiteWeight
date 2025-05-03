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
		Links = userExercise.Links;
		Focuses = userExercise.Focuses;
		Notes = userExercise.Notes;
	}

	[FirestoreProperty("exerciseName")]
	public string ExerciseName { get; set; } = null!;
	
	[FirestoreProperty("notes")]
	public string? Notes { get; set; }

	[FirestoreProperty("videoUrl")]
	public IList<Link> Links { get; set; }

	[FirestoreProperty("focuses")]
	public IList<string> Focuses { get; set; } = [];
}