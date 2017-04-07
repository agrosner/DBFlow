package com.raizlabs.android.dbflow.structure.database.transaction;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.structure.InternalAdapter;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Description: Similiar to {@link ProcessModelTransaction} in that it allows you to store a {@link List} of
 * {@link Model}, except that it performs it as efficiently as possible. Also due to way the class operates,
 * only one kind of {@link TModel} is allowed.
 */
public class FastStoreModelTransaction<TModel> implements ITransaction {

    @NonNull
    public static <TModel> Builder<TModel> saveBuilder(@NonNull InternalAdapter<TModel> internalAdapter) {
        return new Builder<>(new ProcessModelList<TModel>() {
            @Override
            public void processModel(@NonNull List<TModel> tModels, InternalAdapter<TModel> adapter, DatabaseWrapper wrapper) {
                adapter.saveAll(tModels, wrapper);
            }
        }, internalAdapter);
    }

    @NonNull
    public static <TModel> Builder<TModel> insertBuilder(@NonNull InternalAdapter<TModel> internalAdapter) {
        return new Builder<>(new ProcessModelList<TModel>() {
            @Override
            public void processModel(@NonNull List<TModel> tModels, InternalAdapter<TModel> adapter, DatabaseWrapper wrapper) {
                adapter.insertAll(tModels, wrapper);
            }
        }, internalAdapter);
    }

    @NonNull
    public static <TModel> Builder<TModel> updateBuilder(@NonNull InternalAdapter<TModel> internalAdapter) {
        return new Builder<>(new ProcessModelList<TModel>() {
            @Override
            public void processModel(@NonNull List<TModel> tModels, InternalAdapter<TModel> adapter, DatabaseWrapper wrapper) {
                adapter.updateAll(tModels, wrapper);
            }
        }, internalAdapter);
    }

    /**
     * Description: Simple interface for acting on a model in a Transaction or list of {@link Model}
     */
    interface ProcessModelList<TModel> {

        /**
         * Called when processing models
         *
         * @param modelList The model list to process
         */
        void processModel(@NonNull List<TModel> modelList, InternalAdapter<TModel> adapter,
                          DatabaseWrapper wrapper);
    }

    final List<TModel> models;
    final ProcessModelList<TModel> processModelList;
    final InternalAdapter<TModel> internalAdapter;

    FastStoreModelTransaction(Builder<TModel> builder) {
        models = builder.models;
        processModelList = builder.processModelList;
        internalAdapter = builder.internalAdapter;
    }

    @Override
    public void execute(DatabaseWrapper databaseWrapper) {
        if (models != null) {
            processModelList.processModel(models, internalAdapter, databaseWrapper);
        }
    }

    /**
     * Makes it easy to build a {@link ProcessModelTransaction}.
     *
     * @param <TModel>
     */
    public static final class Builder<TModel> {

        private final ProcessModelList<TModel> processModelList;
        @NonNull private final InternalAdapter<TModel> internalAdapter;
        List<TModel> models = new ArrayList<>();

        Builder(@NonNull ProcessModelList<TModel> processModelList,
                @NonNull InternalAdapter<TModel> internalAdapter) {
            this.processModelList = processModelList;
            this.internalAdapter = internalAdapter;
        }

        @NonNull
        public Builder<TModel> add(TModel model) {
            models.add(model);
            return this;
        }

        /**
         * Adds all specified models to the {@link ArrayList}.
         */
        @NonNull
        @SafeVarargs
        public final Builder<TModel> addAll(TModel... models) {
            this.models.addAll(Arrays.asList(models));
            return this;
        }

        /**
         * Adds a {@link Collection} of {@link Model} to the existing {@link ArrayList}.
         */
        @NonNull
        public Builder<TModel> addAll(Collection<? extends TModel> models) {
            if (models != null) {
                this.models.addAll(models);
            }
            return this;
        }

        /**
         * @return A new {@link ProcessModelTransaction}. Subsequent calls to this method produce
         * new instances.
         */
        @NonNull
        public FastStoreModelTransaction<TModel> build() {
            return new FastStoreModelTransaction<>(this);
        }
    }
}
