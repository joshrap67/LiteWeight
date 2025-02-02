namespace LiteWeightAPI.Errors.Attributes.Setup;

[AttributeUsage(AttributeTargets.Method)]
public abstract class BaseErrorAttribute : Attribute
{
	protected BaseErrorAttribute(string errorType)
	{
		ErrorType = errorType;
	}

	public string ErrorType { get; }
}