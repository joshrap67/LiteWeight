using Google.Cloud.Firestore;
using LiteWeightAPI.Domain.Users;
using LiteWeightAPI.Domain.Workouts;

namespace LiteWeightAPI.Domain.ReceivedWorkouts;

[FirestoreData]
public class ReceivedWorkout
{
	// public ctor needed for firebase serialization
	public ReceivedWorkout()
	{
	}

	public ReceivedWorkout(Workout workout, string recipientId, string receivedWorkoutId, User sender)
	{
		Id = receivedWorkoutId;
		RecipientId = recipientId;
		WorkoutName = workout.Name;
		SenderId = sender.Id;
		SenderUsername = sender.Username;
		Routine = new ReceivedRoutine(workout.Routine, sender.Exercises);

		// preserve the focuses and video url of the exercises since recipient user might not have the same exercises
		DistinctExercises = new List<ReceivedWorkoutDistinctExercise>();
		var exerciseIdToExercise = sender.Exercises.ToDictionary(x => x.Id, x => x);
		var exercisesOfWorkout = workout.Routine.Weeks
			.SelectMany(x => x.Days)
			.SelectMany(x => x.Exercises)
			.DistinctBy(x => x.ExerciseId).ToList();
		foreach (var routineExercise in exercisesOfWorkout)
		{
			var ownedExercise = exerciseIdToExercise[routineExercise.ExerciseId];
			var receivedWorkoutExercise = new ReceivedWorkoutDistinctExercise(ownedExercise, ownedExercise.Name);
			DistinctExercises.Add(receivedWorkoutExercise);
		}
	}

	[FirestoreDocumentId]
	public string Id { get; set; }

	[FirestoreProperty("workoutName")]
	public string WorkoutName { get; set; }

	[FirestoreProperty("senderId")]
	public string SenderId { get; set; }

	[FirestoreProperty("senderUsername")]
	public string SenderUsername { get; set; }

	[FirestoreProperty("recipientId")]
	public string RecipientId { get; set; }

	[FirestoreProperty("routine")]
	public ReceivedRoutine Routine { get; set; }

	// on the surface this property seems unnecessary, but it's required since the sender user can change their exercises. Need to preserve the original values of the exercise
	[FirestoreProperty("distinctExercises")]
	public IList<ReceivedWorkoutDistinctExercise> DistinctExercises { get; set; }
}