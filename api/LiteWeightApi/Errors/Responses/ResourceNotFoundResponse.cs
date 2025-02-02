namespace LiteWeightAPI.Errors.Responses;

public class ResourceNotFoundResponse
{
	/// <summary>
	/// Response message.
	/// </summary>
	/// <example>The workout with id=cfbb1fd4-6d29-465c-9ea0-4938407ac65b was not found</example>
	public string Message { get; set; } = null!;
}