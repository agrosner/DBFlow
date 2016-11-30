package com.raizlabs.android.dbflow.structure;

/**
 * Description: The main interface for which all DB-based objects should implement.
 */
public interface Model {

    /**
     * Returned when {@link #insert()} occurs in an async state or some kind of issue occurs.
     */
    long INVALID_ROW_ID = -1;

    /**
     * Loads from the database the most recent version of the model based on it's primary keys.
     */
    void load();

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

    /**
     * @return true if this object exists in the DB. It combines all of it's primary key fields
     * into a SELECT query and checks to see if any results occur.
     */
    boolean exists();

}
