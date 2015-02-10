package com.raizlabs.android.dbflow.structure;

import android.database.Cursor;

/**
 * Description: Provides a base retrieval interface for all {@link com.raizlabs.android.dbflow.structure.Model} backed
 * adapters.
 */
public interface RetrievalAdapter<ModelClass extends Model> {

    /**
     * Assigns the {@link android.database.Cursor} data into the specified {@link ModelClass}
     *
     * @param model  The model to assign cursor data to
     * @param cursor The cursor to load into the model
     */
    public void loadFromCursor(Cursor cursor, ModelClass model);


    /**
     * @param model The model to query values from
     * @return True if it exists as VIEW row in the database table
     */
    public boolean exists(ModelClass model);

}
