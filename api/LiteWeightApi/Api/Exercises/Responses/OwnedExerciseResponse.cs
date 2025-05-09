using LiteWeightAPI.Api.Common.Responses;
using LiteWeightAPI.Api.Exercises.Requests;

namespace LiteWeightAPI.Api.Exercises.Responses;

public class OwnedExerciseResponse
{
	/// <summary>
	/// Id of the exercise.
	/// </summary>
	/// <example>88a54457-2253-404e-ac09-82a8f2ce5fb8</example>
	public string Id { get; set; } = null!;

	/// <summary>
	/// Name of the exercise.
	/// </summary>
	/// <example>Barbell Curl</example>
	public string Name { get; set; } = null!;

	/// <summary>
	/// Default weight of the exercise (lb). Value that the exercise will be defaulted to when adding it to a workout.
	/// </summary>
	/// <example>65.0</example>
	public double DefaultWeight { get; set; }

	/// <summary>
	/// Default sets of the exercise. Value that the exercise will be defaulted to when adding it to a workout.
	/// </summary>
	/// <example>3</example>
	public int DefaultSets { get; set; }

	/// <summary>
	/// Default reps of the exercise. Value that the exercise will be defaulted to when adding it to a workout.
	/// </summary>
	/// <example>15</example>
	public int DefaultReps { get; set; }

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
	/// <example>["Biceps", "Forearms"]</example>
	public IList<string> Focuses { get; set; } = new List<string>();

	/// <summary>
	/// List of workouts this exercise is associated with.
	/// </summary>
	public IList<OwnedExerciseWorkoutResponse> Workouts { get; set; } = [];
}