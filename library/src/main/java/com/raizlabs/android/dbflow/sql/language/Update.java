package com.raizlabs.android.dbflow.sql.language;

import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: The SQLite UPDATE query. Will update rows in the DB.
 */
public class Update implements Query {

    /**
     * The conflict action to resolve updates.
     */
    private ConflictAction mConflictAction = ConflictAction.NONE;

    public Update conflictAction(ConflictAction conflictAction) {
        mConflictAction = conflictAction;
        return this;
    }

    /**
     * @return This instance.
     * @see {@link com.raizlabs.android.dbflow.annotation.ConflictAction#ROLLBACK}
     */
    public Update orRollback() {
        return conflictAction(ConflictAction.ROLLBACK);
    }

    /**
     * @return This instance.
     * @see {@link com.raizlabs.android.dbflow.annotation.ConflictAction#ABORT}
     */
    public Update orAbort() {
        return conflictAction(ConflictAction.ABORT);
    }

    /**
     * @return This instance.
     * @see {@link com.raizlabs.android.dbflow.annotation.ConflictAction#REPLACE}
     */
    public Update orReplace() {
        return conflictAction(ConflictAction.REPLACE);
    }

    /**
     * @return This instance.
     * @see {@link com.raizlabs.android.dbflow.annotation.ConflictAction#FAIL}
     */
    public Update orFail() {
        return conflictAction(ConflictAction.FAIL);
    }

    /**
     * @return This instance.
     * @see {@link com.raizlabs.android.dbflow.annotation.ConflictAction#IGNORE}
     */
    public Update orIgnore() {
        return conflictAction(ConflictAction.IGNORE);
    }

    /**
     * Specifies the table to UPDATE
     *
     * @param table        The table to update
     * @param <ModelClass> The class that implements {@link com.raizlabs.android.dbflow.structure.Model}
     * @return The FROM part of this query, used in calling a {@link com.raizlabs.android.dbflow.sql.language.Set}
     */
    public <ModelClass extends Model> From<ModelClass> table(Class<ModelClass> table) {
        return new From<>(this, table);
    }

    @Override
    public String getQuery() {
        QueryBuilder queryBuilder = new QueryBuilder("UPDATE ");
        if (mConflictAction != null && !mConflictAction.equals(ConflictAction.NONE)) {
            queryBuilder.append("OR").appendSpaceSeparated(mConflictAction.name());
        }
        return queryBuilder.getQuery();
    }
}
