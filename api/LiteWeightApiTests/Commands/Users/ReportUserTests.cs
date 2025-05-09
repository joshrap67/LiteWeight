using LiteWeightAPI.Commands.Users;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;
using NodaTime;

namespace LiteWeightApiTests.Commands.Users;

public class ReportUserTests : BaseTest
{
	private readonly ReportUserHandler _handler;
	private readonly IRepository _mockRepository;

	public ReportUserTests()
	{
		_mockRepository = Substitute.For<IRepository>();
		var clockMock = Substitute.For<IClock>();
		_handler = new ReportUserHandler(_mockRepository, clockMock);
	}

	[Fact]
	public async Task Should_Report_Friend()
	{
		var command = Fixture.Create<ReportUser>();


		var reportedUser = Fixture.Build<User>()
			.With(x => x.Id, command.ReportedUserId)
			.Create();

		_mockRepository
			.GetUser(Arg.Is<string>(y => y == command.ReportedUserId))
			.Returns(reportedUser);

		var response = await _handler.HandleAsync(command);
		Assert.Equal(command.ReportedUserId, response.ReportedUserId);
	}

	[Fact]
	public async Task Should_Exception_User_Does_Not_Exist()
	{
		var command = Fixture.Create<ReportUser>();

		_mockRepository
			.GetUser(Arg.Is<string>(y => y == command.ReportedUserId))
			.Returns((User)null!);

		await Assert.ThrowsAsync<ResourceNotFoundException>(() => _handler.HandleAsync(command));
	}
}