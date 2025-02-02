using LiteWeightAPI.Errors.Attributes.Setup;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;

namespace LiteWeightAPI.Errors.Exceptions;

public class MaxLimitException : BadRequestException
{
	public MaxLimitException(string message) : base(GetFormattedResponse(message, ErrorTypes.MaxLimit))
	{
	}
}