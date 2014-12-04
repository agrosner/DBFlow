package com.grosner.dbflow.structure;

import android.database.Cursor;
import com.grosner.dbflow.sql.builder.ConditionQueryBuilder;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: The base class for a {@link ModelViewClass} adapter that defines how it interacts with the DB.
 */
public abstract class ModelViewAdapter<ModelClass extends Model, ModelViewClass extends BaseModelView<ModelClass>> {

    /**
     * Loads the cursor into a new model view object
     * @param cursor The cursor to query
     * @return The new instance of the {@link ModelViewClass}
     */
    public abstract ModelViewClass loadFromCursor(Cursor cursor);

    /**
     * @return a string of the query that is used to create this model view.
     */
    public abstract String getCreationQuery();

    /**
     * @param modelView The modelview to read values from
     * @return The {@link com.grosner.dbflow.sql.builder.ConditionQueryBuilder} of all its columns
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
