using LiteWeightAPI.Imports;
using LiteWeightAPI.OpenApi.Utils;
using Microsoft.AspNetCore.OpenApi;
using Microsoft.OpenApi;

namespace LiteWeightAPI.OpenApi.Transformers;

public class AppendPushNotificationIndicatorTransformer : IOpenApiOperationTransformer
{
	/// <summary>
	/// Automatically appends an indicator that successful completion of the action will send a push notification
	/// </summary>
	public Task TransformAsync(OpenApiOperation operation, OpenApiOperationTransformerContext context,
		CancellationToken ctx)
	{
		var methodInfo = OpenApiUtils.GetMethodInfo(context);
		if (methodInfo == null)
		{
			return Task.CompletedTask;
		}

		var attributes = (IEnumerable<PushNotificationAttribute>)Attribute.GetCustomAttributes(methodInfo,
			typeof(PushNotificationAttribute));

		if (!attributes.Any())
		{
			return Task.CompletedTask;
		}

		operation.Description +=
			$"\n\n" +
			$"> [!success]" +
			$"\n" +
			$"Sends Push Notification";
		return Task.CompletedTask;
	}
}