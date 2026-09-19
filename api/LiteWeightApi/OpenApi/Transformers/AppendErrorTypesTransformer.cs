using LiteWeightAPI.Errors.Attributes.Setup;
using LiteWeightAPI.Errors.Responses;
using LiteWeightAPI.OpenApi.Utils;
using Microsoft.AspNetCore.OpenApi;
using Microsoft.OpenApi;

namespace LiteWeightAPI.OpenApi.Transformers;

public class AppendErrorTypesTransformer : IOpenApiOperationTransformer
{
	/// <summary>
	/// Automatically appends error types to the end of an operation description if the controller has any error attributes
	/// </summary>
	public async Task TransformAsync(OpenApiOperation operation, OpenApiOperationTransformerContext context,
		CancellationToken ctx)
	{
		var methodInfo = OpenApiUtils.GetMethodInfo(context);
		if (methodInfo == null)
		{
			return;
		}

		var attributes =
			(IEnumerable<BaseErrorAttribute>)Attribute.GetCustomAttributes(methodInfo, typeof(BaseErrorAttribute));

		var errors = attributes
			.Select(x => x.ErrorType)
			.ToList();
		if (errors.Count == 0)
		{
			return;
		}

		errors.Sort(StringComparer.InvariantCulture);
		operation.Description +=
			$"\n\n" +
			$"> [!caution]" +
			$"\n" +
			$"Potential 400 Errors: {string.Join(", ", errors)}";
		operation.Responses?[StatusCodes.Status400BadRequest.ToString()] = new OpenApiResponse
		{
			Description = "Bad Request",
			Content = new Dictionary<string, OpenApiMediaType>
			{
				["application/json"] = new()
				{
					Schema = await OpenApiUtils.TryAddSchema<BadRequestResponse>(context, ctx)
				}
			}
		};
	}
}