using System.ComponentModel.DataAnnotations;
using LiteWeightAPI.Imports;

namespace LiteWeightAPI.Api.Workouts.Responses;

public class RoutineExerciseResponse
{
	/// <summary>
	/// Id of the exercise (reference to the list of exercises on the user).
	/// </summary>
	/// <example>88a54457-2253-404e-ac09-82a8f2ce5fb8</example>
	public string ExerciseId { get; set; } = null!;

	/// <summary>
	/// Has the user completed this exercise?
	/// </summary>
	public bool Completed { get; set; }

	/// <summary>
	/// Weight of the exercise (lb).
	/// </summary>
	/// <example>30.0</example>
	[Range(0.0, Globals.MaxWeight)]
	public double Weight { get; set; }

	/// <summary>
	/// Number of sets for the exercise.
	/// </summary>
	/// <example>3</example>
	[Range(0.0, Globals.MaxSets)]
	public int Sets { get; set; }

	/// <summary>
	/// Number of reps for the exercise.
	/// </summary>
	/// <example>15</example>
	[Range(0.0, Globals.MaxReps)]
	public int Reps { get; set; }

	/// <summary>
	/// Optional instructions for this exercise.
	/// </summary>
	/// <example>Rest for 90 seconds between sets.</example>
	[MaxLength(Globals.MaxInstructionsLength)]
	public string? Instructions { get; set; }
}