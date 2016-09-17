package com.raizlabs.android.dbflow.sql.migration;

import android.support.annotation.CallSuper;

import com.raizlabs.android.dbflow.sql.language.BaseQueriable;
import com.raizlabs.android.dbflow.sql.language.ConditionGroup;
import com.raizlabs.android.dbflow.sql.language.SQLCondition;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

/**
 * Description: Provides a simple way to update a table's field or fields quickly in a migration.
 * It ties an SQLite {@link com.raizlabs.android.dbflow.sql.language.Update}
 * to migrations whenever we want to batch update tables in a structured manner.
 */
public class UpdateTableMigration<TModel> extends BaseMigration {

    /**
     * The table to update
     */
    private final Class<TModel> table;

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

    @Override
    public final void migrate(DatabaseWrapper database) {
        getUpdateStatement().execute(database);
    }

    @CallSuper
    @Override
    public void onPostMigrate() {
        // make fields eligible for GC
        setConditionGroup = null;
        whereConditionGroup = null;
    }

    public BaseQueriable<TModel> getUpdateStatement() {
        return SQLite.update(table)
                .set(setConditionGroup)
                .where(whereConditionGroup);
    }

}
