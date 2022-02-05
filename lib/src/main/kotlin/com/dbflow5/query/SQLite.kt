@file:JvmName("SQLite")


package com.dbflow5.query

import com.dbflow5.adapter.RetrievalAdapter
import com.dbflow5.adapter.SQLObjectAdapter
import com.dbflow5.query.property.IProperty
import com.dbflow5.query.property.Property
import com.dbflow5.structure.Model


/**
 * @param properties The properties/columns to SELECT.
 * @return A beginning of the SELECT statement.
 */
fun select(vararg properties: IProperty<*>): Select = Select(*properties)

/**
 * Starts a new SELECT COUNT(property1, property2, propertyn) (if properties specified) or
 * SELECT COUNT(*).
 *
 * @param properties Optional, if specified returns the count of non-null ROWs from a specific single/group of columns.
 * @return A new select statement SELECT COUNT(expression)
 */
fun selectCountOf(vararg properties: IProperty<*>): Select = Select(count(*properties))

/**
 * @param table    The tablet to update.
 * @return A new UPDATE statement.
 */
fun <T : Any> SQLObjectAdapter<T>.update(): Update<T> = Update(this)

fun <T : Any> RetrievalAdapter<T>.insertInto() =
    insert(columns = arrayOf())

fun <T : Any> RetrievalAdapter<T>.insert() = Insert(this)

fun <T : Any> RetrievalAdapter<T>.insert(
    vararg columnValues: Pair<Property<*>, Any?>
) = Insert(this)
    .columnValues(*columnValues)

fun <T : Any> insert(
    adapter: RetrievalAdapter<T>,
    vararg operators: SQLOperator
) = Insert(adapter)
    .columnValues(*operators)

/**
 * @param table    The table to insert.
 * @return A new INSERT statement.
 */
fun <T : Any> RetrievalAdapter<T>.insert(vararg columns: Property<*>): Insert<T> =
    Insert(this, *columns)

/**
 * @return Begins a DELETE statement.
 */
fun delete(): Delete = Delete()

/**
 * Starts a DELETE statement on the specified table.
 *
 * @param table    The table to delete from.
 * @param [T] The class that implements [Model].
 * @return A [From] with specified DELETE on table.
 */
fun <T : Any> SQLObjectAdapter<T>.delete(): From<T> = Delete().from(this)

/**
 * Starts an INDEX statement on specified table.
 *
 * @param name     The name of the index.
 * @param [T] The class that implements [Model].
 * @return A new INDEX statement.
 */
fun <T : Any> index(name: String, adapter: SQLObjectAdapter<T>): Index<T> = Index(name, adapter)


/**
 * Starts a TRIGGER statement.
 *
 * @param name The name of the trigger.
 * @return A new TRIGGER statement.
 */
fun createTrigger(name: String): Trigger = Trigger.create(name)

/**
 * Starts a temporary TRIGGER statement.
 *
 * @param name The name of the trigger.
 * @return A new TEMPORARY TRIGGER statement.
 */
fun createTempTrigger(name: String): Trigger = Trigger.create(name).temporary()

/**
 * Starts a CASE statement.
 *
 * @param operator The condition to check for in the WHEN.
 * @return A new [CaseCondition].
 */
fun <T> caseWhen(operator: SQLOperator): CaseCondition<T> = Case<T>().whenever(operator)

/**
 * Starts an efficient CASE statement. The value passed here is only evaulated once. A non-efficient
 * case statement will evaluate all of its [SQLOperator].
 *
 * @param caseColumn The value
 */
@JvmName("_case")
fun <TReturn> case(caseColumn: Property<TReturn>): Case<TReturn> = Case(caseColumn)

/**
 * Starts an efficient CASE statement. The value passed here is only evaulated once. A non-efficient
 * case statement will evaluate all of its [SQLOperator].
 *
 * @param caseColumn The value
 */
@JvmName("_case")
fun <TReturn> case(caseColumn: IProperty<*>): Case<TReturn> = Case(caseColumn)