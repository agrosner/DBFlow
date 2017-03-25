package com.raizlabs.android.dbflow.kotlinextensions

import android.content.ContentValues
import com.raizlabs.android.dbflow.config.DatabaseDefinition
import com.raizlabs.android.dbflow.config.FlowManager.*
import com.raizlabs.android.dbflow.structure.Model
import com.raizlabs.android.dbflow.structure.ModelAdapter
import com.raizlabs.android.dbflow.structure.ModelViewAdapter
import com.raizlabs.android.dbflow.structure.QueryModelAdapter
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper
import com.raizlabs.android.dbflow.structure.database.transaction.FastStoreModelTransaction.*
import com.raizlabs.android.dbflow.structure.database.transaction.ProcessModelTransaction
import com.raizlabs.android.dbflow.structure.database.transaction.ProcessModelTransaction.ProcessModel
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction

typealias ProcessFunction<T> = (T, DatabaseWrapper) -> Unit

/**
 * Easily get access to its [DatabaseDefinition] directly.
 */
inline fun <reified T> database() = getDatabase(T::class.java)

inline fun <reified T> writableDatabaseForTable() = getWritableDatabaseForTable(T::class.java)

/**
 * Easily get access to its [DatabaseDefinition] directly.
 */
inline fun <reified T> databaseForTable() = getDatabaseForTable(T::class.java)

/**
 * Easily get its table name.
 */
inline fun <reified T> tableName() = getTableName(T::class.java)

/**
 * Easily get its [ModelAdapter].
 */
inline fun <reified T> modelAdapter() = getModelAdapter(T::class.java)

/**
 * Easily get its [QueryModelAdapter].
 */
inline fun <reified T> queryModelAdapter() = getQueryModelAdapter(T::class.java)

/**
 * Easily get its [ModelViewAdapter]
 */
inline fun <reified T> modelViewAdapter() = getModelViewAdapter(T::class.java)

/**
 * Enables a collection of T objects to easily operate on them within a synchronous database transaction.
 */
inline fun <reified T : Any> Collection<T>.processInTransaction(
        crossinline processFunction: ProcessFunction<T>) {
    databaseForTable<T>().executeTransaction { db -> forEach { processFunction(it, db) } }
}

/**
 * Places the [Collection] of items on the [ITransactionQueue]. Use the [processFunction] to perform
 * an action on each individual [Model]. This happens on a non-UI thread.
 */
inline fun <reified T : Any> Collection<T>.processInTransactionAsync(
        crossinline processFunction: ProcessFunction<T>,
        success: Transaction.Success? = null,
        error: Transaction.Error? = null,
        processListener: ProcessModelTransaction.OnModelProcessListener<T>? = null) {
    databaseForTable<T>().beginTransactionAsync(
            this.processTransaction(processFunction)
                    .processListener(processListener).build())
            .success(success).error(error).execute()
}

inline fun <reified T : Any> Collection<T>.processTransaction(
        crossinline processFunction: ProcessFunction<T>): ProcessModelTransaction.Builder<T> {
    return ProcessModelTransaction.Builder<T>(ProcessModel { model, wrapper -> processFunction(model, wrapper) }).addAll(this)
}

inline fun <reified T : Any> Collection<T>.fastSave() = saveBuilder(modelAdapter<T>()).addAll(this)

inline fun <reified T : Any> Collection<T>.fastInsert() = insertBuilder(modelAdapter<T>()).addAll(this)

inline fun <reified T : Any> Collection<T>.fastUpdate() = updateBuilder(modelAdapter<T>()).addAll(this)

operator fun ContentValues.set(key: String, value: String?) = put(key, value)

operator fun ContentValues.set(key: String, value: Byte?) = put(key, value)

operator fun ContentValues.set(key: String, value: Short?) = put(key, value)

operator fun ContentValues.set(key: String, value: Int?) = put(key, value)

operator fun ContentValues.set(key: String, value: Long?) = put(key, value)

operator fun ContentValues.set(key: String, value: Float?) = put(key, value)

operator fun ContentValues.set(key: String, value: Double?) = put(key, value)

operator fun ContentValues.set(key: String, value: Boolean?) = put(key, value)

operator fun ContentValues.set(key: String, value: ByteArray?) = put(key, value)



