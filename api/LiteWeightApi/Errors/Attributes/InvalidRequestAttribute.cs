using LiteWeightAPI.Errors.Attributes.Setup;

namespace LiteWeightAPI.Errors.Attributes;

public class InvalidRequestAttribute : BaseErrorAttribute
{
    public InvalidRequestAttribute() : base(ErrorTypes.InvalidRequest)
    {
    }
}