using LiteWeightAPI.Domain;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;

namespace LiteWeightAPI.Commands.Self;

public class SetReceivedWorkoutSeen : ICommand<bool>
{
	public required string UserId { get; set; }
	public required string ReceivedWorkoutId { get; set; }
}

public class SetReceivedWorkoutSeenHandler : ICommandHandler<SetReceivedWorkoutSeen, bool>
{
	private readonly IRepository _repository;

	public SetReceivedWorkoutSeenHandler(IRepository repository)
	{
		_repository = repository;
	}

	public async Task<bool> HandleAsync(SetReceivedWorkoutSeen command)
	{
		var user = await _repository.GetUser(command.UserId);
		if (user == null)
		{
			throw new ResourceNotFoundException("User");
		}

		var receivedWorkoutInfo =
			user.ReceivedWorkouts.FirstOrDefault(x => x.ReceivedWorkoutId == command.ReceivedWorkoutId);
		if (receivedWorkoutInfo == null)
		{
			return false;
		}

		receivedWorkoutInfo.Seen = true;
		await _repository.PutUser(user);

		return true;
	}
}