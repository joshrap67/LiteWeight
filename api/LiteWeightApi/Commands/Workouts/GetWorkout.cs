using AutoMapper;
using LiteWeightAPI.Api.Workouts.Responses;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;
using LiteWeightAPI.Utils;

namespace LiteWeightAPI.Commands.Workouts;

public class GetWorkout : ICommand<WorkoutResponse>
{
	public required string UserId { get; set; }
	public required string WorkoutId { get; set; }
}

public class GetWorkoutHandler : ICommandHandler<GetWorkout, WorkoutResponse>
{
	private readonly IRepository _repository;
	private readonly IMapper _mapper;

	public GetWorkoutHandler(IRepository repository, IMapper mapper)
	{
		_repository = repository;
		_mapper = mapper;
	}

	public async Task<WorkoutResponse> HandleAsync(GetWorkout command)
	{
		var workout = await _repository.GetWorkout(command.WorkoutId);
		if (workout == null)
		{
			throw new ResourceNotFoundException("Workout");
		}

		ValidationUtils.EnsureWorkoutOwnership(command.UserId, workout);

		return _mapper.Map<WorkoutResponse>(workout);
	}
}