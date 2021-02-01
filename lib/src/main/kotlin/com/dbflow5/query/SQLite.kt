@file:JvmName("SQLite")


package com.dbflow5.query

import com.dbflow5.query.property.IProperty
import com.dbflow5.query.property.Property
import com.dbflow5.structure.Model
import kotlin.reflect.KClass


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

inline fun <reified T : Any> update() = update(T::class.java)

/**
 * @param table    The tablet to update.
 * @return A new UPDATE statement.
 */
fun <T : Any> update(table: Class<T>): Update<T> = Update(table)

/**
 * @param table    The table to update.
 * @return A new UPDATE statement.
 */
fun <T : Any> update(table: KClass<T>) = update(table.java)

inline fun <reified T: Any> insertInto() = insert<T>(columns = arrayOf())

inline fun <reified T : Any> insert(vararg columns: Property<*>) = insert(T::class, *columns)

inline fun <reified T: Any> insert(vararg columnValues: Pair<Property<*>, Any?>) = insert(T::class)
        .columnValues(*columnValues)

inline fun <reified T: Any> insert(vararg operators: SQLOperator) = insert(T::class)
        .columnValues(*operators)

/**
 * @param table    The table to insert.
 * @return A new INSERT statement.
 */
fun <T : Any> insert(table: Class<T>, vararg columns: Property<*>): Insert<T> = Insert(table, *columns)

/**
 * @param table    The table to insert.
 * @return A new INSERT statement.
 */
fun <T : Any> insert(table: KClass<T>, vararg columns: Property<*>) = insert(table.java, *columns)

/**
 * @return Begins a DELETE statement.
 */
fun delete(): Delete = Delete()

inline fun <reified T : Any> delete() = delete(T::class.java)

/**
 * Starts a DELETE statement on the specified table.
 *
 * @param table    The table to delete from.
 * @param [T] The class that implements [Model].
 * @return A [From] with specified DELETE on table.
 */
fun <T : Any> delete(table: Class<T>): From<T> = delete().from(table)

/**
 * Starts a DELETE statement on the specified table.
 *
 * @param table    The table to delete from.
 * @param [T] The class that implements [Model].
 * @return A [From] with specified DELETE on table.
 */
fun <T : Any> delete(table: KClass<T>): From<T> = delete().from(table)

/**
 * Starts an INDEX statement on specified table.
 *
 * @param name     The name of the index.
 * @param [T] The class that implements [Model].
 * @return A new INDEX statement.
 */
fun <T> index(name: String, table: Class<T>): Index<T> = Index(name, table)


/**
 * Starts an INDEX statement on specified table.
 *
 * @param name     The name of the index.
 * @param [T] The class that implements [Model].
 * @return A new INDEX statement.
 */
fun <T : Any> index(name: String, table: KClass<T>): Index<T> = Index(name, table.java)

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