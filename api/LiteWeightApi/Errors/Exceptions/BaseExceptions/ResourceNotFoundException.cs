namespace LiteWeightAPI.Errors.Exceptions.BaseExceptions;

public class ResourceNotFoundException : Exception
{
	private const string DefaultMessage = "Resource not found";

	public ResourceNotFoundException() : base(DefaultMessage)
	{
	}

	public ResourceNotFoundException(string resourceName) : base(GetFormattedMessage(resourceName))
	{
		FormattedMessage = GetFormattedMessage(resourceName);
	}

	public string FormattedMessage { get; } = DefaultMessage;

	private static string GetFormattedMessage(string resourceName)
	{
		return $"{resourceName} not found";
	}
}