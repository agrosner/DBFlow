package com.raizlabs.android.dbflow.structure;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class PrimaryKeyNotFoundException extends RuntimeException {

    public PrimaryKeyNotFoundException(String detailMessage) {
        super(detailMessage);
    }
}
