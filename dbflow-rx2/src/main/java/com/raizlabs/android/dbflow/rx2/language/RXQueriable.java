package com.raizlabs.android.dbflow.rx2.language;

import android.database.Cursor;
import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.Insert;
import com.raizlabs.android.dbflow.sql.language.Set;
import com.raizlabs.android.dbflow.sql.queriable.Queriable;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.database.DatabaseStatement;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;

/**
 * Description: Mirrors {@link Queriable} with RX constructs.
 */
public interface RXQueriable {

    /**
     * @return An {@link Single} from the DB based on this query
     */
    @NonNull
    Maybe<Cursor> query();

    /**
     * Allows you to pass in a {@link DatabaseWrapper} manually.
     *
     * @param databaseWrapper The wrapper to pass in.
     * @return An {@link Single} from the DB based on this query
     */
    @NonNull
    Maybe<Cursor> query(DatabaseWrapper databaseWrapper);


    /**
     * @return An {@link Single} of {@link DatabaseStatement} from this query.
     */
    @NonNull
    Single<DatabaseStatement> compileStatement();

    /**
     * @param databaseWrapper The wrapper to use.
     * @return An {@link Single} of {@link DatabaseStatement} from this query with database specified.
     */
    @NonNull
    Single<DatabaseStatement> compileStatement(DatabaseWrapper databaseWrapper);

    /**
     * @return the count of the results of the query.
     * @deprecated use {@link #longValue()}
     */
    @NonNull
    Single<Long> count();

    /**
     * Allows you to pass in a {@link DatabaseWrapper} manually.
     *
     * @return the count of the results of the query.
     * @deprecated use {@link #longValue(DatabaseWrapper)}
     */
    @NonNull
    Single<Long> count(DatabaseWrapper databaseWrapper);

    /**
     * @return the long value of this query.
     **/
    @NonNull
    Single<Long> longValue();

    /**
     * @return the long value of this query.
     **/
    @NonNull
    Single<Long> longValue(DatabaseWrapper databaseWrapper);

    /**
     * @return This may return the number of rows affected from a {@link Insert}  statement.
     * If not, returns {@link Model#INVALID_ROW_ID}
     */
    @NonNull
    Single<Long> executeInsert();

    /**
     * @return This may return the number of rows affected from a {@link Insert}  statement.
     * If not, returns {@link Model#INVALID_ROW_ID}
     */
    @NonNull
    Single<Long> executeInsert(DatabaseWrapper databaseWrapper);

    /**
     * @return This may return the number of rows affected from a {@link Set} or {@link Delete} statement.
     * If not, returns {@link Model#INVALID_ROW_ID}
     */
    @NonNull
    Single<Long> executeUpdateDelete(DatabaseWrapper databaseWrapper);

    /**
     * @return This may return the number of rows affected from a {@link Set} or {@link Delete} statement.
     * If not, returns {@link Model#INVALID_ROW_ID}
     */
    @NonNull
    Single<Long> executeUpdateDelete();

    /**
     * @return True if this query has data. It will run a {@link #count()} greater than 0.
     */
    @NonNull
    Single<Boolean> hasData();

    /**
     * Allows you to pass in a {@link DatabaseWrapper} manually.
     *
     * @return True if this query has data. It will run a {@link #count()} greater than 0.
     */
    @NonNull
    Single<Boolean> hasData(DatabaseWrapper databaseWrapper);

    /**
     * Will not return a result, rather simply will execute a SQL statement. Use this for non-SELECT statements or when
     * you're not interested in the result.
     */
    @NonNull
    Completable execute();

    /**
     * Will not return a result, rather simply will execute a SQL statement. Use this for non-SELECT statements or when
     * you're not interested in the result.
     */
    @NonNull
    Completable execute(DatabaseWrapper databaseWrapper);
}
