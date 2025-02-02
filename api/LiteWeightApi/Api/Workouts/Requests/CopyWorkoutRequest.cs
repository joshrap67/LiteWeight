using System.ComponentModel.DataAnnotations;
using LiteWeightAPI.Imports;

namespace LiteWeightAPI.Api.Workouts.Requests;

public class CopyWorkoutRequest
{
	/// <summary>
	/// Name for the workout created from the copy. Must be unique.
	/// </summary>
	/// <example>After-Work Workout</example>
	[Required]
	[MaxLength(Globals.MaxWorkoutNameLength)]
	public string Name { get; set; } = null!;
}