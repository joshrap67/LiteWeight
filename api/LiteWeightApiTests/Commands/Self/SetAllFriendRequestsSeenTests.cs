using LiteWeightAPI.Commands.Self;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.Users;

namespace LiteWeightApiTests.Commands.Self;

public class SetAllFriendRequestsSeenTests : BaseTest
{
	private readonly SetAllFriendRequestsSeenHandler _handler;
	private readonly IRepository _mockRepository;

	public SetAllFriendRequestsSeenTests()
	{
		_mockRepository = Substitute.For<IRepository>();
		_handler = new SetAllFriendRequestsSeenHandler(_mockRepository);
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
			.GetUser(Arg.Is<string>(y => y == command.UserId))
			.Returns(user);

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
			.GetUser(Arg.Is<string>(y => y == command.UserId))
			.Returns(user);

		await _handler.HandleAsync(command);
		Assert.True(user.FriendRequests.All(x => x.Seen));
	}
}