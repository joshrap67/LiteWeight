using LiteWeightAPI.Commands.Users;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.Users;

namespace LiteWeightApiTests.Commands.Users;

public class SearchByUsernameTests : BaseTest
{
	private readonly SearchByUsernameHandler _handler;
	private readonly IRepository _mockRepository;

	public SearchByUsernameTests()
	{
		_mockRepository = Substitute.For<IRepository>();
		_handler = new SearchByUsernameHandler(_mockRepository);
	}

	[Fact]
	public async Task Should_Find_User_Not_Private_Account()
	{
		var command = Fixture.Create<SearchByUsername>();

		var preferences = new UserSettings();
		var foundUser = Fixture.Build<User>()
			.With(x => x.Username, command.Username)
			.With(x => x.Settings, preferences)
			.Create();

		_mockRepository
			.GetUserByUsername(Arg.Is<string>(y => y == command.Username))
			.Returns(foundUser);

		var response = await _handler.HandleAsync(command);
		Assert.NotNull(response);
		Assert.Equal(command.Username, response.Username);
	}

	[Fact]
	public async Task Should_Find_User_Private_Account()
	{
		var command = Fixture.Create<SearchByUsername>();

		var preferences = new UserSettings { PrivateAccount = true };
		var foundUser = Fixture.Build<User>()
			.With(x => x.Username, command.Username)
			.With(x => x.Settings, preferences)
			.With(x => x.Friends, [Fixture.Build<Friend>().With(x => x.UserId, command.InitiatorId).Create()])
			.Create();

		_mockRepository
			.GetUserByUsername(Arg.Is<string>(y => y == command.Username))
			.Returns(foundUser);

		var response = await _handler.HandleAsync(command);
		Assert.NotNull(response);
		Assert.Equal(command.Username, response.Username);
	}

	[Fact]
	public async Task Should_Return_Null_User_Not_Found()
	{
		var command = Fixture.Create<SearchByUsername>();

		_mockRepository
			.GetUserByUsername(Arg.Is<string>(y => y == command.Username))
			.Returns((User?)null);

		var response = await _handler.HandleAsync(command);
		Assert.Null(response);
	}

	[Fact]
	public async Task Should_Return_Null_Private_User()
	{
		var command = Fixture.Create<SearchByUsername>();

		var preferences = new UserSettings { PrivateAccount = true };
		var foundUser = Fixture.Build<User>()
			.With(x => x.Username, command.Username)
			.With(x => x.Settings, preferences)
			.Create();

		_mockRepository
			.GetUserByUsername(Arg.Is<string>(y => y == command.Username))
			.Returns(foundUser);

		var response = await _handler.HandleAsync(command);
		Assert.Null(response);
	}
}