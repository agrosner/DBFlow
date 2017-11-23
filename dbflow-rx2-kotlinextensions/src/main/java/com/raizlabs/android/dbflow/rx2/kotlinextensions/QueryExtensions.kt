package com.raizlabs.android.dbflow.rx2.kotlinextensions

import android.database.Cursor
import com.raizlabs.android.dbflow.rx2.language.RXModelQueriable
import com.raizlabs.android.dbflow.rx2.language.RXQueriable
import com.raizlabs.android.dbflow.rx2.language.RXSQLite
import com.raizlabs.android.dbflow.rx2.structure.BaseRXModel
import com.raizlabs.android.dbflow.sql.language.CursorResult
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable
import com.raizlabs.android.dbflow.sql.queriable.Queriable
import com.raizlabs.android.dbflow.structure.database.DatabaseStatement
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper
import io.reactivex.disposables.Disposable

fun <T : Any> ModelQueriable<T>.rx() = RXSQLite.rx<T>(this)

inline fun <reified T : Any> Queriable.rx() = RXSQLite.rx(T::class.java, this)

// queriable extensions

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

fun BaseRXModel.save(databaseWrapper: DatabaseWrapper, func: (Boolean) -> Unit): Disposable = save(databaseWrapper).subscribe { success -> func(success) }

infix inline fun BaseRXModel.save(crossinline func: (Boolean) -> Unit): Disposable = save().subscribe { success -> func(success) }

fun BaseRXModel.insert(databaseWrapper: DatabaseWrapper, func: (Long) -> Unit): Disposable = insert(databaseWrapper).subscribe { rowId -> func(rowId) }

infix inline fun BaseRXModel.insert(crossinline func: (Long) -> Unit): Disposable = insert().subscribe { rowId -> func(rowId) }

fun BaseRXModel.update(databaseWrapper: DatabaseWrapper, func: (Boolean) -> Unit): Disposable = update(databaseWrapper).subscribe { success -> func(success) }

infix inline fun BaseRXModel.update(crossinline func: (Boolean) -> Unit): Disposable = update().subscribe { success -> func(success) }

fun BaseRXModel.delete(databaseWrapper: DatabaseWrapper, func: (Boolean) -> Unit): Disposable = delete(databaseWrapper).subscribe { success -> func(success) }

infix inline fun BaseRXModel.delete(crossinline func: (Boolean) -> Unit): Disposable = delete().subscribe { success -> func(success) }

// async extensions

infix inline fun <T : Any> RXModelQueriable<T>.list(crossinline func: (MutableList<T>) -> Unit): Disposable = list.subscribe { modelList -> func(modelList) }

infix inline fun <T : Any> RXModelQueriable<T>.result(crossinline func: (T?) -> Unit): Disposable = result.subscribe { result -> func(result) }

infix inline fun <T : Any> RXModelQueriable<T>.streamResults(crossinline func: (T?) -> Unit): Disposable = streamResults.subscribe { model -> func(model) }

infix inline fun <T : Any> RXModelQueriable<T>.cursorResult(crossinline func: (CursorResult<T>) -> Unit): Disposable = cursorResult.subscribe { result -> func(result) }

infix inline fun <T : Any> RXModelQueriable<T>.tableChanges(crossinline func: (ModelQueriable<T>) -> Unit): Disposable = tableChanges.subscribe { queriable -> func(queriable) }

infix inline fun RXQueriable.statement(crossinline func: (DatabaseStatement) -> Unit): Disposable = statement.subscribe { statement -> func(statement) }

infix inline fun RXQueriable.hasData(crossinline func: (Boolean) -> Unit): Disposable = hasData.subscribe { hasData -> func(hasData) }

infix inline fun RXQueriable.cursor(crossinline func: (Cursor) -> Unit): Disposable = cursor.subscribe { cursor -> func(cursor) }
