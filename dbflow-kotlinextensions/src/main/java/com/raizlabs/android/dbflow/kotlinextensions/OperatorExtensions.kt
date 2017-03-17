package com.raizlabs.android.dbflow.kotlinextensions

import com.raizlabs.android.dbflow.sql.language.NameAlias
import com.raizlabs.android.dbflow.sql.language.Operator


fun <T : Any> NameAlias.op() = Operator.op<T>(this)

fun <T : Any> String.op(): Operator<T> = nameAlias.op<T>()