using Google.Cloud.Storage.V1;
using LiteWeightAPI.Options;
using Microsoft.Extensions.Options;
using Object = Google.Apis.Storage.v1.Data.Object;

namespace LiteWeightAPI.Services;

public interface IStorageService
{
	Task UploadProfilePicture(byte[] fileData, string fileName);
	Task DeleteProfilePicture(string fileName);
	Task<string> UploadDefaultProfilePicture(string fileName);
}

public class StorageService : IStorageService
{
	private const string DefaultProfilePictureObject = "DefaultProfilePicture.jpg";

	private readonly string _defaultProfilePictureBucket;
	private readonly string _profilePictureBucket;

	public StorageService(IOptions<FirebaseOptions> firebaseOptions)
	{
		_profilePictureBucket = firebaseOptions.Value.ProfilePictureBucket;
		_defaultProfilePictureBucket = firebaseOptions.Value.DefaultProfilePictureBucket;
	}

	public async Task UploadProfilePicture(byte[] fileData, string fileName)
	{
		using var stream = new MemoryStream(fileData);
		var storage = await StorageClient.CreateAsync();

		var obj = new Object
		{
			Bucket = _profilePictureBucket,
			Name = fileName,
			ContentType = "image/jpeg",
			CacheControl = "public,max-age=0"
		};

		await storage.UploadObjectAsync(obj, stream);
	}

	public async Task DeleteProfilePicture(string fileName)
	{
		var storage = await StorageClient.CreateAsync();
		await storage.DeleteObjectAsync(_profilePictureBucket, fileName);
	}

	public async Task<string> UploadDefaultProfilePicture(string fileName)
	{
		var storage = await StorageClient.CreateAsync();

		var memoryStream = new MemoryStream();
		await storage.DownloadObjectAsync(_defaultProfilePictureBucket, DefaultProfilePictureObject, memoryStream);
		var bytes = memoryStream.ToArray();

		await UploadProfilePicture(bytes, fileName);

		return fileName;
	}
}