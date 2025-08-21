using Google.Cloud.Firestore;

namespace LiteWeightAPI.Domain.Workouts;

[FirestoreData]
public class RoutineDay
{
	[FirestoreProperty("exercises")]
	public IList<RoutineExercise> Exercises { get; set; } = new List<RoutineExercise>();

	[FirestoreProperty("tag")]
	public string? Tag { get; set; }

	public RoutineDay Clone()
	{
		var clonedDay = new RoutineDay { Tag = Tag };
		foreach (var exercise in Exercises)
		{
			clonedDay.Exercises.Add(exercise.Clone());
		}

		return clonedDay;
	}

	public void AppendExercise(RoutineExercise exercise)
	{
		Exercises.Add(exercise);
	}

	public void DeleteExercise(string exerciseId)
	{
		var exercise = Exercises.FirstOrDefault(x => x.ExerciseId == exerciseId);
		if (exercise != null)
		{
			Exercises.Remove(exercise);
		}
	}
}