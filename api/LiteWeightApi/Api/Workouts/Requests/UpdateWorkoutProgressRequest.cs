using System.ComponentModel.DataAnnotations;
using LiteWeightAPI.Imports;

namespace LiteWeightAPI.Api.Workouts.Requests;

public class UpdateWorkoutProgressRequest
{
	/// <summary>
	/// Index of the current week the user is on.
	/// </summary>
	/// <example>2</example>
	[Range(0, Globals.MaxWeeksRoutine)]
	public int CurrentWeek { get; set; }

	/// <summary>
	/// Index of the current day of the current week the user is on.
	/// </summary>
	/// <example>0</example>
	[Range(0, Globals.MaxDaysRoutine)]
	public int CurrentDay { get; set; }

	/// <summary>
	/// Routine to update.
	/// </summary>
	[Required]
	public SetRoutineRequest Routine { get; set; } = null!;
}