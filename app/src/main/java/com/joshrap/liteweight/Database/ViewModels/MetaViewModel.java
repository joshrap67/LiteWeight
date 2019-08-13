package com.joshrap.liteweight.Database.ViewModels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

import com.joshrap.liteweight.Database.Entities.*;
import com.joshrap.liteweight.Database.Repositories.*;

import java.util.ArrayList;

public class MetaViewModel extends AndroidViewModel {
    private WorkoutRepository repository;

    public MetaViewModel(@NonNull Application application) {
        super(application);
        repository = new WorkoutRepository(application);
    }

    public void insert(MetaEntity entity) {
        repository.insertMetaEntity(entity);
    }

    public void update(MetaEntity entity) {
        repository.updateMetaEntity(entity);
    }

    public void delete(MetaEntity entity) {
        repository.deleteMetaEntity(entity);
    }

    public void deleteAllMeta() {
        repository.deleteAllMetadata();
    }

    public ArrayList<MetaEntity> getAllMetadata() {
        return new ArrayList<>(repository.getAllMetadata());
    }

    public MetaEntity getCurrentWorkoutMeta(){
        return repository.getCurrentWorkoutMeta();
    }
}
