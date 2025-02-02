using System.Reflection;
using AutoMapper;
using LiteWeightAPI.Api.Workouts;

namespace LiteWeightApiTests.Config;

public class AutoMapperTests
{
	[Fact]
	public void AutoMapperConfigurationValid()
	{
		var assembliesToTest = new List<Assembly> { typeof(WorkoutsController).GetTypeInfo().Assembly };
		var config = new MapperConfiguration(config => config.AddMaps(assembliesToTest));
		config.AssertConfigurationIsValid();
	}
}