package com.grosner.dbflow.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Migration {

    int version();

    /**
     * Specify the database name that this migration belongs to. By default it will reference the main Db.
     * @return
     */
    String databaseName() default "";

}
