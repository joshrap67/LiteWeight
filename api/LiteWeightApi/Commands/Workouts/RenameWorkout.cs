using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Domain.Workouts;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;
using LiteWeightAPI.Utils;

namespace LiteWeightAPI.Commands.Workouts;

public class RenameWorkout : ICommand<bool>
{
	public required string UserId { get; set; }
	public required string WorkoutId { get; set; }
	public required string NewName { get; set; }
}

public class RenameWorkoutHandler : ICommandHandler<RenameWorkout, bool>
{
	private readonly IRepository _repository;

	public RenameWorkoutHandler(IRepository repository)
	{
		_repository = repository;
	}

	public async Task<bool> HandleAsync(RenameWorkout command)
	{
		var user = (await _repository.GetUser(command.UserId))!;
		var workout = await _repository.GetWorkout(command.WorkoutId);
		if (workout == null)
		{
			throw new ResourceNotFoundException("Workout");
		}

		ValidationUtils.EnsureWorkoutOwnership(user.Id, workout);
		ValidationUtils.ValidWorkoutName(command.NewName, user);

		var newName = command.NewName;
		workout.Name = newName;
		foreach (var exercise in user.Exercises)
		{
			var exerciseWorkout = exercise.Workouts.FirstOrDefault(x => x.WorkoutId == command.WorkoutId);
			if (exerciseWorkout != null)
			{
				// old workout name found, replace it with newly named one
				exerciseWorkout.WorkoutName = newName;
			}
		}

		user.Workouts.First(x => x.WorkoutId == command.WorkoutId).WorkoutName = newName;

		await _repository.ExecuteBatchWrite(
			workoutsToPut: new List<Workout> { workout },
			usersToPut: new List<User> { user }
		);

		return true;
	}
}