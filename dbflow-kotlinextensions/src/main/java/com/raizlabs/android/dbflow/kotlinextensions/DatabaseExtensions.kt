package com.raizlabs.android.dbflow.kotlinextensions

import com.raizlabs.android.dbflow.config.DatabaseDefinition
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.structure.BaseQueryModel
import com.raizlabs.android.dbflow.structure.Model
import com.raizlabs.android.dbflow.structure.ModelAdapter
import com.raizlabs.android.dbflow.structure.QueryModelAdapter
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper
import com.raizlabs.android.dbflow.structure.database.transaction.ProcessModelTransaction
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction

/**
 * Easily get access to its [DatabaseDefinition] directly.
 */
inline fun <reified T : Any> database(): DatabaseDefinition {
    return FlowManager.getDatabaseForTable(T::class.java)
}

/**
 * Easily get its table name.
 */
inline fun <reified T : Any> tableName(): String {
    return FlowManager.getTableName(T::class.java)
}

/**
 * Easily get its [ModelAdapter].
 */
inline fun <reified T : Any> modelAdapter(): ModelAdapter<T> {
    return FlowManager.getModelAdapter(T::class.java)
}

/**
 * Easily get its [QueryModelAdapter].
 */
inline fun <reified T : BaseQueryModel> queryModelAdapter(): QueryModelAdapter<T> {
    return FlowManager.getQueryModelAdapter(T::class.java)
}


/**
 * Enables a collection of T objects to easily operate on them within a synchronous database transaction.
 */
inline fun <reified T : Any> Collection<T>.processInTransaction(crossinline processFunction: (T, DatabaseWrapper) -> Unit) {
    val wrapper = database<T>()
    wrapper.executeTransaction {
        forEach { processFunction(it, wrapper.writableDatabase) }
    }
}

/**
 * Places the [Collection] of items on the [ITransactionQueue]. Use the [processFunction] to perform
 * an action on each individual [Model]. This happens on a non-UI thread.
 */
inline fun <reified T : Any> Collection<T>.processInTransactionAsync(crossinline processFunction: (T, DatabaseWrapper) -> Unit) {
    val wrapper = database<T>()
    wrapper.beginTransactionAsync(
            ProcessModelTransaction.Builder(ProcessModelTransaction.ProcessModel<T> {
                processFunction(it, wrapper.writableDatabase)
            }).addAll(this).build()
    ).build().execute();
}

/**
 * Places the [Collection] of items on the [ITransactionQueue]. Use the [processFunction] to perform
 * an action on each individual [Model]. This happens on a non-UI thread.
 */
inline fun <reified T : Any> Collection<T>.processInTransactionAsync(crossinline processFunction: (T, DatabaseWrapper) -> Unit,
                                                                     success: Transaction.Success? = null,
                                                                     error: Transaction.Error? = null) {
    val wrapper = database<T>()
    wrapper.beginTransactionAsync(
            ProcessModelTransaction.Builder(ProcessModelTransaction.ProcessModel<T> {
                processFunction(it, wrapper.writableDatabase)
            }).addAll(this).build()
    ).success(success).error(error).build().execute();
}

/**
 * Places the [Collection] of items on the [ITransactionQueue]. Use the [processFunction] to perform
 * an action on each individual [Model]. This happens on a non-UI thread.
 */
inline fun <reified T : Any> Collection<T>.processInTransactionAsync(crossinline processFunction: (T, DatabaseWrapper) -> Unit,
                                                                     processListener: ProcessModelTransaction.OnModelProcessListener<T>? = null,
                                                                     success: Transaction.Success? = null,
                                                                     error: Transaction.Error? = null) {
    val wrapper = database<T>()
    wrapper.beginTransactionAsync(
            ProcessModelTransaction.Builder(ProcessModelTransaction.ProcessModel<T> {
                processFunction(it, wrapper.writableDatabase)
            }).addAll(this).processListener(processListener).build()
    ).success(success).error(error).build().execute();
}

/**
 * Adds the execute method to the [DatabaseWrapper]
 */
fun DatabaseWrapper.executeUpdateDelete(rawQuery: String) {
    compileStatement(rawQuery).executeUpdateDelete()
}