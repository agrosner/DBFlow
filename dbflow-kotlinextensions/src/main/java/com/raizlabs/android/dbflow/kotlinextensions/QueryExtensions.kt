package com.raizlabs.android.dbflow.kotlinextensions

import android.database.Cursor
import com.raizlabs.android.dbflow.sql.language.*
import com.raizlabs.android.dbflow.sql.language.Set
import com.raizlabs.android.dbflow.sql.language.property.IProperty
import com.raizlabs.android.dbflow.sql.queriable.AsyncQuery
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable
import com.raizlabs.android.dbflow.sql.queriable.Queriable
import com.raizlabs.android.dbflow.structure.AsyncModel
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.structure.database.DatabaseStatement
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction
import kotlin.reflect.KClass

/**
 * Description: A file containing extensions for adding query syntactic sugar.
 */

val select: Select
    get() = SQLite.select()

inline fun <reified T : Any> Select.from(): From<T> = from(T::class.java)

fun <T : Any> delete(modelClass: KClass<T>): From<T> = SQLite.delete(modelClass.java)

infix fun <T : Any> Select.from(modelClass: KClass<T>): From<T> = from(modelClass.java)

infix fun <T : Any> From<T>.whereExists(where: Where<T>): Where<T> = where().exists(where)

infix fun <T : Any> From<T>.where(sqlCondition: SQLCondition): Where<T> = where(sqlCondition)

infix fun <T : Any> Set<T>.where(sqlCondition: SQLCondition): Where<T> = where(sqlCondition)

infix fun <T : Any> Where<T>.and(sqlCondition: SQLCondition): Where<T> = and(sqlCondition)

infix fun <T : Any> Where<T>.or(sqlCondition: SQLCondition): Where<T> = and(sqlCondition)

infix fun <T : Any> Case<T>.`when`(sqlCondition: SQLCondition): CaseCondition<T> = `when`(sqlCondition)

infix fun <T : Any> Case<T>.`when`(property: IProperty<*>): CaseCondition<T> = `when`(property)

infix fun <T : Any> Case<T>.`when`(value: T?): CaseCondition<T> = `when`(value)

infix fun <T : Any> CaseCondition<T>.then(value: T?): Case<T> = then(value)

infix fun <T : Any> CaseCondition<T>.then(property: IProperty<*>): Case<T> = then(property)

infix fun <T : Any> Case<T>.`else`(value: T?) = _else(value)

infix fun <T : Any> Case<T>.end(columnName: String) = end(columnName)

// queriable extensions

val <T : Any> Queriable<T>.count: Long
    get() = count()

val <T : Any> Queriable<T>.cursor: Cursor?
    get() = query()

val <T : Any> Queriable<T>.hasData: Boolean
    get() = hasData()

val <T : Any> Queriable<T>.statement: DatabaseStatement
    get() = compileStatement()

val <T : Any> ModelQueriable<T>.list: MutableList<T>
    get() = queryList()

val <T : Any> ModelQueriable<T>.result: T?
    get() = querySingle()

val <T : Any> ModelQueriable<T>.cursorResult: CursorResult<T>
    get() = queryResults()

// async extensions

val <T : Any> ModelQueriable<T>.async: AsyncQuery<T>
    get() = async()

infix fun <T : Any> AsyncQuery<T>.list(callback: (QueryTransaction<*>, MutableList<T>?) -> Unit)
        = queryListResultCallback { queryTransaction, mutableList -> callback(queryTransaction, mutableList) }
        .execute()

infix fun <T : Any> AsyncQuery<T>.result(callback: (QueryTransaction<*>, T?) -> Unit)
        = querySingleResultCallback { queryTransaction, model -> callback(queryTransaction, model) }
        .execute()

infix fun <T : Any> AsyncQuery<T>.cursorResult(callback: (QueryTransaction<*>, CursorResult<T>) -> Unit)
        = queryResultCallback { queryTransaction, cursorResult -> callback(queryTransaction, cursorResult) }
        .execute()

val BaseModel.async: AsyncModel<BaseModel>
    get() = async()

infix fun <T : Any> AsyncModel<T>.insert(listener: (T) -> Unit) = withListener { listener(it) }.insert()

infix fun <T : Any> AsyncModel<T>.update(listener: (T) -> Unit) = withListener { listener(it) }.update()

infix fun <T : Any> AsyncModel<T>.delete(listener: (T) -> Unit) = withListener { listener(it) }.delete()

infix fun <T : Any> AsyncModel<T>.save(listener: (T) -> Unit) = withListener { listener(it) }.save()

// Transformable methods

infix fun <T : Any> Transformable<T>.groupBy(nameAlias: NameAlias) = groupBy(nameAlias)

infix fun <T : Any> Transformable<T>.groupBy(property: IProperty<*>) = groupBy(property)

infix fun <T : Any> Transformable<T>.orderBy(orderBy: OrderBy) = orderBy(orderBy)

infix fun <T : Any> Transformable<T>.limit(limit: Int) = limit(limit)

infix fun <T : Any> Transformable<T>.offset(offset: Int) = offset(offset)

infix fun <T : Any> Transformable<T>.having(sqlCondition: SQLCondition) = having(sqlCondition)

// join

infix fun <T : Any, V : Any> From<V>.innerJoin(joinTable: KClass<T>) = join(joinTable.java, Join.JoinType.INNER)

infix fun <T : Any, V : Any> From<V>.crossJoin(joinTable: KClass<T>) = join(joinTable.java, Join.JoinType.CROSS)

infix fun <T : Any, V : Any> From<V>.leftOuterJoin(joinTable: KClass<T>) = join(joinTable.java, Join.JoinType.LEFT_OUTER)

infix fun <T : Any, V : Any> Join<T, V>.on(sqlCondition: SQLCondition) = on(sqlCondition)

// update methods

fun <T : Any> update(modelClass: KClass<T>): Update<T> = SQLite.update(modelClass.java)

infix fun <T : Any> Update<T>.set(sqlCondition: SQLCondition) = set(sqlCondition)


// delete

inline fun <reified T : Any> delete() = SQLite.delete(T::class.java)

inline fun <reified T : Any> delete(deleteClause: From<T>.() -> BaseModelQueriable<T>) = deleteClause(SQLite.delete(T::class.java))

// insert methods

fun <T : Any> insert(modelClass: KClass<T>) = SQLite.insert(modelClass.java)

infix fun <T : Any> Insert<T>.orReplace(into: Array<out Pair<IProperty<*>, *>>) = orReplace().into(*into)

infix fun <T : Any> Insert<T>.orRollback(into: Array<out Pair<IProperty<*>, *>>) = orRollback().into(*into)

infix fun <T : Any> Insert<T>.orAbort(into: Array<out Pair<IProperty<*>, *>>) = orAbort().into(*into)

infix fun <T : Any> Insert<T>.orFail(into: Array<out Pair<IProperty<*>, *>>) = orFail().into(*into)

infix fun <T : Any> Insert<T>.orIgnore(into: Array<out Pair<IProperty<*>, *>>) = orIgnore().into(*into)

infix fun <T : Any> Insert<T>.select(from: From<*>) = select(from)

fun into(vararg pairs: Pair<IProperty<*>, *>): Array<out Pair<IProperty<*>, *>> = pairs

fun <T> Insert<T>.into(vararg pairs: Pair<IProperty<*>, *>): Insert<T> {
    val columns: MutableList<IProperty<*>> = java.util.ArrayList()
    val values = java.util.ArrayList<Any?>()
    pairs.forEach {
        columns.add(it.first)
        values.add(it.second)
    }
    this.columns(columns).values(values.toArray())
    return this
}


// DSL

fun <T> select(vararg property: IProperty<out IProperty<*>>,
               init: Select.() -> BaseModelQueriable<T>): BaseModelQueriable<T> {
    val select = SQLite.select(*property)
    return init(select)
}

fun <T : Any> select(init: Select.() -> BaseModelQueriable<T>):
        BaseModelQueriable<T> = init(SQLite.select())

inline fun <reified T : Any> Select.from(fromClause: From<T>.() -> Where<T>):
        BaseModelQueriable<T> = fromClause(from(T::class.java))

inline fun <T : Any> From<T>.where(sqlConditionClause: () -> SQLCondition) = where(sqlConditionClause())

inline fun <T : Any> Set<T>.where(sqlConditionClause: () -> SQLCondition) = where(sqlConditionClause())

inline fun <T : Any> Where<T>.and(sqlConditionClause: () -> SQLCondition) = and(sqlConditionClause())

inline fun <T : Any> Where<T>.or(sqlConditionClause: () -> SQLCondition) = or(sqlConditionClause())

inline fun <T : Any, reified TJoin : Any> From<T>.join(joinType: Join.JoinType, function: Join<TJoin, T>.() -> Unit): Where<T> {
    function(join(TJoin::class.java, joinType))
    return where()
}

inline fun <reified T : Any> insert(insertMethod: Insert<T>.() -> Unit): Insert<T> {
    val insert = SQLite.insert(T::class.java)
    insertMethod(insert)
    return insert
}

inline infix fun <T : Any, TJoin : Any> Join<TJoin, T>.on(conditionFunction: () -> Array<out SQLCondition>) = on(*conditionFunction())

inline fun <reified T : Any> update(setMethod: Update<T>.() -> BaseModelQueriable<T>): BaseModelQueriable<T> {
    val update = SQLite.update(T::class.java)
    return setMethod(update)
}

inline fun <T : Any> Update<T>.set(setClause: Set<T>.() -> Where<T>):
        BaseModelQueriable<T> = setClause(set())