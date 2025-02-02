using LiteWeightAPI.Domain;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;

namespace LiteWeightAPI.Commands.Self;

public class SetAllReceivedWorkoutsSeen : ICommand<bool>
{
	public required string UserId { get; set; }
}

public class SetAllReceivedWorkoutsSeenHandler : ICommandHandler<SetAllReceivedWorkoutsSeen, bool>
{
	private readonly IRepository _repository;

	public SetAllReceivedWorkoutsSeenHandler(IRepository repository)
	{
		_repository = repository;
	}

	public async Task<bool> HandleAsync(SetAllReceivedWorkoutsSeen command)
	{
		var user = await _repository.GetUser(command.UserId);
		if (user == null)
		{
			throw new ResourceNotFoundException("User");
		}

		foreach (var receivedWorkoutInfo in user.ReceivedWorkouts)
		{
			receivedWorkoutInfo.Seen = true;
		}

		await _repository.PutUser(user);

		return true;
	}
}