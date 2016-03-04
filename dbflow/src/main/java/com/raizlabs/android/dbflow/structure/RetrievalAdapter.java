package com.raizlabs.android.dbflow.structure;

import android.database.Cursor;
import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.ConditionGroup;
import com.raizlabs.android.dbflow.sql.queriable.ListModelLoader;
import com.raizlabs.android.dbflow.sql.queriable.SingleModelLoader;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

/**
 * Description: Provides a base retrieval class for all {@link Model} backed
 * adapters.
 */
public abstract class RetrievalAdapter<ModelClass extends Model, TableClass extends Model> {

    private SingleModelLoader<TableClass> singleModelLoader;
    private ListModelLoader<TableClass> listModelLoader;

    /**
     * Assigns the {@link android.database.Cursor} data into the specified {@link ModelClass}
     *
     * @param model  The model to assign cursor data to
     * @param cursor The cursor to load into the model
     */
    public abstract void loadFromCursor(Cursor cursor, ModelClass model);

    /**
     * @param model The model to query values from
     * @return True if it exists as a row in the corresponding database table
     */
    public boolean exists(ModelClass model) {
        return exists(model, FlowManager.getDatabaseForTable(getModelClass()).getWritableDatabase());
    }

    /**
     * @param model The model to query values from
     * @return True if it exists as a row in the corresponding database table
     */
    public abstract boolean exists(ModelClass model, DatabaseWrapper databaseWrapper);

    /**
     * @param model The primary condition clause.
     * @return The clause that contains necessary primary conditions for this table.
     */
    public abstract ConditionGroup getPrimaryConditionClause(ModelClass model);

    /**
     * @return the model class this adapter corresponds to
     */
    public abstract Class<TableClass> getModelClass();


    public ListModelLoader<TableClass> getListModelLoader() {
        if (listModelLoader == null) {
            listModelLoader = createListModelLoader();
        }
        return listModelLoader;
    }

    protected ListModelLoader<TableClass> createListModelLoader() {
        return new ListModelLoader<>(getModelClass());
    }

    public SingleModelLoader<TableClass> getSingleModelLoader() {
        if (singleModelLoader == null) {
            singleModelLoader = createSingleModelLoader();
        }
        return singleModelLoader;
    }

    /**
     * Overrides the default implementation and allows you to provide your own implementation. Defines
     * how a single {@link TableClass} is loaded.
     *
     * @param singleModelLoader The loader to use.
     */
    public void setSingleModelLoader(@NonNull SingleModelLoader<TableClass> singleModelLoader) {
        this.singleModelLoader = singleModelLoader;
    }

    /**
     * Overrides the default implementation and allows you to provide your own implementation. Defines
     * how a list of {@link TableClass} are loaded.
     *
     * @param listModelLoader The loader to use.
     */
    public void setListModelLoader(@NonNull ListModelLoader<TableClass> listModelLoader) {
        this.listModelLoader = listModelLoader;
    }

    protected SingleModelLoader<TableClass> createSingleModelLoader() {
        return new SingleModelLoader<>(getModelClass());
    }
}
