using LiteWeightAPI.Errors.Exceptions.BaseExceptions;

namespace LiteWeightAPI.Middleware;

public class EmailVerifiedMiddleware
{
	private readonly RequestDelegate _next;

	public EmailVerifiedMiddleware(RequestDelegate next)
	{
		_next = next;
	}

	public async Task InvokeAsync(HttpContext context)
	{
		var emailVerified = context.User.Claims.ToList().FirstOrDefault(x => x.Type == "email_verified");
		if (emailVerified == null || !bool.Parse(emailVerified.Value))
		{
			throw new ForbiddenException("Email not verified");
		}

		await _next(context);
	}
}