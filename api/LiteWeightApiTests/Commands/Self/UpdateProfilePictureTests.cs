using LiteWeightAPI.Commands.Self;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Services;

namespace LiteWeightApiTests.Commands.Self;

public class UpdateProfilePictureTests : BaseTest
{
	private readonly UpdateProfilePictureHandler _handler;
	private readonly IRepository _mockRepository;

	public UpdateProfilePictureTests()
	{
		_mockRepository = Substitute.For<IRepository>();
		var storageService = Substitute.For<IStorageService>();
		_handler = new UpdateProfilePictureHandler(_mockRepository, storageService);
	}

	[Fact]
	public async Task Should_Set_Profile_Picture()
	{
		var command = Fixture.Create<UpdateProfilePicture>();

		var user = Fixture.Build<User>()
			.With(x => x.Id, command.UserId)
			.Create();

		_mockRepository
			.GetUser(Arg.Is<string>(y => y == command.UserId))
			.Returns(user);

		var response = await _handler.HandleAsync(command);
		Assert.True(response);
	}
}