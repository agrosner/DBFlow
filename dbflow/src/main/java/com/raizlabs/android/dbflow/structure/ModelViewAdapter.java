package com.raizlabs.android.dbflow.structure;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.config.DatabaseDefinition;

/**
 * Description: The base class for a {@link TModelView} adapter that defines how it interacts with the DB.
 */
public abstract class ModelViewAdapter<TModelView>
        extends InstanceAdapter<TModelView> {

    public ModelViewAdapter(@NonNull DatabaseDefinition databaseDefinition) {
        super(databaseDefinition);
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
