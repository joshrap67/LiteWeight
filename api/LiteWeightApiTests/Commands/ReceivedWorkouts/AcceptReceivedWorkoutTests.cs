using LiteWeightAPI.Commands.ReceivedWorkouts;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.ReceivedWorkouts;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Errors.Exceptions;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;
using LiteWeightAPI.Imports;
using LiteWeightApiTests.TestHelpers;
using NodaTime;

namespace LiteWeightApiTests.Commands.ReceivedWorkouts;

public class AcceptReceivedWorkoutTests : BaseTest
{
	private readonly AcceptReceivedWorkoutHandler _handler;
	private readonly Mock<IRepository> _mockRepository;

	public AcceptReceivedWorkoutTests()
	{
		_mockRepository = new Mock<IRepository>();
		var clock = new Mock<IClock>();
		_handler = new AcceptReceivedWorkoutHandler(_mockRepository.Object, clock.Object);
	}

	[Fact]
	public async Task Should_Accept_Workout()
	{
		var command = Fixture.Create<AcceptReceivedWorkout>();

		var receivedWorkout = ReceivedWorkoutHelper.GetReceivedWorkout(command.UserId);

		var workouts = Enumerable.Range(0, Globals.MaxWorkouts / 2)
			.Select(_ => Fixture.Create<WorkoutInfo>())
			.ToList();

		var originalExerciseCount = (Globals.MaxExercises - receivedWorkout.DistinctExercises.Count) / 2;
		var ownedExercises = Enumerable.Range(0, originalExerciseCount)
			.Select(_ => Fixture.Create<OwnedExercise>())
			.ToList();

		var user = Fixture.Build<User>()
			.With(x => x.Id, command.UserId)
			.With(x => x.Workouts, workouts)
			.With(x => x.Exercises, ownedExercises)
			.Create();

		_mockRepository
			.Setup(x => x.GetReceivedWorkout(It.Is<string>(y => y == command.ReceivedWorkoutId)))
			.ReturnsAsync(receivedWorkout);

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.UserId)))
			.ReturnsAsync(user);

		var response = await _handler.HandleAsync(command);
		Assert.Equal(originalExerciseCount + receivedWorkout.DistinctExercises.Count, user.Exercises.Count);
		Assert.Equal(originalExerciseCount + receivedWorkout.DistinctExercises.Count, response.UserExercises.Count());
		Assert.Equal(command.NewName, response.NewWorkoutInfo.WorkoutName);
		Assert.Contains(user.Workouts, x => x.WorkoutId == response.NewWorkoutInfo.WorkoutId);
		Assert.True(user.ReceivedWorkouts.All(x => x.ReceivedWorkoutId != command.ReceivedWorkoutId));
	}

	[Fact]
	public async Task Should_Accept_Workout_Name_Not_Specified()
	{
		var command = Fixture.Build<AcceptReceivedWorkout>().With(x => x.NewName, (string?)null).Create();

		var receivedWorkout = ReceivedWorkoutHelper.GetReceivedWorkout(command.UserId);

		var workouts = Enumerable.Range(0, Globals.MaxWorkouts / 2)
			.Select(_ => Fixture.Create<WorkoutInfo>())
			.ToList();

		var originalExerciseCount = (Globals.MaxExercises - receivedWorkout.DistinctExercises.Count) / 2;
		var ownedExercises = Enumerable.Range(0, originalExerciseCount)
			.Select(_ => Fixture.Create<OwnedExercise>())
			.ToList();

		var user = Fixture.Build<User>()
			.With(x => x.Id, command.UserId)
			.With(x => x.Workouts, workouts)
			.With(x => x.Exercises, ownedExercises)
			.Create();

		_mockRepository
			.Setup(x => x.GetReceivedWorkout(It.Is<string>(y => y == command.ReceivedWorkoutId)))
			.ReturnsAsync(receivedWorkout);

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.UserId)))
			.ReturnsAsync(user);

		var response = await _handler.HandleAsync(command);
		Assert.Equal(originalExerciseCount + receivedWorkout.DistinctExercises.Count, user.Exercises.Count);
		Assert.Equal(originalExerciseCount + receivedWorkout.DistinctExercises.Count, response.UserExercises.Count());
		Assert.Equal(receivedWorkout.WorkoutName, response.NewWorkoutInfo.WorkoutName);
		Assert.Contains(user.Workouts, x => x.WorkoutId == response.NewWorkoutInfo.WorkoutId);
		Assert.True(user.ReceivedWorkouts.All(x => x.ReceivedWorkoutId != command.ReceivedWorkoutId));
	}

	[Fact]
	public async Task Should_Throw_Exception_Workout_Does_Not_Exist()
	{
		var command = Fixture.Create<AcceptReceivedWorkout>();

		_mockRepository
			.Setup(x => x.GetReceivedWorkout(It.Is<string>(y => y == command.ReceivedWorkoutId)))
			.ReturnsAsync((ReceivedWorkout)null!);

		await Assert.ThrowsAsync<ResourceNotFoundException>(() => _handler.HandleAsync(command));
	}

	[Fact]
	public async Task Should_Throw_Exception_Missing_Permissions_Workout()
	{
		var command = Fixture.Create<AcceptReceivedWorkout>();
		var user = Fixture.Create<User>();

		var receivedWorkout = ReceivedWorkoutHelper.GetReceivedWorkout();

		_mockRepository
			.Setup(x => x.GetReceivedWorkout(It.Is<string>(y => y == command.ReceivedWorkoutId)))
			.ReturnsAsync(receivedWorkout);

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.UserId)))
			.ReturnsAsync(user);

		await Assert.ThrowsAsync<ForbiddenException>(() => _handler.HandleAsync(command));
	}

	[Fact]
	public async Task Should_Throw_Exception_Max_Free_Workouts_Exceeded()
	{
		var command = Fixture.Create<AcceptReceivedWorkout>();
		var workouts = Enumerable.Range(0, Globals.MaxFreeWorkouts)
			.Select(_ => Fixture.Create<WorkoutInfo>())
			.ToList();
		var user = Fixture.Build<User>()
			.With(x => x.Id, command.UserId)
			.With(x => x.Workouts, workouts)
			.With(x => x.PremiumToken, (string?)null)
			.Create();

		var receivedWorkout = ReceivedWorkoutHelper.GetReceivedWorkout(command.UserId);

		_mockRepository
			.Setup(x => x.GetReceivedWorkout(It.Is<string>(y => y == command.ReceivedWorkoutId)))
			.ReturnsAsync(receivedWorkout);

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.UserId)))
			.ReturnsAsync(user);

		await Assert.ThrowsAsync<MaxLimitException>(() => _handler.HandleAsync(command));
	}

	[Fact]
	public async Task Should_Throw_Exception_Max_Workouts_Exceeded()
	{
		var command = Fixture.Create<AcceptReceivedWorkout>();
		var workouts = Enumerable.Range(0, Globals.MaxWorkouts)
			.Select(_ => Fixture.Create<WorkoutInfo>())
			.ToList();
		var user = Fixture.Build<User>()
			.With(x => x.Id, command.UserId)
			.With(x => x.Workouts, workouts)
			.Create();

		var receivedWorkout = ReceivedWorkoutHelper.GetReceivedWorkout(command.UserId);

		_mockRepository
			.Setup(x => x.GetReceivedWorkout(It.Is<string>(y => y == command.ReceivedWorkoutId)))
			.ReturnsAsync(receivedWorkout);

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.UserId)))
			.ReturnsAsync(user);

		await Assert.ThrowsAsync<MaxLimitException>(() => _handler.HandleAsync(command));
	}

	[Fact]
	public async Task Should_Throw_Exception_Workout_Name_Duplicate_From_Command()
	{
		var command = Fixture.Create<AcceptReceivedWorkout>();
		var workouts = Enumerable.Range(0, Globals.MaxWorkouts / 2)
			.Select(_ => Fixture.Build<WorkoutInfo>().With(y => y.WorkoutName, command.NewName).Create())
			.ToList();
		var user = Fixture.Build<User>()
			.With(x => x.Id, command.UserId)
			.With(x => x.Workouts, workouts)
			.Create();

		var receivedWorkout = ReceivedWorkoutHelper.GetReceivedWorkout(command.UserId);

		_mockRepository
			.Setup(x => x.GetReceivedWorkout(It.Is<string>(y => y == command.ReceivedWorkoutId)))
			.ReturnsAsync(receivedWorkout);

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.UserId)))
			.ReturnsAsync(user);

		await Assert.ThrowsAsync<AlreadyExistsException>(() => _handler.HandleAsync(command));
	}

	[Fact]
	public async Task Should_Throw_Exception_Workout_Name_Duplicate()
	{
		var command = Fixture.Build<AcceptReceivedWorkout>().With(x => x.NewName, (string?)null).Create();
		var workoutName = Fixture.Create<string>();
		var workouts = Enumerable.Range(0, Globals.MaxWorkouts / 2)
			.Select(_ => Fixture.Build<WorkoutInfo>().With(y => y.WorkoutName, workoutName).Create())
			.ToList();
		var user = Fixture.Build<User>()
			.With(x => x.Id, command.UserId)
			.With(x => x.Workouts, workouts)
			.Create();

		var receivedWorkout = ReceivedWorkoutHelper.GetReceivedWorkout(command.UserId);
		receivedWorkout.WorkoutName = workoutName;

		_mockRepository
			.Setup(x => x.GetReceivedWorkout(It.Is<string>(y => y == command.ReceivedWorkoutId)))
			.ReturnsAsync(receivedWorkout);

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.UserId)))
			.ReturnsAsync(user);

		await Assert.ThrowsAsync<AlreadyExistsException>(() => _handler.HandleAsync(command));
	}

	[Fact]
	public async Task Should_Throw_Exception_Max_Free_Exercises_Exceeded()
	{
		var command = Fixture.Build<AcceptReceivedWorkout>().With(x => x.NewName, (string?)null).Create();

		var receivedWorkout = ReceivedWorkoutHelper.GetReceivedWorkout(command.UserId);

		var workouts = Enumerable.Range(0, Globals.MaxFreeWorkouts / 2)
			.Select(_ => Fixture.Create<WorkoutInfo>())
			.ToList();

		var exercisesToCreate = Globals.MaxFreeExercises - receivedWorkout.DistinctExercises.Count + 1;
		var ownedExercises = Enumerable.Range(0, exercisesToCreate)
			.Select(_ => Fixture.Create<OwnedExercise>())
			.ToList();

		var user = Fixture.Build<User>()
			.With(x => x.Id, command.UserId)
			.With(x => x.Workouts, workouts)
			.With(x => x.PremiumToken, (string?)null)
			.With(x => x.Exercises, ownedExercises)
			.Create();

		_mockRepository
			.Setup(x => x.GetReceivedWorkout(It.Is<string>(y => y == command.ReceivedWorkoutId)))
			.ReturnsAsync(receivedWorkout);

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.UserId)))
			.ReturnsAsync(user);

		await Assert.ThrowsAsync<MaxLimitException>(() => _handler.HandleAsync(command));
	}

	[Fact]
	public async Task Should_Throw_Exception_Max_Exercises_Exceeded()
	{
		var command = Fixture.Build<AcceptReceivedWorkout>().With(x => x.NewName, (string?)null).Create();

		var receivedWorkout = ReceivedWorkoutHelper.GetReceivedWorkout(command.UserId);

		var workouts = Enumerable.Range(0, Globals.MaxFreeWorkouts / 2)
			.Select(_ => Fixture.Create<WorkoutInfo>())
			.ToList();

		var exercisesToCreate = Globals.MaxExercises - receivedWorkout.DistinctExercises.Count + 1;
		var ownedExercises = Enumerable.Range(0, exercisesToCreate)
			.Select(_ => Fixture.Create<OwnedExercise>())
			.ToList();

		var user = Fixture.Build<User>()
			.With(x => x.Id, command.UserId)
			.With(x => x.Workouts, workouts)
			.With(x => x.Exercises, ownedExercises)
			.Create();

		_mockRepository
			.Setup(x => x.GetReceivedWorkout(It.Is<string>(y => y == command.ReceivedWorkoutId)))
			.ReturnsAsync(receivedWorkout);

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.UserId)))
			.ReturnsAsync(user);

		await Assert.ThrowsAsync<MaxLimitException>(() => _handler.HandleAsync(command));
	}
}