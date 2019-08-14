package com.joshrap.liteweight.Database.Repositories;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import com.joshrap.liteweight.Database.Daos.*;
import com.joshrap.liteweight.Database.Entities.*;
import com.joshrap.liteweight.Database.*;

import java.util.List;

public class WorkoutRepository {
    private WorkoutDao workoutDao;
    private MetaDao metaDao;
    private ExerciseDao exerciseDao;
    private LiveData<List<WorkoutEntity>> allWorkouts;

    public WorkoutRepository(Application application) {
        WorkoutDatabase database = WorkoutDatabase.getInstance(application);
        workoutDao = database.workoutDao();
        metaDao = database.logDao();
        exerciseDao = database.exerciseDao();
        allWorkouts = workoutDao.getAllWorkouts();
    }

    // region
    // View Model methods for workout table
    public void insertWorkoutEntity(WorkoutEntity workout) {
        new InsertWorkoutAsyncTask(workoutDao).execute(workout);
    }

    public void updateWorkoutEntity(WorkoutEntity workout) {
        new UpdateWorkoutAsyncTask(workoutDao).execute(workout);

    }

    public void updateExerciseName(String oldName, String newName) {
        new UpdateExerciseNameAsyncTask(workoutDao, oldName, newName).execute();
    }

    public void deleteWorkoutEntity(WorkoutEntity workout) {
        new DeleteWorkoutAsyncTask(workoutDao).execute(workout);
    }

    public void deleteEntireWorkout(String workoutName) {
        workoutDao.deleteEntireWorkout(workoutName);
    }

    public void deleteSpecificExerciseFromWorkout(String workoutName, String exerciseName, int day) {
        new DeleteSpecificExerciseFromWorkoutAsyncTask(workoutDao, workoutName, exerciseName, day).execute();
    }

    public void deleteExerciseFromWorkouts(String exerciseName) {
        new DeleteExerciseFromWorkoutsAsyncTask(workoutDao).execute(exerciseName);
    }

    public void deleteAllWorkouts() {
        new DeleteAllWorkoutAsyncTask(workoutDao).execute();
    }

    public List<WorkoutEntity> getExercises(String workout) {
        return workoutDao.getExercises(workout);
    }

    public LiveData<List<WorkoutEntity>> getAllWorkouts() {
        return allWorkouts;
    }

    // endregion
    // region
    // View Model methods for meta table
    public void insertMetaEntity(MetaEntity metaEntity) {
        new InsertMetaAsyncTask(metaDao).execute(metaEntity);
    }

    public void updateMetaEntity(MetaEntity metaEntity) {
        new UpdateMetaAsyncTask(metaDao).execute(metaEntity);
    }

    public void deleteMetaEntity(MetaEntity metaEntity) {
        new DeleteMetaAsyncTask(metaDao).execute(metaEntity);
    }

    public void deleteAllMetadata() {
        new DeleteAllMetadataAsyncTask(metaDao).execute();
    }

    public MetaEntity getCurrentWorkoutMeta() {
        return metaDao.getCurrentWorkoutMeta();
    }

    public List<MetaEntity> getAllMetadata() {
        return metaDao.getAllMetadata();
    }

    // endregion
    // region
    // ViewModel methods for exercise table
    public void insertExerciseEntity(ExerciseEntity entity) {
        new InsertExerciseAsyncTask(exerciseDao).execute(entity);
    }

    public void updateExerciseEntity(ExerciseEntity entity) {
        new UpdateExerciseAsyncTask(exerciseDao).execute(entity);
    }

    public void deleteExerciseEntity(ExerciseEntity entity) {
        new DeleteExerciseAsyncTask(exerciseDao).execute(entity);
    }

    public void deleteAllExerciseEntities() {
        new DeleteAllExercisesAsyncTask(exerciseDao).execute();
    }

    public List<ExerciseEntity> getAllExercises() {
        return exerciseDao.getAllExercises();
    }
    // endregion
    // region
    // Private classes used to execute the WORKOUT queries using the DAOs

    private static class InsertWorkoutAsyncTask extends AsyncTask<WorkoutEntity, Void, Void> {
        private WorkoutDao workoutDao;

        private InsertWorkoutAsyncTask(WorkoutDao workoutDao) {
            this.workoutDao = workoutDao;
        }

        @Override
        protected Void doInBackground(WorkoutEntity... workoutEntities) {
            workoutDao.insert(workoutEntities[0]);
            return null;
        }
    }

    private static class UpdateWorkoutAsyncTask extends AsyncTask<WorkoutEntity, Void, Void> {
        private WorkoutDao workoutDao;

        private UpdateWorkoutAsyncTask(WorkoutDao workoutDao) {
            this.workoutDao = workoutDao;
        }

        @Override
        protected Void doInBackground(WorkoutEntity... workoutEntities) {
            workoutDao.update(workoutEntities[0]);
            return null;
        }
    }

    private static class DeleteSpecificExerciseFromWorkoutAsyncTask extends AsyncTask<Void, Void, Void> {
        private WorkoutDao workoutDao;
        private String workoutName;
        private String exerciseName;
        private int day;

        private DeleteSpecificExerciseFromWorkoutAsyncTask(WorkoutDao workoutDao, String workoutName, String exerciseName, int day) {
            this.workoutDao = workoutDao;
            this.workoutName = workoutName;
            this.exerciseName = exerciseName;
            this.day = day;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            workoutDao.deleteSpecificExerciseFromWorkout(workoutName, exerciseName, day);
            return null;
        }
    }

    private static class DeleteExerciseFromWorkoutsAsyncTask extends AsyncTask<String, Void, Void> {
        private WorkoutDao workoutDao;

        private DeleteExerciseFromWorkoutsAsyncTask(WorkoutDao workoutDao) {
            this.workoutDao = workoutDao;
        }

        @Override
        protected Void doInBackground(String... params) {
            workoutDao.deleteExerciseFromWorkouts(params[0]);
            return null;
        }
    }

    private static class DeleteWorkoutAsyncTask extends AsyncTask<WorkoutEntity, Void, Void> {
        private WorkoutDao workoutDao;

        private DeleteWorkoutAsyncTask(WorkoutDao workoutDao) {
            this.workoutDao = workoutDao;
        }

        @Override
        protected Void doInBackground(WorkoutEntity... workoutEntities) {
            workoutDao.delete(workoutEntities[0]);
            return null;
        }
    }

    private static class DeleteAllWorkoutAsyncTask extends AsyncTask<Void, Void, Void> {
        private WorkoutDao workoutDao;

        private DeleteAllWorkoutAsyncTask(WorkoutDao workoutDao) {
            this.workoutDao = workoutDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            workoutDao.deleteAllWorkouts();
            return null;
        }
    }

    private static class UpdateExerciseNameAsyncTask extends AsyncTask<Void, Void, Void> {
        private WorkoutDao workoutDao;
        private String oldName;
        private String newName;

        private UpdateExerciseNameAsyncTask(WorkoutDao workoutDao, String oldName, String newName) {
            this.workoutDao = workoutDao;
            this.oldName = oldName;
            this.newName = newName;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            workoutDao.updateExerciseName(oldName, newName);
            return null;
        }
    }

    // endregion
    // region
    // Private classes used to execute the METADATA queries using the DAOs
    private static class InsertMetaAsyncTask extends AsyncTask<MetaEntity, Void, Void> {
        private MetaDao metaDao;

        private InsertMetaAsyncTask(MetaDao metaDao) {
            this.metaDao = metaDao;
        }

        @Override
        protected Void doInBackground(MetaEntity... metaEntities) {
            metaDao.insert(metaEntities[0]);
            return null;
        }
    }

    private static class UpdateMetaAsyncTask extends AsyncTask<MetaEntity, Void, Void> {
        private MetaDao metaDao;

        private UpdateMetaAsyncTask(MetaDao metaDao) {
            this.metaDao = metaDao;
        }

        @Override
        protected Void doInBackground(MetaEntity... metaEntities) {
            metaDao.update(metaEntities[0]);
            return null;
        }
    }

    private static class DeleteMetaAsyncTask extends AsyncTask<MetaEntity, Void, Void> {
        private MetaDao metaDao;

        private DeleteMetaAsyncTask(MetaDao metaDao) {
            this.metaDao = metaDao;
        }

        @Override
        protected Void doInBackground(MetaEntity... metaEntities) {
            metaDao.delete(metaEntities[0]);
            return null;
        }
    }

    private static class DeleteAllMetadataAsyncTask extends AsyncTask<Void, Void, Void> {
        private MetaDao metaDao;

        private DeleteAllMetadataAsyncTask(MetaDao metaDao) {
            this.metaDao = metaDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            metaDao.deleteAllMetadata();
            return null;
        }
    }

    // endregion
    // region
    // Private classes used to execute the EXERCISE queries using the DAOs
    private static class InsertExerciseAsyncTask extends AsyncTask<ExerciseEntity, Void, Void> {
        private ExerciseDao exerciseDao;

        private InsertExerciseAsyncTask(ExerciseDao exerciseDao) {
            this.exerciseDao = exerciseDao;
        }

        @Override
        protected Void doInBackground(ExerciseEntity... exerciseEntities) {
            exerciseDao.insert(exerciseEntities[0]);
            return null;
        }
    }

    private static class UpdateExerciseAsyncTask extends AsyncTask<ExerciseEntity, Void, Void> {
        private ExerciseDao exerciseDao;

        private UpdateExerciseAsyncTask(ExerciseDao exerciseDao) {
            this.exerciseDao = exerciseDao;
        }

        @Override
        protected Void doInBackground(ExerciseEntity... exerciseEntities) {
            exerciseDao.update(exerciseEntities[0]);
            return null;
        }
    }

    private static class DeleteExerciseAsyncTask extends AsyncTask<ExerciseEntity, Void, Void> {
        private ExerciseDao exerciseDao;

        private DeleteExerciseAsyncTask(ExerciseDao exerciseDao) {
            this.exerciseDao = exerciseDao;
        }

        @Override
        protected Void doInBackground(ExerciseEntity... exerciseEntities) {
            exerciseDao.delete(exerciseEntities[0]);
            return null;
        }
    }

    private static class DeleteAllExercisesAsyncTask extends AsyncTask<Void, Void, Void> {
        private ExerciseDao exerciseDao;

        private DeleteAllExercisesAsyncTask(ExerciseDao exerciseDao) {
            this.exerciseDao = exerciseDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            exerciseDao.deleteAllExercises();
            return null;
        }
    }
    // endregion
}
