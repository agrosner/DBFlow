package com.raizlabs.android.dbflow.kotlinextensions

import com.raizlabs.android.dbflow.annotation.Collate
import com.raizlabs.android.dbflow.sql.language.NameAlias
import com.raizlabs.android.dbflow.sql.language.Operator
import com.raizlabs.android.dbflow.sql.language.OperatorGroup.clause
import com.raizlabs.android.dbflow.sql.language.SQLOperator


fun <T : Any> NameAlias.op() = Operator.op<T>(this)

fun <T : Any> String.op(): Operator<T> = nameAlias.op<T>()

infix fun <T : Any> Operator<T>.collate(collation: Collate) = collate(collation)

infix fun <T : Any> Operator<T>.collate(collation: String) = collate(collation)

infix fun <T : Any> Operator<T>.postfix(collation: String) = postfix(collation)

infix fun <T : Any> Operator.Between<T>.and(value: T?) = and(value)

infix fun <T : Any> Operator.In<T>.and(value: T?) = and(value)

infix fun <T : Any> Operator<T>.and(sqlOperator: SQLOperator) = clause(this).and(sqlOperator)

infix fun <T : Any> Operator<T>.or(sqlOperator: SQLOperator) = clause(this).or(sqlOperator)

infix fun <T : Any> Operator<T>.andAll(sqlOperator: Collection<SQLOperator>) = clause(this).andAll(sqlOperator)

infix fun <T : Any> Operator<T>.orAll(sqlOperator: Collection<SQLOperator>) = clause(this).orAll(sqlOperator)

