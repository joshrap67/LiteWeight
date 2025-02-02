namespace LiteWeightAPI.Api.Workouts.Responses;

public class RoutineWeekResponse
{
	/// <summary>
	/// List of days in the routine.
	/// </summary>
	public IList<RoutineDayResponse> Days { get; set; } = [];
}