namespace LiteWeightAPI.Api.ReceivedWorkouts.Responses;

public class ReceivedWeekResponse
{
	/// <summary>
	/// List of days in the routine.
	/// </summary>
	public IList<ReceivedDayResponse> Days { get; set; } = [];
}