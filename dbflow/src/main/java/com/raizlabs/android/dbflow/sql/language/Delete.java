package com.raizlabs.android.dbflow.sql.language;

import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: Constructs the beginning of a SQL DELETE query
 */
public class Delete implements Query {

    /**
     * Deletes the specified table
     *
     * @param table        The table to delete
     * @param conditions   The list of conditions to use to delete from the specified table
     * @param <TModel> The class that implements {@link com.raizlabs.android.dbflow.structure.Model}
     */
    public static <TModel extends Model> void table(Class<TModel> table, SQLCondition... conditions) {
        new Delete().from(table).where(conditions).query();
    }

    /**
     * Deletes the list of tables specified.
     * WARNING: this will completely clear all rows from each table.
     *
     * @param tables The list of tables to wipe.
     */
    @SafeVarargs
    public static void tables(Class<? extends Model>... tables) {
        for (Class modelClass : tables) {
            table(modelClass);
        }
    }

    /**
     * Returns the new SQL FROM statement wrapper
     *
     * @param table        The table we want to run this query from
     * @param <TModel> The table class
     * @return
     */
    public <TModel extends Model> From<TModel> from(Class<TModel> table) {
        return new From<>(this, table);
    }

    @Override
    public String getQuery() {
        return new QueryBuilder()
                .append("DELETE")
                .appendSpace().getQuery();
    }
}
