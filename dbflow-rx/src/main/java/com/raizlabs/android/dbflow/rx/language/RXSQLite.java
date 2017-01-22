package com.raizlabs.android.dbflow.rx.language;

import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.From;
import com.raizlabs.android.dbflow.sql.language.Method;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.language.property.IProperty;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: The RX implementation of SQLite language queries. Meant to be interchangeable
 * with {@link SQLite}.
 */
public class RXSQLite {

    /**
     * @param properties The properties/columns to SELECT.
     * @return A beginning of the SELECT statement.
     */
    public static RXSelect select(IProperty... properties) {
        return new RXSelect(properties);
    }

    /**
     * Starts a new SELECT COUNT(property1, property2, propertyn) (if properties specified) or
     * SELECT COUNT(*).
     *
     * @param properties Optional, if specified returns the count of non-null ROWs from a specific single/group of columns.
     * @return A new select statement SELECT COUNT(expression)
     */
    public static RXSelect selectCountOf(IProperty... properties) {
        return new RXSelect(Method.count(properties));
    }

    /**
     * @param table    The tablet to update.
     * @param <TModel> The class that implements {@link Model}.
     * @return A new UPDATE statement.
     */
    public static <TModel> RXUpdate<TModel> update(Class<TModel> table) {
        return new RXUpdate<>(table);
    }

    /**
     * @param table    The table to insert.
     * @param <TModel> The class that implements {@link Model}.
     * @return A new INSERT statement.
     */
    public static <TModel> RXInsert<TModel> insert(Class<TModel> table) {
        return new RXInsert<>(table);
    }

    /**
     * @return Begins a DELETE statement.
     */
    public static Delete delete() {
        return new Delete();
    }

    /**
     * Starts a DELETE statement on the specified table.
     *
     * @param table    The table to delete from.
     * @param <TModel> The class that implements {@link Model}.
     * @return A {@link From} with specified DELETE on table.
     */
    public static <TModel> From<TModel> delete(Class<TModel> table) {
        return delete().from(table);
    }

}
