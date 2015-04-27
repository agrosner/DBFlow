package com.raizlabs.android.dbflow.structure;

/**
 * Description: The main interface for which all DB-based objects should implement.
 */
public interface Model {

    /**
     * Saves the object in the DB.
     *
     * @param async If we want this to happen on the {@link com.raizlabs.android.dbflow.runtime.DBBatchSaveQueue}
     */
    void save(boolean async);

    /**
     * Deletes the object in the DB
     *
     * @param async If we want this to happen on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}
     */
    void delete(boolean async);

    /**
     * Updates an object in the DB
     *
     * @param async If we want this to happen on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}
     */
    void update(boolean async);

    /**
     * Inserts the object into the DB
     *
     * @param async
     */
    void insert(boolean async);

    /**
     * Returns whether this Model exists or not
     *
     * @return
     */
    boolean exists();

}
