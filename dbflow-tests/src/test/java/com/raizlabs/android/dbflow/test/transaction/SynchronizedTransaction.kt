package com.raizlabs.android.dbflow.test.transaction

import com.raizlabs.android.dbflow.structure.database.transaction.Transaction

import java.util.concurrent.CountDownLatch

/**
 * Description: Blocks calling thread until transaction completes. Great for testing.
 */
class SynchronizedTransaction(transaction: Transaction.Builder) {

    private val transaction: Transaction
    private var successCallback: Transaction.Success? = null

    private val countDownLatch = CountDownLatch(1)

    private val errorCallback = Transaction.Error { transaction, error -> countDownLatch.countDown() }

    private val success = Transaction.Success { transaction ->
        countDownLatch.countDown()
        if (successCallback != null) {
            successCallback!!.onSuccess(transaction)
        }
    }

    init {
        this.transaction = transaction.runCallbacksOnSameThread(true)
            .error(errorCallback).success(success).build()
    }

    fun successCallback(successCallback: Transaction.Success): SynchronizedTransaction {
        this.successCallback = successCallback
        return this
    }

    fun execute() {
        transaction.execute()
        try {
            countDownLatch.await()
        } catch (e: InterruptedException) {
            errorCallback.onError(transaction, e)
        }

    }
}
