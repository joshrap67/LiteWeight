using LiteWeightAPI.Api.Common.Responses;
using LiteWeightAPI.Commands.Common;
using LiteWeightAPI.Domain.Users;

namespace LiteWeightAPI.ExtensionMethods.Mapping;

public static class CommonMappings
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