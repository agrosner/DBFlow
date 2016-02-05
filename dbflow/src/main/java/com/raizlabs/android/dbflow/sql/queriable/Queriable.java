package com.raizlabs.android.dbflow.sql.queriable;

import android.database.Cursor;

import com.raizlabs.android.dbflow.sql.language.Insert;
import com.raizlabs.android.dbflow.sql.language.Set;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

/**
 * Description: The most basic interface that some of the classes such as {@link Insert}, {@link ModelQueriable},
 * {@link Set}, and more implement for convenience.
 */
public interface Queriable {

    /**
     * @return A cursor from the DB based on this query
     */
    Cursor query();

    /**
     * Allows you to pass in a {@link DatabaseWrapper} manually.
     *
     * @param databaseWrapper The wrapper to pass in.
     * @return A cursor from the DB based on this query
     */
    Cursor query(DatabaseWrapper databaseWrapper);

    /**
     * Will not return a result, rather simply will execute a SQL statement. Use this for non-SELECT statements or when
     * you're not interested in the result.
     */
    void execute();

    /**
     * Will not return a result, rather simply will execute a SQL statement. Use this for non-SELECT statements or when
     * you're not interested in the result.
     */
    void execute(DatabaseWrapper databaseWrapper);

}
