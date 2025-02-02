using System.Reflection;
using System.Threading.RateLimiting;
using LiteWeightAPI.Errors.Exceptions;
using LiteWeightAPI.Errors.Responses;
using LiteWeightAPI.Options;
using LiteWeightAPI.Swagger;
using LiteWeightAPI.Utils;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.AspNetCore.Mvc;
using Microsoft.IdentityModel.Tokens;
using Microsoft.OpenApi.Models;
using NetCore.AutoRegisterDi;
using NodaTime;

namespace LiteWeightAPI.ExtensionMethods;

public static class ServiceCollectionExtensions
{
	private const string EnvRootKey = "LiteWeight_";

	public static void ConfigureApi(this IServiceCollection services)
	{
		services.AddControllers();
		services.AddHttpContextAccessor();
		services.AddRouting(options => options.LowercaseUrls = true);
		services.Configure<ApiBehaviorOptions>(options =>
		{
			// when models are incorrectly bound or have validation error, throw an error that ensures an explicit, defined response
			options.InvalidModelStateResponseFactory = ModelBindingErrorHandler;
		});
	}

	public static void ConfigureDependencies(this IServiceCollection services)
	{
		services.AddAutoMapper(typeof(Program));
		services.AddSingleton<IClock>(SystemClock.Instance);
		// auto registers all classes that inherit from an interface as transient
		services.RegisterAssemblyPublicNonGenericClasses().AsPublicImplementedInterfaces();
	}

	public static void ConfigureOptions(this IServiceCollection services, IConfiguration configuration)
	{
		services.Configure<JwtOptions>(configuration.GetSection(EnvRootKey + "Jwt"));
		services.Configure<FirebaseOptions>(configuration.GetSection(EnvRootKey + "Firebase"));
	}

	public static void ConfigureRateLimiting(this IServiceCollection services)
	{
		services.AddRateLimiter(options =>
		{
			options.OnRejected = (context, token) =>
			{
				context.HttpContext.Response.StatusCode = StatusCodes.Status429TooManyRequests;
				context.HttpContext.Response.WriteAsync(JsonUtils.Serialize(new TooManyRequestsResponse
					{ Message = "Too many requests. Please try again later." }), cancellationToken: token);

				return new ValueTask();
			};
			options.GlobalLimiter = PartitionedRateLimiter.Create<HttpContext, string>(httpContext =>
			{
				var userIdClaim = httpContext.User.Claims.ToList().FirstOrDefault(x => x.Type == "user_id");
				return RateLimitPartition.GetFixedWindowLimiter(
					partitionKey: userIdClaim?.Value ?? httpContext.Request.Headers.Host.ToString(),
					factory: _ => new FixedWindowRateLimiterOptions
					{
						AutoReplenishment = true,
						PermitLimit = 100,
						QueueLimit = 0,
						Window = TimeSpan.FromMinutes(1)
					});
			});
		});
	}

	public static void ConfigureSwagger(this IServiceCollection services)
	{
		services.AddSwaggerGen(options =>
		{
			// generate docs from xml comments on methods/models
			var assemblyXml = $"{Assembly.GetExecutingAssembly().GetName().Name}.xml";
			var assemblyXmlPath = Path.Combine(AppContext.BaseDirectory, assemblyXml);
			options.IncludeXmlComments(assemblyXmlPath);

			// generate default responses for all endpoints
			options.OperationFilter<DefaultResponsesOperationFilter>();

			// append any error types that are attributed to each endpoint
			options.OperationFilter<AppendErrorTypesOperationFilter>();

			// append an indicator of which actions send a push notification
			options.OperationFilter<AppendPushNotificationIndicatorOperationFilter>();

			const string bearerDefinition = "BearerDefinition";
			options.AddSecurityDefinition(bearerDefinition, new OpenApiSecurityScheme
			{
				BearerFormat = "JWT",
				Description =
					"Token authentication. \n\n 'Bearer TOKEN'\n\nBearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
				In = ParameterLocation.Header,
				Name = "Authorization",
				Scheme = "Bearer",
				Type = SecuritySchemeType.Http
			});
		});
	}

	public static void ConfigureAuthentication(this IServiceCollection services, IConfiguration configuration)
	{
		var authorityUrl = configuration[EnvRootKey + "Jwt:AuthorityUrl"];
		services.AddAuthentication(options =>
		{
			options.DefaultAuthenticateScheme = JwtBearerDefaults.AuthenticationScheme;
			options.DefaultChallengeScheme = JwtBearerDefaults.AuthenticationScheme;
		}).AddJwtBearer(jwt =>
		{
			jwt.Authority = authorityUrl;
			jwt.TokenValidationParameters = new TokenValidationParameters
			{
				ValidateIssuer = true,
				ValidateLifetime = true,
				ValidateAudience = false
			};
		});
	}

	private static IActionResult ModelBindingErrorHandler(ActionContext context)
	{
		var errors = context.ModelState.Keys
			.SelectMany(key => context.ModelState[key]?.Errors.Select(x => new ModelBindingError
			{
				Property = key,
				Message = x.ErrorMessage
			}))
			.ToList();
		throw new InvalidRequestException("Invalid request", errors);
	}
}