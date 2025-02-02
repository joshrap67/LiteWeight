using LiteWeightAPI.Domain;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;

namespace LiteWeightAPI.Commands.Self;

public class SetFirebaseMessagingToken : ICommand<bool>
{
	public required string UserId { get; set; }
	public string? Token { get; set; }
}

public class SetFirebaseMessagingTokenHandler : ICommandHandler<SetFirebaseMessagingToken, bool>
{
	private readonly IRepository _repository;

	public SetFirebaseMessagingTokenHandler(IRepository repository)
	{
		_repository = repository;
	}

	public async Task<bool> HandleAsync(SetFirebaseMessagingToken command)
	{
		var user = await _repository.GetUser(command.UserId);
		if (user == null)
		{
			throw new ResourceNotFoundException("User");
		}

		user.FirebaseMessagingToken = command.Token;

		await _repository.PutUser(user);

		return true;
	}
}