using LiteWeightAPI.Api.Complaints.Responses;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;
using LiteWeightAPI.Maps;

namespace LiteWeightAPI.Commands.Complaints;

public class GetComplaint : ICommand<ComplaintResponse>
{
	public required string UserId { get; set; }
	public required string ComplaintId { get; set; }
}

public class GetComplaintHandler : ICommandHandler<GetComplaint, ComplaintResponse>
{
	private readonly IRepository _repository;

	public GetComplaintHandler(IRepository repository)
	{
		_repository = repository;
	}

	public async Task<ComplaintResponse> HandleAsync(GetComplaint command)
	{
		var complaint = await _repository.GetComplaint(command.ComplaintId);
		if (complaint == null)
		{
			throw new ResourceNotFoundException("complaint");
		}

		if (complaint.ClaimantUserId != command.UserId)
		{
			throw new ForbiddenException("Missing permissions");
		}

		return complaint.ToResponse();
	}
}