package com.raizlabs.android.dbflow.rx.kotlinextensions

import android.database.Cursor
import com.raizlabs.android.dbflow.rx.language.RXModelQueriable
import com.raizlabs.android.dbflow.rx.language.RXQueriable
import com.raizlabs.android.dbflow.rx.language.RXSQLite
import com.raizlabs.android.dbflow.sql.language.BaseQueriable
import com.raizlabs.android.dbflow.sql.language.CursorResult
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable
import com.raizlabs.android.dbflow.sql.queriable.Queriable
import com.raizlabs.android.dbflow.structure.database.DatabaseStatement

fun <T : Any> ModelQueriable<T>.rx() = RXSQLite.rx<T>(this)

inline fun <reified T : Any> Queriable.rx() = RXSQLite.rx(T::class.java, this)

// queriable extensions

inline val RXQueriable.count
    get() = count()

inline val RXQueriable.cursor
    get() = query()

inline val RXQueriable.hasData
    get() = hasData()

inline val RXQueriable.statement
    get() = compileStatement()

inline val <T : Any> RXModelQueriable<T>.list
    get() = queryList()

inline val <T : Any> RXModelQueriable<T>.result
    get() = querySingle()

inline val <T : Any> RXModelQueriable<T>.cursorResult
    get() = queryResults()

inline val <T : Any> RXModelQueriable<T>.streamResults
    get() = queryStreamResults()

// async extensions

infix inline fun <T : Any> RXModelQueriable<T>.list(crossinline func: (MutableList<T>) -> Unit) = list.subscribe({ func(it) })

infix inline fun <T : Any> RXModelQueriable<T>.result(crossinline func: (T?) -> Unit) = result.subscribe({ func(it) })

infix inline fun <T : Any> RXModelQueriable<T>.streamResults(crossinline func: (T?) -> Unit) = streamResults.subscribe({ func(it) })

infix inline fun <T : Any> RXModelQueriable<T>.cursorResult(crossinline func: (CursorResult<T>) -> Unit) = cursorResult.subscribe({ func(it) })

infix inline fun RXQueriable.statement(crossinline func: (DatabaseStatement) -> Unit) = statement.subscribe({ func(it) })

infix inline fun RXQueriable.hasData(crossinline func: (Boolean) -> Unit) = hasData.subscribe({ func(it) })

infix inline fun RXQueriable.cursor(crossinline func: (Cursor) -> Unit) = cursor.subscribe({ func(it) })

infix inline fun RXQueriable.count(crossinline func: (Long) -> Unit) = count.subscribe({ func(it) })