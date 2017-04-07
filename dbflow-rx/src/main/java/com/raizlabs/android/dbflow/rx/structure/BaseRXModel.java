package com.raizlabs.android.dbflow.rx.structure;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.annotation.ColumnIgnore;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.InvalidDBConfiguration;
import com.raizlabs.android.dbflow.structure.ModelAdapter;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import rx.Completable;
import rx.Single;

/**
 * Description: Similar to {@link BaseModel} with RX constructs. Extend this for convenience methods.
 */
@SuppressWarnings("unchecked")
public class BaseRXModel {

    @ColumnIgnore
    private transient RXModelAdapter modelAdapter;

    @NonNull
    public Single<Boolean> save() {
        return getRXModelAdapter().save(this);
    }

    @NonNull
    public Single<Boolean> save(DatabaseWrapper databaseWrapper) {
        return getRXModelAdapter().save(this, databaseWrapper);
    }

    @NonNull
    public Completable load() {
        return getRXModelAdapter().load(this);
    }

    @NonNull
    public Completable load(DatabaseWrapper databaseWrapper) {
        return getRXModelAdapter().load(this, databaseWrapper);
    }

    @NonNull
    public Single<Boolean> delete() {
        return getRXModelAdapter().delete(this);
    }

    @NonNull
    public Single<Boolean> delete(DatabaseWrapper databaseWrapper) {
        return getRXModelAdapter().delete(this, databaseWrapper);
    }

    @NonNull
    public Single<Boolean> update() {
        return getRXModelAdapter().update(this);
    }

    @NonNull
    public Single<Boolean> update(DatabaseWrapper databaseWrapper) {
        return getRXModelAdapter().update(this, databaseWrapper);
    }

    @NonNull
    public Single<Long> insert() {
        return getRXModelAdapter().insert(this);
    }

    @NonNull
    public Single<Long> insert(DatabaseWrapper databaseWrapper) {
        return getRXModelAdapter().insert(this, databaseWrapper);
    }

    @NonNull
    public Single<Boolean> exists() {
        return getRXModelAdapter().exists(this);
    }

    @NonNull
    public Single<Boolean> exists(DatabaseWrapper databaseWrapper) {
        return getRXModelAdapter().exists(this, databaseWrapper);
    }

    /**
     * @return The associated {@link ModelAdapter}. The {@link FlowManager}
     * may throw a {@link InvalidDBConfiguration} for this call if this class
     * is not associated with a table, so be careful when using this method.
     */
    @NonNull
    private RXModelAdapter getRXModelAdapter() {
        if (modelAdapter == null) {
            modelAdapter = new RXModelAdapter<>(getClass());
        }
        return modelAdapter;
    }
}
