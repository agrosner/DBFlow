package com.raizlabs.android.dbflow.test.transaction;

import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;

import java.util.concurrent.CountDownLatch;

/**
 * Description: Blocks calling thread until transaction completes. Great for testing.
 */
public class SynchronizedTransaction {

    private final Transaction transaction;
    private Transaction.Success successCallback;

    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    public SynchronizedTransaction(Transaction.Builder transaction) {
        this.transaction = transaction.runCallbacksOnSameThread(true)
            .error(errorCallback).success(success).build();
    }

    public SynchronizedTransaction successCallback(Transaction.Success successCallback) {
        this.successCallback = successCallback;
        return this;
    }

    public void execute() {
        transaction.execute();
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            errorCallback.onError(transaction, e);
        }
    }

    private final Transaction.Error errorCallback = new Transaction.Error() {
        @Override
        public void onError(Transaction transaction, Throwable error) {
            countDownLatch.countDown();
        }
    };

    private final Transaction.Success success = new Transaction.Success() {
        @Override
        public void onSuccess(Transaction transaction) {
            countDownLatch.countDown();
            if (successCallback != null) {
                successCallback.onSuccess(transaction);
            }
        }
    };


}
