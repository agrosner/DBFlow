package com.raizlabs.android.dbflow.rx.structure;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.ModelAdapter;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import java.util.Collection;
import java.util.concurrent.Callable;

import rx.Completable;
import rx.Single;

/**
 * Description: Wraps most {@link ModelAdapter} modification operations into RX-style constructs.
 */
public class RXModelAdapter<T> extends RXRetrievalAdapter<T> {

    public static <T> RXModelAdapter<T> from(ModelAdapter<T> modelAdapter) {
        return new RXModelAdapter<>(modelAdapter);
    }

    public static <T> RXModelAdapter<T> from(Class<T> table) {
        return new RXModelAdapter<>(table);
    }

    private final ModelAdapter<T> modelAdapter;

    RXModelAdapter(ModelAdapter<T> modelAdapter) {
        super(modelAdapter);
        this.modelAdapter = modelAdapter;
    }

    public RXModelAdapter(Class<T> table) {
        this(FlowManager.getModelAdapter(table));
    }

    public Single<Boolean> save(final T model) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return modelAdapter.save(model);
            }
        });
    }

    public Single<Boolean> save(final T model, final DatabaseWrapper databaseWrapper) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return modelAdapter.save(model, databaseWrapper);
            }
        });
    }

    public Completable saveAll(final Collection<T> models) {
        return Completable.fromCallable(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                modelAdapter.saveAll(models);
                return null;
            }
        });
    }

    public Completable saveAll(final Collection<T> models, final DatabaseWrapper databaseWrapper) {
        return Completable.fromCallable(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                modelAdapter.saveAll(models, databaseWrapper);
                return null;
            }
        });
    }

    public Single<Long> insert(final T model) {
        return Single.fromCallable(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return modelAdapter.insert(model);
            }
        });
    }

    public Single<Long> insert(final T model, final DatabaseWrapper databaseWrapper) {
        return Single.fromCallable(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return modelAdapter.insert(model, databaseWrapper);
            }
        });
    }

    public Completable insertAll(final Collection<T> models) {
        return Completable.fromCallable(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                modelAdapter.insertAll(models);
                return null;
            }
        });
    }

    public Completable insertAll(final Collection<T> models,
                                  final DatabaseWrapper databaseWrapper) {
        return Completable.fromCallable(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                modelAdapter.insertAll(models, databaseWrapper);
                return null;
            }
        });
    }

    public Single<Boolean> update(final T model) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return modelAdapter.update(model);
            }
        });
    }

    public Completable update(final T model, final DatabaseWrapper databaseWrapper) {
        return Completable.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return modelAdapter.update(model, databaseWrapper);
            }
        });
    }

    public Completable updateAll(final Collection<T> models) {
        return Completable.fromCallable(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                modelAdapter.updateAll(models);
                return null;
            }
        });
    }

    public Completable updateAll(final Collection<T> models, final DatabaseWrapper databaseWrapper) {
        return Completable.fromCallable(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                modelAdapter.updateAll(models, databaseWrapper);
                return null;
            }
        });
    }

    public Single<Boolean> delete(final T model) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return modelAdapter.delete(model);
            }
        });
    }

    public Single<Boolean> delete(final T model, final DatabaseWrapper databaseWrapper) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return modelAdapter.delete(model, databaseWrapper);
            }
        });
    }

    public Completable deleteAll(final Collection<T> models) {
        return Completable.fromCallable(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                modelAdapter.deleteAll(models);
                return null;
            }
        });
    }

    public Completable deleteAll(final Collection<T> models, final DatabaseWrapper databaseWrapper) {
        return Completable.fromCallable(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                modelAdapter.deleteAll(models, databaseWrapper);
                return null;
            }
        });
    }
}
