package com.grosner.dbflow.runtime.transaction;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public interface ResultReceiver<ResultClass> {

    public void onResultReceived(ResultClass resultClass);
}
