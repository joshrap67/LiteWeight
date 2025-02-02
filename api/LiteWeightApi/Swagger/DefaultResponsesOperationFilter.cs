using LiteWeightAPI.Errors.Responses;
using Microsoft.AspNetCore.Mvc;
using Microsoft.OpenApi.Models;
using Swashbuckle.AspNetCore.SwaggerGen;

namespace LiteWeightAPI.Swagger;

public class DefaultResponsesOperationFilter : IOperationFilter
{
	public void Apply(OpenApiOperation operation, OperationFilterContext context)
	{
		const string contentType = "application/json";
		// ensure every operation in swagger spec has these types of responses since every operation can return these
		operation.Responses[StatusCodes.Status401Unauthorized.ToString()] =
			new OpenApiResponse
			{
				Description = "Authentication Error",
				Content = new Dictionary<string, OpenApiMediaType>
				{
					[contentType] = new()
					{
						Schema = context.SchemaGenerator.GenerateSchema(typeof(UnauthorizedResponse),
							context.SchemaRepository)
					}
				}
			};

		operation.Responses[StatusCodes.Status403Forbidden.ToString()] =
			new OpenApiResponse
			{
				Description = "Authorization Error",
				Content = new Dictionary<string, OpenApiMediaType>
				{
					[contentType] = new()
					{
						Schema = context.SchemaGenerator.GenerateSchema(typeof(ForbiddenResponse),
							context.SchemaRepository)
					}
				}
			};

		operation.Responses[StatusCodes.Status429TooManyRequests.ToString()] =
			new OpenApiResponse
			{
				Description = "Too many requests",
				Content = new Dictionary<string, OpenApiMediaType>
				{
					[contentType] = new()
					{
						Schema = context.SchemaGenerator.GenerateSchema(typeof(TooManyRequestsResponse),
							context.SchemaRepository)
					}
				}
			};

		operation.Responses[StatusCodes.Status500InternalServerError.ToString()] =
			new OpenApiResponse
			{
				Description = "Server Error",
				Content = new Dictionary<string, OpenApiMediaType>
				{
					[contentType] = new()
					{
						Schema = context.SchemaGenerator.GenerateSchema(typeof(ServerErrorResponse),
							context.SchemaRepository)
					}
				}
			};

		context.ApiDescription.TryGetMethodInfo(out var methodInfo);
		if (methodInfo == null)
		{
			return;
		}

		// if any api method is explicitly defined to return a specific HTTP code, add the formatted response to the swagger spec
		var attributes = (IEnumerable<ProducesResponseTypeAttribute>)Attribute.GetCustomAttributes(methodInfo,
			typeof(ProducesResponseTypeAttribute));
		foreach (var attribute in attributes)
		{
			switch (attribute.StatusCode)
			{
				case StatusCodes.Status400BadRequest:
					operation.Responses[StatusCodes.Status400BadRequest.ToString()] =
						new OpenApiResponse
						{
							Description = "Bad Request",
							Content = new Dictionary<string, OpenApiMediaType>
							{
								[contentType] = new()
								{
									Schema = context.SchemaGenerator.GenerateSchema(typeof(BadRequestResponse),
										context.SchemaRepository)
								}
							}
						};
					break;
				case StatusCodes.Status404NotFound:
					operation.Responses[StatusCodes.Status404NotFound.ToString()] =
						new OpenApiResponse
						{
							Description = "Not Found",
							Content = new Dictionary<string, OpenApiMediaType>
							{
								[contentType] = new()
								{
									Schema = context.SchemaGenerator.GenerateSchema(typeof(ResourceNotFoundResponse),
										context.SchemaRepository)
								}
							}
						};
					break;
			}
		}
	}
}