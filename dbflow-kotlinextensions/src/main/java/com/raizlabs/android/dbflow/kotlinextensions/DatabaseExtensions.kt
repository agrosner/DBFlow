package com.raizlabs.android.dbflow.kotlinextensions

import android.content.ContentValues
import com.raizlabs.android.dbflow.SQLiteCompatibilityUtils
import com.raizlabs.android.dbflow.config.BaseDatabaseDefinition
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.runtime.TransactionManager
import com.raizlabs.android.dbflow.structure.Model
import com.raizlabs.android.dbflow.structure.ModelAdapter
import com.raizlabs.android.dbflow.structure.container.ModelContainerAdapter
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper
import kotlin.reflect.KClass

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
 * Enables any model class in kotlin to access its database directly.
 */
fun <TModel : Model> KClass<TModel>.database(): BaseDatabaseDefinition {
    return FlowManager.getDatabaseForTable(java)
}

/**
 * Enables any model class in java to access its database directly.
 */
fun <TModel : Model> Class<TModel>.database(): BaseDatabaseDefinition {
    return FlowManager.getDatabaseForTable(this)
}

/**
 * Any kotlin model class can get its tablename.
 */
fun <TModel : Model> KClass<TModel>.tableName(): String {
    return FlowManager.getTableName(java)
}

/**
 * Any java model class can get its tablename.
 */
fun <TModel : Model> Class<TModel>.tableName(): String {
    return FlowManager.getTableName(this)
}

/**
 * Any kotlin model class can get its ModelAdapter.
 */
fun <TModel : Model> KClass<TModel>.modelAdapter(): ModelAdapter<TModel> {
    return FlowManager.getModelAdapter(java)
}

/**
 * Any kotlin model class can get its ModelAdapter.
 */
fun <TModel : Model> Class<TModel>.modelAdapter(): ModelAdapter<TModel> {
    return FlowManager.getModelAdapter(this)
}

/**
 * Any kotlin model class can get its ContainerAdapter.
 */
fun <TModel : Model> KClass<TModel>.containerAdapter(): ModelContainerAdapter<TModel> {
    return FlowManager.getContainerAdapter(java)
}

/**
 * Any java model class can get its ContainerAdapter.
 */
fun <TModel : Model> Class<TModel>.containerAdapter(): ModelContainerAdapter<TModel> {
    return FlowManager.getContainerAdapter(this)
}

/**
 * Enables a collection of TModel objects to easily operate on them within a DB transaction.
 */
inline fun <reified TModel : Model> Collection<TModel>.processInTransaction(crossinline processFunction: (TModel) -> Unit) {
    TransactionManager.transact(FlowManager.getDatabaseForTable(TModel::class.java).writableDatabase) {
        forEach {
            processFunction(it)
        }
    }
}

fun DatabaseWrapper.executeUpdateDelete(rawQuery: String) {
    SQLiteCompatibilityUtils.executeUpdateDelete(this, rawQuery)
}

fun DatabaseWrapper.updateWithOnConflict(tableName: String, contentValues: ContentValues, where: String?, whereArgs: Array<String>?, conflictAlgorithm: Int): Long {
    return SQLiteCompatibilityUtils.updateWithOnConflict(this, tableName, contentValues, where, whereArgs, conflictAlgorithm)
}
