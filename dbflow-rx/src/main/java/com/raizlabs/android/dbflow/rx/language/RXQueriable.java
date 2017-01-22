package com.raizlabs.android.dbflow.rx.language;

import android.database.Cursor;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.Set;
import com.raizlabs.android.dbflow.sql.queriable.Queriable;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.database.DatabaseStatement;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import rx.Single;

/**
 * Description: Mirrors {@link Queriable} with RX constructs.
 */
public interface RXQueriable {

    /**
     * @return An {@link Single} from the DB based on this query
     */
    @Nullable
    Single<Cursor> query();

    /**
     * Allows you to pass in a {@link DatabaseWrapper} manually.
     *
     * @param databaseWrapper The wrapper to pass in.
     * @return An {@link Single} from the DB based on this query
     */
    @Nullable
    Single<Cursor> query(DatabaseWrapper databaseWrapper);


    /**
     * @return An {@link Single} of {@link DatabaseStatement} from this query.
     */
    Single<DatabaseStatement> compileStatement();

    /**
     * @param databaseWrapper The wrapper to use.
     * @return An {@link Single} of {@link DatabaseStatement} from this query with database specified.
     */
    Single<DatabaseStatement> compileStatement(DatabaseWrapper databaseWrapper);

    /**
     * @return the count of the results of the query.
     */
    Single<Long> count();

    /**
     * Allows you to pass in a {@link DatabaseWrapper} manually.
     *
     * @return the count of the results of the query.
     */
    Single<Long> count(DatabaseWrapper databaseWrapper);

    /**
     * @return This may return the number of rows affected from a {@link Set} or {@link Delete} statement.
     * If not, returns {@link Model#INVALID_ROW_ID}
     */
    Single<Long> executeUpdateDelete(DatabaseWrapper databaseWrapper);

    /**
     * @return This may return the number of rows affected from a {@link Set} or {@link Delete} statement.
     * If not, returns {@link Model#INVALID_ROW_ID}
     */
    Single<Long> executeUpdateDelete();

    /**
     * @return True if this query has data. It will run a {@link #count()} greater than 0.
     */
    Single<Boolean> hasData();

    /**
     * Allows you to pass in a {@link DatabaseWrapper} manually.
     *
     * @return True if this query has data. It will run a {@link #count()} greater than 0.
     */
    Single<Boolean> hasData(DatabaseWrapper databaseWrapper);

    /**
     * Will not return a result, rather simply will execute a SQL statement. Use this for non-SELECT statements or when
     * you're not interested in the result.
     */
    Single<Void> execute();

    /**
     * Will not return a result, rather simply will execute a SQL statement. Use this for non-SELECT statements or when
     * you're not interested in the result.
     */
    Single<Void> execute(DatabaseWrapper databaseWrapper);
}
