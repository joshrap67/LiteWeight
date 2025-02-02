using System.Reflection;
using System.Text;
using LiteWeightApiDocumentation.Services;
using Microsoft.OpenApi;
using Microsoft.OpenApi.Extensions;

var openApiDocument = SwaggerService.GetOpenApiDocument();

// upload to firebase
var outputString = openApiDocument.Serialize(OpenApiSpecVersion.OpenApi3_0, OpenApiFormat.Json);
var assembly = Assembly.GetExecutingAssembly();
await StorageService.Upload(Encoding.UTF8.GetBytes(outputString), "swagger.json", "json");

var faviconStream = assembly.GetManifestResourceStream("LiteWeightApiDocumentation.public.favicon.ico")!;
await StorageService.Upload(faviconStream, "favicon.ico", "image/x-icon");

var docsStream = assembly.GetManifestResourceStream("LiteWeightApiDocumentation.public.apiDocs.html")!;
await StorageService.Upload(docsStream, "apiDocs.html", "text/html");