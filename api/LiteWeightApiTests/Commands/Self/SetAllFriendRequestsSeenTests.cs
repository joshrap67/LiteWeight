using LiteWeightAPI.Commands.Self;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.Users;

namespace LiteWeightApiTests.Commands.Self;

public class SetAllFriendRequestsSeenTests : BaseTest
{
	private readonly SetAllFriendRequestsSeenHandler _handler;
	private readonly Mock<IRepository> _mockRepository;

	public SetAllFriendRequestsSeenTests()
	{
		_mockRepository = new Mock<IRepository>();
		_handler = new SetAllFriendRequestsSeenHandler(_mockRepository.Object);
	}

	[Fact]
	public async Task Should_Set_Requests_Seen()
	{
		var command = Fixture.Create<SetAllFriendRequestsSeen>();
		var user = Fixture.Build<User>()
			.With(x => x.FriendRequests,
			[
				Fixture.Build<FriendRequest>().With(x => x.Seen, false).Create(),
				Fixture.Build<FriendRequest>().With(x => x.Seen, false).Create(),
				Fixture.Build<FriendRequest>().With(x => x.Seen, false).Create(),
				Fixture.Build<FriendRequest>().With(x => x.Seen, true).Create()
			])
			.Create();

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.UserId)))
			.ReturnsAsync(user);

		await _handler.HandleAsync(command);
		Assert.True(user.FriendRequests.All(x => x.Seen));
	}

	[Fact]
	public async Task Should_Set_Requests_Seen_Empty_List()
	{
		var command = Fixture.Create<SetAllFriendRequestsSeen>();
		var user = Fixture.Build<User>()
			.With(x => x.FriendRequests, [])
			.Create();

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.UserId)))
			.ReturnsAsync(user);

		await _handler.HandleAsync(command);
		Assert.True(user.FriendRequests.All(x => x.Seen));
	}
}