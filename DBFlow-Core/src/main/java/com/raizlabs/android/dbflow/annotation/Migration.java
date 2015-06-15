package com.raizlabs.android.dbflow.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description: Marks a Migration class to be included in DB construction. The class using this annotation
 * must implement the Migration interface.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Migration {

    /**
     * @return The version the migration will trigger at.
     */
    int version();

    /**
     * @return Specify the database name that this migration belongs to. By default it will reference the main Db if only
     * one DB is specified.
     */
    String databaseName();

}
