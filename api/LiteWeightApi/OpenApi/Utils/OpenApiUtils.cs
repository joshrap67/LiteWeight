namespace LiteWeightAPI.OpenApi.Utils;

using System.Reflection;
using Microsoft.AspNetCore.Mvc.Controllers;
using Microsoft.AspNetCore.OpenApi;
using Microsoft.OpenApi;

public static class OpenApiUtils
{
	public static MethodInfo? GetMethodInfo(OpenApiOperationTransformerContext context)
	{
		var actionDescriptor = context.Description.ActionDescriptor;
		MethodInfo? methodInfo = null;
		if (actionDescriptor is ControllerActionDescriptor controllerActionDescriptor)
		{
			methodInfo = controllerActionDescriptor.MethodInfo;
		}

		return methodInfo;
	}

	public static async Task<IOpenApiSchema?> TryAddSchema<T>(OpenApiOperationTransformerContext context,
		CancellationToken ctx)
	{
		var schemaId = typeof(T).Name;

		context.Document?.Components ??= new OpenApiComponents();
		context.Document?.Components?.Schemas ??= new Dictionary<string, IOpenApiSchema>();

		if (context.Document?.Components?.Schemas == null)
		{
			return null;
		}

		var schema = await context.GetOrCreateSchemaAsync(typeof(T), null, ctx);
		context.Document.Components.Schemas[schemaId] = schema;
		return new OpenApiSchemaReference(schemaId, context.Document);
	}
}