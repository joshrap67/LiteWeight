# LiteWeight - Simple Workout Manager

This app is a lightweight workout manager that allows for workouts to be created, managed, and shared. The application interacts with a .Net Core backend hosted on Google Cloud: [LiteWeightApi](https://github.com/joshrap67/LiteWeightApi). LiteWeight is currently compatible with versions 1.x.x of the API.

Default exercises are provided for adding to workouts, and any custom exercises can be created if desired. All exercises can have a URL associated with it for viewing a video on how to perform it while working out.

Friend requests can be sent to any other LiteWeight user and workouts can additionally be sent to any user.

[Demo of v3.0.0](https://youtu.be/eTNm_Hre1ns)

Refer to the [Wiki](https://github.com/joshrap67/LiteWeight/wiki) for details on the application logic.

Google Play Store requires a website to request for account deletion for GDPR compliance. I just did a quick and dirty react app to accomplish this. It is hosted on Github Pages and is accessible from [this link](https://joshrap67.github.io/LiteWeight/#/home).

## Prerequisites

This application is currently only available on Android devices. The minimum SDK version that this application can run on is: 26. The targeted SDK for this application is: 33.

An internet connection is required to run this application.

If pulling from this repository, Android Studio is required in order to run the application.

If deploying the GH pages app, node is required.

## Deployment

If downloading from the [Google Play Store](https://play.google.com/store/apps/details?id=com.joshrap.liteweight&fbclid=IwAR3tvspaMUvVOcPjw1NHzb0wUL9l5aDlZ9ferGfqPbp9ev7__Ob-D6hP5lw), simply download it and ensure enough space is available on the device.

If pulling from this repository, open the project in Android Studio and run it. If doing it this way, you may need to ensure that you have developer options enabled on your device.

To publish the GH pages website run `npm run deploy` in the directory.

## Built With

- [Android](https://www.android.com/) - Framework that this app was built with
- [Java](https://docs.oracle.com/en/java/) - Language that this app was written in
- [Android Studio](https://developer.android.com/studio) - IDE that was used to build this application. All frontend development was done in Android Studio

## Authors

- Joshua Rapoport - *Creator and Lead Software Developer*

## Acknowledgments

Tutorial that got me started [Coding in flow](https://codinginflow.com/tutorials/android)

App Logo uses free SVG found [here](https://uxwing.com/feather-icon/)

Numerous YouTube videos are used for the exercise videos in this non-profit application. I do not own any of these videos and take no responsibility for any actions of the owners

My friend John Cooley for helping me with the app name
