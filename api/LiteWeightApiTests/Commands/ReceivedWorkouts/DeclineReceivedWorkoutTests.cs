using LiteWeightAPI.Commands.ReceivedWorkouts;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.ReceivedWorkouts;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;
using LiteWeightApiTests.TestHelpers;

namespace LiteWeightApiTests.Commands.ReceivedWorkouts;

public class DeclineReceivedWorkoutTests : BaseTest
{
	private readonly DeclineReceivedWorkoutHandler _handler;
	private readonly IRepository _mockRepository;

	public DeclineReceivedWorkoutTests()
	{
		_mockRepository = Substitute.For<IRepository>();
		_handler = new DeclineReceivedWorkoutHandler(_mockRepository);
	}

	[Fact]
	public async Task Should_Decline_Workout()
	{
		var command = Fixture.Create<DeclineReceivedWorkout>();
		var receivedWorkout = ReceivedWorkoutHelper.GetReceivedWorkout(command.UserId);
		var user = Fixture.Build<User>()
			.With(x => x.Id, command.UserId)
			.With(x => x.ReceivedWorkouts,
			[
				Fixture.Build<ReceivedWorkoutInfo>().With(x => x.ReceivedWorkoutId, command.ReceivedWorkoutId).Create()
			])
			.Create();

		_mockRepository
			.GetReceivedWorkout(Arg.Is<string>(y => y == command.ReceivedWorkoutId))
			.Returns(receivedWorkout);

		_mockRepository
			.GetUser(Arg.Is<string>(y => y == command.UserId))
			.Returns(user);

		await _handler.HandleAsync(command);
		Assert.True(user.ReceivedWorkouts.All(x => x.ReceivedWorkoutId != command.ReceivedWorkoutId));
	}

	[Fact]
	public async Task Should_Throw_Exception_Workout_Does_Not_Exist()
	{
		var command = Fixture.Create<DeclineReceivedWorkout>();

		_mockRepository
			.GetReceivedWorkout(Arg.Is<string>(y => y == command.ReceivedWorkoutId))
			.Returns((ReceivedWorkout?)null);

		await Assert.ThrowsAsync<ResourceNotFoundException>(() => _handler.HandleAsync(command));
	}

	[Fact]
	public async Task Should_Throw_Exception_Missing_Permissions_Workout()
	{
		var command = Fixture.Create<DeclineReceivedWorkout>();
		var user = Fixture.Create<User>();

		var receivedWorkout = ReceivedWorkoutHelper.GetReceivedWorkout();

		_mockRepository
			.GetReceivedWorkout(Arg.Is<string>(y => y == command.ReceivedWorkoutId))
			.Returns(receivedWorkout);

		_mockRepository
			.GetUser(Arg.Is<string>(y => y == command.UserId))
			.Returns(user);

		await Assert.ThrowsAsync<ForbiddenException>(() => _handler.HandleAsync(command));
	}
}