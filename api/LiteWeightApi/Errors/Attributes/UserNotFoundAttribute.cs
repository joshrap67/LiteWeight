using LiteWeightAPI.Errors.Attributes.Setup;

namespace LiteWeightAPI.Errors.Attributes;

public class UserNotFoundAttribute : BaseErrorAttribute
{
	public UserNotFoundAttribute() : base(ErrorTypes.UserNotFound)
	{
	}
}