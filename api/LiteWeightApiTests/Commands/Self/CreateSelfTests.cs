using LiteWeightAPI.Commands.Self;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Errors.Exceptions;
using LiteWeightAPI.Services;

namespace LiteWeightApiTests.Commands.Self;

public class CreateSelfTests : BaseTest
{
	private readonly CreateSelfHandler _handler;
	private readonly IRepository _mockRepository;

	public CreateSelfTests()
	{
		_mockRepository = Substitute.For<IRepository>();
		var storageService = Substitute.For<IStorageService>();
		_handler = new CreateSelfHandler(_mockRepository, storageService);
	}

	[Fact]
	public async Task Should_Create_Self()
	{
		var command = Fixture.Create<CreateSelf>();

		_mockRepository
			.GetUserByUsername(Arg.Is<string>(y => y == command.Username))
			.Returns((User)null!);

		_mockRepository
			.GetUserByEmail(Arg.Is<string>(y => y == command.UserEmail))
			.Returns((User)null!);

		var createdUser = await _handler.HandleAsync(command);
		Assert.Equal(command.Username.ToLowerInvariant(), createdUser.Username);
		Assert.Equal(command.UserEmail, createdUser.Email);
		Assert.Equal(command.UserId, createdUser.Id);
		Assert.Equal(command.MetricUnits, createdUser.Settings.MetricUnits);
		Assert.NotNull(createdUser.ProfilePicture);
	}

	[Fact]
	public async Task Should_Throw_Exception_Username_Already_Exists()
	{
		var command = Fixture.Create<CreateSelf>();

		var user = Fixture.Build<User>()
			.With(x => x.Username, command.Username)
			.Create();

		_mockRepository
			.GetUserByUsername(Arg.Is<string>(y => y == command.Username))
			.Returns(user);

		await Assert.ThrowsAsync<AlreadyExistsException>(() => _handler.HandleAsync(command));
	}

	[Fact]
	public async Task Should_Throw_Exception_Email_Already_Exists()
	{
		var command = Fixture.Create<CreateSelf>();

		var user = Fixture.Build<User>()
			.With(x => x.Email, command.UserEmail)
			.Create();

		_mockRepository
			.GetUserByEmail(Arg.Is<string>(y => y == command.UserEmail))
			.Returns(user);

		await Assert.ThrowsAsync<AlreadyExistsException>(() => _handler.HandleAsync(command));
	}
}