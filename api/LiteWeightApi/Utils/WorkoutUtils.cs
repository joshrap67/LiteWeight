using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Domain.Workouts;

namespace LiteWeightAPI.Utils;

public static class WorkoutUtils
{
	public static void UpdateOwnedExercisesOnCreation(User user, Workout newWorkout, bool updateWeight)
	{
		var updateDefaultWeight = user.Settings.UpdateDefaultWeightOnSave && updateWeight;
		var exerciseIds = new HashSet<string>();
		var exerciseIdToExercise = user.Exercises.ToDictionary(x => x.Id, x => x);
		foreach (var week in newWorkout.Routine.Weeks)
		{
			foreach (var day in week.Days)
			{
				foreach (var routineExercise in day.Exercises)
				{
					var exerciseId = routineExercise.ExerciseId;
					var ownedExercise = exerciseIdToExercise[exerciseId];
					if (updateDefaultWeight && routineExercise.Weight > ownedExercise.DefaultWeight)
					{
						ownedExercise.DefaultWeight = routineExercise.Weight;
					}

					exerciseIds.Add(exerciseId);
				}
			}
		}

		foreach (var exerciseId in exerciseIds)
		{
			exerciseIdToExercise[exerciseId].Workouts.Add(new OwnedExerciseWorkout
			{
				WorkoutId = newWorkout.Id,
				WorkoutName = newWorkout.Name
			});
		}
	}

	public static void FixCurrentDayAndWeek(Workout editedWorkout, WorkoutInfo workoutInfo)
	{
		// make sure that the current week according to the request is actually valid
		var currentDay = workoutInfo.CurrentDay;
		var currentWeek = workoutInfo.CurrentWeek;
		if (currentWeek < 0 || currentWeek >= editedWorkout.Routine.Weeks.Count)
		{
			// request incorrectly set the current week probably from deleting it
			var newWeekIndex = editedWorkout.Routine.Weeks.Count - 1;
			if (newWeekIndex < 0)
			{
				newWeekIndex = 0;
			}

			var newDayIndex = newWeekIndex > 0
				? editedWorkout.Routine.Weeks[newWeekIndex].Days.Count - 1
				: 0; // atm is overkill since app doesn't allow 0 weeks, but adding this check just in case that changes
			if (newDayIndex < 0)
			{
				newDayIndex = 0;
			}

			workoutInfo.CurrentWeek = newWeekIndex;
			workoutInfo.CurrentDay = newDayIndex;
		}
		else if (currentDay < 0 || currentDay >= editedWorkout.Routine.Weeks[currentWeek].Days.Count)
		{
			var newDayIndex = editedWorkout.Routine.Weeks[currentWeek].Days.Count - 1;
			if (newDayIndex < 0)
			{
				newDayIndex = 0;
			}

			workoutInfo.CurrentDay = newDayIndex;
		}
	}
}