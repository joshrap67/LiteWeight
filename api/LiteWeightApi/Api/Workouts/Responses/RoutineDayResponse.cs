namespace LiteWeightAPI.Api.Workouts.Responses;

public class RoutineDayResponse
{
	/// <summary>
	/// Arbitrary tag of the day.
	/// </summary>
	/// <example>Back and Biceps Day</example>
	public string? Tag { get; set; }

	/// <summary>
	/// List of exercises for the given day.
	/// </summary>
	public IList<RoutineExerciseResponse> Exercises { get; set; } =[];
}