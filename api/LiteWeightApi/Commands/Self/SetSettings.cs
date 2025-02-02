using LiteWeightAPI.Domain;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;

namespace LiteWeightAPI.Commands.Self;

public class SetSettings : ICommand<bool>
{
	public required string UserId { get; set; }

	public bool PrivateAccount { get; set; }

	public bool UpdateDefaultWeightOnSave { get; set; }

	public bool UpdateDefaultWeightOnRestart { get; set; }

	public bool MetricUnits { get; set; }
}

public class SetSettingsHandler : ICommandHandler<SetSettings, bool>
{
	private readonly IRepository _repository;

	public SetSettingsHandler(IRepository repository)
	{
		_repository = repository;
	}

	public async Task<bool> HandleAsync(SetSettings command)
	{
		var user = await _repository.GetUser(command.UserId);
		if (user == null)
		{
			throw new ResourceNotFoundException("User");
		}

		user.Settings.Update(command.PrivateAccount, command.UpdateDefaultWeightOnSave,
			command.UpdateDefaultWeightOnRestart, command.MetricUnits);

		await _repository.PutUser(user);

		return true;
	}
}