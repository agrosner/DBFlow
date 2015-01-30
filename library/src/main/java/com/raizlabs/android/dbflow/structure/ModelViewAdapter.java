package com.raizlabs.android.dbflow.structure;

import android.database.Cursor;

import com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder;

/**
 * Author: andrewgrosner
 * Description: The base class for a {@link ModelViewClass} adapter that defines how it interacts with the DB.
 */
public abstract class ModelViewAdapter<ModelClass extends Model, ModelViewClass extends BaseModelView<ModelClass>> {

    /**
     * Loads the cursor into a new model view object
     *
     * @param cursor         The cursor to query
     * @param modelViewClass The model view to assign the cursor data to
     */
    public abstract void loadFromCursor(Cursor cursor, ModelViewClass modelViewClass);

    public ModelViewClass loadFromCursor(Cursor cursor) {
        ModelViewClass modelViewClass = newInstance();
        loadFromCursor(cursor, modelViewClass);
        return modelViewClass;
    }

    /**
     * @return A new instace of the {@link ModelViewClass} must have a default constructor.
     */
    protected abstract ModelViewClass newInstance();

    /**
     * @return a string of the query that is used to create this model view.
     */
    public abstract String getCreationQuery();

    /**
     * @param modelView The modelview to read values from
     * @return The {@link com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder} of all its columns
     */
    public abstract ConditionQueryBuilder<ModelViewClass> getPrimaryModelWhere(ModelViewClass modelView);

    /**
     * @param model The model to query values from
     * @return True if it exists as VIEW row in the database table
     */
    public abstract boolean exists(ModelViewClass model);

    /**
     * @return The name of this view in the database
     */
    public abstract String getViewName();
}
