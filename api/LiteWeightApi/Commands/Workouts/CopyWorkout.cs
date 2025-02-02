using AutoMapper;
using LiteWeightAPI.Api.Self.Responses;
using LiteWeightAPI.Api.Workouts.Responses;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Domain.Workouts;
using LiteWeightAPI.Errors.Exceptions;
using LiteWeightAPI.Errors.Exceptions.BaseExceptions;
using LiteWeightAPI.Imports;
using LiteWeightAPI.Utils;
using NodaTime;

namespace LiteWeightAPI.Commands.Workouts;

public class CopyWorkout : ICommand<UserAndWorkoutResponse>
{
	public required string UserId { get; set; }
	public required string WorkoutId { get; set; }
	public required string Name { get; set; }
}

public class CopyWorkoutHandler : ICommandHandler<CopyWorkout, UserAndWorkoutResponse>
{
	private readonly IRepository _repository;
	private readonly IClock _clock;
	private readonly IMapper _mapper;

	public CopyWorkoutHandler(IRepository repository, IClock clock, IMapper mapper)
	{
		_repository = repository;
		_clock = clock;
		_mapper = mapper;
	}

	public async Task<UserAndWorkoutResponse> HandleAsync(CopyWorkout command)
	{
		var user = (await _repository.GetUser(command.UserId))!;
		var workoutToCopy = await _repository.GetWorkout(command.WorkoutId);

		if (workoutToCopy == null)
		{
			throw new ResourceNotFoundException("Workout");
		}

		ValidationUtils.EnsureWorkoutOwnership(user.Id, workoutToCopy);

		if (user.Workouts.Count > Globals.MaxFreeWorkouts && user.PremiumToken == null)
		{
			throw new MaxLimitException("Max amount of free workouts reached");
		}

		if (user.Workouts.Count > Globals.MaxWorkouts && user.PremiumToken != null)
		{
			throw new MaxLimitException("Maximum workouts exceeded");
		}

		ValidationUtils.ValidWorkoutName(command.Name, user);

		var newWorkoutId = Guid.NewGuid().ToString();
		var now = _clock.GetCurrentInstant();
		var newRoutine = workoutToCopy.Routine.Clone();
		var newWorkout = new Workout
		{
			Id = newWorkoutId,
			Name = command.Name,
			Routine = newRoutine,
			CreatorId = command.UserId,
			CreationUtc = now
		};

		user.Workouts.Add(new WorkoutInfo
		{
			WorkoutId = newWorkoutId,
			WorkoutName = newWorkout.Name
		});
		// update all the exercises that are now part of this workout
		WorkoutUtils.UpdateOwnedExercisesOnCreation(user, newWorkout, false);

		await _repository.ExecuteBatchWrite(
			workoutsToPut: new List<Workout> { newWorkout },
			usersToPut: new List<User> { user }
		);

		return new UserAndWorkoutResponse
		{
			User = _mapper.Map<UserResponse>(user),
			Workout = _mapper.Map<WorkoutResponse>(newWorkout)
		};
	}
}