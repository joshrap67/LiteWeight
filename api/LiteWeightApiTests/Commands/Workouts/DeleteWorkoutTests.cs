using LiteWeightAPI.Commands.Workouts;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Domain.Workouts;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;
using LiteWeightAPI.Imports;

namespace LiteWeightApiTests.Commands.Workouts;

public class DeleteWorkoutTests : BaseTest
{
	private readonly DeleteWorkoutHandler _handler;
	private readonly Mock<IRepository> _mockRepository;

	public DeleteWorkoutTests()
	{
		_mockRepository = new Mock<IRepository>();
		_handler = new DeleteWorkoutHandler(_mockRepository.Object);
	}

	[Fact]
	public async Task Should_Delete()
	{
		var command = Fixture.Create<DeleteWorkout>();
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
			Fixture.Build<OwnedExercise>()
				.With(x => x.Id, exerciseId)
				.With(x => x.Workouts,
					new List<OwnedExerciseWorkout>
					{
						Fixture.Build<OwnedExerciseWorkout>()
							.With(x => x.WorkoutId, command.WorkoutId)
							.Create()
					})
				.Create()
		));

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

		await _handler.HandleAsync(command);
		// no exercise on the user should have this workout anymore
		Assert.True(user.Exercises.All(x => x.Workouts.All(y => y.WorkoutId != command.WorkoutId)));
		Assert.True(user.Workouts.All(x => x.WorkoutId != command.WorkoutId));
	}

	[Fact]
	public async Task Should_Throw_Exception_Missing_Permissions_Workout()
	{
		var command = Fixture.Create<DeleteWorkout>();
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
		var command = Fixture.Create<DeleteWorkout>();

		_mockRepository
			.Setup(x => x.GetWorkout(It.Is<string>(y => y == command.WorkoutId)))
			.ReturnsAsync((Workout?)null);

		await Assert.ThrowsAsync<ResourceNotFoundException>(() => _handler.HandleAsync(command));
	}
}