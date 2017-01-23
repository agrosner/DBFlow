package com.raizlabs.android.dbflow.rx.structure;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.RetrievalAdapter;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import java.util.concurrent.Callable;

import rx.Single;

import static rx.Single.fromCallable;

/**
 * Description: Mirrors the {@link RetrievalAdapter} with subset of exposed methods, mostly for
 * {@link #load(Object)} and {@link #exists(Object)}
 */
public class RXRetrievalAdapter<TModel> {

    private final RetrievalAdapter<TModel> retrievalAdapter;

    public RXRetrievalAdapter(RetrievalAdapter<TModel> retrievalAdapter) {
        this.retrievalAdapter = retrievalAdapter;
    }

    public RXRetrievalAdapter(Class<TModel> table) {
        this(FlowManager.getInstanceAdapter(table));
    }

    public Single<Void> load(final TModel model) {
        return fromCallable(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                retrievalAdapter.load(model);
                return null;
            }
        });
    }

    public Single<Void> load(final TModel model, final DatabaseWrapper databaseWrapper) {
        return fromCallable(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                retrievalAdapter.load(model, databaseWrapper);
                return null;
            }
        });
    }

    /**
     * @param model The model to query values from
     * @return True if it exists as a row in the corresponding database table
     */
    public Single<Boolean> exists(final TModel model) {
        return fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return retrievalAdapter.exists(model);
            }
        });
    }

    public Single<Boolean> exists(final TModel model, final DatabaseWrapper wrapper) {
        return fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return retrievalAdapter.exists(model, wrapper);
            }
        });
    }
}
