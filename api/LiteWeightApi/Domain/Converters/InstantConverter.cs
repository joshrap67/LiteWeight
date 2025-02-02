using Google.Cloud.Firestore;
using LiteWeightAPI.Utils;
using NodaTime;

namespace LiteWeightAPI.Domain.Converters;

public class InstantConverter : IFirestoreConverter<Instant>
{
	public object ToFirestore(Instant value)
	{
		return value.ToString();
	}

	public Instant FromFirestore(object value)
	{
		return value switch
		{
			string timestamp => ParsingUtils.ParseStringToInstant(timestamp),
			null => throw new ArgumentNullException(nameof(value)),
			_ => throw new ArgumentException($"Unexpected data: {value.GetType()}")
		};
	}
}