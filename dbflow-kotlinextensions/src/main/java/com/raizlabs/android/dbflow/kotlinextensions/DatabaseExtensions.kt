package com.raizlabs.android.dbflow.kotlinextensions

import android.content.ContentValues
import com.raizlabs.android.dbflow.SQLiteCompatibilityUtils
import com.raizlabs.android.dbflow.config.BaseDatabaseDefinition
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.runtime.TransactionManager
import com.raizlabs.android.dbflow.runtime.transaction.BaseTransaction
import com.raizlabs.android.dbflow.runtime.transaction.process.ProcessModelInfo
import com.raizlabs.android.dbflow.runtime.transaction.process.ProcessModelTransaction
import com.raizlabs.android.dbflow.structure.BaseQueryModel
import com.raizlabs.android.dbflow.structure.Model
import com.raizlabs.android.dbflow.structure.ModelAdapter
import com.raizlabs.android.dbflow.structure.QueryModelAdapter
import com.raizlabs.android.dbflow.structure.container.ModelContainerAdapter
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper

/**
 * Allows you to do something on the database.
 */
inline fun BaseDatabaseDefinition.transact(transaction: () -> Unit) {
    writableDatabase.beginTransaction()
    try {
        transaction()
        writableDatabase.setTransactionSuccessful()
    } finally {
        writableDatabase.endTransaction()
    }
}

/**
 * Easily get access to its [BaseDatabaseDefinition] directly.
 */
inline fun <reified TModel : Model> database(): BaseDatabaseDefinition {
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
 * Easily get its [ModelContainerAdapter].
 */
inline fun <reified TModel : Model> containerAdapter(): ModelContainerAdapter<TModel> {
    return FlowManager.getContainerAdapter(TModel::class.java)
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
inline fun <reified TModel : Model> Collection<TModel>.processInTransaction(crossinline processFunction: (TModel) -> Unit) {
    database<TModel>().transact {
        forEach { processFunction(it) }
    }
}

/**
 * Places the [Collection] of items on the [DBTransactionQueue]. Use the [processFunction] to perform
 * an action on each individual [Model]. This happens on a non-UI thread.
 */
inline fun <TModel : Model> Collection<TModel>.processInTransactionAsync(crossinline processFunction: (TModel) -> Unit) {
    object : ProcessModelTransaction<TModel>(ProcessModelInfo.withModels(this), null) {
        override fun processModel(model: TModel) {
            processFunction(model)
        }
    }.transact()
}

/**
 * Transacts a [BaseTransaction] at the end for cleaner feel.
 */
fun <TResult> BaseTransaction<TResult>.transact() {
    TransactionManager.getInstance().addTransaction(this)
}

/**
 * Adds the execute method to the [DatabaseWrapper]
 */
fun DatabaseWrapper.executeUpdateDelete(rawQuery: String) {
    SQLiteCompatibilityUtils.executeUpdateDelete(this, rawQuery)
}

/**
 * Adds the update method to the [DatabaseWrapper]
 */
fun DatabaseWrapper.updateWithOnConflict(tableName: String, contentValues: ContentValues, where: String?, whereArgs: Array<String>?, conflictAlgorithm: Int): Long {
    return SQLiteCompatibilityUtils.updateWithOnConflict(this, tableName, contentValues, where, whereArgs, conflictAlgorithm)
}
