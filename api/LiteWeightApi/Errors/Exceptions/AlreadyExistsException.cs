using LiteWeightAPI.Errors.Attributes.Setup;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;

namespace LiteWeightAPI.Errors.Exceptions;

public class AlreadyExistsException : BadRequestException
{
	public AlreadyExistsException(string message) : base(GetFormattedResponse(message, ErrorTypes.AlreadyExists))
	{
	}
}