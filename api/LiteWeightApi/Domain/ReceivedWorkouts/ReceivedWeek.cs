using Google.Cloud.Firestore;

namespace LiteWeightAPI.Domain.ReceivedWorkouts;

[FirestoreData]
public class ReceivedWeek
{
	[FirestoreProperty("days")]
	public IList<ReceivedDay> Days { get; set; } = new List<ReceivedDay>();

	public void AppendDay(ReceivedDay receivedDay)
	{
		Days.Add(receivedDay);
	}
}