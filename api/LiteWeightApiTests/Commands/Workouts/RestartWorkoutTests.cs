using LiteWeightAPI.Commands.Workouts;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Domain.Workouts;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;
using LiteWeightAPI.Imports;

namespace LiteWeightApiTests.Commands.Workouts;

public class RestartWorkoutTests : BaseTest
{
	private readonly RestartWorkoutHandler _handler;
	private readonly IRepository _mockRepository;

	public RestartWorkoutTests()
	{
		_mockRepository = Substitute.For<IRepository>();
		_handler = new RestartWorkoutHandler(_mockRepository);
	}

	[Theory]
	[InlineData(true)]
	[InlineData(false)]
	public async Task Should_Restart(bool shouldUpdateDefault)
	{
		var routine = Fixture.Build<SetRoutine>()
			.With(x => x.Weeks, new List<SetRoutineWeek>
			{
				Fixture.Build<SetRoutineWeek>().With(x => x.Days, new List<SetRoutineDay>
				{
					Fixture.Build<SetRoutineDay>().With(x => x.Exercises, new List<SetRoutineExercise>
					{
						Fixture.Build<SetRoutineExercise>()
							.With(x => x.ExerciseId, "A")
							.With(x => x.Completed, true)
							.With(x => x.Weight, 200)
							.Create(),
						Fixture.Build<SetRoutineExercise>()
							.With(x => x.ExerciseId, "B")
							.With(x => x.Completed, false)
							.With(x => x.Weight, 25)
							.Create(),
						Fixture.Build<SetRoutineExercise>()
							.With(x => x.ExerciseId, "C")
							.With(x => x.Completed, true)
							.With(x => x.Weight, 110)
							.Create()
					}).Create(),
					Fixture.Build<SetRoutineDay>().With(x => x.Exercises, new List<SetRoutineExercise>
					{
						Fixture.Build<SetRoutineExercise>()
							.With(x => x.ExerciseId, "A")
							.With(x => x.Completed, false)
							.With(x => x.Weight, 125)
							.Create(),
						Fixture.Build<SetRoutineExercise>()
							.With(x => x.ExerciseId, "B")
							.With(x => x.Completed, false)
							.With(x => x.Weight, 25)
							.Create(),
						Fixture.Build<SetRoutineExercise>()
							.With(x => x.ExerciseId, "C")
							.With(x => x.Completed, true)
							.With(x => x.Weight, 85)
							.Create(),
						Fixture.Build<SetRoutineExercise>()
							.With(x => x.ExerciseId, "D")
							.With(x => x.Completed, true)
							.With(x => x.Weight, 100)
							.Create()
					}).Create()
				}).Create(),
				Fixture.Build<SetRoutineWeek>().With(x => x.Days, new List<SetRoutineDay>
				{
					Fixture.Build<SetRoutineDay>().With(x => x.Exercises, new List<SetRoutineExercise>
					{
						Fixture.Build<SetRoutineExercise>()
							.With(x => x.ExerciseId, "A")
							.With(x => x.Completed, true)
							.With(x => x.Weight, 300)
							.Create(),
						Fixture.Build<SetRoutineExercise>()
							.With(x => x.ExerciseId, "C")
							.With(x => x.Completed, false)
							.With(x => x.Weight, 100)
							.Create(),
						Fixture.Build<SetRoutineExercise>()
							.With(x => x.ExerciseId, "D")
							.With(x => x.Completed, false)
							.With(x => x.Weight, 600)
							.Create()
					}).Create(),
					Fixture.Build<SetRoutineDay>().With(x => x.Exercises, new List<SetRoutineExercise>
					{
						Fixture.Build<SetRoutineExercise>()
							.With(x => x.ExerciseId, "E")
							.With(x => x.Completed, true)
							.With(x => x.Weight, 260)
							.Create(),
						Fixture.Build<SetRoutineExercise>()
							.With(x => x.ExerciseId, "F")
							.With(x => x.Completed, false)
							.With(x => x.Weight, 100)
							.Create()
					}).Create()
				}).Create()
			}).Create();
		var command = Fixture.Build<RestartWorkout>()
			.With(x => x.Routine, routine)
			.Create();

		var workouts = Enumerable.Range(0, Globals.MaxFreeWorkouts / 2)
			.Select(_ => Fixture.Create<WorkoutInfo>())
			.ToList();
		var previousInfo = Fixture.Build<WorkoutInfo>()
			.With(x => x.WorkoutId, command.WorkoutId)
			.With(x => x.TimesRestarted, 2)
			.With(x => x.AverageWorkoutCompletion, .850)
			.Create();
		workouts.Add(previousInfo);
		var workout = Fixture.Build<Workout>()
			.With(x => x.CreatorId, command.UserId)
			.Create();

		var ownedExercises = Enumerable.Range(0, 10)
			.Select(_ => Fixture.Create<OwnedExercise>())
			.ToList();
		var originalExercisesToWeight = new Dictionary<string, double>
		{
			{ "A", 125.0 },
			{ "B", 25.0 },
			{ "C", 100.0 },
			{ "D", 20.0 },
			{ "E", 100.0 },
			{ "F", 100 }
		};
		ownedExercises.AddRange(originalExercisesToWeight.Select(kvp =>
			Fixture.Build<OwnedExercise>()
				.With(x => x.Id, kvp.Key)
				.With(x => x.DefaultWeight, kvp.Value)
				.Create()
		));

		var user = Fixture.Build<User>()
			.With(x => x.Id, command.UserId)
			.With(x => x.Workouts, workouts)
			.With(x => x.Exercises, ownedExercises)
			.With(x => x.Settings, new UserSettings { UpdateDefaultWeightOnRestart = shouldUpdateDefault })
			.Create();

		_mockRepository
			.GetWorkout(Arg.Is<string>(y => y == command.WorkoutId))
			.Returns(workout);

		_mockRepository
			.GetUser(Arg.Is<string>(y => y == command.UserId))
			.Returns(user);

		await _handler.HandleAsync(command);
		Assert.Equal(3, previousInfo.TimesRestarted);
		Assert.Equal(.73333333, previousInfo.AverageWorkoutCompletion, 4);
		Assert.Equal(0, previousInfo.CurrentDay);
		Assert.Equal(0, previousInfo.CurrentWeek);
		Assert.True(workout.Routine.Weeks.SelectMany(x => x.Days).SelectMany(x => x.Exercises).All(x => !x.Completed));
		if (shouldUpdateDefault)
		{
			Assert.Equal(300, user.Exercises.First(x => x.Id == "A").DefaultWeight);
			Assert.Equal(25, user.Exercises.First(x => x.Id == "B").DefaultWeight);
			Assert.Equal(110, user.Exercises.First(x => x.Id == "C").DefaultWeight);
			Assert.Equal(100, user.Exercises.First(x => x.Id == "D").DefaultWeight);
			Assert.Equal(260, user.Exercises.First(x => x.Id == "E").DefaultWeight);
			Assert.Equal(100, user.Exercises.First(x => x.Id == "F").DefaultWeight);
		}
		else
		{
			foreach (var kvp in originalExercisesToWeight)
			{
				Assert.Equal(kvp.Value, user.Exercises.First(x => x.Id == kvp.Key).DefaultWeight);
			}
		}
	}

	[Fact]
	public async Task Should_Throw_Exception_Missing_Permissions_Workout()
	{
		var command = Fixture.Create<RestartWorkout>();
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
		var command = Fixture.Create<RestartWorkout>();

		_mockRepository
			.GetWorkout(Arg.Is<string>(y => y == command.WorkoutId))
			.Returns((Workout?)null);

		await Assert.ThrowsAsync<ResourceNotFoundException>(() => _handler.HandleAsync(command));
	}
}