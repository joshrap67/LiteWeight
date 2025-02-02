using LiteWeightAPI.Errors.Attributes.Setup;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;

namespace LiteWeightAPI.Errors.Exceptions;

public class WorkoutNotFoundException : BadRequestException
{
	public WorkoutNotFoundException(string message) : base(GetFormattedResponse(message, ErrorTypes.WorkoutNotFound))
	{
	}
}