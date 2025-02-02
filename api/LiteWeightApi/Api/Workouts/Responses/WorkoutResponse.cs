namespace LiteWeightAPI.Api.Workouts.Responses;

public class WorkoutResponse
{
	/// <summary>
	/// Unique identifier of the workout.
	/// </summary>
	/// <example>b7c3c321-d5a0-4448-978c-0a8b7709ceb2</example>
	public string Id { get; set; } = null!;

	/// <summary>
	/// Name of the workout.
	/// </summary>
	/// <example>Main Workout</example>
	public string Name { get; set; } = null!;

	/// <summary>
	/// Timestamp of when the workout was created (UTC).
	/// </summary>
	/// <example>2023-04-23T16:49:02.310661Z</example>
	public string CreationUtc { get; set; } = null!;

	/// <summary>
	/// Id of the user who created the workout.
	/// </summary>
	/// <example>a7be7348-bca5-466c-b290-55ae38e2bad0</example>
	public string CreatorId { get; set; } = null!;

	/// <summary>
	/// Routine of the user.
	/// </summary>
	public RoutineResponse Routine { get; set; } = null!;
}