using LiteWeightAPI.Commands.Workouts;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Domain.Workouts;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;

namespace LiteWeightApiTests.Commands.Workouts;

public class GetWorkoutTests : BaseTest
{
	private readonly GetWorkoutHandler _handler;
	private readonly Mock<IRepository> _mockRepository;

	public GetWorkoutTests()
	{
		_mockRepository = new Mock<IRepository>();
		_handler = new GetWorkoutHandler(_mockRepository.Object);
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
			.Setup(x => x.GetWorkout(It.Is<string>(y => y == command.WorkoutId)))
			.ReturnsAsync(workout);

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.UserId)))
			.ReturnsAsync(user);

		var response = await _handler.HandleAsync(command);
		Assert.Equal(command.WorkoutId, response.Id);
	}

	[Fact]
	public async Task Should_Throw_Exception_Missing_Permissions_Workout()
	{
		var command = Fixture.Create<GetWorkout>();
		var user = Fixture.Create<User>();

		_mockRepository
			.Setup(x => x.GetWorkout(It.Is<string>(y => y == command.WorkoutId)))
			.ReturnsAsync(Fixture.Create<Workout>());

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.UserId)))
			.ReturnsAsync(user);

		await Assert.ThrowsAsync<ForbiddenException>(() => _handler.HandleAsync(command));
	}

	[Fact]
	public async Task Should_Throw_Exception_Workout_Not_Found()
	{
		var command = Fixture.Create<GetWorkout>();

		_mockRepository
			.Setup(x => x.GetWorkout(It.Is<string>(y => y == command.WorkoutId)))
			.ReturnsAsync((Workout?)null);

		await Assert.ThrowsAsync<ResourceNotFoundException>(() => _handler.HandleAsync(command));
	}
}