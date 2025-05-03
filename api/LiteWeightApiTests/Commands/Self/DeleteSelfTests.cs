using LiteWeightAPI.Commands.Self;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;
using LiteWeightAPI.Services;

namespace LiteWeightApiTests.Commands.Self;

public class DeleteSelfTests : BaseTest
{
	private readonly DeleteSelfHandler _handler;
	private readonly IRepository _mockRepository;

	public DeleteSelfTests()
	{
		_mockRepository = Substitute.For<IRepository>();
		var storageService = Substitute.For<IStorageService>();
		var firebaseAuthService = Substitute.For<IFirebaseAuthService>();
		var pushNotificationService = Substitute.For<IPushNotificationService>();
		_handler = new DeleteSelfHandler(_mockRepository, storageService, firebaseAuthService,
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
			.GetUser(Arg.Is<string>(y => y == userWhoSentFriendRequestId))
			.Returns(userWhoSentFriendRequest);

		_mockRepository
			.GetUser(Arg.Is<string>(y => y == friendOfUserId))
			.Returns(friendOfUser);

		_mockRepository
			.GetUser(Arg.Is<string>(y => y == userWhoReceivedFriendRequestId))
			.Returns(userWhoReceivedFriendRequest);

		_mockRepository
			.GetUser(Arg.Is<string>(y => y == command.UserId))
			.Returns(user);

		await _handler.HandleAsync(command);

		Assert.True(friendOfUser.Friends.All(x => x.UserId != userId));
		Assert.True(userWhoSentFriendRequest.Friends.All(x => x.UserId != userId));
		Assert.True(userWhoReceivedFriendRequest.FriendRequests.All(x => x.UserId != userId));
		await _mockRepository.Received(user.ReceivedWorkouts.Count).DeleteReceivedWorkout(Arg.Any<string>());
		await _mockRepository.Received(user.Workouts.Count).DeleteWorkout(Arg.Any<string>());
	}

	[Fact]
	public async Task Should_Throw_Exception_User_Does_Not_Exist()
	{
		var command = Fixture.Create<DeleteSelf>();

		_mockRepository
			.GetUser(Arg.Is<string>(y => y == command.UserId))
			.Returns((User)null!);

		await Assert.ThrowsAsync<ResourceNotFoundException>(() => _handler.HandleAsync(command));
	}
}