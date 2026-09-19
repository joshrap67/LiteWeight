using System.Reflection;
using Microsoft.OpenApi;
using NodaTime;

namespace LiteWeightApiDocumentation.Services;

public static class OpenApiService
{
	public static async Task<OpenApiDocument> GetOpenApiDocument(string openApiFilePath)
	{
		var (document, _) = await OpenApiDocument.LoadAsync(openApiFilePath);
		var openApiDocument = document ?? throw new Exception("OpenApiDocument is null");

		openApiDocument.Info.Description = GetDescription();
		openApiDocument.Info.Contact = new OpenApiContact
		{
			Email = "binary0010productions@gmail.com",
			Name = "Josh Rapoport",
			Url = new Uri("https://github.com/joshrap67")
		};
		return openApiDocument;
	}

	private static string GetDescription()
	{
		var now = SystemClock.Instance.GetCurrentInstant();
		var lastPublished = $"_Last published {now.ToString()}_";

		var assembly = Assembly.GetExecutingAssembly();
		var stream = new StreamReader(
			assembly.GetManifestResourceStream("LiteWeightApiDocumentation.Markdown.InfoDescription.md")!);
		var fileString = stream.ReadToEnd();
		return $"{fileString}\n\n\n{lastPublished}";
	}
}