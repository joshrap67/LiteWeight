using System.ComponentModel.DataAnnotations;
using LiteWeightAPI.Imports;

namespace LiteWeightAPI.Api.Workouts.Requests;

public class SetRoutineRequest
{
	/// <summary>
	/// Weeks of the routine
	/// </summary>
	[Required]
	[MaxLength(Globals.MaxWeeksRoutine)]
	public IList<SetRoutineWeekRequest> Weeks { get; set; } = [];
}

public class SetRoutineWeekRequest
{
	/// <summary>
	/// Days of the routine
	/// </summary>
	[Required]
	[MaxLength(Globals.MaxDaysRoutine)]
	public IList<SetRoutineDayRequest> Days { get; set; } = [];
}

public class SetRoutineDayRequest
{
	/// <summary>
	/// Arbitrary tag of the day.
	/// </summary>
	/// <example>Back and Biceps Day</example>
	[MaxLength(Globals.MaxDayTagLength)]
	public string? Tag { get; set; }

	/// <summary>
	/// List of exercises for the given day.
	/// </summary>
	[Required]
	[MaxLength(Globals.MaxExercises)]
	public IList<SetRoutineExerciseRequest> Exercises { get; set; } = [];
}

public class SetRoutineExerciseRequest
{
	/// <summary>
	/// Id of the exercise (reference to the list of exercises on the user).
	/// </summary>
	/// <example>88a54457-2253-404e-ac09-82a8f2ce5fb8</example>
	[Required]
	public string ExerciseId { get; set; } = null!;

	/// <summary>
	/// Has the user completed this exercise?
	/// </summary>
	public bool Completed { get; set; }

	/// <summary>
	/// Weight of the exercise.
	/// </summary>
	/// <example>30.0</example>
	[Required]
	[Range(0.0, Globals.MaxWeight)]
	public double Weight { get; set; }

	/// <summary>
	/// Number of sets for the exercise.
	/// </summary>
	/// <example>3</example>
	[Required]
	[Range(0, Globals.MaxSets)]
	public int Sets { get; set; }

	/// <summary>
	/// Number of reps for the exercise.
	/// </summary>
	/// <example>15</example>
	[Required]
	[Range(0, Globals.MaxReps)]
	public int Reps { get; set; }

	/// <summary>
	/// Optional instructions for this exercise.
	/// </summary>
	/// <example>Rest for 90 seconds between sets.</example>
	[MaxLength(Globals.MaxInstructionsLength)]
	public string? Instructions { get; set; }
}