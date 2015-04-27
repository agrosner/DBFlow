package com.raizlabs.android.dbflow.sql;

import android.database.Cursor;

import com.raizlabs.android.dbflow.sql.language.Insert;
import com.raizlabs.android.dbflow.sql.language.Set;

/**
 * Description: The most basic interface that some of the classes such as {@link Insert}, {@link ModelQueriable},
 * {@link Set}, and more implement for convenience.
 * {@link }
 */
public interface Queriable {

    /**
     * @return a cursor from the DB based on this query
     */
    Cursor query();

    /**
     * Will not return a result, rather simply will execute a SQL statement. Use this for non-SELECT statements or when
     * you're not interested in the result.
     */
    void queryClose();

}
