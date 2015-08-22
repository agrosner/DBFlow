package com.raizlabs.android.dbflow.sql.language;

import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: The SQLite UPDATE query. Will update rows in the DB.
 */
public class Update<ModelClass extends Model> implements Query {

    /**
     * The conflict action to resolve updates.
     */
    private ConflictAction mConflictAction = ConflictAction.NONE;

    private final Class<ModelClass> mTable;

    /**
     * @param table        The table to update.
     * @param <ModelClass> The class that implements {@link Model}
     * @return A new update object. Begins a generic UPDATE query.
     */
    public static <ModelClass extends Model> Update table(Class<ModelClass> table) {
        return new Update(table);
    }

    /**
     * Constructs new instace of an UPDATE query with the specified table.
     *
     * @param table The table to use.
     */
    public Update(Class<ModelClass> table) {
        mTable = table;
    }

    public Update conflictAction(ConflictAction conflictAction) {
        mConflictAction = conflictAction;
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
    public Set<ModelClass> set(Condition... conditions) {
        return new Set<>(this, mTable).conditions(conditions);
    }

    /**
     * Begins a SET piece of this query with a {@link com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder} as its conditions.
     *
     * @param conditionQueryBuilder The builder of a specific set of conditions used in this query
     * @return A SET query piece of this statement
     */
    public Set<ModelClass> set(ConditionQueryBuilder<ModelClass> conditionQueryBuilder) {
        return set().conditionQuery(conditionQueryBuilder);
    }

    /**
     * Begins a SET piece of this query with a string clause with args
     *
     * @param setClause The clause to use as a string clause.
     * @param args      The arguments to append that will get properly type-converted.
     * @return A SET query piece of this statement.
     */
    public Set<ModelClass> set(String setClause, Object... args) {
        return set().conditionClause(setClause, args);
    }

    @Override
    public String getQuery() {
        QueryBuilder queryBuilder = new QueryBuilder("UPDATE ");
        if (mConflictAction != null && !mConflictAction.equals(ConflictAction.NONE)) {
            queryBuilder.append("OR").appendSpaceSeparated(mConflictAction.name());
        }
        queryBuilder.appendQuoted(FlowManager.getTableName(mTable)).appendSpace();
        return queryBuilder.getQuery();
    }
}
