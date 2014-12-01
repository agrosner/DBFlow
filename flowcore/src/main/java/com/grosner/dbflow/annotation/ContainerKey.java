package com.grosner.dbflow.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author: andrewgrosner
 * Description: Add this to a {@link com.grosner.dbflow.annotation.Column}
 *  field to specify the key for a ModelContainer object that the column references. It
 *  enables different key than column name.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface ContainerKey {

    /**
     * @return The key that we use to retrieve the value from the ModelContainer
     */
    String value();
}
