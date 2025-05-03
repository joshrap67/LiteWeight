using LiteWeightAPI.Commands.Common;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Errors.Exceptions;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;
using LiteWeightAPI.Maps;

namespace LiteWeightAPI.Commands.Exercises;

public class UpdateExercise : ICommand<bool>
{
	public required string UserId { get; set; }

	public required string ExerciseId { get; set; }

	public required string Name { get; set; }

	public double DefaultWeight { get; set; }

	public int DefaultSets { get; set; }

	public int DefaultReps { get; set; }

	public string? Notes { get; set; }

	public IList<string> Focuses { get; set; } = new List<string>();

	public IList<SetLink> Links { get; set; } = new List<SetLink>();
}

public class UpdateExerciseHandler : ICommandHandler<UpdateExercise, bool>
{
	private readonly IRepository _repository;

	public UpdateExerciseHandler(IRepository repository)
	{
		_repository = repository;
	}

	public async Task<bool> HandleAsync(UpdateExercise command)
	{
		var user = (await _repository.GetUser(command.UserId))!;

		var oldExercise = user.Exercises.FirstOrDefault(x => x.Id == command.ExerciseId);
		if (oldExercise == null)
		{
			throw new ResourceNotFoundException("Exercise");
		}

		var oldExerciseName = oldExercise.Name;
		var exerciseNames = user.Exercises.Select(x => x.Name).ToHashSet();

		if (command.Name != oldExerciseName && exerciseNames.Contains(command.Name))
		{
			// compare old name since user might not have changed name and otherwise would always get error saying exercise already exists
			throw new AlreadyExistsException("Exercise name already exists");
		}

		var ownedExercise = user.Exercises.First(x => x.Id == command.ExerciseId);
		ownedExercise.Update(command.Name, command.DefaultWeight, command.DefaultSets, command.DefaultReps,
			command.Links.Select(x => x.ToDomain()).ToList(), command.Notes, command.Focuses);

		await _repository.ExecuteBatchWrite(usersToPut: new List<User> { user });

		return true;
	}
}