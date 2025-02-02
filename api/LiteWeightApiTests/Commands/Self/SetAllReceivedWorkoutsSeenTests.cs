using LiteWeightAPI.Commands.Self;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.Users;

namespace LiteWeightApiTests.Commands.Self;

public class SetAllReceivedWorkoutsSeenTests : BaseTest
{
	private readonly SetAllReceivedWorkoutsSeenHandler _handler;
	private readonly Mock<IRepository> _mockRepository;

	public SetAllReceivedWorkoutsSeenTests()
	{
		_mockRepository = new Mock<IRepository>();
		_handler = new SetAllReceivedWorkoutsSeenHandler(_mockRepository.Object);
	}

	[Fact]
	public async Task Should_Set_Workouts_Seen()
	{
		var command = Fixture.Create<SetAllReceivedWorkoutsSeen>();
		var user = Fixture.Build<User>()
			.With(x => x.ReceivedWorkouts,
			[
				Fixture.Build<ReceivedWorkoutInfo>().With(x => x.Seen, false).Create(),
				Fixture.Build<ReceivedWorkoutInfo>().With(x => x.Seen, false).Create(),
				Fixture.Build<ReceivedWorkoutInfo>().With(x => x.Seen, false).Create(),
				Fixture.Build<ReceivedWorkoutInfo>().With(x => x.Seen, true).Create()
			])
			.Create();

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.UserId)))
			.ReturnsAsync(user);

		await _handler.HandleAsync(command);
		Assert.True(user.ReceivedWorkouts.All(x => x.Seen));
	}

	[Fact]
	public async Task Should_Set_Workouts_Seen_Empty_List()
	{
		var command = Fixture.Create<SetAllReceivedWorkoutsSeen>();
		var user = Fixture.Build<User>()
			.With(x => x.ReceivedWorkouts, [])
			.Create();

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.UserId)))
			.ReturnsAsync(user);

		await _handler.HandleAsync(command);
		Assert.True(user.ReceivedWorkouts.All(x => x.Seen));
	}
}