package com.raizlabs.android.dbflow.structure;

/**
 * Description: The main interface for which all DB-based objects should implement.
 */
public interface Model {

    /**
     * Saves the object in the DB.
     */
    void save();

    /**
     * Deletes the object in the DB
     */
    void delete();

    /**
     * Updates an object in the DB
     */
    void update();

    /**
     * Inserts the object into the DB
     */
    void insert();

    /**
     * Returns whether this Model exists or not
     *
     * @return
     */
    boolean exists();

}
