using LiteWeightAPI.Commands.Users;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Errors.Exceptions;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;
using LiteWeightAPI.Imports;
using LiteWeightAPI.Services;
using LiteWeightApiTests.TestHelpers;
using NodaTime;

namespace LiteWeightApiTests.Commands.Users;

public class SendFriendRequestTests : BaseTest
{
	private readonly SendFriendRequestHandler _handler;
	private readonly Mock<IRepository> _mockRepository;
	private readonly Mock<IPushNotificationService> _mockPushNotificationService;

	public SendFriendRequestTests()
	{
		_mockRepository = new Mock<IRepository>();
		_mockPushNotificationService = new Mock<IPushNotificationService>();
		var clock = new Mock<IClock>().Object;
		_handler = new SendFriendRequestHandler(_mockRepository.Object, DependencyHelper.GetMapper(), clock,
			_mockPushNotificationService.Object);
	}

	[Fact]
	public async Task Should_Send_Friend_Request()
	{
		var command = Fixture.Create<SendFriendRequest>();

		var preferences = new UserSettings { PrivateAccount = false };
		var recipientUser = Fixture.Build<User>()
			.With(x => x.Id, command.RecipientId)
			.With(x => x.Settings, preferences)
			.Create();

		var senderUser = Fixture.Build<User>()
			.With(x => x.Id, command.SenderId)
			.Create();

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.RecipientId)))
			.ReturnsAsync(recipientUser);

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.SenderId)))
			.ReturnsAsync(senderUser);

		var response = await _handler.HandleAsync(command);
		_mockPushNotificationService.Verify(
			x => x.SendNewFriendRequestNotification(It.IsAny<User>(), It.IsAny<FriendRequest>()), Times.Once);
		Assert.Equal(command.RecipientId, response.UserId);
		Assert.Contains(recipientUser.FriendRequests, x => x.UserId == command.SenderId);
		Assert.Contains(senderUser.Friends, x => x.UserId == command.RecipientId && !x.Confirmed);
	}

	[Fact]
	public async Task Should_Fail_Gracefully_Already_Sent()
	{
		var command = Fixture.Create<SendFriendRequest>();

		var preferences = new UserSettings { PrivateAccount = false };
		var recipientUser = Fixture.Build<User>()
			.With(x => x.Id, command.RecipientId)
			.With(x => x.Settings, preferences)
			.Create();

		var senderUser = Fixture.Build<User>()
			.With(x => x.Id, command.SenderId)
			.With(x => x.Friends, [Fixture.Build<Friend>().With(x => x.UserId, command.RecipientId).Create()])
			.Create();

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.RecipientId)))
			.ReturnsAsync(recipientUser);

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.SenderId)))
			.ReturnsAsync(senderUser);

		var response = await _handler.HandleAsync(command);
		_mockPushNotificationService.Verify(
			x => x.SendNewFriendRequestNotification(It.IsAny<User>(), It.IsAny<FriendRequest>()), Times.Never);
		Assert.Equal(command.RecipientId, response.UserId);
	}

	[Fact]
	public async Task Should_Throw_Exception_Sending_Request_To_Self()
	{
		var command = Fixture.Build<SendFriendRequest>()
			.With(x => x.SenderId, "Mantis Toboggan")
			.With(x => x.RecipientId, "Mantis Toboggan")
			.Create();
		await Assert.ThrowsAsync<MiscErrorException>(() => _handler.HandleAsync(command));
	}

	[Fact]
	public async Task Should_Throw_Exception_Friend_Request_Already_Received()
	{
		var command = Fixture.Create<SendFriendRequest>();

		var preferences = new UserSettings { PrivateAccount = false };
		var recipientUser = Fixture.Build<User>()
			.With(x => x.Id, command.RecipientId)
			.With(x => x.Settings, preferences)
			.Create();

		var friendsRequests = new List<FriendRequest>
		{
			Fixture.Build<FriendRequest>().With(x => x.UserId, command.RecipientId).Create()
		};
		var senderUser = Fixture.Build<User>()
			.With(x => x.Id, command.SenderId)
			.With(x => x.FriendRequests, friendsRequests)
			.Create();

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.RecipientId)))
			.ReturnsAsync(recipientUser);

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.SenderId)))
			.ReturnsAsync(senderUser);

		await Assert.ThrowsAsync<MiscErrorException>(() => _handler.HandleAsync(command));
	}

	[Fact]
	public async Task Should_Throw_Exception_Max_Limit_Friend_Requests()
	{
		var command = Fixture.Create<SendFriendRequest>();

		var preferences = new UserSettings { PrivateAccount = false };
		var friendRequests = Enumerable.Range(0, Globals.MaxFriendRequests + 1)
			.Select(_ => Fixture.Build<FriendRequest>().Create())
			.ToList();
		var recipientUser = Fixture.Build<User>()
			.With(x => x.Id, command.RecipientId)
			.With(x => x.Settings, preferences)
			.With(x => x.FriendRequests, friendRequests)
			.Create();

		var senderUser = Fixture.Build<User>()
			.With(x => x.Id, command.SenderId)
			.Create();

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.RecipientId)))
			.ReturnsAsync(recipientUser);

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.SenderId)))
			.ReturnsAsync(senderUser);

		await Assert.ThrowsAsync<MaxLimitException>(() => _handler.HandleAsync(command));
	}

	[Fact]
	public async Task Should_Throw_Exception_Max_Limit_Friends()
	{
		var command = Fixture.Create<SendFriendRequest>();

		var preferences = new UserSettings { PrivateAccount = false };
		var recipientUser = Fixture.Build<User>()
			.With(x => x.Id, command.RecipientId)
			.With(x => x.Settings, preferences)
			.With(x => x.Friends, [Fixture.Build<Friend>().With(x => x.UserId, command.SenderId).Create()])
			.Create();

		var friends = Enumerable.Range(0, Globals.MaxNumberFriends + 1)
			.Select(_ => Fixture.Build<Friend>().Create())
			.ToList();
		var senderUser = Fixture.Build<User>()
			.With(x => x.Id, command.SenderId)
			.With(x => x.Friends, friends)
			.Create();

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.RecipientId)))
			.ReturnsAsync(recipientUser);

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.SenderId)))
			.ReturnsAsync(senderUser);

		await Assert.ThrowsAsync<MaxLimitException>(() => _handler.HandleAsync(command));
	}

	[Fact]
	public async Task Should_Throw_Exception_Private_User()
	{
		var command = Fixture.Create<SendFriendRequest>();

		var preferences = new UserSettings { PrivateAccount = true };
		var recipientUser = Fixture.Build<User>()
			.With(x => x.Id, command.RecipientId)
			.With(x => x.Settings, preferences)
			.Create();

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.RecipientId)))
			.ReturnsAsync(recipientUser);

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.SenderId)))
			.ReturnsAsync(Fixture.Create<User>());

		await Assert.ThrowsAsync<ResourceNotFoundException>(() => _handler.HandleAsync(command));
	}

	[Fact]
	public async Task Should_Throw_Exception_User_Does_Not_Exist()
	{
		var command = Fixture.Create<SendFriendRequest>();

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.RecipientId)))
			.ReturnsAsync((User)null!);

		await Assert.ThrowsAsync<ResourceNotFoundException>(() => _handler.HandleAsync(command));
	}
}