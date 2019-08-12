package com.example.workoutmadness.Database.Repositories;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import com.example.workoutmadness.Database.Daos.*;
import com.example.workoutmadness.Database.Entities.*;
import com.example.workoutmadness.Database.*;

import java.util.ArrayList;
import java.util.List;

public class WorkoutRepository {
    private WorkoutDao workoutDao;
    private MetaDao metaDao;
    private ExerciseDao exerciseDao;
    private LiveData<List<WorkoutEntity>> allWorkouts;
    private ArrayList<WorkoutEntity> exercises;

    public WorkoutRepository(Application application){
        WorkoutDatabase database = WorkoutDatabase.getInstance(application);
        workoutDao = database.workoutDao();
        metaDao = database.logDao();
        exerciseDao = database.exerciseDao();
        allWorkouts = workoutDao.getAllWorkouts();
//        allMetadata = metaDao.getAllMetadata();
    }

    // region
    // View Model methods for workout table
    public void insertWorkoutEntity(WorkoutEntity workout){
        new InsertWorkoutAsyncTask(workoutDao).execute(workout);
    }
    public void updateWorkoutEntity(WorkoutEntity workout){
        new UpdateWorkoutAsyncTask(workoutDao).execute(workout);

    }
    public void deleteWorkoutEntity(WorkoutEntity workout){
        new DeleteWorkoutAsyncTask(workoutDao).execute(workout);
    }
    public void updateExerciseName(String oldName, String newName){
        new UpdateExerciseNameAsyncTask(workoutDao,oldName,newName).execute();
    }
    public void deleteEntireWorkout(String workoutName){
        workoutDao.deleteEntireWorkout(workoutName);
    }
    public void deleteAllWorkouts(){
        new DeleteAllWorkoutAsyncTask(workoutDao).execute();
    }

    public List<WorkoutEntity> getExercises(String workout){
        return workoutDao.getExercises(workout);
    }

    public LiveData<List<WorkoutEntity>> getAllWorkouts() {
        return allWorkouts;
    }
    // endregion

    // region
    // View Model methods for meta table
    public void insertMetaEntity(MetaEntity metaEntity){
        new InsertMetaAsyncTask(metaDao).execute(metaEntity);
    }
    public void updateMetaEntity(MetaEntity metaEntity){
        new UpdateMetaAsyncTask(metaDao).execute(metaEntity);
    }
    public void deleteMetaEntity(MetaEntity metaEntity){
        new DeleteMetaAsyncTask(metaDao).execute(metaEntity);
    }
    public void deleteAllMetadata(){
        new DeleteAllMetadataAsyncTask(metaDao).execute();
    }
    public MetaEntity getCurrentWorkoutMeta(){
        return metaDao.getCurrentWorkoutMeta();
//        currentWorkoutMeta = metaDao.getCurrentWorkoutMeta();
//        GetCurrentWorkoutMetaAsyncTask task = new GetCurrentWorkoutMetaAsyncTask(metaDao);
//        task.delegate = this;
//        task.execute();
    }

    public List<MetaEntity> getAllMetadata() {
        return metaDao.getAllMetadata();
    }


    // endregion

    // region
    // ViewModel methods for exercise table
    public void insertExerciseEntity(ExerciseEntity entity){
        new InsertExerciseAsyncTask(exerciseDao).execute(entity);
    }
    public void updateExerciseEntity(ExerciseEntity entity){
        new UpdateExerciseAsyncTask(exerciseDao).execute(entity);
    }
    public void deleteExerciseEntity(ExerciseEntity entity){
        new DeleteExerciseAsyncTask(exerciseDao).execute(entity);
    }
    public void deleteAllExerciseEntities(){
        new DeleteAllExercisesAsyncTask(exerciseDao).execute();
    }
    public List<ExerciseEntity> getAllExercises(){
        return exerciseDao.getAllExercises();
    }
    // endregion

    // region
    // Private classes used to execute the workout queries using the database access objects (DAOs)

    private static class InsertWorkoutAsyncTask extends AsyncTask<WorkoutEntity, Void, Void>{
        private WorkoutDao workoutDao;

        private InsertWorkoutAsyncTask(WorkoutDao _workoutDao){
            workoutDao=_workoutDao;
        }
        @Override
        protected Void doInBackground(WorkoutEntity... workoutEntities) {
            workoutDao.insert(workoutEntities[0]);
            return null;
        }
    }

    private static class UpdateWorkoutAsyncTask extends AsyncTask<WorkoutEntity, Void, Void>{
        private WorkoutDao workoutDao;

        private UpdateWorkoutAsyncTask(WorkoutDao _workoutDao){
            workoutDao=_workoutDao;
        }
        @Override
        protected Void doInBackground(WorkoutEntity... workoutEntities) {
            workoutDao.update(workoutEntities[0]);
            return null;
        }
    }

    private static class DeleteWorkoutAsyncTask extends AsyncTask<WorkoutEntity, Void, Void>{
        private WorkoutDao workoutDao;

        private DeleteWorkoutAsyncTask(WorkoutDao _workoutDao){
            workoutDao=_workoutDao;
        }
        @Override
        protected Void doInBackground(WorkoutEntity... workoutEntities) {
            workoutDao.delete(workoutEntities[0]);
            return null;
        }
    }

    private static class DeleteAllWorkoutAsyncTask extends AsyncTask<Void, Void, Void>{
        private WorkoutDao workoutDao;

        private DeleteAllWorkoutAsyncTask(WorkoutDao _workoutDao){
            workoutDao=_workoutDao;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            workoutDao.deleteAllWorkouts();
            return null;
        }
    }

    private static class UpdateExerciseNameAsyncTask extends AsyncTask<Void, Void, Void>{
        private WorkoutDao workoutDao;
        private String oldName;
        private String newName;
        private UpdateExerciseNameAsyncTask(WorkoutDao workoutDao, String oldName, String newName){
            this.workoutDao = workoutDao;
            this.oldName = oldName;
            this.newName = newName;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            workoutDao.updateExerciseName(oldName,newName);
            return null;
        }
    }

    private static class GetExercisesAsyncTask extends AsyncTask<Void, Void, List<WorkoutEntity>>{
        private WorkoutDao workoutDao;
        private WorkoutRepository delegate = null;
        private String workoutSearch;

        private GetExercisesAsyncTask(WorkoutDao _workoutDao, String workout){
            workoutDao = _workoutDao;
            workoutSearch = workout;
        }
        @Override
        protected List<WorkoutEntity> doInBackground(Void... params) {
            return workoutDao.getExercises(workoutSearch);
        }
        @Override
        protected void onPostExecute(List<WorkoutEntity> result){
            delegate.getExercisesFinished(result);
        }
    }

    private void getExercisesFinished(List<WorkoutEntity> results) {
        /*
            Called whenever GetExercisesAsyncTask is finished
        */
        exercises = new ArrayList<>();
        for(WorkoutEntity entity : results){
            exercises.add(entity);
        }
    }
    // endregion

    // region
    // Private classes used to execute the metadata queries using the database access objects (DAOs)
    private static class InsertMetaAsyncTask extends AsyncTask<MetaEntity, Void, Void>{
        private MetaDao metaDao;

        private InsertMetaAsyncTask(MetaDao metaDao){
            this.metaDao = metaDao;
        }
        @Override
        protected Void doInBackground(MetaEntity... metaEntities) {
            metaDao.insert(metaEntities[0]);
            return null;
        }
    }

    private static class UpdateMetaAsyncTask extends AsyncTask<MetaEntity, Void, Void>{
        private MetaDao metaDao;

        private UpdateMetaAsyncTask(MetaDao metaDao){
            this.metaDao=metaDao;
        }
        @Override
        protected Void doInBackground(MetaEntity... metaEntities) {
            metaDao.update(metaEntities[0]);
            return null;
        }
    }

    private static class DeleteMetaAsyncTask extends AsyncTask<MetaEntity, Void, Void>{
        private MetaDao metaDao;

        private DeleteMetaAsyncTask(MetaDao metaDao){
            this.metaDao=metaDao;
        }
        @Override
        protected Void doInBackground(MetaEntity... metaEntities) {
            metaDao.delete(metaEntities[0]);
            return null;
        }
    }

    private static class DeleteAllMetadataAsyncTask extends AsyncTask<Void, Void, Void>{
        private MetaDao metaDao;

        private DeleteAllMetadataAsyncTask(MetaDao metaDao){
            this.metaDao=metaDao;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            metaDao.deleteAllMetadata();
            return null;
        }
    }

    private static class GetCurrentWorkoutMetaAsyncTask extends AsyncTask<Void, Void, MetaEntity>{
        private MetaDao metaDao;
        private WorkoutRepository delegate = null;

        private GetCurrentWorkoutMetaAsyncTask(MetaDao metaDao){
            this.metaDao = metaDao;
        }
        @Override
        protected MetaEntity doInBackground(Void... voids) {
            return metaDao.getCurrentWorkoutMeta();
        }
        @Override
        protected void onPostExecute(MetaEntity result) {
//            delegate.getCurrentWorkoutMetaFinished(result);
        }

    }
//    private void getCurrentWorkoutMetaFinished(MetaEntity result) {
//        /*
//            Called whenever GetExercisesAsyncTask is finished
//        */
//        currentWorkoutMeta = result;
//    }
    // endregion
    // region
    // Private classes used to execute the exercise queries using the database access objects (DAOs)
    private static class InsertExerciseAsyncTask extends AsyncTask<ExerciseEntity, Void, Void>{
        private ExerciseDao exerciseDao;

        private InsertExerciseAsyncTask(ExerciseDao exerciseDao){
            this.exerciseDao=exerciseDao;
        }
        @Override
        protected Void doInBackground(ExerciseEntity... exerciseEntities) {
            exerciseDao.insert(exerciseEntities[0]);
            return null;
        }
    }

    private static class UpdateExerciseAsyncTask extends AsyncTask<ExerciseEntity, Void, Void>{
        private ExerciseDao exerciseDao;

        private UpdateExerciseAsyncTask(ExerciseDao exerciseDao){
            this.exerciseDao=exerciseDao;
        }
        @Override
        protected Void doInBackground(ExerciseEntity... exerciseEntities) {
            exerciseDao.update(exerciseEntities[0]);
            return null;
        }
    }

    private static class DeleteExerciseAsyncTask extends AsyncTask<ExerciseEntity, Void, Void>{
        private ExerciseDao exerciseDao;

        private DeleteExerciseAsyncTask(ExerciseDao exerciseDao){
            this.exerciseDao=exerciseDao;
        }
        @Override
        protected Void doInBackground(ExerciseEntity... exerciseEntities) {
            exerciseDao.delete(exerciseEntities[0]);
            return null;
        }
    }

    private static class DeleteAllExercisesAsyncTask extends AsyncTask<Void, Void, Void>{
        private ExerciseDao exerciseDao;

        private DeleteAllExercisesAsyncTask(ExerciseDao exerciseDao){
            this.exerciseDao = exerciseDao;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            exerciseDao.deleteAllExercises();
            return null;
        }
    }

    private static class GetAllExercisesAsyncTask extends AsyncTask<Void, Void, List<ExerciseEntity>>{
        private ExerciseDao exerciseDao;
        private WorkoutRepository delegate = null;

        private GetAllExercisesAsyncTask(ExerciseDao exerciseDao){
            this.exerciseDao = exerciseDao;
        }
        @Override
        protected List<ExerciseEntity> doInBackground(Void... voids) {
            return exerciseDao.getAllExercises();
        }
        @Override
        protected void onPostExecute(List<ExerciseEntity> result) {
//            delegate.getAllExercisesFinished(result);
        }

    }
//    private void getAllExercisesFinished(List<ExerciseEntity> result) {
//        /*
//            Called whenever GetExercisesAsyncTask is finished
//        */
//        allExercisesResults = result;
//    }
    // endregion
}
