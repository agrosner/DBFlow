package com.raizlabs.android.dbflow.config;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.queriable.ListModelLoader;
import com.raizlabs.android.dbflow.sql.queriable.ModelContainerLoader;
import com.raizlabs.android.dbflow.sql.queriable.SingleModelLoader;
import com.raizlabs.android.dbflow.sql.saveable.ModelSaver;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.ModelAdapter;
import com.raizlabs.android.dbflow.structure.container.ModelContainer;
import com.raizlabs.android.dbflow.structure.container.ModelContainerAdapter;

import java.util.List;

/**
 * Description: Represents certain table configuration options. This allows you to easily specify
 * certain configuration options for a table.
 */
public final class TableConfig<TModel extends Model> {

    private final Class<TModel> tableClass;
    private final ModelSaver<TModel, TModel, ModelAdapter<TModel>> modelSaver;
    private final SingleModelLoader<TModel> singleModelLoader;
    private final ListModelLoader<TModel> listModelLoader;
    private final ModelContainerLoader<TModel> modelContainerLoader;
    private final ModelSaver<TModel, ModelContainer<TModel, ?>, ModelContainerAdapter<TModel>>
            modelContainerModelSaver;

    TableConfig(Builder<TModel> builder) {
        tableClass = builder.tableClass;
        modelSaver = builder.modelAdapterModelSaver;
        singleModelLoader = builder.singleModelLoader;
        listModelLoader = builder.listModelLoader;
        modelContainerLoader = builder.modelContainerLoader;
        modelContainerModelSaver = builder.modelContainerModelSaver;
    }

    public Class<? extends Model> tableClass() {
        return tableClass;
    }

    public ModelSaver<TModel, TModel, ModelAdapter<TModel>> modelSaver() {
        return modelSaver;
    }

    public ListModelLoader<TModel> listModelLoader() {
        return listModelLoader;
    }

    public SingleModelLoader<TModel> singleModelLoader() {
        return singleModelLoader;
    }

    public ModelContainerLoader<TModel> modelContainerLoader() {
        return modelContainerLoader;
    }

    public ModelSaver<TModel, ModelContainer<TModel, ?>, ModelContainerAdapter<TModel>> modelContainerModelSaver() {
        return modelContainerModelSaver;
    }

    public static final class Builder<TModel extends Model> {

        final Class<TModel> tableClass;
        ModelSaver<TModel, TModel, ModelAdapter<TModel>> modelAdapterModelSaver;
        ModelSaver<TModel, ModelContainer<TModel, ?>, ModelContainerAdapter<TModel>> modelContainerModelSaver;
        SingleModelLoader<TModel> singleModelLoader;
        ListModelLoader<TModel> listModelLoader;
        ModelContainerLoader<TModel> modelContainerLoader;

        public Builder(Class<TModel> tableClass) {
            this.tableClass = tableClass;
        }

        /**
         * Define how the {@link ModelAdapter} saves data into the DB from its associated {@link TModel}. This
         * will override the default.
         */
        public Builder<TModel> modelAdapterModelSaver(@NonNull ModelSaver<TModel, TModel, ModelAdapter<TModel>> modelSaver) {
            this.modelAdapterModelSaver = modelSaver;
            return this;
        }

        /**
         * Define how the {@link ModelContainerAdapter} saves data into the DB from its associated {@link TModel}. This
         * will override the default.
         */
        public Builder<TModel> modelContainerModelSaver(@NonNull ModelSaver<TModel, ModelContainer<TModel, ?>,
                ModelContainerAdapter<TModel>> modelSaver) {
            this.modelContainerModelSaver = modelSaver;
            return this;
        }

        /**
         * Define how the table loads single models. This will override the default.
         */
        public Builder<TModel> singleModelLoader(@NonNull SingleModelLoader<TModel> singleModelLoader) {
            this.singleModelLoader = singleModelLoader;
            return this;
        }

        /**
         * Define how the table loads a {@link List} of items. This will override the default.
         */
        public Builder<TModel> listModelLoader(@NonNull ListModelLoader<TModel> listModelLoader) {
            this.listModelLoader = listModelLoader;
            return this;
        }

        /**
         * If a {@link ModelContainer} annotation specified for this table, define how models are
         * loaded individually from the DB. This will override the default.
         */
        public Builder<TModel> modelContainerLoader(@NonNull ModelContainerLoader<TModel> modelContainerModelLoader) {
            this.modelContainerLoader = modelContainerModelLoader;
            return this;
        }

        /**
         * @return A new {@link TableConfig}. Subsequent calls to this method produce a new instance
         * of {@link TableConfig}.
         */
        public TableConfig build() {
            return new TableConfig<>(this);
        }
    }
}
