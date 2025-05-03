using LiteWeightAPI.Api.Exercises.Responses;
using LiteWeightAPI.Commands.Common;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Errors.Exceptions;
using LiteWeightAPI.Imports;
using LiteWeightAPI.Maps;

namespace LiteWeightAPI.Commands.Exercises;

public class CreateExercise : ICommand<OwnedExerciseResponse>
{
	public required string UserId { get; set; }

	public required string Name { get; set; }

	public double DefaultWeight { get; set; }

	public int DefaultSets { get; set; }

	public int DefaultReps { get; set; }

	public IList<string> Focuses { get; set; } = new List<string>();

	public IList<SetLink> Links { get; set; } = new List<SetLink>();

	public string? Notes { get; set; }
}

public class CreateExerciseHandler : ICommandHandler<CreateExercise, OwnedExerciseResponse>
{
	private readonly IRepository _repository;

	public CreateExerciseHandler(IRepository repository)
	{
		_repository = repository;
	}

	public async Task<OwnedExerciseResponse> HandleAsync(CreateExercise command)
	{
		var user = (await _repository.GetUser(command.UserId))!;
		var exerciseNames = user.Exercises.Select(x => x.Name);

		if (exerciseNames.Any(x => x == command.Name))
		{
			throw new AlreadyExistsException("Exercise name already exists");
		}

		if (user.PremiumToken == null && user.Exercises.Count >= Globals.MaxFreeExercises)
		{
			throw new MaxLimitException("Max exercise limit reached");
		}

		if (user.PremiumToken != null && user.Exercises.Count >= Globals.MaxExercises)
		{
			throw new MaxLimitException("Max exercise limit reached");
		}

		var newExercise = command.ToDomain();
		user.Exercises.Add(newExercise);

		await _repository.PutUser(user);

		var response = newExercise.ToResponse();
		response.Id = newExercise.Id;
		return response;
	}
}