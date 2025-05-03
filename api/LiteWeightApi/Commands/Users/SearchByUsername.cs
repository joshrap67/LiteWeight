using LiteWeightAPI.Api.Users.Responses;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Maps;

namespace LiteWeightAPI.Commands.Users;

public class SearchByUsername : ICommand<SearchUserResponse?>
{
	public required string Username { get; init; }
	public required string InitiatorId { get; init; }
}

public class SearchByUsernameHandler : ICommandHandler<SearchByUsername, SearchUserResponse?>
{
	private readonly IRepository _repository;

	public SearchByUsernameHandler(IRepository repository)
	{
		_repository = repository;
	}

	public async Task<SearchUserResponse?> HandleAsync(SearchByUsername command)
	{
		var user = await _repository.GetUserByUsername(command.Username);
		if (user == null)
		{
			return null;
		}

		// if user is private account, they should not show up in the search unless already friends (or pending friend) with the initiator
		if (user.Settings.PrivateAccount && user.Friends.All(x => x.UserId != command.InitiatorId))
		{
			return null;
		}

		return user.ToSearchResponse();
	}
}