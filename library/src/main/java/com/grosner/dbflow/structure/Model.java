package com.grosner.dbflow.structure;

import android.database.Cursor;

import com.grosner.dbflow.annotation.Ignore;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: The main interface for which all DB-based objects should implement.
 */
@Ignore
public interface Model {

    /**
     * Saves the object in the DB.
     *
     * @param async If we want this to happen on the {@link com.grosner.dbflow.runtime.DBBatchSaveQueue}
     */
    public void save(boolean async);

    /**
     * Deletes the object in the DB
     *
     * @param async If we want this to happen on the {@link com.grosner.dbflow.runtime.DBTransactionQueue}
     */
    public void delete(boolean async);

    /**
     * Updates an object in the DB
     *
     * @param async If we want this to happen on the {@link com.grosner.dbflow.runtime.DBTransactionQueue}
     */
    public void update(boolean async);

    /**
     * Inserts the object into the DB
     *
     * @param async
     */
    public void insert(boolean async);

    /**
     * Returns whether this Model exists or not
     *
     * @return
     */
    public boolean exists();

}
