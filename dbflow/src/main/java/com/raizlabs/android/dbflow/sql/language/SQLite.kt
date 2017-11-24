@file:JvmName("SQLite")


package com.raizlabs.android.dbflow.sql.language

import com.raizlabs.android.dbflow.sql.language.property.IProperty
import com.raizlabs.android.dbflow.sql.language.property.Property
import com.raizlabs.android.dbflow.structure.Model
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper
import kotlin.reflect.KClass


/**
 * @param properties The properties/columns to SELECT.
 * @return A beginning of the SELECT statement.
 */
fun DatabaseWrapper.select(vararg properties: IProperty<*>): Select = Select(this, *properties)

/**
 * Starts a new SELECT COUNT(property1, property2, propertyn) (if properties specified) or
 * SELECT COUNT(*).
 *
 * @param properties Optional, if specified returns the count of non-null ROWs from a specific single/group of columns.
 * @return A new select statement SELECT COUNT(expression)
 */
fun DatabaseWrapper.selectCountOf(vararg properties: IProperty<*>): Select = Select(this, count(*properties))

inline fun <reified T : Any> DatabaseWrapper.update() = update(T::class.java)

/**
 * @param table    The tablet to update.
 * @return A new UPDATE statement.
 */
infix fun <T : Any> DatabaseWrapper.update(table: Class<T>): Update<T> = Update(this, table)

/**
 * @param table    The table to update.
 * @return A new UPDATE statement.
 */
infix fun <T : Any> DatabaseWrapper.update(modelClass: KClass<T>) = update(modelClass.java)

inline fun <reified T : Any> DatabaseWrapper.insert() = insert(T::class.java)

/**
 * @param table    The table to insert.
 * @return A new INSERT statement.
 */
infix fun <T : Any> DatabaseWrapper.insert(table: Class<T>): Insert<T> = Insert(this, table)

/**
 * @param table    The table to insert.
 * @return A new INSERT statement.
 */
infix fun <T : Any> DatabaseWrapper.insert(modelClass: KClass<T>) = insert(modelClass.java)

/**
 * @return Begins a DELETE statement.
 */
fun DatabaseWrapper.delete(): Delete = Delete(this)

/**
 * Starts a DELETE statement on the specified table.
 *
 * @param table    The table to delete from.
 * @param [T] The class that implements [Model].
 * @return A [From] with specified DELETE on table.
 */
infix fun <T : Any> DatabaseWrapper.delete(table: Class<T>): From<T> = delete().from(table)

/**
 * Starts an INDEX statement on specified table.
 *
 * @param name     The name of the index.
 * @param [T] The class that implements [Model].
 * @return A new INDEX statement.
 */
fun <T> DatabaseWrapper.index(name: String): Index<T> = Index(this, name)

/**
 * Starts a TRIGGER statement.
 *
 * @param name The name of the trigger.
 * @return A new TRIGGER statement.
 */
fun createTrigger(name: String): Trigger = Trigger.create(name)

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
fun <TReturn> _case(caseColumn: Property<TReturn>): Case<TReturn> = Case(caseColumn)

/**
 * Starts an efficient CASE statement. The value passed here is only evaulated once. A non-efficient
 * case statement will evaluate all of its [SQLOperator].
 *
 * @param caseColumn The value
 */
@JvmName("_case")
fun <TReturn> case(caseColumn: IProperty<*>): Case<TReturn> = Case(caseColumn)