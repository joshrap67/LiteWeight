using LiteWeightAPI.Errors.Attributes.Setup;

namespace LiteWeightAPI.Errors.Attributes;

public class AlreadyExistsAttribute : BaseErrorAttribute
{
	public AlreadyExistsAttribute() : base(ErrorTypes.AlreadyExists)
	{
	}
}