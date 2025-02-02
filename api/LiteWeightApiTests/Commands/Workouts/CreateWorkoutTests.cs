using LiteWeightAPI.Commands.Workouts;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Errors.Exceptions;
using LiteWeightAPI.Imports;
using NodaTime;

namespace LiteWeightApiTests.Commands.Workouts;

public class CreateWorkoutTests : BaseTest
{
	private readonly CreateWorkoutHandler _handler;
	private readonly Mock<IRepository> _mockRepository;

	public CreateWorkoutTests()
	{
		_mockRepository = new Mock<IRepository>();
		var clock = new Mock<IClock>();
		_handler = new CreateWorkoutHandler(_mockRepository.Object, clock.Object, Mapper);
	}

	[Theory]
	[InlineData(true)]
	[InlineData(false)]
	public async Task Should_Create(bool setAsCurrentWorkout)
	{
		var command = Fixture.Build<CreateWorkout>()
			.With(x => x.SetAsCurrentWorkout, setAsCurrentWorkout)
			.Create();
		var workouts = Enumerable.Range(0, Globals.MaxFreeWorkouts / 2)
			.Select(_ => Fixture.Create<WorkoutInfo>())
			.ToList();
		var exercisesOfWorkout = command.Routine.Weeks
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
		if (setAsCurrentWorkout)
		{
			Assert.Equal(user.CurrentWorkoutId, response.Workout.Id);
		}
	}

	[Fact]
	public async Task Should_Throw_Exception_Workout_Name_Duplicate()
	{
		var command = Fixture.Create<CreateWorkout>();
		var workouts = Enumerable.Range(0, Globals.MaxWorkouts / 2)
			.Select(_ => Fixture.Build<WorkoutInfo>().With(y => y.WorkoutName, command.Name).Create())
			.ToList();
		var user = Fixture.Build<User>()
			.With(x => x.Id, command.UserId)
			.With(x => x.Workouts, workouts)
			.Create();

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.UserId)))
			.ReturnsAsync(user);

		await Assert.ThrowsAsync<AlreadyExistsException>(() => _handler.HandleAsync(command));
	}

	[Fact]
	public async Task Should_Throw_Exception_Max_Workouts()
	{
		var command = Fixture.Create<CreateWorkout>();
		var workouts = Enumerable.Range(0, Globals.MaxWorkouts + 1)
			.Select(_ => Fixture.Build<WorkoutInfo>().Create())
			.ToList();
		var user = Fixture.Build<User>()
			.With(x => x.Id, command.UserId)
			.With(x => x.Workouts, workouts)
			.Create();

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.UserId)))
			.ReturnsAsync(user);

		await Assert.ThrowsAsync<MaxLimitException>(() => _handler.HandleAsync(command));
	}

	[Fact]
	public async Task Should_Throw_Exception_Max_Free_Workouts()
	{
		var command = Fixture.Create<CreateWorkout>();
		var workouts = Enumerable.Range(0, Globals.MaxFreeWorkouts + 1)
			.Select(_ => Fixture.Build<WorkoutInfo>().Create())
			.ToList();
		var user = Fixture.Build<User>()
			.With(x => x.Id, command.UserId)
			.With(x => x.Workouts, workouts)
			.With(x => x.PremiumToken, (string)null)
			.Create();

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.UserId)))
			.ReturnsAsync(user);

		await Assert.ThrowsAsync<MaxLimitException>(() => _handler.HandleAsync(command));
	}
}