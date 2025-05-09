using LiteWeightAPI.Commands.Users;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Domain.Workouts;
using LiteWeightAPI.Errors.Exceptions;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;
using LiteWeightAPI.Imports;
using LiteWeightAPI.Services;
using NodaTime;

namespace LiteWeightApiTests.Commands.Users;

public class SendWorkoutTests : BaseTest
{
	private readonly SendWorkoutHandler _handler;
	private readonly IRepository _mockRepository;
	private readonly IPushNotificationService _mockPushNotificationService;

	public SendWorkoutTests()
	{
		_mockRepository = Substitute.For<IRepository>();
		_mockPushNotificationService = Substitute.For<IPushNotificationService>();
		var clock = Substitute.For<IClock>();
		var statisticsService = Substitute.For<IStatisticsService>();
		_handler = new SendWorkoutHandler(_mockRepository, _mockPushNotificationService, clock,
			statisticsService);
	}

	[Fact]
	public async Task Should_Send_Workout_Private_Recipient()
	{
		var command = Fixture.Create<SendWorkout>();

		var workout = Fixture.Build<Workout>()
			.With(x => x.CreatorId, command.SenderUserId)
			.Create();
		var exerciseIds = workout.Routine.Weeks
			.SelectMany(x => x.Days)
			.SelectMany(x => x.Exercises)
			.Select(x => x.ExerciseId)
			.Distinct();
		var ownedExercises = exerciseIds.Select(exerciseId => Fixture.Build<OwnedExercise>()
				.With(x => x.Id, exerciseId)
				.Create())
			.ToList();

		var preferences = new UserSettings { PrivateAccount = true };
		var recipientUser = Fixture.Build<User>()
			.With(x => x.Id, command.RecipientUserId)
			.With(x => x.Settings, preferences)
			.With(x => x.Friends, [Fixture.Build<Friend>().With(x => x.UserId, command.SenderUserId).Create()])
			.Create();

		var senderUser = Fixture.Build<User>()
			.With(x => x.Id, command.SenderUserId)
			.With(x => x.Exercises, ownedExercises)
			.With(x => x.PremiumToken, (string?)null)
			.With(x => x.WorkoutsSent, Globals.MaxFreeWorkoutsSent - 1)
			.Create();

		_mockRepository
			.GetUser(Arg.Is<string>(y => y == command.RecipientUserId))
			.Returns(recipientUser);

		_mockRepository
			.GetUser(Arg.Is<string>(y => y == command.SenderUserId))
			.Returns(senderUser);

		_mockRepository
			.GetWorkout(Arg.Is<string>(y => y == command.WorkoutId))
			.Returns(workout);

		var response = await _handler.HandleAsync(command);
		Assert.NotNull(response);
		await _mockPushNotificationService.Received(1)
			.SendReceivedWorkoutPushNotification(Arg.Any<User>(), Arg.Any<ReceivedWorkoutInfo>());
	}

	[Fact]
	public async Task Should_Send_Workout_Non_Private_Recipient()
	{
		var command = Fixture.Create<SendWorkout>();

		var workout = Fixture.Build<Workout>()
			.With(x => x.CreatorId, command.SenderUserId)
			.Create();
		var exerciseIds = workout.Routine.Weeks
			.SelectMany(x => x.Days)
			.SelectMany(x => x.Exercises)
			.Select(x => x.ExerciseId)
			.Distinct();
		var ownedExercises = exerciseIds
			.Select(exerciseId => Fixture.Build<OwnedExercise>().With(x => x.Id, exerciseId).Create())
			.ToList();

		var preferences = new UserSettings { PrivateAccount = false };
		var recipientUser = Fixture.Build<User>()
			.With(x => x.Id, command.RecipientUserId)
			.With(x => x.Settings, preferences)
			.Create();

		var senderUser = Fixture.Build<User>()
			.With(x => x.Id, command.SenderUserId)
			.With(x => x.Exercises, ownedExercises)
			.With(x => x.PremiumToken, (string?)null)
			.With(x => x.WorkoutsSent, Globals.MaxFreeWorkoutsSent - 1)
			.Create();

		_mockRepository
			.GetUser(Arg.Is<string>(y => y == command.RecipientUserId))
			.Returns(recipientUser);

		_mockRepository
			.GetUser(Arg.Is<string>(y => y == command.SenderUserId))
			.Returns(senderUser);

		_mockRepository
			.GetWorkout(Arg.Is<string>(y => y == command.WorkoutId))
			.Returns(workout);

		var response = await _handler.HandleAsync(command);
		Assert.NotNull(response);
		await _mockPushNotificationService.Received(1)
			.SendReceivedWorkoutPushNotification(Arg.Any<User>(), Arg.Any<ReceivedWorkoutInfo>());
	}

	[Fact]
	public async Task Should_Throw_Exception_Max_Free_Workouts_Sent()
	{
		var command = Fixture.Create<SendWorkout>();

		var preferences = new UserSettings { PrivateAccount = false };
		var recipientUser = Fixture.Build<User>()
			.With(x => x.Id, command.RecipientUserId)
			.With(x => x.Settings, preferences)
			.Create();

		var senderUser = Fixture.Build<User>()
			.With(x => x.Id, command.SenderUserId)
			.With(x => x.PremiumToken, (string?)null)
			.With(x => x.WorkoutsSent, Globals.MaxFreeWorkoutsSent)
			.Create();

		var workout = Fixture.Build<Workout>()
			.With(x => x.CreatorId, command.SenderUserId)
			.Create();

		_mockRepository
			.GetUser(Arg.Is<string>(y => y == command.RecipientUserId))
			.Returns(recipientUser);

		_mockRepository
			.GetUser(Arg.Is<string>(y => y == command.SenderUserId))
			.Returns(senderUser);

		_mockRepository
			.GetWorkout(Arg.Is<string>(y => y == command.WorkoutId))
			.Returns(workout);

		await Assert.ThrowsAsync<MaxLimitException>(() => _handler.HandleAsync(command));
	}

	[Fact]
	public async Task Should_Throw_Exception_Max_Limit_Received_Workouts()
	{
		var command = Fixture.Create<SendWorkout>();

		var preferences = new UserSettings { PrivateAccount = false };
		var receivedWorkouts = Enumerable.Range(0, Globals.MaxReceivedWorkouts + 1)
			.Select(_ => Fixture.Build<ReceivedWorkoutInfo>().Create())
			.ToList();
		var recipientUser = Fixture.Build<User>()
			.With(x => x.Id, command.RecipientUserId)
			.With(x => x.Settings, preferences)
			.With(x => x.ReceivedWorkouts, receivedWorkouts)
			.Create();

		var workout = Fixture.Build<Workout>()
			.With(x => x.CreatorId, command.SenderUserId)
			.Create();

		_mockRepository
			.GetUser(Arg.Is<string>(y => y == command.RecipientUserId))
			.Returns(recipientUser);

		_mockRepository
			.GetWorkout(Arg.Is<string>(y => y == command.WorkoutId))
			.Returns(workout);

		await Assert.ThrowsAsync<MaxLimitException>(() => _handler.HandleAsync(command));
	}

	[Fact]
	public async Task Should_Throw_Exception_Private_User()
	{
		var command = Fixture.Create<SendWorkout>();

		var preferences = new UserSettings { PrivateAccount = true };
		var recipientUser = Fixture.Build<User>()
			.With(x => x.Id, command.RecipientUserId)
			.With(x => x.Settings, preferences)
			.Create();

		var workout = Fixture.Build<Workout>()
			.With(x => x.CreatorId, command.SenderUserId)
			.Create();

		_mockRepository
			.GetUser(Arg.Is<string>(y => y == command.RecipientUserId))
			.Returns(recipientUser);

		_mockRepository
			.GetWorkout(Arg.Is<string>(y => y == command.WorkoutId))
			.Returns(workout);

		await Assert.ThrowsAsync<ResourceNotFoundException>(() => _handler.HandleAsync(command));
	}

	[Fact]
	public async Task Should_Throw_Exception_Missing_Permissions_Workout()
	{
		var command = Fixture.Create<SendWorkout>();
		var user = Fixture.Create<User>();

		_mockRepository
			.GetUser(Arg.Is<string>(y => y == command.RecipientUserId))
			.Returns(user);

		_mockRepository
			.GetWorkout(Arg.Is<string>(y => y == command.WorkoutId))
			.Returns(Fixture.Create<Workout>());

		await Assert.ThrowsAsync<ForbiddenException>(() => _handler.HandleAsync(command));
	}

	[Fact]
	public async Task Should_Throw_Exception_Referenced_Workout_Not_Found()
	{
		var command = Fixture.Create<SendWorkout>();

		_mockRepository
			.GetUser(Arg.Is<string>(y => y == command.RecipientUserId))
			.Returns(Fixture.Create<User>());

		_mockRepository
			.GetWorkout(Arg.Is<string>(y => y == command.WorkoutId))
			.Returns((Workout?)null);

		await Assert.ThrowsAsync<WorkoutNotFoundException>(() => _handler.HandleAsync(command));
	}

	[Fact]
	public async Task Should_Throw_Exception_User_Does_Not_Exist()
	{
		var command = Fixture.Create<SendWorkout>();

		_mockRepository
			.GetUser(Arg.Is<string>(y => y == command.RecipientUserId))
			.Returns((User)null!);

		await Assert.ThrowsAsync<ResourceNotFoundException>(() => _handler.HandleAsync(command));
	}

	[Fact]
	public async Task Should_Throw_Exception_Recipient_Sender_Equal()
	{
		var command = Fixture.Build<SendWorkout>()
			.With(x => x.RecipientUserId, "Golden God")
			.With(x => x.SenderUserId, "Golden God")
			.Create();

		await Assert.ThrowsAsync<MiscErrorException>(() => _handler.HandleAsync(command));
	}
}