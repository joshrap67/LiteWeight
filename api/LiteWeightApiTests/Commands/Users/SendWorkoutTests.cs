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
	private readonly Mock<IRepository> _mockRepository;
	private readonly Mock<IPushNotificationService> _mockPushNotificationService;

	public SendWorkoutTests()
	{
		_mockRepository = new Mock<IRepository>();
		_mockPushNotificationService = new Mock<IPushNotificationService>();
		var clock = new Mock<IClock>().Object;
		var statisticsService = new Mock<IStatisticsService>().Object;
		_handler = new SendWorkoutHandler(_mockRepository.Object, _mockPushNotificationService.Object, clock,
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
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.RecipientUserId)))
			.ReturnsAsync(recipientUser);

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.SenderUserId)))
			.ReturnsAsync(senderUser);

		_mockRepository
			.Setup(x => x.GetWorkout(It.Is<string>(y => y == command.WorkoutId)))
			.ReturnsAsync(workout);

		var response = await _handler.HandleAsync(command);
		Assert.NotNull(response);
		_mockPushNotificationService.Verify(
			x => x.SendReceivedWorkoutPushNotification(It.IsAny<User>(), It.IsAny<ReceivedWorkoutInfo>()), Times.Once);
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
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.RecipientUserId)))
			.ReturnsAsync(recipientUser);

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.SenderUserId)))
			.ReturnsAsync(senderUser);

		_mockRepository
			.Setup(x => x.GetWorkout(It.Is<string>(y => y == command.WorkoutId)))
			.ReturnsAsync(workout);

		var response = await _handler.HandleAsync(command);
		Assert.NotNull(response);
		_mockPushNotificationService.Verify(
			x => x.SendReceivedWorkoutPushNotification(It.IsAny<User>(), It.IsAny<ReceivedWorkoutInfo>()), Times.Once);
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
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.RecipientUserId)))
			.ReturnsAsync(recipientUser);

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.SenderUserId)))
			.ReturnsAsync(senderUser);

		_mockRepository
			.Setup(x => x.GetWorkout(It.Is<string>(y => y == command.WorkoutId)))
			.ReturnsAsync(workout);

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
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.RecipientUserId)))
			.ReturnsAsync(recipientUser);

		_mockRepository
			.Setup(x => x.GetWorkout(It.Is<string>(y => y == command.WorkoutId)))
			.ReturnsAsync(workout);

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
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.RecipientUserId)))
			.ReturnsAsync(recipientUser);

		_mockRepository
			.Setup(x => x.GetWorkout(It.Is<string>(y => y == command.WorkoutId)))
			.ReturnsAsync(workout);

		await Assert.ThrowsAsync<ResourceNotFoundException>(() => _handler.HandleAsync(command));
	}

	[Fact]
	public async Task Should_Throw_Exception_Missing_Permissions_Workout()
	{
		var command = Fixture.Create<SendWorkout>();
		var user = Fixture.Create<User>();

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.RecipientUserId)))
			.ReturnsAsync(user);

		_mockRepository
			.Setup(x => x.GetWorkout(It.Is<string>(y => y == command.WorkoutId)))
			.ReturnsAsync(Fixture.Create<Workout>());

		await Assert.ThrowsAsync<ForbiddenException>(() => _handler.HandleAsync(command));
	}

	[Fact]
	public async Task Should_Throw_Exception_Referenced_Workout_Not_Found()
	{
		var command = Fixture.Create<SendWorkout>();

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.RecipientUserId)))
			.ReturnsAsync(Fixture.Create<User>());

		_mockRepository
			.Setup(x => x.GetWorkout(It.Is<string>(y => y == command.WorkoutId)))
			.ReturnsAsync((Workout?)null);

		await Assert.ThrowsAsync<WorkoutNotFoundException>(() => _handler.HandleAsync(command));
	}

	[Fact]
	public async Task Should_Throw_Exception_User_Does_Not_Exist()
	{
		var command = Fixture.Create<SendWorkout>();

		_mockRepository
			.Setup(x => x.GetUser(It.Is<string>(y => y == command.RecipientUserId)))
			.ReturnsAsync((User)null!);

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