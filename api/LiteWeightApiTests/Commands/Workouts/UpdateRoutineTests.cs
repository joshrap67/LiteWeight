using LiteWeightAPI.Commands.Workouts;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Domain.Workouts;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;

namespace LiteWeightApiTests.Commands.Workouts;

public class UpdateRoutineTests : BaseTest
{
	private readonly UpdateRoutineHandler _handler;
	private readonly Mock<IRepository> _mockRepository;

	public UpdateRoutineTests()
	{
		_mockRepository = new Mock<IRepository>();
		_handler = new UpdateRoutineHandler(_mockRepository.Object, Mapper);
	}

	[Theory]
	[InlineData(true)]
	[InlineData(false)]
	public async Task Should_Update(bool shouldUpdateDefault)
	{
		var newRoutine = Fixture.Build<SetRoutine>()
			.With(x => x.Weeks, new List<SetRoutineWeek>
			{
				Fixture.Build<SetRoutineWeek>().With(x => x.Days, new List<SetRoutineDay>
				{
					Fixture.Build<SetRoutineDay>().With(x => x.Exercises, new List<SetRoutineExercise>
					{
						Fixture.Build<SetRoutineExercise>()
							.With(x => x.ExerciseId, "A")
							.With(x => x.Weight, 200)
							.Create(),
						Fixture.Build<SetRoutineExercise>()
							.With(x => x.ExerciseId, "B")
							.With(x => x.Weight, 25)
							.Create(),
						Fixture.Build<SetRoutineExercise>()
							.With(x => x.ExerciseId, "C")
							.With(x => x.Weight, 110)
							.Create()
					}).Create(),
					Fixture.Build<SetRoutineDay>().With(x => x.Exercises, new List<SetRoutineExercise>
					{
						Fixture.Build<SetRoutineExercise>()
							.With(x => x.ExerciseId, "A")
							.With(x => x.Weight, 125)
							.Create(),
						Fixture.Build<SetRoutineExercise>()
							.With(x => x.ExerciseId, "B")
							.With(x => x.Weight, 25)
							.Create(),
						Fixture.Build<SetRoutineExercise>()
							.With(x => x.ExerciseId, "C")
							.With(x => x.Weight, 85)
							.Create(),
						Fixture.Build<SetRoutineExercise>()
							.With(x => x.ExerciseId, "D")
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
							.With(x => x.Weight, 300)
							.Create(),
						Fixture.Build<SetRoutineExercise>()
							.With(x => x.ExerciseId, "C")
							.With(x => x.Weight, 100)
							.Create(),
						Fixture.Build<SetRoutineExercise>()
							.With(x => x.ExerciseId, "D")
							.With(x => x.Weight, 600)
							.Create()
					}).Create(),
					Fixture.Build<SetRoutineDay>().With(x => x.Exercises, new List<SetRoutineExercise>
					{
						Fixture.Build<SetRoutineExercise>()
							.With(x => x.ExerciseId, "E")
							.With(x => x.Weight, 260)
							.Create(),
						Fixture.Build<SetRoutineExercise>()
							.With(x => x.ExerciseId, "Z")
							.With(x => x.Weight, 130)
							.Create(),
						Fixture.Build<SetRoutineExercise>()
							.With(x => x.ExerciseId, "F")
							.With(x => x.Weight, 100)
							.Create()
					}).Create()
				}).Create()
			}).Create();

		var oldRoutine = Fixture.Build<Routine>()
			.With(x => x.Weeks, new List<RoutineWeek>
			{
				Fixture.Build<RoutineWeek>().With(x => x.Days, new List<RoutineDay>
				{
					Fixture.Build<RoutineDay>().With(x => x.Exercises, new List<RoutineExercise>
					{
						Fixture.Build<RoutineExercise>().With(x => x.ExerciseId, "A").Create(),
						Fixture.Build<RoutineExercise>().With(x => x.ExerciseId, "G").Create(),
						Fixture.Build<RoutineExercise>().With(x => x.ExerciseId, "C").Create()
					}).Create(),
					Fixture.Build<RoutineDay>().With(x => x.Exercises, new List<RoutineExercise>
					{
						Fixture.Build<RoutineExercise>().With(x => x.ExerciseId, "H").Create(),
						Fixture.Build<RoutineExercise>().With(x => x.ExerciseId, "B").Create(),
						Fixture.Build<RoutineExercise>().With(x => x.ExerciseId, "C").Create(),
						Fixture.Build<RoutineExercise>().With(x => x.ExerciseId, "A").Create()
					}).Create()
				}).Create(),
				Fixture.Build<RoutineWeek>().With(x => x.Days, new List<RoutineDay>
				{
					Fixture.Build<RoutineDay>().With(x => x.Exercises, new List<RoutineExercise>
					{
						Fixture.Build<RoutineExercise>().With(x => x.ExerciseId, "A").Create(),
						Fixture.Build<RoutineExercise>().With(x => x.ExerciseId, "C").Create(),
						Fixture.Build<RoutineExercise>().With(x => x.ExerciseId, "D").Create()
					}).Create(),
					Fixture.Build<RoutineDay>().With(x => x.Exercises, new List<RoutineExercise>
					{
						Fixture.Build<RoutineExercise>().With(x => x.ExerciseId, "J").Create(),
						Fixture.Build<RoutineExercise>().With(x => x.ExerciseId, "F").Create()
					}).Create()
				}).Create()
			}).Create();

		var command = Fixture.Build<UpdateRoutine>()
			.With(x => x.Routine, newRoutine)
			.Create();
		var workout = Fixture.Build<Workout>()
			.With(x => x.CreatorId, command.UserId)
			.With(x => x.Id, command.WorkoutId)
			.With(x => x.Routine, oldRoutine)
			.Create();

		var ownedExercises = Enumerable.Range(0, 10)
			.Select(_ => Fixture.Create<OwnedExercise>())
			.ToList();
		var exerciseIdToWeight = new Dictionary<string, double>
		{
			{ "A", 125.0 },
			{ "B", 25.0 },
			{ "C", 100.0 },
			{ "D", 20.0 },
			{ "E", 100.0 },
			{ "F", 100 },
			{ "Z", 100 }
		};
		ownedExercises.AddRange(exerciseIdToWeight.Select(kvp =>
			Fixture.Build<OwnedExercise>()
				.With(x => x.Id, kvp.Key)
				.With(x => x.DefaultWeight, kvp.Value)
				.Create()
		));
		// exercises of original routine
		ownedExercises.Add(Fixture.Build<OwnedExercise>()
			.With(x => x.Id, "G")
			.With(x => x.Workouts,
				new List<OwnedExerciseWorkout>
				{
					Fixture.Build<OwnedExerciseWorkout>().With(x => x.WorkoutId, command.WorkoutId).Create()
				})
			.Create()
		);
		ownedExercises.Add(Fixture.Build<OwnedExercise>()
			.With(x => x.Id, "H")
			.With(x => x.Workouts,
				new List<OwnedExerciseWorkout>
				{
					Fixture.Build<OwnedExerciseWorkout>().With(x => x.WorkoutId, command.WorkoutId).Create()
				})
			.Create()
		);
		ownedExercises.Add(Fixture.Build<OwnedExercise>()
			.With(x => x.Id, "J")
			.With(x => x.Workouts,
				new List<OwnedExerciseWorkout>
				{
					Fixture.Build<OwnedExerciseWorkout>().With(x => x.WorkoutId, command.WorkoutId).Create()
				})
			.Create()
		);

		var workoutInfos = new List<WorkoutInfo>
		{
			Fixture.Build<WorkoutInfo>().With(x => x.WorkoutId, command.WorkoutId).Create()
		};
		var user = Fixture.Build<User>()
			.With(x => x.Id, command.UserId)
			.With(x => x.Exercises, ownedExercises)
			.With(x => x.Workouts, workoutInfos)
			.With(x => x.Settings, new UserSettings { UpdateDefaultWeightOnSave = shouldUpdateDefault })
			.Create();

		_mockRepository
			.Setup(x => x.GetWorkout(It.Is<string>(y => y == command.WorkoutId)))
			.ReturnsAsync(workout);

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.UserId)))
			.ReturnsAsync(user);

		await _handler.HandleAsync(command);
		var exercisesOfWorkout = workout.Routine.Weeks
			.SelectMany(x => x.Days)
			.SelectMany(x => x.Exercises)
			.DistinctBy(x => x.ExerciseId)
			.Select(x => x.ExerciseId)
			.ToHashSet();
		Assert.Contains("A", exercisesOfWorkout);
		Assert.Contains("B", exercisesOfWorkout);
		Assert.Contains("C", exercisesOfWorkout);
		Assert.Contains("D", exercisesOfWorkout);
		Assert.Contains("E", exercisesOfWorkout);
		Assert.Contains("F", exercisesOfWorkout);
		Assert.Contains("Z", exercisesOfWorkout);
		// new exercises
		Assert.Contains(user.Exercises.First(x => x.Id == "E").Workouts,
			x => x.WorkoutId == command.WorkoutId);
		Assert.Contains(user.Exercises.First(x => x.Id == "Z").Workouts,
			x => x.WorkoutId == command.WorkoutId);

		// these exercises should have been deleted from routine
		Assert.DoesNotContain("G", exercisesOfWorkout);
		Assert.DoesNotContain("H", exercisesOfWorkout);
		Assert.DoesNotContain("J", exercisesOfWorkout);
		Assert.True(user.Exercises.First(x => x.Id == "G").Workouts.All(x => x.WorkoutId != command.WorkoutId));
		Assert.True(user.Exercises.First(x => x.Id == "H").Workouts.All(x => x.WorkoutId != command.WorkoutId));
		Assert.True(user.Exercises.First(x => x.Id == "J").Workouts.All(x => x.WorkoutId != command.WorkoutId));

		if (shouldUpdateDefault)
		{
			Assert.Equal(300, user.Exercises.First(x => x.Id == "A").DefaultWeight);
			Assert.Equal(25, user.Exercises.First(x => x.Id == "B").DefaultWeight);
			Assert.Equal(110, user.Exercises.First(x => x.Id == "C").DefaultWeight);
			Assert.Equal(600, user.Exercises.First(x => x.Id == "D").DefaultWeight);
			Assert.Equal(260, user.Exercises.First(x => x.Id == "E").DefaultWeight);
			Assert.Equal(100, user.Exercises.First(x => x.Id == "F").DefaultWeight);
			Assert.Equal(130, user.Exercises.First(x => x.Id == "Z").DefaultWeight);
		}
		else
		{
			foreach (var kvp in exerciseIdToWeight)
			{
				Assert.Equal(kvp.Value, user.Exercises.First(x => x.Id == kvp.Key).DefaultWeight);
			}
		}
	}

	[Fact]
	public async Task Should_Throw_Exception_Missing_Permissions_Workout()
	{
		var command = Fixture.Create<UpdateRoutine>();
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
		var command = Fixture.Create<UpdateRoutine>();

		_mockRepository
			.Setup(x => x.GetWorkout(It.Is<string>(y => y == command.WorkoutId)))
			.ReturnsAsync((Workout?)null);

		await Assert.ThrowsAsync<ResourceNotFoundException>(() => _handler.HandleAsync(command));
	}
}