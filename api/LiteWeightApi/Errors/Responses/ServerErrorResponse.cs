namespace LiteWeightAPI.Errors.Responses;

public class ServerErrorResponse
{
	/// <summary>
	/// Response message.
	/// </summary>
	/// <example>The server has encountered an error</example>
	public string Message { get; set; } = null!;
}