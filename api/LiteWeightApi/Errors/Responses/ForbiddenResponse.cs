namespace LiteWeightAPI.Errors.Responses;

public class ForbiddenResponse
{
	/// <summary>
	///  Response message.
	/// </summary>
	/// <example>Forbidden access</example>
	public string Message { get; set; } = null!;
}