namespace LiteWeightAPI.Errors.Responses;

public class UnauthorizedResponse
{
	/// <summary>
	/// Response message.
	/// </summary>
	/// <example>Unauthorized access</example>
	public string Message { get; set; } = null!;
}