using Google.Cloud.Firestore;

namespace LiteWeightAPI.Domain.ReceivedWorkouts;

[FirestoreData]
public class ReceivedDay
{
	[FirestoreProperty("exercises")]
	public IList<ReceivedExercise> Exercises { get; set; } = new List<ReceivedExercise>();

	[FirestoreProperty("tag")]
	public string? Tag { get; set; }

	public void AppendExercise(ReceivedExercise receivedExercise)
	{
		Exercises.Add(receivedExercise);
	}
}