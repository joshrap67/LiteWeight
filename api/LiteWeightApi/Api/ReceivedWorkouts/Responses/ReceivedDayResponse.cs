namespace LiteWeightAPI.Api.ReceivedWorkouts.Responses;

public class ReceivedDayResponse
{
	/// <summary>
	/// Arbitrary tag of the day.
	/// </summary>
	/// <example>Cardio and Legs Day</example>
	public string? Tag { get; set; }

	/// <summary>
	/// List of exercises for the given day.
	/// </summary>
	public IList<ReceivedExerciseResponse> Exercises { get; set; } = [];
}