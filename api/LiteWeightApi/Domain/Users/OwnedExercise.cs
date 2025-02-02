using Google.Cloud.Firestore;

namespace LiteWeightAPI.Domain.Users;

[FirestoreData]
public class OwnedExercise
{
	[FirestoreProperty("id")]
	public string Id { get; set; } = Guid.NewGuid().ToString();

	[FirestoreProperty("name")]
	public required string Name { get; set; }

	[FirestoreProperty("defaultWeight")]
	public double DefaultWeight { get; set; } // stored in lbs

	[FirestoreProperty("defaultSets")]
	public int DefaultSets { get; set; } = 3;

	[FirestoreProperty("defaultReps")]
	public int DefaultReps { get; set; } = 15;

	[FirestoreProperty("defaultDetails")]
	public string? DefaultDetails { get; set; }

	[FirestoreProperty("videoUrl")]
	public string? VideoUrl { get; set; }

	[FirestoreProperty("focuses")]
	public IList<string> Focuses { get; set; } = new List<string>();

	[FirestoreProperty("workouts")]
	public IList<OwnedExerciseWorkout> Workouts { get; set; } = new List<OwnedExerciseWorkout>();

	public void Update(string exerciseName, double defaultWeight, int defaultSets, int defaultReps,
		string? defaultDetails, string? videoUrl, IList<string> focuses)
	{
		Name = exerciseName;
		DefaultWeight = defaultWeight;
		DefaultSets = defaultSets;
		DefaultReps = defaultReps;
		DefaultDetails = defaultDetails;
		VideoUrl = videoUrl;
		Focuses = focuses;
	}
}