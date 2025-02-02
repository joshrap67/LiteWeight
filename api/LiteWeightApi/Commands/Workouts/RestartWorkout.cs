using AutoMapper;
using LiteWeightAPI.Api.Self.Responses;
using LiteWeightAPI.Api.Workouts.Responses;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Domain.Workouts;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;
using LiteWeightAPI.Utils;

namespace LiteWeightAPI.Commands.Workouts;

public class RestartWorkout : ICommand<UserAndWorkoutResponse>
{
	public required string UserId { get; set; }
	public required string WorkoutId { get; set; }
	public required SetRoutine Routine { get; set; }
}

public class RestartWorkoutHandler : ICommandHandler<RestartWorkout, UserAndWorkoutResponse>
{
	private readonly IRepository _repository;
	private readonly IMapper _mapper;

	public RestartWorkoutHandler(IRepository repository, IMapper mapper)
	{
		_repository = repository;
		_mapper = mapper;
	}

	public async Task<UserAndWorkoutResponse> HandleAsync(RestartWorkout command)
	{
		var user = (await _repository.GetUser(command.UserId))!;
		var workout = await _repository.GetWorkout(command.WorkoutId);
		var routine = _mapper.Map<Routine>(command.Routine);
		if (workout == null)
		{
			throw new ResourceNotFoundException("Workout");
		}

		ValidationUtils.EnsureWorkoutOwnership(user.Id, workout);

		var workoutInfo = user.Workouts.First(x => x.WorkoutId == command.WorkoutId);
		var exerciseIdToExercise = user.Exercises.ToDictionary(x => x.Id, x => x);
		var completedCount = 0;
		var totalCount = 0;
		foreach (var week in routine.Weeks)
		{
			foreach (var day in week.Days)
			{
				foreach (var routineExercise in day.Exercises)
				{
					totalCount++;
					if (!routineExercise.Completed) continue;

					completedCount++;
					routineExercise.Completed = false;

					if (!user.Settings.UpdateDefaultWeightOnRestart) continue;

					// automatically update default weight with this weight if it's higher than previous
					var exerciseId = routineExercise.ExerciseId;
					var ownedExercise = exerciseIdToExercise[exerciseId];
					if (routineExercise.Weight > ownedExercise.DefaultWeight)
					{
						ownedExercise.DefaultWeight = routineExercise.Weight;
					}
				}
			}
		}

		var completionPercentage = 0.0;
		if (totalCount != 0)
		{
			completionPercentage = (double)completedCount / totalCount;
		}

		var newAverage = CalculateAverage(workoutInfo.AverageWorkoutCompletion, workoutInfo.TimesRestarted,
			completionPercentage);

		workoutInfo.AverageWorkoutCompletion = newAverage;
		workoutInfo.TimesRestarted += 1;
		workoutInfo.CurrentDay = 0;
		workoutInfo.CurrentWeek = 0;
		workout.Routine = routine;

		await _repository.ExecuteBatchWrite(
			workoutsToPut: new List<Workout> { workout },
			usersToPut: new List<User> { user }
		);

		return new UserAndWorkoutResponse
		{
			User = _mapper.Map<UserResponse>(user),
			Workout = _mapper.Map<WorkoutResponse>(workout)
		};
	}

	private static double CalculateAverage(double oldAverage, int oldCount, double newCompletionPercentage)
	{
		return (oldAverage * oldCount + newCompletionPercentage) / (oldCount + 1);
	}
}