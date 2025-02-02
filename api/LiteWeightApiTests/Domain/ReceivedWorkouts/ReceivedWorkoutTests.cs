using LiteWeightAPI.Domain.ReceivedWorkouts;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Domain.Workouts;

namespace LiteWeightApiTests.Domain.ReceivedWorkouts;

public class ReceivedWorkoutTests : BaseTest
{
	[Fact]
	public void Ctor_Success()
	{
		var workout = Fixture.Create<Workout>();
		var distinctExercises = workout.Routine.Weeks
			.SelectMany(x => x.Days)
			.SelectMany(x => x.Exercises)
			.ToList();
		var ownedExercises = distinctExercises
			.Select(x => x.ExerciseId)
			.Select(exerciseId => Fixture.Build<OwnedExercise>().With(x => x.Id, exerciseId).Create())
			.ToList();

		var sender = Fixture.Build<User>().With(x => x.Exercises, ownedExercises).Create();
		var receivedWorkoutId = Fixture.Create<string>();
		var recipientId = Fixture.Create<string>();

		var receivedWorkout = new ReceivedWorkout(workout, recipientId, receivedWorkoutId, sender);

		Assert.Equal(recipientId, receivedWorkout.RecipientId);
		Assert.Equal(sender.Id, receivedWorkout.SenderId);
		Assert.Equal(sender.Username, receivedWorkout.SenderUsername);
		Assert.Equal(workout.Name, receivedWorkout.WorkoutName);
		Assert.Equal(receivedWorkoutId, receivedWorkout.Id);

		var exerciseNameToExercise = ownedExercises.ToDictionary(x => x.Name, x => x);
		foreach (var distinctExercise in receivedWorkout.DistinctExercises)
		{
			var ownedExercise = exerciseNameToExercise[distinctExercise.ExerciseName];
			Assert.Equal(ownedExercise.VideoUrl, distinctExercise.VideoUrl);
			Assert.Equal(ownedExercise.Focuses, distinctExercise.Focuses);
		}

		for (var i = 0; i < receivedWorkout.Routine.Weeks.Count; i++)
		{
			var expectedWeek = workout.Routine.Weeks[i];
			var actualWeek = receivedWorkout.Routine.Weeks[i];
			Assert.Equal(expectedWeek.Days.Count, actualWeek.Days.Count);
			for (var j = 0; j < actualWeek.Days.Count; j++)
			{
				var expectedDay = expectedWeek.Days[j];
				var actualDay = actualWeek.Days[j];
				Assert.Equal(expectedDay.Tag, actualDay.Tag);
				Assert.Equal(expectedDay.Exercises.Count, actualDay.Exercises.Count);
				for (var k = 0; k < actualDay.Exercises.Count; k++)
				{
					var expectedExercise = expectedDay.Exercises[k];
					var actualExercise = actualDay.Exercises[k];
					var id = exerciseNameToExercise[actualExercise.ExerciseName].Id;
					Assert.Equal(expectedExercise.ExerciseId, id);
					Assert.Equal(expectedExercise.Weight, actualExercise.Weight);
					Assert.Equal(expectedExercise.Sets, actualExercise.Sets);
					Assert.Equal(expectedExercise.Reps, actualExercise.Reps);
					Assert.Equal(expectedExercise.Details, actualExercise.Details);
				}
			}
		}
	}
}