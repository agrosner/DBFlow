package com.raizlabs.android.dbflow.sql.language;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.language.property.IProperty;
import com.raizlabs.android.dbflow.sql.language.property.Property;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: The main entry point into SQLite queries.
 */
public class SQLite {

    /**
     * @param properties The properties/columns to SELECT.
     * @return A beginning of the SELECT statement.
     */
    @NonNull
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
    @NonNull
    public static Select selectCountOf(IProperty... properties) {
        return new Select(Method.count(properties));
    }

    /**
     * @param table    The tablet to update.
     * @param <TModel> The class that implements {@link Model}.
     * @return A new UPDATE statement.
     */
    @NonNull
    public static <TModel> Update<TModel> update(@NonNull Class<TModel> table) {
        return new Update<>(table);
    }

    /**
     * @param table    The table to insert.
     * @param <TModel> The class that implements {@link Model}.
     * @return A new INSERT statement.
     */
    @NonNull
    public static <TModel> Insert<TModel> insert(@NonNull Class<TModel> table) {
        return new Insert<>(table);
    }

    /**
     * @return Begins a DELETE statement.
     */
    @NonNull
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
    @NonNull
    public static <TModel> From<TModel> delete(@NonNull Class<TModel> table) {
        return delete().from(table);
    }

    /**
     * Starts an INDEX statement on specified table.
     *
     * @param name     The name of the index.
     * @param <TModel> The class that implements {@link Model}.
     * @return A new INDEX statement.
     */
    @NonNull
    public static <TModel> Index<TModel> index(@NonNull String name) {
        return new Index<>(name);
    }

    /**
     * Starts a TRIGGER statement.
     *
     * @param name The name of the trigger.
     * @return A new TRIGGER statement.
     */
    @NonNull
    public static Trigger createTrigger(@NonNull String name) {
        return Trigger.create(name);
    }

    /**
     * Starts a CASE statement.
     *
     * @param operator The condition to check for in the WHEN.
     * @return A new {@link CaseCondition}.
     */
    @NonNull
    public static <TReturn> CaseCondition<TReturn> caseWhen(@NonNull SQLOperator operator) {
        return new Case<TReturn>().when(operator);
    }

    /**
     * Starts an efficient CASE statement. The value passed here is only evaulated once. A non-efficient
     * case statement will evaluate all of its {@link SQLOperator}.
     *
     * @param caseColumn The value
     */
    @NonNull
    public static <TReturn> Case<TReturn> _case(@NonNull Property<TReturn> caseColumn) {
        return new Case<>(caseColumn);
    }

    /**
     * Starts an efficient CASE statement. The value passed here is only evaulated once. A non-efficient
     * case statement will evaluate all of its {@link SQLOperator}.
     *
     * @param caseColumn The value
     */
    @NonNull
    public static <TReturn> Case<TReturn> _case(@NonNull IProperty caseColumn) {
        return new Case<>(caseColumn);
    }
}
