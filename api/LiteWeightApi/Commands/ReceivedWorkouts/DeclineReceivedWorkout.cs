using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.ReceivedWorkouts;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;
using LiteWeightAPI.Utils;

namespace LiteWeightAPI.Commands.ReceivedWorkouts;

public class DeclineReceivedWorkout : ICommand<bool>
{
	public required string UserId { get; set; }
	public required string ReceivedWorkoutId { get; set; }
}

public class DeclineReceivedWorkoutHandler : ICommandHandler<DeclineReceivedWorkout, bool>
{
	private readonly IRepository _repository;

	public DeclineReceivedWorkoutHandler(IRepository repository)
	{
		_repository = repository;
	}

	public async Task<bool> HandleAsync(DeclineReceivedWorkout command)
	{
		var user = (await _repository.GetUser(command.UserId))!;
		var workoutToDecline = await _repository.GetReceivedWorkout(command.ReceivedWorkoutId);

		if (workoutToDecline == null)
		{
			throw new ResourceNotFoundException("Received workout");
		}

		ValidationUtils.EnsureReceivedWorkoutOwnership(command.UserId, workoutToDecline);

		var workoutToRemove =
			user.ReceivedWorkouts.FirstOrDefault(x => x.ReceivedWorkoutId == command.ReceivedWorkoutId);
		if (workoutToRemove == null) return false;
		user.ReceivedWorkouts.Remove(workoutToRemove);

		await _repository.ExecuteBatchWrite(
			usersToPut: new List<User> { user },
			receivedWorkoutsToDelete: new List<ReceivedWorkout> { workoutToDecline }
		);

		return true;
	}
}