namespace LiteWeightAPI.Api.Self.Responses;

public class ReceivedWorkoutInfoResponse
{
	/// <summary>
	/// Id of the received workout.
	/// </summary>
	/// <example>194939d5-c40d-43e5-b40a-7d30d764b7f7</example>
	public string ReceivedWorkoutId { get; set; } = null!;

	/// <summary>
	/// Name of the received workout.
	/// </summary>
	/// <example>High Intensity Workout</example>
	public string WorkoutName { get; set; } = null!;

	/// <summary>
	/// Timestamp of when the workout was received (UTC).
	/// </summary>
	/// <example>2023-04-23T13:43:44.685341Z</example>
	public string ReceivedUtc { get; set; } = null!;

	/// <summary>
	/// Is this received workout seen by the user?
	/// </summary>
	public bool Seen { get; set; }

	/// <summary>
	/// Id of the user who sent the workout.
	/// </summary>
	/// <example>37386768-da24-47ba-b081-6493df36686f</example>
	public string SenderId { get; set; } = null!;

	/// <summary>
	/// Username of who sent the workout.
	/// </summary>
	/// <example>jessica78</example>
	public string SenderUsername { get; set; } = null!;

	/// <summary>
	/// File path of the sender's profile picture.
	/// </summary>
	/// <example>61fcf9b4-15f1-4413-9534-683b085875b9.jpg</example>
	public string SenderProfilePicture { get; set; } = null!;

	/// <summary>
	/// Total number of days in the received workout.
	/// </summary>
	/// <example>16</example>
	public int TotalDays { get; set; }

	/// <summary>
	/// Most frequent exercise focus of the received workout.
	/// </summary>
	/// <example>Biceps</example>
	public string MostFrequentFocus { get; set; } = null!;
}