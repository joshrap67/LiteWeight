using LiteWeightAPI.Domain.ReceivedWorkouts;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Domain.Workouts;

namespace LiteWeightApiTests.TestHelpers;

public static class ReceivedWorkoutHelper
{
	private static readonly Fixture Fixture = new();

	public static ReceivedWorkout GetReceivedWorkout(string recipientId = null, string receivedWorkoutId = null)
	{
		var workout = Fixture.Create<Workout>();
		var workoutExerciseIds = workout.Routine.Weeks.SelectMany(x => x.Days).SelectMany(x => x.Exercises)
			.Select(x => x.ExerciseId).ToList();
		var exercises = workoutExerciseIds
			.Select(exerciseId => Fixture.Build<OwnedExercise>().With(x => x.Id, exerciseId).Create()).ToList();

		var sender = Fixture.Build<User>()
			.With(x => x.Exercises, exercises)
			.Create();

		return new ReceivedWorkout(workout, recipientId ?? Fixture.Create<string>(),
			receivedWorkoutId ?? Fixture.Create<string>(), sender);
	}
}