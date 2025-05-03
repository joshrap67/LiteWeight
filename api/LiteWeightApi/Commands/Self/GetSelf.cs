using LiteWeightAPI.Api.Self.Responses;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;
using LiteWeightAPI.Maps;

namespace LiteWeightAPI.Commands.Self;

public class GetSelf : ICommand<UserResponse>
{
	public required string UserId { get; init; }
}

public class GetSelfHandler : ICommandHandler<GetSelf, UserResponse>
{
	private readonly IRepository _repository;

	public GetSelfHandler(IRepository repository)
	{
		_repository = repository;
	}

	public async Task<UserResponse> HandleAsync(GetSelf command)
	{
		var user = await _repository.GetUser(command.UserId);
		if (user == null)
		{
			throw new ResourceNotFoundException("User");
		}

		return user.ToResponse();
	}
}