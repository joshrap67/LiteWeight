using AutoMapper;
using LiteWeightAPI.Api.Self.Responses;
using LiteWeightAPI.Api.Workouts.Responses;
using LiteWeightAPI.Domain;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Domain.Workouts;
using LiteWeightAPI.Errors.Exceptions;
using LiteWeightAPI.Imports;
using LiteWeightAPI.Utils;
using NodaTime;

namespace LiteWeightAPI.Commands.Workouts;

public class CreateWorkout : ICommand<UserAndWorkoutResponse>
{
	public required string UserId { get; set; }
	public required string Name { get; set; }
	public required SetRoutine Routine { get; set; }
	public bool SetAsCurrentWorkout { get; set; }
}

public class CreateWorkoutHandler : ICommandHandler<CreateWorkout, UserAndWorkoutResponse>
{
	private readonly IRepository _repository;
	private readonly IClock _clock;
	private readonly IMapper _mapper;

	public CreateWorkoutHandler(IRepository repository, IClock clock, IMapper mapper)
	{
		_repository = repository;
		_clock = clock;
		_mapper = mapper;
	}

	public async Task<UserAndWorkoutResponse> HandleAsync(CreateWorkout command)
	{
		var user = (await _repository.GetUser(command.UserId))!;
		var routine = _mapper.Map<Routine>(command.Routine);

		var workoutId = Guid.NewGuid().ToString();

		if (user.Workouts.Count >= Globals.MaxFreeWorkouts && user.PremiumToken == null)
		{
			throw new MaxLimitException("Max amount of free workouts reached");
		}

		if (user.Workouts.Count >= Globals.MaxWorkouts && user.PremiumToken != null)
		{
			throw new MaxLimitException("Maximum workouts exceeded");
		}

		ValidationUtils.ValidWorkoutName(command.Name, user);

		var now = _clock.GetCurrentInstant();
		var newWorkout = new Workout
		{
			Id = workoutId,
			Name = command.Name.Trim(),
			CreationUtc = now,
			CreatorId = command.UserId,
			Routine = routine
		};

		var workoutInfo = new WorkoutInfo
		{
			WorkoutId = workoutId,
			WorkoutName = command.Name
		};
		user.Workouts.Add(workoutInfo);
		if (command.SetAsCurrentWorkout)
		{
			user.CurrentWorkoutId = workoutId;
			workoutInfo.LastSetAsCurrentUtc = now;
		}

		// update all the exercises that are now part of this workout
		WorkoutUtils.UpdateOwnedExercisesOnCreation(user, newWorkout, true);

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