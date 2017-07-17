package com.raizlabs.android.dbflow.sql.queriable;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.Insert;
import com.raizlabs.android.dbflow.sql.language.Set;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.database.DatabaseStatement;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;
import com.raizlabs.android.dbflow.structure.database.FlowCursor;

/**
 * Description: The most basic interface that some of the classes such as {@link Insert}, {@link ModelQueriable},
 * {@link Set}, and more implement for convenience.
 */
public interface Queriable extends Query {

    /**
     * @return A cursor from the DB based on this query
     */
    @Nullable
    FlowCursor query();

    /**
     * Allows you to pass in a {@link DatabaseWrapper} manually.
     *
     * @param databaseWrapper The wrapper to pass in.
     * @return A cursor from the DB based on this query
     */
    @Nullable
    FlowCursor query(@NonNull DatabaseWrapper databaseWrapper);


    /**
     * @return A new {@link DatabaseStatement} from this query.
     */
    @NonNull
    DatabaseStatement compileStatement();

    /**
     * @param databaseWrapper The wrapper to use.
     * @return A new {@link DatabaseStatement} from this query with database specified.
     */
    @NonNull
    DatabaseStatement compileStatement(@NonNull DatabaseWrapper databaseWrapper);

    /**
     * @return the count of the results of the query.
     */
    long count();

    /**
     * Allows you to pass in a {@link DatabaseWrapper} manually.
     *
     * @return the count of the results of the query.
     */
    long count(@NonNull DatabaseWrapper databaseWrapper);

    /**
     * @return This may return the number of rows affected from a {@link Set} or {@link Delete} statement.
     * If not, returns {@link Model#INVALID_ROW_ID}
     */
    long executeUpdateDelete(@NonNull DatabaseWrapper databaseWrapper);

    /**
     * @return This may return the number of rows affected from a {@link Set} or {@link Delete} statement.
     * If not, returns {@link Model#INVALID_ROW_ID}
     */
    long executeUpdateDelete();

    /**
     * @return This may return the number of rows affected from a {@link Insert}  statement.
     * If not, returns {@link Model#INVALID_ROW_ID}
     */
    long executeInsert();

    /**
     * @return This may return the number of rows affected from a {@link Insert}  statement.
     * If not, returns {@link Model#INVALID_ROW_ID}
     */
    long executeInsert(@NonNull DatabaseWrapper databaseWrapper);

    /**
     * @return True if this query has data. It will run a {@link #count()} greater than 0.
     */
    boolean hasData();

    /**
     * Allows you to pass in a {@link DatabaseWrapper} manually.
     *
     * @return True if this query has data. It will run a {@link #count()} greater than 0.
     */
    boolean hasData(@NonNull DatabaseWrapper databaseWrapper);

    /**
     * Will not return a result, rather simply will execute a SQL statement. Use this for non-SELECT statements or when
     * you're not interested in the result.
     */
    void execute();

    /**
     * Will not return a result, rather simply will execute a SQL statement. Use this for non-SELECT statements or when
     * you're not interested in the result.
     */
    void execute(@NonNull DatabaseWrapper databaseWrapper);

    @NonNull
    BaseModel.Action getPrimaryAction();
}
