namespace LiteWeightAPI.OpenApi.Transformers;

using Microsoft.AspNetCore.OpenApi;
using Microsoft.OpenApi;

public class BearerSecuritySchemeTransformer : IOpenApiDocumentTransformer
{
	public Task TransformAsync(OpenApiDocument document, OpenApiDocumentTransformerContext context,
		CancellationToken ctx)
	{
		var requirements = new Dictionary<string, IOpenApiSecurityScheme>
		{
			["Bearer"] = new OpenApiSecurityScheme
			{
				Type = SecuritySchemeType.Http,
				Scheme = "Bearer",
				In = ParameterLocation.Header,
				Name = "Authorization",
				Description =
					"Token authentication. \n\n 'Bearer TOKEN'\n\nBearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ3aHkgdGYgYXJlIHlvdSBsb29raW5nIGF0IHRoaXMifQ.6p-wk672AaP_5hhhKlpRtaGOQuIqdxIrTNNqXyZaYHs",
			}
		};

		document.Components ??= new OpenApiComponents();
		document.Components.SecuritySchemes = requirements;
		return Task.CompletedTask;
	}
}