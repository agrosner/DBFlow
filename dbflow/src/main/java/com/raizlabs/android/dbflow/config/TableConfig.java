package com.raizlabs.android.dbflow.config;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.sql.queriable.ListModelLoader;
import com.raizlabs.android.dbflow.sql.queriable.ModelContainerLoader;
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
    private final ModelSaver modelSaver;
    private final SingleModelLoader<TModel> singleModelLoader;
    private final ListModelLoader<TModel> listModelLoader;
    private final ModelContainerLoader<TModel> modelContainerLoader;

    TableConfig(Builder<TModel> builder) {
        tableClass = builder.tableClass;
        modelSaver = builder.modelSaver;
        singleModelLoader = builder.singleModelLoader;
        listModelLoader = builder.listModelLoader;
        modelContainerLoader = builder.modelContainerLoader;
    }

    public Class<? extends Model> tableClass() {
        return tableClass;
    }

    public ModelSaver modelSaver() {
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

    public static final class Builder<TModel extends Model> {

        final Class<TModel> tableClass;
        ModelSaver modelSaver;
        SingleModelLoader<TModel> singleModelLoader;
        ListModelLoader<TModel> listModelLoader;
        ModelContainerLoader<TModel> modelContainerLoader;

        public Builder(Class<TModel> tableClass) {
            this.tableClass = tableClass;
        }

        /**
         * Define how the table saves data into the DB from its associated {@link TModel}. This
         * will override the default.
         */
        public Builder<TModel> modelSaver(@NonNull ModelSaver modelSaver) {
            this.modelSaver = modelSaver;
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
