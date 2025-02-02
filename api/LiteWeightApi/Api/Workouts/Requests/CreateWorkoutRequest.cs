using System.ComponentModel.DataAnnotations;
using LiteWeightAPI.Imports;

namespace LiteWeightAPI.Api.Workouts.Requests;

public class CreateWorkoutRequest
{
	/// <summary>
	/// Name of the workout.
	/// </summary>
	/// <example>High Intensity Workout</example>
	[Required]
	[MaxLength(Globals.MaxWorkoutNameLength)]
	public string Name { get; set; } = null!;

	/// <summary>
	/// Routine of the workout.
	/// </summary>
	[Required]
	public SetRoutineRequest Routine { get; set; } = null!;

	/// <summary>
	/// If true, set this new workout to be the current workout for the user creating the workout.
	/// </summary>
	public bool SetAsCurrentWorkout { get; set; } = true;
}