using LiteWeightAPI.Domain;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;

namespace LiteWeightAPI.Commands.Self;

public class SetAllFriendRequestsSeen : ICommand<bool>
{
	public required string UserId { get; init; }
}

public class SetAllFriendRequestsSeenHandler : ICommandHandler<SetAllFriendRequestsSeen, bool>
{
	private readonly IRepository _repository;

	public SetAllFriendRequestsSeenHandler(IRepository repository)
	{
		_repository = repository;
	}

	public async Task<bool> HandleAsync(SetAllFriendRequestsSeen command)
	{
		var user = await _repository.GetUser(command.UserId);
		if (user == null)
		{
			throw new ResourceNotFoundException("User");
		}

		foreach (var friendRequest in user.FriendRequests)
		{
			friendRequest.Seen = true;
		}

		await _repository.PutUser(user);

		return true;
	}
}