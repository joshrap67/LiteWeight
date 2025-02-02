using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Domain.Workouts;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;
using LiteWeightAPI.Utils;

namespace LiteWeightAPI.Commands.Workouts;

public class DeleteWorkout : ICommand<bool>
{
	public required string UserId { get; set; }
	public required string WorkoutId { get; set; }
}

public class DeleteWorkoutHandler : ICommandHandler<DeleteWorkout, bool>
{
	private readonly IRepository _repository;

	public DeleteWorkoutHandler(IRepository repository)
	{
		_repository = repository;
	}

	public async Task<bool> HandleAsync(DeleteWorkout command)
	{
		var user = (await _repository.GetUser(command.UserId))!;
		var workoutToDelete = await _repository.GetWorkout(command.WorkoutId);
		if (workoutToDelete == null)
		{
			throw new ResourceNotFoundException("Workout");
		}

		ValidationUtils.EnsureWorkoutOwnership(user.Id, workoutToDelete);

		user.Workouts.RemoveAll(x => x.WorkoutId == command.WorkoutId);
		foreach (var ownedExercise in user.Exercises)
		{
			var ownedExerciseWorkout = ownedExercise.Workouts.FirstOrDefault(x => x.WorkoutId == command.WorkoutId);
			if (ownedExerciseWorkout != null)
			{
				ownedExercise.Workouts.Remove(ownedExerciseWorkout);
			}
		}

		await _repository.ExecuteBatchWrite(
			workoutsToDelete: new List<Workout> { workoutToDelete },
			usersToPut: new List<User> { user }
		);

		return true;
	}
}