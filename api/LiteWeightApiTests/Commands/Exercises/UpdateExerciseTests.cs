using LiteWeightAPI.Commands.Exercises;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Errors.Exceptions;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;

namespace LiteWeightApiTests.Commands.Exercises;

public class UpdateExerciseTests : BaseTest
{
	private readonly UpdateExerciseHandler _handler;
	private readonly IRepository _mockRepository;

	public UpdateExerciseTests()
	{
		_mockRepository = Substitute.For<IRepository>();
		_handler = new UpdateExerciseHandler(_mockRepository);
	}

	[Fact]
	public async Task Should_Update_Exercise_New_Name()
	{
		var command = Fixture.Create<UpdateExercise>();
		var exercise = Fixture.Build<OwnedExercise>()
			.With(x => x.Id, command.ExerciseId)
			.Create();
		var exercises = new List<OwnedExercise>
		{
			exercise,
			Fixture.Create<OwnedExercise>()
		};
		var user = Fixture.Build<User>()
			.With(x => x.Exercises, exercises)
			.Create();

		_mockRepository
			.GetUser(Arg.Is<string>(y => y == command.UserId))
			.Returns(user);

		await _handler.HandleAsync(command);

		Assert.True(exercise.Name == command.Name);
		Assert.True(Math.Abs(exercise.DefaultWeight - command.DefaultWeight) < 0.01);
		Assert.True(exercise.DefaultReps == command.DefaultReps);
		Assert.True(exercise.DefaultSets == command.DefaultSets);
		foreach (var exerciseLink in exercise.Links)
		{
			Assert.Contains(command.Links, x => x.Label == exerciseLink.Label);
			Assert.Contains(command.Links, x => x.Url == exerciseLink.Url);
		}

		Assert.True(exercise.Notes == command.Notes);
		Assert.Equivalent(command.Focuses, exercise.Focuses);
		Assert.Contains(user.Exercises, x => x.Id == exercise.Id);
	}

	[Fact]
	public async Task Should_Update_Exercise_Same_Name()
	{
		var command = Fixture.Create<UpdateExercise>();
		var exercise = Fixture.Build<OwnedExercise>()
			.With(x => x.Id, command.ExerciseId)
			.With(x => x.Name, command.Name)
			.Create();
		var exercises = new List<OwnedExercise>
		{
			exercise,
			Fixture.Create<OwnedExercise>()
		};
		var user = Fixture.Build<User>()
			.With(x => x.Exercises, exercises)
			.Create();

		_mockRepository
			.GetUser(Arg.Is<string>(y => y == command.UserId))
			.Returns(user);

		await _handler.HandleAsync(command);

		Assert.True(exercise.Name == command.Name);
		Assert.True(Math.Abs(exercise.DefaultWeight - command.DefaultWeight) < 0.01);
		Assert.True(exercise.DefaultReps == command.DefaultReps);
		Assert.True(exercise.DefaultSets == command.DefaultSets);
		foreach (var exerciseLink in exercise.Links)
		{
			Assert.Contains(command.Links, x => x.Label == exerciseLink.Label);
			Assert.Contains(command.Links, x => x.Url == exerciseLink.Url);
		}

		Assert.Equivalent(command.Focuses, exercise.Focuses);
		Assert.Contains(user.Exercises, x => x.Id == exercise.Id);
	}

	[Fact]
	public async Task Should_Throw_Exception_Exercise_Not_Found()
	{
		var command = Fixture.Create<UpdateExercise>();
		command.ExerciseId = Fixture.Create<string>();

		_mockRepository
			.GetUser(Arg.Is<string>(y => y == command.UserId))
			.Returns(Fixture.Create<User>());

		await Assert.ThrowsAsync<ResourceNotFoundException>(() => _handler.HandleAsync(command));
	}

	[Fact]
	public async Task Should_Throw_Exception_Exercise_Name_Already_Exists()
	{
		var command = Fixture.Create<UpdateExercise>();
		var name = Fixture.Create<string>();
		command.Name = name;
		var exercise = Fixture.Build<OwnedExercise>()
			.With(x => x.Id, command.ExerciseId)
			.Create();
		var exercises = new List<OwnedExercise>
		{
			exercise,
			Fixture.Build<OwnedExercise>().With(x => x.Name, name).Create()
		};
		var user = Fixture.Build<User>()
			.With(x => x.Exercises, exercises)
			.Create();

		_mockRepository
			.GetUser(Arg.Is<string>(y => y == command.UserId))
			.Returns(user);

		await Assert.ThrowsAsync<AlreadyExistsException>(() => _handler.HandleAsync(command));
	}
}