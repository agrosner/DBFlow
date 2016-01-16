package com.raizlabs.android.dbflow.structure;

import android.database.Cursor;

import com.raizlabs.android.dbflow.sql.language.ConditionGroup;
import com.raizlabs.android.dbflow.sql.queriable.ListModelLoader;
import com.raizlabs.android.dbflow.sql.queriable.SingleModelLoader;

/**
 * Description: Provides a base retrieval class for all {@link Model} backed
 * adapters.
 */
public abstract class RetrievalAdapter<ModelClass extends Model> {

    private SingleModelLoader<ModelClass> singleModelLoader;
    private ListModelLoader<ModelClass> listModelLoader;

    /**
     * Assigns the {@link android.database.Cursor} data into the specified {@link ModelClass}
     *
     * @param model  The model to assign cursor data to
     * @param cursor The cursor to load into the model
     */
    public abstract void loadFromCursor(Cursor cursor, ModelClass model);

    /**
     * @param model The model to query values from
     * @return True if it exists as VIEW row in the database table
     */
    public abstract boolean exists(ModelClass model);

    /**
     * @param model The primary condition clause.
     * @return The clause that contains necessary
     */
    public abstract ConditionGroup getPrimaryConditionClause(ModelClass model);

    /**
     * @return the model class this adapter corresponds to
     */
    public abstract Class<ModelClass> getModelClass();


    public ListModelLoader<ModelClass> getListModelLoader() {
        if (listModelLoader == null) {
            listModelLoader = createListModelLoader();
        }
        return listModelLoader;
    }

    protected ListModelLoader<ModelClass> createListModelLoader() {
        return new ListModelLoader<>(getModelClass());
    }

    public SingleModelLoader<ModelClass> getSingleModelLoader() {
        if (singleModelLoader == null) {
            singleModelLoader = createSingleModelLoader();
        }
        return singleModelLoader;
    }

    protected SingleModelLoader<ModelClass> createSingleModelLoader() {
        return new SingleModelLoader<>(getModelClass());
    }
}
