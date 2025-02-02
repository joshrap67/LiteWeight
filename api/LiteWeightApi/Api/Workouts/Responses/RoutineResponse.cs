namespace LiteWeightAPI.Api.Workouts.Responses;

public class RoutineResponse
{
	/// <summary>
	/// List of weeks in the routine.
	/// </summary>
	public IList<RoutineWeekResponse> Weeks { get; set; } = [];
}