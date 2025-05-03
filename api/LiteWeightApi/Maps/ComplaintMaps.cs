using LiteWeightAPI.Api.Complaints.Responses;
using LiteWeightAPI.Domain.Complaints;
using LiteWeightAPI.Utils;

namespace LiteWeightAPI.Maps;

public static class ComplaintMaps
{
	public static ComplaintResponse ToResponse(this Complaint complaint)
	{
		return new ComplaintResponse
		{
			Id = complaint.Id,
			Description = complaint.Description,
			ReportedUsername = complaint.ReportedUsername,
			ReportedUtc = ParsingUtils.ConvertInstantToString(complaint.ReportedUtc),
			ClaimantUserId = complaint.ClaimantUserId,
			ReportedUserId = complaint.ReportedUserId
		};
	}
}