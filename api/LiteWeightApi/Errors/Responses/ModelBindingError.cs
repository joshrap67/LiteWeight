namespace LiteWeightAPI.Errors.Responses;

public class ModelBindingError
{
	/// <summary>
	/// Property that is causing the error.
	/// </summary>
	/// <example>name</example>
	public string Property { get; init; } = null!;

	/// <summary>
	/// Error message for problematic property.
	/// </summary>
	/// <example>The name property cannot be empty</example>
	public string Message { get; init; } = null!;
}