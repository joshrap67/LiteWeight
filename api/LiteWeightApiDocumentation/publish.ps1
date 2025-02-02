dotnet build ..\LiteWeightApi
# https://github.com/domaindrivendev/Swashbuckle.AspNetCore#retrieve-swagger-directly-from-a-startup-assembly
swagger tofile --output .\public\swagger.json ..\LiteWeightApi\bin\Debug\net8.0\LiteWeightApi.dll v1
dotnet run