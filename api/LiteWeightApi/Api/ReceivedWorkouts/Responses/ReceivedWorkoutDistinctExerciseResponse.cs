using LiteWeightAPI.Api.Exercises.Requests;

namespace LiteWeightAPI.Api.ReceivedWorkouts.Responses;

public class ReceivedWorkoutDistinctExerciseResponse
{
	/// <summary>
	/// Name of the exercise.
	/// </summary>
	/// <example>Squat</example>
	public string ExerciseName { get; set; } = null!;
	
	/// <summary>
	/// Arbitrary notes detailing information such as hints/cues for certain exercises.
	/// </summary>
	/// <example>Ensure deep stretch. Don't over extend arms</example>
	public string? Notes { get; set; }

	/// <summary>
	/// Links associated with this exercise.
	/// </summary>
	public IList<LinkResponse> Links { get; set; } = new List<LinkResponse>();

	/// <summary>
	/// List of focuses of the exercise.
	/// </summary>
	/// <example>["Legs", "Strength Training"]</example>
	public IList<string> Focuses { get; set; } = [];
}