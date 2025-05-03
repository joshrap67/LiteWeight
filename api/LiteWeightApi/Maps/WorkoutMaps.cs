using LiteWeightAPI.Api.Workouts.Requests;
using LiteWeightAPI.Api.Workouts.Responses;
using LiteWeightAPI.Commands.Workouts;
using LiteWeightAPI.Domain.Workouts;
using LiteWeightAPI.Utils;

namespace LiteWeightAPI.Maps;

public static class WorkoutMaps
{
	public static CreateWorkout ToCommand(this CreateWorkoutRequest request, string userId)
	{
		return new CreateWorkout
		{
			Name = request.Name,
			UserId = userId,
			SetAsCurrentWorkout = request.SetAsCurrentWorkout,
			Routine = request.Routine.ToCommand()
		};
	}

	public static SetRoutine ToCommand(this SetRoutineRequest request)
	{
		return new SetRoutine
		{
			Weeks = request.Weeks.Select(x => x.ToCommand()).ToList()
		};
	}

	private static SetRoutineWeek ToCommand(this SetRoutineWeekRequest request)
	{
		return new SetRoutineWeek
		{
			Days = request.Days.Select(x => x.ToCommand()).ToList()
		};
	}

	private static SetRoutineDay ToCommand(this SetRoutineDayRequest request)
	{
		return new SetRoutineDay
		{
			Tag = request.Tag,
			Exercises = request.Exercises.Select(x => x.ToCommand()).ToList()
		};
	}

	private static SetRoutineExercise ToCommand(this SetRoutineExerciseRequest request)
	{
		return new SetRoutineExercise
		{
			ExerciseId = request.ExerciseId,
			Completed = request.Completed,
			Instructions = request.Instructions,
			Weight = request.Weight,
			Reps = request.Reps,
			Sets = request.Sets
		};
	}

	public static Routine ToDomain(this SetRoutine command)
	{
		return new Routine
		{
			Weeks = command.Weeks.Select(x => x.ToDomain()).ToList()
		};
	}

	private static RoutineWeek ToDomain(this SetRoutineWeek command)
	{
		return new RoutineWeek
		{
			Days = command.Days.Select(x => x.ToDomain()).ToList()
		};
	}

	private static RoutineDay ToDomain(this SetRoutineDay command)
	{
		return new RoutineDay
		{
			Tag = command.Tag,
			Exercises = command.Exercises.Select(x => x.ToDomain()).ToList()
		};
	}

	private static RoutineExercise ToDomain(this SetRoutineExercise command)
	{
		return new RoutineExercise
		{
			ExerciseId = command.ExerciseId,
			Completed = command.Completed,
			Instructions = command.Instructions,
			Weight = command.Weight,
			Reps = command.Reps,
			Sets = command.Sets
		};
	}

	public static WorkoutResponse ToResponse(this Workout workout)
	{
		return new WorkoutResponse
		{
			Id = workout.Id,
			Name = workout.Name,
			CreationUtc = ParsingUtils.ConvertInstantToString(workout.CreationUtc),
			CreatorId = workout.CreatorId,
			Routine = workout.Routine.ToResponse()
		};
	}

	private static RoutineResponse ToResponse(this Routine routine)
	{
		return new RoutineResponse
		{
			Weeks = routine.Weeks.Select(x => x.ToResponse()).ToList()
		};
	}

	private static RoutineWeekResponse ToResponse(this RoutineWeek routineWeek)
	{
		return new RoutineWeekResponse
		{
			Days = routineWeek.Days.Select(x => x.ToResponse()).ToList()
		};
	}

	private static RoutineDayResponse ToResponse(this RoutineDay routineDay)
	{
		return new RoutineDayResponse
		{
			Tag = routineDay.Tag,
			Exercises = routineDay.Exercises.Select(x => x.ToResponse()).ToList()
		};
	}

	private static RoutineExerciseResponse ToResponse(this RoutineExercise routineExercise)
	{
		return new RoutineExerciseResponse
		{
			ExerciseId = routineExercise.ExerciseId,
			Completed = routineExercise.Completed,
			Instructions = routineExercise.Instructions,
			Weight = routineExercise.Weight,
			Reps = routineExercise.Reps,
			Sets = routineExercise.Sets
		};
	}
}