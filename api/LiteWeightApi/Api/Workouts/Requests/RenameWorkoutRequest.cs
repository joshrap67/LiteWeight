using System.ComponentModel.DataAnnotations;
using LiteWeightAPI.Imports;

namespace LiteWeightAPI.Api.Workouts.Requests;

public class RenameWorkoutRequest
{
	/// <summary>
	/// New name of the workout. Must be unique.
	/// </summary>
	[Required]
	[MaxLength(Globals.MaxWorkoutNameLength)]
	public string Name { get; set; } = null!;
}