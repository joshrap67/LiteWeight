using AutoMapper;
using LiteWeightAPI.Api.Exercises.Responses;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Errors.Exceptions;
using LiteWeightAPI.Imports;
using ILogger = Serilog.ILogger;

namespace LiteWeightAPI.Commands.Exercises;

public class CreateExercise : ICommand<OwnedExerciseResponse>
{
	public required string UserId { get; set; }

	public required string Name { get; set; }

	public double DefaultWeight { get; set; }

	public int DefaultSets { get; set; }

	public int DefaultReps { get; set; }

	public IList<string> Focuses { get; set; } = new List<string>();

	public string? DefaultDetails { get; set; }

	public string? VideoUrl { get; set; }
}

public class CreateExerciseHandler : ICommandHandler<CreateExercise, OwnedExerciseResponse>
{
	private readonly IRepository _repository;
	private readonly IMapper _mapper;

	public CreateExerciseHandler(IRepository repository, IMapper mapper)
	{
		_repository = repository;
		_mapper = mapper;
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

		var newExercise = _mapper.Map<OwnedExercise>(command);
		user.Exercises.Add(newExercise);

		await _repository.PutUser(user);

		var response = _mapper.Map<OwnedExerciseResponse>(newExercise);
		response.Id = newExercise.Id;
		return response;
	}
}