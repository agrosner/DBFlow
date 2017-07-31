package com.raizlabs.android.dbflow.config;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.sql.queriable.ListModelLoader;
import com.raizlabs.android.dbflow.sql.queriable.SingleModelLoader;
import com.raizlabs.android.dbflow.sql.saveable.ModelSaver;
import com.raizlabs.android.dbflow.structure.ModelAdapter;

import java.util.List;

/**
 * Description: Represents certain table configuration options. This allows you to easily specify
 * certain configuration options for a table.
 */
public final class TableConfig<TModel> {

    public static <TModel> TableConfig.Builder<TModel> builder(Class<TModel> tableClass) {
        return new TableConfig.Builder<>(tableClass);
    }

    private final Class<TModel> tableClass;
    private final ModelSaver<TModel> modelSaver;
    private final SingleModelLoader<TModel> singleModelLoader;
    private final ListModelLoader<TModel> listModelLoader;

    TableConfig(Builder<TModel> builder) {
        tableClass = builder.tableClass;
        modelSaver = builder.modelAdapterModelSaver;
        singleModelLoader = builder.singleModelLoader;
        listModelLoader = builder.listModelLoader;
    }

    @NonNull
    public Class<?> tableClass() {
        return tableClass;
    }

    @Nullable
    public ModelSaver<TModel> modelSaver() {
        return modelSaver;
    }

    @Nullable
    public ListModelLoader<TModel> listModelLoader() {
        return listModelLoader;
    }

    @Nullable
    public SingleModelLoader<TModel> singleModelLoader() {
        return singleModelLoader;
    }

    public static final class Builder<TModel> {

        final Class<TModel> tableClass;
        ModelSaver<TModel> modelAdapterModelSaver;
        SingleModelLoader<TModel> singleModelLoader;
        ListModelLoader<TModel> listModelLoader;

        public Builder(@NonNull Class<TModel> tableClass) {
            this.tableClass = tableClass;
        }

        /**
         * Define how the {@link ModelAdapter} saves data into the DB from its associated {@link TModel}. This
         * will override the default.
         */
        @NonNull
        public Builder<TModel> modelAdapterModelSaver(@NonNull ModelSaver<TModel> modelSaver) {
            this.modelAdapterModelSaver = modelSaver;
            return this;
        }

        /**
         * Define how the table loads single models. This will override the default.
         */
        @NonNull
        public Builder<TModel> singleModelLoader(@NonNull SingleModelLoader<TModel> singleModelLoader) {
            this.singleModelLoader = singleModelLoader;
            return this;
        }

        /**
         * Define how the table loads a {@link List} of items. This will override the default.
         */
        @NonNull
        public Builder<TModel> listModelLoader(@NonNull ListModelLoader<TModel> listModelLoader) {
            this.listModelLoader = listModelLoader;
            return this;
        }

        /**
         * @return A new {@link TableConfig}. Subsequent calls to this method produce a new instance
         * of {@link TableConfig}.
         */
        @NonNull
        public TableConfig build() {
            return new TableConfig<>(this);
        }
    }
}
