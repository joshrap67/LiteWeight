using AutoMapper;
using LiteWeightAPI.Api.Complaints.Responses;
using LiteWeightAPI.Domain.Complaints;

namespace LiteWeightAPI.AutoMapper;

public class ComplaintMaps : Profile
{
	public ComplaintMaps()
	{
		CreateMap<Complaint, ComplaintResponse>();
	}
}