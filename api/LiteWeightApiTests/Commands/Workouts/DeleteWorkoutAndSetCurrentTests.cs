using LiteWeightAPI.Commands.Workouts;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Domain.Workouts;
using LiteWeightAPI.Errors.Exceptions;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;
using LiteWeightAPI.Imports;
using NodaTime;

namespace LiteWeightApiTests.Commands.Workouts;

public class DeleteWorkoutAndSetCurrentTests : BaseTest
{
	private readonly DeleteWorkoutAndSetCurrentHandler _handler;
	private readonly Mock<IRepository> _mockRepository;
	private readonly Mock<IClock> _mockClock;

	public DeleteWorkoutAndSetCurrentTests()
	{
		_mockRepository = new Mock<IRepository>();
		_mockClock = new Mock<IClock>();
		_handler = new DeleteWorkoutAndSetCurrentHandler(_mockRepository.Object, _mockClock.Object);
	}

	[Theory]
	[InlineData((string)null)]
	[InlineData("abc")]
	public async Task Should_Delete_And_Set_Current(string currentWorkoutId)
	{
		var command = Fixture.Build<DeleteWorkoutAndSetCurrent>()
			.With(x => x.CurrentWorkoutId, currentWorkoutId)
			.Create();
		var workouts = Enumerable.Range(0, Globals.MaxFreeWorkouts / 2)
			.Select(_ =>
			{
				return currentWorkoutId != null
					? Fixture.Build<WorkoutInfo>().With(x => x.WorkoutId, currentWorkoutId).Create()
					: Fixture.Create<WorkoutInfo>();
			})
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
			Fixture.Build<OwnedExercise>()
				.With(x => x.Id, exerciseId)
				.With(x => x.Workouts,
					new List<OwnedExerciseWorkout>
					{
						Fixture.Build<OwnedExerciseWorkout>()
							.With(x => x.WorkoutId, command.WorkoutToDeleteId)
							.Create()
					})
				.Create()
		));

		var user = Fixture.Build<User>()
			.With(x => x.Id, command.UserId)
			.With(x => x.Workouts, workouts)
			.With(x => x.Exercises, ownedExercises)
			.Create();

		var instant = Fixture.Create<Instant>();
		_mockClock.Setup(x => x.GetCurrentInstant()).Returns(instant);

		_mockRepository
			.Setup(x => x.GetWorkout(It.Is<string>(y => y == command.WorkoutToDeleteId)))
			.ReturnsAsync(workout);

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.UserId)))
			.ReturnsAsync(user);

		await _handler.HandleAsync(command);
		// no exercise on the user should have this workout anymore
		Assert.True(user.Exercises.All(x => x.Workouts.All(y => y.WorkoutId != command.WorkoutToDeleteId)));
		Assert.True(user.Workouts.All(x => x.WorkoutId != command.WorkoutToDeleteId));
		Assert.Equal(user.CurrentWorkoutId, currentWorkoutId);
		if (currentWorkoutId != null)
		{
			Assert.Equal(instant,
				user.Workouts.First(x => x.WorkoutId == command.CurrentWorkoutId).LastSetAsCurrentUtc);
		}
	}

	[Fact]
	public async Task Should_Throw_Exception_Missing_Referenced_Workout_Not_Found()
	{
		var command = Fixture.Create<DeleteWorkoutAndSetCurrent>();
		var workouts = Enumerable.Range(0, Globals.MaxFreeWorkouts / 2)
			.Select(_ => Fixture.Create<WorkoutInfo>())
			.ToList();
		var workout = Fixture.Build<Workout>()
			.With(x => x.CreatorId, command.UserId)
			.Create();

		var user = Fixture.Build<User>()
			.With(x => x.Id, command.UserId)
			.With(x => x.Workouts, workouts)
			.Create();

		_mockRepository
			.Setup(x => x.GetWorkout(It.Is<string>(y => y == command.WorkoutToDeleteId)))
			.ReturnsAsync(workout);

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.UserId)))
			.ReturnsAsync(user);

		await Assert.ThrowsAsync<WorkoutNotFoundException>(() => _handler.HandleAsync(command));
	}

	[Fact]
	public async Task Should_Throw_Exception_Missing_Permissions_Workout()
	{
		var command = Fixture.Create<DeleteWorkoutAndSetCurrent>();
		var user = Fixture.Create<User>();

		_mockRepository
			.Setup(x => x.GetWorkout(It.Is<string>(y => y == command.WorkoutToDeleteId)))
			.ReturnsAsync(Fixture.Create<Workout>());

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.UserId)))
			.ReturnsAsync(user);

		await Assert.ThrowsAsync<ForbiddenException>(() => _handler.HandleAsync(command));
	}

	[Fact]
	public async Task Should_Throw_Exception_Workout_Not_Found()
	{
		var command = Fixture.Create<DeleteWorkoutAndSetCurrent>();

		_mockRepository
			.Setup(x => x.GetWorkout(It.Is<string>(y => y == command.WorkoutToDeleteId)))
			.ReturnsAsync((Workout)null);

		await Assert.ThrowsAsync<ResourceNotFoundException>(() => _handler.HandleAsync(command));
	}
}