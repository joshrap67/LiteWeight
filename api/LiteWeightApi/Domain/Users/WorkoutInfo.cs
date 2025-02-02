using Google.Cloud.Firestore;
using LiteWeightAPI.Domain.Converters;
using NodaTime;

namespace LiteWeightAPI.Domain.Users;

[FirestoreData]
public class WorkoutInfo
{
	[FirestoreProperty("workoutId")]
	public required string WorkoutId { get; set; }

	[FirestoreProperty("workoutName")]
	public required string WorkoutName { get; set; }
	
	[FirestoreProperty("currentDay")]
	public int CurrentDay { get; set; }

	[FirestoreProperty("currentWeek")]
	public int CurrentWeek { get; set; }

	[FirestoreProperty("lastSetAsCurrentUtc", ConverterType = typeof(InstantConverter))]
	public Instant LastSetAsCurrentUtc { get; set; }

	[FirestoreProperty("timesRestarted")]
	public int TimesRestarted { get; set; }

	[FirestoreProperty("averageWorkoutCompletion")]
	public double AverageWorkoutCompletion { get; set; }
}