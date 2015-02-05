package com.raizlabs.android.dbflow.sql;

import android.database.Cursor;

/**
 * Description:
 */
public interface Queriable {

    /**
     * @return a cursor from the DB based on this query
     */
    public Cursor query();

    /**
     * Will not return a result, rather simply will execute a SQL statement. Use this for non-SELECT statements or when
     * you're not interested in the result.
     */
    public void queryClose();

}
