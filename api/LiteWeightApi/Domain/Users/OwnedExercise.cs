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

	[FirestoreProperty("links")]
	public IList<Link> Links { get; set; } = new List<Link>();

	[FirestoreProperty("notes")]
	public string? Notes { get; set; }

	[FirestoreProperty("focuses")]
	public IList<string> Focuses { get; set; } = new List<string>();

	[FirestoreProperty("workouts")]
	public IList<OwnedExerciseWorkout> Workouts { get; set; } = new List<OwnedExerciseWorkout>();

	public void Update(string exerciseName, double defaultWeight, int defaultSets, int defaultReps,
		IList<Link> links, string? notes, IList<string> focuses)
	{
		Name = exerciseName;
		DefaultWeight = defaultWeight;
		DefaultSets = defaultSets;
		DefaultReps = defaultReps;
		Links = links;
		Focuses = focuses;
		Notes = notes;
	}
}