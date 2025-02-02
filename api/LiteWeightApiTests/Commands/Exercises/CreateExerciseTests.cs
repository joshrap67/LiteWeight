using LiteWeightAPI.Commands.Exercises;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Errors.Exceptions;
using LiteWeightAPI.Imports;

namespace LiteWeightApiTests.Commands.Exercises;

public class CreateExerciseTests : BaseTest
{
	private readonly CreateExerciseHandler _handler;
	private readonly Mock<IRepository> _mockRepository;

	public CreateExerciseTests()
	{
		_mockRepository = new Mock<IRepository>();
		_handler = new CreateExerciseHandler(_mockRepository.Object, Mapper);
	}

	[Fact]
	public async Task Should_Create_Exercise()
	{
		var command = Fixture.Create<CreateExercise>();
		var exercises = Enumerable.Range(0, Globals.MaxExercises - 1)
			.Select(_ => Fixture.Build<OwnedExercise>().Create())
			.ToList();

		var user = Fixture.Build<User>()
			.With(x => x.Exercises, exercises)
			.With(x => x.PremiumToken, Fixture.Create<string>())
			.Create();

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.UserId)))
			.ReturnsAsync(user);

		var createdExercise = await _handler.HandleAsync(command);

		Assert.True(createdExercise.Name == command.Name);
		Assert.True(createdExercise.DefaultDetails == command.DefaultDetails);
		Assert.True(Math.Abs(createdExercise.DefaultWeight - command.DefaultWeight) < 0.01);
		Assert.True(createdExercise.DefaultReps == command.DefaultReps);
		Assert.True(createdExercise.DefaultSets == command.DefaultSets);
		Assert.True(createdExercise.VideoUrl == command.VideoUrl);
		Assert.Equivalent(command.Focuses, createdExercise.Focuses);
		Assert.Contains(user.Exercises, x => x.Id == createdExercise.Id);
	}

	[Fact]
	public async Task Should_Throw_Exception_Name_Already_Exists()
	{
		var command = Fixture.Create<CreateExercise>();
		command.Name = "Name";

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.UserId)))
			.ReturnsAsync(Fixture.Build<User>()
				.With(x => x.Exercises, [new() { Name = "Name" }])
				.Create()
			);

		await Assert.ThrowsAsync<AlreadyExistsException>(() => _handler.HandleAsync(command));
	}

	[Fact]
	public async Task Should_Throw_Exception_Max_Limit_Free()
	{
		var command = Fixture.Create<CreateExercise>();
		var exercises = Enumerable.Range(0, Globals.MaxFreeExercises + 1)
			.Select(_ => Fixture.Build<OwnedExercise>().Create())
			.ToList();

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.UserId)))
			.ReturnsAsync(Fixture.Build<User>()
				.With(x => x.Exercises, exercises)
				.With(x => x.PremiumToken, (string)null)
				.Create());

		await Assert.ThrowsAsync<MaxLimitException>(() => _handler.HandleAsync(command));
	}

	[Fact]
	public async Task Should_Throw_Exception_Max_Limit()
	{
		var command = Fixture.Create<CreateExercise>();
		var exercises = Enumerable.Range(0, Globals.MaxExercises + 1)
			.Select(_ => Fixture.Build<OwnedExercise>().Create())
			.ToList();

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.UserId)))
			.ReturnsAsync(Fixture.Build<User>()
				.With(x => x.Exercises, exercises)
				.With(x => x.PremiumToken, Fixture.Create<string>())
				.Create());

		await Assert.ThrowsAsync<MaxLimitException>(() => _handler.HandleAsync(command));
	}
}