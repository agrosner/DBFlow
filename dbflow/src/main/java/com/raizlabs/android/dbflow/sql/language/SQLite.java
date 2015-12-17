package com.raizlabs.android.dbflow.sql.language;

import com.raizlabs.android.dbflow.sql.language.property.IProperty;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: The main entry point into SQLite queries.
 */
public class SQLite {

    /**
     * @param properties The properties/columns to SELECT.
     * @return A beginning of the SELECT statement.
     */
    public static Select select(IProperty... properties) {
        return new Select(properties);
    }

    /**
     * Starts a new SELECT COUNT(property1, property2, propertyn) (if properties specified) or
     * SELECT COUNT(*).
     *
     * @param properties Optional, if specified returns the count of non-null ROWs from a specific single/group of columns.
     * @return A new select statement SELECT COUNT(expression)
     */
    public static Select selectCountOf(IProperty... properties) {
        return new Select(Method.count(properties));
    }

    /**
     * @param table        The tablet to update.
     * @param <ModelClass> The class that implements {@link Model}.
     * @return A new UPDATE statement.
     */
    public static <ModelClass extends Model> Update<ModelClass> update(Class<ModelClass> table) {
        return new Update<>(table);
    }

    /**
     * @param table        The table to insert.
     * @param <ModelClass> The class that implements {@link Model}.
     * @return A new INSERT statement.
     */
    public static <ModelClass extends Model> Insert<ModelClass> insert(Class<ModelClass> table) {
        return new Insert<>(table);
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
     * @param table        The table to delete from.
     * @param <ModelClass> The class that implements {@link Model}.
     * @return A {@link From} with specified DELETE on table.
     */
    public static <ModelClass extends Model> From<ModelClass> delete(Class<ModelClass> table) {
        return delete().from(table);
    }
}
