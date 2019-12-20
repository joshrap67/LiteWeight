# LiteWeight - Simple Workout Manager

This app is a lightweight workout manager that allows for workouts to be created and managed. The application follows a MVVM software architectural pattern with a single repository that interacts with a local SQLite database.

Default exercises are provided for adding to workouts, and any custom exercises can be created if desired. All exercises can have a URL associated with it for viewing a video on how to perform it while working out. Additionally, each exercise also tracks the most recent amount of weight that was used.

## Prerequisites

This application is currently only available on Android devices. The minimum SDK version that this application can run on is: 15. The targeted SDK for this application is: 28.

No internet connection is needed for this application at this time as all data is stored locally in a SQLite database.

If pulling from this repository, Android Studio is required in order to run the application.

## Deployment

If downloading from the Google Play Store, just simply download it and ensure enough space is available on the device.

If pulling from this repository, open the project in Android Studio and run it. If doing it this way, you may need to ensure that you have developer options enabled on your device.

## Application Logic Overview

### Room Persistence Library Classes
The "Database" folder in the source code contains all the logic for interacting with the SQLite database that the Room library provides. The database logic is broken down by: Entities, Database Access Objects (DAOs), Repository, ViewModels, and the actual Database class.

**Entities**

With the Room library, each entity corresponds to a unique table in the database. Currently, there are three entities: WorkoutEntity, MetaEntity, and ExerciseEntity. Note the table names are different than the entity name since I feel a table should have a separate name from the entity. Each instance variable of these classes corresponds to a column in the corresponding table.

**_WorkoutEntity_**

| id | day | workout | exercise | status |
| --- | --- | --- | --- | --- |
| 22 | 0 | Josh's Workout | Barbell Curl | false |

Every time a user creates a workout, workout entities are created for each exercise in the workout and placed in this table. The workout name column thus serves as a secondary key when pulling all exercises from a given workout. Note that the day values start at 0, so e.g. if a workout only has 1 day, then the only day index in that workout will be 0. The status column corresponds to whether the given exercise on that day has been completed or not.

**_MetaEntity_**

| id | timesCompleted | completedSum | totalSum | currentDay |  totalDays | workoutName | dateLast | dateCreated | mostFrequentFocus | percentageExercisesCompleted | currentWorkout |
| --- | --- | --- | --- | --- |  --- | --- | --- | --- | --- | --- | --- |
| 3 | 0 | 34 | 65 | 1 | 5 | Josh's Workout | 2019-08-17T16:52:49 | 2019-08-17T16:52:49 | Legs | 52.3% | true |

The MetaEntity contains metadata about a specific workout. It is created when a user creates a new workout and is periodically updated throughout the application whenever the user interacts with the workout. Most of the columns are pretty self explanatory, to promote brevity only a few will be explained. The completedSum and totalSum are used to calculate the percentageExercisesCompleted. This may seem a little unnecessary to have two separate columns just for an arithmetic mean calculation, but since the user can add and remove exercises, this is the only clean way of ensuring the percentage is accurate. The dateLast is the date the workout was last selected, and is currently used for sorting functionality. The currentWorkout value is either true or false to indicate if it is the workout the user is currently doing.

**_ExerciseEntity_**

| id | timesCompleted | exerciseName | focus | url |  defaultExercise | currentWeight | minWeight | maxWeight |
| --- | --- | --- | --- | --- |  --- | --- | --- | --- |
| 34 | 3 | Barbell Curl | Biceps, Strength Training | https://www.youtube.com/watch?v=FAEWpmb9YQs |  true | 50 lb | 10 lb | 60 lb |

There is only one entity for each unique exercise. Default exercises are provided with the base application and cannot be deleted, but the video URLs may be altered. The focus describes the muscle groups that are worked or the type of exercise it is. If there are multiple, the value is comma delimited.

Note that all weight in the DB is stored in Imperial units, and any conversion is done in the fragments. When the user specifies that they wish to ignore weight, a sentinel value of -1 is put in the currentWeight column.

**DAOs**

The DAOs are interfaces that the Room library uses to directly query the DB. Many tutorials online highlight using a LiveData type for return values, but I found that for this application the LiveData was more trouble than it was worth. Consequently, most queries just return a List type instead.

**Repository**

The repository is a layer above the DAOs. It contains all the DAOs as instance variables and allows for the ViewModels to initiate any queries to the DB. For some queries like update, delete, or insert, asynchronous tasks are utilized here in the repository. This is to follow best practice since DB queries shouldn't be done on the main UI thread. However, some queries are not asynchronous here as the view itself handles the asynchronous call.

This is perhaps not best practice, but I found that the code is much cleaner this way since I can directly catch any errors in the fragment if something goes wrong. So, for example, the query to get all metadata is not asynchronous in the repository, the method just simply calls the DAO getAllMeta query. In the fragment, the ViewModel method that calls the repository method **is** in wrapped in an asynchronous task, and if it fails here I can easily display an error message.

**ViewModels**

The ViewModels are a layer above the repository and call the methods of the repository. The ViewModels are utilized by the fragments and the MainActivity class.

**Database**

The database class is very simple and boiler plate. It only needs to be updated by increasing the version number whenever a table's columns are modified, if a table is deleted or created from the DB.

Whenever the version number is increased, a migration must also be made and then added to the constructor of the database instance. The migration must handle whatever change was done. E.g. if a new column was added in the workout_table, the migration must perform a SQL alter table query. This ensures that old data won't be lost whenever the database is updated beacause without this migration, the table is wiped clean.

### Main Activity & Fragments

**MainActivity**

The MainActivity is the only activity in this application - it is the entry point of the app. It contains a fragment manager that holds all the fragments that are available. Only one fragment can be loaded in the container at a time. The MainActivity also has a toolbar that all the fragments can interact with in order to enable or disable the loading animation or to change the toolbar title.

The first thing the activity does is check the shared preferences to see if the app has already been loaded at least once. If not, then the default exercises are copied from the assets folder to the exercise table. This is to avoid needless queries to the DB.

Once the exercise table is guaranteed to be populated with default exercises, then the navigation pane is initialized.

A couple of methods are overridden, such as the onBackPressed to ensure that a user doesn't exit the app when creating a workout. In the future, I hope to make the onBackPressed more intuitive and allow for it to cycle through fragments that have already been visited (in a stack like manner).

**Current Workout Fragment**

This is the landing fragment of the app loaded by the MainActivity. It displays the workout that the user is currently working on as specified by the My Workout fragment. The user can cycle through all of the days of the workout and mark any exercises as completed, view any videos that the exercise has, or update the current weight of an exercise.

The workout can be reset by either reaching the last day, or by holding down the "Next" button. When the workout is reset, all the workout entities are set to be incomplete, and any statistics are updated in the corresponding MetaEntity.

The exercises are displayed on a table layout using a pre-defined XML layout for each row. A separate class, "Exercise", was made for these rows just for readability purposes and handles all of the logic for updating the entity and displaying any Android views.

There is also a simple timer that the user can use to either time an exercise or keep track of time between reps. This can be enabled or disabled in the Settings fragment.

**My Workouts Fragment**

This fragment lists all the workouts that the user has created. The top workout is the currently selected one, and all the other workouts are listed in the list view below sorted by most recently used.

The user can also reset any statistics of a given workout, delete it, or edit it to add or remove exercises. When editing, any exercise can be added or removed, but it isn't saved until the user clicks the appropriate button.

**Create Workout Fragment**

This fragment is where workouts are created. First the user must provide info for the workout: a name, number of weeks in the workout, and number of days in each of those weeks.

A class called "Validator" was made to help validate any user input in this fragment, as well as sometimes in other fragments. Currently you can't have a workout with more than 10 weeks, 7 days, or one that has a name that's already been used before. Additionally, there is an upper limit of 50 for the number of workouts allowed to be made.

Once the input is validated, the user can then add exercises to the workout. Each day must have at least one exercise in it before able to be finalized and uploaded to the DB.

**Settings Fragment**

This fragment allows for any user settings to be enabled or disabled and stored in the application's shared preferences. Currently, the user can toggle videos to be viewable when in the Current Workout fragment, toggle a timer to be available in the Current Workout fragment, or toggle between metric and imperial units for weight.

The user can also view all exercises in this fragment and edit them. If editing a default exercise, the user can edit the weight or URL associated with it. If editing a custom one, the user can additionally rename it or delete it entirely. Note that deleting it will remove it from any already made workout.

**About Fragment**

This fragment at this time has no actual functionality, it just gives the user information about the app.


## Built With

- [Android](https://www.android.com/) - Framework that this app was built with
- [Java](https://docs.oracle.com/en/java/) - Used for the backend of the application
- [Android Room Persistence Library](https://developer.android.com/topic/libraries/architecture/room) - Library used to store data locally on the Android device in a SQLite database.
- [Android Studio](https://developer.android.com/studio) - IDE that was used to build this application. All frontend development was done in Android Studio.

## Authors

- Joshua Rapoport - *Creator and Lead Software Developer*

## Acknowledgments

[Coding in flow](https://codinginflow.com/tutorials/android)

App Logo made by [Monkik](https://www.flaticon.com/authors/monkik)

Exercise Nav Icon made by [Demograph](https://thenounproject.com/demograph/uploads/?i=149086)

Numerous YouTube videos are used for the exercise videos in this non-profit application. I do not own any of these videos and take no responsibility for any actions of the owners.
