using AutoMapper;
using LiteWeightAPI.Api.Exercises;

namespace LiteWeightApiTests.TestHelpers;

public static class DependencyHelper
{
	public static IMapper GetMapper()
	{
		var configuration = new MapperConfiguration(cfg => { cfg.AddMaps(typeof(ExercisesController)); });
		var mapper = new Mapper(configuration);
		return mapper;
	}
}