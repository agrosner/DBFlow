package com.raizlabs.dbflow5.query

import com.raizlabs.dbflow5.query.property.IProperty

/**
 * Description: Provides a standard set of methods for ending a SQLite query method. These include
 * groupby, orderby, having, limit and offset.
 */
interface Transformable<T : Any> {

    fun groupBy(vararg nameAliases: NameAlias): Where<T>

    fun groupBy(vararg properties: IProperty<*>): Where<T>

    fun orderBy(nameAlias: NameAlias, ascending: Boolean): Where<T>

    fun orderBy(property: IProperty<*>, ascending: Boolean): Where<T>

    infix fun orderBy(orderBy: OrderBy): Where<T>

    infix fun limit(count: Long): Where<T>

    infix fun offset(offset: Long): Where<T>

    fun having(vararg conditions: SQLOperator): Where<T>

    fun orderByAll(orderBies: List<OrderBy>): Where<T>
}


infix fun <T : Any> Transformable<T>.groupBy(nameAlias: NameAlias): Where<T> = groupBy(nameAlias)

infix fun <T : Any> Transformable<T>.groupBy(property: IProperty<*>): Where<T> = groupBy(property)

infix fun <T : Any> Transformable<T>.having(sqlOperator: SQLOperator): Where<T> = having(sqlOperator)