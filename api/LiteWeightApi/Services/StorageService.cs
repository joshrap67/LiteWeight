using Google.Cloud.Storage.V1;

namespace LiteWeightAPI.Services;

public interface IStorageService
{
	Task UploadProfilePicture(byte[] fileData, string fileName);
	Task DeleteProfilePicture(string fileName);
	Task<string> UploadDefaultProfilePicture(string fileName);
}

public class StorageService : IStorageService
{
	private const string ProfilePictureBucket = "liteweight-profile-pictures";
	private const string DefaultProfilePictureBucket = "liteweight-private-images";
	private const string DefaultProfilePictureObject = "DefaultProfilePicture.jpg";

	public async Task UploadProfilePicture(byte[] fileData, string fileName)
	{
		using var stream = new MemoryStream(fileData);
		var storage = await StorageClient.CreateAsync();

		var obj = new Google.Apis.Storage.v1.Data.Object
		{
			Bucket = ProfilePictureBucket,
			Name = fileName,
			ContentType = "image/jpeg",
			CacheControl = "public,max-age=0"
		};

		await storage.UploadObjectAsync(obj, stream);
	}

	public async Task DeleteProfilePicture(string fileName)
	{
		var storage = await StorageClient.CreateAsync();
		await storage.DeleteObjectAsync(ProfilePictureBucket, fileName);
	}

	public async Task<string> UploadDefaultProfilePicture(string fileName)
	{
		var storage = await StorageClient.CreateAsync();

		var memoryStream = new MemoryStream();
		await storage.DownloadObjectAsync(DefaultProfilePictureBucket, DefaultProfilePictureObject, memoryStream);
		var bytes = memoryStream.ToArray();

		await UploadProfilePicture(bytes, fileName);

		return fileName;
	}
}