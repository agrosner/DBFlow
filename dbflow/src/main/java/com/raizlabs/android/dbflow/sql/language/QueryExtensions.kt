package com.raizlabs.android.dbflow.sql.language

import com.raizlabs.android.dbflow.sql.language.property.IProperty
import kotlin.reflect.KClass

/**
 * Description: A file containing extensions for adding query syntactic sugar.
 */

inline val select: Select
    get() = SQLite.select()

inline fun <reified T : Any> Select.from() = from(T::class.java)

fun <T : Any> delete(modelClass: KClass<T>) = SQLite.delete(modelClass.java)

infix fun <T : Any> Select.from(modelClass: KClass<T>) = from(modelClass.java)

infix fun <T : Any> From<T>.whereExists(where: Where<T>) = where().exists(where)

infix fun <T : Any> From<T>.where(sqlOperator: SQLOperator) = where(sqlOperator)

infix fun <T : Any> From<T>.`as`(alias: String) = `as`(alias)

infix fun <T : Any> Set<T>.where(sqlOperator: SQLOperator) = where(sqlOperator)

infix fun <T : Any> Where<T>.and(sqlOperator: SQLOperator) = and(sqlOperator)

infix fun <T : Any> Where<T>.or(sqlOperator: SQLOperator) = or(sqlOperator)

infix fun <T : Any> Case<T>.`when`(sqlOperator: SQLOperator) = `when`(sqlOperator)

infix fun <T : Any> Case<T>.`when`(property: IProperty<*>) = `when`(property)

infix fun <T : Any> Case<T>.`when`(value: T?) = `when`(value)

infix fun <T : Any> CaseCondition<T>.then(value: T?) = then(value)

infix fun <T : Any> CaseCondition<T>.then(property: IProperty<*>) = then(property)

infix fun <T : Any> Case<T>.`else`(value: T?) = _else(value)

infix fun <T : Any> Case<T>.end(columnName: String) = end(columnName)

fun <T : Any> case(caseColumn: IProperty<*>) = SQLite._case<T>(caseColumn)

fun <T : Any> caseWhen(operator: SQLOperator) = SQLite.caseWhen<T>(operator)

inline fun <reified T : Any> insert() = Insert(T::class.java)

inline fun <reified T : Any> indexOn(indexName: String, vararg property: IProperty<*>) = Index<T>(indexName).on(T::class.java, *property)

inline fun <reified T : Any> indexOn(indexName: String, firstNameAlias: NameAlias, vararg arrayOfNameAlias: NameAlias) = Index<T>(indexName).on(T::class.java, firstNameAlias, *arrayOfNameAlias)


