package com.raizlabs.android.dbflow.structure;

/**
 * Description: The main interface for which all DB-based objects should implement.
 */
public interface Model {

    /**
     * Saves the object in the DB.
     */
    public void save();

    /**
     * Deletes the object in the DB
     */
    public void delete();

    /**
     * Updates an object in the DB
     */
    public void update();

    /**
     * Inserts the object into the DB
     */
    public void insert();

    /**
     * Returns whether this Model exists or not
     *
     * @return
     */
    public boolean exists();

}
