using System.ComponentModel.DataAnnotations;

namespace LiteWeightAPI.Api.Workouts.Requests;

public class RestartWorkoutRequest
{
	/// <summary>
	/// Workout to restart.
	/// </summary>
	[Required]
	public SetRoutineRequest Routine { get; set; } = null!;
}