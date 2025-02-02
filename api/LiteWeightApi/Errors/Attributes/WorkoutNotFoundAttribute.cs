using LiteWeightAPI.Errors.Attributes.Setup;

namespace LiteWeightAPI.Errors.Attributes;

public class WorkoutNotFoundAttribute : BaseErrorAttribute
{
	public WorkoutNotFoundAttribute() : base(ErrorTypes.WorkoutNotFound)
	{
	}
}