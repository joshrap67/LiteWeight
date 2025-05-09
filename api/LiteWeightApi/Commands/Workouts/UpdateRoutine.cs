using LiteWeightAPI.Api.Workouts.Responses;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Domain.Workouts;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;
using LiteWeightAPI.Maps;
using LiteWeightAPI.Utils;

namespace LiteWeightAPI.Commands.Workouts;

public class UpdateRoutine : ICommand<UserAndWorkoutResponse>
{
	public required string UserId { get; set; }
	public required string WorkoutId { get; set; }
	public required SetRoutine Routine { get; set; }
}

public class UpdateRoutineHandler : ICommandHandler<UpdateRoutine, UserAndWorkoutResponse>
{
	private readonly IRepository _repository;

	public UpdateRoutineHandler(IRepository repository)
	{
		_repository = repository;
	}

	public async Task<UserAndWorkoutResponse> HandleAsync(UpdateRoutine command)
	{
		var user = (await _repository.GetUser(command.UserId))!;
		var workout = await _repository.GetWorkout(command.WorkoutId);
		var routine = command.Routine.ToDomain();

		if (workout == null)
		{
			throw new ResourceNotFoundException("Workout");
		}

		ValidationUtils.EnsureWorkoutOwnership(user.Id, workout);

		UpdateOwnedExercisesOnEdit(user, routine, workout);
		workout.Routine = routine;
		WorkoutUtils.FixCurrentDayAndWeek(workout, user.Workouts.First(x => x.WorkoutId == command.WorkoutId));

		await _repository.ExecuteBatchWrite(
			workoutsToPut: new List<Workout> { workout },
			usersToPut: new List<User> { user }
		);

		return new UserAndWorkoutResponse
		{
			User = user.ToResponse(),
			Workout = workout.ToResponse()
		};
	}

	private static void UpdateOwnedExercisesOnEdit(User user, Routine newRoutine, Workout workout)
	{
		var updateDefaultWeight = user.Settings.UpdateDefaultWeightOnSave;
		var currentExerciseIds = new HashSet<string>();
		var exerciseIdToExercise = user.Exercises.ToDictionary(x => x.Id, x => x);
		foreach (var week in newRoutine.Weeks)
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

					currentExerciseIds.Add(exerciseId);
				}
			}
		}

		var oldExerciseIds = new HashSet<string>();
		foreach (var week in workout.Routine.Weeks)
		{
			foreach (var day in week.Days)
			{
				foreach (var routineExercise in day.Exercises)
				{
					oldExerciseIds.Add(routineExercise.ExerciseId);
				}
			}
		}

		var deletedExercises = oldExerciseIds.Where(x => !currentExerciseIds.Contains(x)).ToList();
		var newExercises = currentExerciseIds.Where(x => !oldExerciseIds.Contains(x)).ToList();

		foreach (var ownedExercise in newExercises.Select(exerciseId => exerciseIdToExercise[exerciseId]))
		{
			ownedExercise.Workouts.Add(new OwnedExerciseWorkout
			{
				WorkoutId = workout.Id,
				WorkoutName = workout.Name
			});
		}

		foreach (var exerciseId in deletedExercises)
		{
			var ownedExercise = exerciseIdToExercise[exerciseId];
			var ownedExerciseWorkout = ownedExercise.Workouts.First(x => x.WorkoutId == workout.Id);
			ownedExercise.Workouts.Remove(ownedExerciseWorkout);
		}
	}
}