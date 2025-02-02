using System.Text.Json;
using NodaTime;
using NodaTime.Serialization.SystemTextJson;

namespace LiteWeightAPI.Utils;

public static class JsonUtils
{
	private static readonly JsonSerializerOptions DefaultSerializerSettings = new JsonSerializerOptions
	{
		PropertyNamingPolicy = JsonNamingPolicy.CamelCase
	}.ConfigureForNodaTime(DateTimeZoneProviders.Tzdb);

	public static T? Deserialize<T>(string json)
	{
		return JsonSerializer.Deserialize<T>(json, DefaultSerializerSettings);
	}

	public static string Serialize<T>(T data)
	{
		return JsonSerializer.Serialize(data, DefaultSerializerSettings);
	}
}