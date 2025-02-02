using LiteWeightAPI.Errors.Exceptions.BaseExceptions;
using LiteWeightAPI.Errors.Responses;
using LiteWeightAPI.Utils;
using Microsoft.AspNetCore.Diagnostics;

namespace LiteWeightAPI.ExtensionMethods;

public static class AppBuilderExtensions
{
	public static void UseCustomExceptionHandling(this IApplicationBuilder applicationBuilder)
	{
		applicationBuilder.UseExceptionHandler(builder =>
		{
			// whenever an exception is thrown and not caught, it ends up here to be formatted properly and returned
			builder.Run(async context =>
			{
				var httpContextException = context.Features.Get<IExceptionHandlerFeature>();
				var exception = httpContextException?.Error;
				context.Response.ContentType = "application/json";

				switch (exception)
				{
					case BadRequestException bre:
						context.Response.StatusCode = StatusCodes.Status400BadRequest;
						await context.Response.WriteAsync(JsonUtils.Serialize(bre.FormattedResponse));
						break;
					case UnauthorizedException ue:
						context.Response.StatusCode = StatusCodes.Status401Unauthorized;
						await context.Response.WriteAsync(JsonUtils.Serialize(new UnauthorizedResponse
							{ Message = ue.Message }));
						break;
					case ForbiddenException fe:
						context.Response.StatusCode = StatusCodes.Status403Forbidden;
						await context.Response.WriteAsync(
							JsonUtils.Serialize(new ForbiddenResponse { Message = fe.Message }));
						break;
					case ResourceNotFoundException rnfe:
						context.Response.StatusCode = StatusCodes.Status404NotFound;
						await context.Response.WriteAsync(JsonUtils.Serialize(new ResourceNotFoundResponse
							{ Message = rnfe.FormattedMessage }));
						break;
					default:
						context.Response.StatusCode = StatusCodes.Status500InternalServerError;
						await context.Response.WriteAsync(JsonUtils.Serialize(new ServerErrorResponse
							{ Message = "The server has encountered an error" }));
						break;
				}
			});
		});
	}
}