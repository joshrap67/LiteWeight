using LiteWeightAPI.Commands.Workouts;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;
using LiteWeightAPI.Imports;

namespace LiteWeightApiTests.Commands.Workouts;

public class ResetStatisticsTests : BaseTest
{
	private readonly ResetStatisticsHandler _handler;
	private readonly IRepository _mockRepository;

	public ResetStatisticsTests()
	{
		_mockRepository = Substitute.For<IRepository>();
		_handler = new ResetStatisticsHandler(_mockRepository);
	}

	[Fact]
	public async Task Should_Reset_Statistics()
	{
		var command = Fixture.Create<ResetStatistics>();
		var workouts = Enumerable.Range(0, Globals.MaxFreeWorkouts / 2)
			.Select(_ => Fixture.Create<WorkoutInfo>())
			.ToList();
		workouts.Add(Fixture.Build<WorkoutInfo>().With(x => x.WorkoutId, command.WorkoutId).Create());

		var user = Fixture.Build<User>()
			.With(x => x.Id, command.UserId)
			.With(x => x.Workouts, workouts)
			.Create();

		_mockRepository
			.GetUser(Arg.Is<string>(y => y == command.UserId))
			.Returns(user);

		await _handler.HandleAsync(command);
		Assert.Equal(0, user.Workouts.First(x => x.WorkoutId == command.WorkoutId).TimesRestarted);
		Assert.Equal(0, user.Workouts.First(x => x.WorkoutId == command.WorkoutId).AverageWorkoutCompletion);
	}


	[Fact]
	public async Task Should_Throw_Exception_Workout_Not_Found()
	{
		var command = Fixture.Create<ResetStatistics>();

		_mockRepository
			.GetUser(Arg.Is<string>(y => y == command.UserId))
			.Returns(Fixture.Create<User>());

		await Assert.ThrowsAsync<ResourceNotFoundException>(() => _handler.HandleAsync(command));
	}
}