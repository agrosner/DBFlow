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
inline fun <reified TModel : Model> database(): DatabaseDefinition {
    return FlowManager.getDatabaseForTable(TModel::class.java)
}

/**
 * Easily get its table name.
 */
inline fun <reified TModel : Model> tableName(): String {
    return FlowManager.getTableName(TModel::class.java)
}

/**
 * Easily get its [ModelAdapter].
 */
inline fun <reified TModel : Model> modelAdapter(): ModelAdapter<TModel> {
    return FlowManager.getModelAdapter(TModel::class.java)
}

/**
 * Easily get its [QueryModelAdapter].
 */
inline fun <reified TModel : BaseQueryModel> queryModelAdapter(): QueryModelAdapter<TModel> {
    return FlowManager.getQueryModelAdapter(TModel::class.java)
}


/**
 * Enables a collection of TModel objects to easily operate on them within a synchronous database transaction.
 */
inline fun <reified TModel : Model> Collection<TModel>.processInTransaction(crossinline processFunction: (TModel, DatabaseWrapper) -> Unit) {
    val wrapper = database<TModel>()
    wrapper.executeTransaction {
        forEach { processFunction(it, wrapper.writableDatabase) }
    }
}

/**
 * Places the [Collection] of items on the [ITransactionQueue]. Use the [processFunction] to perform
 * an action on each individual [Model]. This happens on a non-UI thread.
 */
inline fun <reified TModel : Model> Collection<TModel>.processInTransactionAsync(crossinline processFunction: (TModel, DatabaseWrapper) -> Unit) {
    val wrapper = database<TModel>()
    wrapper.beginTransactionAsync(
            ProcessModelTransaction.Builder(ProcessModelTransaction.ProcessModel<TModel> {
                processFunction(it, wrapper.writableDatabase)
            }).addAll(this).build()
    ).build().execute();
}

/**
 * Places the [Collection] of items on the [ITransactionQueue]. Use the [processFunction] to perform
 * an action on each individual [Model]. This happens on a non-UI thread.
 */
inline fun <reified TModel : Model> Collection<TModel>.processInTransactionAsync(crossinline processFunction: (TModel, DatabaseWrapper) -> Unit,
                                                                                 success: Transaction.Success? = null,
                                                                                 error: Transaction.Error? = null) {
    val wrapper = database<TModel>()
    wrapper.beginTransactionAsync(
            ProcessModelTransaction.Builder(ProcessModelTransaction.ProcessModel<TModel> {
                processFunction(it, wrapper.writableDatabase)
            }).addAll(this).build()
    ).success(success).error(error).build().execute();
}

/**
 * Places the [Collection] of items on the [ITransactionQueue]. Use the [processFunction] to perform
 * an action on each individual [Model]. This happens on a non-UI thread.
 */
inline fun <reified TModel : Model> Collection<TModel>.processInTransactionAsync(crossinline processFunction: (TModel, DatabaseWrapper) -> Unit,
                                                                                 processListener: ProcessModelTransaction.OnModelProcessListener<TModel>? = null,
                                                                                 success: Transaction.Success? = null,
                                                                                 error: Transaction.Error? = null) {
    val wrapper = database<TModel>()
    wrapper.beginTransactionAsync(
            ProcessModelTransaction.Builder(ProcessModelTransaction.ProcessModel<TModel> {
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