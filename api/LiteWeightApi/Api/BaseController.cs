using LiteWeightAPI.Errors.Exceptions.BaseExceptions;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Filters;

namespace LiteWeightAPI.Api;

[Produces("application/json")]
[Authorize]
public class BaseController : Controller
{
	protected string CurrentUserId { get; private set; } = "";

	public override Task OnActionExecutionAsync(ActionExecutingContext context, ActionExecutionDelegate next)
	{
		var userIdClaim = HttpContext.User.Claims.ToList().FirstOrDefault(x => x.Type == "user_id");
		if (userIdClaim == null)
		{
			throw new ForbiddenException();
		}

		CurrentUserId = userIdClaim.Value;

		return base.OnActionExecutionAsync(context, next);
	}
}