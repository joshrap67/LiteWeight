using LiteWeightAPI.Commands.Self;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.Users;

namespace LiteWeightApiTests.Commands.Self;

public class SetReceivedWorkoutSeenTests : BaseTest
{
	private readonly SetReceivedWorkoutSeenHandler _handler;
	private readonly IRepository _mockRepository;

	public SetReceivedWorkoutSeenTests()
	{
		_mockRepository = Substitute.For<IRepository>();
		_handler = new SetReceivedWorkoutSeenHandler(_mockRepository);
	}

	[Theory]
	[InlineData(false)]
	[InlineData(true)]
	public async Task Should_Set_Workout_Seen(bool isAlreadySeen)
	{
		var command = Fixture.Create<SetReceivedWorkoutSeen>();

		var user = Fixture.Build<User>()
			.With(x => x.Id, command.UserId)
			.Create();
		var receivedWorkoutInfo = Fixture.Build<ReceivedWorkoutInfo>()
			.With(x => x.ReceivedWorkoutId, command.ReceivedWorkoutId)
			.With(x => x.Seen, isAlreadySeen)
			.Create();
		user.ReceivedWorkouts.Add(receivedWorkoutInfo);

		_mockRepository
			.GetUser(Arg.Is<string>(y => y == command.UserId))
			.Returns(user);

		await _handler.HandleAsync(command);
		Assert.True(receivedWorkoutInfo.Seen);
	}

	[Fact]
	public async Task Should_Not_Fail_Workout_Not_Found()
	{
		var command = Fixture.Create<SetReceivedWorkoutSeen>();

		var user = Fixture.Build<User>()
			.With(x => x.Id, command.UserId)
			.Create();

		_mockRepository
			.GetUser(Arg.Is<string>(y => y == command.UserId))
			.Returns(user);

		var response = await _handler.HandleAsync(command);
		Assert.False(response);
	}
}