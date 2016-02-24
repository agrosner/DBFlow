package com.raizlabs.android.dbflow.kotlinextensions

import com.raizlabs.android.dbflow.sql.language.*
import com.raizlabs.android.dbflow.sql.language.Set
import com.raizlabs.android.dbflow.sql.language.property.IProperty
import com.raizlabs.android.dbflow.sql.language.property.Property
import com.raizlabs.android.dbflow.structure.Model
import java.util.*

/**
 * Description: A file containing extensions for adding query syntactic sugar.
 */


fun <TModel : Model> select(vararg property: IProperty<out IProperty<*>>, init: Select.() -> BaseModelQueriable<TModel>): BaseModelQueriable<TModel> {
    var select = SQLite.select(*property)
    return init(select)
}

fun <TModel : Model> select(init: Select.() -> BaseModelQueriable<TModel>):
        BaseModelQueriable<TModel> = init(SQLite.select())

inline fun <reified TModel : Model> Select.from(fromClause: From<TModel>.() -> Where<TModel>):
        BaseModelQueriable<TModel> = fromClause(from(TModel::class.java))

inline fun <reified TModel : Model> Select.from(): From<TModel> = from(TModel::class.java)

inline fun <TModel : Model> From<TModel>.where(sqlConditionClause: () -> SQLCondition): Where<TModel> = where(sqlConditionClause())

inline fun <TModel : Model> Set<TModel>.where(sqlConditionClause: () -> SQLCondition): Where<TModel> = where(sqlConditionClause())

inline fun <TModel : Model> Where<TModel>.and(sqlConditionClause: () -> SQLCondition): Where<TModel> = and(sqlConditionClause())

inline fun <TModel : Model> Where<TModel>.or(sqlConditionClause: () -> SQLCondition): Where<TModel> = or(sqlConditionClause())

inline fun <TModel : Model, reified TJoin : Model> From<TModel>.join(joinType: Join.JoinType, function: Join<TJoin, TModel>.() -> Unit): Where<TModel> {
    function(join(TJoin::class.java, joinType))
    return where()
}

inline fun <TModel : Model, TJoin : Model> Join<TJoin, TModel>.on(conditionFunction: () -> SQLCondition) = on(conditionFunction())

inline fun <reified TModel : Model> update(setMethod: Update<TModel>.() -> BaseModelQueriable<TModel>): BaseModelQueriable<TModel> {
    var update = SQLite.update(TModel::class.java)
    return setMethod(update)
}

inline fun <TModel : Model> Update<TModel>.set(setClause: Set<TModel>.() -> Where<TModel>):
        BaseModelQueriable<TModel> = setClause(set())

inline fun <reified TModel : Model> insert(insertMethod: Insert<TModel>.() -> Unit): Insert<TModel> {
    var insert = SQLite.insert(TModel::class.java)
    insertMethod(insert)
    return insert
}

fun <TModel : Model> Insert<TModel>.into(vararg pairs: Pair<Property<*>, *>) {
    var columns = ArrayList<Property<*>>()
    var values = ArrayList<Any?>()
    pairs.forEach {
        columns.add(it.first)
        values.add(it.second)
    }
    this.columns(columns as List<IProperty<IProperty<*>>>).values(values.toArray())
}

inline fun <reified TModel : Model> delete(): BaseModelQueriable<TModel> = SQLite.delete(TModel::class.java)

inline fun <reified TModel : Model> delete(deleteClause: From<TModel>.() -> BaseModelQueriable<TModel>): BaseModelQueriable<TModel> = deleteClause(SQLite.delete(TModel::class.java))
