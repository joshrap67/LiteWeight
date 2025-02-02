using LiteWeightAPI.Commands.Users;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;
using LiteWeightAPI.Services;

namespace LiteWeightApiTests.Commands.Users;

public class CancelFriendRequestTests : BaseTest
{
	private readonly CancelFriendRequestHandler _handler;
	private readonly Mock<IRepository> _mockRepository;

	public CancelFriendRequestTests()
	{
		_mockRepository = new Mock<IRepository>();
		var pushNotificationService = new Mock<IPushNotificationService>().Object;
		_handler = new CancelFriendRequestHandler(_mockRepository.Object, pushNotificationService);
	}

	[Fact]
	public async Task Should_Cancel_Friend_Request()
	{
		var command = Fixture.Create<CancelFriendRequest>();

		var friends = new List<Friend>
		{
			Fixture.Create<Friend>(),
			Fixture.Build<Friend>().With(x => x.UserId, command.UserIdToCancel).Create()
		};
		var initiator = Fixture.Build<User>()
			.With(x => x.Id, command.InitiatorUserId)
			.With(x => x.Friends, friends)
			.Create();

		var friendRequests = new List<FriendRequest>
		{
			Fixture.Create<FriendRequest>(),
			Fixture.Build<FriendRequest>().With(x => x.UserId, command.InitiatorUserId).Create()
		};
		var canceledUser = Fixture.Build<User>()
			.With(x => x.Id, command.UserIdToCancel)
			.With(x => x.FriendRequests, friendRequests)
			.Create();

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.UserIdToCancel)))
			.ReturnsAsync(canceledUser);
		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.InitiatorUserId)))
			.ReturnsAsync(initiator);

		var response = await _handler.HandleAsync(command);
		Assert.True(response);
		Assert.True(initiator.Friends.All(x => x.UserId != command.UserIdToCancel));
		Assert.True(initiator.FriendRequests.All(x => x.UserId != command.InitiatorUserId));
	}

	[Fact]
	public async Task Should_Not_Fail_Friend_Not_Found()
	{
		var command = Fixture.Create<CancelFriendRequest>();

		var initiator = Fixture.Build<User>()
			.With(x => x.Id, command.InitiatorUserId)
			.Create();
		var canceledUser = Fixture.Build<User>().With(x => x.Id, command.UserIdToCancel).Create();

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.UserIdToCancel)))
			.ReturnsAsync(canceledUser);
		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.InitiatorUserId)))
			.ReturnsAsync(initiator);

		var response = await _handler.HandleAsync(command);
		Assert.False(response);
	}

	[Fact]
	public async Task Should_Exception_User_Does_Not_Exist()
	{
		var command = Fixture.Create<CancelFriendRequest>();

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.UserIdToCancel)))
			.ReturnsAsync((User)null!);

		await Assert.ThrowsAsync<ResourceNotFoundException>(() => _handler.HandleAsync(command));
	}
}