package com.raizlabs.android.dbflow.config;

import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.sql.queriable.ListModelLoader;
import com.raizlabs.android.dbflow.sql.queriable.SingleModelLoader;
import com.raizlabs.android.dbflow.sql.saveable.ModelSaver;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.List;

/**
 * Description: Represents certain table configuration options. This allows you to easily specify
 * certain configuration options for a table.
 */
public final class TableConfig<TModel extends Model> {

    private final Class<TModel> tableClass;
    private final ModelSaver<TModel, ? extends Model, ?> modelSaver;
    private final SingleModelLoader<TModel> singleModelLoader;
    private final ListModelLoader<TModel> listModelLoader;

    TableConfig(Builder<TModel> builder) {
        tableClass = builder.tableClass;
        modelSaver = builder.modelSaver;
        singleModelLoader = builder.singleModelLoader;
        listModelLoader = builder.listModelLoader;
    }

    public Class<? extends Model> tableClass() {
        return tableClass;
    }

    public ModelSaver<TModel, ? extends Model, ?> modelSaver() {
        return modelSaver;
    }

    public ListModelLoader<TModel> listModelLoader() {
        return listModelLoader;
    }

    public SingleModelLoader<TModel> singleModelLoader() {
        return singleModelLoader;
    }

    public static final class Builder<TModel extends Model> {

        final Class<TModel> tableClass;
        ModelSaver<TModel, ? extends Model, ?> modelSaver;
        SingleModelLoader<TModel> singleModelLoader;
        ListModelLoader<TModel> listModelLoader;
        SingleModelLoader<TModel> modelContainerModelLoader;
        ListModelLoader<TModel> modelContainerListModelLoader;

        public Builder(Class<TModel> tableClass) {
            this.tableClass = tableClass;
        }

        /**
         * Define how the table saves data into the DB from its associated {@link TModel}. This
         * will override the default.
         */
        public Builder<TModel> modelSaver(ModelSaver<TModel, ? extends Model, ?> modelSaver) {
            this.modelSaver = modelSaver;
            return this;
        }

        /**
         * Define how the table loads single models. This will override the default.
         */
        public Builder<TModel> singleModelLoader(SingleModelLoader<TModel> singleModelLoader) {
            this.singleModelLoader = singleModelLoader;
            return this;
        }

        /**
         * Define how the table loads a {@link List} of items. This will override the default.
         */
        public Builder<TModel> listModelLoader(ListModelLoader<TModel> listModelLoader) {
            this.listModelLoader = listModelLoader;
            return this;
        }

        /**
         * If a {@link ModelContainer} annotation specified for this table, define how models are
         * loaded individually from the DB. This will override the default.
         */
        public Builder<TModel> modelContainerModelLoader(SingleModelLoader<TModel> modelContainerModelLoader) {
            this.modelContainerModelLoader = modelContainerModelLoader;
            return this;
        }

        /**
         * If a {@link ModelContainer} annotation specified for this table, define how a {@link List}
         * of models are loaded from the DB. This will override the default.
         */
        public Builder<TModel> modelContainerListModelLoader(ListModelLoader<TModel> modelContainerListModelLoader) {
            this.modelContainerListModelLoader = modelContainerListModelLoader;
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
