using LiteWeightAPI.Commands.Exercises;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Domain.Workouts;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;

namespace LiteWeightApiTests.Commands.Exercises;

public class DeleteExerciseTests : BaseTest
{
	private readonly DeleteExerciseHandler _handler;
	private readonly IRepository _mockRepository;

	public DeleteExerciseTests()
	{
		_mockRepository = Substitute.For<IRepository>();
		_handler = new DeleteExerciseHandler(_mockRepository);
	}

	[Fact]
	public async Task Should_Delete_Exercise()
	{
		var command = Fixture.Create<DeleteExercise>();
		var exercise = Fixture.Build<OwnedExercise>()
			.With(x => x.Id, command.ExerciseId)
			.Create();
		var exercises = new List<OwnedExercise> { exercise };

		var routine = Fixture.Build<Routine>()
			.With(x => x.Weeks, new List<RoutineWeek>
			{
				new()
				{
					Days = new List<RoutineDay>
					{
						new()
						{
							Exercises = new List<RoutineExercise>
							{
								Fixture.Build<RoutineExercise>().With(x => x.ExerciseId, exercise.Id).Create()
							}
						}
					}
				}
			}).Create();

		var workout = Fixture.Build<Workout>()
			.With(x => x.Routine, routine)
			.Create();

		var user = Fixture.Build<User>()
			.With(x => x.Exercises, exercises)
			.Create();

		_mockRepository
			.GetUser(Arg.Is<string>(y => y == command.UserId))
			.Returns(user);
		_mockRepository
			.GetWorkout(Arg.Is<string>(y => exercise.Workouts.Any(z => z.WorkoutId == y)))
			.Returns(workout);

		await _handler.HandleAsync(command);

		Assert.True(user.Exercises.All(x => x.Id != exercise.Id));
		// workout should no longer contain exercise
		Assert.True(workout.Routine.Weeks.SelectMany(x => x.Days).SelectMany(x => x.Exercises)
			.All(x => x.ExerciseId != exercise.Id));
	}

	[Fact]
	public async Task Should_Throw_Exception_Exercise_Not_Found()
	{
		var command = Fixture.Create<DeleteExercise>();
		command.ExerciseId = Fixture.Create<string>();

		_mockRepository
			.GetUser(Arg.Is<string>(y => y == command.UserId))
			.Returns(Fixture.Create<User>());

		await Assert.ThrowsAsync<ResourceNotFoundException>(() => _handler.HandleAsync(command));
	}
}