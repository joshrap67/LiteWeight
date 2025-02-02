using LiteWeightAPI.Errors.Attributes.Setup;

namespace LiteWeightAPI.Errors.Attributes;

public class MaxLimitAttribute : BaseErrorAttribute
{
	public MaxLimitAttribute() : base(ErrorTypes.MaxLimit)
	{
	}
}