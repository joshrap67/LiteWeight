using AutoMapper;
using LiteWeightAPI.Api.Exercises;

namespace LiteWeightApiTests;

public class BaseTest
{
	protected readonly Fixture Fixture = new();

	protected static IMapper Mapper
	{
		get
		{
			var configuration = new MapperConfiguration(cfg => { cfg.AddMaps(typeof(ExercisesController)); });
			var mapper = new Mapper(configuration);
			return mapper;
		}
	}
}