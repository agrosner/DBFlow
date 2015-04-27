package com.raizlabs.android.dbflow.structure;

import android.database.Cursor;

/**
 * Author: andrewgrosner
 * Description: The base class for a {@link ModelViewClass} adapter that defines how it interacts with the DB.
 */
public abstract class ModelViewAdapter<ModelClass extends Model, ModelViewClass extends BaseModelView<ModelClass>>
        implements InstanceAdapter<ModelViewClass, ModelViewClass> {

    /**
     * Creates a new {@link ModelViewClass} and loads the cursor into it.
     *
     * @param cursor The cursor to query
     * @return The new model view with the cursor data in it.
     */
    public ModelViewClass loadFromCursor(Cursor cursor) {
        ModelViewClass modelViewClass = newInstance();
        loadFromCursor(cursor, modelViewClass);
        return modelViewClass;
    }

    /**
     * @return a string of the query that is used to create this model view.
     */
    public abstract String getCreationQuery();

    /**
     * @return The name of this view in the database
     */
    public abstract String getViewName();
}
