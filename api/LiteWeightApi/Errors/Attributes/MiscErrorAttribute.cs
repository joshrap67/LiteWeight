using LiteWeightAPI.Errors.Attributes.Setup;

namespace LiteWeightAPI.Errors.Attributes;

public class MiscErrorAttribute : BaseErrorAttribute
{
	public MiscErrorAttribute() : base(ErrorTypes.MiscError)
	{
	}
}