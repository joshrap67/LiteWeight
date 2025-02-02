using AutoMapper;
using LiteWeightAPI.Utils;
using NodaTime;

namespace LiteWeightAPI.AutoMapper;

public class SharedMaps : Profile
{
	public SharedMaps()
	{
		CreateMap<string, LocalDate>().ConvertUsing(x => ParsingUtils.ParseStringToLocalDate(x));
		CreateMap<Instant, string>().ConvertUsing(x => ParsingUtils.ConvertInstantToString(x));
		CreateMap<LocalDate, string>().ConvertUsing(x => ParsingUtils.ConvertLocalDateToString(x));
	}
}