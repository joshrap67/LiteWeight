namespace LiteWeightAPI.Errors.Responses;

public class TooManyRequestsResponse
{
	/// <summary>
	///  Response message.
	/// </summary>
	/// <example>Too many requests. Please try again later.</example>
	public string Message { get; set; } = null!;
}