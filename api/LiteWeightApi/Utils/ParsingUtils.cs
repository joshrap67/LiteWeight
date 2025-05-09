using System.Globalization;
using NodaTime;
using NodaTime.Text;

namespace LiteWeightAPI.Utils;

public static class ParsingUtils
{
	public static string ConvertInstantToString(Instant instant)
	{
		return instant.ToString("g", CultureInfo.InvariantCulture);
	}

	public static Instant ParseStringToInstant(string isoString)
	{
		var result = InstantPattern.General.Parse(isoString);
		if (!result.Success)
		{
			throw new Exception("Date not in proper format utc format");
		}

		return result.Value;
	}
}