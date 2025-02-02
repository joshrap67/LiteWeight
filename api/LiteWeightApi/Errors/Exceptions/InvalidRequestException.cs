using LiteWeightAPI.Errors.Attributes.Setup;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;
using LiteWeightAPI.Errors.Responses;

namespace LiteWeightAPI.Errors.Exceptions;

public class InvalidRequestException : BadRequestException
{
	public InvalidRequestException(string message, IEnumerable<ModelBindingError> bindingErrors = null) :
		base(GetFormattedResponse(message, ErrorTypes.InvalidRequest, bindingErrors))
	{
	}
}