using LiteWeightAPI.Imports;
using ILogger = Serilog.ILogger;

namespace LiteWeightAPI.Middleware;

public class LogInfoMiddleware
{
	private readonly RequestDelegate _next;
	private readonly ILogger _logger;

	public LogInfoMiddleware(RequestDelegate next, ILogger logger)
	{
		_next = next;
		_logger = logger;
	}

	public async Task InvokeAsync(HttpContext context)
	{
		var version = context.Request.Headers[RequestFields.VersionNameHeader].ToString();
		var versionCodeString = context.Request.Headers[RequestFields.AndroidVersionCodeHeader].ToString();
		_logger.Information($"LiteWeight version code for request: {version}");
		_logger.Information($"LiteWeight android version number for request: {versionCodeString}");

		await _next(context);
	}
}