using Google.Cloud.Storage.V1;
using Object = Google.Apis.Storage.v1.Data.Object;

namespace LiteWeightApiDocumentation.Services;

public static class StorageService
{
	public static async Task Upload(byte[] fileData, string fileName, string contentType)
	{
		using var stream = new MemoryStream(fileData);
		var storage = await StorageClient.CreateAsync();

		var obj = new Object
		{
			Bucket = "liteweight-api-documentation",
			Name = fileName,
			ContentType = contentType
		};

		await storage.UploadObjectAsync(obj, stream);
	}

	public static async Task Upload(Stream stream, string fileName, string contentType)
	{
		var storage = await StorageClient.CreateAsync();

		var obj = new Object
		{
			Bucket = "liteweight-api-documentation",
			Name = fileName,
			ContentType = contentType
		};

		await storage.UploadObjectAsync(obj, stream);
	}
}