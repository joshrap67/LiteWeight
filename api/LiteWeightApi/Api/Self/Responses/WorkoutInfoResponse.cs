namespace LiteWeightAPI.Api.Self.Responses;

public class WorkoutInfoResponse
{
	/// <summary>
	/// Id of the workout.
	/// </summary>
	/// <example>84a34611-9f4c-443f-97f9-cbf5cde69c65</example>
	public string WorkoutId { get; set; } = null!;

	/// <summary>
	/// Name of the workout.
	/// </summary>
	/// <example>3-Day Split</example>
	public string WorkoutName { get; set; } = null!;
	
	/// <summary>
	/// Index of the current week the user is on.
	/// </summary>
	/// <example>2</example>
	public int CurrentWeek { get; set; }

	/// <summary>
	/// Index of the current day of the current week the user is on.
	/// </summary>
	/// <example>0</example>
	public int CurrentDay { get; set; }

	/// <summary>
	/// Timestamp of when the workout was last set as the current workout (UTC).
	/// </summary>
	/// <example>2023-04-06T23:20:39.665047Z</example>
	public string LastSetAsCurrentUtc { get; set; } = null!;

	/// <summary>
	/// Total times the workout has been restarted.
	/// </summary>
	/// <example>15</example>
	public int TimesRestarted { get; set; }

	/// <summary>
	/// Average of the exercise completion percentages for all restarted iterations of this workout.
	/// </summary>
	/// <example>94.2</example>
	public double AverageWorkoutCompletion { get; set; }
}