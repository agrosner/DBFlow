package com.raizlabs.android.dbflow.kotlinextensions

import com.raizlabs.android.dbflow.config.BaseDatabaseDefinition
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.runtime.TransactionManager
import com.raizlabs.android.dbflow.structure.Model
import kotlin.reflect.KClass

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