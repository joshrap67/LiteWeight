using LiteWeightAPI.Commands.Self;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Services;

namespace LiteWeightApiTests.Commands.Self;

public class UpdateProfilePictureTests : BaseTest
{
	private readonly UpdateProfilePictureHandler _handler;
	private readonly Mock<IRepository> _mockRepository;

	public UpdateProfilePictureTests()
	{
		_mockRepository = new Mock<IRepository>();
		var storageService = new Mock<IStorageService>();
		_handler = new UpdateProfilePictureHandler(_mockRepository.Object, storageService.Object);
	}

	[Fact]
	public async Task Should_Set_Profile_Picture()
	{
		var command = Fixture.Create<UpdateProfilePicture>();

		var user = Fixture.Build<User>()
			.With(x => x.Id, command.UserId)
			.Create();

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.UserId)))
			.ReturnsAsync(user);

		var response = await _handler.HandleAsync(command);
		Assert.True(response);
	}
}