using LiteWeightAPI.Commands.Workouts;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Domain.Workouts;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;

namespace LiteWeightApiTests.Commands.Workouts;

public class GetWorkoutTests : BaseTest
{
	private readonly GetWorkoutHandler _handler;
	private readonly IRepository _mockRepository;

	public GetWorkoutTests()
	{
		_mockRepository = Substitute.For<IRepository>();
		_handler = new GetWorkoutHandler(_mockRepository);
	}

	[Fact]
	public async Task Should_Get()
	{
		var command = Fixture.Create<GetWorkout>();
		var workout = Fixture.Build<Workout>()
			.With(x => x.CreatorId, command.UserId)
			.With(x => x.Id, command.WorkoutId)
			.Create();

		var user = Fixture.Build<User>()
			.With(x => x.Id, command.UserId)
			.Create();

		_mockRepository
			.GetWorkout(Arg.Is<string>(y => y == command.WorkoutId))
			.Returns(workout);

		_mockRepository
			.GetUser(Arg.Is<string>(y => y == command.UserId))
			.Returns(user);

		var response = await _handler.HandleAsync(command);
		Assert.Equal(command.WorkoutId, response.Id);
	}

	[Fact]
	public async Task Should_Throw_Exception_Missing_Permissions_Workout()
	{
		var command = Fixture.Create<GetWorkout>();
		var user = Fixture.Create<User>();

		_mockRepository
			.GetWorkout(Arg.Is<string>(y => y == command.WorkoutId))
			.Returns(Fixture.Create<Workout>());

		_mockRepository
			.GetUser(Arg.Is<string>(y => y == command.UserId))
			.Returns(user);

		await Assert.ThrowsAsync<ForbiddenException>(() => _handler.HandleAsync(command));
	}

	[Fact]
	public async Task Should_Throw_Exception_Workout_Not_Found()
	{
		var command = Fixture.Create<GetWorkout>();

		_mockRepository
			.GetWorkout(Arg.Is<string>(y => y == command.WorkoutId))
			.Returns((Workout?)null);

		await Assert.ThrowsAsync<ResourceNotFoundException>(() => _handler.HandleAsync(command));
	}
}