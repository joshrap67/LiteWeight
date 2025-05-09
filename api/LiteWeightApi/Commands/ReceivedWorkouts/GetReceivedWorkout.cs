using LiteWeightAPI.Api.ReceivedWorkouts.Responses;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;
using LiteWeightAPI.Maps;
using LiteWeightAPI.Utils;

namespace LiteWeightAPI.Commands.ReceivedWorkouts;

public class GetReceivedWorkout : ICommand<ReceivedWorkoutResponse>
{
	public required string UserId { get; set; }
	public required string ReceivedWorkoutId { get; set; }
}

public class GetReceivedWorkoutHandler : ICommandHandler<GetReceivedWorkout, ReceivedWorkoutResponse>
{
	private readonly IRepository _repository;

	public GetReceivedWorkoutHandler(IRepository repository)
	{
		_repository = repository;
	}

	public async Task<ReceivedWorkoutResponse> HandleAsync(GetReceivedWorkout command)
	{
		var receivedWorkout = await _repository.GetReceivedWorkout(command.ReceivedWorkoutId);
		if (receivedWorkout == null)
		{
			throw new ResourceNotFoundException("Received workout");
		}

		ValidationUtils.EnsureReceivedWorkoutOwnership(command.UserId, receivedWorkout);

		return receivedWorkout.ToResponse();
	}
}