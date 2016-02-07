package com.raizlabs.android.dbflow.sql.queriable;

import android.database.Cursor;

import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.Insert;
import com.raizlabs.android.dbflow.sql.language.Set;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

/**
 * Description: The most basic interface that some of the classes such as {@link Insert}, {@link ModelQueriable},
 * {@link Set}, and more implement for convenience.
 */
public interface Queriable {

    /**
     * @param selectionArgs You may include ?s in selection, which will be replaced by the values
     *                      from selectionArgs, in order that they appear in the selection. The
     *                      values will be bound as Strings.
     * @return A cursor from the DB based on this query
     */
    Cursor query(String... selectionArgs);

    /**
     * Allows you to pass in a {@link DatabaseWrapper} manually.
     *
     * @param databaseWrapper The wrapper to pass in.
     * @param selectionArgs   You may include ?s in selection, which will be replaced by the values
     *                        from selectionArgs, in order that they appear in the selection. The
     *                        values will be bound as Strings.
     * @return A cursor from the DB based on this query
     */
    Cursor query(DatabaseWrapper databaseWrapper, String... selectionArgs);

    /**
     * @param selectionArgs You may include ?s in selection, which will be replaced by the values
     *                      from selectionArgs, in order that they appear in the selection. The
     *                      values will be bound as Strings.
     * @return the count of the results of the query. This may return the
     * number of rows affected from a {@link Set} or {@link Delete} statement.
     */
    long count(String... selectionArgs);

    /**
     * Allows you to pass in a {@link DatabaseWrapper} manually.
     *
     * @param selectionArgs You may include ?s in selection, which will be replaced by the values
     *                      from selectionArgs, in order that they appear in the selection. The
     *                      values will be bound as Strings.
     * @return the count of the results of the query. This may return the
     * number of rows affected from a {@link Set} or {@link Delete} statement.
     */
    long count(DatabaseWrapper databaseWrapper, String... selectionArgs);

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
