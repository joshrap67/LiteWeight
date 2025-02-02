namespace LiteWeightAPI.Services.Notifications;

public class NotificationData
{
	public required string Action { get; set; }
	public required string JsonPayload { get; set; }
}