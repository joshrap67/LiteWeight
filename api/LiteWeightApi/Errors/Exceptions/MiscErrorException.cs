using LiteWeightAPI.Errors.Attributes.Setup;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;

namespace LiteWeightAPI.Errors.Exceptions;

public class MiscErrorException : BadRequestException
{
	public MiscErrorException(string message) : base(GetFormattedResponse(message, ErrorTypes.MiscError))
	{
	}
}