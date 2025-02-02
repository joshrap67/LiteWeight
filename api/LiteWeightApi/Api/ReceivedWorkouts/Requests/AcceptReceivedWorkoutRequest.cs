using System.ComponentModel.DataAnnotations;
using LiteWeightAPI.Imports;

namespace LiteWeightAPI.Api.ReceivedWorkouts.Requests;

public class AcceptReceivedWorkoutRequest
{
	/// <summary>
	/// Optional name to set for the workout once accepted. Must be unique. If not specified, the created workout will have the name of the received workout.
	/// </summary>
	/// <example>Olympic Routine</example>
	[MaxLength(Globals.MaxWorkoutNameLength)]
	public string? WorkoutName { get; set; }
}