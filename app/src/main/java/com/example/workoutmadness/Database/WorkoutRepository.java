package com.example.workoutmadness.Database;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

public class WorkoutRepository {
    private WorkoutDao workoutDao;
    private LogDao logDao;
    private LiveData<List<WorkoutEntity>> allWorkouts;
    private LiveData<List<LogEntity>> allLogs;
    private MutableLiveData<String> currentWorkout = new MutableLiveData<String>();
    private void asyncFinished(String result) {
        currentWorkout.setValue(result);
    }

    public WorkoutRepository(Application application){
        WorkoutDatabase database = WorkoutDatabase.getInstance(application);
        workoutDao = database.workoutDao();
        logDao = database.logDao();
        allWorkouts = workoutDao.getAllWorkouts();
        allLogs = logDao.getAllLogs();
    }

    // region View Model methods for workout table
    public void insertWorkoutEntity(WorkoutEntity workout){
        new InsertWorkoutAsyncTask(workoutDao).execute(workout);
    }
    public void updateWorkoutEntity(WorkoutEntity workout){
        new UpdateWorkoutAsyncTask(workoutDao).execute(workout);

    }
    public void deleteWorkoutEntity(WorkoutEntity workout){
        new DeleteWorkoutAsyncTask(workoutDao).execute(workout);
    }
    public void deleteAllWorkouts(){
        new DeleteAllWorkoutAsyncTask(workoutDao).execute();
    }

    public LiveData<List<WorkoutEntity>> getAllWorkouts() {
        return allWorkouts;
    }
    // endregion

    // region View Model methods for log table
    public void insertLogEntity(LogEntity log){
        new InsertLogAsyncTask(logDao).execute(log);
    }
    public void updateLogEntity(LogEntity log){
        new UpdateLogAsyncTask(logDao).execute(log);
    }
    public void deleteLogEntity(LogEntity log){
        new DeleteLogAsyncTask(logDao).execute(log);
    }
    public void deleteAllLogs(){
        new DeleteAllLogsAsyncTask(logDao).execute();
    }
    public void getCurrentWorkout(){
        GetCurrentWorkoutAsyncTask task = new GetCurrentWorkoutAsyncTask(logDao);
        task.delegate = this;
        task.execute();
    }
    public LiveData<List<LogEntity>> getAllLogs() {
        return allLogs;
    }
    // endregion

    // region Private classes used to execute the workout queries using the database access objects (DAOs)

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
    // endregion

    // region Private classes used to execute the log queries using the database access objects (DAOs)
    private static class InsertLogAsyncTask extends AsyncTask<LogEntity, Void, Void>{
        private LogDao logDao;

        private InsertLogAsyncTask(LogDao _logDao){
            logDao=_logDao;
        }
        @Override
        protected Void doInBackground(LogEntity... logEntities) {
            logDao.insert(logEntities[0]);
            return null;
        }
    }

    private static class UpdateLogAsyncTask extends AsyncTask<LogEntity, Void, Void>{
        private LogDao logDao;

        private UpdateLogAsyncTask(LogDao _logDao){
            logDao=_logDao;
        }
        @Override
        protected Void doInBackground(LogEntity... logEntities) {
            logDao.update(logEntities[0]);
            return null;
        }
    }

    private static class DeleteLogAsyncTask extends AsyncTask<LogEntity, Void, Void>{
        private LogDao logDao;

        private DeleteLogAsyncTask(LogDao _logDao){
            logDao=_logDao;
        }
        @Override
        protected Void doInBackground(LogEntity... logEntities) {
            logDao.delete(logEntities[0]);
            return null;
        }
    }

    private static class DeleteAllLogsAsyncTask extends AsyncTask<Void, Void, Void>{
        private LogDao logDao;

        private DeleteAllLogsAsyncTask(LogDao _logDao){
            logDao=_logDao;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            logDao.deleteAllLogs();
            return null;
        }
    }

    private static class GetCurrentWorkoutAsyncTask extends AsyncTask<Void, Void, String>{
        private LogDao logDao;
        private WorkoutRepository delegate=null;

        private GetCurrentWorkoutAsyncTask(LogDao _logDao){
            logDao = _logDao;
        }
        @Override
        protected String doInBackground(Void... voids) {
            return logDao.getCurrentWorkout();
        }
        @Override
        protected void onPostExecute(String result) {
            delegate.asyncFinished(result);
        }

    }
    // endregion
}
