package com.raizlabs.android.dbflow.runtime.transaction;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Provides a simple callback interface that returns a result. This result is on the main UI thread
 * when completed.
 */
public interface ResultReceiver<ResultClass> {

    /**
     * The result of the transaction is placed here on the UI thread. For retrievals, it returns the
     * list models or single model item. For any other operation it will return the {@link ResultClass} that was
     * included in the main {@link com.raizlabs.android.dbflow.runtime.transaction.BaseResultTransaction}
     *
     * @param resultClass The result that we will return
     */
    public void onResultReceived(ResultClass resultClass);
}
