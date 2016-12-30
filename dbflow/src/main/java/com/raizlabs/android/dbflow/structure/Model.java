package com.raizlabs.android.dbflow.structure;

public interface Model extends ReadOnlyModel {

    /**
     * Returned when {@link #insert()} occurs in an async state or some kind of issue occurs.
     */
    long INVALID_ROW_ID = -1;

    /**
     * Saves the object in the DB.
     */
    void save();

    /**
     * Deletes the object in the DB
     */
    void delete();

    /**
     * Updates an object in the DB. Does not insert on failure.
     */
    void update();

    /**
     * Inserts the object into the DB
     */
    long insert();

}
