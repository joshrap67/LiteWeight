namespace LiteWeightAPI.Api.Self.Requests;

public class SetFirebaseMessagingTokenRequest
{
	/// <summary>
	/// Firebase token to associate with the user. Enables push notifications to be sent to the device associated with the token. Null removes the ability for the user to receive notifications.
	/// </summary>
	/// <example>c_-_jNKuQI-9USNk0c9uEY:APA91bG53pjv4wNcg7m2h2d1yfCjVfidEj7AXyIu8ddNYz2_Stwy0J7znQayzltXDgEL8Q9tSj8i2yx8hQiSuNDKlZtGm3cEmJTlLMupmJVTc1g_LdMPdx9VOL2hC3vBTseIcmBDYjRG</example>
	public string? FirebaseMessagingToken { get; set; }
}