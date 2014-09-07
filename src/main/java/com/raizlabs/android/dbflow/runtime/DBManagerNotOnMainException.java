package com.raizlabs.android.dbflow.runtime;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class DBManagerNotOnMainException extends RuntimeException {

    public DBManagerNotOnMainException(String detailMessage) {
        super(detailMessage);
    }
}
