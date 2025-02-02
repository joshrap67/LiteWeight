using LiteWeightAPI.Errors.Responses;

namespace LiteWeightAPI.Errors.Exceptions.BaseExceptions;

public class BadRequestException : Exception
{
	protected BadRequestException(BadRequestResponse formattedResponse) : base(formattedResponse.Message)
	{
		FormattedResponse = formattedResponse;
	}

	public BadRequestResponse FormattedResponse { get; }

	protected static BadRequestResponse GetFormattedResponse(string message, string errorType,
		IEnumerable<ModelBindingError>? bindingErrors = null)
	{
		return new BadRequestResponse
		{
			Message = message,
			ErrorType = errorType,
			RequestErrors = bindingErrors ?? new List<ModelBindingError>()
		};
	}
}