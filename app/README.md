# LiteWeight Android App

This app is a lightweight workout manager that allows for workouts to be created, managed, and shared. The application interacts with a [.Net Core backend hosted on Google Cloud](api/README.md).

Default exercises are provided for adding to workouts, and any custom exercises can be created if desired. All exercises can have a links associated with it for viewing videos on how to perform it while working out, or to link to articles on the exercise.

Friend requests can be sent to any other LiteWeight user and workouts can additionally be sent to any user.

[Demo of v3.0.0](https://youtu.be/eTNm_Hre1ns)

Refer to the [Wiki](https://github.com/joshrap67/LiteWeight/wiki) for details on the application logic.

## Prerequisites

This application is currently only available on Android devices. The minimum SDK version that this application can run on is: 26. The targeted SDK for this application is: 35.

An internet connection is required to run this application.

If pulling from this repository, Android Studio is required in order to run the application.

## Deployment

Build the app APK using the proper keys in Android Studio. The app is no longer deployed to Google Play thanks to Google requiring me to submit my address and phone number.

There are two flavors: sandbox and prod. Switch build profiles on Android Studio to build to a specific flavor. When generating a prod APK you must ensure there is a signing.properties file (ignored by git) in the app directory. That file should have the following setup:

```
STORE_FILE=
STORE_PASSWORD=
KEY_ALIAS=
KEY_PASSWORD=
```

When switching build profiles just remember to reassemble the project.

## Built With

- [Android](https://www.android.com/) - Framework that this app was built with
- [Java](https://docs.oracle.com/en/java/) - Language that this app was written in
- [Android Studio](https://developer.android.com/studio) - IDE that was used to build this application. All frontend development was done in Android Studio

## Acknowledgments

Tutorial that got me started [Coding in flow](https://codinginflow.com/tutorials/android)

App Logo uses free SVG found [here](https://uxwing.com/feather-icon/)

Numerous YouTube videos are used for the exercise videos in this non-profit application. I do not own any of these videos and take no responsibility for any actions of the owners

My friend John Cooley for helping me with the app name
