package com.raizlabs.android.dbflow.rx.kotlinextensions

import android.database.Cursor
import com.raizlabs.android.dbflow.rx.language.RXModelQueriable
import com.raizlabs.android.dbflow.rx.language.RXQueriable
import com.raizlabs.android.dbflow.rx.language.RXSQLite
import com.raizlabs.android.dbflow.rx.structure.BaseRXModel
import com.raizlabs.android.dbflow.sql.language.BaseQueriable
import com.raizlabs.android.dbflow.sql.language.CursorResult
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable
import com.raizlabs.android.dbflow.sql.queriable.Queriable
import com.raizlabs.android.dbflow.structure.database.DatabaseStatement
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper
import rx.Subscription

fun <T : Any> ModelQueriable<T>.rx() = RXSQLite.rx<T>(this)

fun <T : Any> BaseQueriable<T>.rxBaseQueriable() = RXSQLite.rx<T>(table, this)

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

inline val <T : Any> RXModelQueriable<T>.tableChanges
    get() = observeOnTableChanges()

// model extensions

fun BaseRXModel.save(databaseWrapper: DatabaseWrapper, func: (Boolean) -> Unit): Subscription = save(databaseWrapper).subscribe { func(it) }

infix inline fun BaseRXModel.save(crossinline func: (Boolean) -> Unit): Subscription = save().subscribe { func(it) }

fun BaseRXModel.insert(databaseWrapper: DatabaseWrapper, func: (Long) -> Unit): Subscription = insert(databaseWrapper).subscribe { func(it) }

infix inline fun BaseRXModel.insert(crossinline func: (Long) -> Unit): Subscription = insert().subscribe { func(it) }

fun BaseRXModel.update(databaseWrapper: DatabaseWrapper, func: (Boolean) -> Unit): Subscription = update(databaseWrapper).subscribe { func(it) }

infix inline fun BaseRXModel.update(crossinline func: (Boolean) -> Unit): Subscription = update().subscribe { func(it) }

fun BaseRXModel.delete(databaseWrapper: DatabaseWrapper, func: (Boolean) -> Unit): Subscription = delete(databaseWrapper).subscribe { func(it) }

infix inline fun BaseRXModel.delete(crossinline func: (Boolean) -> Unit): Subscription = delete().subscribe { func(it) }

// async extensions

infix inline fun <T : Any> RXModelQueriable<T>.list(crossinline func: (MutableList<T>) -> Unit): Subscription = list.subscribe { func(it) }

infix inline fun <T : Any> RXModelQueriable<T>.result(crossinline func: (T?) -> Unit): Subscription = result.subscribe { func(it) }

infix inline fun <T : Any> RXModelQueriable<T>.streamResults(crossinline func: (T?) -> Unit): Subscription = streamResults.subscribe { func(it) }

infix inline fun <T : Any> RXModelQueriable<T>.cursorResult(crossinline func: (CursorResult<T>) -> Unit): Subscription = cursorResult.subscribe { func(it) }

infix inline fun <T : Any> RXModelQueriable<T>.tableChanges(crossinline func: (ModelQueriable<T>) -> Unit): Subscription = tableChanges.subscribe { func(it) }

infix inline fun RXQueriable.statement(crossinline func: (DatabaseStatement) -> Unit): Subscription = statement.subscribe { func(it) }

infix inline fun RXQueriable.hasData(crossinline func: (Boolean) -> Unit): Subscription = hasData.subscribe { func(it) }

infix inline fun RXQueriable.cursor(crossinline func: (Cursor) -> Unit): Subscription = cursor.subscribe { func(it) }

infix inline fun RXQueriable.count(crossinline func: (Long) -> Unit): Subscription = count.subscribe { func(it) }