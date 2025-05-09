using LiteWeightAPI.Commands.Users;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;
using LiteWeightAPI.Services;

namespace LiteWeightApiTests.Commands.Users;

public class DeclineFriendRequestTests : BaseTest
{
	private readonly DeclineFriendRequestHandler _handler;
	private readonly IRepository _mockRepository;

	public DeclineFriendRequestTests()
	{
		_mockRepository = Substitute.For<IRepository>();
		var pushNotificationService = Substitute.For<IPushNotificationService>();
		_handler = new DeclineFriendRequestHandler(_mockRepository, pushNotificationService);
	}

	[Fact]
	public async Task Should_Decline_Friend_Request()
	{
		var command = Fixture.Create<DeclineFriendRequest>();
		var friendRequests = new List<FriendRequest>
		{
			Fixture.Create<FriendRequest>(),
			Fixture.Build<FriendRequest>().With(x => x.UserId, command.UserIdToDecline).Create()
		};

		var initiator = Fixture.Build<User>()
			.With(x => x.Id, command.InitiatorUserId)
			.With(x => x.FriendRequests, friendRequests)
			.Create();
		var declinedUser = Fixture.Build<User>()
			.With(x => x.Id, command.UserIdToDecline)
			.With(x => x.Friends, [
				Fixture.Build<Friend>()
					.With(x => x.UserId, command.InitiatorUserId)
					.With(x => x.Confirmed, false)
					.Create()
			])
			.Create();

		_mockRepository
			.GetUser(Arg.Is<string>(y => y == command.UserIdToDecline))
			.Returns(declinedUser);
		_mockRepository
			.GetUser(Arg.Is<string>(y => y == command.InitiatorUserId))
			.Returns(initiator);

		var response = await _handler.HandleAsync(command);
		Assert.True(response);
		Assert.True(initiator.FriendRequests.All(x => x.UserId != command.UserIdToDecline));
		Assert.True(declinedUser.Friends.All(x => x.UserId != command.InitiatorUserId));
	}

	[Fact]
	public async Task Should_Not_Fail_Friend_Request_Not_Found()
	{
		var command = Fixture.Create<DeclineFriendRequest>();

		var initiator = Fixture.Build<User>()
			.With(x => x.Id, command.InitiatorUserId)
			.Create();
		var declinedUser = Fixture.Build<User>().With(x => x.Id, command.UserIdToDecline).Create();

		_mockRepository
			.GetUser(Arg.Is<string>(y => y == command.UserIdToDecline))
			.Returns(declinedUser);
		_mockRepository
			.GetUser(Arg.Is<string>(y => y == command.InitiatorUserId))
			.Returns(initiator);

		var response = await _handler.HandleAsync(command);
		Assert.False(response);
	}

	[Fact]
	public async Task Should_Exception_User_Does_Not_Exist()
	{
		var command = Fixture.Create<DeclineFriendRequest>();

		_mockRepository
			.GetUser(Arg.Is<string>(y => y == command.UserIdToDecline))
			.Returns((User)null!);

		await Assert.ThrowsAsync<ResourceNotFoundException>(() => _handler.HandleAsync(command));
	}
}