package com.grosner.dbflow.structure;

import android.database.Cursor;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: The main interface for which all DB-based objects should implement.
 */
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
     * Loads the {@link com.grosner.dbflow.structure.Model} from the specified cursor.
     *
     * @param cursor
     */
    public void load(Cursor cursor);

    /**
     * Returns whether this Model exists or not
     *
     * @return
     */
    public boolean exists();

}
