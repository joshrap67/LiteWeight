package com.example.workoutmadness.Database;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

public class WorkoutRepository {
    private WorkoutDao workoutDao;
    private LiveData<List<WorkoutEntity>> allWorkouts;

    public WorkoutRepository(Application application){
        WorkoutDatabase database = WorkoutDatabase.getInstance(application);
        workoutDao = database.workoutDao();
        allWorkouts=workoutDao.getAllWorkouts();
    }
    // view model will interact with these methods.
    public void insert(WorkoutEntity workout){
        new InsertWorkoutAsyncTask(workoutDao).execute(workout);
    }
    public void update(WorkoutEntity workout){
        new UpdateWorkoutAsyncTask(workoutDao).execute(workout);

    }
    public void delete(WorkoutEntity workout){
        new DeleteWorkoutAsyncTask(workoutDao).execute(workout);

    }
    public void deleteAllWorkouts(){
        new DeleteAllWorkoutAsyncTask(workoutDao).execute();
    }

    public LiveData<List<WorkoutEntity>> getAllWorkouts() {
        return allWorkouts;
    }

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
}
