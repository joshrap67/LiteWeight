using LiteWeightAPI.Domain;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;

namespace LiteWeightAPI.Commands.Workouts;

public class ResetStatistics : ICommand<bool>
{
	public required string UserId { get; set; }
	public required string WorkoutId { get; set; }
}

public class ResetStatisticsHandler : ICommandHandler<ResetStatistics, bool>
{
	private readonly IRepository _repository;

	public ResetStatisticsHandler(IRepository repository)
	{
		_repository = repository;
	}

	public async Task<bool> HandleAsync(ResetStatistics command)
	{
		var user = (await _repository.GetUser(command.UserId))!;
		if (user.Workouts.All(x => x.WorkoutId != command.WorkoutId))
		{
			throw new ResourceNotFoundException("Workout");
		}

		var workoutInfo = user.Workouts.First(x => x.WorkoutId == command.WorkoutId);
		workoutInfo.TimesRestarted = 0;
		workoutInfo.AverageWorkoutCompletion = 0.0;

		await _repository.PutUser(user);

		return true;
	}
}