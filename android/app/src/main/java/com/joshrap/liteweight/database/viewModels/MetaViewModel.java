package com.joshrap.liteweight.database.viewModels;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.annotation.NonNull;

import com.joshrap.liteweight.database.entities.*;
import com.joshrap.liteweight.database.repositories.*;

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

    public MetaEntity getCurrentWorkoutMeta() {
        return repository.getCurrentWorkoutMeta();
    }
}
