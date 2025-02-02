using AutoMapper;
using LiteWeightAPI.Api.Exercises.Responses;
using LiteWeightAPI.Api.ReceivedWorkouts.Responses;
using LiteWeightAPI.Api.Self.Responses;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.ReceivedWorkouts;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Domain.Workouts;
using LiteWeightAPI.Errors.Exceptions;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;
using LiteWeightAPI.Imports;
using LiteWeightAPI.Utils;
using NodaTime;

namespace LiteWeightAPI.Commands.ReceivedWorkouts;

public class AcceptReceivedWorkout : ICommand<AcceptReceivedWorkoutResponse>
{
	public required string UserId { get; set; }
	public required string ReceivedWorkoutId { get; set; }
	public string? NewName { get; set; }
}

public class AcceptReceivedWorkoutHandler : ICommandHandler<AcceptReceivedWorkout, AcceptReceivedWorkoutResponse>
{
	private readonly IRepository _repository;
	private readonly IClock _clock;
	private readonly IMapper _mapper;

	public AcceptReceivedWorkoutHandler(IRepository repository, IClock clock, IMapper mapper)
	{
		_repository = repository;
		_clock = clock;
		_mapper = mapper;
	}

	public async Task<AcceptReceivedWorkoutResponse> HandleAsync(AcceptReceivedWorkout command)
	{
		var user = (await _repository.GetUser(command.UserId))!;
		var workoutToAccept = await _repository.GetReceivedWorkout(command.ReceivedWorkoutId);

		if (workoutToAccept == null)
		{
			throw new ResourceNotFoundException("Received workout");
		}

		ValidationUtils.EnsureReceivedWorkoutOwnership(user.Id, workoutToAccept);

		// lots of validation
		var newExercises = GetNewExercisesFromReceivedWorkout(workoutToAccept, user).ToList();
		if (user.PremiumToken == null && user.Workouts.Count >= Globals.MaxFreeWorkouts)
		{
			throw new MaxLimitException("Maximum workouts would be exceeded");
		}

		if (user.PremiumToken != null && user.Workouts.Count >= Globals.MaxWorkouts)
		{
			throw new MaxLimitException("Maximum workouts would be exceeded");
		}

		ValidationUtils.ValidWorkoutName(command.NewName ?? workoutToAccept.WorkoutName, user);

		var ownedExerciseNames = user.Exercises.Select(x => x.Name);
		var newExercisesNames = newExercises.Select(x => x.Name);

		var totalExercises = newExercisesNames.Union(ownedExerciseNames).ToList();
		if (user.PremiumToken == null && totalExercises.Count > Globals.MaxFreeExercises)
		{
			throw new MaxLimitException("Accepting this workout would put you above the amount of exercises allowed");
		}

		if (user.PremiumToken != null && totalExercises.Count > Globals.MaxExercises)
		{
			throw new MaxLimitException("Accepting this workout would put you above the amount of exercises allowed");
		}

		if (command.NewName != null)
		{
			workoutToAccept.WorkoutName = command.NewName;
		}

		foreach (var newExercise in newExercises)
		{
			user.Exercises.Add(newExercise);
		}

		var exerciseNameToId = user.Exercises.ToDictionary(x => x.Name, y => y.Id);
		var newWorkoutId = Guid.NewGuid().ToString();
		var now = _clock.GetCurrentInstant();
		var newWorkout = new Workout
		{
			Id = newWorkoutId,
			Name = workoutToAccept.WorkoutName,
			CreatorId = command.UserId,
			CreationUtc = now,
			Routine = new Routine(workoutToAccept.Routine, exerciseNameToId)
		};

		var workoutInfo = new WorkoutInfo
		{
			WorkoutName = newWorkout.Name,
			WorkoutId = newWorkoutId
		};
		user.Workouts.Add(workoutInfo);
		user.ReceivedWorkouts.RemoveAll(x => x.ReceivedWorkoutId == command.ReceivedWorkoutId);
		WorkoutUtils.UpdateOwnedExercisesOnCreation(user, newWorkout, false);

		await _repository.ExecuteBatchWrite(
			workoutsToPut: new List<Workout> { newWorkout },
			usersToPut: new List<User> { user },
			receivedWorkoutsToDelete: new List<ReceivedWorkout> { workoutToAccept }
		);

		return new AcceptReceivedWorkoutResponse
		{
			NewWorkoutInfo = _mapper.Map<WorkoutInfoResponse>(workoutInfo),
			UserExercises = _mapper.Map<IList<OwnedExerciseResponse>>(user.Exercises)
		};
	}

	private static List<OwnedExercise> GetNewExercisesFromReceivedWorkout(ReceivedWorkout? receivedWorkout, User? user)
	{
		if (receivedWorkout == null || user == null)
		{
			return [];
		}

		var newExercises = new List<OwnedExercise>();
		var receivedExerciseNames = receivedWorkout.Routine.Weeks
			.SelectMany(x => x.Days)
			.SelectMany(x => x.Exercises)
			.Select(x => x.ExerciseName)
			.ToHashSet();

		var ownedExerciseNames = user.Exercises.Select(x => x.Name).ToHashSet();

		receivedExerciseNames.ExceptWith(ownedExerciseNames);

		foreach (var exerciseName in receivedExerciseNames)
		{
			// for each of the exercises that the user doesn't own, make a new entry for them
			var receivedExercises = receivedWorkout.DistinctExercises.First(x => x.ExerciseName == exerciseName);
			var ownedExercise = new OwnedExercise
			{
				Name = exerciseName,
				Focuses = receivedExercises.Focuses,
				VideoUrl = receivedExercises.VideoUrl
			};
			newExercises.Add(ownedExercise);
		}

		return newExercises;
	}
}