using LiteWeightAPI.Commands.Complaints;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.Complaints;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;

namespace LiteWeightApiTests.Commands.Complaints;

public class GetComplaintTests : BaseTest
{
	private readonly GetComplaintHandler _handler;
	private readonly Mock<IRepository> _mockRepository;

	public GetComplaintTests()
	{
		_mockRepository = new Mock<IRepository>();
		_handler = new GetComplaintHandler(_mockRepository.Object);
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
			.Setup(x => x.GetComplaint(It.Is<string>(y => y == command.ComplaintId)))
			.ReturnsAsync(complaint);

		var response = await _handler.HandleAsync(command);

		Assert.Equal(command.ComplaintId, response.Id);
	}
	
	[Fact]
	public async Task Should_Throw_Exception_Missing_Permissions()
	{
		var command = Fixture.Create<GetComplaint>();
		
		_mockRepository
			.Setup(x => x.GetComplaint(It.Is<string>(y => y == command.ComplaintId)))
			.ReturnsAsync(Fixture.Create<Complaint>());
		
		await Assert.ThrowsAsync<ForbiddenException>(() => _handler.HandleAsync(command));
	}

	[Fact]
	public async Task Should_Throw_Exception_Complaint_Does_Not_Exist()
	{
		var command = Fixture.Create<GetComplaint>();

		_mockRepository
			.Setup(x => x.GetComplaint(It.Is<string>(y => y == command.UserId)))
			.ReturnsAsync((Complaint)null!);

		await Assert.ThrowsAsync<ResourceNotFoundException>(() => _handler.HandleAsync(command));
	}
}