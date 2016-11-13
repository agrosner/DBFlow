package com.raizlabs.android.dbflow.structure;

import android.database.Cursor;

import com.raizlabs.android.dbflow.config.DatabaseDefinition;

/**
 * Description: The base class for a {@link TModelView} adapter that defines how it interacts with the DB.
 */
public abstract class ModelViewAdapter<TModelView extends BaseModelView>
        extends InstanceAdapter<TModelView> {

    public ModelViewAdapter(DatabaseDefinition databaseDefinition) {
        super(databaseDefinition);
    }

    /**
     * Creates a new {@link TModelView} and loads the cursor into it.
     *
     * @param cursor The cursor to query
     * @return The new model view with the cursor data in it.
     */
    public TModelView loadFromCursor(Cursor cursor) {
        TModelView TModelView = newInstance();
        loadFromCursor(cursor, TModelView);
        return TModelView;
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
