using LiteWeightAPI.Commands.Self;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Errors.Exceptions;
using NodaTime;

namespace LiteWeightApiTests.Commands.Self;

public class SetCurrentWorkoutTests : BaseTest
{
	private readonly SetCurrentWorkoutHandler _handler;
	private readonly Mock<IRepository> _mockRepository;
	private readonly Mock<IClock> _mockClock;

	public SetCurrentWorkoutTests()
	{
		_mockRepository = new Mock<IRepository>();
		_mockClock = new Mock<IClock>();
		_handler = new SetCurrentWorkoutHandler(_mockRepository.Object, _mockClock.Object);
	}

	[Fact]
	public async Task Should_Set_Current_Workout_Not_Null()
	{
		var command = Fixture.Create<SetCurrentWorkout>();

		var workoutInfo = Fixture.Build<WorkoutInfo>().With(x => x.WorkoutId, command.CurrentWorkoutId).Create();
		var user = Fixture.Build<User>()
			.With(x => x.Id, command.UserId)
			.With(x => x.Workouts, [workoutInfo])
			.Create();
		var instant = Fixture.Create<Instant>();
		_mockClock.Setup(x => x.GetCurrentInstant()).Returns(instant);

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.UserId)))
			.ReturnsAsync(user);

		await _handler.HandleAsync(command);

		Assert.Equal(command.CurrentWorkoutId, user.CurrentWorkoutId);
		Assert.Equal(instant, workoutInfo.LastSetAsCurrentUtc);
	}

	[Fact]
	public async Task Should_Set_Current_Workout_Null()
	{
		var command = Fixture.Build<SetCurrentWorkout>().With(x => x.CurrentWorkoutId, (string)null).Create();
		var user = Fixture.Build<User>().With(x => x.Id, command.UserId).Create();

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.UserId)))
			.ReturnsAsync(user);

		await _handler.HandleAsync(command);

		Assert.Null(user.CurrentWorkoutId);
	}

	[Fact]
	public async Task Should_Throw_Exception_Workout_Does_Not_Exist()
	{
		var command = Fixture.Create<SetCurrentWorkout>();

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.UserId)))
			.ReturnsAsync(Fixture.Create<User>());

		await Assert.ThrowsAsync<WorkoutNotFoundException>(() => _handler.HandleAsync(command));
	}
}