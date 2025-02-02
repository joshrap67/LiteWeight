namespace LiteWeightAPI.Errors.Exceptions.BaseExceptions;

public class UnauthorizedException : Exception
{
    public UnauthorizedException() : base("Unauthorized access")
    {
    }
}