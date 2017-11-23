package com.raizlabs.android.dbflow.sql

import com.raizlabs.android.dbflow.config.DatabaseDefinition
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.structure.database.transaction.ITransaction
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction
import com.raizlabs.android.dbflow.structure.database.transaction.transactionError
import com.raizlabs.android.dbflow.structure.database.transaction.transactionSuccess

/**
 * Description: Internal use to provide common implementation for async objects.
 */
open class BaseAsyncObject<out TAsync>(open val table: Class<*>) {

    private var successCallback: Transaction.Success? = null
    private var errorCallback: Transaction.Error? = null
    private var currentTransaction: Transaction? = null
    private val databaseDefinition: DatabaseDefinition by lazy { FlowManager.getDatabaseForTable(table) }

    private val error = transactionError { transaction, error ->
        errorCallback?.onError(transaction, error)
        this@BaseAsyncObject.onError(transaction, error)
        currentTransaction = null
    }

    private val success = transactionSuccess { transaction ->
        successCallback?.onSuccess(transaction)
        this@BaseAsyncObject.onSuccess(transaction)
        currentTransaction = null
    }

    /**
     * Listen for any errors that occur during operations on this [TAsync].
     */
    fun error(errorCallback: Transaction.Error?): TAsync {
        this.errorCallback = errorCallback
        return this as TAsync
    }

    /**
     * Listens for successes on this [TAsync]. Will return the [Transaction].
     */
    fun success(success: Transaction.Success?): TAsync {
        this.successCallback = success
        return this as TAsync
    }

    /**
     * Cancels current running transaction.
     */
    fun cancel() {
        currentTransaction?.cancel()
    }

    protected fun executeTransaction(transaction: ITransaction) {
        cancel()
        currentTransaction = databaseDefinition
                .beginTransactionAsync(transaction)
                .error(error)
                .success(success)
                .build()
        currentTransaction?.execute()
    }

    protected fun onError(transaction: Transaction, error: Throwable) = Unit

    protected open fun onSuccess(transaction: Transaction) = Unit
}
