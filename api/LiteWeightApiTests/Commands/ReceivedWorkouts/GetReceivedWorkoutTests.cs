using LiteWeightAPI.Commands.ReceivedWorkouts;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.ReceivedWorkouts;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;
using LiteWeightApiTests.TestHelpers;

namespace LiteWeightApiTests.Commands.ReceivedWorkouts;

public class GetReceivedWorkoutTests : BaseTest
{
	private readonly GetReceivedWorkoutHandler _handler;
	private readonly IRepository _mockRepository;

	public GetReceivedWorkoutTests()
	{
		_mockRepository = Substitute.For<IRepository>();
		_handler = new GetReceivedWorkoutHandler(_mockRepository);
	}

	[Fact]
	public async Task Should_Get_Workout()
	{
		var command = Fixture.Create<GetReceivedWorkout>();
		var receivedWorkout = ReceivedWorkoutHelper.GetReceivedWorkout(command.UserId, command.ReceivedWorkoutId);

		_mockRepository
			.GetReceivedWorkout(Arg.Is<string>(y => y == command.ReceivedWorkoutId))
			.Returns(receivedWorkout);

		_mockRepository
			.GetUser(Arg.Is<string>(y => y == command.UserId))
			.Returns(Fixture.Create<User>());

		var response = await _handler.HandleAsync(command);
		Assert.Equal(command.ReceivedWorkoutId, response.Id);
	}

	[Fact]
	public async Task Should_Throw_Exception_Workout_Does_Not_Exist()
	{
		var command = Fixture.Create<GetReceivedWorkout>();

		_mockRepository
			.GetReceivedWorkout(Arg.Is<string>(y => y == command.ReceivedWorkoutId))
			.Returns((ReceivedWorkout?)null);

		await Assert.ThrowsAsync<ResourceNotFoundException>(() => _handler.HandleAsync(command));
	}

	[Fact]
	public async Task Should_Throw_Exception_Missing_Permissions_Workout()
	{
		var command = Fixture.Create<GetReceivedWorkout>();
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