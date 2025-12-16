using Microsoft.Extensions.Options;
using Microsoft.OpenApi;
using Swashbuckle.AspNetCore.SwaggerGen;

namespace LiteWeightAPI.Swagger;

public class CreateSwaggerInfoSection : IConfigureOptions<SwaggerGenOptions>
{
	public void Configure(SwaggerGenOptions options)
	{
		options.SwaggerDoc("v1", CreateApiInfo());
	}

	private static OpenApiInfo CreateApiInfo()
	{
		var openApiInfo = new OpenApiInfo
		{
			Title = "LiteWeight API",
			Version = "v1"
		};

		return openApiInfo;
	}
}