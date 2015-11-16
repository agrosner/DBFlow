package com.raizlabs.android.dbflow.structure;

/**
 * Description: Thrown when a DB is incorrectly configured.
 */
public class InvalidDBConfiguration extends RuntimeException {

    public InvalidDBConfiguration() {
        super("No Databases were found. Did you create a Database Annotation placeholder class?");
    }

    public InvalidDBConfiguration(String message) {
        super(message);
    }
}
