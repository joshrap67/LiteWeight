using LiteWeightAPI.Errors.Responses;
using LiteWeightAPI.OpenApi.Utils;
using Microsoft.AspNetCore.OpenApi;
using Microsoft.OpenApi;

namespace LiteWeightAPI.OpenApi.Transformers;

public class DefaultResponsesTransformer : IOpenApiOperationTransformer
{
	public async Task TransformAsync(OpenApiOperation operation, OpenApiOperationTransformerContext context,
		CancellationToken ctx)
	{
		// ensure every operation in spec has these types of responses since every operation can return these. Don't set a value if an operation explicitly set a response.
		await TryAddResponse<UnauthorizedResponse>(StatusCodes.Status401Unauthorized, "Authentication Error", operation,
			context, ctx);
		await TryAddResponse<ForbiddenResponse>(StatusCodes.Status403Forbidden, "Authorization Error", operation,
			context, ctx);
		await TryAddResponse<TooManyRequestsResponse>(StatusCodes.Status429TooManyRequests, "Too many requests",
			operation, context, ctx);
		await TryAddResponse<ServerErrorResponse>(StatusCodes.Status500InternalServerError, "Server Error", operation,
			context, ctx);

		if (operation.Parameters != null && operation.Parameters.Any(p => p.In == ParameterLocation.Path))
		{
			await TryAddResponse<ResourceNotFoundResponse>(StatusCodes.Status404NotFound, "Not Found", operation,
				context, ctx);
		}
	}

	private static async Task TryAddResponse<T>(int statusCode, string description, OpenApiOperation operation,
		OpenApiOperationTransformerContext context, CancellationToken ctx)
	{
		const string contentType = "application/json";
		if (operation.Responses != null && !operation.Responses.ContainsKey(statusCode.ToString()))
		{
			operation.Responses[statusCode.ToString()] = new OpenApiResponse
			{
				Description = description,
				Content = new Dictionary<string, OpenApiMediaType>
				{
					[contentType] = new() { Schema = await OpenApiUtils.TryAddSchema<T>(context, ctx) }
				}
			};
		}
	}
}