using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Domain.Workouts;

namespace LiteWeightAPI.Services;

public interface IStatisticsService
{
	string FindMostFrequentFocus(Dictionary<string, OwnedExercise> exerciseIdToExercise, Routine routine);
}

public class StatisticsService : IStatisticsService
{
	public string FindMostFrequentFocus(Dictionary<string, OwnedExercise> exerciseIdToExercise, Routine routine)
	{
		var focusCount = new Dictionary<string, int>();
		foreach (var week in routine.Weeks)
		{
			foreach (var day in week.Days)
			{
				foreach (var routineExercise in day.Exercises)
				{
					var exerciseId = routineExercise.ExerciseId;
					foreach (var focus in exerciseIdToExercise[exerciseId].Focuses)
					{
						if (!focusCount.TryAdd(focus, 1))
						{
							focusCount[focus]++;
						}
					}
				}
			}
		}

		if (focusCount.Values.Count == 0)
		{
			return "";
		}
		var max = focusCount.Values.Max();

		var maxFocuses = (from focus in focusCount.Keys
				let count = focusCount[focus]
				where count == max
				select focus)
			.ToList();

		var retVal = string.Join(",", maxFocuses);
		return retVal;
	}
}