using LiteWeightAPI.Commands.Workouts;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Domain.Workouts;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;

namespace LiteWeightApiTests.Commands.Workouts;

public class UpdateWorkoutProgressTests : BaseTest
{
	private readonly UpdateWorkoutProgressHandler _handler;
	private readonly IRepository _mockRepository;

	public UpdateWorkoutProgressTests()
	{
		_mockRepository = Substitute.For<IRepository>();
		_handler = new UpdateWorkoutProgressHandler(_mockRepository);
	}

	[Fact]
	public async Task Should_Update_Progress()
	{
		var command = Fixture.Build<UpdateWorkoutProgress>()
			.With(x => x.CurrentWeek, 0)
			.With(x => x.CurrentDay, 0)
			.Create();

		var workoutInfo = Fixture.Build<WorkoutInfo>().With(x => x.WorkoutId, command.WorkoutId).Create();
		var workouts = new List<WorkoutInfo> { workoutInfo };
		var workout = Fixture.Build<Workout>()
			.With(x => x.CreatorId, command.UserId)
			.Create();

		var user = Fixture.Build<User>()
			.With(x => x.Id, command.UserId)
			.With(x => x.Workouts, workouts)
			.Create();

		_mockRepository
			.GetWorkout(Arg.Is<string>(y => y == command.WorkoutId))
			.Returns(workout);

		_mockRepository
			.GetUser(Arg.Is<string>(y => y == command.UserId))
			.Returns(user);

		await _handler.HandleAsync(command);
		Assert.Equal(command.CurrentWeek, workoutInfo.CurrentWeek);
		Assert.Equal(command.CurrentDay, workoutInfo.CurrentDay);
		Assert.Equal(command.Routine.Weeks.SelectMany(x => x.Days).SelectMany(x => x.Exercises).First().ExerciseId,
			workout.Routine.Weeks.SelectMany(x => x.Days).SelectMany(x => x.Exercises).First().ExerciseId);
	}

	[Fact]
	public async Task Should_Throw_Exception_Missing_Permissions_Workout()
	{
		var command = Fixture.Create<UpdateWorkoutProgress>();
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
		var command = Fixture.Create<UpdateWorkoutProgress>();

		_mockRepository
			.GetWorkout(Arg.Is<string>(y => y == command.WorkoutId))
			.Returns((Workout?)null);

		await Assert.ThrowsAsync<ResourceNotFoundException>(() => _handler.HandleAsync(command));
	}
}