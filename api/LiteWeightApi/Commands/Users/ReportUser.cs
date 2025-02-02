using AutoMapper;
using LiteWeightAPI.Api.Complaints.Responses;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.Complaints;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;
using NodaTime;

namespace LiteWeightAPI.Commands.Users;

public class ReportUser : ICommand<ComplaintResponse>
{
	public required string InitiatorUserId { get; set; }
	public required string ReportedUserId { get; set; }
	public required string Description { get; set; }
}

public class ReportUserHandler : ICommandHandler<ReportUser, ComplaintResponse>
{
	private readonly IRepository _repository;
	private readonly IClock _clock;
	private readonly IMapper _mapper;

	public ReportUserHandler(IRepository repository, IClock clock, IMapper mapper)
	{
		_repository = repository;
		_clock = clock;
		_mapper = mapper;
	}

	public async Task<ComplaintResponse> HandleAsync(ReportUser command)
	{
		var userToReport = await _repository.GetUser(command.ReportedUserId);

		if (userToReport == null)
		{
			throw new ResourceNotFoundException("User");
		}

		var complaint = new Complaint
		{
			ReportedUserId = command.ReportedUserId,
			ReportedUsername = userToReport.Username,
			ReportedUtc = _clock.GetCurrentInstant(),
			ClaimantUserId = command.InitiatorUserId,
			Description = command.Description
		};
		await _repository.CreateComplaint(complaint);

		return _mapper.Map<ComplaintResponse>(complaint);
	}
}