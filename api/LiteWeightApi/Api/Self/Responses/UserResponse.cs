using LiteWeightAPI.Api.Exercises.Responses;

namespace LiteWeightAPI.Api.Self.Responses;

public class UserResponse
{
	/// <summary>
	/// Unique identifier of the user
	/// </summary>
	/// <example>f1e03cd1-e62c-4a53-84ed-498c72776fc2</example>
	public string Id { get; set; } = null!;

	/// <summary>
	/// Username of the user.
	/// </summary>
	/// <example>barbell_bill</example>
	public string Username { get; set; } = null!;

	/// <summary>
	/// Email address of the user.
	/// </summary>
	/// <example>barbellB12@gmail.com</example>
	public string Email { get; set; } = null!;

	/// <summary>
	/// File path of the user's profile picture.
	/// </summary>
	/// <example>0f1d96c3-ca22-4657-9f9b-136bb4621985.jpg</example>
	public string ProfilePicture { get; set; } = null!;

	/// <summary>
	/// Firebase token registered for the user's device to receive push notifications.
	/// </summary>
	/// <example>c-Id_13FTDf5c5PZGjsmg9:APA91bFG4w3Qnd16l0WDZzFYco3_71-_X595oncjilyKhSJBZC9FUD4mjg2lE68HCcDQR80Y6GDTuz-zbXK69V9D-jTBs4aXhv7LBAUTGzoC8h91q_QV-H1vz6XUGF8Mcob84_2-izMI</example>
	public string? FirebaseMessagingToken { get; set; }

	/// <summary>
	/// Token indicating the user has purchased LiteWeight premium. Currently not used.
	/// </summary>
	/// <example>a704f441-8ee3-471b-ac8a-abb0b7d8249a</example>
	public string? PremiumToken { get; set; }

	/// <summary>
	/// Workout Id that the user is currently on. Null signifies the user is not working on a workout.
	/// </summary>
	/// <example>b209f062-36fa-4089-aca0-31df4815744f</example>
	public string? CurrentWorkoutId { get; set; }

	/// <summary>
	/// Total number of workouts sent.
	/// </summary>
	/// <example>14</example>
	public int WorkoutsSent { get; set; }

	/// <summary>
	/// List of exercises the user owns.
	/// </summary>
	public IList<OwnedExerciseResponse> Exercises { get; set; } = [];

	/// <summary>
	/// List of pending friend requests for the user.
	/// </summary>
	public IList<FriendRequestResponse> FriendRequests { get; set; } = [];

	/// <summary>
	/// List of friends for the user.
	/// </summary>
	public IList<FriendResponse> Friends { get; set; } = [];

	/// <summary>
	/// List of workouts sent to the user.
	/// </summary>
	public IList<ReceivedWorkoutInfoResponse> ReceivedWorkouts { get; set; } = [];

	/// <summary>
	/// Preferences of the user.
	/// </summary>
	public UserSettingsResponse Settings { get; set; } = null!;

	/// <summary>
	/// List of workouts the user owns.
	/// </summary>
	public IList<WorkoutInfoResponse> Workouts { get; set; } = [];
}