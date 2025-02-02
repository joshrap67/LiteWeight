using System.ComponentModel.DataAnnotations;
using LiteWeightAPI.Imports;

namespace LiteWeightAPI.Api.Self.Requests;

public class CreateSelfRequest
{
	/// <summary>
	/// Username of the new user - must be unique. Note that the username will be converted to lowercase.
	/// </summary>
	/// <example>randy_bo_bandy</example>
	[Required]
	[MaxLength(Globals.MaxUsernameLength)]
	public string Username { get; set; } = null!;

	/// <summary>
	/// Base 64 encoding of the image to upload. If not specified, then the user will have a default profile picture.
	/// </summary>
	/// <example>iVBORw0KGgoAAAANSUhEUgAAAlgAAAJ</example>
	public byte[]? ProfilePictureData { get; set; }

	/// <summary>
	/// Should the created user have metric units enabled?
	/// </summary>
	public bool MetricUnits { get; set; }
}