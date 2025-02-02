namespace LiteWeightAPI.Api.ReceivedWorkouts.Responses;

public class ReceivedWorkoutResponse
{
	/// <summary>
	/// Id of the received workout.
	/// </summary>
	/// <example>3ac84a61-4822-4ba3-ac93-626fdf087acf</example>
	public string Id { get; set; } = null!;

	/// <summary>
	/// Name of the received workout.
	/// </summary>
	/// <example>Legs Galore</example>
	public string WorkoutName { get; set; } = null!;

	/// <summary>
	/// Id of the user who sent the workout.
	/// </summary>
	/// <example>juo06et3-n81k-9bb1-61dj-j12k1152hae1</example>
	public string SenderId { get; set; } = null!;

	/// <summary>
	/// Username of the user who sent the workout.
	/// </summary>
	/// <example>arthur_v</example>
	public string SenderUsername { get; set; } = null!;

	/// <summary>
	/// Id of the recipient of the received workout.
	/// </summary>
	/// <example>f1e03cd1-e62c-4a53-84ed-498c72776fc2</example>
	public string RecipientId { get; set; } = null!;

	/// <summary>
	/// Routine of the received workout.
	/// </summary>
	public ReceivedRoutineResponse Routine { get; set; } = null!;

	/// <summary>
	/// List of distinct exercises in the routine of the received workout. May contain exercises the recipient does not have (by name).
	/// </summary>
	public IList<ReceivedWorkoutDistinctExerciseResponse> DistinctExercises { get; set; } = [];
}