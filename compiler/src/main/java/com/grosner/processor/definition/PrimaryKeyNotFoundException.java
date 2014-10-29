package com.grosner.processor.definition;

/**
 * Author: andrewgrosner
 * Description: The exception called when a primary key is not found for a {@link com.grosner.dbflow.structure.Model} class.
 */
public class PrimaryKeyNotFoundException extends RuntimeException {

    public PrimaryKeyNotFoundException(String detailMessage) {
        super(detailMessage);
    }
}
