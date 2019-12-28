# LiteWeight - Simple Workout Manager

This app is a lightweight workout manager that allows for workouts to be created and managed. The application follows a MVVM software architectural pattern with a single repository that interacts with a local SQLite database.

Default exercises are provided for adding to workouts, and any custom exercises can be created if desired. All exercises can have a URL associated with it for viewing a video on how to perform it while working out. Additionally, each exercise also tracks the most recent amount of weight that was used.

Refer to the Wiki for details on the application logic.

## Prerequisites

This application is currently only available on Android devices. The minimum SDK version that this application can run on is: 16. The targeted SDK for this application is: 28.

No internet connection is needed for this application at this time as all data is stored locally in a SQLite database.

If pulling from this repository, Android Studio is required in order to run the application.

## Deployment

If downloading from the Google Play Store, just simply download it and ensure enough space is available on the device.

If pulling from this repository, open the project in Android Studio and run it. If doing it this way, you may need to ensure that you have developer options enabled on your device.

## Built With

- [Android](https://www.android.com/) - Framework that this app was built with
- [Java](https://docs.oracle.com/en/java/) - Used for the backend of the application
- [Android Room Persistence Library](https://developer.android.com/topic/libraries/architecture/room) - Library used to store data locally on the Android device in a SQLite database
- [Android Studio](https://developer.android.com/studio) - IDE that was used to build this application. All frontend development was done in Android Studio

## Authors

- Joshua Rapoport - *Creator and Lead Software Developer*

## Acknowledgments

Tutorial that got me started [Coding in flow](https://codinginflow.com/tutorials/android)

App Logo crated using asset made by [Monkik](https://www.flaticon.com/authors/monkik)

MyExericses Nav Icon made by [Demograph](https://thenounproject.com/demograph/uploads/?i=149086)

Numerous YouTube videos are used for the exercise videos in this non-profit application. I do not own any of these videos and take no responsibility for any actions of the owners

My friend John Cooley for helping me with the app name
