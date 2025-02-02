namespace LiteWeightAPI.Api.ReceivedWorkouts.Responses;

public class ReceivedRoutineResponse
{
	/// <summary>
	/// List of weeks in the routine.
	/// </summary>
	public IList<ReceivedWeekResponse> Weeks { get; set; } = [];
}