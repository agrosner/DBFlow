package com.raizlabs.android.dbflow.structure;

import com.raizlabs.android.dbflow.sql.From;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Describes the SQL query for a view. It is a virtual table that we can query from.
 */
public interface ModelView<ModelClass extends Model> {

    /**
     * Returns the {@link com.raizlabs.android.dbflow.sql.From} query that creates this class
     * @return The creation query for creating this view.
     */
    public From<ModelClass> getFrom();

    /**
     * Returns the name of the View in the DB
     * @return
     */
    public String getName();
}
