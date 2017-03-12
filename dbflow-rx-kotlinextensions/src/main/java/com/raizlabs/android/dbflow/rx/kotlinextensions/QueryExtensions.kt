package com.raizlabs.android.dbflow.rx.kotlinextensions

import android.database.Cursor
import com.raizlabs.android.dbflow.rx.language.*
import com.raizlabs.android.dbflow.sql.language.BaseModelQueriable
import com.raizlabs.android.dbflow.sql.language.CursorResult
import com.raizlabs.android.dbflow.sql.language.Join
import com.raizlabs.android.dbflow.sql.language.SQLCondition
import com.raizlabs.android.dbflow.sql.language.property.IProperty
import com.raizlabs.android.dbflow.structure.database.DatabaseStatement
import kotlin.reflect.KClass

val select: RXSelect
    get() = RXSQLite.select()

inline fun <reified T : Any> RXSelect.from() = from(T::class.java)

fun <T : Any> delete(modelClass: KClass<T>) = RXSQLite.delete(modelClass.java)

infix fun <T : Any> RXSelect.from(modelClass: KClass<T>) = from(modelClass.java)

infix fun <T : Any> RXFrom<T>.whereExists(where: RXWhere<T>) = where().exists(where)

infix fun <T : Any> RXFrom<T>.where(sqlCondition: SQLCondition) = where(sqlCondition)

infix fun <T : Any> RXSet<T>.where(sqlCondition: SQLCondition) = where(sqlCondition)

infix fun <T : Any> RXWhere<T>.and(sqlCondition: SQLCondition) = and(sqlCondition)

infix fun <T : Any> RXWhere<T>.or(sqlCondition: SQLCondition) = and(sqlCondition)

// queriable extensions

inline val RXQueriable.count
    get() = count()

inline val RXQueriable.cursor
    get() = query()

inline val RXQueriable.hasData
    get() = hasData()

inline val RXQueriable.statement
    get() = compileStatement()

inline val <T : Any> RXModelQueriable<T>.list
    get() = queryList()

inline val <T : Any> RXModelQueriable<T>.result
    get() = querySingle()

inline val <T : Any> RXModelQueriable<T>.cursorResult
    get() = queryResults()

inline val <T : Any> RXModelQueriable<T>.streamResults
    get() = queryStreamResults()

// async extensions

infix inline fun <T : Any> RXModelQueriable<T>.list(crossinline func: (MutableList<T>) -> Unit) = list.subscribe({ func(it) })

infix inline fun <T : Any> RXModelQueriable<T>.result(crossinline func: (T?) -> Unit) = result.subscribe({ func(it) })

infix inline fun <T : Any> RXModelQueriable<T>.streamResults(crossinline func: (T?) -> Unit) = streamResults.subscribe({ func(it) })

infix inline fun <T : Any> RXModelQueriable<T>.cursorResult(crossinline func: (CursorResult<T>) -> Unit) = cursorResult.subscribe({ func(it) })

infix inline fun RXQueriable.statement(crossinline func: (DatabaseStatement) -> Unit) = statement.subscribe({ func(it) })

infix inline fun RXQueriable.hasData(crossinline func: (Boolean) -> Unit) = hasData.subscribe({ func(it) })

infix inline fun RXQueriable.cursor(crossinline func: (Cursor) -> Unit) = cursor.subscribe({ func(it) })

infix inline fun RXQueriable.count(crossinline func: (Long) -> Unit) = count.subscribe({ func(it) })

// join

infix fun <T : Any, V : Any> RXFrom<V>.innerJoin(joinTable: KClass<T>) = join(joinTable.java, Join.JoinType.INNER)

infix fun <T : Any, V : Any> RXFrom<V>.crossJoin(joinTable: KClass<T>) = join(joinTable.java, Join.JoinType.CROSS)

infix fun <T : Any, V : Any> RXFrom<V>.leftOuterJoin(joinTable: KClass<T>) = join(joinTable.java, Join.JoinType.LEFT_OUTER)

infix fun <T : Any, V : Any> Join<T, V>.on(sqlCondition: SQLCondition) = on(sqlCondition)

// update methods

fun <T : Any> update(modelClass: KClass<T>) = RXSQLite.update(modelClass.java)

infix fun <T : Any> RXUpdate<T>.set(sqlCondition: SQLCondition) = set(sqlCondition)


// delete

inline fun <reified T : Any> delete() = RXSQLite.delete(T::class.java)

inline fun <reified T : Any> delete(deleteClause: RXFrom<T>.() -> BaseModelQueriable<T>) = deleteClause(RXSQLite.delete(T::class.java))

// insert methods

fun <T : Any> insert(modelClass: KClass<T>) = RXSQLite.insert(modelClass.java)

infix fun <T : Any> RXInsert<T>.orReplace(into: Array<out Pair<IProperty<*>, *>>) = orReplace().into(*into)

infix fun <T : Any> RXInsert<T>.orRollback(into: Array<out Pair<IProperty<*>, *>>) = orRollback().into(*into)

infix fun <T : Any> RXInsert<T>.orAbort(into: Array<out Pair<IProperty<*>, *>>) = orAbort().into(*into)

infix fun <T : Any> RXInsert<T>.orFail(into: Array<out Pair<IProperty<*>, *>>) = orFail().into(*into)

infix fun <T : Any> RXInsert<T>.orIgnore(into: Array<out Pair<IProperty<*>, *>>) = orIgnore().into(*into)

infix fun <T : Any> RXInsert<T>.select(from: RXFrom<*>) = select(from)

fun into(vararg pairs: Pair<IProperty<*>, *>): Array<out Pair<IProperty<*>, *>> = pairs

fun <T> RXInsert<T>.into(vararg pairs: Pair<IProperty<*>, *>): RXInsert<T> {
    val columns: MutableList<IProperty<*>> = java.util.ArrayList()
    val values = java.util.ArrayList<Any?>()
    pairs.forEach {
        columns.add(it.first)
        values.add(it.second)
    }
    this.columns(columns).values(values.toArray())
    return this
}