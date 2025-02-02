Read-Host 'THIS WILL PUBLISH TO PRODUCTION. PRESS ANY KEY TO CONTINUE';
gcloud config set project liteweight-faa1a
docker build -t us-east1-docker.pkg.dev/liteweight-faa1a/liteweight-api/api-image .
docker push us-east1-docker.pkg.dev/liteweight-faa1a/liteweight-api/api-image
gcloud run deploy liteweightapi --image=us-east1-docker.pkg.dev/liteweight-faa1a/liteweight-api/api-image --region us-central1