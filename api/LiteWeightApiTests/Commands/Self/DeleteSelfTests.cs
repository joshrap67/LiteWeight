using LiteWeightAPI.Commands.Self;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;
using LiteWeightAPI.Services;

namespace LiteWeightApiTests.Commands.Self;

public class DeleteSelfTests : BaseTest
{
	private readonly DeleteSelfHandler _handler;
	private readonly Mock<IRepository> _mockRepository;

	public DeleteSelfTests()
	{
		_mockRepository = new Mock<IRepository>();
		var storageService = new Mock<IStorageService>().Object;
		var firebaseAuthService = new Mock<IFirebaseAuthService>().Object;
		var pushNotificationService = new Mock<IPushNotificationService>().Object;
		_handler = new DeleteSelfHandler(_mockRepository.Object, storageService, firebaseAuthService,
			pushNotificationService);
	}

	[Fact]
	public async Task Should_Delete_Self()
	{
		var command = Fixture.Create<DeleteSelf>();
		var friendOfUserId = Fixture.Create<string>();
		var userWhoSentFriendRequestId = Fixture.Create<string>();
		var userWhoReceivedFriendRequestId = Fixture.Create<string>();

		var user = Fixture.Build<User>()
			.With(x => x.Friends, [
				Fixture.Build<Friend>()
					.With(x => x.Confirmed, true)
					.With(x => x.UserId, friendOfUserId).Create(),

				Fixture.Build<Friend>()
					.With(x => x.Confirmed, false)
					.With(x => x.UserId, userWhoReceivedFriendRequestId).Create()
			])
			.With(x => x.FriendRequests,
			[
				Fixture.Build<FriendRequest>()
					.With(x => x.UserId, userWhoSentFriendRequestId).Create()
			])
			.With(x => x.Id, command.UserId)
			.Create();
		var userId = user.Id;

		var friendOfUser = Fixture.Build<User>().With(x => x.Friends,
			[Fixture.Build<Friend>().With(y => y.UserId, userId).Create()]).Create();

		var userWhoSentFriendRequest = Fixture.Build<User>().With(x => x.Friends,
			[Fixture.Build<Friend>().With(y => y.UserId, userId).Create()]).Create();

		var userWhoReceivedFriendRequest = Fixture.Build<User>().With(x => x.FriendRequests,
			[Fixture.Build<FriendRequest>().With(y => y.UserId, userId).Create()]).Create();


		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == userWhoSentFriendRequestId)))
			.ReturnsAsync(userWhoSentFriendRequest);

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == friendOfUserId)))
			.ReturnsAsync(friendOfUser);

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == userWhoReceivedFriendRequestId)))
			.ReturnsAsync(userWhoReceivedFriendRequest);

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.UserId)))
			.ReturnsAsync(user);

		await _handler.HandleAsync(command);

		Assert.True(friendOfUser.Friends.All(x => x.UserId != userId));
		Assert.True(userWhoSentFriendRequest.Friends.All(x => x.UserId != userId));
		Assert.True(userWhoReceivedFriendRequest.FriendRequests.All(x => x.UserId != userId));
		_mockRepository.Verify(
			x => x.DeleteReceivedWorkout(It.IsAny<string>()), Times.Exactly(user.ReceivedWorkouts.Count));
		_mockRepository.Verify(
			x => x.DeleteWorkout(It.IsAny<string>()), Times.Exactly(user.Workouts.Count));
	}

	[Fact]
	public async Task Should_Throw_Exception_User_Does_Not_Exist()
	{
		var command = Fixture.Create<DeleteSelf>();

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.UserId)))
			.ReturnsAsync((User)null!);

		await Assert.ThrowsAsync<ResourceNotFoundException>(() => _handler.HandleAsync(command));
	}
}