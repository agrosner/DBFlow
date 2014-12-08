package com.raizlabs.android.dbflow.sql.builder;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: A simple exception to signal primary key cannot be null.
 */
public class PrimaryKeyCannotBeNullException extends Throwable {

    public PrimaryKeyCannotBeNullException(String s) {
        super(s);
    }

}
