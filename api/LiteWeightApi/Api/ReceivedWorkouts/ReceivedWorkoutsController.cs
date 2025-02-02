using LiteWeightAPI.Api.ReceivedWorkouts.Requests;
using LiteWeightAPI.Api.ReceivedWorkouts.Responses;
using LiteWeightAPI.Commands;
using LiteWeightAPI.Commands.ReceivedWorkouts;
using LiteWeightAPI.Commands.Self;
using LiteWeightAPI.Errors.Attributes;
using Microsoft.AspNetCore.Mvc;

namespace LiteWeightAPI.Api.ReceivedWorkouts;

[Route("received-workouts")]
[ApiController]
public class ReceivedWorkoutsController : BaseController
{
	private readonly ICommandDispatcher _dispatcher;

	public ReceivedWorkoutsController(ICommandDispatcher dispatcher)
	{
		_dispatcher = dispatcher;
	}

	/// <summary>Get Received Workout</summary>
	/// <remarks>Gets a received workout. Conditional that it was sent to the authenticated user.</remarks>
	/// <param name="receivedWorkoutId">Id of the received workout to get</param>
	[HttpGet("{receivedWorkoutId}")]
	[ProducesResponseType(StatusCodes.Status200OK)]
	[ProducesResponseType(StatusCodes.Status404NotFound)]
	public async Task<ActionResult<ReceivedWorkoutResponse>> GetReceivedWorkout(string receivedWorkoutId)
	{
		var receivedWorkout = await _dispatcher.DispatchAsync<GetReceivedWorkout, ReceivedWorkoutResponse>(
			new GetReceivedWorkout
			{
				UserId = CurrentUserId,
				ReceivedWorkoutId = receivedWorkoutId
			});
		return receivedWorkout;
	}

	/// <summary>Accept Received Workout</summary>
	/// <remarks>
	/// Accepts a received workout and adds any exercises that the user doesn't already own to their owned exercises.<br/><br/>
	/// Accepting a workout deletes the received workout from the database and creates a workout with the values of that received workout.
	/// </remarks>
	/// <param name="receivedWorkoutId">Id of the received workout to accept</param>
	/// <param name="request">Request</param>
	[HttpPost("{receivedWorkoutId}/accept")]
	[InvalidRequest, MaxLimit, AlreadyExists]
	[ProducesResponseType(StatusCodes.Status201Created)]
	[ProducesResponseType(StatusCodes.Status404NotFound)]
	public async Task<ActionResult<AcceptReceivedWorkoutResponse>> AcceptReceivedWorkout(string receivedWorkoutId,
		AcceptReceivedWorkoutRequest request)
	{
		var response = await _dispatcher.DispatchAsync<AcceptReceivedWorkout, AcceptReceivedWorkoutResponse>(
			new AcceptReceivedWorkout
			{
				UserId = CurrentUserId,
				ReceivedWorkoutId = receivedWorkoutId,
				NewName = request.WorkoutName
			});
		return new CreatedResult(new Uri($"/workouts/{response.NewWorkoutInfo.WorkoutId}", UriKind.Relative), response);
	}

	/// <summary>Decline Received Workout</summary>
	/// <remarks>Declines a workout and deletes it from the database. Conditional that the recipient matches the authenticated user.</remarks>
	/// <param name="receivedWorkoutId">Id of the received workout to decline</param>
	[HttpDelete("{receivedWorkoutId}/decline")]
	[ProducesResponseType(StatusCodes.Status204NoContent)]
	[ProducesResponseType(StatusCodes.Status404NotFound)]
	public async Task<ActionResult> DeclineReceivedWorkout(string receivedWorkoutId)
	{
		await _dispatcher.DispatchAsync<DeclineReceivedWorkout, bool>(new DeclineReceivedWorkout
		{
			UserId = CurrentUserId, ReceivedWorkoutId = receivedWorkoutId
		});
		return NoContent();
	}
	
	/// <summary>Set All Received Workouts Seen</summary>
	/// <remarks>Sets all received workouts for the authenticated user as seen.</remarks>
	[HttpPut("all-seen")]
	[ProducesResponseType(StatusCodes.Status204NoContent)]
	public async Task<ActionResult> SetAllReceivedWorkoutsSeen()
	{
		await _dispatcher.DispatchAsync<SetAllReceivedWorkoutsSeen, bool>(new SetAllReceivedWorkoutsSeen
		{
			UserId = CurrentUserId
		});
		return NoContent();
	}

	/// <summary>Set Received Workout Seen</summary>
	/// <remarks>Sets a given received workout for the authenticated user as seen.</remarks>
	/// <param name="receivedWorkoutId">Received workout to set as seen</param>
	[HttpPut("{receivedWorkoutId}/seen")]
	[ProducesResponseType(StatusCodes.Status204NoContent)]
	public async Task<ActionResult> SetReceivedWorkoutSeen(string receivedWorkoutId)
	{
		await _dispatcher.DispatchAsync<SetReceivedWorkoutSeen, bool>(new SetReceivedWorkoutSeen
		{
			UserId = CurrentUserId, ReceivedWorkoutId = receivedWorkoutId
		});
		return NoContent();
	}
}