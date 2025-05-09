# LiteWeight - Simple Workout Manager

LiteWeight is a lightweight workout manager that allows for workouts to be created and managed. Default exercises are provided, and users can create exercises as needed. There are also social aspects to the app where you can add friends and send workouts to other LiteWeight users.

[API Readme](api/README.md)

[Mobile App Readme](app/README.md)

[Demo of v3.0.0](https://youtube.com/watch?v=JwR2EJi_Pgs)

Refer to the [Wiki](https://github.com/joshrap67/LiteWeight/wiki) for details on the application logic.

## Deployment

Until I setup a CI/CD process these are the steps for a full release.

1. Run unit tests for the API.
2. Run the publish script for API documentation, if API changed.
3. Publish API (follow instructions on API readme)
4. Run unit tests for mobile app.
5. Build signed APK

Some of these steps can be skipped depending on what needs to be released. E.g. if there is only a backend change with no client facing API changes then only steps 1 through 3 need to be followed.

## Authors

- Joshua Rapoport - *Creator and Lead Software Developer*