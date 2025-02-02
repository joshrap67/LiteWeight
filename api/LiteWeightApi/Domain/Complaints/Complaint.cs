using Google.Cloud.Firestore;
using LiteWeightAPI.Domain.Converters;
using NodaTime;

namespace LiteWeightAPI.Domain.Complaints;

[FirestoreData]
public class Complaint
{
	[FirestoreDocumentId]
	public string Id { get; set; } = Guid.NewGuid().ToString();

	[FirestoreProperty("claimantUserId")]
	public required string ClaimantUserId { get; set; }

	[FirestoreProperty("reportedUserId")]
	public required string ReportedUserId { get; set; }

	[FirestoreProperty("reportedUsername")]
	public required string ReportedUsername { get; set; }

	[FirestoreProperty("description")]
	public required string Description { get; set; }

	// in the future could add profile picture if wanting to persist a potentially offensive profile picture

	[FirestoreProperty("reportedUtc", ConverterType = typeof(InstantConverter))]
	public Instant ReportedUtc { get; set; }
}