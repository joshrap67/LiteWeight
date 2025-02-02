using LiteWeightAPI.Domain.ReceivedWorkouts;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Domain.Workouts;
using LiteWeightAPI.Errors.Exceptions;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;

namespace LiteWeightAPI.Utils;

public static class ValidationUtils
{
	public static void EnsureWorkoutOwnership(string userId, Workout workout)
	{
		if (workout.CreatorId != userId)
		{
			throw new ForbiddenException("User does not have permissions to access workout");
		}
	}

	public static void EnsureReceivedWorkoutOwnership(string userId, ReceivedWorkout receivedWorkout)
	{
		if (receivedWorkout.RecipientId != userId)
		{
			throw new ForbiddenException("User does not have permissions to access received workout");
		}
	}

	public static void ValidWorkoutName(string workoutName, User user)
	{
		if (user.Workouts.Any(x => x.WorkoutName == workoutName))
		{
			throw new AlreadyExistsException("Workout name already exists");
		}
	}
}