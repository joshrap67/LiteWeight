gcloud config set project liteweight-faa1a
dotnet build ..\LiteWeightApi -p:OpenApiGenerateDocuments=true
dotnet run -- "../LiteWeightApi/OpenApi/LiteWeightApi.json"