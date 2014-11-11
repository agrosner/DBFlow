package com.grosner.dbflow.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author: andrewgrosner
 * Description: Marks a class as being a table for only ONE DB. It must implement the Model interface and all fields MUST be package private.
 * This will generate a $Table and $Adapter class. The $Table class generates static final column name variables to reference in queries.
 * The $Adapter class defines how to retrieve and store this object as well as other methods for acting on model objects in the database.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Table {

    /**
     * Specifies a different name for the table
     *
     * @return
     */
    String value() default "";

    /**
     * Specify the database name that this table belongs to. By default it will reference the main Db.
     * @return
     */
    String databaseName() default "";
}
