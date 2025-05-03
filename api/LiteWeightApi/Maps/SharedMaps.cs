using LiteWeightAPI.Api.Exercises.Requests;
using LiteWeightAPI.Commands.Common;
using LiteWeightAPI.Domain.Users;

namespace LiteWeightAPI.Maps;

public static class SharedMaps
{
	public static Link ToDomain(this SetLink command)
	{
		return new Link
		{
			Url = command.Url,
			Label = command.Label
		};
	}

	public static LinkResponse ToResponse(this Link command)
	{
		return new LinkResponse
		{
			Label = command.Label,
			Url = command.Url
		};
	}
}