namespace LiteWeightAPI.Api.Complaints.Responses;

public class ComplaintResponse
{
	/// <summary>
	/// Id of the complaint.
	/// </summary>
	/// <example>29062e23-fa61-4977-af93-aa4fdb177247</example>
	public string Id { get; set; } = null!;

	/// <summary>
	/// Description of the complaint.
	/// </summary>
	/// <example>Inappropriate workout name.</example>
	public string Description { get; set; } = null!;

	/// <summary>
	/// User id of the user who made the complaint.
	/// </summary>
	/// <example>3f96d8c2-127c-4605-8272-003630d8c1a1</example>
	public string ClaimantUserId { get; set; } = null!;

	/// <summary>
	/// User id of the user who was reported.
	/// </summary>
	/// <example>b36291c6-19ee-4bd0-b1f2-1d092a2e831e</example>
	public string ReportedUserId { get; set; } = null!;

	/// <summary>
	/// Username of the user who was reported. Note this is the username at the time of being reported.
	/// </summary>
	/// <example>pepe_silvia</example>
	public string ReportedUsername { get; set; } = null!;

	/// <summary>
	/// Timestamp of when the complaint was created (UTC).
	/// </summary>
	/// <example>2023-05-20T08:43:44.685341Z</example>
	public string ReportedUtc { get; set; } = null!;
}