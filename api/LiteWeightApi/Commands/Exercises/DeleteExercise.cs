using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Domain.Workouts;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;

namespace LiteWeightAPI.Commands.Exercises;

public class DeleteExercise : ICommand<bool>
{
	public required string UserId { get; set; }

	public required string ExerciseId { get; set; }
}

public class DeleteExerciseHandler : ICommandHandler<DeleteExercise, bool>
{
	private readonly IRepository _repository;

	public DeleteExerciseHandler(IRepository repository)
	{
		_repository = repository;
	}

	public async Task<bool> HandleAsync(DeleteExercise command)
	{
		var user = (await _repository.GetUser(command.UserId))!;
		var oldExercise = user.Exercises.FirstOrDefault(x => x.Id == command.ExerciseId);
		if (oldExercise == null)
		{
			throw new ResourceNotFoundException("Exercise");
		}

		var ownedExercise = user.Exercises.First(x => x.Id == command.ExerciseId);
		user.Exercises.Remove(ownedExercise);

		var workouts = new List<Workout>();
		foreach (var workoutId in ownedExercise.Workouts.Select(x => x.WorkoutId))
		{
			var workout = await _repository.GetWorkout(workoutId);
			workout!.Routine.DeleteExerciseFromRoutine(command.ExerciseId);
			workouts.Add(workout);
		}

		await _repository.ExecuteBatchWrite(
			workoutsToPut: workouts,
			usersToPut: new List<User> { user }
		);

		return true;
	}
}