using LiteWeightAPI.Imports;
using Microsoft.OpenApi.Models;
using Swashbuckle.AspNetCore.SwaggerGen;

namespace LiteWeightAPI.Swagger;

public class AppendPushNotificationIndicatorOperationFilter : IOperationFilter
{
	/// <summary>
	/// Automatically appends an indicator that successful completion of the action will send a push notification
	/// </summary>
	public void Apply(OpenApiOperation operation, OperationFilterContext context)
	{
		context.ApiDescription.TryGetMethodInfo(out var methodInfo);
		if (methodInfo == null)
		{
			return;
		}

		var attributes = (IEnumerable<PushNotificationAttribute>)Attribute.GetCustomAttributes(methodInfo,
			typeof(PushNotificationAttribute));

		if (!attributes.Any())
		{
			return;
		}

		operation.Description +=
			$"<br/><br/>" +
			$"<div style=\"background-color: #2f3031;padding: 12px 0px 12px 12px;border-left: 5px solid #02F88F;\">" +
			$"<b>Sends Push Notification</b>" +
			$"</div>";
	}
}