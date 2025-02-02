using Google.Cloud.Firestore;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Domain.Workouts;

namespace LiteWeightAPI.Domain.ReceivedWorkouts;

[FirestoreData]
public class ReceivedRoutine
{
	// public ctor needed for firebase serialization
	public ReceivedRoutine()
	{
	}

	public ReceivedRoutine(Routine routine, IEnumerable<OwnedExercise> ownedExercises)
	{
		var exerciseIdToExercise = ownedExercises.ToDictionary(x => x.Id, x => x);
		Weeks = new List<ReceivedWeek>();
		foreach (var week in routine.Weeks)
		{
			var receivedWeek = new ReceivedWeek();
			foreach (var day in week.Days)
			{
				var receivedDay = new ReceivedDay { Tag = day.Tag };
				foreach (var exercise in day.Exercises)
				{
					var ownedExercise = exerciseIdToExercise[exercise.ExerciseId];
					var receivedExercise = new ReceivedExercise(exercise, ownedExercise.Name);
					receivedDay.AppendExercise(receivedExercise);
				}

				receivedWeek.AppendDay(receivedDay);
			}

			AppendWeek(receivedWeek);
		}
	}

	[FirestoreProperty("weeks")]
	public IList<ReceivedWeek> Weeks { get; set; }

	private void AppendWeek(ReceivedWeek receivedWeek)
	{
		Weeks.Add(receivedWeek);
	}
}