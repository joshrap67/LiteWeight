using LiteWeightAPI.Api.Self.Responses;

namespace LiteWeightAPI.Api.Workouts.Responses;

public class UserAndWorkoutResponse
{
	/// <summary>
	/// Updated user.
	/// </summary>
	public UserResponse User { get; set; } = null!;

	/// <summary>
	/// Updated workout.
	/// </summary>
	public WorkoutResponse Workout { get; set; } = null!;
}