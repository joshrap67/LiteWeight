using LiteWeightAPI.Commands.Users;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;
using LiteWeightAPI.Services;

namespace LiteWeightApiTests.Commands.Users;

public class RemoveFriendTests : BaseTest
{
	private readonly RemoveFriendHandler _handler;
	private readonly IRepository _mockRepository;

	public RemoveFriendTests()
	{
		_mockRepository = Substitute.For<IRepository>();
		var pushNotificationService = Substitute.For<IPushNotificationService>();
		_handler = new RemoveFriendHandler(_mockRepository, pushNotificationService);
	}

	[Fact]
	public async Task Should_Remove_Friend()
	{
		var command = Fixture.Create<RemoveFriend>();

		var initiatorFriends = new List<Friend>
		{
			Fixture.Create<Friend>(),
			Fixture.Build<Friend>().With(x => x.UserId, command.RemovedUserId).Create()
		};
		var initiator = Fixture.Build<User>()
			.With(x => x.Id, command.InitiatorUserId)
			.With(x => x.Friends, initiatorFriends)
			.Create();

		var removedFriendFriends = new List<Friend>
		{
			Fixture.Create<Friend>(),
			Fixture.Build<Friend>().With(x => x.UserId, command.InitiatorUserId).Create()
		};
		var removedFriend = Fixture.Build<User>()
			.With(x => x.Id, command.RemovedUserId)
			.With(x => x.Friends, removedFriendFriends)
			.Create();

		_mockRepository
			.GetUser(Arg.Is<string>(y => y == command.RemovedUserId))
			.Returns(removedFriend);
		_mockRepository
			.GetUser(Arg.Is<string>(y => y == command.InitiatorUserId))
			.Returns(initiator);

		var response = await _handler.HandleAsync(command);
		Assert.True(response);
		Assert.True(initiator.Friends.All(x => x.UserId != command.RemovedUserId));
		Assert.True(initiator.Friends.All(x => x.UserId != command.InitiatorUserId));
	}

	[Fact]
	public async Task Should_Not_Fail_Friend_Not_Found()
	{
		var command = Fixture.Create<RemoveFriend>();

		var initiator = Fixture.Build<User>()
			.With(x => x.Id, command.InitiatorUserId)
			.Create();
		var canceledUser = Fixture.Build<User>().With(x => x.Id, command.RemovedUserId).Create();

		_mockRepository
			.GetUser(Arg.Is<string>(y => y == command.RemovedUserId))
			.Returns(canceledUser);
		_mockRepository
			.GetUser(Arg.Is<string>(y => y == command.InitiatorUserId))
			.Returns(initiator);

		var response = await _handler.HandleAsync(command);
		Assert.False(response);
	}

	[Fact]
	public async Task Should_Exception_User_Does_Not_Exist()
	{
		var command = Fixture.Create<RemoveFriend>();

		_mockRepository
			.GetUser(Arg.Is<string>(y => y == command.RemovedUserId))
			.Returns((User)null!);

		await Assert.ThrowsAsync<ResourceNotFoundException>(() => _handler.HandleAsync(command));
	}
}