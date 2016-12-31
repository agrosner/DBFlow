package com.raizlabs.android.dbflow.sql.language;

import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;

/**
 * Description: The SQLite UPDATE query. Will update rows in the DB.
 */
public class Update<TModel> implements Query {

    /**
     * The conflict action to resolve updates.
     */
    private ConflictAction conflictAction = ConflictAction.NONE;

    private final Class<TModel> table;

    /**
     * Constructs new instace of an UPDATE query with the specified table.
     *
     * @param table The table to use.
     */
    public Update(Class<TModel> table) {
        this.table = table;
    }

    public Update conflictAction(ConflictAction conflictAction) {
        this.conflictAction = conflictAction;
        return this;
    }

    /**
     * @return This instance.
     * @see ConflictAction#ROLLBACK
     */
    public Update orRollback() {
        return conflictAction(ConflictAction.ROLLBACK);
    }

    /**
     * @return This instance.
     * @see ConflictAction#ABORT
     */
    public Update orAbort() {
        return conflictAction(ConflictAction.ABORT);
    }

    /**
     * @return This instance.
     * @see ConflictAction#REPLACE
     */
    public Update orReplace() {
        return conflictAction(ConflictAction.REPLACE);
    }

    /**
     * @return This instance.
     * @see ConflictAction#FAIL
     */
    public Update orFail() {
        return conflictAction(ConflictAction.FAIL);
    }

    /**
     * @return This instance.
     * @see ConflictAction#IGNORE
     */
    public Update orIgnore() {
        return conflictAction(ConflictAction.IGNORE);
    }

    /**
     * Begins a SET piece of the SQL query
     *
     * @param conditions The array of conditions that define this SET statement
     * @return A SET query piece of this statement
     */
    public Set<TModel> set(SQLCondition... conditions) {
        return new Set<>(this, table).conditions(conditions);
    }

    @Override
    public String getQuery() {
        QueryBuilder queryBuilder = new QueryBuilder("UPDATE ");
        if (conflictAction != null && !conflictAction.equals(ConflictAction.NONE)) {
            queryBuilder.append("OR").appendSpaceSeparated(conflictAction.name());
        }
        queryBuilder.append(FlowManager.getTableName(table)).appendSpace();
        return queryBuilder.getQuery();
    }
}
