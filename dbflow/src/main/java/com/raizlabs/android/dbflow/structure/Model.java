package com.raizlabs.android.dbflow.structure;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;
import com.raizlabs.android.dbflow.structure.database.transaction.DefaultTransactionQueue;

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
     * Saves the object in the DB.
     *
     * @return true if successful
     */
    boolean save(@NonNull DatabaseWrapper wrapper);

    /**
     * Deletes the object in the DB
     *
     * @return true if successful
     */
    boolean delete();

    /**
     * Deletes the object in the DB
     *
     * @return true if successful
     */
    boolean delete(@NonNull DatabaseWrapper wrapper);

    /**
     * Updates an object in the DB. Does not insert on failure.
     *
     * @return true if successful
     */
    boolean update();

    /**
     * Updates an object in the DB. Does not insert on failure.
     *
     * @return true if successful
     */
    boolean update(@NonNull DatabaseWrapper wrapper);

    /**
     * Inserts the object into the DB
     *
     * @return the count of the rows affected, should only be 1 here, or -1 if failed.
     */
    long insert();

    /**
     * Inserts the object into the DB
     *
     * @return the count of the rows affected, should only be 1 here, or -1 if failed.
     */
    long insert(DatabaseWrapper wrapper);

    /**
     * @return An async instance of this model where all transactions are on the {@link DefaultTransactionQueue}
     */
    @NonNull
    AsyncModel<? extends Model> async();

}
