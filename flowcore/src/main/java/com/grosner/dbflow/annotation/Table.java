package com.grosner.dbflow.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Author: andrewgrosner
 * Description: An optional annotation for {@link com.grosner.dbflow.structure.Model} classes
 * that allow a different name than the Model's class name.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {

    /**
     * Specifies a different name for the table
     *
     * @return
     */
    String name();
}
