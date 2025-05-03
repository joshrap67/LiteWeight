using LiteWeightAPI.Commands.Workouts;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Domain.Workouts;
using LiteWeightAPI.Errors.Exceptions;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;
using LiteWeightAPI.Imports;
using NodaTime;

namespace LiteWeightApiTests.Commands.Workouts;

public class CopyWorkoutTests : BaseTest
{
	private readonly CopyWorkoutHandler _handler;
	private readonly Mock<IRepository> _mockRepository;

	public CopyWorkoutTests()
	{
		_mockRepository = new Mock<IRepository>();
		var clock = new Mock<IClock>();
		_handler = new CopyWorkoutHandler(_mockRepository.Object, clock.Object);
	}

	[Fact]
	public async Task Should_Copy()
	{
		var command = Fixture.Create<CopyWorkout>();
		var workouts = Enumerable.Range(0, Globals.MaxFreeWorkouts / 2)
			.Select(_ => Fixture.Create<WorkoutInfo>())
			.ToList();
		var workout = Fixture.Build<Workout>()
			.With(x => x.CreatorId, command.UserId)
			.Create();
		var exercisesOfWorkout = workout.Routine.Weeks
			.SelectMany(x => x.Days)
			.SelectMany(x => x.Exercises)
			.Select(x => x.ExerciseId)
			.ToList();
		var ownedExercises = Enumerable.Range(0, 10)
			.Select(_ => Fixture.Create<OwnedExercise>())
			.ToList();
		ownedExercises.AddRange(exercisesOfWorkout.Select(exerciseId =>
			Fixture.Build<OwnedExercise>().With(x => x.Id, exerciseId).Create()));

		var user = Fixture.Build<User>()
			.With(x => x.Id, command.UserId)
			.With(x => x.Workouts, workouts)
			.With(x => x.Exercises, ownedExercises)
			.Create();

		_mockRepository
			.Setup(x => x.GetWorkout(It.Is<string>(y => y == command.WorkoutId)))
			.ReturnsAsync(workout);

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.UserId)))
			.ReturnsAsync(user);

		var response = await _handler.HandleAsync(command);
		// all exercises of workout should have this workout after the copy
		Assert.True(user.Exercises.Where(x => exercisesOfWorkout.Contains(x.Id))
			.All(x => x.Workouts.Any(y => y.WorkoutId == response.Workout.Id)));
		// exercises not apart of the workout should not have changed
		Assert.True(user.Exercises.Where(x => !exercisesOfWorkout.Contains(x.Id))
			.All(x => x.Workouts.All(y => y.WorkoutId != response.Workout.Id)));
		Assert.Contains(user.Workouts, x => x.WorkoutId == response.Workout.Id);
	}

	[Fact]
	public async Task Should_Throw_Exception_Workout_Name_Duplicate()
	{
		var command = Fixture.Create<CopyWorkout>();
		var workouts = Enumerable.Range(0, Globals.MaxWorkouts / 2)
			.Select(_ => Fixture.Build<WorkoutInfo>().With(y => y.WorkoutName, command.Name).Create())
			.ToList();
		var user = Fixture.Build<User>()
			.With(x => x.Id, command.UserId)
			.With(x => x.Workouts, workouts)
			.Create();

		var workout = Fixture.Build<Workout>()
			.With(x => x.CreatorId, user.Id)
			.Create();

		_mockRepository
			.Setup(x => x.GetWorkout(It.Is<string>(y => y == command.WorkoutId)))
			.ReturnsAsync(workout);

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.UserId)))
			.ReturnsAsync(user);

		await Assert.ThrowsAsync<AlreadyExistsException>(() => _handler.HandleAsync(command));
	}

	[Fact]
	public async Task Should_Throw_Exception_Max_Workouts()
	{
		var command = Fixture.Create<CopyWorkout>();
		var workouts = Enumerable.Range(0, Globals.MaxWorkouts)
			.Select(_ => Fixture.Build<WorkoutInfo>().Create())
			.ToList();
		var user = Fixture.Build<User>()
			.With(x => x.Id, command.UserId)
			.With(x => x.Workouts, workouts)
			.Create();
		var workout = Fixture.Build<Workout>()
			.With(x => x.CreatorId, user.Id)
			.Create();

		_mockRepository
			.Setup(x => x.GetWorkout(It.Is<string>(y => y == command.WorkoutId)))
			.ReturnsAsync(workout);

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.UserId)))
			.ReturnsAsync(user);

		await Assert.ThrowsAsync<MaxLimitException>(() => _handler.HandleAsync(command));
	}

	[Fact]
	public async Task Should_Throw_Exception_Max_Free_Workouts()
	{
		var command = Fixture.Create<CopyWorkout>();
		var workouts = Enumerable.Range(0, Globals.MaxFreeWorkouts)
			.Select(_ => Fixture.Build<WorkoutInfo>().Create())
			.ToList();
		var user = Fixture.Build<User>()
			.With(x => x.Id, command.UserId)
			.With(x => x.Workouts, workouts)
			.With(x => x.PremiumToken, (string?)null)
			.Create();
		var workout = Fixture.Build<Workout>()
			.With(x => x.CreatorId, user.Id)
			.Create();

		_mockRepository
			.Setup(x => x.GetWorkout(It.Is<string>(y => y == command.WorkoutId)))
			.ReturnsAsync(workout);

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.UserId)))
			.ReturnsAsync(user);

		await Assert.ThrowsAsync<MaxLimitException>(() => _handler.HandleAsync(command));
	}

	[Fact]
	public async Task Should_Throw_Exception_Missing_Permissions_Workout()
	{
		var command = Fixture.Create<CopyWorkout>();
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
		var command = Fixture.Create<CopyWorkout>();

		_mockRepository
			.Setup(x => x.GetWorkout(It.Is<string>(y => y == command.WorkoutId)))
			.ReturnsAsync((Workout?)null);

		await Assert.ThrowsAsync<ResourceNotFoundException>(() => _handler.HandleAsync(command));
	}
}