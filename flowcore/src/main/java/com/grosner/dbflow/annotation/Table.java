package com.grosner.dbflow.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author: andrewgrosner
 * Description: An optional annotation for {@link com.grosner.dbflow.structure.Model} classes
 * that allow a different name than the Model's class name.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Table {

    /**
     * Specifies a different name for the table
     *
     * @return
     */
    String name() default "";
}
