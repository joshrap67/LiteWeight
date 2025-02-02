using System.Reflection;
using Microsoft.OpenApi.Models;
using Microsoft.OpenApi.Readers;
using NodaTime;

namespace LiteWeightApiDocumentation.Services;

public static class SwaggerService
{
	public static OpenApiDocument GetOpenApiDocument()
	{
		var openApiDocument = GetOpenApiDoc();
		openApiDocument.Info.Description = GetDescription();
		openApiDocument.Info.Contact = new OpenApiContact
		{
			Email = "binary0010productions@gmail.com",
			Name = "Josh Rapoport",
			Url = new Uri("https://github.com/joshrap67")
		};
		return openApiDocument;
	}

	private static OpenApiDocument GetOpenApiDoc()
	{
		var assembly = Assembly.GetExecutingAssembly();
		return new OpenApiStreamReader().Read(
			assembly.GetManifestResourceStream("LiteWeightApiDocumentation.public.swagger.json"),
			out _);
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