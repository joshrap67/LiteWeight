using LiteWeightAPI.Api.ReceivedWorkouts.Responses;
using LiteWeightAPI.Domain.ReceivedWorkouts;

namespace LiteWeightAPI.Maps;

public static class ReceivedWorkoutMaps
{
	public static ReceivedWorkoutResponse ToResponse(this ReceivedWorkout receivedWorkout)
	{
		return new ReceivedWorkoutResponse
		{
			Id = receivedWorkout.Id,
			WorkoutName = receivedWorkout.WorkoutName,
			RecipientId = receivedWorkout.RecipientId,
			SenderId = receivedWorkout.SenderId,
			SenderUsername = receivedWorkout.SenderUsername,
			Routine = receivedWorkout.Routine.ToResponse(),
			DistinctExercises = receivedWorkout.DistinctExercises.Select(x => x.ToResponse()).ToList()
		};
	}

	private static ReceivedRoutineResponse ToResponse(this ReceivedRoutine routine)
	{
		return new ReceivedRoutineResponse
		{
			Weeks = routine.Weeks.Select(x => x.ToResponse()).ToList()
		};
	}

	private static ReceivedWeekResponse ToResponse(this ReceivedWeek responseWeek)
	{
		return new ReceivedWeekResponse
		{
			Days = responseWeek.Days.Select(x => x.ToResponse()).ToList()
		};
	}

	private static ReceivedDayResponse ToResponse(this ReceivedDay responseDay)
	{
		return new ReceivedDayResponse
		{
			Tag = responseDay.Tag,
			Exercises = responseDay.Exercises.Select(x => x.ToResponse()).ToList()
		};
	}

	private static ReceivedExerciseResponse ToResponse(this ReceivedExercise responseExercise)
	{
		return new ReceivedExerciseResponse
		{
			ExerciseName = responseExercise.ExerciseName,
			Instructions = responseExercise.Instructions,
			Weight = responseExercise.Weight,
			Sets = responseExercise.Sets,
			Reps = responseExercise.Reps
		};
	}

	private static ReceivedWorkoutDistinctExerciseResponse ToResponse(this ReceivedWorkoutDistinctExercise exercise)
	{
		return new ReceivedWorkoutDistinctExerciseResponse
		{
			Focuses = exercise.Focuses,
			Links = exercise.Links.Select(x => x.ToResponse()).ToList(),
			ExerciseName = exercise.ExerciseName,
			Notes = exercise.Notes
		};
	}
}