using LiteWeightAPI.Api.Common.Responses;
using LiteWeightAPI.Api.Exercises.Requests;
using LiteWeightAPI.Api.Exercises.Responses;
using LiteWeightAPI.Commands.Common;
using LiteWeightAPI.Commands.Exercises;
using LiteWeightAPI.Domain.Users;

namespace LiteWeightAPI.Maps;

public static class ExerciseMaps
{
	public static CreateExercise ToCommand(this SetExerciseRequest request, string userId)
	{
		return new CreateExercise
		{
			UserId = userId,
			Name = request.Name,
			Links = request.Links.Select(x => new SetLink { Label = x.Label, Url = x.Url }).ToList(),
			Focuses = request.Focuses,
			DefaultWeight = request.DefaultWeight,
			DefaultSets = request.DefaultSets,
			DefaultReps = request.DefaultReps,
			Notes = request.Notes
		};
	}

	public static UpdateExercise ToCommand(this SetExerciseRequest request, string exerciseId, string userId)
	{
		return new UpdateExercise
		{
			ExerciseId = exerciseId,
			UserId = userId,
			Name = request.Name,
			Links = request.Links.Select(x => new SetLink { Label = x.Label, Url = x.Url }).ToList(),
			Focuses = request.Focuses,
			DefaultWeight = request.DefaultWeight,
			DefaultSets = request.DefaultSets,
			DefaultReps = request.DefaultReps,
			Notes = request.Notes
		};
	}

	public static OwnedExercise ToDomain(this CreateExercise request)
	{
		return new OwnedExercise
		{
			Name = request.Name,
			Links = request.Links.Select(x => new Link { Label = x.Label, Url = x.Url }).ToList(),
			Focuses = request.Focuses,
			DefaultWeight = request.DefaultWeight,
			DefaultSets = request.DefaultSets,
			DefaultReps = request.DefaultReps,
			Notes = request.Notes
		};
	}

	private static OwnedExerciseWorkoutResponse ToResponse(this OwnedExerciseWorkout workout)
	{
		return new OwnedExerciseWorkoutResponse
		{
			WorkoutId = workout.WorkoutId,
			WorkoutName = workout.WorkoutName
		};
	}

	public static OwnedExerciseResponse ToResponse(this OwnedExercise response)
	{
		return new OwnedExerciseResponse
		{
			Id = response.Id,
			Name = response.Name,
			Links = response.Links.Select(x => new LinkResponse { Label = x.Label, Url = x.Url }).ToList(),
			Focuses = response.Focuses,
			DefaultWeight = response.DefaultWeight,
			DefaultSets = response.DefaultSets,
			DefaultReps = response.DefaultReps,
			Notes = response.Notes,
			Workouts = response.Workouts.Select(x => x.ToResponse()).ToList()
		};
	}
}