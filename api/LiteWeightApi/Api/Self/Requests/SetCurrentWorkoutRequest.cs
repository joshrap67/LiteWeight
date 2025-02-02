namespace LiteWeightAPI.Api.Self.Requests;

public class SetCurrentWorkoutRequest
{
	/// <summary>
	/// Id of the workout to set as the current workout. Null signifies no workout is selected as current.
	/// </summary>
	/// <example>4d17e3e7-edf6-41c0-ade2-fdd624b5c9bb</example>
	public string? CurrentWorkoutId { get; set; }
}