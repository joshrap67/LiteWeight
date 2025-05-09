using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Domain.Workouts;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;
using LiteWeightAPI.Maps;
using LiteWeightAPI.Utils;

namespace LiteWeightAPI.Commands.Workouts;

public class UpdateWorkoutProgress : ICommand<bool>
{
	public required string UserId { get; set; }
	public required string WorkoutId { get; set; }
	public int CurrentWeek { get; set; }
	public int CurrentDay { get; set; }
	public required SetRoutine Routine { get; set; }
}

public class UpdateWorkoutProgressHandler : ICommandHandler<UpdateWorkoutProgress, bool>
{
	private readonly IRepository _repository;

	public UpdateWorkoutProgressHandler(IRepository repository)
	{
		_repository = repository;
	}

	public async Task<bool> HandleAsync(UpdateWorkoutProgress command)
	{
		var user = (await _repository.GetUser(command.UserId))!;
		var workoutToUpdate = await _repository.GetWorkout(command.WorkoutId);
		var routine = command.Routine.ToDomain();
		if (workoutToUpdate == null)
		{
			throw new ResourceNotFoundException("Workout");
		}

		ValidationUtils.EnsureWorkoutOwnership(user.Id, workoutToUpdate);

		workoutToUpdate.Routine = routine;

		var workoutInfo = user.Workouts.First(x => x.WorkoutId == command.WorkoutId);
		workoutInfo.CurrentWeek = command.CurrentWeek;
		workoutInfo.CurrentDay = command.CurrentDay;

		WorkoutUtils.FixCurrentDayAndWeek(workoutToUpdate, workoutInfo);

		await _repository.ExecuteBatchWrite(
			workoutsToPut: new List<Workout> { workoutToUpdate },
			usersToPut: new List<User> { user }
		);

		return true;
	}
}