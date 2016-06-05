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
import com.raizlabs.android.dbflow.structure.Model
import com.raizlabs.android.dbflow.structure.database.DatabaseStatement
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction
import kotlin.reflect.KClass

/**
 * Description: A file containing extensions for adding query syntactic sugar.
 */

val select: Select
    get() = SQLite.select()

inline fun <reified TModel : Model> Select.from(): From<TModel> = from(TModel::class.java)

fun <T : Model> delete(modelClass: KClass<T>): From<T> = SQLite.delete(modelClass.java)

infix fun <T : Model> Select.from(modelClass: KClass<T>): From<T> = from(modelClass.java)

infix fun <T : Model> From<T>.whereExists(where: Where<T>): Where<T> {
    return where().exists(where)
}

infix fun <T : Model> From<T>.where(sqlCondition: SQLCondition): Where<T> = where(sqlCondition)

infix fun <T : Model> Set<T>.where(sqlCondition: SQLCondition): Where<T> = where(sqlCondition)

infix fun <T : Model> Where<T>.and(sqlCondition: SQLCondition): Where<T> = and(sqlCondition)

infix fun <T : Model> Where<T>.or(sqlCondition: SQLCondition): Where<T> = and(sqlCondition)

infix fun <T> Case<T>.`when`(sqlCondition: SQLCondition) = `when`(sqlCondition)

infix fun <T> Case<T>.`when`(property: IProperty<*>) = `when`(property)

infix fun <T> Case<T>.`when`(value: T?) = `when`(value)

infix fun <T> CaseCondition<T>.then(value: T?) = then(value)

infix fun <T> CaseCondition<T>.then(property: IProperty<*>) = then(property)

infix fun <T> Case<T>.`else`(value: T?) = _else(value)

infix fun <T> Case<T>.end(columnName: String) = end(columnName)

// queriable extensions

val Queriable.count: Long
    get() = count()

val Queriable.cursor: Cursor?
    get() = query()

val Queriable.hasData: Boolean
    get() = hasData()

val Queriable.statement: DatabaseStatement
    get() = compileStatement()

val <T : Model> ModelQueriable<T>.list: MutableList<T>
    get() = queryList()

val <T : Model> ModelQueriable<T>.result: T?
    get() = querySingle()

val <T : Model> ModelQueriable<T>.cursorResult: CursorResult<T>
    get() = queryResults()

// async extensions

val <T : Model> ModelQueriable<T>.async: AsyncQuery<T>
    get() = async()

infix fun <T : Model> AsyncQuery<T>.list(callback: (QueryTransaction<*>, MutableList<T>?) -> Unit)
        = queryListResultCallback { queryTransaction, mutableList -> callback(queryTransaction, mutableList) }
        .execute()

infix fun <T : Model> AsyncQuery<T>.result(callback: (QueryTransaction<*>, T?) -> Unit)
        = querySingleResultCallback { queryTransaction, model -> callback(queryTransaction, model) }
        .execute()

infix fun <T : Model> AsyncQuery<T>.cursorResult(callback: (QueryTransaction<*>, CursorResult<T>) -> Unit)
        = queryResultCallback { queryTransaction, cursorResult -> callback(queryTransaction, cursorResult) }
        .execute()

val BaseModel.async: AsyncModel<BaseModel>
    get() = async()

infix fun <T : Model> AsyncModel<T>.insert(listener: (T) -> Unit) = withListener { listener(it) }.insert()

infix fun <T : Model> AsyncModel<T>.update(listener: (T) -> Unit) = withListener { listener(it) }.update()

infix fun <T : Model> AsyncModel<T>.delete(listener: (T) -> Unit) = withListener { listener(it) }.delete()

infix fun <T : Model> AsyncModel<T>.save(listener: (T) -> Unit) = withListener { listener(it) }.save()

// Transformable methods

infix fun <T : Model> Transformable<T>.groupBy(nameAlias: NameAlias): Where<T> = groupBy(nameAlias)

infix fun <T : Model> Transformable<T>.groupBy(property: IProperty<*>): Where<T> = groupBy(property)

infix fun <T : Model> Transformable<T>.orderBy(orderBy: OrderBy): Where<T> = orderBy(orderBy)

infix fun <T : Model> Transformable<T>.limit(limit: Int): Where<T> = limit(limit)

infix fun <T : Model> Transformable<T>.offset(offset: Int): Where<T> = offset(offset)

infix fun <T : Model> Transformable<T>.having(sqlCondition: SQLCondition) = having(sqlCondition)

// join

infix fun <T : Model, V : Model> From<V>.innerJoin(joinTable: KClass<T>): Join<T, V> = join(joinTable.java, Join.JoinType.INNER)

infix fun <T : Model, V : Model> From<V>.crossJoin(joinTable: KClass<T>): Join<T, V> = join(joinTable.java, Join.JoinType.CROSS)

infix fun <T : Model, V : Model> From<V>.leftOuterJoin(joinTable: KClass<T>): Join<T, V> = join(joinTable.java, Join.JoinType.LEFT_OUTER)

infix fun <T : Model, V : Model> Join<T, V>.on(sqlCondition: SQLCondition): From<V> = on(sqlCondition)

// update methods

fun <T : Model> update(modelClass: KClass<T>): Update<T> = SQLite.update(modelClass.java)

infix fun <T : Model> Update<T>.set(sqlCondition: SQLCondition) = set(sqlCondition)


// delete

inline fun <reified TModel : Model> delete(): BaseModelQueriable<TModel> = SQLite.delete(TModel::class.java)

inline fun <reified TModel : Model> delete(deleteClause: From<TModel>.() -> BaseModelQueriable<TModel>): BaseModelQueriable<TModel> = deleteClause(SQLite.delete(TModel::class.java))

// insert methods

fun <T : Model> insert(modelClass: KClass<T>): Insert<T> = SQLite.insert(modelClass.java)

infix fun <T : Model> Insert<T>.orReplace(into: Array<out Pair<IProperty<*>, *>>): Insert<T> = orReplace().into(*into)

infix fun <T : Model> Insert<T>.orRollback(into: Array<out Pair<IProperty<*>, *>>): Insert<T> = orRollback().into(*into)

infix fun <T : Model> Insert<T>.orAbort(into: Array<out Pair<IProperty<*>, *>>): Insert<T> = orAbort().into(*into)

infix fun <T : Model> Insert<T>.orFail(into: Array<out Pair<IProperty<*>, *>>): Insert<T> = orFail().into(*into)

infix fun <T : Model> Insert<T>.orIgnore(into: Array<out Pair<IProperty<*>, *>>): Insert<T> = orIgnore().into(*into)

infix fun <T : Model> Insert<T>.select(from: From<*>): Insert<T> = select(from)

fun into(vararg pairs: Pair<IProperty<*>, *>): Array<out Pair<IProperty<*>, *>> = pairs

fun <TModel : Model> Insert<TModel>.into(vararg pairs: Pair<IProperty<*>, *>): Insert<TModel> {
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

fun <TModel : Model> select(vararg property: IProperty<out IProperty<*>>, init: Select.() -> BaseModelQueriable<TModel>): BaseModelQueriable<TModel> {
    val select = SQLite.select(*property)
    return init(select)
}

fun <TModel : Model> select(init: Select.() -> BaseModelQueriable<TModel>):
        BaseModelQueriable<TModel> = init(SQLite.select())

inline fun <reified TModel : Model> Select.from(fromClause: From<TModel>.() -> Where<TModel>):
        BaseModelQueriable<TModel> = fromClause(from(TModel::class.java))

inline fun <TModel : Model> From<TModel>.where(sqlConditionClause: () -> SQLCondition): Where<TModel> = where(sqlConditionClause())

inline fun <TModel : Model> Set<TModel>.where(sqlConditionClause: () -> SQLCondition): Where<TModel> = where(sqlConditionClause())

inline fun <TModel : Model> Where<TModel>.and(sqlConditionClause: () -> SQLCondition): Where<TModel> = and(sqlConditionClause())

inline fun <TModel : Model> Where<TModel>.or(sqlConditionClause: () -> SQLCondition): Where<TModel> = or(sqlConditionClause())

inline fun <TModel : Model, reified TJoin : Model> From<TModel>.join(joinType: Join.JoinType, function: Join<TJoin, TModel>.() -> Unit): Where<TModel> {
    function(join(TJoin::class.java, joinType))
    return where()
}

inline fun <reified TModel : Model> insert(insertMethod: Insert<TModel>.() -> Unit): Insert<TModel> {
    val insert = SQLite.insert(TModel::class.java)
    insertMethod(insert)
    return insert
}

inline infix fun <TModel : Model, TJoin : Model> Join<TJoin, TModel>.on(conditionFunction: () -> Array<out SQLCondition>) = on(*conditionFunction())

inline fun <reified TModel : Model> update(setMethod: Update<TModel>.() -> BaseModelQueriable<TModel>): BaseModelQueriable<TModel> {
    val update = SQLite.update(TModel::class.java)
    return setMethod(update)
}

inline fun <TModel : Model> Update<TModel>.set(setClause: Set<TModel>.() -> Where<TModel>):
        BaseModelQueriable<TModel> = setClause(set())