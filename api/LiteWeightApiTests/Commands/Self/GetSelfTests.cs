using LiteWeightAPI.Commands.Self;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;

namespace LiteWeightApiTests.Commands.Self;

public class GetSelfTests : BaseTest
{
	private readonly GetSelfHandler _handler;
	private readonly Mock<IRepository> _mockRepository;

	public GetSelfTests()
	{
		_mockRepository = new Mock<IRepository>();
		_handler = new GetSelfHandler(_mockRepository.Object);
	}

	[Fact]
	public async Task Should_Get_Self()
	{
		var command = Fixture.Create<GetSelf>();

		var user = Fixture.Build<User>().With(x => x.Id, command.UserId).Create();

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.UserId)))
			.ReturnsAsync(user);

		var response = await _handler.HandleAsync(command);

		Assert.Equal(command.UserId, response.Id);
	}

	[Fact]
	public async Task Should_Throw_Exception_User_Does_Not_Exist()
	{
		var command = Fixture.Create<GetSelf>();

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.UserId)))
			.ReturnsAsync((User)null!);

		await Assert.ThrowsAsync<ResourceNotFoundException>(() => _handler.HandleAsync(command));
	}
}