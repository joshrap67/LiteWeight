using LiteWeightAPI.Api.Exercises.Requests;
using LiteWeightAPI.Api.Exercises.Responses;
using LiteWeightAPI.Commands;
using LiteWeightAPI.Commands.Exercises;
using LiteWeightAPI.Errors.Attributes;
using LiteWeightAPI.Maps;
using Microsoft.AspNetCore.Mvc;

namespace LiteWeightAPI.Api.Exercises;

[Route("exercises")]
[ApiController]
public class ExercisesController : BaseController
{
	private readonly ICommandDispatcher _dispatcher;

	public ExercisesController(ICommandDispatcher dispatcher)
	{
		_dispatcher = dispatcher;
	}

	/// <summary>Create Exercise</summary>
	/// <remarks>Creates an exercise to be owned by the authenticated user. The name of the exercise must not already exist for the user.</remarks>
	[HttpPost]
	[AlreadyExists, InvalidRequest, MaxLimit]
	[ProducesResponseType(StatusCodes.Status201Created)]
	public async Task<ActionResult<OwnedExerciseResponse>> CreateExercise(SetExerciseRequest request)
	{
		var command = request.ToCommand(CurrentUserId);

		var response = await _dispatcher.DispatchAsync<CreateExercise, OwnedExerciseResponse>(command);
		return new CreatedResult(new Uri($"/exercises/{response.Id}", UriKind.Relative), response);
	}

	/// <summary>Update Exercise</summary>
	/// <remarks>Updates an exercise owned by the authenticated user, if it exists. Note that any new name for the exercise must not already exist.</remarks>
	/// <param name="exerciseId">Id of the exercise to update</param>
	/// <param name="request">Request</param>
	[HttpPut("{exerciseId}")]
	[AlreadyExists, InvalidRequest]
	[ProducesResponseType(StatusCodes.Status204NoContent)]
	[ProducesResponseType(StatusCodes.Status404NotFound)]
	public async Task<ActionResult> UpdateExercise(string exerciseId, SetExerciseRequest request)
	{
		var command = request.ToCommand(exerciseId, CurrentUserId);

		await _dispatcher.DispatchAsync<UpdateExercise, bool>(command);
		return NoContent();
	}

	/// <summary>Delete Exercise</summary>
	/// <remarks>Deletes an exercise owned by the authenticated user. Removes the deleted exercise from any workout it was a part of.</remarks>
	/// <param name="exerciseId">Id of the exercise to delete</param>
	[HttpDelete("{exerciseId}")]
	[ProducesResponseType(StatusCodes.Status204NoContent)]
	[ProducesResponseType(StatusCodes.Status404NotFound)]
	public async Task<ActionResult> DeleteExercise(string exerciseId)
	{
		var command = new DeleteExercise { ExerciseId = exerciseId, UserId = CurrentUserId };
		await _dispatcher.DispatchAsync<DeleteExercise, bool>(command);
		return NoContent();
	}
}