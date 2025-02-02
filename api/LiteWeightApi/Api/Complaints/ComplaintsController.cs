using LiteWeightAPI.Api.Complaints.Responses;
using LiteWeightAPI.Commands;
using LiteWeightAPI.Commands.Complaints;
using Microsoft.AspNetCore.Mvc;

namespace LiteWeightAPI.Api.Complaints;

[Route("complaints")]
[ApiController]
public class ComplaintsController : BaseController
{
	private readonly ICommandDispatcher _dispatcher;

	public ComplaintsController(ICommandDispatcher dispatcher)
	{
		_dispatcher = dispatcher;
	}

	/// <summary>Get Complaint</summary>
	/// <remarks>Gets a complaint that was filed by the authenticated user.</remarks>
	/// <param name="complaintId">Id of the complaint to get</param>
	[HttpGet("{complaintId}")]
	[ProducesResponseType(StatusCodes.Status200OK)]
	[ProducesResponseType(StatusCodes.Status404NotFound)]
	public async Task<ActionResult<ComplaintResponse>> GetComplaint(string complaintId)
	{
		var command = new GetComplaint { ComplaintId = complaintId, UserId = CurrentUserId };
		var response = await _dispatcher.DispatchAsync<GetComplaint, ComplaintResponse>(command);

		return response;
	}
}