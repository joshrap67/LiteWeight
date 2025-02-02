using System.ComponentModel.DataAnnotations;
using LiteWeightAPI.Imports;

namespace LiteWeightAPI.Api.Exercises.Requests;

public class SetExerciseRequest
{
	/// <summary>
	/// Name of the exercise.
	/// </summary>
	/// <example>Bench Press</example>
	[Required]
	[MaxLength(Globals.MaxExerciseName)]
	public string Name { get; set; } = null!;

	/// <summary>
	/// Default weight of the exercise (lb). Value that the exercise will be defaulted to when adding it to a workout.
	/// </summary>
	/// <example>225.5</example>
	[Required]
	[Range(0.0, Globals.MaxWeight)]
	public double DefaultWeight { get; set; }

	/// <summary>
	/// Default sets of the exercise. Value that the exercise will be defaulted to when adding it to a workout.
	/// </summary>
	/// <example>3</example>
	[Required]
	[Range(0, Globals.MaxSets)]
	public int DefaultSets { get; set; }

	/// <summary>
	/// Default reps of the exercise. Value that the exercise will be defaulted to when adding it to a workout.
	/// </summary>
	/// <example>12</example>
	[Required]
	[Range(0, Globals.MaxReps)]
	public int DefaultReps { get; set; }

	/// <summary>
	/// List of focuses of the exercise.
	/// </summary>
	/// <example>["Chest", "Strength Training"]</example>
	[Required]
	[MinLength(1)]
	[MaxLength(Globals.MaxFocusesLength)]
	public IList<string> Focuses { get; set; } = new List<string>();

	/// <summary>
	/// Default details of the exercise. Value that the exercise will be defaulted to when adding it to a workout.
	/// </summary>
	/// <example>Make sure to get a spotter.</example>
	[MaxLength(Globals.MaxDetailsLength)]
	public string? DefaultDetails { get; set; }

	/// <summary>
	/// Video URL of the exercise. Suggested use case is a video of how to perform the exercise.
	/// </summary>
	/// <example>https://www.youtube.com/watch?v=rT7DgCr-3pg</example>
	[MaxLength(Globals.MaxUrlLength)]
	public string? VideoUrl { get; set; }
}