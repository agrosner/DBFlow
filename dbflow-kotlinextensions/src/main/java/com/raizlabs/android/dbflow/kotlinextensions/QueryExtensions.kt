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

<<<<<<< HEAD
fun <TModel : Model> delete(modelClass: KClass<TModel>): From<TModel> = SQLite.delete(modelClass.java)

infix fun <TModel : Model> Select.from(modelClass: KClass<TModel>) = from(modelClass.java)

infix fun <TModel : Model> From<TModel>.whereExists(where: Where<TModel>) = where().exists(where)

infix fun <TModel : Model> From<TModel>.where(sqlCondition: SQLCondition) = where(sqlCondition)

infix fun <TModel : Model> Set<TModel>.where(sqlCondition: SQLCondition) = where(sqlCondition)

infix fun <TModel : Model> Where<TModel>.and(sqlCondition: SQLCondition) = and(sqlCondition)

infix fun <TModel : Model> Where<TModel>.or(sqlCondition: SQLCondition) = and(sqlCondition)

infix fun <TModel> Case<TModel>.`when`(sqlCondition: SQLCondition) = `when`(sqlCondition)

infix fun <TModel> Case<TModel>.`when`(property: IProperty<*>) = `when`(property)

infix fun <TModel> Case<TModel>.`when`(value: TModel?) = `when`(value)

infix fun <TModel> CaseCondition<TModel>.then(value: TModel?) = then(value)

infix fun <TModel> CaseCondition<TModel>.then(property: IProperty<*>) = then(property)

infix fun <TModel> Case<TModel>.`else`(value: TModel?) = _else(value)

infix fun <TModel> Case<TModel>.end(columnName: String) = end(columnName)
=======
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
>>>>>>> raizlabs/develop

// queriable extensions

val Queriable.count: Long
    get() = count()

val Queriable.cursor: Cursor?
    get() = query()

val Queriable.hasData: Boolean
    get() = hasData()

val Queriable.statement: DatabaseStatement
    get() = compileStatement()

<<<<<<< HEAD
val <TModel : Model> ModelQueriable<TModel>.list: MutableList<TModel>
    get() = queryList()

val <TModel : Model> ModelQueriable<TModel>.result: TModel?
    get() = querySingle()

val <TModel : Model> ModelQueriable<TModel>.cursorResult: CursorResult<TModel>
=======
val <T : Any> ModelQueriable<T>.list: MutableList<T>
    get() = queryList()

val <T : Any> ModelQueriable<T>.result: T?
    get() = querySingle()

val <T : Any> ModelQueriable<T>.cursorResult: CursorResult<T>
>>>>>>> raizlabs/develop
    get() = queryResults()

// async extensions

<<<<<<< HEAD
val <TModel : Model> ModelQueriable<TModel>.async: AsyncQuery<TModel>
    get() = async()

infix fun <TModel : Model> AsyncQuery<TModel>.list(callback: (QueryTransaction<*>, MutableList<TModel>?) -> Unit)
        = queryListResultCallback { queryTransaction, mutableList -> callback(queryTransaction, mutableList) }
        .execute()

infix fun <TModel : Model> AsyncQuery<TModel>.result(callback: (QueryTransaction<*>, TModel?) -> Unit)
        = querySingleResultCallback { queryTransaction, model -> callback(queryTransaction, model) }
        .execute()

infix fun <TModel : Model> AsyncQuery<TModel>.cursorResult(callback: (QueryTransaction<*>, CursorResult<TModel>) -> Unit)
=======
val <T : Any> ModelQueriable<T>.async: AsyncQuery<T>
    get() = async()

infix fun <T : Any> AsyncQuery<T>.list(callback: (QueryTransaction<*>, MutableList<T>?) -> Unit)
        = queryListResultCallback { queryTransaction, mutableList -> callback(queryTransaction, mutableList) }
        .execute()

infix fun <T : Any> AsyncQuery<T>.result(callback: (QueryTransaction<*>, T?) -> Unit)
        = querySingleResultCallback { queryTransaction, model -> callback(queryTransaction, model) }
        .execute()

infix fun <T : Any> AsyncQuery<T>.cursorResult(callback: (QueryTransaction<*>, CursorResult<T>) -> Unit)
>>>>>>> raizlabs/develop
        = queryResultCallback { queryTransaction, cursorResult -> callback(queryTransaction, cursorResult) }
        .execute()

val BaseModel.async: AsyncModel<BaseModel>
    get() = async()

<<<<<<< HEAD
infix fun <TModel : Model> AsyncModel<TModel>.insert(listener: (TModel) -> Unit) = withListener { listener(it) }.insert()

infix fun <TModel : Model> AsyncModel<TModel>.update(listener: (TModel) -> Unit) = withListener { listener(it) }.update()

infix fun <TModel : Model> AsyncModel<TModel>.delete(listener: (TModel) -> Unit) = withListener { listener(it) }.delete()

infix fun <TModel : Model> AsyncModel<TModel>.save(listener: (TModel) -> Unit) = withListener { listener(it) }.save()

// Transformable methods

infix fun <TModel : Model> Transformable<TModel>.groupBy(nameAlias: NameAlias) = groupBy(nameAlias)

infix fun <TModel : Model> Transformable<TModel>.groupBy(property: IProperty<*>) = groupBy(property)

infix fun <TModel : Model> Transformable<TModel>.orderBy(orderBy: OrderBy) = orderBy(orderBy)

infix fun <TModel : Model> Transformable<TModel>.limit(limit: Int) = limit(limit)

infix fun <TModel : Model> Transformable<TModel>.offset(offset: Int) = offset(offset)

infix fun <TModel : Model> Transformable<TModel>.having(sqlCondition: SQLCondition) = having(sqlCondition)

// join

infix fun <TModel : Model, V : Model> From<V>.innerJoin(joinTable: KClass<TModel>) = join(joinTable.java, Join.JoinType.INNER)

infix fun <TModel : Model, V : Model> From<V>.crossJoin(joinTable: KClass<TModel>) = join(joinTable.java, Join.JoinType.CROSS)

infix fun <TModel : Model, V : Model> From<V>.leftOuterJoin(joinTable: KClass<TModel>) = join(joinTable.java, Join.JoinType.LEFT_OUTER)

infix fun <TModel : Model, V : Model> Join<TModel, V>.on(sqlCondition: SQLCondition) = on(sqlCondition)

// update methods

fun <TModel : Model> update(modelClass: KClass<TModel>): Update<TModel> = SQLite.update(modelClass.java)

infix fun <TModel : Model> Update<TModel>.set(sqlCondition: SQLCondition) = set(sqlCondition)
=======
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
>>>>>>> raizlabs/develop


// delete

inline fun <reified T : Any> delete() = SQLite.delete(T::class.java)

inline fun <reified T : Any> delete(deleteClause: From<T>.() -> BaseModelQueriable<T>) = deleteClause(SQLite.delete(T::class.java))

// insert methods

<<<<<<< HEAD
fun <TModel : Model> insert(modelClass: KClass<TModel>) = SQLite.insert(modelClass.java)

infix fun <TModel : Model> Insert<TModel>.orReplace(into: Array<out Pair<IProperty<*>, *>>) = orReplace().into(*into)

infix fun <TModel : Model> Insert<TModel>.orRollback(into: Array<out Pair<IProperty<*>, *>>) = orRollback().into(*into)

infix fun <TModel : Model> Insert<TModel>.orAbort(into: Array<out Pair<IProperty<*>, *>>) = orAbort().into(*into)

infix fun <TModel : Model> Insert<TModel>.orFail(into: Array<out Pair<IProperty<*>, *>>) = orFail().into(*into)

infix fun <TModel : Model> Insert<TModel>.orIgnore(into: Array<out Pair<IProperty<*>, *>>) = orIgnore().into(*into)

infix fun <TModel : Model> Insert<TModel>.select(from: From<*>) = select(from)
=======
fun <T : Any> insert(modelClass: KClass<T>) = SQLite.insert(modelClass.java)

infix fun <T : Any> Insert<T>.orReplace(into: Array<out Pair<IProperty<*>, *>>) = orReplace().into(*into)

infix fun <T : Any> Insert<T>.orRollback(into: Array<out Pair<IProperty<*>, *>>) = orRollback().into(*into)

infix fun <T : Any> Insert<T>.orAbort(into: Array<out Pair<IProperty<*>, *>>) = orAbort().into(*into)

infix fun <T : Any> Insert<T>.orFail(into: Array<out Pair<IProperty<*>, *>>) = orFail().into(*into)

infix fun <T : Any> Insert<T>.orIgnore(into: Array<out Pair<IProperty<*>, *>>) = orIgnore().into(*into)

infix fun <T : Any> Insert<T>.select(from: From<*>) = select(from)
>>>>>>> raizlabs/develop

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