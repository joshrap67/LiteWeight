using System.ComponentModel.DataAnnotations;
using LiteWeightAPI.Imports;

namespace LiteWeightAPI.Api.Users.Requests;

public class ReportUserRequest
{
	/// <summary>
	/// Description of the complaint.
	/// </summary>
	/// <example>Inappropriate username.</example>
	[Required]
	[MaxLength(Globals.MaxReportDescription)]
	public string Description { get; set; } = null!;
}