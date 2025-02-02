using Google.Cloud.Firestore;

namespace LiteWeightAPI.Domain.Users;

[FirestoreData]
public class UserSettings
{
	[FirestoreProperty("privateAccount")]
	public bool PrivateAccount { get; set; }

	[FirestoreProperty("updateDefaultWeightOnSave")]
	public bool UpdateDefaultWeightOnSave { get; set; }

	[FirestoreProperty("updateDefaultWeightOnRestart")]
	public bool UpdateDefaultWeightOnRestart { get; set; }

	[FirestoreProperty("metricUnits")]
	public bool MetricUnits { get; set; }

	public void Update(bool privateAccount, bool updateDefaultWeightOnSave, bool updateDefaultWeightOnRestart,
		bool metricUnits)
	{
		PrivateAccount = privateAccount;
		UpdateDefaultWeightOnSave = updateDefaultWeightOnSave;
		UpdateDefaultWeightOnRestart = updateDefaultWeightOnRestart;
		MetricUnits = metricUnits;
	}
}