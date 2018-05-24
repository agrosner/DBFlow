@file:JvmName("TransformableUtils")

package com.dbflow5.query

import com.dbflow5.query.property.IProperty
import com.dbflow5.sql.QueryCloneable

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

    fun orderByAll(orderByList: List<OrderBy>): Where<T>
}

infix fun <T : Any> Transformable<T>.groupBy(nameAlias: NameAlias): Where<T> = groupBy(nameAlias)

infix fun <T : Any> Transformable<T>.groupBy(property: IProperty<*>): Where<T> = groupBy(property)

infix fun <T : Any> Transformable<T>.having(sqlOperator: SQLOperator): Where<T> = having(sqlOperator)

/**
 * Constrains the given [Transformable] by the [offset] and [limit] specified. It copies over itself
 *  into a new instance to not preserve changes.
 */
fun <T : Any> Transformable<T>.constrain(offset: Long, limit: Long): ModelQueriable<T> {
    var tr: Transformable<T> = this
    @Suppress("UNCHECKED_CAST")
    if (tr is QueryCloneable<*>) {
        tr = tr.cloneSelf() as Transformable<T>
    }
    return tr.offset(offset).limit(limit)
}

/**
 * Attempt to constrain this [ModelQueriable] if it supports it via [Transformable] methods. Otherwise,
 * we just return itself.
 */
@Suppress("UNCHECKED_CAST")
fun <T : Any> ModelQueriable<T>.attemptConstrain(offset: Long, limit: Long): ModelQueriable<T> {
    return when {
        this is Transformable<*> -> (this as Transformable<T>).constrain(offset, limit)
        else -> this
    }
}