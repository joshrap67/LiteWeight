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
	/// Default details of the exercise. Value that the exercise will be defaulted to when adding it to a workout.
	/// </summary>
	/// <example>Don't overextend arms.</example>
	public string? DefaultDetails { get; set; }

	/// <summary>
	/// Video url of the exercise. Suggested use case is a video of how to perform the exercise.
	/// </summary>
	/// <example>https://www.youtube.com/watch?v=kwG2ipFRgfo</example>
	public string? VideoUrl { get; set; }

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