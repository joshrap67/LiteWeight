using LiteWeightAPI.Commands.Complaints;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.Complaints;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;


namespace LiteWeightApiTests.Commands.Complaints;

public class GetComplaintTests : BaseTest
{
	private readonly GetComplaintHandler _handler;
	private readonly IRepository _mockRepository;

	public GetComplaintTests()
	{
		_mockRepository = Substitute.For<IRepository>();
		_handler = new GetComplaintHandler(_mockRepository);
	}

	[Fact]
	public async Task Should_Get_Complaint()
	{
		var command = Fixture.Create<GetComplaint>();

		var complaint = Fixture.Build<Complaint>()
			.With(x => x.ClaimantUserId, command.UserId)
			.With(x => x.Id, command.ComplaintId)
			.Create();

		_mockRepository
			.GetComplaint(Arg.Is<string>(y => y == command.ComplaintId))
			.Returns(complaint);

		var response = await _handler.HandleAsync(command);

		Assert.Equal(command.ComplaintId, response.Id);
	}

	[Fact]
	public async Task Should_Throw_Exception_Missing_Permissions()
	{
		var command = Fixture.Create<GetComplaint>();

		_mockRepository
			.GetComplaint(Arg.Is<string>(y => y == command.ComplaintId))
			.Returns(Fixture.Create<Complaint>());

		await Assert.ThrowsAsync<ForbiddenException>(() => _handler.HandleAsync(command));
	}

	[Fact]
	public async Task Should_Throw_Exception_Complaint_Does_Not_Exist()
	{
		var command = Fixture.Create<GetComplaint>();

		_mockRepository
			.GetComplaint(Arg.Is<string>(y => y == command.UserId))
			.Returns((Complaint)null!);

		await Assert.ThrowsAsync<ResourceNotFoundException>(() => _handler.HandleAsync(command));
	}
}