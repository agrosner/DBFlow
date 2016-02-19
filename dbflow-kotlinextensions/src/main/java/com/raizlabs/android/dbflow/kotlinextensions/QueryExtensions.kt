package com.raizlabs.android.dbflow.kotlinextensions

import com.raizlabs.android.dbflow.sql.language.*
import com.raizlabs.android.dbflow.sql.language.property.IProperty
import com.raizlabs.android.dbflow.structure.Model

/**
 * Description: A file containing extensions for adding query syntactic sugar.
 */


fun <TModel : Model> select(vararg property: IProperty<out IProperty<*>>, init: Select.() -> BaseModelQueriable<TModel>): BaseModelQueriable<TModel> {
    var select = SQLite.select(*property)
    return init(select)
}

fun <TModel : Model> select(init: Select.() -> BaseModelQueriable<TModel>): BaseModelQueriable<TModel> {
    return init(SQLite.select())
}

fun <TModel : Model> Select.from(tableClass: Class<TModel>, fromClause: From<TModel>.() -> Where<TModel>): BaseModelQueriable<TModel> {
    return fromClause(from(tableClass))
}

fun <TModel : Model> Where<TModel>.where(sqlCondition: SQLCondition): Where<TModel> {
    return and(sqlCondition)
}

fun <TModel: Model, TJoin: Model> From<TModel>.join(joinClass: Class<TJoin>,
                                                    joinType: Join.JoinType, onClause: Join<TJoin, TModel>.() -> From<TModel>) : Where<TModel> {
    return onClause(join(joinClass, joinType)).where()
}