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

fun <T : Any> RetrievalAdapter<T>.insert() = Insert(this)

fun <T : Any> RetrievalAdapter<T>.insert(
    vararg columnValues: Pair<Property<*>, Any?>
) = Insert(this)
    .columnValues(*columnValues)

fun <T : Any> RetrievalAdapter<T>.insert(
    vararg operators: SQLOperator
) = Insert(this)
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
