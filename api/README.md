# LiteWeight API

This web service exposes a HTTP API intended to be consumed by the [Android Application](api/README.md) (compatible with versions >= 3.x.x)

[API References](https://storage.googleapis.com/liteweight-api-documentation/apiDocs.html)

Refer to the [Wiki](https://github.com/joshrap67/LiteWeight/wiki/API) for details on the implementation of the service.

## Prerequisites

.Net 8 is used for the C# projects in this repository.

Firebase credentials must be installed locally. Ensure environment variable GOOGLE_APPLICATION_CREDENTIALS is pointed to `%APPDATA%\gcloud\application_default_credentials.json`

To switch locally between projects run the below commands:

`gcloud config set project <ProjectId>`

`gcloud auth application-default login`

Docker must be installed in order to build the container image to deploy.

Below environment variables must be set

- LiteWeight_Firebase__ProjectId
- LiteWeight_Firebase__ProfilePictureBucket
- LiteWeight_Firebase__DefaultProfilePictureBucket
- LiteWeight_Jwt__AuthorityUrl


## Deployment

There are currently two environments: Prod and Sandbox (dev). It is important to switch to the proper environment whenever deploying. Until I get the drive to do proper CI/CD, there are two powershell scripts that handle everything for deploying the API.

If not using those scripts the commands below can be used assuming the current project is properly set. The examples below use prod as the project id.

To deploy a new docker image to Google Cloud run the following commands in the root of the API directory (same hierarchy as the Dockerfile)

`docker build -t us-east1-docker.pkg.dev/liteweight-faa1a/liteweight-api/api-image .`

`docker push us-east1-docker.pkg.dev/liteweight-faa1a/liteweight-api/api-image`

Note that this will create a new image digest in Artifact Registry. After a couple or so docker pushes it may be prudent to go to Artifact Registry to clean up unused images.

To deploy the API, assuming the API is already initialized in Google Cloud Run, run the following command:

`run deploy liteweightapi --image=us-east1-docker.pkg.dev/liteweight-faa1a/liteweight-api/api-image --region us-central1`

If it is not initialized the same command can be used but the environment variables must be set either manually in the cloud UI or via the `--set-env-vars` flag.

To deploy the documentation, simply run the publish powershell script in the documentation directory. This requires google cloud credentials and the [Swashbuckle CLI NuGet package](https://www.nuget.org/packages/Swashbuckle.AspNetCore.Cli) globally installed.
