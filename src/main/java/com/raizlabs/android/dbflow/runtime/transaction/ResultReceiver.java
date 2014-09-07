package com.raizlabs.android.dbflow.runtime.transaction;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public interface ResultReceiver<ResultClass> {

    public void onResultReceived(ResultClass resultClass);
}
