package com.raizlabs.android.dbflow.sql.language

import com.raizlabs.android.dbflow.sql.language.property.IProperty
import com.raizlabs.android.dbflow.sql.language.property.Property
import com.raizlabs.android.dbflow.structure.Model

/**
 * Description: The main entry point into SQLite queries.
 */
object SQLite {

    /**
     * @param properties The properties/columns to SELECT.
     * @return A beginning of the SELECT statement.
     */
    @JvmStatic
    fun select(vararg properties: IProperty<*>): Select = Select(*properties)

    /**
     * Starts a new SELECT COUNT(property1, property2, propertyn) (if properties specified) or
     * SELECT COUNT(*).
     *
     * @param properties Optional, if specified returns the count of non-null ROWs from a specific single/group of columns.
     * @return A new select statement SELECT COUNT(expression)
     */
    @JvmStatic
    fun selectCountOf(vararg properties: IProperty<*>): Select = Select(count(*properties))

    /**
     * @param table    The tablet to update.
     * @param [T] The class that implements [Model].
     * @return A new UPDATE statement.
     */
    @JvmStatic
    fun <T : Any> update(table: Class<T>): Update<T> = Update(table)

    /**
     * @param table    The table to insert.
     * @param [T] The class that implements [Model].
     * @return A new INSERT statement.
     */
    @JvmStatic
    fun <T : Any> insert(table: Class<T>): Insert<T> = Insert(table)

    /**
     * @return Begins a DELETE statement.
     */
    @JvmStatic
    fun delete(): Delete = Delete()

    /**
     * Starts a DELETE statement on the specified table.
     *
     * @param table    The table to delete from.
     * @param [T] The class that implements [Model].
     * @return A [From] with specified DELETE on table.
     */
    @JvmStatic
    fun <T : Any> delete(table: Class<T>): From<T> = delete().from(table)

    /**
     * Starts an INDEX statement on specified table.
     *
     * @param name     The name of the index.
     * @param [T] The class that implements [Model].
     * @return A new INDEX statement.
     */
    @JvmStatic
    fun <T> index(name: String): Index<T> = Index(name)

    /**
     * Starts a TRIGGER statement.
     *
     * @param name The name of the trigger.
     * @return A new TRIGGER statement.
     */
    @JvmStatic
    fun createTrigger(name: String): Trigger = Trigger.create(name)

    /**
     * Starts a CASE statement.
     *
     * @param operator The condition to check for in the WHEN.
     * @return A new [CaseCondition].
     */
    @JvmStatic
    fun <TReturn> caseWhen(operator: SQLOperator): CaseCondition<TReturn> =
            Case<TReturn>().`when`(operator)

    /**
     * Starts an efficient CASE statement. The value passed here is only evaulated once. A non-efficient
     * case statement will evaluate all of its [SQLOperator].
     *
     * @param caseColumn The value
     */
    @JvmStatic
    fun <TReturn> _case(caseColumn: Property<TReturn>): Case<TReturn> = Case(caseColumn)

    /**
     * Starts an efficient CASE statement. The value passed here is only evaulated once. A non-efficient
     * case statement will evaluate all of its [SQLOperator].
     *
     * @param caseColumn The value
     */
    @JvmStatic
    fun <TReturn> _case(caseColumn: IProperty<*>): Case<TReturn> = Case(caseColumn)
}
