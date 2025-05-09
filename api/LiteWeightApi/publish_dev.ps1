gcloud config set project liteweight-sandbox
docker build -t us-east1-docker.pkg.dev/liteweight-sandbox/liteweight-api/api-image .
docker push us-east1-docker.pkg.dev/liteweight-sandbox/liteweight-api/api-image
gcloud run deploy liteweightapi --image=us-east1-docker.pkg.dev/liteweight-sandbox/liteweight-api/api-image --region us-central1