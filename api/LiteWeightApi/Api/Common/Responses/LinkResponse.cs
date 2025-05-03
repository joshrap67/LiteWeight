using System.ComponentModel.DataAnnotations;
using LiteWeightAPI.Imports;

namespace LiteWeightAPI.Api.Common.Responses;

public class LinkResponse
{
	/// <summary>
	/// Full url of the link. Suggested use case is a video of how to perform the exercise or pictures of muscles worked.
	/// </summary>
	/// <example>https://www.youtube.com/watch?v=rT7DgCr-3pg</example>
	[MaxLength(Globals.MaxUrlLength)]
	public string Url { get; set; } = null!;

	/// <summary>
	/// Optional label of the link.
	/// </summary>
	/// <example>5 min video</example>
	[MaxLength(Globals.MaxLabelLength)]
	public string? Label { get; set; }
}