dotnet build ..\LiteWeightApi
# https://github.com/domaindrivendev/Swashbuckle.AspNetCore#retrieve-swagger-directly-from-a-startup-assembly
# https://www.nuget.org/packages/Swashbuckle.AspNetCore.Cli
swagger tofile --output .\public\swagger.json ..\LiteWeightApi\bin\Debug\net10.0\LiteWeightApi.dll v1
dotnet run