using System.Reflection;
using LiteWeightApiDocumentation.Services;
using Microsoft.OpenApi;

if (args.Length == 0)
{
	Console.WriteLine("Usage: dotnet run -- [FILENAME]");
	return -1;
}

var openApiDocument = await OpenApiService.GetOpenApiDocument(args[0]);

// upload to firebase
await using var stream = File.Create("./public/swagger.json");
await openApiDocument.SerializeAsync(stream, OpenApiSpecVersion.OpenApi3_1, OpenApiConstants.Json,
	CancellationToken.None);
await StorageService.Upload(stream, "swagger.json", "json");

var assembly = Assembly.GetExecutingAssembly();
var faviconStream = assembly.GetManifestResourceStream("LiteWeightApiDocumentation.public.favicon.ico")!;
await StorageService.Upload(faviconStream, "favicon.ico", "image/x-icon");

var docsStream = assembly.GetManifestResourceStream("LiteWeightApiDocumentation.public.apiDocs.html")!;
await StorageService.Upload(docsStream, "apiDocs.html", "text/html");

return 1;