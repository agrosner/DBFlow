package com.grosner.dbflow.runtime;

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
