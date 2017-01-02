package com.raizlabs.android.dbflow.structure;

public interface ReadOnlyModel {

    /**
     * Loads from the database the most recent version of the model based on it's primary keys.
     */
    void load();

    /**
     * @return true if this object exists in the DB. It combines all of it's primary key fields
     * into a SELECT query and checks to see if any results occur.
     */
    boolean exists();

}
