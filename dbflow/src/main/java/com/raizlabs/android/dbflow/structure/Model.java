package com.raizlabs.android.dbflow.structure;

public interface Model extends ReadOnlyModel {

    /**
     * Returned when {@link #insert()} occurs in an async state or some kind of issue occurs.
     */
    long INVALID_ROW_ID = -1;

    /**
     * Saves the object in the DB.
     *
     * @return true if successful
     */
    boolean save();

    /**
     * Deletes the object in the DB
     *
     * @return true if successful
     */
    boolean delete();

    /**
     * Updates an object in the DB. Does not insert on failure.
     *
     * @return true if successful
     */
    boolean update();

    /**
     * Inserts the object into the DB
     *
     * @return the count of the rows affected, should only be 1 here, or -1 if failed.
     */
    long insert();

}
