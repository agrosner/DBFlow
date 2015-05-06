package com.raizlabs.android.dbflow.annotation;

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
     * @return The fully qualified query that is used for this View
     */
    String query();

    /**
     * @return The name of this view
     */
    String name() default "";

    /**
     * @return The name of the database this corresponds to. By default if one DB is defined, no need to specify the name.
     */
    String databaseName();
}
