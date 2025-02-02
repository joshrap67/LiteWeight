using FirebaseAdmin.Auth;

namespace LiteWeightAPI.Services;

public interface IFirebaseAuthService
{
	Task DeleteUser(string userId);
}

public class FirebaseAuthService : IFirebaseAuthService
{
	public async Task DeleteUser(string userId)
	{
		await FirebaseAuth.DefaultInstance.DeleteUserAsync(userId);
	}
}