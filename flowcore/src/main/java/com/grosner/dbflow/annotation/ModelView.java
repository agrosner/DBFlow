package com.grosner.dbflow.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author: andrewgrosner
 * Description: Marks a class as being an SQL VIEW definition. It must extend BaseModelView.
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

    /**
     * The name of the database this corresponds to. By default if one DB is defined, no need to specify the name.
     * @return
     */
    String databaseName() default "";
}
