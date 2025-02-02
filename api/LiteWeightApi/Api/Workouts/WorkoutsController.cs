using AutoMapper;
using LiteWeightAPI.Api.Self.Requests;
using LiteWeightAPI.Api.Workouts.Requests;
using LiteWeightAPI.Api.Workouts.Responses;
using LiteWeightAPI.Commands;
using LiteWeightAPI.Commands.Workouts;
using LiteWeightAPI.Errors.Attributes;
using Microsoft.AspNetCore.Mvc;

namespace LiteWeightAPI.Api.Workouts;

[Route("workouts")]
[ApiController]
public class WorkoutsController : BaseController
{
	private readonly ICommandDispatcher _dispatcher;
	private readonly IMapper _mapper;

	public WorkoutsController(ICommandDispatcher dispatcher, IMapper mapper)
	{
		_dispatcher = dispatcher;
		_mapper = mapper;
	}

	/// <summary>Create Workout</summary>
	/// <remarks>Creates a workout and adds it to the authenticated user's list of workouts. Any exercises that were added to the workout are updated.</remarks>
	[HttpPost]
	[InvalidRequest, MaxLimit]
	[ProducesResponseType(StatusCodes.Status201Created)]
	public async Task<ActionResult<UserAndWorkoutResponse>> CreateWorkout(CreateWorkoutRequest request)
	{
		var command = _mapper.Map<CreateWorkout>(request);
		command.UserId = CurrentUserId;

		var response = await _dispatcher.DispatchAsync<CreateWorkout, UserAndWorkoutResponse>(command);
		return new CreatedResult(new Uri($"/workouts/{response.Workout.Id}", UriKind.Relative), response);
	}

	/// <summary>Get Workout</summary>
	/// <remarks>Gets a workout. Conditional that the creator matches the authenticated user.</remarks>
	/// <param name="workoutId">Id of the workout to get</param>
	[HttpGet("{workoutId}")]
	[ProducesResponseType(StatusCodes.Status200OK)]
	[ProducesResponseType(StatusCodes.Status404NotFound)]
	public async Task<ActionResult<WorkoutResponse>> GetWorkout(string workoutId)
	{
		var response = await _dispatcher.DispatchAsync<GetWorkout, WorkoutResponse>(new GetWorkout
		{
			WorkoutId = workoutId,
			UserId = CurrentUserId
		});
		return response;
	}

	/// <summary>Copy Workout</summary>
	/// <remarks>Copies a workout as a new workout. Any exercises that are now apart of the copied workout are updated.</remarks>
	/// <param name="workoutId">Id of the workout to copy</param>
	/// <param name="request"></param>
	[HttpPost("{workoutId}/copy")]
	[InvalidRequest, AlreadyExists, MaxLimit]
	[ProducesResponseType(StatusCodes.Status201Created)]
	[ProducesResponseType(StatusCodes.Status404NotFound)]
	public async Task<ActionResult<UserAndWorkoutResponse>> CopyWorkout(string workoutId, CopyWorkoutRequest request)
	{
		var response = await _dispatcher.DispatchAsync<CopyWorkout, UserAndWorkoutResponse>(new CopyWorkout
		{
			UserId = CurrentUserId,
			Name = request.Name,
			WorkoutId = workoutId
		});
		return new CreatedResult(new Uri($"/workouts/{response.Workout.Id}", UriKind.Relative), response);
	}

	/// <summary>Set Routine</summary>
	/// <remarks>Sets the routine of a given workout. Any exercises that are now apart of the updated routine are updated.</remarks>
	/// <param name="request"></param>
	/// <param name="workoutId">Id of the workout of the routine</param>
	[HttpPut("{workoutId}/routine")]
	[InvalidRequest]
	[ProducesResponseType(StatusCodes.Status200OK)]
	[ProducesResponseType(StatusCodes.Status404NotFound)]
	public async Task<ActionResult<UserAndWorkoutResponse>> SetRoutine(SetRoutineRequest request, string workoutId)
	{
		var response = await _dispatcher.DispatchAsync<UpdateRoutine, UserAndWorkoutResponse>(new UpdateRoutine
		{
			Routine = _mapper.Map<SetRoutine>(request),
			UserId = CurrentUserId,
			WorkoutId = workoutId
		});
		return response;
	}

	/// <summary>Update Workout Progress</summary>
	/// <remarks>Updates the specified workout's progress.</remarks>
	/// <param name="workoutId">Id of the workout to update the progress of</param>
	/// <param name="request"></param>
	[HttpPut("{workoutId}/update-progress")]
	[InvalidRequest]
	[ProducesResponseType(StatusCodes.Status204NoContent)]
	[ProducesResponseType(StatusCodes.Status404NotFound)]
	public async Task<ActionResult> UpdateProgress(string workoutId, UpdateWorkoutProgressRequest request)
	{
		var command = new UpdateWorkoutProgress
		{
			WorkoutId = workoutId,
			UserId = CurrentUserId,
			Routine = _mapper.Map<SetRoutine>(request.Routine),
			CurrentWeek = request.CurrentWeek,
			CurrentDay = request.CurrentDay
		};
		await _dispatcher.DispatchAsync<UpdateWorkoutProgress, bool>(command);
		return NoContent();
	}

	/// <summary>Reset Statistics</summary>
	/// <remarks>Resets the statistics for a given workout, if it exists.</remarks>
	/// <param name="workoutId">Id of the workout to reset the statistics of</param>
	[HttpPut("{workoutId}/reset-statistics")]
	[ProducesResponseType(StatusCodes.Status204NoContent)]
	[ProducesResponseType(StatusCodes.Status404NotFound)]
	public async Task<ActionResult> ResetStatistics(string workoutId)
	{
		await _dispatcher.DispatchAsync<ResetStatistics, bool>(new ResetStatistics
		{
			UserId = CurrentUserId,
			WorkoutId = workoutId
		});
		return NoContent();
	}

	/// <summary>Restart Workout</summary>
	/// <remarks>
	/// Restarts the workout to have all exercises set to incomplete, and updates the statistics of the authenticated user using the state of the workout before it was restarted.
	/// <br/><br/>If enabled on the authenticated user's preferences, the default weights of any completed exercises will be updated if their completed weight is greater than the current default weight.
	/// </remarks>
	/// <param name="workoutId">Id of the workout to restart</param>
	/// <param name="request"></param>
	[HttpPost("{workoutId}/restart")]
	[InvalidRequest]
	[ProducesResponseType(StatusCodes.Status200OK)]
	[ProducesResponseType(StatusCodes.Status404NotFound)]
	public async Task<ActionResult<UserAndWorkoutResponse>> RestartWorkout(string workoutId,
		RestartWorkoutRequest request)
	{
		var response = await _dispatcher.DispatchAsync<RestartWorkout, UserAndWorkoutResponse>(new RestartWorkout
		{
			UserId = CurrentUserId,
			WorkoutId = workoutId,
			Routine = _mapper.Map<SetRoutine>(request.Routine)
		});
		return response;
	}

	/// <summary>Rename Workout</summary>
	/// <remarks>Renames a given workout. Name must be unique.</remarks>
	/// <param name="workoutId">Id of the workout to rename</param>
	/// <param name="request"></param>
	[HttpPut("{workoutId}/rename")]
	[InvalidRequest, AlreadyExists]
	[ProducesResponseType(StatusCodes.Status204NoContent)]
	[ProducesResponseType(StatusCodes.Status404NotFound)]
	public async Task<ActionResult> RenameWorkout(string workoutId, RenameWorkoutRequest request)
	{
		await _dispatcher.DispatchAsync<RenameWorkout, bool>(new RenameWorkout
		{
			UserId = CurrentUserId,
			WorkoutId = workoutId,
			NewName = request.Name
		});
		return NoContent();
	}

	/// <summary>Delete Workout</summary>
	/// <remarks>Deletes a given workout. Removes it from the authenticated user's list of workouts, and from the list of workouts on the exercises of the deleted workout.</remarks>
	/// <param name="workoutId">Id of the workout to delete</param>
	[HttpDelete("{workoutId}")]
	[ProducesResponseType(StatusCodes.Status204NoContent)]
	[ProducesResponseType(StatusCodes.Status404NotFound)]
	public async Task<ActionResult> DeleteWorkout(string workoutId)
	{
		await _dispatcher.DispatchAsync<DeleteWorkout, bool>(new DeleteWorkout
		{
			UserId = CurrentUserId,
			WorkoutId = workoutId
		});
		return NoContent();
	}

	/// <summary>Delete Workout and Set Current</summary>
	/// <remarks>
	/// Deletes a given workout. Removes it from the authenticated user's list of workouts, and from the list of workouts on the exercises of the deleted workout.
	/// <br/><br/> Also sets the current workout to the specified workout, if it exists.
	/// </remarks>
	/// <param name="workoutId">Id of the workout to delete</param>
	/// <param name="request">Request</param>
	[HttpPut("{workoutId}/delete-and-set-current")]
	[WorkoutNotFound]
	[ProducesResponseType(StatusCodes.Status204NoContent)]
	[ProducesResponseType(StatusCodes.Status404NotFound)]
	public async Task<ActionResult> DeleteWorkoutAndSetCurrent(string workoutId, SetCurrentWorkoutRequest request)
	{
		// combining these two actions since it is a terrible state to be in on the app atm if the delete succeeds and the set current workout does not. So need a transactional request
		await _dispatcher.DispatchAsync<DeleteWorkoutAndSetCurrent, bool>(new DeleteWorkoutAndSetCurrent
		{
			UserId = CurrentUserId,
			WorkoutToDeleteId = workoutId,
			CurrentWorkoutId = request.CurrentWorkoutId
		});
		return NoContent();
	}
}