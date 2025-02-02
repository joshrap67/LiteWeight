namespace LiteWeightAPI.Errors.Exceptions.BaseExceptions;

public class ForbiddenException : Exception
{
	public ForbiddenException(string? message = null) : base(message ?? "Unauthorized access")
	{
	}
}