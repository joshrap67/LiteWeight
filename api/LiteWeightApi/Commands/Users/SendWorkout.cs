using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.ReceivedWorkouts;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Errors.Exceptions;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;
using LiteWeightAPI.Imports;
using LiteWeightAPI.Services;
using LiteWeightAPI.Utils;
using NodaTime;

namespace LiteWeightAPI.Commands.Users;

public class SendWorkout : ICommand<string>
{
	public required string SenderUserId { get; set; }
	public required string RecipientUserId { get; set; }
	public required string WorkoutId { get; set; }
}

public class SendWorkoutHandler : ICommandHandler<SendWorkout, string>
{
	private readonly IRepository _repository;
	private readonly IPushNotificationService _pushNotificationService;
	private readonly IClock _clock;
	private readonly IStatisticsService _statisticsService;

	public SendWorkoutHandler(IRepository repository, IPushNotificationService pushNotificationService, IClock clock,
		IStatisticsService statisticsService)
	{
		_repository = repository;
		_pushNotificationService = pushNotificationService;
		_clock = clock;
		_statisticsService = statisticsService;
	}

	public async Task<string> HandleAsync(SendWorkout command)
	{
		var senderUser = (await _repository.GetUser(command.SenderUserId))!;
		var recipientUser = await _repository.GetUser(command.RecipientUserId);
		var workoutToSend = await _repository.GetWorkout(command.WorkoutId);

		if (command.SenderUserId == command.RecipientUserId)
		{
			throw new MiscErrorException("Cannot send workout to yourself");
		}

		if (recipientUser == null)
		{
			throw new ResourceNotFoundException("User");
		}

		if (workoutToSend == null)
		{
			throw new WorkoutNotFoundException("Referenced workout does not exist");
		}

		ValidationUtils.EnsureWorkoutOwnership(command.SenderUserId, workoutToSend);

		if (recipientUser.Settings.PrivateAccount &&
		    recipientUser.Friends.All(x => x.UserId != command.SenderUserId))
		{
			throw new ResourceNotFoundException("User");
		}

		if (recipientUser.ReceivedWorkouts.Count >= Globals.MaxReceivedWorkouts)
		{
			throw new MaxLimitException($"{command.RecipientUserId} has too many received workouts");
		}

		if (senderUser.PremiumToken == null && senderUser.WorkoutsSent >= Globals.MaxFreeWorkoutsSent)
		{
			throw new MaxLimitException("You have reached the maximum number of workouts that you can send");
		}

		var receivedWorkoutId = Guid.NewGuid().ToString();
		var receivedWorkoutInfo = new ReceivedWorkoutInfo
		{
			ReceivedWorkoutId = receivedWorkoutId,
			SenderId = command.SenderUserId,
			SenderUsername = senderUser.Username,
			SenderProfilePicture = senderUser.ProfilePicture,
			WorkoutName = workoutToSend.Name,
			ReceivedUtc = _clock.GetCurrentInstant(),
			TotalDays = workoutToSend.Routine.TotalNumberOfDays,
			MostFrequentFocus = _statisticsService
				.FindMostFrequentFocus(senderUser.Exercises.ToDictionary(x => x.Id, x => x), workoutToSend.Routine)
		};
		recipientUser.ReceivedWorkouts.Add(receivedWorkoutInfo);
		senderUser.WorkoutsSent++;

		var receivedWorkout =
			new ReceivedWorkout(workoutToSend, command.RecipientUserId, receivedWorkoutId, senderUser);

		await _repository.ExecuteBatchWrite(
			usersToPut: new List<User> { senderUser, recipientUser },
			receivedWorkoutsToPut: new List<ReceivedWorkout> { receivedWorkout }
		);

		await _pushNotificationService.SendReceivedWorkoutPushNotification(recipientUser, receivedWorkoutInfo);

		return receivedWorkoutId;
	}
}