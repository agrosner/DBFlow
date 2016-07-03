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

// queriable extensions

val Queriable.count: Long
    get() = count()

val Queriable.cursor: Cursor?
    get() = query()

val Queriable.hasData: Boolean
    get() = hasData()

val Queriable.statement: DatabaseStatement
    get() = compileStatement()

val <TModel : Model> ModelQueriable<TModel>.list: MutableList<TModel>
    get() = queryList()

val <TModel : Model> ModelQueriable<TModel>.result: TModel?
    get() = querySingle()

val <TModel : Model> ModelQueriable<TModel>.cursorResult: CursorResult<TModel>
    get() = queryResults()

// async extensions

val <TModel : Model> ModelQueriable<TModel>.async: AsyncQuery<TModel>
    get() = async()

infix fun <TModel : Model> AsyncQuery<TModel>.list(callback: (QueryTransaction<*>, MutableList<TModel>?) -> Unit)
        = queryListResultCallback { queryTransaction, mutableList -> callback(queryTransaction, mutableList) }
        .execute()

infix fun <TModel : Model> AsyncQuery<TModel>.result(callback: (QueryTransaction<*>, TModel?) -> Unit)
        = querySingleResultCallback { queryTransaction, model -> callback(queryTransaction, model) }
        .execute()

infix fun <TModel : Model> AsyncQuery<TModel>.cursorResult(callback: (QueryTransaction<*>, CursorResult<TModel>) -> Unit)
        = queryResultCallback { queryTransaction, cursorResult -> callback(queryTransaction, cursorResult) }
        .execute()

val BaseModel.async: AsyncModel<BaseModel>
    get() = async()

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


// delete

inline fun <reified TModel : Model> delete() = SQLite.delete(TModel::class.java)

inline fun <reified TModel : Model> delete(deleteClause: From<TModel>.() -> BaseModelQueriable<TModel>) = deleteClause(SQLite.delete(TModel::class.java))

// insert methods

fun <TModel : Model> insert(modelClass: KClass<TModel>) = SQLite.insert(modelClass.java)

infix fun <TModel : Model> Insert<TModel>.orReplace(into: Array<out Pair<IProperty<*>, *>>) = orReplace().into(*into)

infix fun <TModel : Model> Insert<TModel>.orRollback(into: Array<out Pair<IProperty<*>, *>>) = orRollback().into(*into)

infix fun <TModel : Model> Insert<TModel>.orAbort(into: Array<out Pair<IProperty<*>, *>>) = orAbort().into(*into)

infix fun <TModel : Model> Insert<TModel>.orFail(into: Array<out Pair<IProperty<*>, *>>) = orFail().into(*into)

infix fun <TModel : Model> Insert<TModel>.orIgnore(into: Array<out Pair<IProperty<*>, *>>) = orIgnore().into(*into)

infix fun <TModel : Model> Insert<TModel>.select(from: From<*>) = select(from)

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

fun <TModel : Model> select(vararg property: IProperty<out IProperty<*>>,
                            init: Select.() -> BaseModelQueriable<TModel>): BaseModelQueriable<TModel> {
    val select = SQLite.select(*property)
    return init(select)
}

fun <TModel : Model> select(init: Select.() -> BaseModelQueriable<TModel>):
        BaseModelQueriable<TModel> = init(SQLite.select())

inline fun <reified TModel : Model> Select.from(fromClause: From<TModel>.() -> Where<TModel>):
        BaseModelQueriable<TModel> = fromClause(from(TModel::class.java))

inline fun <TModel : Model> From<TModel>.where(sqlConditionClause: () -> SQLCondition) = where(sqlConditionClause())

inline fun <TModel : Model> Set<TModel>.where(sqlConditionClause: () -> SQLCondition) = where(sqlConditionClause())

inline fun <TModel : Model> Where<TModel>.and(sqlConditionClause: () -> SQLCondition) = and(sqlConditionClause())

inline fun <TModel : Model> Where<TModel>.or(sqlConditionClause: () -> SQLCondition) = or(sqlConditionClause())

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