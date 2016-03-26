package com.raizlabs.android.dbflow.sql.migration;

import android.support.annotation.CallSuper;

import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.language.ConditionGroup;
import com.raizlabs.android.dbflow.sql.language.SQLCondition;
import com.raizlabs.android.dbflow.sql.language.Update;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

/**
 * Description: Provides a simple way to update a table's field or fields quickly in a migration.
 * It ties an SQLite {@link com.raizlabs.android.dbflow.sql.language.Update}
 * to migrations whenever we want to batch update tables in a structured manner.
 */
public class UpdateTableMigration<TModel extends Model> extends BaseMigration implements Query {

    /**
     * The table to update
     */
    private final Class<TModel> table;

    /**
     * The query to use
     */
    private QueryBuilder query;

    /**
     * Builds the conditions for the WHERE part of our query
     */
    private ConditionGroup whereConditionGroup;

    /**
     * The conditions to use to set fields in the update query
     */
    private ConditionGroup setConditionGroup;

    /**
     * Creates an update migration.
     *
     * @param table The table to update
     */
    public UpdateTableMigration(Class<TModel> table) {
        this.table = table;
    }

    /**
     * This will append a condition to this migration. It will execute each of these in succession with the order
     * that this is called.
     *
     * @param conditions The conditions to append
     */
    public UpdateTableMigration<TModel> set(SQLCondition... conditions) {
        if (setConditionGroup == null) {
            setConditionGroup = ConditionGroup.nonGroupingClause();
        }

        setConditionGroup.andAll(conditions);
        return this;
    }

    public UpdateTableMigration<TModel> where(SQLCondition... conditions) {
        if (whereConditionGroup == null) {
            whereConditionGroup = ConditionGroup.nonGroupingClause();
        }

        whereConditionGroup.andAll(conditions);
        return this;
    }

    private String generateQuery() {
        query = new QueryBuilder(new Update<>(table)
                .set(setConditionGroup)
                .where(whereConditionGroup).getQuery());
        return query.getQuery();
    }

    @Override
    public final void migrate(DatabaseWrapper database) {
        database.execSQL(generateQuery());
    }

    @CallSuper
    @Override
    public void onPostMigrate() {
        // make fields eligible for GC
        query = null;
        setConditionGroup = null;
        whereConditionGroup = null;
    }

    @Override
    public String getQuery() {
        return generateQuery();
    }
}
