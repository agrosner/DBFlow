package com.grosner.dbflow.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Marks a class as being a ModelView
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface ModelView {

    /**
     * The fully qualified query that is used for this View
     * @return
     */
    String query();

    /**
     * The name of this view
     * @return
     */
    String name() default "";

    String databaseName() default "";
}
