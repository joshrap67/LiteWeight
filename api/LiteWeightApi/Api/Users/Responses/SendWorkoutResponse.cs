namespace LiteWeightAPI.Api.Users.Responses;

public class SendWorkoutResponse
{
	/// <summary>
	/// Id of the received workout that was created as a result of sending the workout.
	/// </summary>
	/// <example>3ac84a61-4822-4ba3-ac93-626fdf087acf</example>
	public string ReceivedWorkoutId { get; set; } = null!;
}